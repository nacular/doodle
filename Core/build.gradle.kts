plugins {
    kotlin("multiplatform")
}

kotlin {
    explicitApi()

    jsTargets    ()
    jvmTargets   ()
    wasmJsTargets()

    applyDefaultHierarchyTemplate()

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

        val jsCommon by creating { dependsOn(commonMain.get()) }

        jsMain.get().dependsOn(jsCommon)

        jvmTest.dependencies {
            implementation(kotlin("test")        )
            implementation(libs.bundles.test.libs)
        }

        jsTest.dependencies {
            implementation(kotlin("test-js"))
        }

        val wasmJsMain by getting {
            dependsOn(jsCommon)
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}