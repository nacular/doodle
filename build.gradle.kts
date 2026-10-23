plugins {
    id("org.jetbrains.dokka"          )
    id("org.jetbrains.kotlinx.kover"  )
    id("com.gradleup.nmcp.aggregation")
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

nmcpAggregation {
    centralPortal {
        username       = findProperty("mavenCentralUsername")?.toString()
        password       = findProperty("mavenCentralPassword")?.toString()
        publishingType = "USER_MANAGED"
    }
}

dependencies {
    subprojects.forEach { project ->
        project(":${project.name}").let {
            kover          (it)
            dokka          (it)
            nmcpAggregation(it)
        }
    }
}