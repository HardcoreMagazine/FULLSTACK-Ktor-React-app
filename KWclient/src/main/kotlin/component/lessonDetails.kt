package component

import react.Props
import react.dom.*
import react.fc

external interface LessonDetailsProps : Props {
    var teachers: List<Pair<String, String>> //<name, id>
    var students: List<Triple<String, String, String>> //name, group, id
}

fun fcLessonDetails() = fc("LessonDetails") { p: LessonDetailsProps ->
    div {
        h4 { +"Teachers:" }
        ul {
            if (p.teachers.isEmpty())
                li { +"empty" }
            else
                p.teachers.map { t ->
                    li {
                        a {
                            +t.first
                            attrs.href = "http://localhost:8000/#/teachers/${t.second}"
                        }
                    }
                }
        }
    }
    div {
        h4 { +"Students:" }
        ul {
            if (p.students.isEmpty()) {
                li { +"empty" }
            }
            else
                p.students
                    .map { it.second }
                    .toSet()
                    .map { gn ->
                        a {
                            +"Group '$gn':"
                            attrs.href = "http://localhost:8000/#/groups/$gn"
                        }
                    p.students.map { s ->
                        if (s.second == gn)
                            li {
                                a {
                                    +s.first
                                    attrs.href = "http://localhost:8000/#/students/${s.third}"
                                }
                            }
                    }
                }
        }
    }
}
