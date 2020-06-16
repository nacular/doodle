<div align="center"><img src="docs/doodle.svg" alt="doodle" style="height:100px;margin-bottom:50px"></div>
<div align="center"><h1>A pure Kotlin, UI framework for the Web</h1></div>

----

[![Kotlin 1.3.72](https://img.shields.io/badge/Kotlin-1.3.72-blue.svg?style=flat&logo=kotlin)](http://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://github.com/pusolito/doodle/blob/master/LICENSE)

----
## Why Doodle?

### Single-language
Doodle is written entirely in [Kotlin](http://kotlinlang.org) and so are its apps. Doodle [Applications](applications.md) do not use HTML,
CSS styles or Javascript libraries. In fact, apps are not aware of the Browser (or Browser concepts) at all, and can be written entirely
as common (cross-platform) code in [multi-platform](https://kotlinlang.org/docs/reference/platform-specific-declarations.html) setups.

### Expressive
Creating expressive, intuitive apps is natural with Doodle. It makes complex rendering easy with powerful, vector-oriented rendering,
provides fully customizable layouts and simplifies pointer and keyboard handling.

Simply define your View hierarchy, business logic and go.

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
class HelloWorld(display: Display): Application {
    init {
        display.children += object: View() {
            init { size = display.size }

            override fun render(canvas: Canvas) {
                canvas.text("Hello, world!",
                    at = Origin, 
                    brush = ColorBrush(Black))
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

## Feedback

Doodle is still under active development, so there are going to be gaps and bugs. Please report [issues](https://github.com/pusolito/doodle/issues),
and submit feature requests.