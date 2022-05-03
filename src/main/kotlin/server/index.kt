package server

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.routing.*
import kotlinx.html.*

fun Route.index() {
    get("/") {
        call.respondHtml(HttpStatusCode.OK) {
            head {
                meta {
                    attributes += "charset" to "UTF-8"
                }
                title {
                    +"Web App Client"
                }
            }
            body {
                div {
                    attributes += "id" to "root"
                }
                script ("text/javascript", "KWclient.js") { }
            }
        }
    }
    static {
        resource("/KWclient.js","KWclient.js")
        resource("/KWclient.js.map","KWclient.js.map")
    }
}