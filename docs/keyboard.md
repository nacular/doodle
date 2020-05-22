# Key Input
-----------

### Key handling is simple with Doodle.

The first thing you need to do is include the [`keyboardModule`]() when launching your app.

```kotlin
class MyApp(display: Display): Application {
    // ...
}
```
```kotlin
import com.nectar.doodle.application.Modules.Companion.mouseModule

fun main () {
    // "full screen" launch with keyboard support
    application(modules = setOf(keyboardModule)) {
        MyApp(display = instance())
    }
}
```

### Only focused Views receive key events

A View must gain `focus` in order to begin receiving key events. This ensures that only a single View
can receive key events at any time within the app.

### Use the FocusManager to control focus

The [`FocusManager`]() is included in the [`keyboardModule`](). Just inject it into your app to begin managing
the focus.

```kotlin
class MyApp(display: Display, focusManager: FocusManager): Application {
    init {
        // ...
        focusManager.requestFocus(view)
        // ...
    }
}

fun main () {
    application(modules = setOf(keyboardModule)) {
        MyApp(display = instance(), focusManager = instance())
    }
}
```

?> Some controls (i.e. text fields) also manage their focus when styled in the native theme

## Key Listeners

Views are able to receive key events once the `keyboardModule` is loaded and they have `focus`. You can
then attach a [`KeyListener`]() to any View and get notified.

Key listeners are notified whenever a key is:
- **Pressed**
- **Released**

You get these notifications by registering with a View's `keyChanged` property.

```kotlin
view.keyChanged += object: KeyListener {
    override fun keyPressed(event: MouseEvent) {
        // ..
    }
}
```

?> [`KeyListener`]() has no-op defaults for the 2 events, so you only need to implement the ones you need.

Notice that `keyChanged`--like other observable properties--supports many observers and enables you to add/remove
an observer any time.