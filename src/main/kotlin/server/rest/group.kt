package server.rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import server.model.Config
import server.repo.groupsRepo
import server.repo.studentsRepo

fun Route.group() = route(Config.groupsPath) {
    get {
        if (!groupsRepo.isEmpty()) call.respond(groupsRepo.findAll())
        else call.respondText("No groups found", status = HttpStatusCode.NotFound)
    }
    get("{group}") {
        if (!groupsRepo.isEmpty()) {
            val gpID = call.parameters["group"]?:return@get call
                .respondText("No groups found with name", status = HttpStatusCode.NotFound)
            val groupStudentsItem = studentsRepo.findAll().filter { it.elem.group == gpID }
            call.response.etag(groupStudentsItem.toString())
            call.respond(groupStudentsItem)
        }
        else call.respondText("No groups found", status = HttpStatusCode.NotFound)
    }
}
