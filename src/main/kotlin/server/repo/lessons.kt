package server.repo

import server.model.Lesson

val lessonsRepo = ListRepo<Lesson>()

val basicLessonsData = listOf(
    Lesson("Math", "Practice", 172),
    Lesson("Physics", "Lab", 144),
    Lesson("History", "Lecture", 112),
    Lesson("Programming", "Lab", 172)
)

val studentSets = listOf(
    studentsRepoTestData.filter { it.group == "29m" }.map { it.fullID }.toSet(),
    studentsRepoTestData.filter { it.group == "29z" }.map { it.fullID }.toSet(),
    listOf(11, 10, 9, 8).map { studentsRepoTestData[it] }.map { it.fullID }.toSet(),
    listOf(0, 1, 11).map { studentsRepoTestData[it] }.map { it.fullID }.toSet()
)

val lessonsRepoTestData = basicLessonsData.mapIndexed { i, l ->
    Lesson(
        l.name,
        l.type,
        l.totalHours,
        setOf(teachersRepoTestData[i].shortID),
        studentSets[i]
    )
}