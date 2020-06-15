import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun KotlinMultiplatformExtension.jsTargets() {
    js {
        val releaseBuild = project.hasProperty("release")

        compilations.all {
            kotlinOptions {
                moduleKind = "umd"
                sourceMap  = !releaseBuild
                if (sourceMap) {
                    sourceMapEmbedSources = "always"
                }
                freeCompilerArgs = listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes")
            }
        }
        browser {
            browser.testTask {
                enabled = false
            }
        }
    }
}

fun KotlinMultiplatformExtension.jvmTargets() {
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes")
            }
        }
    }
}

fun MavenPom.setupPom() {
    name.set       ("Doodle"                                 )
    description.set("A pure Kotlin, UI framework for the Web")
    url.set        ("https://github.com/nacular/doodle"      )
    licenses {
        license {
            name.set("MIT"                                )
            url.set ("https://opensource.org/licenses/MIT")
        }
    }
    developers {
        developer {
            id.set  ("pusolito"     )
            name.set("Nicholas Eddy")
        }
    }
    scm {
        url.set                ("https://github.com/nacular/doodle.git"      )
        connection.set         ("scm:git:git://github.com/nacular/doodle.git")
        developerConnection.set("scm:git:git://github.com/nacular/doodle.git")
    }
}

fun Project.setupPublication() {
    extensions.getByType<PublishingExtension>().run {
        publications.withType<MavenPublication>().all {
            pom {
                setupPom()
            }
        }

        repositories {
            maven {
                val releaseBuild = project.hasProperty("release")

                url = uri(when {
                    releaseBuild -> "https://oss.sonatype.org/service/local/staging/deploy/maven2"
                    else         -> "https://oss.sonatype.org/content/repositories/snapshots"
                })

                credentials {
                    username = findProperty("suser")?.toString()
                    password = findProperty("spwd" )?.toString()
                }
            }
        }
    }
}