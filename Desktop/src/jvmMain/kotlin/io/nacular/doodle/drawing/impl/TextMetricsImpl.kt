package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.text.StyledText
import org.jetbrains.skija.FontMgr
import org.jetbrains.skija.paragraph.BaselineMode
import org.jetbrains.skija.paragraph.FontCollection
import org.jetbrains.skija.paragraph.ParagraphBuilder
import org.jetbrains.skija.paragraph.ParagraphStyle
import org.jetbrains.skija.paragraph.PlaceholderAlignment
import org.jetbrains.skija.paragraph.PlaceholderStyle
import org.jetbrains.skija.paragraph.TextStyle
import org.jetbrains.skija.Font as SkijaFont

/**
 * Created by Nicholas Eddy on 6/8/21.
 */
internal class TextMetricsImpl(private val defaultFont: SkijaFont): TextMetrics {
    private val Font?.skia get() = when (this) {
        is FontImpl -> skiaFont
        else        -> defaultFont
    }

    override fun width(text: String, font: Font?) = font.skia.measureTextWidth(text).toDouble()

    // FIXME
    override fun width(text: String, width: Double, indent: Double, font: Font?) = width

    override fun width(text: StyledText): Double {
        TODO("Not yet implemented")
    }

    override fun width(text: StyledText, width: Double, indent: Double): Double {
        TODO("Not yet implemented")
    }

    override fun height(text: String, font: Font?) = font.skia.measureText(text).height.toDouble()

    override fun height(text: String, width: Double, indent: Double, font: Font?): Double {
        val style = ParagraphStyle().apply {
            textStyle = TextStyle().apply {
                typeface = font.skia.typeface
                fontSize = font.skia.size
            }
        }

        val fontCollection = FontCollection().apply {
            setDefaultFontManager(FontMgr.getDefault())
        }

        val builder = ParagraphBuilder(style, fontCollection).
        addPlaceholder(PlaceholderStyle((indent).toFloat(), 0f, PlaceholderAlignment.BASELINE, BaselineMode.ALPHABETIC, 0f)).
        addText(text)

        val paragraph = builder.build()

        paragraph.layout(width.toFloat())

        return paragraph.height.toDouble()
    }

    override fun height(text: StyledText): Double {
        TODO("Not yet implemented")
    }

    override fun height(text: StyledText, width: Double, indent: Double): Double {
        TODO("Not yet implemented")
    }
}