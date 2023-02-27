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
import io.nacular.doodle.utils.HorizontalAlignment.Left
import io.nacular.doodle.utils.LeastRecentlyUsedCache
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.math.max


private class WrappedInfo(val text: String, val width: Double, val indent: Double, val font: Font?) {
    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + width.hashCode()
        result = 31 * result + indent.hashCode()
        result = 31 * result + (font?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WrappedInfo) return false

        if (text != other.text) return false
        if (width != other.width) return false
        if (indent != other.indent) return false
        if (font != other.font) return false

        return true
    }
}

private class WrappedStyleInfo(val text: StyledText, val width: Double, val indent: Double) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WrappedStyleInfo) return false

        if (text != other.text) return false
        if (width != other.width) return false
        if (indent != other.indent) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + width.hashCode()
        result = 31 * result + indent.hashCode()
        return result
    }
}

internal class TextMetricsImpl(
        private val textFactory   : TextFactory,
        private val htmlFactory   : HtmlFactory,
        private val elementRuler  : ElementRuler,
        private val fontSerializer: FontSerializer,
                    cacheLength   : Int
): TextMetrics {
    private data class WidthInfo(val text: String, val font: Font?, val letterSpacing: Double = 0.0)

    private val widths              = LeastRecentlyUsedCache<WidthInfo, Double>        (maxSize = cacheLength)
    private val styledWidths        = LeastRecentlyUsedCache<StyledText, Double>      (maxSize = cacheLength)
    private val wrappedWidths       = LeastRecentlyUsedCache<WrappedInfo, Double>     (maxSize = cacheLength)
    private val wrappedStyledWidths = LeastRecentlyUsedCache<WrappedStyleInfo, Double>(maxSize = cacheLength)

    private val fontHeights = mutableMapOf<Font?, Double>()

    private val renderingContext = htmlFactory.create<HTMLCanvasElement>("canvas").getContext("2d") as CanvasRenderingContext2D

    private val noLetterSpacingSupport = (renderingContext.letterSpacing == undefined)

    override fun width(text: String, font: Font?, letterSpacing: Double) = widths.getOrPut(TextInfo(text, font, letterSpacing)) {
        textWidth(text, font, letterSpacing)
    }

    override fun width(text: StyledText, letterSpacing: Double) = styledWidths.getOrPut(StyledTextInfo(text, letterSpacing)) {
        textWidth(text, letterSpacing)
    }

    override fun width(text: String, width: Double, indent: Double, font: Font?, letterSpacing: Double) = wrappedWidths.getOrPut(WrappedInfo(text, width, indent, font)) {
        val box = htmlFactory.create<HTMLElement>()

        box.appendChild(textFactory.wrapped(text, font, width, indent, alignment = Left, lineSpacing = 1f, letterSpacing = letterSpacing))
        box.style.setWidth(width)

        elementRuler.width(box)
    }

    override fun width(text: StyledText, width: Double, indent: Double, letterSpacing: Double) = wrappedStyledWidths.getOrPut(WrappedStyleInfo(text, width, indent)) {
        val box = htmlFactory.create<HTMLElement>()

        box.appendChild(textFactory.wrapped(text, width, indent, alignment = Left, lineSpacing = 1f, letterSpacing = letterSpacing))
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
        text = text,
        font = font,
        width = width,
        indent = indent,
        alignment = Left,
        lineSpacing = lineSpacing,
        letterSpacing = letterSpacing,
    ))

    override fun height(text: StyledText, width: Double, indent: Double, lineSpacing: Float, letterSpacing: Double) = elementRuler.height(textFactory.wrapped(
        text = text,
        width = width,
        indent = indent,
        alignment = Left,
        lineSpacing = lineSpacing,
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