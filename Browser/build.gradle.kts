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
            api(projects.core    )
            api(projects.controls)
            api(projects.themes  )
            api(libs.kodein.di   )

            implementation(libs.coroutines.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))

            implementation(projects.core) {
                capabilities {
                    requireCapability("$group:$name-test-fixtures:$version")
                }
            }
        }

        val jsCommon by creating { dependsOn(commonMain.get()) }

        jsMain.get().dependsOn(jsCommon)

        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
            implementation(libs.bundles.test.libs)
        }

        val wasmJsMain by getting {
            dependsOn(jsCommon)
        }
    }
}