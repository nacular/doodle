package com.nectar.doodle.drawing

import kotlin.math.max
import kotlin.math.min

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

    fun darker(times: Int = 1): Color {
        var red   = this.red
        var green = this.green
        var blue  = this.blue

        for (i in 0 until times) {
            red   = max(0f, red   * scaleFactor).toInt()
            green = max(0f, green * scaleFactor).toInt()
            blue  = max(0f, blue  * scaleFactor).toInt()
        }

        return Color(red, green, blue, opacity)
    }

    fun lighter(times: Int = 1): Color {
        var red   = this.red
        var green = this.green
        var blue  = this.blue

        val i = (1.0 / (1.0 - scaleFactor)).toInt()

        if (red == 0 && green == 0 && blue == 0) {
            return Color(i, i, i, opacity)
        }

        for (j in 0 until times) {
            red   = min(red   / scaleFactor, 255f).toInt()
            green = min(green / scaleFactor, 255f).toInt()
            blue  = min(blue  / scaleFactor, 255f).toInt()

            if (red   in 1 until i) { red   = i }
            if (green in 1 until i) { green = i }
            if (blue  in 1 until i) { blue  = i }
        }

        return Color(red, green, blue, opacity)
    }

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

        private val scaleFactor = 0.9f
    }
}