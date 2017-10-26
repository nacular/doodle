package com.zinoti.jaz.drawing

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

private fun toRgb(hex: String): RGB {
    try {
        val rgbValue = hex.toInt(16)

        return RGB(
                rgbValue and 0xff0000 shr 16,
                rgbValue and 0x00ff00 shr 8,
                rgbValue and 0x0000ff)
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException(hex)
    }
}

class Color(
        val red    : Int   = 0,
        val green  : Int   = 0,
        val blue   : Int   = 0,
        val opacity: Float = 1f) {

    private constructor(rgb: RGB, opacity: Float = 1f): this(rgb.red, rgb.green, rgb.blue, opacity)

    constructor(hex: String, opacity: Float = 1f): this(toRgb(hex), opacity)

    init {
        val range = 0..255

        require(red     in range) { "red must be in $range"      }
        require(green   in range) { "green must be in $range"    }
        require(blue    in range) { "blue must be in $range"     }
        require(opacity in 0..1 ) { "opacity must be in ${0..1}" }
    }

    private val decimal: Int by lazy { (red shl 16) + (green shl 8) + blue }

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

    fun with(opactiy: Float) = Color(red, green, blue, opactiy)

    val inverted by lazy { Color((Int.MAX_VALUE xor decimal).toHex()) }

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
        val red         = Color("ff0000")
        val pink        = Color("ffc0cb")
        val blue        = Color("0000ff")
        val cyan        = Color("00ffff")
        val gray        = Color("a9a9a9")
        val black       = Color("000000")
        val green       = Color("00ff00")
        val white       = Color("ffffff")
        val yellow      = Color("ffff00")
        val orange      = Color("ffa500")
        val magenta     = Color("ff00ff")
        val darkgray    = Color("808080")
        val lightgray   = Color("d3d3d3")
        val Transparent = black.with(0f)

        private val scaleFactor = 0.9f
    }
}