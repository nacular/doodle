apply (from = "../jacoco.gradle")

plugins {
    kotlin("multiplatform")
}

kotlin {
    explicitApi()

    jvmTargets()

    val mockkVersion     : String by project
    val junitVersion     : String by project
    val log4jVersion     : String by project
    val kodeinVersion    : String by project
    val logbackVersion   : String by project
    val coroutinesVersion: String by project
    val skikoVersion     : String by project
    val dateTimeVersion  : String = "0.2.0"

    val osName = System.getProperty("os.name")
    val targetOs = when {
        osName == "Mac OS X"       -> "macos"
        osName.startsWith("Win")   -> "windows"
        osName.startsWith("Linux") -> "linux"
        else                       -> error("Unsupported OS: $osName")
    }

    val osArch = System.getProperty("os.arch")
    val targetArch = when (osArch) {
        "x86_64", "amd64" -> "x64"
        "aarch64"         -> "arm64"
        else              -> error("Unsupported arch: $osArch")
    }

    val target = "${targetOs}-${targetArch}"

    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("io.nacular.doodle.core.Internal")
        }

        val commonMain by getting {
            dependencies {
                api(project(":core"    ))
                api(project(":controls"))
                api(project(":themes"  ))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                api("org.kodein.di:kodein-di:$kodeinVersion")
                api("org.jetbrains.skiko:skiko-jvm-runtime-$target:$skikoVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion" )
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime-jvm:$dateTimeVersion")
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
    }
}
//apply (from = "../jacoco.gradle")
//
//plugins {
//    kotlin("jvm")
//}
//
//val mockkVersion     : String by project
//val junitVersion     : String by project
//val log4jVersion     : String by project
//val kodeinVersion    : String by project
//val logbackVersion   : String by project
//val coroutinesVersion: String by project
//val skikoVersion     : String by project
//
//kotlin {
//    explicitApi()
//
//    target {
//        compilations.all {
//            kotlinOptions {
//                jvmTarget        = "1.8"
//                freeCompilerArgs = listOf("-Xopt-in=kotlin.ExperimentalUnsignedTypes")
//            }
//        }
//    }
//
//}
//
//val osName = System.getProperty("os.name")
//val targetOs = when {
//    osName == "Mac OS X"       -> "macos"
//    osName.startsWith("Win")   -> "windows"
//    osName.startsWith("Linux") -> "linux"
//    else                       -> error("Unsupported OS: $osName")
//}
//
//val osArch = System.getProperty("os.arch")
//var targetArch = when (osArch) {
//    "x86_64", "amd64" -> "x64"
//    "aarch64"         -> "arm64"
//    else              -> error("Unsupported arch: $osArch")
//}
//
//val target = "${targetOs}-${targetArch}"
//
//dependencies {
//    api(project(":core"    ))
//    api(project(":controls"))
//    api(project(":themes"  ))
//    api("org.kodein.di:kodein-di:$kodeinVersion")
//
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
//    implementation("org.jetbrains.skiko:skiko-jvm-runtime-$target:$skikoVersion")
//
//    testImplementation("junit:junit:$junitVersion")
//    testImplementation(kotlin("test-junit"))
//    testImplementation("org.slf4j:slf4j-api:$log4jVersion")
//    testImplementation("ch.qos.logback:logback-classic:$logbackVersion")
//    testImplementation("io.mockk:mockk:$mockkVersion")
//}
