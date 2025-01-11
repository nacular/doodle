import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget
import org.jetbrains.kotlin.gradle.plugin.usesPlatformOf

plugins {
    kotlin("multiplatform")
}

kotlin {
    explicitApi()

    jsTargets    ()
    jvmTargets   ()
    wasmJsTargets()

    applyDefaultHierarchyTemplate()

    sourceSets {
        all {
            languageSettings.optIn("io.nacular.doodle.core.Internal")
        }

        commonMain.dependencies {
            api(libs.measured)

            implementation(kotlin("reflect"))
        }

        commonTest.dependencies {
            implementation(kotlin("test-common"            ))
            implementation(kotlin("test-annotations-common"))
        }

        val jsCommon by creating { dependsOn(commonMain.get()) }

        jsMain.get().dependsOn(jsCommon)

        jvmTest.dependencies {
            implementation(kotlin("test")        )
            implementation(libs.bundles.test.libs)
        }

        val wasmJsMain by getting {
            dependsOn(jsCommon)
        }
    }

    targets.all target@{
        if (this is KotlinMetadataTarget) return@target
        val name = "${name}TestFixtures"

        compilations.create("testFixtures") {
            associateWith(this@target.compilations.getByName("main"))

            this@target.compilations.getByName("test").associateWith(this)

            configurations.consumable("${name}ApiElements") {
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, objects.named(if (platformType == KotlinPlatformType.jvm) "java-api-jars" else "kotlin-api"))
                    attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                }
                extendsFrom(configurations.getByName(apiConfigurationName))
                usesPlatformOf(this@target)
                outgoing {
                    capability("$group:${project.name}-test-fixtures:$version")
                    for (output in output.allOutputs) artifact(output) { builtBy(compileTaskProvider) }
                }
            }

            configurations.consumable("${name}RuntimeElements") {
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, objects.named(if (platformType == KotlinPlatformType.jvm) "java-runtime-jars" else "kotlin-runtime"))
                    attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                }
                extendsFrom(configurations.getByName(implementationConfigurationName))
                extendsFrom(configurations.getByName(runtimeOnlyConfigurationName   ))
                usesPlatformOf(this@target)
                outgoing {
                    capability("$group:${project.name}-test-fixtures:$version")

                    for (output in output.allOutputs) artifact(output) { builtBy(compileTaskProvider) }
                }
            }
        }
    }
    applyDefaultHierarchyTemplate {
        withSourceSetTree(KotlinSourceSetTree("testFixtures"))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}