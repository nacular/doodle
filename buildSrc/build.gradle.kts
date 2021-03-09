plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    jcenter     ()
}

val kotlinVersion = "1.4.31" //: String by System.getProperties()

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation(gradleApi())
}