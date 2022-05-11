package component

import kotlinext.js.jso
import kotlinx.browser.window
import kotlinx.html.INPUT
import kotlinx.html.InputType
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
    var rmLesson: (Int) -> Unit
}

fun fcLessonList() = fc("LessonList") { props: LessonListProps ->
    val lessonTypeRef = useRef<SELECT>()
    val lessonNameRef = useRef<INPUT>()
    val lessonHoursRef = useRef<INPUT>()
    div {
        h4 { +"Add lesson:" }
        input { ref = lessonNameRef; attrs.placeholder = "Name" }
        select {
            ref = lessonTypeRef
            option {
                +"Type"
                attrs.value = "Type"
            }
            //lesson types are fixed/final
            listOf("Lecture", "Lab", "Practice").map {
                option {
                    attrs.value = it
                    +it
                }
            }
        }
        input { ref = lessonHoursRef; attrs.placeholder = "Hours total (semester)"; attrs.type = InputType.number }
        button {
            +"+"
            attrs.onClickFunction = {
                lessonNameRef.current?.value?.let { n ->
                    if (n.isBlank())
                        window.alert("<Add lesson>: 'Name' field must not be empty!")
                    else {
                        val selType = lessonTypeRef.current.unsafeCast<SelectedElement>()
                        if (selType.value == "Type" || selType.value.isBlank())
                            window.alert("<Add lesson>: select lesson type!")
                        else
                            lessonHoursRef.current?.value?.let { h ->
                                props.addLesson(n, selType.value, h.toInt())
                            }
                    }
                }
            }
        }
    }

    h3 { +"Lessons" }
    ol {
        props.lessons.sortedBy { it.elem.name }.mapIndexed { i, l ->
            li {
                Link {
                    attrs.to = "/lessons/${l.uuid}/details"
                    +"${l.elem.name} (${l.elem.type})"
                }
                +"⠀"
                button {
                    +"rm"
                    attrs.onClickFunction = {
                        //failsafe:
                        if (props.lessons.size == 1)
                            window.alert("<Remove lesson>: " +
                                    "unable to delete last lesson in the list.")
                        else
                            props.rmLesson(i)
                    }
                }
            }
        }
    }
}

fun fcContainerLessonList() = fc("LessonListContainer") { _: Props ->
    val queryClient = useQueryClient()

    val queryLessons = useQuery<String, QueryError, String, String>(
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
                queryClient.invalidateQueries<Any>("lessonsList")
            }
        }
    )

    val rmLessonMutation = useMutation<Any, Any, Item<Lesson>, Any>({ l ->
        axios<String>(jso {
            url = "$lessonsURL/${l.uuid}"
            method = "Delete"
        })
    },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("lessonsList")
            }
        }
    )

    if (queryLessons.isLoading)
        div { +"Loading .." }
    else if (queryLessons.isError)
        div { +"Error!" }
    else {
        val lessons: List<ClientItemLesson> = Json.decodeFromString(queryLessons.data?:"")
        child(fcLessonList()) {
            attrs.lessons = lessons
            attrs.addLesson = { n, t, h ->
                addLessonMutation.mutate(Lesson(n, t, h), null)
            }
            attrs.rmLesson = {
                rmLessonMutation.mutate(lessons[it], null)
            }
        }
    }
}