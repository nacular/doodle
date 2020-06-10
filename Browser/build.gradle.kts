apply (from = "../jacoco.gradle")

plugins {
    kotlin("multiplatform")

    id("maven-publish"      )
    id("org.jetbrains.dokka")
}

kotlin {
    jvm().compilations.all {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    js {
        browser.testTask {
            enabled = false
        }
    }.compilations.all {
        kotlinOptions {
            sourceMap             = true
            moduleKind            = "commonjs"
            sourceMapEmbedSources = "always"
        }
    }

    targets.all {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes")
//                    allWarningsAsErrors = true
            }
        }
    }

    val mockkVersion     : String by project
    val junitVersion     : String by project
    val log4jVersion     : String by project
    val kodeinVersion    : String by project
    val logbackVersion   : String by project
    val mockkJsVersion   : String by project
    val coroutinesVersion: String by project

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"    ))
                api(project(":controls"))
                api(project(":themes"  ))

                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutinesVersion")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("io.mockk:mockk-common:$mockkVersion")
            }
        }

        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
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

        js().compilations["main"].defaultSourceSet {
            dependencies {
                api("org.kodein.di:kodein-di-erased-js:$kodeinVersion")

                implementation(kotlin("stdlib-js"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion")
            }
        }

        js().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-js"))
                implementation("io.mockk:mockk-js:$mockkJsVersion")
            }
        }
    }
}

//apply plugin: 'kotlin-multiplatform'
//apply plugin: 'maven-publish'
//apply plugin: 'org.jetbrains.dokka'
//apply from  : '../jacoco.gradle'
//
//kotlin {
//    jvm ().compilations.all {
//        kotlinOptions {
//            jvmTarget = "1.8"
//        }
//    }
//    js {
//        browser.testTask {
//            enabled = false
//        }
//    }.compilations.all {
//        kotlinOptions {
//            sourceMap             = true
//            moduleKind            = "commonjs"
//            sourceMapEmbedSources = 'always'
//        }
//    }
//
//    targets {
//        all {
//            compilations.all {
//                tasks[compileKotlinTaskName].kotlinOptions {
//                    freeCompilerArgs = ["-Xuse-experimental=kotlin.ExperimentalUnsignedTypes"]
////                    allWarningsAsErrors = true
//                }
//            }
//        }
//    }
//
//    sourceSets {
//        commonMain.dependencies {
//            api project(':core'    )
//            api project(':controls')
//            api project(':themes'  )
//
//            implementation dep.kotlin_stdlib_common
//            implementation dep.kotlinx_coroutines_core_common
//        }
//
//        commonTest.dependencies {
//            implementation dep.kotlin_test_common
//            implementation dep.kotlin_test_annotations_common
//            implementation dep.mockk_common
//        }
//
//        jvmMain.dependencies {
//            implementation dep.kotlin_stdlib_jdk8
//            implementation dep.kotlinx_coroutines_core_jvm
//        }
//
//        jvmTest.dependencies {
//            implementation dep.junit
//            implementation dep.kotlin_test_junit
//
//            implementation dep.log4j
//            implementation dep.logback
//            implementation dep.mockk
//        }
//
//        jsMain.dependencies {
//            api dep.kodein_js
//            implementation dep.kotlin_stdlib_js
//            implementation dep.kotlinx_coroutines_core_js
//        }
//
//        jsTest.dependencies {
//            implementation dep.mockk_js
//            implementation dep.kotlin_test_js
//        }
//    }
//}
