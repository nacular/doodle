plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlinVersion.get()}")
    implementation(gradleApi())
    implementation("com.gradleup.nmcp:nmcp:${libs.versions.nmcpVersion.get()}")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:${libs.versions.dokkaVersion.get()}")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:${libs.versions.koverVersion.get()}")
}