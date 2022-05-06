package server.rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import server.model.Config.Companion.studentsPath
import server.model.Student
import server.repo.studentsRepo

fun Route.student() = route(studentsPath) {
    get {
        if (!studentsRepo.isEmpty())
            call.respond(studentsRepo.findAll())
        else
            call.respondText("No students found", status=HttpStatusCode.NotFound)
    }
    get("{id}") {
        val id = call.parameters["id"]?:return@get call
            .respondText("Invalid student id", status=HttpStatusCode.BadRequest)
        val studentItem =
            studentsRepo[id]?:return@get call
                .respondText("No students found with id $id", status=HttpStatusCode.NotFound)
        call.response.etag(studentItem.etag.toString())
        call.respond(studentItem)
    }
    post {
        val student = call.receive<Student>()
        studentsRepo.create(student)
        call.respondText("Successful student creation", status = HttpStatusCode.Created)
    }
    delete("{id}") {
        val id = call.parameters["id"] ?: return@delete call
            .respondText("Invalid student id", status = HttpStatusCode.BadRequest)
        if (studentsRepo.delete(id)) {
            call.respondText("Student removed correctly", status = HttpStatusCode.Accepted)
        } else {
            call.respondText("Student with id $id does not exist", status = HttpStatusCode.NotFound)
        }
    }
    put("{id}") {
        val id = call.parameters["id"] ?: return@put call.respondText(
            "Invalid student id",
            status = HttpStatusCode.BadRequest
        )
        studentsRepo[id] ?: return@put call.respondText(
            "No students found with id $id",
            status = HttpStatusCode.NotFound
        )
        val newStudent = call.receive<Student>()
        studentsRepo.update(id, newStudent)
        call.respondText("Student successfully updated", status = HttpStatusCode.Created)
    }
}
