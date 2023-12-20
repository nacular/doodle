plugins {
    kotlin("multiplatform")
}

kotlin {
    explicitApi()

    jsTargets ()
    jvmTargets()

    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
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