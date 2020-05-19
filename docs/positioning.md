# Positioning Views
-------------------

### Doodle is explicit about positioning.

Every View has an `x,y` position (in pixels) relative to its parent. This is exactly where the View will be rendered--unless it also has
a `transform`. Doodle ensures that there is never a disconnect between a View's position, transform and render coordinates.

```kotlin
val view = object: View() {}.apply { size = Size(100.0) }

display.children += view // view's position is 0,0
```

### Manual Positioning

Set the View's [`x`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/core/View.kt#L77),
[`y`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/core/View.kt#L82), or
[`position`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/core/View.kt#L87) properties to update
its location.

```kotlin
view.x = 10.0                 // move to 10,0
view.position = Point(13, -2) // reposition to 13,-2
```

These are proxies to the View's [`bounds`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/core/View.kt#L110)
property, which represents its rectangular boundary relative to its parent. Views can also have
[Affine Transformations](https://en.wikipedia.org/wiki/Affine_transformation) to change how they are displayed. A transformed View still
retains the same `bounds`, but its [`boundingBox`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/core/View.kt#L158) property changes, since it reflects the smallest rectangle that encloses the View's
**transformed** bounds.

?> `boundingBox` == `bounds` when
[`transform`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/core/View.kt#L144) ==
[`Identity`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/drawing/AffineTransform.kt#L134) 

### Layouts

Doodle also supports automatic positioning of Views via [`Layout`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/core/Layout.kt#L75).
A Layout monitors a View and updates its children's positions whenever the View's `size` changes, or any of the View's children have a `bounds`
change.

The View class also `protects` its `layout` property from callers, but sub-classes are free to expose
it.

```kotlin
val box = Box()

box.layout = HorizontalFlowLayout() // Box exposes its layout
```

[`HorizontalFlowLayout`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/layout/HorizontalFlowLayout.kt#L16)
wraps a View's children from left to right within its bounds.

?> Changes to a View's `transform` will not trigger layout

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