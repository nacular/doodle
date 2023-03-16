import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithJsPresetFunctions
import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithPresetFunctions
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.targets.js.webpack.WebpackDevtool


private fun KotlinJsTargetDsl.configure() {
    compilations.all {
        kotlinOptions {
            moduleKind = "umd"
            sourceMapEmbedSources = "always"
            freeCompilerArgs = listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes")
        }
    }
    browser {
        testTask {
            enabled = false
        }
    }
}


fun KotlinJsProjectExtension.jsTargets(compiler: KotlinJsCompilerType = defaultJsCompilerType) {
    js(compiler).configure()
}


fun KotlinTargetContainerWithJsPresetFunctions.jsTargets(compiler: KotlinJsCompilerType = defaultJsCompilerType) {
    js(compiler).configure()
}


fun KotlinTargetContainerWithPresetFunctions.jvmTargets() {
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs = listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes")
            }
        }
    }
}


fun KotlinMultiplatformExtension.jsTargets() {
    js {
        val releaseBuild = project.hasProperty("release")

        compilations.all {
            kotlinOptions {
                sourceMap = !releaseBuild
                if (sourceMap) {
                    sourceMapEmbedSources = "always"
                }
                moduleKind = "umd"
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

fun KotlinMultiplatformExtension.jsTargetsWithWebpack() {
    js {
        compilations.all {
            kotlinOptions {
                sourceMap = true
                sourceMapEmbedSources = "always"
                moduleKind = "umd"
                freeCompilerArgs = listOf("-Xopt-in=kotlin.ExperimentalUnsignedTypes")
                metaInfo = true
                verbose = false
            }
        }
        browser {
            commonWebpackConfig {
                devServer?.`open` = false
                devServer?.`port` = 8080
                mode = KotlinWebpackConfig.Mode.DEVELOPMENT
                devtool = WebpackDevtool.EVAL_SOURCE_MAP
                sourceMaps = true
                showProgress = true
            }
            testTask {
                enabled = false
            }
            binaries.executable()
        }
    }
}

fun KotlinMultiplatformExtension.jvmTargets(jvmTarget: String = "11") {
    jvm {
        withJava()
        compilations.all {
            kotlinOptions {
                this.jvmTarget = jvmTarget
                freeCompilerArgs = listOf("-Xopt-in=kotlin.ExperimentalUnsignedTypes")
            }
        }
    }
}

fun MavenPom.setupPom() {
    name.set("Doodle")
    description.set("A pure Kotlin, UI framework for the Web")
    url.set("https://github.com/nacular/doodle")
    licenses {
        license {
            name.set("MIT")
            url.set("https://opensource.org/licenses/MIT")
        }
    }
    developers {
        developer {
            id.set("pusolito")
            name.set("Nicholas Eddy")
        }
    }
    scm {
        url.set("https://github.com/nacular/doodle.git")
        connection.set("scm:git:git://github.com/nacular/doodle.git")
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

                url = uri(
                    when {
                        releaseBuild -> "https://oss.sonatype.org/service/local/staging/deploy/maven2"
                        else -> "https://oss.sonatype.org/content/repositories/snapshots"
                    }
                )

                credentials {
                    username = findProperty("suser")?.toString()
                    password = findProperty("spwd")?.toString()
                }
            }
        }
    }
}

fun Project.installFullScreenDemo(suffix: String) {
    try {
        val webPack = project.tasks.getByName(
            "jsBrowser${suffix}Webpack",
            org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack::class
        )

        tasks.register<Copy>("installFullScreenDemo$suffix") {
            dependsOn(webPack)

            val kotlinExtension = project.extensions.getByName("kotlin") as KotlinMultiplatformExtension
            val kotlinSourceSets = kotlinExtension.sourceSets

            val jsFile = webPack.outputFile
            val commonResources = kotlinSourceSets.getByName("commonMain").resources
            val jsResources = kotlinSourceSets.getByName("jsMain").resources
            val docDirectory = "$buildDir/../../docs/${project.name.lowercase().removeSuffix("runner")}"

            from(commonResources, jsResources, jsFile)
            into(docDirectory)
        }
    } catch (ignored: Exception) {
    }
}
