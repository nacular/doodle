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
                implementation(project(":photos"))
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
                    osName == "Mac OS X"       -> "macos"
                    osName.startsWith("Win"  ) -> "windows"
                    osName.startsWith("Linux") -> "linux"
                    else                       -> error("Unsupported OS: $osName")
                }

                val targetArch = when (val osArch = System.getProperty("os.arch")) {
                    "x86_64", "amd64" -> "x64"
                    "aarch64"         -> "arm64"
                    else              -> error("Unsupported arch: $osArch")
                }

                val target = "${targetOs}-${targetArch}"

                implementation ("io.nacular.doodle:desktop-jvm-$target:$doodleVersion")
                // I want to replace the line above with:
                //      implementation(project(":desktop"))
                // But this is incorrect syntax:  Cannot access class 'org.jetbrains.skia.PathMeasure'.
                // I am not sure what the correct syntax should be to specify "desktop-jvm-x64",
                // but I will not be creating any kotlin desktop apps.
            }
        }
    }
}

application {
    mainClass.set("io.nacular.doodle.examples.MainKt")
}

installFullScreenDemo("Development")
installFullScreenDemo("Production" )