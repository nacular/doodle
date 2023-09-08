plugins {
    kotlin("multiplatform")
}

kotlin {
    explicitApi()

    jvmTargets("11")

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

    val skikoLib = "org.jetbrains.skiko:skiko-awt-runtime-$targetOs-$targetArch:${libs.versions.skikoVersion.get()}"

    sourceSets {
        all {
            languageSettings.optIn("io.nacular.doodle.core.Internal")
        }

        @Suppress("UNUSED_VARIABLE")
        val commonMain by getting {
            dependencies {
                api(project(":core"    ))
                api(project(":controls"))
                api(project(":themes"  ))

                implementation(libs.datetime        )
                implementation(libs.coroutines.core )
                implementation(libs.coroutines.swing)
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"            ))
                implementation(kotlin("test-annotations-common"))
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val jvmMain by getting {
            dependencies {
                api(libs.kodein.di)

                compileOnly(skikoLib)
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit")  )
                implementation(libs.coroutines.test  )
                implementation(libs.bundles.test.libs)
                implementation(skikoLib              )
            }
        }
    }
}

afterEvaluate {
    publishing {
        val pubs = mapOf(
            "linux-x64"   to "skiko-awt-runtime-linux-x64",
            "linux-arm64" to "skiko-awt-runtime-linux-arm64",
            "macos-x64"   to "skiko-awt-runtime-macos-x64",
            "macos-arm64" to "skiko-awt-runtime-macos-arm64",
            "windows-x64" to "skiko-awt-runtime-windows-x64",
        )

        publications {
            pubs.forEach { (name, artifact) ->
                jvmOs(name, artifact)
            }
        }

        val releaseBuild = project.hasProperty("release")


        if (releaseBuild) {
            // Need to explicitly establish dependencies between tasks otherwise Gradle will fail
            val publishTasks = pubs.keys.map { "publish${"Jvm$it"}PublicationToMavenRepository" }
            val signingTasks = listOf(
                "signKotlinMultiplatformPublication",
                "signJvmPublication",
            ) + pubs.keys.map { "sign${"Jvm$it"}Publication" }

            tasks.getByName("publishKotlinMultiplatformPublicationToMavenRepository") {
                dependsOn(signingTasks)
            }

            tasks.getByName("publishJvmPublicationToMavenRepository") {
                dependsOn(signingTasks)
            }

            publishTasks.forEach {
                tasks.getByName(it) {
                    dependsOn(signingTasks)
                }
            }
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
                    appendNode("groupId",    "org.jetbrains.skiko"           )
                    appendNode("artifactId", skikoArtifactId                 )
                    appendNode("version",    libs.versions.skikoVersion.get())
                    appendNode("scope",      "compile"                       )
                }
            }
        }
    }
}
