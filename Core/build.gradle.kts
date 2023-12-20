plugins {
    kotlin("multiplatform")
}

kotlin {
    explicitApi()

    jsTargets ()
    jvmTargets()

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

        jvmTest.dependencies {
            implementation(kotlin("test")        )
            implementation(libs.bundles.test.libs)
        }

        jsTest.dependencies {
            implementation(kotlin("test-js"))
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}