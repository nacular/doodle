plugins {
    kotlin("multiplatform")
}

kotlin {

    jsTargets()
    jvmTargets()

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))

                api(project(":core"))
                api(project(":controls"))
                api(project(":animation"))
            }
        }
    }
}