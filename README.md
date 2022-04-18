<div style="text-align:center"><img src="docs/img/doodle.svg" alt="doodle" style="height:100px;margin-bottom:50px"></div>
<div style="text-align:center"><h1>A pure Kotlin, UI framework</h1></div>

[![Kotlin 1.6.10](https://img.shields.io/badge/Kotlin-1.6.10-blue.svg?style=for-the-badge&logo=kotlin&logoColor=white)](http://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](https://github.com/pusolito/doodle/blob/master/LICENSE)
[![Chat: on slack](https://img.shields.io/badge/slack-doodle-green.svg?style=for-the-badge&logo=slack)](https://kotlinlang.slack.com/messages/doodle)

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
