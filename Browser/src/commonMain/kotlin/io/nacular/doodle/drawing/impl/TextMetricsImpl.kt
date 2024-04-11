package io.nacular.doodle.drawing.impl

import io.nacular.doodle.FontSerializer
import io.nacular.doodle.dom.CanvasRenderingContext2D
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.HTMLCanvasElement
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.letterSpacing
import io.nacular.doodle.dom.setWidth
import io.nacular.doodle.dom.wordSpacing
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.TextFactory
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.text.TextSpacing
import io.nacular.doodle.text.TextSpacing.Companion.default
import io.nacular.doodle.utils.LeastRecentlyUsedCache
import io.nacular.doodle.utils.TextAlignment.Start
import kotlin.math.max

internal class TextMetricsImpl(
    private val textFactory   : TextFactory,
    private val htmlFactory   : HtmlFactory,
    private val elementRuler  : ElementRuler,
    private val fontSerializer: FontSerializer,
                cacheLength   : Int
): TextMetrics {
    private data class TextInfo        (val text: String,     val font: Font?,   val textSpacing: TextSpacing = default)
    private data class StyledTextInfo  (val text: StyledText,                    val textSpacing: TextSpacing = default)
    private data class WrappedInfo     (val text: String,     val width: Double, val indent: Double, val font: Font?, val textSpacing: TextSpacing)
    private data class WrappedStyleInfo(val text: StyledText, val width: Double, val indent: Double, val textSpacing: TextSpacing)

    private val widths              = LeastRecentlyUsedCache<TextInfo,         Double> (maxSize = cacheLength)
    private val styledWidths        = LeastRecentlyUsedCache<StyledTextInfo,   Double> (maxSize = cacheLength)
    private val wrappedWidths       = LeastRecentlyUsedCache<WrappedInfo,      Double> (maxSize = cacheLength)
    private val wrappedStyledWidths = LeastRecentlyUsedCache<WrappedStyleInfo, Double> (maxSize = cacheLength)

    private val fontHeights = mutableMapOf<Font?, Double>()

    private val renderingContext = htmlFactory.create<HTMLCanvasElement>("canvas").getContext("2d") as CanvasRenderingContext2D

    private val noWordSpacingSupport   = (renderingContext.wordSpacing   == null)
    private val noLetterSpacingSupport = (renderingContext.letterSpacing == null)

    override fun width(text: String, font: Font?, textSpacing: TextSpacing) = widths.getOrPut(TextInfo(text, font, textSpacing)) {
        textWidth(text, font, textSpacing)
    }

    override fun width(text: StyledText, textSpacing: TextSpacing) = styledWidths.getOrPut(StyledTextInfo(text, textSpacing)) {
        textWidth(text, textSpacing)
    }

    override fun width(text: String, width: Double, indent: Double, font: Font?, textSpacing: TextSpacing) = wrappedWidths.getOrPut(
        WrappedInfo(text, width, indent, font, textSpacing)
    ) {
        val box = htmlFactory.create<HTMLElement>()

        box.appendChild(textFactory.wrapped(text, font, width, indent, alignment = Start, lineSpacing = 1f, textSpacing = textSpacing))
        box.style.setWidth(width)

        elementRuler.width(box)
    }

    override fun width(text: StyledText, width: Double, indent: Double, textSpacing: TextSpacing) = wrappedStyledWidths.getOrPut(
        WrappedStyleInfo(text, width, indent, textSpacing)
    ) {
        val box = htmlFactory.create<HTMLElement>()

        box.appendChild(textFactory.wrapped(text, width, indent, alignment = Start, lineSpacing = 1f, textSpacing = textSpacing))
        box.style.setWidth(width)

        elementRuler.width(box)
    }

    // Special check for blank added to avoid font black-holing if first text checked is empty string
    override fun height(text: String, font: Font?) = if (text.isBlank()) 0.0 else fontHeights.getOrPut(font) {
        elementRuler.height(textFactory.create(text, font, textSpacing = default))
    }

    override fun height(text: StyledText, textSpacing: TextSpacing): Double  {
        var maxHeight = 0.0

        text.forEach { (string, style) ->
            maxHeight = max(maxHeight, height(string, style.font))
        }

        return maxHeight
    }

    override fun height(text: String, width: Double, indent: Double, font: Font?, lineSpacing: Float, textSpacing: TextSpacing) = elementRuler.height(textFactory.wrapped(
        text        = text,
        font        = font,
        width       = width,
        indent      = indent,
        alignment   = Start,
        lineSpacing = lineSpacing,
        textSpacing = textSpacing,
    ))

    override fun height(text: StyledText, width: Double, indent: Double, lineSpacing: Float, textSpacing: TextSpacing) = elementRuler.height(textFactory.wrapped(
        text        = text,
        width       = width,
        indent      = indent,
        alignment   = Start,
        lineSpacing = lineSpacing,
        textSpacing = textSpacing,
    ))

    private fun textWidth(text: String, font: Font?, textSpacing: TextSpacing): Double {
        val notSupported = (textSpacing.wordSpacing   != 0.0 && noWordSpacingSupport  ) ||
                           (textSpacing.letterSpacing != 0.0 && noLetterSpacingSupport)

        return when {
            notSupported -> elementRuler.size(textFactory.create(text, font, textSpacing)).width
            else         -> {
                renderingContext.font          = fontSerializer(font)
                renderingContext.wordSpacing   = "${max(0.0, textSpacing.wordSpacing  )}px"
                renderingContext.letterSpacing = "${max(0.0, textSpacing.letterSpacing)}px"

                renderingContext.measureText(text).width
            }
        }
    }

    private fun textWidth(text: StyledText, textSpacing: TextSpacing): Double {
        var width    = 0.0
        var numItems = 0

        text.forEach { (string, style) ->
            if (string.isNotBlank()) {
                ++numItems
                width += textWidth(string, style.font, textSpacing)
            }
        }

        return width + (numItems - 1) * textSpacing.wordSpacing
    }
}