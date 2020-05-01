package com.nectar.doodle.drawing

import com.nectar.doodle.drawing.Font.Style.Italic


/**
 * Represents a font used to render text.
 *
 * @author Nicholas Eddy
 * @see Canvas.text
 */
interface Font {
    val size  : Int
    val style : Set<Style>
    val weight: Int
    val family: String

    val italic get() = Italic in style

    enum class Style {
        Italic
    }

    companion object {
        const val Thinnest = 100
        const val Thinner  = 200
        const val Thin     = 300
        const val Normal   = 400
        const val Thick    = 500
        const val Thicker  = 600
        const val Bold     = 700
        const val Bolder   = 800
        const val Boldest  = 900
    }
}