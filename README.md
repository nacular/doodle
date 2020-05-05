# doodle

A pure Kotlin UI framework for the Web.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](https://github.com/pusolito/doodle/LICENSE)

**Expressive:** Creating expressive, intuitive apps is natural with doodle. It handles all complex rendering and layout timing for you. You simply
define your View heirarchy, business logic and go. User interactions (i.e. mouse and keyboard) are consistent and easy to use.

**Single-language:** Focus on your app and not the nuances of HTML, Javascript or CSS. With doodle, you write all parts of your application in Kotlin.
This means you get all the power of a sophisticated, strongly-typed language.

**Vector Oriented:** Its easy to build beautifully detailed UIs with doodle. All rendering in doodle is vector-oriented. So ellipses, paths,
lines, affine transforms, gradients, etc. are simple to use. 

**Transparent:** Doodle hides the complexity of HTML and CSS, but it exposes control over all other aspects of UI presentation. This makes it easier
to make guarantees about what is rendered.

**Testable:** Apps written with doodle are dependency-injected.  And there are no global objects that make mocking challenging.

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

### Layout
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