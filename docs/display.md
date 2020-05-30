# The Display
-------------

## An app's root container

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

## Adding Views to the Display

The Display has a `children` property that contains all its direct descendants. These top-level views have no
`parent` and are the top-most ancestors of all other Views in an app. An app can have any number of these Views.

!> Top-level Views are displayed, yet they have no `parent`. Similarly, Views can have parents even before they are
in the display hierarchy: when the parents themselves are not displayed. This means the `parent` property says nothing
about a View being in the Display hierarchy. Luckily, View has the `displayed` property for this exact purpose.

Add a top-level View like this.

```kotlin
class MyApp(display: Display): View() {
    init {
        display.children += view
    }
    // ...
}
```

And remove it like this.

```kotlin
class MyApp(display: Display): View() {
    init {
        display.children -= view
    }
    // ...
}
```

## Launch mode changes the Display

A [**Stand-Alone**](applications.md?id=stand-alone) app that uses the entire page will have a Display tied to the page body. While
an app hosted in an element will have a Display that is tied to that element.

```kotlin
application(modules = listOf(/*...*/)) {
    MyApp(instance /*,...*/)
}
```

Here the Display will be tied to `someDiv`.

```kotlin
application(root = someDiv, modules = listOf(/*...*/)) {
    MyApp(instance /*,...*/)
}
```

The Displays for a [**Nested app**](applications.md?id=nested) sits within the View hosting it. This means changes to that View's
size will change the Display size. 