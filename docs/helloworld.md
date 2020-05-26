# Hello World
-------------

The classic "Hello World" app in Doodle. This version displays "Hello, world!" in the top-left of the page.

```kotlin
class HelloWorld(display: Display): Application {
    init {
        display.children += object: View() {
            init { size = display.size }

            override fun render(canvas: Canvas) {
                canvas.text("Hello, world!", at = Origin, brush = ColorBrush(black))
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

This is an example of a top-level [**Application**](applications.md).