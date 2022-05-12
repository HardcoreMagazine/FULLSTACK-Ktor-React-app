package server.repo

import server.model.Student

val studentsRepo = ListRepo<Student>()
val groupsRepo = ListRepo<String>()

val studentsRepoTestData = listOf(
    Student("Sheldon", "Cooper", "29m"),
    Student("Anwar", "Pearce", "29m"),
    Student("Ignacy", "Yu", "29m"),
    Student("Mark", "Ether", "29m"),
    Student("Howard", "Wolowitz", "29z"),
    Student("Penny", "Hofstadter", "29z"),
    Student("Nusaybah", "Roach", "29z"),
    Student("Wiktoria", "Kerr", "29z"),
    Student("Fionnuala", "Conway", "29z"),
    Student("Leonard", "Hofstadter", "29i"),
    Student("Freddie", "Sutton", "29i"),
    Student("Danny", "Flores", "29i"),
    Student("Reis", "Morrow", "29i")
)
