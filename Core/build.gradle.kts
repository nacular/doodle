
import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Category.LIBRARY
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget
import org.jetbrains.kotlin.gradle.plugin.usesPlatformOf

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

        wasmJsMain {
            dependsOn(jsCommon)
        }
    }

    // Custom TextFixture until there is proper support for this in Multiplatform. Based on
    // https://kotlinlang.slack.com/archives/C3PQML5NU/p1735910631062769
    targets.all target@{
        if (this is KotlinMetadataTarget) return@target
        val name = "${name}TestFixtures"

        compilations.create("testFixtures") {
            fun ConsumableConfiguration.setup(
                       jvmUsageName   : String,
                       kotlinUsageName: String,
                vararg extendsFrom    : String
            ) {
                attributes {
                    attribute(USAGE_ATTRIBUTE,    objects.named(if (platformType == jvm) jvmUsageName else kotlinUsageName))
                    attribute(CATEGORY_ATTRIBUTE, objects.named(LIBRARY))
                }
                extendsFrom.forEach {
                    extendsFrom(configurations.getByName(it))
                }
                usesPlatformOf(this@target)
                outgoing {
                    capability("$group:${project.name}-test-fixtures:$version")
                    this@target.compilations.all compilation@{
                        for (classesDir in output.classesDirs) artifact(classesDir) { builtBy(compileTaskProvider) }
                        if (this@compilation is KotlinJvmCompilation) {
                            artifact(output.resourcesDir) { builtBy(processResourcesTaskName) }
                        }
                    }
                }
            }

            associateWith(this@target.compilations.getByName("main"))

            this@target.compilations.getByName("test").associateWith(this)

            configurations.consumable("${name}ApiElements") {
                setup(
                    jvmUsageName    = "java-api-jars",
                    kotlinUsageName = "kotlin-api",
                    apiConfigurationName
                )
            }

            configurations.consumable("${name}RuntimeElements") {
                setup(
                    jvmUsageName    = "java-runtime-jars",
                    kotlinUsageName = "kotlin-runtime",
                    implementationConfigurationName,
                    runtimeOnlyConfigurationName
                )

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