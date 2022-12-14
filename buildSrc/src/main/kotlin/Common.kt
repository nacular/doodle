import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun KotlinMultiplatformExtension.jsTargets() {
    js {
        val releaseBuild = project.hasProperty("release")

        compilations.all {
            kotlinOptions {
                sourceMap  = !releaseBuild
                if (sourceMap) {
                    sourceMapEmbedSources = "always"
                }
                moduleKind       = "umd"
                freeCompilerArgs = listOf("-Xopt-in=kotlin.ExperimentalUnsignedTypes")
            }
        }
        browser {
            testTask {
                enabled = false
            }
        }
    }
}

fun KotlinMultiplatformExtension.jvmTargets(jvmTarget: String = "1.8") {
    jvm {
        compilations.all {
            kotlinOptions {
                this.jvmTarget   = jvmTarget
                freeCompilerArgs = listOf("-Xopt-in=kotlin.ExperimentalUnsignedTypes")
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

fun Project.setupSigning() {
    extensions.getByType<SigningExtension>().run {
        isRequired = project.hasProperty("release")
        useGpgCmd()

        if (isRequired) {
            sign(extensions.getByType<PublishingExtension>().publications)
        }
    }
}

fun Project.setupPublication(dokkaJar: Jar) {
    extensions.getByType<PublishingExtension>().run {
        publications.withType<MavenPublication>().all {
            if (project.hasProperty("release") || project.hasProperty("snapshot")) {
                artifact(dokkaJar)

                pom {
                    setupPom()
                }
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