package component

import kotlinext.js.jso
import kotlinx.html.INPUT
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.dom.events.Event
import react.*
import react.dom.*
import react.query.useMutation
import react.query.useQuery
import react.query.useQueryClient
import react.router.useParams
import server.model.Config
import server.model.Item
import server.model.Lesson
import server.model.Student
import wrappers.QueryError
import wrappers.axios
import wrappers.fetchText
import kotlin.js.json

external interface StudentProps : Props {
    var student: Item<Student>
    var lessons: List<Item<Lesson>>
    var updateStudent: (String, String, String) -> Unit
}

fun fcStudent() = fc("Student") { p: StudentProps ->
    val firstnameRef = useRef<INPUT>()
    val surnameRef = useRef<INPUT>()
    val groupRef = useRef<INPUT>()

    val (firstname, setFirstname) = useState(p.student.elem.firstname)
    val (surname, setSurname) = useState(p.student.elem.surname)
    val (group, setGroup) = useState(p.student.elem.group)

    fun changeOnEdit(setter: StateSetter<String>, ref: MutableRefObject<INPUT>) = { _: Event ->
        setter(ref.current?.value ?: "ERROR!")
    }

    div {
        h4 { +"Profile editor" }
        p {
            +"Firstname: "
            input {
                ref = firstnameRef
                attrs.value = firstname
                attrs.onChangeFunction = changeOnEdit(setFirstname, firstnameRef)
            }
        }
        p {
            +"Surname: "
            input {
                ref = surnameRef
                attrs.value = surname
                attrs.onChangeFunction = changeOnEdit(setSurname, surnameRef)
            }
        }
        p {
            +"Group: "
            input {
                ref = groupRef
                attrs.value = group
                attrs.onChangeFunction = changeOnEdit(setGroup, groupRef)
            }
            button {
                +"Update profile"
                attrs.onClickFunction = {
                    firstnameRef.current?.value?.let { firstname ->
                        surnameRef.current?.value?.let { surname ->
                            groupRef.current?.value?.let { group ->
                                p.updateStudent(firstname, surname, group)
                            }
                        }
                    }
                }
            }
        }
    }
    div {
        h4 { +"Prescribed lessons:" }
        ul {
            val filteredLessons =
                p.lessons.filter { it.elem.students.toString().contains(p.student.elem.fullID) }
            /**.contains() function is deprecated in JS, there is no alternative to iterate over OBJECT
            -> convert object to string and use RegEx to filter elements out**/
            if (filteredLessons.isEmpty()) li { +"empty" }
            else {
                filteredLessons.map {
                    li {
                        a {
                            attrs.href = "http://localhost:8000/#/lessons/${it.uuid}/details"
                            +it.elem.name
                        }
                    }
                }
            }
        }
    }
}

fun fcContainerStudent() = fc("ContainerStudent") { _: Props ->
    val queryClient = useQueryClient()
    val studentParams = useParams()
    val studentId = studentParams["id"] ?: "Route param error"

    val queryStudent = useQuery<String, QueryError, String, String>(
        studentId, { fetchText(Config.studentsURL + studentId) })
    val queryLessons = useQuery<String, QueryError, String, String>(
        "lessonList", { fetchText(Config.lessonsURL) })

    val updateStudentMutation = useMutation<Any, Any, Student, Any>({ elem ->
            axios<String>(jso {
                url = "${Config.studentsURL}/$studentId"
                method = "Put"
                headers = json("Content-Type" to "application/json",)
                data = JSON.stringify(elem)
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(studentId)
            }
        }
    )
    //changing student data will cause loss of all lessons,
    //however, old student still be in lessons data repository;
    //(this can be changed anytime on project backend)
    //see notes in studentList, teacher components (similar queries)

    if (queryStudent.isLoading or queryLessons.isLoading)
        div { +"Loading ..." }
    else if (queryStudent.isError or queryLessons.isError)
        div { +"Query error. Please contact server administrator at: admin@adminmail." }
    else {
        val student: ClientItemStudent = Json.decodeFromString(queryStudent.data?:"")
        val lessons: List<ClientItemLesson> = Json.decodeFromString(queryLessons.data?:"")
        child(fcStudent()) {
            attrs.student = student
            attrs.lessons = lessons
            attrs.updateStudent = { fn, sn, g ->
                updateStudentMutation.mutate(Student(fn, sn, g), null)
            }
        }
    }
}
