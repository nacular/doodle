# Rendering
-----------

### Views are responsible for rendering

You can override the [`render`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/core/View.kt#L478)
method to draw a `View`'s contents to the screen. The framework calls this method whenever a View needs a visual update.

```kotlin
class RectView: View() {
    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, ColorBrush(Color.blue))
    }
}
```

The `RectView` draws a filled rectangle covering its entire area.

?> `render` is automatically called on `size` changes and `visibile` changing to `true`

### Efficient Rendering

Doodle optimizes rendering to avoid re-applying operations when rendering the same content repeatedly.

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
                canvas.rect(bounds.atOrigin, Pen(red))
                canvas.text("${clock.epoch}", Origin, ColorBrush(black))
                canvas.rect(bounds.at(y = 20.0), ColorBrush(green))
            }
        }
    }
    //..
}
```

The `Timer` app renders the epoch time every millisecond. The DOM gets minimal updates since only the text changes.

## Drawing on Canvas

The `render` method provides a [`Canvas`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/drawing/Canvas.kt#L23)
onto which the `View` can draw. Canvas offers a rich set of operations for geometric shapes, paths, images, and text. It
also supports different [`Brush`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/drawing/Brush.kt#L3)
types for filling regions.

This Canvas has a coordinate system anchored at the View's top-left corner: `0,0` on the Canvas. The Canvas itself extends in all
directions beyond the bounds of the View; but the contents drawn to it will be clipped to the view's bounds by default.

?> sub-classes can disable clipping by setting [`clipCanvasToBounds`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/core/View.kt#L122)
to `false`

### You can also transform a Canvas.

```kotlin
class MyView: View() {
    override fun render(canvas: Canvas) {
        canvas.flipHorizontally(around = width / 2) {
            text("hello", Origin, ColorBrush(black))
        }
    }
}
```

Here, the view flips the Canvas horizontally around its mid-point and draws some text. You can apply any [`AffineTransform`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/drawing/AffineTransform.kt#L16)
to a Canvas; these can be nested as well.

### Pattern Brushes

Sometimes you need to fill a region with a repeating pattern: often an image. Doodle has the [`PatterBrush`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/drawing/PatternBrush.kt#L10) to make this easy.
This brush has a "render" body that provides a powerful and familiar way of creating repeating patterns.

You create this brush by specifying a `size` and a `fill` lambda. The lambda--defined as `Canvas.() -> Unit`--has access to
all the `Canvas` APIs.

```kotlin
val rowHeight = 20.0
val horizontalStripes = PatternBrush(Size(rowHeight, 2 * rowHeight)) {
    rect(Rectangle(                rowHeight, rowHeight), ColorBrush(red  ))
    rect(Rectangle(0.0, rowHeight, rowHeight, rowHeight), ColorBrush(white))
}

class MyView: View() {
    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, horizontalStripes)
    }
}
```

Here, `horizontalStripes` will fill shapes with a repeating red and white stripe pattern. 