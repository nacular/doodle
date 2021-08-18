package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.theme.native.textStyle
import org.jetbrains.skija.paragraph.BaselineMode.IDEOGRAPHIC
import org.jetbrains.skija.paragraph.FontCollection
import org.jetbrains.skija.paragraph.Paragraph
import org.jetbrains.skija.paragraph.ParagraphBuilder
import org.jetbrains.skija.paragraph.ParagraphStyle
import org.jetbrains.skija.paragraph.PlaceholderAlignment
import org.jetbrains.skija.paragraph.PlaceholderStyle
import org.jetbrains.skija.paragraph.RectHeightMode
import org.jetbrains.skija.paragraph.RectWidthMode
import kotlin.Float.Companion.POSITIVE_INFINITY
import kotlin.math.max
import kotlin.math.round
import org.jetbrains.skija.Font as SkiaFont

/**
 * Created by Nicholas Eddy on 6/8/21.
 */
internal class TextMetricsImpl(private val defaultFont: SkiaFont, private val fontCollection: FontCollection): TextMetrics {
    private val Font?.skia get() = when (this) {
        is FontImpl -> skiaFont
        else        -> defaultFont
    }

    private fun StyledText.paragraph(indent: Double = 0.0, width: Double? = null): Paragraph {
        val builder = ParagraphBuilder(ParagraphStyle(), fontCollection).also { builder ->
            this.forEach { (text, style) ->
                builder.pushStyle(style.font.skia.textStyle())

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

    private fun String.paragraph(font: SkiaFont, indent: Double = 0.0, width: Double? = null): Paragraph {
        val style = ParagraphStyle().apply {
            textStyle = font./*skia.*/textStyle()
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

    override fun width(text: String,                                    font: Font?) = width(text,                font.skia)
    override fun width(text: String,     width: Double, indent: Double, font: Font?) = width(text, width, indent, font.skia)
    override fun width(text: StyledText                                            ) = max(0.0, text.paragraph(                   ).longestWidth)
    override fun width(text: StyledText, width: Double, indent: Double             ) = max(0.0, text.paragraph(indent,       width).longestWidth)

    override fun height(text: String,                                    font: Font?) = height(text,                font.skia)
    override fun height(text: String,     width: Double, indent: Double, font: Font?) = height(text, width, indent, font.skia)
    override fun height(text: StyledText                                            ) = max(0.0, text.paragraph(                   ).totalHeight)
    override fun height(text: StyledText, width: Double, indent: Double             ) = max(0.0, text.paragraph(indent, width      ).totalHeight)

    internal fun width(text: String,                                    font: SkiaFont = defaultFont) = max(0.0, text.paragraph(font               ).longestWidth)
    internal fun width(text: String,     width: Double, indent: Double, font: SkiaFont = defaultFont) = max(0.0, text.paragraph(font, indent, width).longestWidth)

    internal fun height(text: String,                                    font: SkiaFont = defaultFont) = max(0.0, text.paragraph(font               ).totalHeight)
    internal fun height(text: String,     width: Double, indent: Double, font: SkiaFont = defaultFont) = max(0.0, text.paragraph(font, indent, width).totalHeight)

    internal fun size(text: String,                                      font: SkiaFont = defaultFont): Size = Size(width(text, font), height(text, font))
    internal fun size(text: String, width: Double, indent: Double = 0.0, font: SkiaFont = defaultFont): Size = Size(width(text, width, indent, font), height(text, width, indent, font))

    // NOTE: This gives better results than relying on Paragraph.height
    private val Paragraph.totalHeight: Double get() = round(lineMetrics.sumByDouble { it.ascent + it.descent })

    private val Paragraph.longestWidth: Double get() = lineMetrics.maxOfOrNull {
        getRectsForRange(it.left.toInt(), it.endIncludingNewline.toInt(), RectHeightMode.TIGHT, RectWidthMode.MAX).fold(0f) { left, right ->
            left + right.rect.width
        }.toDouble()
    } ?: 0.0
}