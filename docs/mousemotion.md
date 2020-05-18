# Mouse Motion Handling
-----------------------

Mouse motion events occur whenever the mouse moves within a View. They are treated separately from [**mouse events**](mouse.md?id=mouse-handling)
because of their high frequency. The [`mouseModule`](mouse.md?id=mouse-handling-is-very-straight-forward-with-doodle) is also
required to enable them. And [**hit detection**](mouse.md?id=hit-detection) follows the same rules as with mouse events.

Registration is different though. You use listen to the [`mouseMotionChanged`]() property and implement [`MouseMotionListener`]().

Mouse motion listeners are notified whenever the mouse:
- **Moves** within a View
- **Drags** anywhere while pressed, if the press started in a View

```kotlin
view.mouseMotionChanged += object: MouseMotionListener {
    override fun mouseMoved(event: MouseEvent) {
        // ..
    }

    override fun mouseDragged(event: MouseEvent) {
        // ..
    }
}
```
 