apply (from = "../jacoco.gradle")

plugins {
    kotlin("multiplatform")

    id("org.jetbrains.dokka")
}

kotlin {
    explicitApi()

    jsTargets ()
    jvmTargets()

    val mockkVersion     : String by project
    val junitVersion     : String by project
    val log4jVersion     : String by project
    val kodeinVersion    : String by project
    val logbackVersion   : String by project
    val coroutinesVersion: String by project

    sourceSets {
        all {
            languageSettings.optIn("io.nacular.doodle.core.Internal")
        }

        val commonMain by getting {
            dependencies {
                implementation(project(":controls" ))
                implementation(project(":animation"))

                implementation("org.kodein.di:kodein-di:$kodeinVersion")
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
                implementation("junit:junit:$junitVersion")
                implementation(kotlin("test-junit"))

                implementation("org.slf4j:slf4j-api:$log4jVersion")
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