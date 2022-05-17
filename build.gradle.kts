@file:Suppress("GradlePackageUpdate")

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
}

group = "KW"
version = "1.0"
application {
    mainClass.set("KW.ApplicationKt")
    //applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
    //not going to work unless used in VM options in <start> configuration editor
}

repositories {
    mavenCentral()
    flatDir {
        dirs("$projectDir/KWmodel/build/libs")
    }
}

dependencies {
    implementation("KW:KWmodel-jvm-1.0") //local repo
    implementation("KW:KWmodel-js-1.0")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-server-sessions:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-html-builder:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
