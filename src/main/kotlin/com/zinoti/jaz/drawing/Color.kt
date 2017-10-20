package com.zinoti.jaz.drawing

import kotlin.math.max
import kotlin.math.min

private fun Int.toHex(): String {

    var i        = this
    var hash     = ""
    val alphabet = "0123456789abcdef"
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

class Color private constructor(
        val red    : Int   = 0,
        var green  : Int   = 0,
        var blue   : Int   = 0,
        var opacity: Float = 1f) {

    private constructor(rgb: RGB, opacity: Float = 1f): this(rgb.red, rgb.green, rgb.blue, opacity)

    private constructor(hex: String, opacity: Float = 1f): this(toRgb(hex), opacity)

    init {
        val range = 0..255

        require(red     in range) { "red must be in $range"      }
        require(green   in range) { "green must be in $range"    }
        require(blue    in range) { "blue must be in $range"     }
        require(opacity in 0..1 ) { "opacity must be in ${0..1}" }
    }

    private var decimal: Int = (red shl 16) + (green shl 8) + blue

    var hexString: String = decimal.toHex()

    fun darker(times: Int = 1): Color {
        var red   = this.red
        var green = this.green
        var blue  = this.blue

        for (i in 0 until times) {
            red   = max(0f, red   * sScaleFactor).toInt()
            green = max(0f, green * sScaleFactor).toInt()
            blue  = max(0f, blue  * sScaleFactor).toInt()
        }

        return Color.create(red, green, blue, opacity)
    }

    fun lighter(times: Int = 1): Color {
        var red   = this.red
        var green = this.green
        var blue  = this.blue

        val i = (1.0 / (1.0 - sScaleFactor)).toInt()

        if (red == 0 && green == 0 && blue == 0) {
            return Color.create(i, i, i, opacity)
        }

        for (j in 0 until times) {
            red   = min(red   / sScaleFactor, 255f).toInt()
            green = min(green / sScaleFactor, 255f).toInt()
            blue  = min(blue  / sScaleFactor, 255f).toInt()

            if (red   in 1 until i) { red   = i }
            if (green in 1 until i) { green = i }
            if (blue  in 1 until i) { blue  = i }
        }

        return Color.create(red, green, blue, opacity)
    }

    fun with(opactiy: Float): Color = Color.create(red, green, blue, opactiy)

    val inverted: Color = Color.create((Int.MAX_VALUE xor decimal).toHex())

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
        fun create(hexString: String) = Color(hexString)

        fun create(hexString: String, opacity: Float) = Color(hexString, opacity)

        fun create(red: Int, green: Int, blue: Int) = Color(red, green, blue)

        fun create(red: Int, green: Int, blue: Int, opacity: Float) = Color(red, green, blue, opacity)

        /** ff0000  */
        val RED = Color("ff0000")
        /** ffc0cb  */
        val PINK = Color("ffc0cb")
        /** 0000ff  */
        val BLUE = Color("0000ff")
        /** 00ffff  */
        val CYAN = Color("00ffff")
        /** a9a9a9  */
        val GRAY = Color("a9a9a9")
        /** 000000  */
        val BLACK = Color("000000")
        /** 00ff00  */
        val GREEN = Color("00ff00")
        /** ffffff  */
        val WHITE = Color("ffffff")
        /** ffff00  */
        val YELLOW = Color("ffff00")
        /** ffa500  */
        val ORANGE = Color("ffa500")
        /** ff00ff  */
        val MAGENTA = Color("ff00ff")
        /** 808080  */
        val DARK_GRAY = Color("808080")
        /** d3d3d3  */
        val LIGHT_GRAY = Color("d3d3d3")

        val TRANSPARENT = Color("000000", 0f)

        private val sScaleFactor = 0.9f
    }
}