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
public interface Font {
    public val size  : Int
    public val style : Style
    public val weight: Int
    public val family: String

    public sealed class Style {
        public object Normal: Style()
        public object Italic: Style()
        public class  Oblique(public val angle: Measure<Angle>? = null): Style()
    }

    public companion object {
        public const val Thinnest: Int = 100
        public const val Thinner: Int  = 200
        public const val Thin: Int     = 300
        public const val Normal: Int   = 400
        public const val Thick: Int    = 500
        public const val Thicker: Int  = 600
        public const val Bold: Int     = 700
        public const val Bolder: Int   = 800
        public const val Boldest: Int  = 900
    }
}