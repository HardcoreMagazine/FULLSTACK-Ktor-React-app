package server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import server.model.Config
import server.repo.*
import server.rest.group
import server.rest.lesson
import server.rest.student
import server.rest.teacher

fun main() {
    embeddedServer(
        Netty,
        port = Config.serverPort,
        host = Config.serverDomain,
        watchPaths = listOf("classes", "resources")
    ) {
        main()
    }.start(wait = true)
}

fun Application.main(isInDevelopment: Boolean = true) {
    if(isInDevelopment) { //fills repositories with data samples
        //students
        studentsRepoTestData.forEach { studentsRepo.create(it) }
        //students groups
        studentsRepoTestData.map { it.group }.toSet().forEach { groupsRepo.create(it) }
        //teachers
        teachersRepoTestData.forEach { teachersRepo.create(it) }
        //lessons
        lessonsRepoTestData.forEach { lessonsRepo.create(it) }
    }
    install(ContentNegotiation) {
        json()
    }
    routing {
        student()
        group()
        teacher()
        lesson()
        index()
    }
}