
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JsModuleKind.MODULE_UMD
import org.jetbrains.kotlin.gradle.dsl.JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_ALWAYS
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.HasProject

fun KotlinMultiplatformExtension.jsTargets() {
    compilerOptions()

    js {
        compilerOptions {
            moduleKind.set(MODULE_UMD)
            sourceMap.set(!releaseBuild)
            if (sourceMap.get()) {
                sourceMapEmbedSources.set(SOURCE_MAP_SOURCE_CONTENT_ALWAYS)
            }
        }

        browser {
            testTask { enabled = false }
        }
    }
}

@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
fun KotlinMultiplatformExtension.wasmJsTargets() {
    compilerOptions()

    wasmJs {
        compilerOptions {
            moduleKind.set(MODULE_UMD)
            sourceMap.set(!releaseBuild)
            if (sourceMap.get()) {
                sourceMapEmbedSources.set(SOURCE_MAP_SOURCE_CONTENT_ALWAYS)
            }
        }

        browser {
            testTask { enabled = false }
        }
    }
}

fun KotlinMultiplatformExtension.jvmTargets(jvmTarget: String = "11") {
    compilerOptions()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = jvmTarget
        }
    }
}

fun MavenPom.setupPom() {
    name.set       ("Doodle"                                             )
    description.set("A pure Kotlin, UI framework for the Web and Desktop")
    url.set        ("https://github.com/nacular/doodle"                  )
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
        useGpgCmd()

        if (project.releaseBuild) {
            sign(extensions.getByType<PublishingExtension>().publications)
        }
    }
}

fun Project.setupPublication(dokkaJar: Jar) {
    extensions.getByType<PublishingExtension>().run {
        publications.withType<MavenPublication>().all {
            if (project.releaseBuild || project.hasProperty("snapshot")) {
                artifact(dokkaJar)

                pom {
                    setupPom()
                }
            }
        }

        extensions.getByType<SigningExtension>().run {
            if (releaseBuild) {
                sign(extensions.getByType<PublishingExtension>().publications)
            }
        }

        // Workaround https://github.com/gradle/gradle/issues/26091
        tasks.withType<AbstractPublishToMaven>().configureEach {
            val signingTasks = tasks.withType<Sign>()
            mustRunAfter(signingTasks)
        }
    }
}

private fun KotlinMultiplatformExtension.compilerOptions() {
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                    freeCompilerArgs.add("-opt-in=kotlin.ExperimentalUnsignedTypes")
                }
            }
        }
    }
}

private val Project.releaseBuild get() = hasProperty("release")
private val HasProject.releaseBuild get() = project.releaseBuild