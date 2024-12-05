package io.nacular.doodle.drawing

public class FrostedGlassPaint(public val color: Color, public val blurRadius: Double): Paint() {
    override val visible: Boolean get() = color.visible

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FrostedGlassPaint

        if (color      != other.color     ) return false
        if (blurRadius != other.blurRadius) return false

        return true
    }

    override fun hashCode(): Int {
        var result = color.hashCode()
        result = 31 * result + blurRadius.hashCode()
        return result
    }
}

/**
 * Creates a new [FrostedGlassPaint] from the given [Color].
 *
 * The paint will have a [blurRadius][FrostedGlassPaint.blurRadius] of `10.0` and a default
 * opacity if the color is fully opaque.
 *
 * @see glassPaint
 */
public inline val Color.glassPaint: FrostedGlassPaint get() = this.glassPaint(10.0)

/**
 * Creates a new [FrostedGlassPaint] from the given [Color].
 *
 * @param blurRadius of the paint
 * @param opacity of the paint, defaults to `0.25` if the color is fully opaque
 */
public fun Color.glassPaint(blurRadius: Double, opacity: Float = if (this.opacity < 1f) this.opacity else 0.25f): FrostedGlassPaint = FrostedGlassPaint(this opacity opacity, blurRadius)