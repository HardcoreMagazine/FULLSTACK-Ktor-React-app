package server.repo

import server.model.Teacher

val teachersRepo = ListRepo<Teacher>()

val teachersRepoTestData = listOf(
    Teacher("Kingston", "Humphries", 40000, 2021, "PJYRD8R7NZ"),
    Teacher("Joseff", "Compton", 35000, 2020, "OY6FI2ZG21"),
    Teacher("Paddy", "Atkins", 54000, 2022, "3IGONSN9IA"),
    Teacher("Jana", "Gross", 60000, 2022, "O0L5QNPRK0"),
    Teacher("Jerry", "Farley", 46000, 2021, "XZQIRZ9H8A")
)