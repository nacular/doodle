plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
    mavenLocal()
}

val kotlinVersion: String by System.getProperties()

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation(gradleApi())
}