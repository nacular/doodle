import org.jetbrains.dokka.DokkaConfiguration.Visibility.PROTECTED
import org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.net.URL

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlinVersion.get()}")
    }
}

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    signing
}

repositories {
    mavenCentral()
}

subprojects {
    apply (plugin = "maven-publish"              )
    apply (plugin = "signing"                    )
    apply (plugin = "org.jetbrains.dokka"        )
    apply (plugin = "org.jetbrains.kotlinx.kover")

    repositories {
        mavenCentral()
        maven       { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        mavenLocal  ()
    }

    val dokkaJar by tasks.creating(Jar::class) {
        group       = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles Kotlin docs with Dokka"
        archiveClassifier.set("javadoc")
        from(tasks.dokkaHtml)
    }

    setupPublication(dokkaJar)

    setupSigning()

    tasks.withType<DokkaTaskPartial>().configureEach {
        outputDirectory.set(buildDir.resolve("javadoc"))

        dokkaSourceSets.configureEach {
            // Do not output deprecated members. Applies globally, can be overridden by packageOptions
            skipDeprecated.set(false)

            // Emit warnings about not documented members. Applies globally, also can be overridden by packageOptions
            reportUndocumented.set(true)

            // Do not create index pages for empty packages
            skipEmptyPackages.set(true)

            includes.from("Module.md")

            documentedVisibilities.set(setOf(PUBLIC, PROTECTED))

            sourceLink {
                localDirectory.set(rootProject.projectDir)
                remoteUrl.set(URL("https://github.com/nacular/doodle/tree/master"))
                remoteLineSuffix.set("#L")
            }

            externalDocumentationLink {
                url.set(URL("https://kotlinlang.org/api/latest/jvm/stdlib/"))
            }

            externalDocumentationLink {
                url.set(URL("https://kotlinlang.org/api/kotlinx-datetime"))
                packageListUrl.set(URL("https://kotlinlang.org/api/kotlinx-datetime/kotlinx-datetime/package-list"))
            }

            externalDocumentationLink {
                url.set(URL("https://nacular.github.io/measured-api"))
                packageListUrl.set(URL("https://nacular.github.io/measured-api/measured/package-list"))
            }
        }
    }
}