# Positioning Views
-------------------

### Doodle is explicit about positioning.

Every View has an `x,y` coordinate relative to its parent. This position is exactly where the View will
be rendered. Doodle ensures that there is never a disconnect between a View's position and screen coordinates.

?> 0,0 is the default position

```kotlin
val view = object: View() {}.apply { size = Size(100.0) }

display.children += view // view's position is 0,0
```

### Manual Positioning

Set the View's `x`, `y`, or `position` properties to update its location.

```kotlin
view.x = 10.0                 // move to 10,0
view.position = Point(13, -2) // reposition to 13,-2
```

### Layouts

Doodle also supports automatic positioning of Views via [`Layout`](). A Layout monitors a View and
updates its children's positions whenever the View's `size` changes, or any of the View's children
have a `bounds` change. 

The View class also `protects` its `layout` property from callers. But sub-classes are free to expose
it.

```kotlin
val box = Box()

box.layout = HorizontalFlowLayout() // Box exposes its layout
```

Given the above uses one of Doodle's existing Layouts to keep `box`'s children wrapped from left to right within its bounds.

### Create Custom Layouts

Custom Layouts are simple to build. Just implement the `Layout` interface:

```kotlin
class CustomLayout: Layout {
    override fun layout(container: PositionableContainer) {
        container.children.filter { it.visible }.forEach { child ->
            child.bounds = Rectangle(/*...*/)
        }
    }
}
```

?> Layouts do not work with View directly because it does not expose its children. `PositionableContainer` proxies the
managed View instead.

### Constraints Based Layout

Doodle provides a useful Layout mechanism that is based on constraints. This covers many of the common layout
use cases and is easy to use.

```kotlin
val container = Box() // a simple container
val panel1    = object: View() {}
val panel2    = object: View() {}

container.children += listOf(panel1, panel2)

// use Layout that follows constraints to position items
container.layout = constrain(panel1, panel2) { panel1, panel2 ->
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

!> `constrain` only supports positioning for siblings within the same parent