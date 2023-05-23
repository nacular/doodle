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

        @Suppress("UNUSED_VARIABLE")
        val commonMain by getting {
            dependencies {
                implementation(project(":controls" ))
                implementation(project(":animation"))

                implementation(libs.datetime       )
                implementation(libs.kodein.di      )
                implementation(libs.coroutines.core)
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"            ))
                implementation(kotlin("test-annotations-common"))
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit")  )
                implementation(libs.bundles.test.libs)
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}