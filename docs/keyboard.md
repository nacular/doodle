# Keyboard Input
----------------

### Key handling is simple with Doodle

The first thing you need to do is include the [`KeyboardModule`](https://github.com/nacular/doodle/blob/master/Browser/src/jsMain/kotlin/io/nacular/doodle/application/Modules.kt#L59)
when launching your app. The underlying framework uses the `KeyboardModule` to produce key events.

```kotlin
class MyApp(display: Display): Application {
    // key events will fire for this app when launched with
    // the KeyboardModule
}
```
```kotlin
import io.nacular.doodle.application.Modules.Companion.KeyboardModule

fun main () {
    // "full screen" launch with keyboard support
    application(modules = listOf(KeyboardModule)) {
        MyApp(display = instance())
    }
}
```
---
## Only focused Views receive key events

A View must gain `focus` in order to begin receiving key events. This ensures that only a single View
can receive key events at any time within the app.

Use the [`FocusManager`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/focus/FocusManager.kt#L9)
to control focus. It is included in the `KeyboardModule`. Just inject it into your app to begin managing the focus.

```kotlin
class MyApp(display: Display, focusManager: FocusManager): Application {
    init {
        // ...
        focusManager.requestFocus(view)
        // ...
    }
}

fun main () {
    application(modules = listOf(KeyboardModule)) {
        MyApp(display = instance(), focusManager = instance())
    }
}
```

?> Some controls (i.e. text fields) also manage their focus when styled in the native theme

---
## Key Listeners

Views are able to receive key events once the `KeyboardModule` is loaded and they have `focus`. You can
then attach a [`KeyListener`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/event/KeyListener.kt#L4)
to any View and get notified.

Key listeners are notified whenever a key is:
- **Pressed**
- **Released**

You get these notifications by registering with a View's [`keyChanged`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L299)
property.

```kotlin
view.keyChanged += object: KeyListener {
    override fun keyPressed(event: MouseEvent) {
        // ..
    }
}
```

?> `KeyListener` has no-op defaults for the 2 events, so you only need to implement the ones you need.

Notice that `keyChanged`--like other observable properties--supports many observers and enables you to add/remove
an observer any time.
---
## Key Event

The event provided to key listeners carries information about the View it originated from (`source`), and
various attributes about the key that was pressed or released.

Key events are **consumable**. This means any observer can call `consume()` on the event and prevent subsequent
listeners from receiving it.

```kotlin
// ...
override fun keyPressed(event: KeyEvent) {
    // ... take action based on event

    event.consume() // indicate that no other listeners should be notified
}
// ..
```

## Identifying Keys

### Virtual keys and text

[`KeyEvent.key`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/event/KeyEvent.kt#L211)
is a layout independent identifier that tells you which "virtual key" was pressed or which text the key can be translated into.
Most key handling use-cases should use this property to compare keys.

```kotlin
import io.nacular.doodle.event.KeyText.Companion.Backspace
import io.nacular.doodle.event.KeyText.Companion.Enter

override fun keyPressed(event: KeyEvent) {
    when (event.key) {
        Enter     -> { /* ... */ }
        Backspace -> { /* ... */ }
        // ...
    }
}
```
```kotlin
override fun keyPressed(event: KeyEvent) {
    // this will be user-appropriate text when the key pressed is not
    // one of the "named" keys (i.e. Tab, Shift, Enter, ...)
    inputText += event.key.text
}
```
### Physical keys

Some applications will require the use of "physical" keys instead of virtual ones. This makes sense for games or other apps
where the key position on a physical keyboard matters.

This information comes from [`KeyEvent.code`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/event/KeyEvent.kt#L211).

```kotlin
import io.nacular.doodle.event.KeyCode.Companion.AltLeft
import io.nacular.doodle.event.KeyCode.Companion.AltRight
import io.nacular.doodle.event.KeyCode.Companion.Backspace 

override fun keyPressed(event: KeyEvent) {
    when (event.code) {
        AltLeft   -> { /* ... */ }
        AltRight  -> { /* ... */ }
        Backspace -> { /* ... */ }
        // ...
    }
}
```

!> Physical keys do not take keyboard differences and locale into account; so avoid them if possible