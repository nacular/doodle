# Display
---------

### The Display is an app's root container.

The Display holds an app's View hierarchy, and behaves like a basic container with [**layout**]() functionality. It even supports
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

### Constraint layouts also work with the Display

However, you **cannot** use the [`constraint`]() function directly since it has no way of accessing the [`Display`]() to resolve dimensions
for the `parent` property (doodle avoids global state). You must use the `Display.constrain` extension function instead.

```kotlin
val panel1 = object: View() {}
val panel2 = object: View() {}

display.children += listOf(panel1, panel2)

// use Layout that follows constraints to position items
display.layout = display.constrain(panel1, panel2) { panel1, panel2 ->
    panel1.top    = parent.top
    panel1.left   = parent.left
    panel1.right  = parent.right
    panel1.height = constant(100.0)
    
    panel2.top    = panel1.bottom
    panel2.left   = panel1.left
    panel2.right  = panel1.right
    panel2.bottom = parent.bottom
}
```
!> Notice the call to `display.constrain` instead of `constrain`

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