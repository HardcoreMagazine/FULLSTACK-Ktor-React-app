package server.rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import server.model.Config
import server.model.Teacher
import server.repo.teachersRepo

fun Route.teacher() = route(Config.teachersPath) {
    get {
        if (!teachersRepo.isEmpty()) call.respond(teachersRepo.findAll())
        else call.respondText("No teachers found", status=HttpStatusCode.NotFound)
    }
    post {
        val teacher = call.receive<Teacher>()
        teachersRepo.create(teacher)
        call.respondText("Successful teacher creation", status=HttpStatusCode.Created)
    }
    get("{id}") {
        if (!teachersRepo.isEmpty()) {
            val id = call.parameters["id"]?:return@get call
                .respondText("Invalid teacher id", status=HttpStatusCode.BadRequest)
            val teacherItem = teachersRepo[id]?:return@get call
                .respondText("No teachers found with id $id", status=HttpStatusCode.NotFound)
            call.response.etag(teacherItem.etag.toString())
            call.respond(teacherItem)
        }
        else call.respondText("No teachers found", status=HttpStatusCode.NotFound)
    }
    put("{id}") {
        val id = call.parameters["id"]?:return@put call
            .respondText("Invalid teacher id", status=HttpStatusCode.BadRequest)
        teachersRepo[id]?:return@put call
            .respondText("No teachers found with id $id", status=HttpStatusCode.NotFound)
        val newTeacher = call.receive<Teacher>()
        teachersRepo.update(id, newTeacher)
        call.respondText("Student successfully updated", status=HttpStatusCode.Created)
    }
}
