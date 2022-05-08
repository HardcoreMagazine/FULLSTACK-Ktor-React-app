plugins {
    kotlin("js") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
}

group = "KW"
version = "1.0"

repositories {
    mavenCentral()
    flatDir {
        dirs("$projectDir/../KWmodel/build/libs")
    }
}


val kotlinWrappersVersion = "0.0.1-pre.296-kotlin-1.6.10"
fun kotlinw(target: String): String =
    "org.jetbrains.kotlin-wrappers:kotlin-$target"

dependencies {
    implementation("KW:KWmodel-js-1.0")
    implementation(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:${kotlinWrappersVersion}"))
    implementation(kotlinw("react"))
    implementation(kotlinw("react-dom"))
    implementation(kotlinw("react-router-dom"))
    implementation(kotlinw("redux"))
    implementation(kotlinw("react-redux"))
    implementation(kotlinw("react-query"))
    implementation(kotlinw("styled-next"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.2")
    implementation(npm("cross-fetch", "3.1.5"))
    implementation(npm("axios", "0.24.0"))
}
kotlin {
    js(LEGACY) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
}

//copies newly generated JS into KW (root) folder
tasks.register<Copy>("copyBuild") {
    from("/build/distributions/KWclient.js", "/build/distributions/KWclient.js.map")
    into("../src/main/resources/")
}
tasks.register<Copy>("copyBuildToBuild") {
    from("/build/distributions/KWclient.js", "/build/distributions/KWclient.js.map")
    into("../build/resources/main/")
}
tasks.named("build") { finalizedBy("copyBuild") }
tasks.named("build") { finalizedBy("copyBuildToBuild") }
//by default project uses Production version of React
//in order to enable development version
//use: Gradle -> %PROJECT_NAME% -> Tasks
// -> kotlin browser -> browserDevelopmentWebpack
tasks.named("browserDevelopmentWebpack") { finalizedBy("copyBuild") }
tasks.named("browserDevelopmentWebpack") { finalizedBy("copyBuildToBuild") }