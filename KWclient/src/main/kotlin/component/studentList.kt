package component

import kotlinext.js.jso
import kotlinx.browser.window
import kotlinx.html.INPUT
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import react.Props
import react.dom.*
import react.fc
import react.query.useMutation
import react.query.useQuery
import react.query.useQueryClient
import react.router.dom.Link
import react.useRef
import server.model.Config.Companion.studentsURL
import server.model.Item
import server.model.Student
import wrappers.QueryError
import wrappers.axios
import wrappers.fetchText
import kotlin.js.json

external interface StudentListProps : Props {
    var students: List<Item<Student>>
    var addStudent: (String, String, String) -> Unit
    var rmStudent: (Int) -> Unit
}

fun fcStudentList() = fc("StudentList") { props: StudentListProps ->
    val firstnameRef = useRef<INPUT>()
    val surnameRef = useRef<INPUT>()
    val groupRef = useRef<INPUT>()
    div {
        h4 { +"Add student:" }
        input { ref = firstnameRef; attrs.placeholder = "Firstname" }
        input { ref = surnameRef; attrs.placeholder = "Surname" }
        input { ref = groupRef; attrs.placeholder = "Group" }
        //.placeholder == pseudo-value/input tip
        button {
            +"+"
            attrs.onClickFunction = {
                firstnameRef.current?.value?.let { firstname ->
                    surnameRef.current?.value?.let { surname ->
                        groupRef.current?.value?.let { group ->
                            if (firstname.isBlank() || group.isBlank())
                                window.alert("<Add student>: 'Firstname' and 'Group' " +
                                        "fields must not be empty!")
                            else props.addStudent(firstname, surname, group)
                        }
                    }
                }
            }
        }
    }

    h3 { +"Students" }
    ol {
        props.students.sortedByDescending { it.elem.group }.mapIndexed { index, studentItem ->
            li {
                val student =
                    Student(studentItem.elem.firstname, studentItem.elem.surname, studentItem.elem.group)
                //required in order to access 'get' fields; otherwise all fields must be accessed directly, i.e.:
                //"${studentItem.elem.firstname} ${studentItem.elem.surname}" -- .fullID, .shortID not going to work
                Link {
                    +student.shortID
                    attrs.to = "/students/${studentItem.uuid}"
                }
                +" | "
                Link {
                    attrs.to = "/groups/${student.group}"
                    +student.group
                }
                +" "
                button {
                    +"rm"
                    attrs.onClickFunction = {
                        props.rmStudent(index)
                    }
                }
            }
        }
    }
}

@Serializable
class ClientItemStudent(
    override val elem: Student,
    override val uuid: String,
    override val etag: Long
) : Item<Student>

fun fcContainerStudentList() = fc("QueryStudentList") { _: Props ->
    val queryClient = useQueryClient()

    /*
    val query = useQuery<Any, QueryError, AxiosResponse<Array<Item<Student>>>, Any>(
        "studentList", {
            axios<Array<Student>>(jso { url = studentsURL }) })
    */
    val query = useQuery<String, QueryError, String, String>(
        "studentList", { fetchText(studentsURL) })


    val addStudentMutation = useMutation<Any, Any, Any, Any>({ student: Student ->
            axios<String>(jso {
                url = studentsURL
                method = "Post"
                headers = json("Content-Type" to "application/json")
                data = JSON.stringify(student)
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("studentList")
            }
        }
    )

    val rmStudentMutation = useMutation<Any, Any, Any, Any>({ studentItem: Item<Student> ->
            axios<String>(jso {
                url = "$studentsURL/${studentItem.uuid}"
                method = "Delete"
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("studentList")
            }
        }
    )

    if (query.isLoading)
        div { +"Loading ..." }
    else if (query.isError)
        div { +"Query error. Please contact server administrator at: admin@adminmail." }
    else {
        //val students = query.data?.data?.toList() ?: emptyList()
        val students: List<ClientItemStudent> = Json.decodeFromString(query.data?:"")
        child(fcStudentList()) {
            attrs.students = students
            attrs.addStudent = { fn, sn, g ->
                addStudentMutation.mutate(Student(fn, sn, g), null)
            }
            attrs.rmStudent = {
                rmStudentMutation.mutate(students[it], null)
            }
        }
    }
}