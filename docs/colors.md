# Rendering Colors
------------------

Doodle supports RGBA colors via the [`Color`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/Color.kt#L12)
class. It uses this for things like `foregroundColor` and `backgroundColor`, and with Strokes and Paints.

```kotlin
val color1 = Color(0xff0000u) // create red
val color2 = Color(0xffffffu, alpha = 0.5f) // white with 0.5 alpha
```

Doodle also has a small set of common colors predefined.

```kotlin
canvas.rect(rect, Color.Blue.paint) // use helper to generate Paint from predefined color
```

### Doodle also has HSL and HSV Colors

Sometimes it is more effective to work within other color spaces. There are two other color classes for doing this. These colors cannot be used
directly with Views or Paints, but they have methods for easily transforming to and from RGBA.

The [`ColorPicker`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/ColorPicker.kt#L46) controls uses HSV internally for example.