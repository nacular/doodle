package io.nacular.doodle.drawing

import io.nacular.doodle.geometry.Size
import io.nacular.doodle.text.StyledText

/**
 * Provides a mechanism to measure the size of various types of text.
 *
 * Created by Nicholas Eddy on 10/30/17.
 *
 * @author Nicholas Eddy
 */
public interface TextMetrics {
    public fun width (text: String, font: Font? = null, letterSpacing: Double = 0.0                                                              ): Double
    public fun width (text: String, width: Double, indent: Double = 0.0, font: Font? = null, letterSpacing: Double = 0.0                         ): Double

    public fun width (text: StyledText, letterSpacing: Double = 0.0                                                                              ): Double
    public fun width (text: StyledText, width: Double, indent: Double = 0.0, letterSpacing: Double = 0.0                                         ): Double

    public fun height(text: String, font: Font? = null                                                                                           ): Double
    public fun height(text: String, width: Double, indent: Double = 0.0, font: Font? = null, lineSpacing: Float = 1f, letterSpacing: Double = 0.0): Double

    public fun height(text: StyledText, letterSpacing: Double = 0.0                                                                              ): Double
    public fun height(text: StyledText, width: Double, indent: Double = 0.0, lineSpacing: Float = 1f, letterSpacing: Double = 0.0                ): Double

    public fun size(text: String, font: Font? = null, letterSpacing: Double = 0.0                                                                ): Size = Size(width(text, font, letterSpacing), height(text, font))
    public fun size(text: String, width: Double, indent: Double = 0.0, font: Font? = null, lineSpacing: Float = 1f, letterSpacing: Double = 0.0  ): Size = Size(width(text, width, indent, font, letterSpacing), height(text, width, indent, font))

    public fun size(text: StyledText, letterSpacing: Double = 0.0                                                                                ): Size = Size(width(text, letterSpacing), height(text, letterSpacing))
    public fun size(text: StyledText, width: Double, indent: Double = 0.0, lineSpacing: Float = 1f, letterSpacing: Double = 0.0                  ): Size = Size(width(text, width, indent, letterSpacing), height(text, width, indent, letterSpacing = letterSpacing))
}