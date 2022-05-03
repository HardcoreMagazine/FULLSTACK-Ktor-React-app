package server.model

import kotlinx.serialization.*

@Serializable
class Teacher(val firstname: String, val surname: String, val salary: Int,
              val lastQual: Int, val govNumber: String) {
    val shortID: String
        get() = "$firstname $surname"
    val detailedData: String
        get() = "$firstname $surname @ salary: $salary @ last re-qualification: $lastQual @ govID: $govNumber"
}