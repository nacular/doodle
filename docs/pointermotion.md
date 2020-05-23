# Pointer Motion Handling
-----------------------

Pointer motion events occur whenever the pointer moves within a View. They are treated separately from [**pointer events**](pointer.md?id=pointer-handling)
because of their high frequency. The [`pointerModule`](pointer.md?id=pointer-handling-is-very-straight-forward-with-doodle) is also
required to enable them. And [**hit detection**](pointer.md?id=hit-detection) follows the same rules as with pointer events.

Registration is different though. You use listen to the [`pointerMotionChanged`]() property and implement [`PointerMotionListener`]().

Pointer motion listeners are notified whenever the pointer:
- **Moves** within a View
- **Drags** anywhere while pressed, if the press started in a View

```kotlin
view.pointerMotionChanged += object: PointerMotionListener {
    override fun pointerMoved(event: PointerEvent) {
        // ..
    }

    override fun pointerDragged(event: PointerEvent) {
        // ..
    }
}
```
 