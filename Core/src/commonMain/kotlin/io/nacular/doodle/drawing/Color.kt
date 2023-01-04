package io.nacular.doodle.drawing

import io.nacular.doodle.utils.lerp
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.div
import io.nacular.measured.units.times
import kotlin.math.abs
import kotlin.math.round

/**
 * Represents an [RGBA](https://en.wikipedia.org/wiki/RGBA_color_model) color.
 *
 * @constructor creates a Color
 * @property red component
 * @property green component
 * @property blue component
 * @property opacity of the color
 */
public class Color(public val red: UByte, public val green: UByte, public val blue: UByte, public val opacity: Float = 1f) {

    private constructor(rgb: RGB, opacity: Float = 1f): this(rgb.red, rgb.green, rgb.blue, opacity)

    /**
     * Creates a new Color using the hex representation.
     *
     * ```
     *
     * val c = Color(0xff0000)
     *
     * ```
     *
     * @param hex representation of the color
     * @param opacity of the color
     */
    public constructor(hex: UInt, opacity: Float = 1f): this(hex.toRgb(), opacity)

    private val decimal: UInt by lazy { (red.toUInt() shl 16) + (green.toUInt() shl 8) + blue.toUInt() }

    init {
        require(opacity in 0f..1f) { "opacity must be in ${0..1}" }
    }

    /** `true` if [opacity] > 0 */
    public val visible: Boolean = opacity > 0

    /** Hex string representation of this Color */
    public val hexString: String by lazy { decimal.toHex().padStart(6, '0') }

    /** the inversion of this Color */
    public val inverted: Color get() = Color(0xffffffu xor decimal)

    override fun hashCode(): Int = arrayOf(decimal, opacity).contentHashCode()

    override fun toString(): String = "$hexString: $opacity"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Color) return false

        if (opacity != other.opacity) return false
        if (decimal != other.decimal) return false

        return true
    }

    public companion object {
        public val Red: Color         = Color(0xff0000u)
        public val Pink: Color        = Color(0xffc0cbu)
        public val Blue: Color        = Color(0x0000ffu)
        public val Cyan: Color        = Color(0x00ffffu)
        public val Gray: Color        = Color(0xa9a9a9u)
        public val Black: Color       = Color(0x000000u)
        public val White: Color       = Color(0xffffffu)
        public val Green: Color       = Color(0x00ff00u)
        public val Brown: Color       = Color(0xA52A2Au)
        public val Yellow: Color      = Color(0xffff00u)
        public val Orange: Color      = Color(0xffa500u)
        public val Magenta: Color     = Color(0xff00ffu)
        public val Darkgray: Color    = Color(0x808080u)
        public val Lightgray: Color   = Color(0xd3d3d3u)
        public val Transparent: Color = Black opacity 0f

        public fun blackOrWhiteContrast(color: Color): Color {
            val y = (299u * color.red + 587u * color.green + 114u * color.blue) / 1000u
            return if (y >= 128u) Black else White
        }
    }
}

/**
 * Creates a new Color like this one except with the given opacity.
 *
 * @param value of the new opacity
 * @return the new color
 */
public infix fun Color.opacity(value: Float): Color = Color(red, green, blue, value)

/**
 * Makes this Color lighter by the given percent.
 *
 * @param percent to lighted the color
 * @return the new color
 */
public fun Color.lighter(percent: Float = 0.5f): Color = HslColor(this).lighter(percent).toRgb()

/**
 * Makes this Color darker by the given percent.
 *
 * @param percent to darken the color
 * @return the new color
 */
public fun Color.darker(percent: Float = 0.5f): Color = HslColor(this).darker(percent).toRgb()

/**
 * @return a gray scale version of this Color
 */
public fun Color.grayScale(): Color {
    val gray = (red.toInt() * 0.2989f + blue.toInt() * 0.5870f + green.toInt() * 0.1140f).toInt().toUByte()
    return Color(gray, gray, gray)
}

/**
 * Represents an [HSL](https://en.wikipedia.org/wiki/HSL_and_HSV) color.
 *
 * @constructor creates a Color
 * @param hue component
 * @property saturation component
 * @property lightness component
 * @property opacity of the color
 */
public class HslColor(hue: Measure<Angle>, public val saturation: Float, public val lightness: Float, public val opacity: Float = 1f) {

    /** Hue component */
    public val hue: Measure<Angle> = ((((hue `in` degrees) % 360) + 360) % 360) * degrees

    /** `true` if [opacity] > 0 */
    public val visible: Boolean = opacity > 0

    override fun equals(other: Any?): Boolean {
        if (this === other    ) return true
        if (other !is HslColor) return false

        if (hue        != other.hue       ) return false
        if (saturation != other.saturation) return false
        if (lightness  != other.lightness ) return false
        if (opacity    != other.opacity   ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hue.hashCode()
        result = 31 * result + saturation.hashCode()
        result = 31 * result + lightness.hashCode ()
        result = 31 * result + opacity.hashCode   ()
        return result
    }

    override fun toString(): String = "$hue, $saturation, $lightness"

    public companion object {
        /**
         * Create a new [HslColor] from an RGB [Color].
         *
         * @param rgb color to convert
         */
        public operator fun invoke(rgb: Color): HslColor {
            val r     = rgb.red.toFloat()   / 255.0
            val g     = rgb.green.toFloat() / 255.0
            val b     = rgb.blue.toFloat()  / 255.0
            val max   = maxOf(r, g, b)
            val min   = minOf(r, g, b)
            val delta = max - min

            val hue = 60 * when {
                delta == 0.0 -> 0.0
                r     == max -> (((g - b) / delta) % 6)
                g     == max -> (( b - r) / delta  + 2)
                b     == max -> (( r - g) / delta  + 4)
                else         -> 0.0
            }

            val lightness  = (max + min).toFloat() / 2
            val saturation = if (delta == 0.0) 0f else (delta / (1 - abs(2 * lightness - 1))).toFloat()

            return HslColor(hue * degrees, saturation, lightness, rgb.opacity)
        }
    }
}

/**
 * Makes this color lighter by the given percent.
 *
 * @param percent to lighted the color
 * @return the new color
 */
public fun HslColor.lighter(percent: Float = 0.5f): HslColor {
    require(percent in 0f .. 1f)

    return if (percent == 0f) this else HslColor(hue, saturation, lightness + (1f - lightness) * percent, opacity)
}

/**
 * Makes this color darker by the given percent.
 *
 * @param percent to darken the color
 * @return the new color
 */
public fun HslColor.darker(percent: Float = 0.5f): HslColor {
    require(percent in 0f .. 1f)

    return if (percent == 0f) this else HslColor(hue, saturation, lightness * (1f - percent), opacity)
}

/** @return an RGB version of this Color */
public fun HslColor.toRgb(): Color {
    val c = (1.0 - abs(2 * lightness - 1)) * saturation
    val x = c * (1.0 - abs((hue / (60 * degrees)) % 2 - 1))
    val m = lightness - c / 2

    val rgb: List<Double> = when (hue `in` degrees) {
        in   0 until  60 -> listOf(  c,   x, 0.0)
        in  60 until 120 -> listOf(  x,   c, 0.0)
        in 120 until 180 -> listOf(0.0,   c,   x)
        in 180 until 240 -> listOf(0.0,   x,   c)
        in 240 until 300 -> listOf(  x, 0.0,   c)
        in 300 until 360 -> listOf(  c, 0.0,   x)
        else -> throw IllegalStateException("Invalid HSL Color: $this")
    }

    return Color(
            round(0xff * (rgb[0] + m)).toUByte(),
            round(0xff * (rgb[1] + m)).toUByte(),
            round(0xff * (rgb[2] + m)).toUByte(),
            opacity)
}

/**
 * Represents an [HSV](https://en.wikipedia.org/wiki/HSL_and_HSV) color.
 *
 * @constructor creates a Color
 * @param hue component
 * @property saturation component
 * @property value component
 * @property opacity of the color
 */
public class HsvColor(hue: Measure<Angle>, public val saturation: Float, public val value: Float, public val opacity: Float = 1f) {

    /** Hue component */
    public val hue: Measure<Angle> = ((((hue `in` degrees) % 360) + 360) % 360) * degrees

    /** `true` if [opacity] > 0 */
    public val visible: Boolean = opacity > 0

    override fun equals(other: Any?): Boolean {
        if (this === other    ) return true
        if (other !is HsvColor) return false

        if (hue        != other.hue       ) return false
        if (saturation != other.saturation) return false
        if (value      != other.value     ) return false
        if (opacity    != other.opacity   ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hue.hashCode()
        result = 31 * result + saturation.hashCode()
        result = 31 * result + value.hashCode     ()
        result = 31 * result + opacity.hashCode   ()
        return result
    }

    override fun toString(): String = "$hue, $saturation, $value"

    public companion object {
        /**
         * Create a new [HsvColor] from an RGB [Color].
         *
         * @param rgb color to convert
         */
        public operator fun invoke(rgb: Color): HsvColor {
            val r     = rgb.red.toFloat  () / 255.0
            val g     = rgb.green.toFloat() / 255.0
            val b     = rgb.blue.toFloat () / 255.0
            val max   = maxOf(r, g, b)
            val min   = minOf(r, g, b)
            val delta = max - min

            val hue = 60 * when {
                delta == 0.0 -> 0.0
                r     == max -> (((g - b) / delta) % 6)
                g     == max -> (( b - r) / delta  + 2)
                b     == max -> (( r - g) / delta  + 4)
                else         -> 0.0
            }

            val saturation = if (max == 0.0) 0f else (delta / max).toFloat()

            // Get modulus instead of remainder https://stackoverflow.com/questions/5385024/mod-in-java-produces-negative-numbers
            return HsvColor(hue * degrees, saturation, max.toFloat(), rgb.opacity)
        }
    }
}

/**
 * Creates a new color like this one except with the given opacity.
 *
 * @param value of the new opacity
 * @return the new color
 */
public fun HsvColor.opacity(value: Float): HsvColor = HsvColor(hue, saturation, this.value, value)

/** @return an RGB version of this Color */
public fun HsvColor.toRgb(): Color {
    val c = (value * saturation).toDouble()
    val x = c * (1.0 - abs((hue / (60 * degrees)) % 2 - 1))
    val m = value - c

    val rgb: List<Double> = when (hue `in` degrees) {
        in   0 until  60 -> listOf(  c,   x, 0.0)
        in  60 until 120 -> listOf(  x,   c, 0.0)
        in 120 until 180 -> listOf(0.0,   c,   x)
        in 180 until 240 -> listOf(0.0,   x,   c)
        in 240 until 300 -> listOf(  x, 0.0,   c)
        in 300 until 360 -> listOf(  c, 0.0,   x)
        else -> throw IllegalStateException("Invalid HSV Color: $this")
    }

    return Color(
            round(0xff * (rgb[0] + m)).toUByte(),
            round(0xff * (rgb[1] + m)).toUByte(),
            round(0xff * (rgb[2] + m)).toUByte(),
            opacity)
}

/**
 * Picks a Color that is [fraction] within the range [[start], [end]] inclusive using linear interpolation.
 *
 * @param start color
 * @param end color
 * @param fraction of the way between them
 * @return the color
 */
public fun lerp(start: Color, end: Color, fraction: Float): Color = when(fraction) {
    0f -> start
    1f -> end
    else -> Color(
        red     = lerp(start.red.toDouble  (), end.red.toDouble  (), fraction).toInt().toUByte(),
        green   = lerp(start.green.toDouble(), end.green.toDouble(), fraction).toInt().toUByte(),
        blue    = lerp(start.blue.toDouble (), end.blue.toDouble (), fraction).toInt().toUByte(),
        opacity = lerp(start.opacity, end.opacity, fraction)
    )
}

public val Color?.visible   : Boolean get() = this?.visible ?: false
public val HslColor?.visible: Boolean get() = this?.visible ?: false
public val HsvColor?.visible: Boolean get() = this?.visible ?: false

private operator fun ClosedRange<Int>.contains(value: Double) = value.toIntExactOrNull().let { if (it != null) contains(it) else false }

private fun Double.toIntExactOrNull() = if (this in Int.MIN_VALUE.toDouble()..Int.MAX_VALUE.toDouble()) this.toInt() else null

private fun Double.toUByte() = toInt().toUByte()

private fun UInt.toHex(): String {
    var i              = this
    var hash           = ""
    val alphabet       = "0123456789ABCDEF"
    val alphabetLength = alphabet.length.toUInt()

    do {
        hash  = alphabet[(i % alphabetLength).toInt()] + hash
        i    /= alphabetLength
    } while (i > 0u)

    return hash
}

private fun UInt.toRgb(): RGB {
    require(this <= 0xffffffu) { "hex value must be in 0 .. 0xffffffu" }

    return RGB(
            (this and 0xff0000u shr 16).toUByte(),
            (this and 0x00ff00u shr  8).toUByte(),
            (this and 0x0000ffu       ).toUByte())
}

private class RGB(val red: UByte, var green: UByte, var blue: UByte)