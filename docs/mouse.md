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

The framework relies on the `contains(Point)` method to determine when the mouse is within a View's boundaries.

?> This point is within the View's parent coordinate system (or the [**Display**](display.md?id=the-display-is-an-apps-root-container)'s for top-level Views).

The default implementation just checks the point against `bounds`. It also accounts for the View's `transform`.
However, more complex hit detection can be used to customize mouse handling.

```kotlin
import com.nectar.doodle.geometry.Circle

class CircularView(val radius: Double): View() {
    private val circle = Circle(radius)

    // calling super avoids manual handling of affine transform
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

The event provided to mouse listeners carries information about the View it originated from (`source`), the View it is sent to (`target`),
various attributes about the state of the mouse--like buttons pressed--and the location of the cursor relative to the target View.

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

## Event Bubbling

### Mouse events "bubble" up to ancestors of a View.

Mouse events sent to a View will also be sent up to its parent and so on. This means you can listen to all events that happen
to the descendants of a View.

The event sent to a parent if slightly different from the one sent to the View. These events continue to have the same `source`
(descendant View where the event fired), but their `target` changes to the recipient ancestor as they bubble.

!> Bubbling is canceled if any listener calls `consume`

## Event Filtering

### Mouse events also "sink" from root ancestors down to their target.

The first phase of mouse event handling is the "sink" phase. It runs **before** the "bubbling" phase. The root ancestor and all
descendants toward the `target` View are notified of the event before the target is.

### The filter phase is like the "bubbling" phase in reverse

Like bubbling, a [`MouseListener`]() is used to handle the event. But unlike "bubbling", registration happens via the `mouseFilter`
property. This phase lets ancestors "veto" an event before it reaches the intended target.

```kotlin
view.mouseFilter += object: MouseListener {
    // called whenever the mouse is pressed on this
    // View or its children, before the target child
    // is notified
    override fun mousePressed(event: MouseEvent) {
        // ..
    }
}
```

?> Calling `consume` during filter will prevent descendants (and the target) from receiving the event