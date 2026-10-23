
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier.Protected
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier.Public

plugins {
    id("maven-publish"      )
    id("signing"            )
    id("com.gradleup.nmcp"  )
    id("org.jetbrains.dokka")
}

repositories {
    mavenCentral()
    maven       ("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    mavenLocal  ()
}

dokka {
    dokkaPublications.html {
        moduleName.set(project.name.replaceFirstChar { it.titlecase() })
        moduleVersion.set(project.version.toString())
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
                create("kotlin-stdlib"   ) { url("https://kotlinlang.org/api/latest/jvm/stdlib/") }
                create("kotlinx-datetime") { url("https://kotlinlang.org/api/kotlinx-datetime/" ) }
                create("measured"        ) {
                    url           ("https://nacular.github.io/measured-api/"                      )
                    packageListUrl("https://nacular.github.io/measured-api/-measured/package-list")
                }
            }
        }
    }
}

// To generate documentation in HTML
val dokkaHtmlJar by tasks.registering(Jar::class) {
    description = "A HTML Documentation JAR containing Dokka HTML"
    from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        if (name.lowercase() == "jvm") {
            artifact(dokkaHtmlJar)
        }

        pom {
            name.set       ("Doodle"                                             )
            description.set("A pure Kotlin, UI framework for the Web and Desktop")
            url.set        ("https://github.com/nacular/doodle"                  )
            licenses {
                license {
                    name.set("MIT"                                )
                    url.set ("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set  ("pusolito"     )
                    name.set("Nicholas Eddy")
                }
            }
            scm {
                url.set                ("https://github.com/nacular/doodle.git"      )
                connection.set         ("scm:git:git://github.com/nacular/doodle.git")
                developerConnection.set("scm:git:git://github.com/nacular/doodle.git")
            }
        }
    }
}

signing {
    setRequired({
        project.hasProperty("release") && gradle.taskGraph.hasTask("publish")
    })
    useGpgCmd()
    sign(publishing.publications)
}