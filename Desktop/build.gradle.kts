plugins {
    kotlin("multiplatform")
}

val skikoVersion: String by project

kotlin {
    explicitApi()

    jvmTargets("11")

    val mockkVersion     : String by project
    val junitVersion     : String by project
    val log4jVersion     : String by project
    val kodeinVersion    : String by project
    val logbackVersion   : String by project
    val dateTimeVersion  : String by project
    val coroutinesVersion: String by project

    val osName = System.getProperty("os.name")
    val targetOs = when {
        osName == "Mac OS X"       -> "macos"
        osName.startsWith("Win"  ) -> "windows"
        osName.startsWith("Linux") -> "linux"
        else                       -> error("Unsupported OS: $osName")
    }

    val osArch = System.getProperty("os.arch")
    val targetArch = when (osArch) {
        "x86_64", "amd64" -> "x64"
        "aarch64"         -> "arm64"
        else              -> error("Unsupported arch: $osArch")
    }

    val target = "${targetOs}-${targetArch}"

    sourceSets {
        all {
            languageSettings.optIn("io.nacular.doodle.core.Internal")
        }

        val commonMain by getting {
            dependencies {
                api(project(":core"    ))
                api(project(":controls"))
                api(project(":themes"  ))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime-jvm:$dateTimeVersion")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                api("org.kodein.di:kodein-di:$kodeinVersion")
                compileOnly("org.jetbrains.skiko:skiko-awt-runtime-$target:$skikoVersion")
            }
        }

        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter:$junitVersion")
                implementation(kotlin("test-junit"))

                implementation("org.jetbrains.skiko:skiko-awt-runtime-$target:$skikoVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")

                implementation("org.slf4j:slf4j-api:$log4jVersion")
                implementation("ch.qos.logback:logback-classic:$logbackVersion")
                implementation("io.mockk:mockk:$mockkVersion")
            }
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            jvmOs("linux-x64",   "skiko-awt-runtime-linux-x64"  )
            jvmOs("linux-arm64", "skiko-awt-runtime-linux-arm64")
            jvmOs("macos-x64",   "skiko-awt-runtime-macos-x64"  )
            jvmOs("macos-arm64", "skiko-awt-runtime-macos-arm64")
            jvmOs("windows-x64", "skiko-awt-runtime-windows-x64")
        }
    }
}

fun PublicationContainer.jvmOs(name: String, skikoArtifactId: String) {
    create("jvm$name", MavenPublication::class) {
        val projectGroup   = project.group
        val projectName    = "${project.name}-jvm"
        val projectVersion = project.version

        artifactId = "$projectName-$name"

        pom {
            withXml {
                val dependenciesNode = asNode().appendNode("dependencies")

                dependenciesNode.appendNode("dependency").apply {
                    appendNode("groupId",    projectGroup  )
                    appendNode("artifactId", projectName   )
                    appendNode("version",    projectVersion)
                    appendNode("scope",      "compile"     )
                }

                dependenciesNode.appendNode("dependency").apply {
                    appendNode("groupId",    "org.jetbrains.skiko")
                    appendNode("artifactId", skikoArtifactId      )
                    appendNode("version",    skikoVersion         )
                    appendNode("scope",      "compile"            )
                }
            }
        }
    }
}
