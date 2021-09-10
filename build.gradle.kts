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
    id    ("org.jetbrains.dokka") version "1.5.0"
    signing
}

allprojects {
    apply (plugin = "org.jetbrains.dokka")
    apply (plugin = "maven-publish"      )
    apply (plugin = "signing"            )
    apply (plugin = "org.jetbrains.dokka")

    repositories {
        maven       { url = uri("https://dl.bintray.com/kotlin/kotlin-eap"              ) }
        maven       { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        mavenCentral()
        mavenLocal  ()
    }

    val dokkaJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles Kotlin docs with Dokka"
        archiveClassifier.set("javadoc")
        from(tasks.dokkaHtml)
    }

    setupPublication(dokkaJar)

    setupSigning()

    tasks.dokkaHtml {
        outputDirectory.set(buildDir.resolve("javadoc"))

        dokkaSourceSets {
            configureEach {
                includeNonPublic.set(false)

                // Do not output deprecated members. Applies globally, can be overridden by packageOptions
                skipDeprecated.set(true)

                // Emit warnings about not documented members. Applies globally, also can be overridden by packageOptions
                reportUndocumented.set(true)

                // Do not create index pages for empty packages
                skipEmptyPackages.set(true)
            }
        }
    }
}