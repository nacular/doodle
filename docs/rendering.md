# Rendering
-----------

## Views manage rendering

You can override the [`render`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L484)
method to draw a `View`'s contents to the screen. The framework calls this method whenever a View needs a visual update.

```kotlin
class RectView: View() {
    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, Blue.paint)
    }
}
```

This `RectView` draws a filled rectangle covering its bounds. The [`paint`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/ColorPaint.kt#L32) 
extension on [`Color`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/Color.kt#L12) 
creates a new [`ColorPaint`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/ColorPaint.kt#L5).

?> `render` is automatically called on `size` changes and `visible` changing to `true`

## Efficient Rendering

Doodle optimizes rendering to avoid re-applying operations when rendering the same content repeatedly. For example, the `Timer` app
below renders the epoch time every millisecond. However, Doodle only updates the changing regions in the DOM. The text in this case.

?> Doodle uses [Measured](https://nacular.github.io/measured/) for time, angles etc.

```kotlin
class Timer(display: Display, clock: Clock, scheduler: Scheduler): Application {
    init {
        display += view {
            size = Size(200)

            scheduler.every(1 * milliseconds) {
                rerender()
            }

            render = {
                rect(bounds.atOrigin, Stroke(Red))
                text("${clock.epoch}", color = Black)
                rect(bounds.at(y = 20.0), color = Green)
            }
        }
    }
    
    override fun shutdown() {}
}
```

```doodle
{
    "border": false,
    "height": "200px",
    "run"   : "DocApps.timer"
}
```
---
## Draw with Canvas

The `render` method provides a [`Canvas`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/Canvas.kt#L24)
onto which a `View` can draw. Canvas offers a rich set of operations for geometric shapes, paths, images, and text. It
also supports different [`Paint`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/Paint.kt#L4)
types (i.e. [`ColorPaint`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/ColorPaint.kt#L5),
[`LinearGradientPaint`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/LinearGradientPaint.kt#L31), and
[PatternPaint](rendering.md?id=pattern-paints)) for filling regions.

The Canvas provided to `render` has a coordinate system anchored to the View's origin, so `0,0` on the View and Canvas are the same point.
The Canvas itself extends in all directions beyond the bounds of the View; but the contents drawn to it will be clipped to the view's
bounds by default.

?> Sub-classes can disable clipping by setting [`clipCanvasToBounds`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L123)
to `false`.

### Canvas transforms

```kotlin
class MyView: View() {
    override fun render(canvas: Canvas) {
        canvas.flipHorizontally(around = width / 2) {
            text("hello", Origin, Black.paint)
        }
    }
}
```

Here, the view flips the Canvas horizontally around its mid-point and draws some text. You can apply any [`AffineTransform`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/AffineTransform.kt#L16)
to a Canvas; these can be nested as well.

### Pattern Paints

Sometimes you need to fill a region with a repeating pattern: often an image. Doodle has the [`PatternPaint`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/PatternPaint.kt#L13) to make this easy.
This paint has a "render" body that provides a powerful and familiar way of creating repeating patterns.

You create this paint by specifying a `size` and a `paint` lambda, which has access to the full `Canvas` APIs.

**io.nacular.doodle.drawing.PatternPaint.kt**

```kotlin
fun stripedPaint(stripeWidth : Double,
                 evenRowColor: Color? = null,
                 oddRowColor : Color? = null,
                 transform   : AffineTransform = Identity) =
PatternPaint(Size(if (evenRowColor.visible || oddRowColor.visible) stripeWidth else 0.0, 2 * stripeWidth), transform) {
    evenRowColor?.paint?.let { rect(Rectangle(                  stripeWidth, stripeWidth), it) }
    oddRowColor?.paint?.let  { rect(Rectangle(0.0, stripeWidth, stripeWidth, stripeWidth), it) }
}
```

```kotlin
private inner class MyView: View() {
    val stripeWidth = 20.0
    var paintAngle by renderProperty(0 * degrees)

    override fun render(canvas: Canvas) {
        val paintCenter = Point(canvas.size.width / 2, canvas.size.height / 2)

        canvas.rect(bounds.atOrigin, stripedPaint(
                stripeWidth  = stripeWidth,
                evenRowColor = Red,
                oddRowColor  = White,
                transform    = Identity.rotate(around = paintCenter, by = paintAngle)
        ))
    }
}
```

```doodle
{
    "border": false,
    "height": "300px",
    "run"   : "DocApps.patternPaint"
}
``` 

This app shows how a `PatternPaint` can be transformed, like rotated around its center for example.