# Rendering
-----------

## Views manage rendering

You can override the [`render`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L478)
method to draw a `View`'s contents to the screen. The framework calls this method whenever a View needs a visual update.

```kotlin
class RectView: View() {
    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, ColorBrush(Blue))
    }
}
```

This `RectView` draws a filled rectangle covering its bounds.

?> `render` is automatically called on `size` changes and `visibile` changing to `true`

## Efficient Rendering

Doodle optimizes rendering to avoid re-applying operations when rendering the same content repeatedly. For example, the `Timer` app
below renders the epoch time every millisecond. However, Doodle only updates the changing regions in the DOM. The text in this case.

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
                canvas.rect(bounds.atOrigin, Pen(Red))
                canvas.text("${clock.epoch}", Origin, ColorBrush(Black))
                canvas.rect(bounds.at(y = 20.0), ColorBrush(Green))
            }
        }
    }
    //..
}
```
---
## Draw with Canvas

The `render` method provides a [`Canvas`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/Canvas.kt#L23)
onto which a `View` can draw. Canvas offers a rich set of operations for geometric shapes, paths, images, and text. It
also supports different [`Brush`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/Brush.kt#L3)
types (i.e. [`ColorBrush`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/ColorBrush.kt#L4),
[`LinearGradientBrush`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/LinearGradientBrush.kt#L5), and
[PatternBrush](rendering.md?id=pattern-brushes)) for filling regions.

The Canvas provided to `render` has a coordinate system anchored to the View's origin, so `0,0` on the View and Canvas are the same point.
The Canvas itself extends in all directions beyond the bounds of the View; but the contents drawn to it will be clipped to the view's
bounds by default.

?> Sub-classes can disable clipping by setting [`clipCanvasToBounds`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L122)
to `false`.

### Canvas transforms

```kotlin
class MyView: View() {
    override fun render(canvas: Canvas) {
        canvas.flipHorizontally(around = width / 2) {
            text("hello", Origin, ColorBrush(Black))
        }
    }
}
```

Here, the view flips the Canvas horizontally around its mid-point and draws some text. You can apply any [`AffineTransform`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/AffineTransform.kt#L16)
to a Canvas; these can be nested as well.

### Pattern Brushes

Sometimes you need to fill a region with a repeating pattern: often an image. Doodle has the [`PatterBrush`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/PatternBrush.kt#L10) to make this easy.
This brush has a "render" body that provides a powerful and familiar way of creating repeating patterns.

You create this brush by specifying a `size` and a `fill` lambda, which has access to the full `Canvas` APIs.

```kotlin
private inner class MyView: View() {
    val stripeWidth = 20.0
    var brushAngle: Measure<Angle> = 0 * degrees
        set(new) { field = new; rerender() }

    override fun render(canvas: Canvas) {
        val brushCenter = Point(canvas.size.width / 2, canvas.size.height / 2)

        canvas.rect(bounds.atOrigin, stripedBrush(
                stripeWidth  = stripeWidth,
                evenRowColor = Red,
                oddRowColor  = White,
                transform    = Identity.rotate(around = brushCenter, by = brushAngle)
        ))
    }
}
```

Here, `horizontalStripes` will fill shapes with a repeating red and white stripe pattern.

```doodle
{
    "border": false,
    "height": "300px",
    "run"   : "DocApps.patternBrush"
}
``` 