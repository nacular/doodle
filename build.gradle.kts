plugins {
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.kover")
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
    }
}

dependencies {
    subprojects.forEach { project ->
        project(":${project.name}").let {
            kover(it)
            dokka(it)
        }
    }
}