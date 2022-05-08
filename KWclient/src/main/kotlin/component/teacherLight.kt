package component

import kotlinext.js.jso
import kotlinx.html.SELECT
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import react.*
import react.dom.*
import react.query.*
import react.router.useParams
import server.model.*
import wrappers.QueryError
import wrappers.axios
import wrappers.fetchText
import kotlin.js.json

external interface TeacherProps : Props {
    var teacher: Item<Teacher>
    var lessons: List<Item<Lesson>>
    /*
    var addLesson: (String) -> Unit
    var rmLesson: (Int) -> Unit
    */
}

fun fcTeacher() = fc("Teacher") { p: TeacherProps ->
    div {
        h1 { +"Profile" }
        p.teacher.elem.let {
            +it.detailedData
        }
        h1 { +"Lessons" }
        p.lessons.filter { it.elem.teachers.contains(p.teacher.elem.shortID) }.forEach {
            +"${it.elem.name} ${it.elem.type}"
        }
    }
    /*
    val lessonSelectAddRef = useRef<SELECT>()
    div {
        h4 { +"Add lesson: " }
        select {
            ref = lessonSelectAddRef
            p.lessons.filterNot {
                it.elem.teachers.toString().contains(p.teacher.elem.shortID)
            }.map {
                option {
                    attrs.value = it.uuid
                    +"${it.elem.name} (${it.elem.type})"
                }
            }
        }
        button {
            +"+"
            attrs.onClickFunction = {
                val select = lessonSelectAddRef.current.unsafeCast<SelectedElement>()
                if (select.value != "" && select.value != " ")
                    p.addLesson(select.value)
                else
                    kotlinx.browser.window.alert("<Add lesson>: Empty values are not allowed.")
            }
        }
    }
    div {
        h4 { +"Prescribed lessons: " }
        ol {
            val prescribedLessons =
                p.lessons.filter { it.elem.teachers.toString().contains(p.teacher.elem.shortID) }
            if (prescribedLessons.isEmpty())
                li { +"empty" }
            else {
                prescribedLessons.mapIndexed { i, l ->
                    li {
                        a {
                            attrs.href = "http://localhost:8000/#/lessons/${l.uuid}/details"
                            +"${l.elem.name} (${l.elem.type})"
                        }
                        +"⠀" //empty symbol/element separator
                        button {
                            +"rm"
                            p.rmLesson(i)
                        }
                    }
                }
            }
        }
    }
    */
}

fun fcContainerTeacher() = fc("ContainerTeacher") { _: Props ->
    //val queryClient = useQueryClient()
    val teacherParams = useParams()
    val teacherId = teacherParams["id"] ?: "Route param error"

    val queryLessons = useQuery<String, QueryError, String, String>(
        "lessonsList", { fetchText(Config.lessonsURL ) })

    val queryTeacher = useQuery<String, QueryError, String, String>(
        teacherId, { fetchText(Config.teachersURL + teacherId) })

    /*
    val addLessonMutation = useMutation<Any, Any, String, Any>({ lessonId ->
        axios<String>(jso {
            url = "${Config.lessonsURL}/$lessonId/details/$teacherId/addt"
            method = "Post"
            headers = json("Content-Type" to "application/json")
        })
    },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(teacherId)
                queryClient.invalidateQueries<Any>("lessonsList")
            }
        }
    )

    val rmLessonMutation = useMutation<Any, Any, String, Any>({ lessonId ->
        axios<String>(jso {
            url = "${Config.lessonsURL}/$lessonId/details/$teacherId/rmt"
            method = "Post"
            headers = json("Content-Type" to "application/json")
        })
    },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(teacherId)
                queryClient.invalidateQueries<Any>("lessonsList")
            }
        }
    )
    */
    /*
    if (queryLessons.isLoading)
        div { +"Loading lessons data..." }
    else if (queryTeacher.isLoading)
        div { +"Loading teacher data..." }
    else if (queryLessons.isError or queryTeacher.isLoading)
        div { +"Query error. Please contact server administrator at: admin@adminmail." }
    */
    if (queryTeacher.isLoading or queryLessons.isLoading)
        div { +"Loading ..." }
    else if (queryTeacher.isLoadingError or queryLessons.isError)
        div { +"Query error" }
    else {
        val lessons: List<ClientItemLesson> = Json.decodeFromString(queryLessons.data?:"")
        val teacher: ClientItemTeacher = Json.decodeFromString(queryTeacher.data?:"")
        child(fcTeacher()) {
            attrs.teacher = teacher
            attrs.lessons = lessons
            /*attrs.addLesson = { lessonId ->
                addLessonMutation.mutate(lessonId, null)
            }
            attrs.rmLesson = {
                rmLessonMutation.mutate(lessons[it].uuid, null)
            }
            */
        }
    }
}
