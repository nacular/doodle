# doodle

A pure Kotlin UI framework for the Web.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](https://github.com/pusolito/doodle/LICENSE)

Doodle lets you focus on building rich web applications without worrying about the nuances of HTML, Javascript or CSS. With doodle, you write
all parts of your application in Kotlin.

Applications in Doodle are made with View heirarchies that allow for clear encapsulation of rendering and behavior. These Views
are modular and composable, enabling reuse within or across apps.


## Features

- Pure Kotlin
- No HTML or CSS
- 

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

## Contact

- Please see [issues](https://github.com/pusolito/doodle/issues) to share bugs you find, make feature requests, or just get help with your questions.
- Don't hesitate to ⭐️ [star](https://github.com/pusolito/doodle) if you find this project useful.