plugins {
    kotlin("multiplatform")
}

kotlin {
    explicitApi()

    jsTargets    ()
    jvmTargets   ()
    wasmJsTargets()

    sourceSets {
        all {
            languageSettings.optIn("io.nacular.doodle.core.Internal")
        }

        commonMain.dependencies {
            api(projects.core)

            implementation(kotlin("reflect")   )
            implementation(libs.datetime       )
            implementation(libs.coroutines.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
        }

        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
            implementation(libs.bundles.test.libs)
        }

        jsTest.dependencies {
            implementation(kotlin("test-js"))
        }
    }
}