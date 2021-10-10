# Installation
--------------

Doodle apps are built using [Gradle](http://www.gradle.org), like other Kotlin JS or Multi-Platform projects.
Learn more by checking out  the Kotlin [docs](https://kotlinlang.org/docs/getting-started.html).

---

## Pure JS Project

You can set up a pure Javascript app with the following build scripts.

<!-- tabs:start -->

#### ** build.gradle.kts **

```kotlin
plugins {
    id ("org.jetbrains.kotlin.js") version "1.5.30"
}

version = "1.0.0"
group   = "com.my.cool.app"

repositories {
    mavenCentral()
}

kotlin {
    js().browser()

    val doodleVersion = "0.6.0" // <--- Latest Doodle version

    dependencies {
        implementation ("io.nacular.doodle:core:$doodleVersion"   )
        implementation ("io.nacular.doodle:browser:$doodleVersion")

        // Optional
        // implementation ("io.nacular.doodle:controls:doodleVersion" )
        // implementation ("io.nacular.doodle:animation:doodleVersion")
        // implementation ("io.nacular.doodle:themes:doodleVersion"   )
    }
}
```

#### ** build.gradle **

```groovy
plugins {
    id 'org.jetbrains.kotlin.js' version '1.5.30'
}

version = '1.0.0'
group   = 'com.my.cool.app'

repositories {
    mavenCentral()
}

ext {
    doodle_version = '0.6.0' // <--- Latest Doodle version
}

kotlin {
    js().browser()

    dependencies {
        implementation "io.nacular.doodle:core:$doodle_version"
        implementation "io.nacular.doodle:browser:$doodle_version"

        // Optional
        // implementation "io.nacular.doodle:controls:$doodle_version"
        // implementation "io.nacular.doodle:animation:$doodle_version"
        // implementation "io.nacular.doodle:themes:$doodle_version"
    }
}
```
<!-- tabs:end -->

## Pure JVM Project

You can set up a pure JVM app with the following build scripts.

<!-- tabs:start -->

#### ** build.gradle.kts **

```kotlin
plugins {
    id ("org.jetbrains.kotlin.jvm") version "1.5.30"
    application
}

version = "1.0.0"
group   = "com.my.cool.app"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

kotlin {
    target.compilations.all {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    val doodleVersion = "0.6.0" // <--- Latest Doodle version

    dependencies {
        implementation ("io.nacular.doodle:core:$doodleVersion"   )
        implementation ("io.nacular.doodle:desktop:$doodleVersion")

        // Optional
        // implementation ("io.nacular.doodle:controls:doodleVersion" )
        // implementation ("io.nacular.doodle:animation:doodleVersion")
        // implementation ("io.nacular.doodle:themes:doodleVersion"   )
    }
}

application {
    mainClass.set("YOUR_CLASS")
}
```

#### ** build.gradle **

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.5.30'
    id 'application'
}

version = '1.0.0'
group   = 'com.my.cool.app'

repositories {
    mavenCentral()
    maven {
        url "https://maven.pkg.jetbrains.space/public/p/compose/dev"
    }
}

ext {
    doodle_version = '0.6.0' // <--- Latest Doodle version
}

kotlin {
    target.compilations.all {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    dependencies {
        implementation "io.nacular.doodle:core:$doodle_version"
        implementation "io.nacular.doodle:desktop:$doodle_version"

        // Optional
        // implementation "io.nacular.doodle:controls:$doodle_version"
        // implementation "io.nacular.doodle:animation:$doodle_version"
        // implementation "io.nacular.doodle:themes:$doodle_version"
    }
}

application {
    mainClassName = "YOUR_CLASS"
}
```
<!-- tabs:end -->

## Multi-platform Project

Doodle is a set of Kotlin Multi-platform (MPP) libraries. Which means you can create an MPP for your app as well. The advantage of this
is that you can write your app entirely (except for `main`) in `common` code and make it available on both Web (JS) and Desktop (JVM). The 
following shows how to create such an app. 

?> App [**launch code**](applications.md?id=app-launch) is the only portion that needs to be in `js` or `jvm`.

<!-- tabs:start -->

#### ** build.gradle.kts **

```kotlin
plugins {
    id ("org.jetbrains.kotlin.multiplatform") version "1.5.30"
    application
}

version = "1.0.0"
group   = "com.my.cool.app"

repositories {
    mavenCentral()
}

kotlin {
    js().browser()

    jvm {
        withJava()
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    val doodleVersion = "0.6.0" // <--- Latest Doodle version

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation ("io.nacular.doodle:core:$doodleVersion")

                // Optional
                // implementation ("io.nacular.doodle:controls:doodleVersion" )
                // implementation ("io.nacular.doodle:animation:doodleVersion")
                // implementation ("io.nacular.doodle:themes:doodleVersion"   )
            }
        }

        val jsMain by getting {
            dependencies {
                implementation ("io.nacular.doodle:browser:$doodleVersion")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation ("io.nacular.doodle:desktop:$doodleVersion")
            }
        }
    }
}

application {
    mainClass.set("YOUR_CLASS")
}
```

#### ** build.gradle **

```groovy
plugins {
    id 'org.jetbrains.kotlin.multiplatform' version '1.5.30'
    id 'application'
}

version = '1.0.0'
group   = 'com.my.cool.app'

repositories {
    mavenCentral()
}

ext {
    doodle_version = '0.6.0' // <--- Latest Doodle version
}

kotlin {
    js().browser()

    jvm {
        withJava()
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation "io.nacular.doodle:core:$doodle_version"

            // Optional
            // implementation "io.nacular.doodle:controls:$doodle_version"
            // implementation "io.nacular.doodle:animation:$doodle_version"
            // implementation "io.nacular.doodle:themes:$doodle_version"
        }

        jsMain.dependencies {
            implementation "io.nacular.doodle:browser:$doodle_version"
        }

        jvmMain.dependencies {
            implementation "io.nacular.doodle:desktop:$doodle_version"
        }
    }
}

application {
    mainClassName = "YOUR_CLASS"
}
```
<!-- tabs:end -->