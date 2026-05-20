
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier.Protected
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier.Public

plugins {
    id("org.jetbrains.dokka")
}

repositories {
    mavenCentral()
}

dokka {
    pluginsConfiguration.html {
        homepageLink.set("https://github.com/nacular/doodle")
        customAssets.setFrom(file("logo-icon.svg"))
        footerMessage.set("© 2024 Nacular")
        separateInheritedMembers = true
        mergeImplicitExpectActualDeclarations = true
    }

    // Sets properties for the whole project
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("documentation/html"))
        dokkaSourceSets.configureEach {
            // Do not output deprecated members. Applies globally, can be overridden by packageOptions
            skipDeprecated.set(true)

            // Emit warnings about not documented members. Applies globally, also can be overridden by packageOptions
            reportUndocumented.set(true)

            // Do not create index pages for empty packages
            skipEmptyPackages.set(true)

            includes.from("Module.md")

            documentedVisibilities.set(setOf(Public, Protected))

            sourceLink {
                localDirectory.set(rootProject.projectDir)
                remoteUrl("https://github.com/nacular/doodle/tree/master")
                remoteLineSuffix.set("#L")
            }

            externalDocumentationLinks {
                // "kotlin-stdlib" is a unique name for this link configuration
                register("kotlin-stdlib") { url("https://kotlinlang.org/api/latest/jvm/stdlib/") }

                register("kotlinx-datetime") { url("https://kotlinlang.org/api/kotlinx-datetime") }

                create("measured") {
                    url("https://nacular.github.io/measured-api")
                    packageListUrl("https://nacular.github.io/measured-api/-measured/package-list")
                }
            }
        }
    }
}

dependencies {
    subprojects.forEach {
        dokka(project(":${it.name}"))
    }
}