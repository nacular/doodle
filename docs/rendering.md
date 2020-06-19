# Rendering
-----------

## Views manage rendering

You can override the [`render`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L484)
method to draw a `View`'s contents to the screen. The framework calls this method whenever a View needs a visual update.

```kotlin
class RectView: View() {
    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, ColorFill(Blue))
    }
}
```

This `RectView` draws a filled rectangle covering its bounds.

?> `render` is automatically called on `size` changes and `visibile` changing to `true`

## Efficient Rendering

Doodle optimizes rendering to avoid re-applying operations when rendering the same content repeatedly. For example, the `Timer` app
below renders the epoch time every millisecond. However, Doodle only updates the changing regions in the DOM. The text in this case.

?> Doodle uses [Measured](https://nacular.github.io/measured/) for time, angles etc.

```kotlin
class Timer(display: Display, clock: Clock, scheduler: Scheduler): Application {
    init {
        display.children += object: View() {
            init {
                size = Size(200)

                scheduler.every(1 * milliseconds) {
                    rerender()
                }
            }

            override fun render(canvas: Canvas) {
                canvas.rect(bounds.atOrigin, Stroke(Red))
                canvas.text("${clock.epoch}", Origin, ColorFill(Black))
                canvas.rect(bounds.at(y = 20.0), ColorFill(Green))
            }
        }
    }
    //..
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
also supports different [`Fill`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/Fill.kt#L3)
types (i.e. [`ColorFill`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/ColorFill.kt#L5),
[`LinearGradientFill`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/LinearGradientFill.kt#L6), and
[PatternFill](rendering.md?id=pattern-fills)) for filling regions.

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
            text("hello", Origin, ColorFill(Black))
        }
    }
}
```

Here, the view flips the Canvas horizontally around its mid-point and draws some text. You can apply any [`AffineTransform`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/AffineTransform.kt#L16)
to a Canvas; these can be nested as well.

### Pattern Fills

Sometimes you need to fill a region with a repeating pattern: often an image. Doodle has the [`PatterFill`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/PatternFill.kt#L13) to make this easy.
This fill has a "render" body that provides a powerful and familiar way of creating repeating patterns.

You create this fill by specifying a `size` and a `fill` lambda, which has access to the full `Canvas` APIs.

**io.nacular.doodle.drawing.PatternFill.kt**

```kotlin
fun stripedFill(stripeWidth : Double,
                 evenRowColor: Color? = null,
                 oddRowColor : Color? = null,
                 transform   : AffineTransform = Identity) =
PatternFill(Size(if (evenRowColor.visible || oddRowColor.visible) stripeWidth else 0.0, 2 * stripeWidth), transform) {
    evenRowColor?.let { rect(Rectangle(                 stripeWidth, stripeWidth), ColorFill(it)) }
    oddRowColor?.let  { rect(Rectangle(0.0, stripeWidth, stripeWidth, stripeWidth), ColorFill(it)) }
}
```

```kotlin
private inner class MyView: View() {
    val stripeWidth = 20.0
    var fillAngle: Measure<Angle> = 0 * degrees
        set(new) { field = new; rerender() }

    override fun render(canvas: Canvas) {
        val fillCenter = Point(canvas.size.width / 2, canvas.size.height / 2)

        canvas.rect(bounds.atOrigin, stripedFill(
                stripeWidth  = stripeWidth,
                evenRowColor = Red,
                oddRowColor  = White,
                transform    = Identity.rotate(around = fillCenter, by = fillAngle)
        ))
    }
}
```

```doodle
{
    "border": false,
    "height": "300px",
    "run"   : "DocApps.patternFill"
}
``` 

This app shows how a `PatternFill` can be rotated around its center.