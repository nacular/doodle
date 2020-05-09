# Rendering
-----------

### Views are responsible for rendering

You can override the `render` method to draw a `View`'s contents to the screen. The framework calls this method
whenever the View needs a visual update.

```kotlin
class RectView: View() {
    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, ColorBrush(Color.blue))
    }
}
```

Here, the `RectView` draws a filled rectangle covering its entire area.

?> `render` is automatically called on size changes and `visibility` changing to `true`

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

The `render` method provides a [`Canvas`]() onto which the `View` can draw. Canvas provides a rich set of operations
for geometric shapes, paths, images, and text. It also supports different `Brush` types for filling regions.

The Canvas provided to a View has a coordinate system anchored at the View's top-left corner. This point is
`0,0` on the Canvas. The Canvas itself extends in all directions beyond the bounds of the View; but the contents drawn
to it will be clipped to the view's bounds by default.

?> sub-classes can disable clipping by setting `clipCanvasToBounds` to `false`

You can also transform a Canvas.

```kotlin
class MyView: View() {
    override fun render(canvas: Canvas) {
        canvas.flipHorizontally(around = width / 2) {
            text("hello", Origin, ColorBrush(black))
        }
    }
}
```

Here, the view flips the Canvas horizontally around its mid-point and draws some text. You can apply any `AffineTransform`
to a Canvas. Transforms can also be stacked by nesting their blocks.