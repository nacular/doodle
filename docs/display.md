# The Display
-------------

### The Display is an app's root container.

The Display holds an app's View hierarchy, and behaves like a basic container. It is not a View however, so many of the capabilities
of Views are not available for the Display.

?> [Layouts](positioning.md?id=layouts-1) can be applied to the Display as well.

The Display is available for injection by default.

```kotlin
class MyApp(display: Display): Application {
    init {
        // ...
    }

    override fun shutdown() {}
}

fun main() {
    application {
        MyApp(display = instance())
    }
}
```

### Add Views to the Display to make them top-level.

The Display has a `children` property that contains all its direct descendants. These top-level views have no
`parent` and are the top-most ancestors of all other Views in an app. An app can have any number of these Views.

Add a top-level View like this.

```kotlin
class MyApp(display: Display): View() {
    init {
        display.children += view
    }
    // ...
}
```

Removing a top-level View is just as simple.

```kotlin
class MyApp(display: Display): View() {
    init {
        display.children -= view
    }
    // ...
}
```

### An app's launch mode changes the Display.

[**Stand-alone**](applications.md?id=stand-alone) apps use the entire page by default, and have a Display tied to the page body.

```kotlin
application(modules = listOf(/*...*/)) {
    MyApp(instance /*,...*/)
}
```

An app can also be hosted in an element, which ties the Display to that element.

```kotlin
application(root = someDiv, modules = listOf(/*...*/)) {
    MyApp(instance /*,...*/)
}
```

Here the Display will be tied to `someDiv`.

A [**nested app's**](applications.md?id=nested) `Display` is tied to its host View.