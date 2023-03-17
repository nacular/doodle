<div style="text-align:center"><img src="docs/img/doodle.svg" alt="doodle" style="height:100px;margin-bottom:50px"></div>
<div style="text-align:center"><h1>A pure Kotlin, UI framework</h1></div>

[![Kotlin 1.7.21](https://img.shields.io/badge/Kotlin-1.7.21-blue.svg?style=for-the-badge&logo=kotlin&logoColor=white)](http://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](https://github.com/pusolito/doodle/blob/master/LICENSE)
[![Chat: on slack](https://img.shields.io/badge/slack-doodle-green.svg?style=for-the-badge&logo=slack)](https://kotlinlang.slack.com/messages/doodle)

## My Fork With Tutorial Examples Included

To learn Kotlin, I am developing my kotlin web apps inside this fork, always generating doodle source maps for debugging.  I keep it all together in one monorepo to make learning Kotlin, and doodle dev a bit more convenient.

Notes to self:

**Never** attempt to create a PR to be merged across forks into the **nacular** repo.

**Never** attempt to merge anything from my fork to the **nacular** repo.

## About Upgrading to Gradle 8.0.2

I could not build (or clean) doodle on Ubnuntu 22.04 using Gradle v7.1.1 due to missing gradle script dedependency:

    org.jetbrains.kotlin.com.intellij.pom.java.LanguageLevel

Before upgrading to v8.0.2, I tried using the same version the `io.nacular.measured:measured` project uses (`v7.1.5`), and that did work, but I want to use the latest gradle version to keep it in line with the Gradle version used to build my java back-end services.  

## About Not Using IR Compilation

I cannot reference `measured` dependendencies from doodle's core if I use `IR`.  I tried including `:measured` in this project, and importing it into `:core` as a project dependency, but that did not work.  I do not think abandoning `IR` is a problem for me because I am not building mobile apps -- at least not this year.  I have a steep-enough learning curve already.

### Single Language
Doodle is written entirely in [Kotlin](http://kotlinlang.org) and so are its apps. Doodle Web [Applications](https://nacular.github.io/doodle/docs/applications) do not use HTML,
CSS styles or Javascript libraries. In fact, apps are not aware of the Browser (or Browser concepts) at all, and can be written entirely
as common (cross-platform) code in [multi-platform](https://kotlinlang.org/docs/reference/platform-specific-declarations.html) setups.

### Multiple Platforms
Doodle supports both JS (Browser) and JVM (alpha) targets; so common code apps are able to run in the Browser and Desktop (with other platforms
planned) without modification. This means you write widgets once and use them on either platform. The only difference is how apps are launched on each 
platform.

### Expressive
Creating expressive, intuitive apps is natural with Doodle. It makes complex rendering easy with powerful, vector-oriented rendering,
provides fully customizable layouts and simplifies pointer and keyboard handling.

Simply define your View hierarchy and business logic, then go.

### Vector Oriented
It is easy to build beautifully detailed UIs with Doodle. All rendering in Doodle is vector-oriented; so ellipses, paths,
lines, gradients, affine transforms etc. are as simple to use as images and rectangles. 

### Precise

Doodle gives you control over all aspects of the UI presentation, including pixel-level positioning, making it easier to precisely
control rendering.

### Modular

Doodle has several libraries and a collection of modules. This allows selective adoption of various features and helps
with bundle size. Apps written with Doodle are also dependency-injected; and there are no global objects or state to make mocking challenging. 

## Hello World
```kotlin
import io.nacular.doodle.application.Application
import io.nacular.doodle.application.application
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.text
import org.kodein.di.instance

class HelloWorld(display: Display): Application {
    init {
        display += view {
            size   = display.size
            render = {
                text("Hello, world!", color = Black)
            }
        }
    }

    override fun shutdown() {}
}

fun main() {
    application {
        HelloWorld(display = instance())
    }
}
```
## Documentation

Check out the [documentation](https://nacular.github.io/doodle/) site for more details and examples.

## Tutorials

You can find helpful tutorials as [doodle-tutorials](https://nacular.github.io/doodle-tutorials).

## Feedback

Doodle is still under active development, so there are going to be gaps and bugs. Please report [issues](https://github.com/pusolito/doodle/issues),
and submit feature requests.

You can also join the discussion on the [#doodle](https://kotlinlang.slack.com/messages/doodle) Kotlin Slack channel.
