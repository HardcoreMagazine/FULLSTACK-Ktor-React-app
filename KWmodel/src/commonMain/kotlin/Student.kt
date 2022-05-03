package server.model

import kotlinx.serialization.*

@Serializable
class Student(val firstname: String, val surname: String, val group: String) {
    val fullID: String
        get() = "$firstname $surname @ $group"
    val shortID: String
        get() = "$firstname $surname"
}