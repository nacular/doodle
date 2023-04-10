plugins {
    kotlin("multiplatform")
}

kotlin {
    explicitApi()

    jsTargets ()
    jvmTargets()

    val sl4jVersion      : String by project
    val mockkVersion     : String by project
    val junitVersion     : String by project
    val logbackVersion   : String by project
    val dateTimeVersion  : String by project
    val coroutinesVersion: String by project

    sourceSets {
        all {
            languageSettings.optIn("io.nacular.doodle.core.Internal")
        }

        val commonMain by getting {
            dependencies {
                api(project(":core"))

                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$dateTimeVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter:$junitVersion")
                implementation(kotlin("test-junit"))

                implementation("org.slf4j:slf4j-api:$sl4jVersion")
                implementation("ch.qos.logback:logback-classic:$logbackVersion")
                implementation("io.mockk:mockk:$mockkVersion")
            }
        }

        js().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}