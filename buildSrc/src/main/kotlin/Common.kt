
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
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
        isRequired = project.hasProperty("release")
        useGpgCmd()

        if (isRequired) {
            sign(extensions.getByType<PublishingExtension>().publications)
        }
    }
}

fun Project.setupPublication(dokkaJar: Jar) {
    val releaseBuild = project.hasProperty("release")

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

        if (releaseBuild) {
            // Need to explicitly establish dependencies between tasks otherwise Gradle will fail
            afterEvaluate {
                val signJs   = getTasksByName("signJsPublication",     false).map { it.name }
                val signJvm  = getTasksByName("signJvmPublication",    false).map { it.name }
                val signWasm = getTasksByName("signWasmJsPublication", false).map { it.name }

                val publishTasks = listOf(
                    "KotlinMultiplatform",
                    "Js",
                    "Jvm",
                    "WasmJs"
                ).map { "publish${it}PublicationToMavenRepository" }

                publishTasks.forEach {
                    runCatching { tasks.getByName(it) }.getOrNull()
                        ?.dependsOn(listOf("signKotlinMultiplatformPublication") + signJs + signJvm + signWasm)
                }
            }
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

private val HasProject.releaseBuild get() = project.hasProperty("release")