# Mouse Handling
----------------

### Mouse handling is very straight forward with Doodle.

The first thing you need to do is include the [`mouseModule`]() when launching your app.

```kotlin
class MyApp(display: Display): Application {
    // ...
}
```
```kotlin
import com.nectar.doodle.application.Modules.Companion.mouseModule

fun main () {
    // "full screen" launch with mouse support
    application(modules = setOf(mouseModule)) {
        MyApp(display = instance())
    }
}
```

Here you can see that the underlying framework, and not you app, depends on the `mouseModule`. Doodle uses opt-in modules like this to improve
bundle size.

## Hit Detection

The framework relies on the `contains(Point)` method to determine when the mouse is within a View's boundaries. The default
implementation just checks the `bounds`. It also accounts for the View's `transform`. However, more complex hit detection logic can be
used to customize mouse handling.

```kotlin
import com.nectar.doodle.geometry.Circle

class CircularView(val radius: Double): View() {
    private val circle = Circle(radius)

    override fun contains(point: Point) = super.contains(point) &&
        (point - position) in circle

    override fun render(canvas: Canvas) {
        canvas.circle(circle, ColorBrush(Color.red))
    }
}
``` 
This view renders a circle and provides precise hit detection for it.

?> An overlay panel with click-through could always return `false` for the contains check.

## Mouse Listeners

Views are able to receive mouse events once the `mouseModule` is loaded, they are `visible` and `enabled`. You can
then attach a [`MouseListener`]() to any View and get notified.

Mouse listeners are notified whenever the mouse:
- **Enters** a View
- **Pressed** within a View
- **Released** within a View
- **Exits** a View

You get these notifications by registering with a View's `mouseChanged` property.

```kotlin
view.mouseChanged += object: MouseListener {
    override fun mousePressed(event: MouseEvent) {
        // ..
    }
}
```

?> [`MouseListener`]() has no-op defaults for the 4 events, so you only need to implement the ones you need.

Notice that `mouseChanged`--like other observable properties--supports many observers and enables you to add/remove
an observer any time.

## Mouse Event

The event provided to mouse listeners carries information about the View it originated from, various attributes about
the state of the mouse--like buttons pressed--and the location of the cursor relative to the target View.

Mouse events are **consumable**. This means any observer can call `consume()` on the event and prevent subsequent
listeners from receiving it.

```kotlin
// ...
override fun mousePressed(event: MouseEvent) {
    // ... take action based on event

    event.consume() // indicate that no other listeners should be notified
}
// ..
```