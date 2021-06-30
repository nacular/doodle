buildscript {
    val kotlinVersion: String by System.getProperties()

    repositories {
        maven       { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven       { url = uri("https://plugins.gradle.org/m2/"          ) }
        mavenCentral()
    }

    dependencies {
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

plugins {
    id    ("org.jetbrains.dokka") version "0.10.1"
    signing
}

allprojects {
    apply (plugin = "org.jetbrains.dokka")
    apply (plugin = "maven-publish"      )
    apply (plugin = "signing"            )

    repositories {
        maven       { url = uri("http://dl.bintray.com/kotlin/kotlin-eap") }
        maven       { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        mavenCentral()
        mavenLocal  ()
        jcenter     ()
    }

    val dokkaJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles Kotlin docs with Dokka"
        classifier = "javadoc"
        from(tasks.dokka)
    }

    setupPublication(dokkaJar)

    setupSigning()

    tasks {
        val dokka by getting(org.jetbrains.dokka.gradle.DokkaTask::class) {
            outputFormat    = "html"
            outputDirectory = "$buildDir/javadoc"
            subProjects     = listOf("Animation", "Browser", "Controls", "Core", "Themes")

            multiplatform {
                val global by creating {
                    // Use to include or exclude non public members.
                    includeNonPublic = false

                    // Do not output deprecated members. Applies globally, can be overridden by packageOptions
                    skipDeprecated = true

                    // Emit warnings about not documented members. Applies globally, also can be overridden by packageOptions
                    reportUndocumented = true

                    // Do not create index pages for empty packages
                    skipEmptyPackages = true
                }
            }
        }
    }
}