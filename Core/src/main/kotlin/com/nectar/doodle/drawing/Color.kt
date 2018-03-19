package com.nectar.doodle.drawing

import com.nectar.doodle.units.Angle
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.degrees
import kotlin.math.abs
import kotlin.math.round

private fun Int.toHex(): String {

    var i              = this
    var hash           = ""
    val alphabet       = "0123456789abcdef"
    val alphabetLength = alphabet.length

    do {
        hash  = alphabet[i % alphabetLength] + hash
        i    /= alphabetLength
    } while (i > 0)

    return hash
}

private data class RGB(val red: Int, var green: Int, var blue: Int)

private fun toRgb(hex: Int): RGB {
    val range = 0..0xffffff

    require(hex in range) { "hex value must be in $range" }

    return RGB(
            hex and 0xff0000 shr 16,
            hex and 0x00ff00 shr  8,
            hex and 0x0000ff)
}

class Color(
        val red    : Int,
        val green  : Int,
        val blue   : Int,
        val opacity: Float = 1f) {

    private constructor(rgb: RGB, opacity: Float = 1f): this(rgb.red, rgb.green, rgb.blue, opacity)

    constructor(hex: Int, opacity: Float = 1f): this(toRgb(hex), opacity)

    init {
        val range = 0..0xff

        require(red     in range) { "red must be in $range"      }
        require(green   in range) { "green must be in $range"    }
        require(blue    in range) { "blue must be in $range"     }
        require(opacity in 0..1 ) { "opacity must be in ${0..1}" }
    }

    private val decimal: Int by lazy { /*((opacity * 255).toInt() shl 32) +*/ (red shl 16) + (green shl 8) + blue }

    val visible = opacity > 0
    val hexString: String by lazy { decimal.toHex().padStart(6, '0') }

    fun darker(percent: Float = 0.5f) = HslColor(this).darker(percent).toRgb()

    fun lighter(percent: Float = 0.5f) = HslColor(this).lighter(percent).toRgb()

    fun with(opacity: Float) = Color(red, green, blue, opacity)

    val inverted by lazy { Color(0xffffff xor decimal) }

    override fun hashCode() = arrayOf(decimal, opacity).contentHashCode()

    override fun toString() = "$hexString: $opacity"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Color) return false

        if (opacity != other.opacity) return false
        if (decimal != other.decimal) return false

        return true
    }

    companion object {
        val red         = Color(0xff0000)
        val pink        = Color(0xffc0cb)
        val blue        = Color(0x0000ff)
        val cyan        = Color(0x00ffff)
        val gray        = Color(0xa9a9a9)
        val black       = Color(0x000000)
        val green       = Color(0x00ff00)
        val white       = Color(0xffffff)
        val yellow      = Color(0xffff00)
        val orange      = Color(0xffa500)
        val magenta     = Color(0xff00ff)
        val darkgray    = Color(0x808080)
        val lightgray   = Color(0xd3d3d3)
        val transparent = black.with(opacity = 0f)
    }
}

class HslColor(val hue: Measure<Angle>, val saturation: Float, val lightness: Float, val opacity: Float = 1f) {

    fun darker(percent: Float = 0.5f): HslColor {
        require(percent in 0f .. 1f)

        return if (percent == 0f) this else HslColor(hue, saturation, lightness * (1f - percent), opacity)
    }

    fun lighter(percent: Float = 0.5f): HslColor {
        require(percent in 0f .. 1f)

        return if (percent == 0f) this else HslColor(hue, saturation, lightness + (1f - lightness) * percent, opacity)
    }

    fun toRgb(): Color {
        val c = (1.0 - abs(2 * lightness - 1)) * saturation
        val x = c * (1.0 - abs((hue / 60.degrees) % 2 - 1))
        val m = lightness - c / 2

        val rgb: List<Double> = when (hue `in` degrees) {
            in   0 until  60 -> listOf(  c,   x, 0.0)
            in  60 until 120 -> listOf(  x,   c, 0.0)
            in 120 until 180 -> listOf(0.0,   c,   x)
            in 180 until 240 -> listOf(0.0,   x,   c)
            in 240 until 300 -> listOf(  x, 0.0,   c)
            in 300 until 360 -> listOf(  c, 0.0,   x)
            else -> emptyList() // FIXME: Handle case
        }

        return Color(
                round(255 * (rgb[0] + m)).toInt(),
                round(255 * (rgb[1] + m)).toInt(),
                round(255 * (rgb[2] + m)).toInt(),
                opacity)
    }

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

    override fun toString() = "$hue, $saturation, $lightness"

    companion object {
        operator fun invoke(rgb: Color): HslColor {
            val r     = rgb.red   / 255.0
            val g     = rgb.green / 255.0
            val b     = rgb.blue  / 255.0
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

            return HslColor(hue.degrees, saturation, lightness, rgb.opacity)
        }
    }
}