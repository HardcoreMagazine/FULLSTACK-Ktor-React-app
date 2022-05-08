package server.model

class Config {
    companion object {
        const val serverDomain = "localhost"
        const val serverPort = 8000
        const val serverApi = "2"
        const val serverUrl = "http://$serverDomain:$serverPort/"
        const val pathPrefix = "api$serverApi/"

        const val studentsPath = "${pathPrefix}students/"
        const val studentsURL = "$serverUrl$studentsPath"

        const val groupsPath = "${pathPrefix}groups/"
        const val groupsURL = "$serverUrl$groupsPath"

        const val teachersPath = "${pathPrefix}teachers/"
        const val teachersURL = "$serverUrl$teachersPath"

        const val lessonsPath = "${pathPrefix}lessons/"
        const val lessonsURL = "$serverUrl$lessonsPath"
    }
}