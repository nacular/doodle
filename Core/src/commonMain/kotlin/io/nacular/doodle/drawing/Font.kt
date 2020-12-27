package io.nacular.doodle.drawing

import io.nacular.measured.units.Angle
import io.nacular.measured.units.Measure


/**
 * Represents a font used to render text. NOTE: this interface is only intended
 * to be implemented by the framework. This ensures that instances always
 * represent a loaded Font that can be used to render text without issue.
 *
 * @author Nicholas Eddy
 * @see Canvas.text
 */
interface Font {
    val size  : Int
    val style : Style
    val weight: Int
    val family: String

    sealed class Style {
        object Normal: Style()
        object Italic: Style()
        class  Oblique(val angle: Measure<Angle>? = null): Style()
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