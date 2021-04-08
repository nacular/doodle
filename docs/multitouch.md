# Multi-touch Support
-----------------------

Pointer events support multiple, simultaneous inputs by default. This covers the multi-touch use-case on mobile and other similar
scenarios. The [`PointerEvent`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/event/PointerEvent.kt#L33)
class contains information about all active [`Interaction`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/event/PointerEvent.kt#L17)s
for the current event. This includes those directed at the event target. Apps are therefore able to incorporate this into their pointer handling.

```kotlin
view.pointerMotionChanged += moved { event ->
    event.targetInteractions  // the set of interactions with the target View
    event.changedInteractions // the interactions that changed (triggered) this event
    event.allInteractions     // all active interactions for the app
}
```

Doodle also does not limit simultaneous interactions to a single View. All active interactions will be sent to the appropriate Views
and managed concurrently. This means it is possible to drag multiple items at the same time.

```doodle
{
    "height": "400px",
    "border": false,
    "run"   : "DocApps.positioning",
    "args"  : [2]
}
```

?> Try moving both boxes at the same time if you are on a mobile device or have multiple pointers.