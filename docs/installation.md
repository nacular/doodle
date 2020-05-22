# Installation
--------------

Doodle apps are built using [Gradle](http://www.gradle.org), like other Kotlin JS or multi-platform projects.
Learn more by checking out [Setting up a Kotlin/JS project](https://kotlinlang.org/docs/tutorials/javascript/setting-up.html).

### Pure JS Project

You can set up a pure Javascript app with the following **build.gradle** script.

```groovy
plugins {
    id 'org.jetbrains.kotlin.js' version '1.3.72'
}

apply plugin: 'idea'
apply plugin: 'maven'

version = '1.0'
group   = 'com.my.cool.app'

repositories {
    jcenter     ()
    mavenCentral()
}

ext {
    doodle_version = '0.1.0' // <--- Latest Doodle version
}

kotlin {
    target.browser.testTask {
        enabled = false
    }

    dependencies {
        implementation "org.jetbrains.kotlin:kotlin-stdlib-js"

        implementation "com.nectar.doodle:core:$doodle_version"
        implementation "com.nectar.doodle:browser:$doodle_version"
        
        // Optional
        // implementation "com.nectar.doodle:controls:$doodle_version"
        // implementation "com.nectar.doodle:animation:$doodle_version"
        // implementation "com.nectar.doodle:themes:$doodle_version"
    }
}
```

### Multi-platform Project

Doodle is a set of Kotlin Multi-platform (MPP) libraries. Which means you can create an MPP for your app as well. The advantage of this
is that you can write your app almost entirely in `common` code and test it with very fast unit tests on the JVM.

?> App [**launch code**](applications.md?id=app-launch) is the only portion that needs to be in `js`

```groovy
plugins {
    id 'org.jetbrains.kotlin.multiplatform' version '1.3.72'
}

repositories {
    jcenter     ()
    mavenCentral()
}

ext {
    doodle_version = '0.1.0' // <--- Latest Doodle version
}

kotlin {
    js {
        browser.testTask {
            enabled = false
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation "org.jetbrains.kotlin:kotlin-stdlib-common"

            implementation "com.nectar.doodle:core:$doodle_version"
            implementation "com.nectar.doodle:browser:$doodle_version"
        
            // Optional
            // implementation "com.nectar.doodle:controls:$doodle_version"
            // implementation "com.nectar.doodle:animation:$doodle_version"
            // implementation "com.nectar.doodle:themes:$doodle_version"
        }

        jsMain.dependencies {
            implementation "org.jetbrains.kotlin:kotlin-stdlib-js"
        }
    }
}
```