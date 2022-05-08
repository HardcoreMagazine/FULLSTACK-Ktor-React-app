package component

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import react.*
import react.dom.*
import react.query.useQuery
import react.router.useParams
import server.model.Config
import server.model.Item
import server.model.Lesson
import server.model.Student
import wrappers.QueryError
import wrappers.fetchText

external interface GroupProps : Props {
    var groupName: String
    var relatedStudents: List<Item<Student>>
    var relatedLessons: List<Item<Lesson>>
}

fun prettyPrintStringArray(iterable: Iterable<String>): String {
    var result = ""
    for (s in iterable)
        result += "$s; "
    return result
}

fun fcGroup() = fc("Group") { p: GroupProps ->
    h3 { +"Group '${p.groupName}'" }
    ol {
        if (p.relatedStudents.isEmpty())
            li { +"empty" }
        else {
            p.relatedStudents.map {
                li {
                    a {
                        attrs.href = "http://localhost:8000/#/students/${it.uuid}"
                        +"${it.elem.firstname} ${it.elem.surname}"
                    }
                }
            }
        }
    }
    h4 { +"Group lessons:" }
    ul {
        if (p.relatedLessons.isEmpty())
            li { +"empty" }
        else {
            p.relatedLessons.map {
                li {
                    a {
                        attrs.href = "http://localhost:8000/#/lessons/${it.uuid}/details"
                        +"${it.elem.name} (${it.elem.type}) @ ${prettyPrintStringArray(it.elem.teachers)}"
                    }
                }
            }
        }
    }
}

fun fcContainerGroup() = fc("ContainerGroup") { _: Props ->
    val studentParams = useParams()
    val groupName = studentParams["group"] ?: "Route param error"

    val queryGroup = useQuery<String, QueryError, String, String>(
        "groupStudentList", { fetchText(Config.groupsURL + groupName) })

    val queryLessons = useQuery<String, QueryError, String, String>(
        "groupLessonsList", { fetchText(Config.lessonsURL) })

    if (queryGroup.isLoading or queryLessons.isLoading)
        div { +"Loading ..." }
    else if (queryGroup.isError or queryLessons.isError)
        div { +"Query error. Please contact server administrator at: admin@adminmail." }
    else {
        val students: List<ClientItemStudent> =
            Json.decodeFromString<List<ClientItemStudent>>(queryGroup.data?:"")
                .filter { it.elem.group == groupName }
        val lessons: List<ClientItemLesson> =
            Json.decodeFromString<List<ClientItemLesson>>(queryLessons.data?:"")
                .filter { it.elem.students.toString().contains(groupName) }
        child(fcGroup()) {
            attrs.groupName = groupName
            attrs.relatedStudents = students
            attrs.relatedLessons = lessons
        }
    }
}
