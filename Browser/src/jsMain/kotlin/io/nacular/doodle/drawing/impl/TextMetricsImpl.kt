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

    private fun textWidth(text: String, font: Font?, letterSpacing: Double): Double {
        renderingContext.font          = fontSerializer(font)
        renderingContext.letterSpacing = if (letterSpacing > 0.0) "${letterSpacing}px" else ""

        return renderingContext.measureText(text).width
    }

    private fun textWidth(text: StyledText, letterSpacing: Double): Double {
        var width = 0.0

        text.forEach { (string, style) ->
            renderingContext.font          = fontSerializer(style.font)
            renderingContext.letterSpacing = if (letterSpacing > 0.0) "${letterSpacing}px" else ""

            renderingContext.measureText(string).let {
                width += it.width
            }
        }

        return width
    }

    override fun width(text: String, font: Font?, letterSpacing: Double) = widths.getOrPut(WidthInfo(text, font, letterSpacing)) {
        textWidth(text, font, letterSpacing)/*.also {
            if (text.isNotEmpty()) {
                fontHeights[font] = it.height
            }
        }.width*/
    }

    override fun width(text: StyledText, letterSpacing: Double) = styledWidths.getOrPut(text) {
        textWidth(text, letterSpacing)/*.also {
            fontHeights[font] = it.height
        }*/
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
            widths[WidthInfo(text, font)] = it.width
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
}

//class TextMetricsImpl2(private val textFactory: TextFactory, svgFactory: SvgFactory, private val elementRuler: ElementRuler, private val htmlFactory: HtmlFactory): TextMetrics {
//    private val textElement = svgFactory<SVGTextElement>("text")
//    private val svg         = svgFactory<SVGElement>("svg" ).apply { appendChild(textElement) }
//
//    // FIXME: These should be caches with limited storage
//    private val widths              = mutableMapOf<Pair<String, Font?>, Double>()
//    private val fontHeights         = mutableMapOf<Font?, Double>()
//    private val styledWidths        = mutableMapOf<StyledText, Double>()
//    private val wrappedWidths       = mutableMapOf<WrappedInfo, Double>()
//    private val wrappedStyledWidths = mutableMapOf<WrappedStyleInfo, Double>()
//
//    private fun measure(text: String, font: Font?): Size {
////        text.setAttribute('text-anchor', getAlignment(this.textAlign));
////        text.setAttribute('alignment-baseline', this.textBaseline);
//        font?.let { textElement.style.setFont(it) }
//        textElement.textContent = text
//        htmlFactory.root.appendChild(svg)
//        val box = textElement.getBBox()
////        htmlFactory.root.removeChild(svg)
//
//        return Size(box.width, box.height)
//    }
//
//    private fun textWidth(text: String, font: Font?) = measure(text, font).width
//
//    private fun textWidth(text: StyledText): Double {
//        var width = 0.0
//
//        text.forEach { (string, style) ->
//            measure(string, style.font).let {
//                width += it.width
//            }
//        }
//
//        return width
//    }
//
//    override fun width(text: String, font: Font?) = widths.getOrPut(text to font) {
//        textWidth(text, font)/*.also {
//            if (text.isNotEmpty()) {
//                fontHeights[font] = it.height
//            }
//        }.width*/
//    }
//
//    override fun width(text: StyledText) = styledWidths.getOrPut(text) {
//        textWidth(text).also {
//            //            fontHeights[font] = it.height
//        }
//    }
//
//    override fun width(text: String, width: Double, indent: Double, font: Font?) = wrappedWidths.getOrPut(WrappedInfo(text, width, indent, font)) {
//        val box = htmlFactory.create<HTMLElement>()
//
//        box.appendChild(textFactory.wrapped(text, font, width, indent))
//        box.style.setWidth(width)
//
//        elementRuler.width(box)
//    }
//
//    override fun width(text: StyledText, width: Double, indent: Double) = wrappedStyledWidths.getOrPut(WrappedStyleInfo(text, width, indent)) {
//        val box = htmlFactory.create<HTMLElement>()
//
//        box.appendChild(textFactory.wrapped(text, width, indent))
//        box.style.setWidth(width)
//
//        elementRuler.width(box)
//    }
//
//    override fun height(text: String, font: Font?) = fontHeights.getOrPut(font) {
//        elementRuler.size(textFactory.create(text, font)).also {
//            widths[text to font] = it.width
//        }.height
//    }
//
//    override fun height(text: StyledText): Double  {
//        var maxHeight = 0.0
//
//        text.forEach { (string, style) ->
//            maxHeight = max(maxHeight, height(string, style.font))
//        }
//
//        return maxHeight
//    }
//
//    override fun height(text: String, width: Double, indent: Double, font: Font?) = elementRuler.height(textFactory.wrapped(text, font, width, indent))
//
//    override fun height(text: StyledText, width: Double, indent: Double) = elementRuler.height(textFactory.wrapped(text, width, indent))
//}