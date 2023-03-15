import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.Mode.DEVELOPMENT
import org.jetbrains.kotlin.gradle.targets.js.webpack.WebpackDevtool.EVAL_SOURCE_MAP

plugins {
    kotlin("multiplatform")
    application
}

kotlin {

    // jsTargets()
    // jvmTargets()

    js {
        browser {
            compilations.all {
                kotlinOptions {
                    metaInfo = true
                    sourceMap = true
                    sourceMapEmbedSources = "always"
                    verbose = true
                }
            }
            commonWebpackConfig {
                devServer?.`open` = false
                devServer?.`port` = 8080
                mode = DEVELOPMENT
                devtool = EVAL_SOURCE_MAP
                sourceMaps = true
                showProgress = true
            }
        }
        binaries.executable()
    }

    /*
    js {
        browser()
        binaries.executable()
    }
     */

    jvm {
        withJava()
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

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
                // api(project(":desktop?"))
            }
        }
    }
}

application {
    mainClass.set("MainKt")
}

installFullScreenDemo("Development")
installFullScreenDemo("Production")
