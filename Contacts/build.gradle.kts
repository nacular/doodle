plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {

    jsTargets()
    jvmTargets()

    val kodeinVersion: String by project
    val coroutinesVersion: String by project
    val serializationVersion: String by project

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                api("org.kodein.di:kodein-di:$kodeinVersion")

                api(project(":core"))
                api(project(":controls"))
                api(project(":themes"))
                api(project(":animation"))
                api(project(":modal"))
            }
        }
    }
}
