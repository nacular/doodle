# Display
---------

### The Display is an app's root container.

The Display holds an app's View hierarchy, and behaves like a basic container with [**layout**](positioning.md?id=layouts-1) functionality. It even supports
the same `layout` objects used with Views to position its children.

### Adding/removing top-level Views

Add top-level Views using the Display's `children` property.

```kotlin
class MyApp(display: Display): View() {
    init {
        display.children += view
    }
    // ...
}
```

Removing a View is just as simple.

```kotlin
class MyApp(display: Display): View() {
    init {
        display.children -= view
    }
    // ...
}
```

### An app's launch mode changes the Display

[**Stand-alone**](applications.md?id=stand-alone) apps use the entire page by default, and have a Display tied to the page body.

```kotlin
application(modules = setOf(/*...*/)) {
    MyApp(instance /*,...*/)
}
```

An app can also be hosted in an element, which ties the Display to that element.

```kotlin
application(root = someDiv, modules = setOf(/*...*/)) {
    MyApp(instance /*,...*/)
}
```

Here the Display will be tied to `someDiv`.

A [**nested app's**](applications.md?id=nested) [`Display`]() is tied to that View.