plugins {
    kotlin("multiplatform")
    application
}

kotlin {

    jsTargetsWithWebpack()
    jvmTargets()

    val doodleVersion: String = "0.9.0"

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":contacts"))
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

                implementation("io.nacular.doodle:desktop-jvm-$target:$doodleVersion")
                // This incorrect syntax:  Cannot access class 'org.jetbrains.skia.PathMeasure'.
                // implementation(project(":desktop"))
                // I am not sure what the correct syntax should be to specify "desktop-jvm-x64",
                // but I will not be creating any kotlin desktop apps.
            }
        }
    }
}

application {
    mainClass.set("MainKt")
}

installFullScreenDemo("Development")
installFullScreenDemo("Production")
