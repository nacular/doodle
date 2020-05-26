# Pointer Handling
------------------

### Pointer handling is very straight forward with Doodle.

The first thing you need to do is include the [`pointerModule`](https://github.com/pusolito/doodle/blob/master/Browser/src/jsMain/kotlin/com/nectar/doodle/application/Modules.kt#L43)
when launching your app.

```kotlin
class MyApp(display: Display): Application {
    // ...
}
```
```kotlin
import com.nectar.doodle.application.Modules.Companion.pointerModule

fun main () {
    // "full screen" launch with pointer support
    application(modules = listOf(pointerModule)) {
        MyApp(display = instance())
    }
}
```

Here you can see that the underlying framework, and not you app, depends on the `pointerModule`. Doodle uses opt-in modules like this to improve
bundle size.

## Hit Detection

The framework relies on the [`View.contains(Point)`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/core/View.kt#L450)
method to determine when the pointer is within a View's boundaries.

?> This point is within the View's parent coordinate system (or the [**Display**](display.md?id=the-display-is-an-apps-root-container)'s for
top-level Views).

The default implementation just checks the point against [`bounds`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/core/View.kt#L108).
It also accounts for the View's [`transform`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/core/View.kt#L142).
However, more complex hit detection can be used to customize pointer handling.

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

## Pointer Listeners

Views are able to receive pointer events once the `pointerModule` is loaded, they are `visible` and `enabled`. You can
then attach a [`PointerListener`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/event/PointerListener.kt#L3)
to any View and get notified.

Pointer listeners are notified whenever the pointer:
- **Enters** a View
- **Pressed** within a View
- **Released** within a View
- **Exits** a View

You get these notifications by registering with a View's [`pointerChanged`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/core/View.kt#L294)
property.

```kotlin
view.pointerChanged += object: PointerListener {
    override fun pressed(event: PointerEvent) {
        // ..
    }
}
```

?> [`PointerListener`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/event/PointerListener.kt#L3)
has no-op defaults for the 4 events, so you only need to implement the ones you need.

Notice that `pointerChanged`--like other observable properties--supports many observers and enables you to add/remove
an observer any time.

## Pointer Event

The event provided to pointer listeners carries information about the View it originated from (`source`), the View it is sent to (`target`),
various attributes about the state of the pointer--like buttons pressed--and the location of the cursor relative to the target View.

Pointer events are **consumable**. This means any observer can call `consume()` on the event and prevent subsequent
listeners from receiving it.

```kotlin
// ...
override fun pressed(event: PointerEvent) {
    // ... take action based on event

    event.consume() // indicate that no other listeners should be notified
}
// ..
```

## Event Bubbling

### Pointer events "bubble" up to ancestors of a View.

Pointer events sent to a View will also be sent up to its parent and so on. This means you can listen to all events that happen
to the descendants of a View.

The event sent to a parent if slightly different from the one sent to the View. These events continue to have the same `source`
(descendant View where the event fired), but their `target` changes to the recipient ancestor as they bubble.

!> Bubbling is canceled if any listener calls `consume`

## Event Filtering

### Pointer events also "sink" from root ancestors down to their target.

The first phase of pointer event handling is the "sink" phase. It runs **before** the "bubbling" phase. The root ancestor and all
descendants toward the `target` View are notified of the event before the target is.

### The filter phase is like the "bubbling" phase in reverse

Like with bubbling, `PointerListener` is used to handle the event. Unlike "bubbling", registration happens via the [`pointerFilter`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/core/View.kt#L289)
property. This phase lets ancestors "veto" an event before it reaches the intended target.

```kotlin
view.pointerFilter += object: PointerListener {
    // called whenever the pointer is pressed on this
    // View or its children, before the target child
    // is notified
    override fun pressed(event: PointerEvent) {
        // ..
    }
}
```

?> Calling `consume` during filter will prevent descendants (and the target) from receiving the event