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
        }

        val jsCommon by creating { dependsOn(commonMain.get()) }

        jsMain.get().dependsOn(jsCommon)

        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
            implementation(libs.bundles.test.libs)
        }

        jsTest.dependencies {
            implementation(kotlin("test-js"))
        }

        val wasmJsMain by getting {
            dependsOn(jsCommon)
        }
    }
}