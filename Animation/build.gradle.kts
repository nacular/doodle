plugins {
    kotlin("multiplatform")
}

kotlin {
    explicitApi()

    jsTargets    ()
    jvmTargets   ()
    wasmJsTargets()

    sourceSets {
        commonMain.dependencies {
            api(projects.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test-common"            ))
            implementation(kotlin("test-annotations-common"))
        }

        jvmTest.dependencies {
            implementation(kotlin("test-junit")  )
            implementation(libs.bundles.test.libs)
        }

        jsTest.dependencies {
            implementation(kotlin("test-js"))
        }
    }
}