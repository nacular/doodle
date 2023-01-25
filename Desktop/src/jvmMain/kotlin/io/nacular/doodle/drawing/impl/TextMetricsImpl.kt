package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.theme.native.textStyle
import org.jetbrains.skia.paragraph.BaselineMode.IDEOGRAPHIC
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.Paragraph
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.PlaceholderAlignment
import org.jetbrains.skia.paragraph.PlaceholderStyle
import org.jetbrains.skia.paragraph.TextStyle
import kotlin.Float.Companion.POSITIVE_INFINITY
import kotlin.math.max
import org.jetbrains.skia.Font as SkiaFont

/**
 * Created by Nicholas Eddy on 6/8/21.
 */
internal class TextMetricsImpl(private val defaultFont: SkiaFont, private val fontCollection: FontCollection): TextMetrics {
    private val Font?.textStyle get() = when (this) {
        is FontImpl -> textStyle
        else        -> defaultFont.textStyle()
    }

    private fun StyledText.paragraph(indent: Double = 0.0, width: Double? = null): Paragraph {
        val builder = ParagraphBuilder(ParagraphStyle(), fontCollection).also { builder ->
            this.forEach { (text, style) ->
                builder.pushStyle(style.font.textStyle)

                if (indent > 0.0) {
                    builder.addPlaceholder(PlaceholderStyle(indent.toFloat(), 0f, PlaceholderAlignment.BASELINE, IDEOGRAPHIC, 0f))
                }

                builder.addText(text)
                builder.popStyle()
            }
        }

        return builder.build().apply {
            layout(width?.toFloat() ?: POSITIVE_INFINITY)

            if (indent > 0.0) {
                layout(max(minIntrinsicWidth + 1, indent.toFloat()))
            }
        }
    }

    private fun String.paragraph(textStyle: TextStyle, indent: Double = 0.0, width: Double? = null): Paragraph {
        val style = ParagraphStyle().also {
            it.textStyle = textStyle
        }

        val builder = ParagraphBuilder(style, fontCollection).also { builder ->
            if (indent > 0.0) {
                builder.addPlaceholder(PlaceholderStyle(indent.toFloat(), 0f, PlaceholderAlignment.BASELINE, IDEOGRAPHIC, 0f))
            }
            builder.addText(this)
        }

        return builder.build().apply {
            layout(width?.toFloat() ?: POSITIVE_INFINITY)

            if (indent > 0.0) {
                layout(max(minIntrinsicWidth + 1, indent.toFloat()))
            }
        }
    }

    override fun width(text: String,                                    font: Font?) = width(text,                font.textStyle)
    override fun width(text: String,     width: Double, indent: Double, font: Font?) = width(text, width, indent, font.textStyle)
    override fun width(text: StyledText                                            ) = max(0.0, text.paragraph(                   ).longestWidth)
    override fun width(text: StyledText, width: Double, indent: Double             ) = max(0.0, text.paragraph(indent,       width).longestWidth)

    override fun height(text: String,                                    font: Font?) = height(text,                font.textStyle)
    override fun height(text: String,     width: Double, indent: Double, font: Font?) = height(text, width, indent, font.textStyle)
    override fun height(text: StyledText                                            ) = max(0.0, text.paragraph(                   ).totalHeight)
    override fun height(text: StyledText, width: Double, indent: Double             ) = max(0.0, text.paragraph(indent, width      ).totalHeight)

    internal fun width(text: String,                                    textStyle: TextStyle = defaultFont.textStyle()) = max(0.0, text.paragraph(textStyle               ).longestWidth)
    internal fun width(text: String,     width: Double, indent: Double, textStyle: TextStyle = defaultFont.textStyle()) = max(0.0, text.paragraph(textStyle, indent, width).longestWidth)

    internal fun height(text: String,                                    textStyle: TextStyle = defaultFont.textStyle()) = max(0.0, text.paragraph(textStyle               ).totalHeight)
    internal fun height(text: String,     width: Double, indent: Double, textStyle: TextStyle = defaultFont.textStyle()) = max(0.0, text.paragraph(textStyle, indent, width).totalHeight)

    internal fun size(text: String,                                      textStyle: TextStyle = defaultFont.textStyle()): Size = Size(width(text, textStyle), height(text, textStyle))
    internal fun size(text: String, width: Double, indent: Double = 0.0, textStyle: TextStyle = defaultFont.textStyle()): Size = Size(width(text, width, indent, textStyle), height(text, width, indent, textStyle))

    // NOTE: This gives better results than relying on Paragraph.height
    private val Paragraph.totalHeight: Double get() = lineMetrics.sumOf { it.ascent + it.descent }

    private val Paragraph.longestWidth: Double get() = longestLine.toDouble()
}