package io.nacular.doodle.drawing.impl

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.defaultFontFamily
import io.nacular.doodle.dom.defaultFontSize
import io.nacular.doodle.dom.defaultFontWeight
import io.nacular.doodle.dom.setFontFamily
import io.nacular.doodle.dom.setFontSize
import io.nacular.doodle.dom.setFontWeight
import io.nacular.doodle.dom.setWidth
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.TextFactory
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.text.StyledText
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

class TextMetricsImpl(private val textFactory: TextFactory, private val htmlFactory: HtmlFactory, private val elementRuler: ElementRuler): TextMetrics {
    // FIXME: Use Canvas for all text measurements (heights are tricky)
    private interface CSSFontSerializer {
        operator fun invoke(font: Font?): String
    }

    private class CSSFontSerializerImpl(htmlFactory: HtmlFactory): CSSFontSerializer {
        private val element = htmlFactory.create<org.w3c.dom.HTMLElement>()

        override fun invoke(font: Font?): String = when {
            font != null -> element.run {
                style.setFontSize  (font.size                )
                style.setFontFamily(font.family.toLowerCase())
                style.setFontWeight(font.weight              )

                style.run { "$fontStyle $fontVariant $fontWeight $fontSize $fontFamily" }
            }
            else -> "$defaultFontWeight ${defaultFontSize}px $defaultFontFamily"
        }
    }

    // FIXME: These should be caches with limited storage
    private val widths              = mutableMapOf<Pair<String, Font?>, Double>()
    private val styledWidths        = mutableMapOf<StyledText, Double>()
    private val wrappedWidths       = mutableMapOf<WrappedInfo, Double>()
    private val wrappedStyledWidths = mutableMapOf<WrappedStyleInfo, Double>()

    private val fontHeights = mutableMapOf<Font?, Double>()

    private val fontSerializer   = CSSFontSerializerImpl(htmlFactory)
    private val renderingContext = htmlFactory.create<HTMLCanvasElement>("canvas").getContext("2d") as CanvasRenderingContext2D

    private fun textWidth(text: String, font: Font?): Double {
        renderingContext.font = fontSerializer(font)

        return renderingContext.measureText(text).width
    }

    private fun textWidth(text: StyledText): Double {
        var width = 0.0

        text.forEach { (string, style) ->
            renderingContext.font = fontSerializer(style.font)

            renderingContext.measureText(string).let {
                width += it.width
            }
        }

        return width
    }

    override fun width(text: String, font: Font?) = widths.getOrPut(text to font) {
        textWidth(text, font)/*.also {
            if (text.isNotEmpty()) {
                fontHeights[font] = it.height
            }
        }.width*/
    }

    override fun width(text: StyledText) = styledWidths.getOrPut(text) {
        textWidth(text).also {
//            fontHeights[font] = it.height
        }
    }

    override fun width(text: String, width: Double, indent: Double, font: Font?) = wrappedWidths.getOrPut(WrappedInfo(text, width, indent, font)) {
        val box = htmlFactory.create<HTMLElement>()

        box.appendChild(textFactory.wrapped(text, font, width, indent))
        box.style.setWidth(width)

        elementRuler.width(box)
    }

    override fun width(text: StyledText, width: Double, indent: Double) = wrappedStyledWidths.getOrPut(WrappedStyleInfo(text, width, indent)) {
        val box = htmlFactory.create<HTMLElement>()

        box.appendChild(textFactory.wrapped(text, width, indent))
        box.style.setWidth(width)

        elementRuler.width(box)
    }

    // Special check for blank added to avoid font black-holing if first text checked is empty string
    override fun height(text: String, font: Font?) = if (text.isBlank()) 0.0 else fontHeights.getOrPut(font) {
        elementRuler.size(textFactory.create(text, font)).also {
            widths[text to font] = it.width
        }.height
    }

    override fun height(text: StyledText): Double  {
        var maxHeight = 0.0

        text.forEach { (string, style) ->
            maxHeight = max(maxHeight, height(string, style.font))
        }

        return maxHeight
    }

    override fun height(text: String, width: Double, indent: Double, font: Font?) = elementRuler.height(textFactory.wrapped(text, font, width, indent))

    override fun height(text: StyledText, width: Double, indent: Double) = elementRuler.height(textFactory.wrapped(text, width, indent))
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