package io.nacular.doodle.drawing.impl

import io.nacular.doodle.FontSerializer
import io.nacular.doodle.HTMLElement
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.setWidth
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.TextFactory
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.letterSpacing
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.utils.LeastRecentlyUsedCache
import io.nacular.doodle.utils.TextAlignment.Start
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.math.max

internal class TextMetricsImpl(
        private val textFactory   : TextFactory,
        private val htmlFactory   : HtmlFactory,
        private val elementRuler  : ElementRuler,
        private val fontSerializer: FontSerializer,
                    cacheLength   : Int
): TextMetrics {
    private data class TextInfo        (val text: String,     val font: Font?,  val letterSpacing: Double = 0.0)
    private data class StyledTextInfo  (val text: StyledText,                   val letterSpacing: Double = 0.0)
    private data class WrappedInfo     (val text: String,     val width: Double, val indent: Double, val font: Font?, val letterSpacing: Double)
    private data class WrappedStyleInfo(val text: StyledText, val width: Double, val indent: Double, val letterSpacing: Double)

    private val widths              = LeastRecentlyUsedCache<TextInfo,         Double> (maxSize = cacheLength)
    private val styledWidths        = LeastRecentlyUsedCache<StyledTextInfo,   Double> (maxSize = cacheLength)
    private val wrappedWidths       = LeastRecentlyUsedCache<WrappedInfo,      Double> (maxSize = cacheLength)
    private val wrappedStyledWidths = LeastRecentlyUsedCache<WrappedStyleInfo, Double> (maxSize = cacheLength)

    private val fontHeights = mutableMapOf<Font?, Double>()

    private val renderingContext = htmlFactory.create<HTMLCanvasElement>("canvas").getContext("2d") as CanvasRenderingContext2D

    private val noLetterSpacingSupport = (renderingContext.letterSpacing == undefined)

    override fun width(text: String, font: Font?, letterSpacing: Double) = widths.getOrPut(TextInfo(text, font, letterSpacing)) {
        textWidth(text, font, letterSpacing)
    }

    override fun width(text: StyledText, letterSpacing: Double) = styledWidths.getOrPut(StyledTextInfo(text, letterSpacing)) {
        textWidth(text, letterSpacing)
    }

    override fun width(text: String, width: Double, indent: Double, font: Font?, letterSpacing: Double) = wrappedWidths.getOrPut(WrappedInfo(text, width, indent, font, letterSpacing)) {
        val box = htmlFactory.create<HTMLElement>()

        box.appendChild(textFactory.wrapped(text, font, width, indent, alignment = Start, lineSpacing = 1f, letterSpacing = letterSpacing))
        box.style.setWidth(width)

        elementRuler.width(box)
    }

    override fun width(text: StyledText, width: Double, indent: Double, letterSpacing: Double) = wrappedStyledWidths.getOrPut(WrappedStyleInfo(text, width, indent, letterSpacing)) {
        val box = htmlFactory.create<HTMLElement>()

        box.appendChild(textFactory.wrapped(text, width, indent, alignment = Start, lineSpacing = 1f, letterSpacing = letterSpacing))
        box.style.setWidth(width)

        elementRuler.width(box)
    }

    // Special check for blank added to avoid font black-holing if first text checked is empty string
    override fun height(text: String, font: Font?) = if (text.isBlank()) 0.0 else fontHeights.getOrPut(font) {
        elementRuler.size(textFactory.create(text, font, letterSpacing = 0.0)).also {
            widths[TextInfo(text, font)] = it.width
        }.height
    }

    override fun height(text: StyledText, letterSpacing: Double): Double  {
        var maxHeight = 0.0

        text.forEach { (string, style) ->
            maxHeight = max(maxHeight, height(string, style.font))
        }

        return maxHeight
    }

    override fun height(text: String, width: Double, indent: Double, font: Font?, lineSpacing: Float, letterSpacing: Double) = elementRuler.height(textFactory.wrapped(
        text          = text,
        font          = font,
        width         = width,
        indent        = indent,
        alignment     = Start,
        lineSpacing   = lineSpacing,
        letterSpacing = letterSpacing,
    ))

    override fun height(text: StyledText, width: Double, indent: Double, lineSpacing: Float, letterSpacing: Double) = elementRuler.height(textFactory.wrapped(
        text          = text,
        width         = width,
        indent        = indent,
        alignment     = Start,
        lineSpacing   = lineSpacing,
        letterSpacing = letterSpacing,
    ))

    private fun textWidth(text: String, font: Font?, letterSpacing: Double): Double {
        return when {
            letterSpacing != 0.0 && noLetterSpacingSupport -> elementRuler.size(textFactory.create(text, font, letterSpacing)).width
            else                                           -> {
                renderingContext.font = fontSerializer(font)
                renderingContext.letterSpacing = if (letterSpacing > 0.0) "${letterSpacing}px" else ""

                renderingContext.measureText(text).width
            }
        }
    }

    private fun textWidth(text: StyledText, letterSpacing: Double): Double {
        var width = 0.0

        text.forEach { (string, style) ->
            width += textWidth(string, style.font, letterSpacing)
        }

        return width
    }
}