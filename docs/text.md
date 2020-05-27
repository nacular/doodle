# Rendering Text
----------------

Doodle is explicit about text rendering, and requires a location for all text drawn to a Canvas. The following View draw's "hello" at `0,0`.

```kotlin
class TextView: View() {
    override fun render(canvas: Canvas) {
        canvas.text("hello", Origin, ColorBrush(Black))
    }
}
```

### What About Centered Text?

You need to measure text in order to align it in any way other than left-aligned at `x,y`. Doodle provides
the `TextMetrics` component for this. Simply inject it into your app and Views. 

```kotlin
class MyApp(/*..,*/ textMetrics: TextMetrics): Application {
    //...
}
```

Here is a View that draws some centered text.

```kotlin
class CenteredTextView(textMetrics: TextMetrics): View() {
    private val textSize = textMetrics.size("hello") // cache text size

    override fun render(canvas: Canvas) {
        // compute each time to account for changes in View's width/height
        canvas.text("hello",
                Point((width - textSize.width) / 2,
                    (height - textSize.height) / 2),
                ColorBrush(Black))
    }
}
```
?> This View could also compute the text location on size changes.

## Fonts

### Doodle is also explicit about fonts.

You can specify a font when drawing text or have Doodle fallback to the default font otherwise. Fonts can be tricky, since
they may not be present on the system at render time. This presents a race-condition for drawing text.

Doodle provides a [`FontDetector`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/drawing/FontDetector.kt#L18)
to help with this. It checks the system asynchronously for a given font and returns only when it has been found.

```kotlin
import kotlinx.coroutines.GlobalScope

class MyApp(fonts: FontDetector): Application {
    init {
        // launch a new coroutine for async font lookup
        val font = GlobalScope.async {
            fonts {
                family = "Roboto"
                size   = 14
                weight = 400
            }
        }
        
        //...
        
        font.await()
    }
    
    //...
}
```

`FontDetector` uses Kotlin's `suspend` functions for its async methods. [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html)
are a flexible way of dealing with async/await semantics. You can support timeouts using `launch` and canceling the resulting Job
after some duration.

```kotlin
import kotlinx.coroutines.GlobalScope

class MyApp(fonts: FontDetector, scheduler: Scheduler): Application {
    init {
        val view: View = object: View() {}
        
        // launch a new coroutine for async font lookup
        val fontJob = GlobalScope.launch {
            // assigns the font when the job resolves
            view.font = fonts {
                family = "Roboto"
                size   = 14
                weight = 400
            }
        }

        // Cancel the job after 5 seconds
        scheduler.after(5 * seconds) {
            fontJob.cancel()
        }
    }

    //...
}
```