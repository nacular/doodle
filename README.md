<div style="text-align:center"><img src="docs/img/doodle.svg" alt="doodle" style="height:100px;margin-bottom:50px"></div>
<div style="text-align:center"><h1>A pure Kotlin, UI framework</h1></div>

[![Kotlin 2.1.10](https://img.shields.io/badge/Kotlin_2.1.10-blue.svg?style=for-the-badge&logo=kotlin&logoColor=white)](http://kotlinlang.org)
[![JS Wasm, JVM](https://img.shields.io/badge/JS%2C_Wasm%2C_JVM-purple?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/docs/js-overview.html)
[![Chat: on slack](https://img.shields.io/badge/doodle-gray.svg?style=for-the-badge&logo=slack)](https://kotlinlang.slack.com/messages/doodle)
[![API](https://img.shields.io/badge/API-orange?style=for-the-badge)](https://nacular.github.io/doodle/)
[![License: MIT](https://img.shields.io/badge/MIT_License-green.svg?style=for-the-badge)](https://github.com/pusolito/doodle/blob/master/LICENSE)

Doodle helps you create beautiful, modern apps entirely in [Kotlin](http://kotlinlang.org). Its render model is intuitive yet powerful, making it easy to achieve [complex UIs](https://nacular.github.io/doodle-tutorials/docs/introduction) with pixel level precision and layouts. This simplicity and power applies to everything from user input to drag and drop. Doodle lets you build and animate anything.

Start creating your app for Web and Desktop; just define your View hierarchy and business logic, and go.

https://github.com/nacular/doodle/assets/9815928/7340bb3c-b1c4-4a79-96ce-002d6f255e0b

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
## Examples and Documentation

Check out the [documentation](https://nacular.github.io/doodle/) site for more details and examples. You can also find helpful tutorials at [doodle-tutorials](https://nacular.github.io/doodle-tutorials).

Doodle is still under active development, so there are going to be gaps and bugs. Please report [issues](https://github.com/pusolito/doodle/issues), and submit feature requests.

You can also join the discussion on the [#doodle](https://kotlinlang.slack.com/messages/doodle) Kotlin Slack channel. Go to http://slack.kotl.in for instructions on getting an invitation.

## Leave a star

Let us know what you think by leaving a comment or a star ⭐️.