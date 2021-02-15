# Rendering Text
----------------

Doodle is explicit about text rendering, and requires a location for all text drawn to a Canvas. The following View draw's "hello" at `0,0`.

```kotlin
class TextView: View() {
    override fun render(canvas: Canvas) {
        canvas.text("hello", Origin, color = Black)
    }
}
```

The above can also be achiEved with the following:

```kotlin
val textView = view {
    render = {
        text("hello", Origin, color = Black)
    }
}
```

## Text alignment

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
                Point((width - textSize.width) / 2, (height - textSize.height) / 2),
                Black.paint)
    }
}
```
?> This View could also compute the text location on size changes.

## Fonts

You can specify a font when drawing text or have Doodle fallback to the default. Fonts can be tricky, since
they may not be present on the system at render time. This presents a race-condition for drawing text.

Doodle provides [`FontLoader`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/FontLoader.kt#L18)
to help with this.

?> Use the [`FontModule`](https://github.com/nacular/doodle/blob/master/Browser/src/jsMain/kotlin/io/nacular/doodle/application/Modules.kt#L124) 
to get access to `FontLoader`

### System Fonts

You can use `FontLoader` to check the system asynchronously for a given font. This allows you to check for OS fonts, or fonts that have
been loaded previously.

```kotlin
import kotlinx.coroutines.GlobalScope

class MyApp(fonts: FontLoader): Application {
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

### Font Files

You can also load a font from a file using `FontLoader`. This is similar to finding a loaded font, but it takes a font file url.

```kotlin
val font = GlobalScope.async {
            // Load this front from the file at "urlToFont"
            fonts("urlToFont") {
                family = "Roboto"
                size   = 14
                weight = 400
            }
        }

//...

font.await()
``` 

### Handling Timeouts

`FontLoader` uses Kotlin's `suspend` functions for its async methods. [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html)
are a flexible way of dealing with async/await semantics. You can support timeouts using `launch` and canceling the resulting Job
after some duration.

```kotlin
import kotlinx.coroutines.GlobalScope

class MyApp(fonts: FontLoader, scheduler: Scheduler): Application {
    init {
        val view = view {}
        
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