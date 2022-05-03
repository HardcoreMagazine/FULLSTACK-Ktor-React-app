package server.model

import kotlinx.serialization.Serializable

@Serializable
data class Lesson(
    val name: String, //name
    val type: String, //type: lecture, lab. work, practice, etc.
    val totalHours: Int, //total hours per semester
    val teachers: Set<String> = emptySet(), //list of prescribed teachers
    val students: Set<String> = emptySet() //list of prescribed students
    //students separated by "groups" on client side
)