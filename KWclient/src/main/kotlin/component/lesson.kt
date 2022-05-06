package component


import kotlinext.js.jso
import kotlinx.browser.window
import kotlinx.html.INPUT
import kotlinx.html.SELECT
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.events.Event
import react.*
import react.dom.*
import react.query.useMutation
import react.query.useQuery
import react.query.useQueryClient
import react.router.useParams
import server.model.*
import wrappers.QueryError
import wrappers.axios
import wrappers.fetchText
import kotlin.js.json

external interface LessonProps : Props {
    var lesson: Item<Lesson>
    var students: List<Item<Student>>
    var teachers: List<Item<Teacher>>
    var updateLesson: (String, String, Int) -> Unit //by name, type, hours
    var addTeacher: (String) -> Unit //by uuid
    var rmTeacher: (String) -> Unit //by name
    var addStudent: (String) -> Unit //by uuid
    var rmStudent: (String) -> Unit //by name
}

interface SelectedElement { val value: String }

//serializers for Item<E>
//required by QUERY to function correctly (see note below)
@Serializable
class ClientItemLesson(
    override val elem: Lesson,
    override val uuid: String,
    override val etag: Long
) : Item<Lesson>

/**
 * this level of abstraction:
 * @Serializable
 * class ClientItemE<E>(
 *     override val elem: E,
 *     override val uuid: String,
 *     override val etag: Long
 * ): Item<E>
 * is not going to work;
 * ClientItem requires <E> in order to work,
 * which breaks entire idea of using <ClintItemElement>
 * @see: /kotlinx.serialization.json/ documentation
**/

fun fcLesson() = fc("Lesson") { lp: LessonProps ->
    val lessonNameRef = useRef<INPUT>()
    val lessonHoursRef = useRef<INPUT>()
    //HTML drop-down lists for student adding && removal FROM selected lesson
    val lessonTypeRef = useRef<SELECT>()
    val lessonAddTeacherRef = useRef<SELECT>()
    val lessonRmTeacherRef = useRef<SELECT>()
    val studentSelectAddRef = useRef<SELECT>()
    val studentSelectRmRef = useRef<SELECT>()

    val (name, setName) = useState(lp.lesson.elem.name)
    //type reference require separate/additional function (not included in current version)
    val (hours, setHours) = useState(lp.lesson.elem.totalHours.toString())

    fun changeOnEdit(setter: StateSetter<String>, ref: MutableRefObject<INPUT>) = { _: Event ->
        setter(ref.current?.value ?: "ERROR!")
    }

    div {
        h4 { +"Lesson editor: " }
        p {
            +"Name: "
            input {
                ref = lessonNameRef
                attrs.value = name
                attrs.onChangeFunction = changeOnEdit(setName, lessonNameRef)
            }
        }
        //lesson types are fixed/final
        p {
            +"Type: "
            select {
                ref = lessonTypeRef
                listOf("Lecture", "Lab", "Practice")
                    .sortedBy { it != lp.lesson.elem.type }
                    //puts lesson-defined *Type* first on the list
                    .map {
                        option {
                            attrs.value = it
                            +it
                        }
                    }
            }
        }
        p {
            +"Hours total (semester): "
            input {
                ref = lessonHoursRef
                attrs.value = hours
                attrs.onChangeFunction = changeOnEdit(setHours, lessonHoursRef)
            }
            button {
                +"Update lesson"
                attrs.onClickFunction = {
                    lessonNameRef.current?.value?.let { lessonName ->
                        val selType = lessonTypeRef.current.unsafeCast<SelectedElement>()
                        //selection is never empty
                        if (selType.value == "" || selType.value == " ")
                            //gonna check value regardless...
                            window.alert("<Update lesson>: 'Type' field must not be empty!")
                        else
                            lessonHoursRef.current?.value?.let { lessonHours ->
                                val lhToInt = lessonHours.toIntOrNull()
                                if (lhToInt == null)
                                    window.alert("<Update lesson>: 'Hours total (semester)'" +
                                            "field must be a number!")
                                else
                                    lp.updateLesson(lessonName, selType.value, lhToInt)
                            }
                    }
                }
            }
        }
    }
    div {
        p {
            +"Add teacher to lesson: "
            select {
                ref = lessonAddTeacherRef
                lp.teachers
                    .filterNot { lp.lesson.elem.teachers.toString().contains(it.elem.shortID) }
                    //all teachers except for included
                    .map {
                        option {
                            +"${it.elem.firstname} ${it.elem.surname}"
                            attrs.value = it.uuid
                        }
                    }
            }
            button {
                +"+"
                attrs.onClickFunction = {
                    val selTeacher = lessonAddTeacherRef.current.unsafeCast<SelectedElement>()
                    if (selTeacher.value != "" && selTeacher.value != " ")
                        lp.addTeacher(selTeacher.value)
                    else
                        window.alert("<Add teacher to lesson>: Empty values are not allowed.")
                }
            }
        }
        p {
            +"Remove teacher from lesson: "
            select {
                ref = lessonRmTeacherRef
                lp.teachers
                    .filter { lp.lesson.elem.teachers.toString().contains(it.elem.shortID) }
                    .map {
                        option {
                            +it.elem.shortID
                            attrs.value = it.uuid
                        }
                    }
            }
            button {
                +"rm"
                attrs.onClickFunction = {
                    val selTeacher = lessonRmTeacherRef.current.unsafeCast<SelectedElement>()
                    if (selTeacher.value != "" && selTeacher.value != " ")
                        lp.rmTeacher(selTeacher.value)
                    else
                        window.alert("<Remove teacher from lesson>: Empty values are not allowed.")
                }
            }
        }
    }
    div {
        p {
            +"Add student to lesson: "
            select {
                ref = studentSelectAddRef
                lp.students.filterNot {
                    lp.lesson.elem.students.toString().contains(it.elem.fullID)
                }.map {
                    option {
                        attrs.value = it.uuid
                        +it.elem.fullID
                    }
                }
            }
            button {
                +"+"
                attrs.onClickFunction = {
                    val selStudent = studentSelectAddRef.current.unsafeCast<SelectedElement>()
                    if (selStudent.value != "" && selStudent.value != " ")
                        lp.addStudent(selStudent.value)
                    else
                        window.alert("<Add student to lesson>: Empty values are not allowed.")
                }
            }
        }
    }
    div {
        p {
            +"Remove student from lesson: "
            select {
                ref = studentSelectRmRef
                lp.students.filter {
                    lp.lesson.elem.students.toString().contains(it.elem.fullID)
                }.map {
                    option {
                        attrs.value = it.uuid
                        +"${it.elem.firstname} ${it.elem.surname} @ ${it.elem.group}"
                    }
                }
            }
            button {
                +"rm"
                attrs.onClickFunction = {
                    val selStudent = studentSelectRmRef.current.unsafeCast<SelectedElement>()
                    if (selStudent.value != "" && selStudent.value != " ")
                        lp.rmStudent(selStudent.value)
                    else
                        window.alert("<Remove student from lesson>: Empty values are not allowed.")
                }
            }
        }
    }

    child(fcLessonDetails()) {
        attrs.teachers =
            lp.teachers.filter { lp.lesson.elem.teachers.toString().contains(it.elem.shortID) }
                .map { Pair(it.elem.shortID, it.uuid) }
        attrs.students =
            lp.students.filter { lp.lesson.elem.students.toString().contains(it.elem.fullID) }
            .map { Triple(it.elem.shortID,it.elem.group ,it.uuid) }
    }
}

private class LessonStates(
    val oldLesson: Item<Lesson>,
    val newLesson: Lesson
)

fun fcContainerLesson() = fc("ContainerLesson") { _: Props ->
    val queryClient = useQueryClient()
    val lessonParams = useParams()
    val lessonId = lessonParams["id"]?:"Route param error"

    val queryLesson = useQuery<String, QueryError, String, String>(
        lessonId, { fetchText(Config.lessonsURL + lessonId) })
    val queryStudents = useQuery<String, QueryError, String, String>(
        "studentList", { fetchText(Config.studentsURL) })
    val queryTeachers = useQuery<String, QueryError, String, String>(
        "teachersList", { fetchText(Config.teachersURL) })

    val updateLessonMutation = useMutation<Any, Any, LessonStates, Any>({ elem ->
        //<name, type, hours>
        axios<String>(jso {
            url = "${Config.lessonsURL}/${elem.oldLesson.uuid}"
            method = "Put"
            headers = json("Content-Type" to "application/json")
            data = Json.encodeToString(elem.newLesson)
        })
    },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(lessonId)
                queryClient.invalidateQueries<Any>("lessonList")
            }
        }
    )

    val addTeacherMutation = useMutation<Any, Any, String, Any>({ teacherId ->
        axios<String>(jso {
            url = "${Config.lessonsURL}/$lessonId/details/$teacherId/addt"
            method = "Post"
            headers = json("Content-Type" to "application/json")
        })
    },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(lessonId)
            }
        }
    )

    val rmTeacherMutation = useMutation<Any, Any, String, Any>({ teacherId ->
        axios<String>(jso {
            url = "${Config.lessonsURL}/$lessonId/details/$teacherId/rmt"
            method = "Post"
            headers = json("Content-Type" to "application/json")
        })
    },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(lessonId)
            }
        }
    )

    val addStudentMutation = useMutation<Any, Any, String, Any>({ studentId ->
        axios<String>(jso {
            url = "${Config.lessonsURL}/$lessonId/details/$studentId/adds"
            method = "Post"
            headers = json("Content-Type" to "application/json")
        })
    },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(lessonId)
            }
        }
    )

    val rmStudentMutation = useMutation<Any, Any, String, Any>({ studentId ->
        axios<String>(jso {
            url = "${Config.lessonsURL}/$lessonId/details/$studentId/rms"
            method = "Post"
            headers = json("Content-Type" to "application/json")
        })
    },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(lessonId)
            }
        }
    )

    if (queryLesson.isLoading or queryStudents.isLoading or queryTeachers.isLoading)
        div { +"Loading ..." }
    else if (queryLesson.isError or queryStudents.isError or queryTeachers.isError)
        div { +"Query error. Please contact server administrator at: admin@adminmail.kt" }
    else {
        val lessonItem: ClientItemLesson = Json.decodeFromString(queryLesson.data?:"")
        val studentItems: List<ClientItemStudent> = Json.decodeFromString(queryStudents.data?:"")
        val teacherItems: List<ClientItemTeacher> = Json.decodeFromString(queryTeachers.data?:"")
        child(fcLesson()) {
            attrs.lesson = lessonItem
            attrs.students = studentItems
            attrs.teachers = teacherItems
            attrs.updateLesson = { n, t, h ->
                updateLessonMutation.mutate(LessonStates(lessonItem, Lesson(n, t, h)), null)
            }
            attrs.addTeacher = {
                addTeacherMutation.mutate(it, null)
            }
            attrs.rmTeacher = {
                rmTeacherMutation.mutate(it, null)
            }
            attrs.addStudent = {
                addStudentMutation.mutate(it, null)
            }
            attrs.rmStudent = {
                rmStudentMutation.mutate(it, null)
            }
        }
    }
}
