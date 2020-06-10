# Installation
--------------

Doodle apps are built using [Gradle](http://www.gradle.org), like other Kotlin JS or multi-platform projects.
Learn more by checking out [Setting up a Kotlin/JS project](https://kotlinlang.org/docs/tutorials/javascript/setting-up.html).

---

## Pure JS Project

You can set up a pure Javascript app with the following **build.gradle** script.

```groovy
plugins {
    id 'org.jetbrains.kotlin.js' version '1.3.72'
}

version = '1.0.0'
group   = 'com.my.cool.app'

repositories {
    jcenter     ()
    mavenCentral()
}

ext {
    doodle_version = '0.1.0' // <--- Latest Doodle version
}

kotlin {
    dependencies {
        implementation "org.jetbrains.kotlin:kotlin-stdlib-js"

        implementation "io.nacular.doodle:core:$doodle_version"
        implementation "io.nacular.doodle:browser:$doodle_version"
        
        // Optional
        // implementation "io.nacular.doodle:controls:$doodle_version"
        // implementation "io.nacular.doodle:animation:$doodle_version"
        // implementation "io.nacular.doodle:themes:$doodle_version"
    }
}
```

## Multi-platform Project

Doodle is a set of Kotlin Multi-platform (MPP) libraries. Which means you can create an MPP for your app as well. The advantage of this
is that you can write your app entirely (except for `main`) in `common` code and test it with very fast unit tests on the JVM.

?> App [**launch code**](applications.md?id=app-launch) is the only portion that needs to be in `js`

```groovy
plugins {
    id 'org.jetbrains.kotlin.multiplatform' version '1.3.72'
}

version = '1.0.0'
group   = 'com.my.cool.app'

repositories {
    jcenter     ()
    mavenCentral()
}

ext {
    doodle_version = '0.1.0' // <--- Latest Doodle version
}

kotlin {
    js {}

    sourceSets {
        commonMain.dependencies {
            implementation "org.jetbrains.kotlin:kotlin-stdlib-common"

            implementation "io.nacular.doodle:core:$doodle_version"
            implementation "io.nacular.doodle:browser:$doodle_version"
        
            // Optional
            // implementation "io.nacular.doodle:controls:$doodle_version"
            // implementation "io.nacular.doodle:animation:$doodle_version"
            // implementation "io.nacular.doodle:themes:$doodle_version"
        }

        jsMain.dependencies {
            implementation "org.jetbrains.kotlin:kotlin-stdlib-js"
        }
    }
}
```