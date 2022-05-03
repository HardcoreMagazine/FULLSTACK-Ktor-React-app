package component

import kotlinext.js.jso
import kotlinx.browser.window
import kotlinx.html.INPUT
import kotlinx.html.SELECT
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import react.Props
import react.dom.*
import react.fc
import react.query.useMutation
import react.query.useQuery
import react.query.useQueryClient
import react.router.dom.Link
import react.useRef
import server.model.Config.Companion.lessonsURL
import server.model.Item
import server.model.Lesson
import wrappers.QueryError
import wrappers.axios
import wrappers.fetchText
import kotlin.js.json

external interface LessonListProps : Props {
    var lessons: List<Item<Lesson>>
    var addLesson: (String, String, Int) -> Unit
}

fun fcLessonList() = fc("LessonList") { props: LessonListProps ->
    val lessonTypeRef = useRef<SELECT>()
    val lessonNameRef = useRef<INPUT>()
    val lessonHoursRef = useRef<INPUT>()
    div {
        h4 { +"Add lesson:" }
        //+"Type:"
        select {
            ref = lessonTypeRef
            option {
                +"Type"
                attrs.value = "Type"
            }
            //lesson types are fixed
            listOf("Lecture", "Lab", "Practice").map {
                option {
                    attrs.value = it
                    +it
                }
            }
        }
        input { ref = lessonNameRef; attrs.placeholder = "Name" }
        input { ref = lessonHoursRef; attrs.placeholder = "Hours total (semester)" }
        button {
            +"+"
            attrs.onClickFunction = {
                lessonTypeRef.current?.value?.let { t ->
                    if (t == "Type")
                        window.alert("<Add lesson>: select lesson type!")
                    else
                        lessonNameRef.current?.value?.let { n ->
                            if (n.isBlank())
                                window.alert("<Add lesson>: 'Name' field must not be empty!")
                            else
                                lessonHoursRef.current?.value?.let { h ->
                                    //try converting value to Int
                                    val hToInt: Int? = h.toIntOrNull()
                                    //if null -> create browser alert with informative message
                                    if (hToInt == null)
                                        window.alert("<Add lesson>: " +
                                                "'Hours total (semester)' field must be a number!")
                                    //else -> create lesson empty of students
                                    else
                                        props.addLesson(n, t, hToInt)
                                }
                        }
                }
            }
        }
    }

    h3 { +"Lessons" }
    ol {
        props.lessons.sortedBy { it.elem.name }.map {
            li {
                Link {
                    attrs.to = "/lessons/${it.uuid}/details"
                    +"${it.elem.name} (${it.elem.type})"
                }
            }
        }
    }
}

fun fcContainerLessonList() = fc("LessonListContainer") { _: Props ->
    val queryClient = useQueryClient()

    val query = useQuery<String, QueryError, String, String>(
        "lessonsList", { fetchText(lessonsURL) })

    val addLessonMutation = useMutation<Any, Any, Lesson, Any>({ l ->
            axios<String>(jso {
                url = lessonsURL
                method = "Post"
                headers = json("Content-Type" to "application/json",)
                data = Json.encodeToString(l)
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("lessonList")
            }
        }
    )

    if (query.isLoading) div { +"Loading .." }
    else if (query.isError) div { +"Error!" }
    else {
        val lessons: List<ClientItemLesson> = Json.decodeFromString(query.data?:"")
        child(fcLessonList()) {
            attrs.lessons = lessons
            attrs.addLesson = { n, t, h ->
                addLessonMutation.mutate(Lesson(n, t, h), null)
            }
        }
    }
}