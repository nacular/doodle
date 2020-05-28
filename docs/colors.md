# Rendering Colors
------------------

Doodle supports RGBA colors via the [`Color`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/drawing/Color.kt#L12)
class. It uses this for things like `foregroundColor` and `backgroundColor`, and with Pens and brushes.

```kotlin
val color1 = Color(0xff0000u) // create red
val color2 = Color(0xffffffu, alpha = 0.5f) // white with 0.5 alpha
```

Doodle also has a small set of common colors predefined.

```kotlin
canvas.rect(rect, ColorBrush(Color.Blue)) // use predefined color
```

### Doodle also has HSL and HSV Colors

Sometimes it is more effective to work within other color spaces. There are two other color classes for doing this. These colors cannot be used
directly with Views or Brushes, but they have methods for easily transforming to and from RGBA.

The ColorPicker controls uses HSV internally for example.