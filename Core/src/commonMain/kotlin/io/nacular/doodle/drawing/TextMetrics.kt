package io.nacular.doodle.drawing

import io.nacular.doodle.geometry.Size
import io.nacular.doodle.text.AnnotatedString
import io.nacular.doodle.text.TextSpacing
import io.nacular.doodle.text.TextSpacing.Companion.default

/**
 * Provides a mechanism to measure the size of various types of text.
 *
 * Created by Nicholas Eddy on 10/30/17.
 *
 * @author Nicholas Eddy
 */
public interface TextMetrics {
    public fun width (text: String,                                      font: Font? = null, textSpacing: TextSpacing = default): Double
    public fun width (text: String, width: Double, indent: Double = 0.0, font: Font? = null, textSpacing: TextSpacing = default): Double

    public fun width (text: AnnotatedString,                                      textSpacing: TextSpacing = default): Double
    public fun width (text: AnnotatedString, width: Double, indent: Double = 0.0, textSpacing: TextSpacing = default): Double

    public fun height(text: String,                                      font: Font? = null                                                        ): Double
    public fun height(text: String, width: Double, indent: Double = 0.0, font: Font? = null,                     textSpacing: TextSpacing = default): Double
    public fun height(text: String, width: Double, indent: Double = 0.0, font: Font? = null, lineSpacing: Float, textSpacing: TextSpacing = default): Double

    public fun height(text: AnnotatedString,                                                          textSpacing: TextSpacing = default): Double
    public fun height(text: AnnotatedString, width: Double, indent: Double = 0.0,                     textSpacing: TextSpacing = default): Double
    public fun height(text: AnnotatedString, width: Double, indent: Double = 0.0, lineSpacing: Float, textSpacing: TextSpacing = default): Double

    public fun size(text: String,                                      font: Font? = null,                     textSpacing: TextSpacing = default): Size = Size(width(text, font, textSpacing), height(text, font))
    public fun size(text: String, width: Double, indent: Double = 0.0, font: Font? = null,                     textSpacing: TextSpacing = default): Size = Size(width(text, width, indent, font, textSpacing), height(text, width, indent, font,                            textSpacing = textSpacing))
    public fun size(text: String, width: Double, indent: Double = 0.0, font: Font? = null, lineSpacing: Float, textSpacing: TextSpacing = default): Size = Size(width(text, width, indent, font, textSpacing), height(text, width, indent, font, lineSpacing = lineSpacing, textSpacing = textSpacing))

    public fun size(text: AnnotatedString,                                                          textSpacing: TextSpacing = default): Size = Size(width(text, textSpacing), height(text, textSpacing))
    public fun size(text: AnnotatedString, width: Double, indent: Double = 0.0,                     textSpacing: TextSpacing = default): Size = Size(width(text, width, indent, textSpacing), height(text, width, indent,                            textSpacing = textSpacing))
    public fun size(text: AnnotatedString, width: Double, indent: Double = 0.0, lineSpacing: Float, textSpacing: TextSpacing = default): Size = Size(width(text, width, indent, textSpacing), height(text, width, indent, lineSpacing = lineSpacing, textSpacing = textSpacing))
}