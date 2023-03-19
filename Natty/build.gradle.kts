plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    application
}

kotlin {

    jsTargetsWithWebpack()
    jvmTargets()

    val coroutinesVersion: String by project
    val serializationVersion: String by project

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

                api(project(":core"))
                api(project(":animation"))
                api(project(":controls"))
                api(project(":themes"))
            }
        }

        val jsMain by getting {
            dependencies {
                api(project(":browser"))
            }
        }

        val jvmMain by getting {
            dependencies {
                val osName = System.getProperty("os.name")
                val targetOs = when {
                    osName == "Mac OS X" -> "macos"
                    osName.startsWith("Win") -> "windows"
                    osName.startsWith("Linux") -> "linux"
                    else -> error("Unsupported OS: $osName")
                }

                val targetArch = when (val osArch = System.getProperty("os.arch")) {
                    "x86_64", "amd64" -> "x64"
                    "aarch64" -> "arm64"
                    else -> error("Unsupported arch: $osArch")
                }

                val target = "${targetOs}-${targetArch}"

                // We need the transitive dependencies, but this project is web-only.
                implementation("io.nacular.doodle:desktop-jvm-$target:0.9.0")
            }
        }

    }
}


application {
    mainClass.set("io.dongxi.natty.server.Server.kt")
}