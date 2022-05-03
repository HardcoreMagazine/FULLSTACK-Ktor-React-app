package server.rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import server.model.Config.Companion.lessonsPath
import server.model.Lesson
import server.repo.RepoItem
import server.repo.lessonsRepo
import server.repo.studentsRepo
import server.repo.teachersRepo

fun Route.lesson() {
    //router for lessonList
    route(lessonsPath) {
        get {
            if (!lessonsRepo.isEmpty()) call.respond(lessonsRepo.findAll())
            else call.respondText("No lessons found", status=HttpStatusCode.NotFound)
        }
        get("{id}") {
            val id = call.parameters["id"]?:return@get call
                .respondText("Invalid lesson id", status=HttpStatusCode.BadRequest)
            val lessonItem = lessonsRepo[id]?:return@get call
                .respondText("No lesson with id $id", status=HttpStatusCode.NotFound)
            call.respond(lessonItem)
        }
        post {
            val lesson = call.receive<Lesson>()
            lessonsRepo.create(lesson)
            call.respondText("Successful lesson creation", status = HttpStatusCode.Created)
        }
        put("{id}") {
            val id = call.parameters["id"]?:return@put call
                .respondText("Bad lesson id", status=HttpStatusCode.BadRequest)
            val lessonItem = lessonsRepo[id]?:return@put call
                .respondText("No lessons found with id $id", status=HttpStatusCode.NotFound)
            val lessonData = call.receive<Lesson>()
            val newLesson = lessonItem.elem.copy(
                name=lessonData.name, type=lessonData.type, totalHours=lessonData.totalHours)
            lessonsRepo.update(lessonItem.uuid, newLesson)
            call.respondText("Lesson updated successfully", status=HttpStatusCode.Created)
        }

    }
    //router for lesson, lessonDetails
    route("$lessonsPath{lessonId}/details/{subjectId}") {
        //add teacher to lesson
        post("/addt") {
            when (val lsRes = lsParameters()) {
                is LSOk -> {
                    val oldLesson = lsRes.lessonItem.elem
                    if (!oldLesson.teachers.contains(lsRes.subject)) {
                        val newLesson = oldLesson.copy(teachers=oldLesson.teachers+lsRes.subject)
                        lessonsRepo.update(lsRes.lessonItem.uuid, newLesson)
                        call.respond(lessonsRepo[lsRes.lessonItem.uuid]!!)
                    }
                    else return@post call
                        .respondText("Teacher '${lsRes.subject}' already included " +
                                "in lesson '${lsRes.lessonItem.elem.name}'",
                        status=HttpStatusCode.BadRequest)
                }
                is LSFail -> call.respondText(lsRes.text, status=lsRes.code)
            }
        }
        //remove teacher from lesson
        post("/rmt") {
            when (val lsRes = lsParameters()) {
                is LSOk -> {
                    val oldLesson = lsRes.lessonItem.elem
                    if (oldLesson.teachers.contains(lsRes.subject)) {
                        val newLesson = oldLesson.copy(teachers=oldLesson.teachers-lsRes.subject)
                        lessonsRepo.update(lsRes.lessonItem.uuid, newLesson)
                        call.respond(lessonsRepo[lsRes.lessonItem.uuid]!!)
                    }
                    else return@post call
                        .respondText("Teacher '${lsRes.subject}' not included " +
                                "in lesson '${lsRes.lessonItem.elem.name}'",
                        status=HttpStatusCode.BadRequest)
                }
                is LSFail -> call.respondText(lsRes.text, status=lsRes.code)
            }
        }
        //add student to lesson
        post("/adds") {
            when (val lsRes = lsParameters()) {
                is LSOk -> {
                    val oldElem = lsRes.lessonItem.elem
                    if (!oldElem.students.contains(lsRes.subject)) {
                        val newElem = oldElem.copy(students=oldElem.students+lsRes.subject)
                        lessonsRepo.update(lsRes.lessonItem.uuid, newElem)
                        call.respond(lessonsRepo[lsRes.lessonItem.uuid]!!)
                    }
                    else {
                        return@post call
                            .respondText("Student '${lsRes.subject}' already included " +
                                "in lesson '${lsRes.lessonItem.elem.name}'",
                            status=HttpStatusCode.BadRequest)
                    }
                }
                is LSFail -> call.respondText(lsRes.text, status=lsRes.code)
            }
        }
        //remove student from lesson
        post("/rms") {
            when (val lsRes = lsParameters()) {
                is LSOk -> {
                    val oldElem = lsRes.lessonItem.elem
                    if (oldElem.students.contains(lsRes.subject)) {
                        val newElem = oldElem.copy(students=oldElem.students-lsRes.subject)
                        lessonsRepo.update(lsRes.lessonItem.uuid, newElem)
                        call.respond(lessonsRepo[lsRes.lessonItem.uuid]!!)
                    }
                    else {
                        return@post call
                            .respondText("Student '${lsRes.subject}' not included " +
                                "in lesson '${lsRes.lessonItem.elem.name}'",
                            status=HttpStatusCode.NotFound)
                    }
                }
                is LSFail -> call.respondText(lsRes.text, status=lsRes.code)
            }
        }
    }
}

private sealed interface LSResult
private class LSOk(val lessonItem: RepoItem<Lesson>, val subject: String) : LSResult
private class LSFail(val text: String, val code: HttpStatusCode) : LSResult

private fun PipelineContext<Unit, ApplicationCall>.lsParameters(): LSResult {
    val lessonId = call.parameters["lessonId"]
        ?:return LSFail("Bad lessonId", HttpStatusCode.BadRequest)
    val lessonItem = lessonsRepo[lessonId]
        ?:return LSFail("No lessons found with id $lessonId", HttpStatusCode.NotFound)
    val subjectId = call.parameters["subjectId"]
        ?:return LSFail("Bad subjectId", HttpStatusCode.BadRequest)
    val teacherSubject = teachersRepo[subjectId]
    val studentSubject = studentsRepo[subjectId]
    /**
     * According to calculations,
     * there is only 1 in 2.71 quintillion (2.71*10^30) chance
     * that UUIDs collide -> "if-else" calls should be safe
     */
    if (teacherSubject != null && studentSubject != null)
        //case: both exist (see note above)
        return LSFail("Internal service error", HttpStatusCode.InternalServerError)
    return if (teacherSubject != null)
        LSOk(lessonItem, teacherSubject.elem.shortID)
    else if (studentSubject != null)
        LSOk(lessonItem, studentSubject.elem.fullID)
    else
        //case: none exist
        LSFail("No subjects found with id '$subjectId'", HttpStatusCode.NotFound)
}
