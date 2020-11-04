# Positioning Views
-------------------

## Explicit positioning

Every View has an `x,y` position (in pixels) relative to its parent. This is exactly where the View will be rendered--unless it also has
a `transform`. Doodle ensures that there is never a disconnect between a View's position, transform and render coordinates.

```kotlin
val panel = view { size = Size(100.0) }

display += view // view's position is 0,0
```

## Manual positioning

You can set the View's [`x`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L75),
[`y`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L80), or
[`position`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L85) properties directly
to move it around. These are proxies to the View's [`bounds`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L109)
property, which represents its rectangular boundary relative to its parent. 

```kotlin
view.x = 10.0                 // move to 10,0
view.position = Point(13, -2) // reposition to 13,-2
```

```doodle
{
    "height": "400px",
    "border": false,
    "run"   : "DocApps.positioning"
}
```

Views can also have
[Affine Transformations](https://en.wikipedia.org/wiki/Affine_transformation) to change how they are displayed. A transformed View still
retains the same `bounds`, but its [`boundingBox`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L156) property changes, since it reflects the smallest rectangle that encloses the View's
**transformed** bounds.

```doodle
{
    "height": "400px",
    "border": false,
    "run"   : "DocApps.transforms"
}
```

?> `boundingBox` == `bounds` when
[`transform`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L143) ==
[`Identity`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/AffineTransform.kt#L134).

## Automatic positioning with Layouts

A [`Layout`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/Layout.kt#L75) keeps track
of a View and its children and automatically arranges the children as sizes change. This happens whenever View's `size` changes, or one of its children has its `bounds` change.

The View class also `protects` its `layout` property from callers, but sub-classes are free to expose
it.

```kotlin
val box = Box()

box.layout = HorizontalFlowLayout() // Box exposes its layout
```

```doodle
{
    "height": "400px",
    "run"   : "DocApps.flowLayout"
}
```

[`HorizontalFlowLayout`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/layout/HorizontalFlowLayout.kt#L16)
wraps a View's children from left to right within its bounds.

!> Changes to a View's `transform` will not trigger layout.

## Constraint-based Layout

This Layout uses anchor points to pin the `top`, `left`, `bottom`, `right`, `centerX`, and `cetnerY` points of Views. It also allows you to
specify values for `width` and `height`. This covers many of the common layout use cases and is easy to use.

```kotlin
val container = Box() // a simple container
val panel1    = view {}
val panel2    = view {}

container += listOf(panel1, panel2)

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

!> `constrain` only supports positioning for siblings within the same parent.
## Custom Layouts

Doodle comes with several useful layouts, including one based on constraints. You can also create custom Layouts very easily.
Just implement the `Layout` interface:

```kotlin
class CustomLayout: Layout {
    override fun layout(container: PositionableContainer) {
        container.children.filter { it.visible }.forEach { child ->
            child.bounds = Rectangle(/*...*/)
        }
    }
}
```

?> Layouts do not work with View directly because it does not expose its children. [`PositionableContainer`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/Layout.kt#L36) proxies the
managed View instead.