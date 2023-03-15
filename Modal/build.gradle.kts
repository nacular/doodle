plugins {
    kotlin("multiplatform")
}

kotlin {

    jsTargets()
    jvmTargets()

    val coroutinesVersion: String by project

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

                api(project(":core"))
                api(project(":controls"))
                api(project(":animation"))
            }
        }
    }
}