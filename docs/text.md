# Text Handling

### Recall that Doodle is explicit. This applies to layout and rendering, and text rendering is no different.

Text rendering requires a location for placement.

```kotlin
class TextView: View() {
    override fun render(canvas: Canvas) {
        canvas.text("hello", Origin, ColorBrush(black))
    }
}
```

MyView draw's its text at `0,0`.

### What About Centered Text?

You need to measure text in order to align it in any way other than left-aligned at `x,y`. Doodle provides
the `TextMetrics` component for this.

```kotlin
class MyApp(/*..,*/ textMetrics: TextMetrics): Application {
    //...
}
```

Simply inject it into your app--it is included in a default modules--and pass it to Views that require it.
Here is an update to the View above that draws _hello_ in its center.

```kotlin
class CenteredTextView(textMetrics: TextMetrics): View() {
    private val textSize = textMetrics.size("hello") // cache text size

    override fun render(canvas: Canvas) {
        // compute each time to account for changes in View's width/height
        canvas.text("hello",
                Point((width - textSize.width) / 2,
                    (height - textSize.height) / 2),
                ColorBrush(black))
    }
}
```
?> this View could also compute the text location on size changes

## Fonts

### Doodle is also explicit about fonts.

You can specify a font when drawing text or have doodle fallback to the default font otherwise. Fonts can be tricky, since
they may not be present on the system at render time. This presents a race-condition for drawing text.

Doodle provides a `FontDetector` to help with this. This component checks the system asynchronously for a given font and returns
only when it has been found.

```kotlin
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
are a flexible way of dealing with async/await semantics.