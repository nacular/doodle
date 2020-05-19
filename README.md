<div align="center"><img src="docs/doodle.svg" alt="doodle" style="height:100px;margin-bottom:50px"></div>
<div align="center"><h1>A pure Kotlin UI framework for the Web.</h1></div>

----

[![Kotlin 1.3.70](https://img.shields.io/badge/Kotlin-1.3.70-blue.svg?style=flat&logo=kotlin)](http://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://github.com/pusolito/doodle/blob/master/LICENSE)

**Expressive:** Creating expressive, intuitive apps is natural with doodle. It handles all complex rendering and layout timing for you. Simply
define your View hierarchy, business logic and go. User interactions (i.e. mouse and keyboard) are consistent and easy to use.

**Single-language:** Doodle apps do not use HTML, Javascript, CSS or wrappers around these concepts. Instead, you write all parts of your
application in a browser agnostic way using [Kotlin](http://kotlinlang.org). Doodle apps can actually be written entirely as common code in
multi-platform setups.

**Vector Oriented:** Its easy to build beautifully detailed UIs with doodle. All rendering in doodle is vector-oriented; so ellipses, paths,
lines, gradients, affine transforms etc. are as simple to use as images and rectangles. 

**Transparent:** Doodle gives you control over all aspects of the UI presentation, making it easier to precisely control rendering.

**Testable:** Apps written with doodle are dependency-injected; and there are no global objects or state to make mocking challenging.

## Installation

## Examples

### Hello World

```kotlin
class HelloWorld(display: Display): Application {
    init {
        display.children += object: View() {
            init { size = display.size }

            override fun render(canvas: Canvas) {
                canvas.text("Hello World!", at = Origin, brush = ColorBrush(black))
            }
        }
    }

    override fun shutdown() {}
}

fun main() {
    // app initialization, with DI via Kodein
    application {
        HelloWorld(display = instance())
    }
}
```

### Simple Button
```kotlin
display.children += PushButton("Submit").apply {
    fired += {
        println("$text pressed")
    }
}
```

### Constraint Layout
```kotlin
// All Views are absolutely positioned and layouts are explicit
// Views can have Layouts that control how their children are positioned

val container = Box() // a simple container
val panel1    = object: View() {}
val panel2    = object: View() {}

container.children += listOf(panel1, panel2)

// create a special Layout that follows constraints to position items
container.layout = constrain(panel1, panel2) { panel1, panel2 ->
    panel1.top    = parent.top
    panel1.left   = parent.left
    panel1.right  = parent.right
    panel1.height = constant(100.0)
    
    panel2.top    = panel1.bottom
    panel2.left   = panel1.left
    panel2.right  = panel1.right
    panel2.bottom = parent.bottom
}
```

## Contact

- Please see [issues](https://github.com/pusolito/doodle/issues) to share bugs you find, make feature requests, or just get help with your questions.
- Don't hesitate to ⭐️ [star](https://github.com/pusolito/doodle) if you find this project useful.