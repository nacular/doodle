package com.nectar.doodle.drawing.impl

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.defaultFontFamily
import com.nectar.doodle.dom.defaultFontSize
import com.nectar.doodle.dom.defaultFontWeight
import com.nectar.doodle.dom.setFontFamily
import com.nectar.doodle.dom.setFontSize
import com.nectar.doodle.dom.setFontWeight
import com.nectar.doodle.dom.setWidth
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.text.StyledText
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.math.max


private data class WrappedInfo     (val text: String,     val width: Double, val indent: Double, val font: Font?)
private data class WrappedStyleInfo(val text: StyledText, val width: Double, val indent: Double                 )

class TextMetricsImpl(private val textFactory: TextFactory, private val elementFactory: HtmlFactory, private val elementRuler: ElementRuler, htmlFactory: HtmlFactory): TextMetrics {
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
        val box = elementFactory.create<HTMLElement>()

        box.appendChild(textFactory.wrapped(text, font, width, indent))
        box.style.setWidth(width)

        elementRuler.width(box)
    }

    override fun width(text: StyledText, width: Double, indent: Double) = wrappedStyledWidths.getOrPut(WrappedStyleInfo(text, width, indent)) {
        val box = elementFactory.create<HTMLElement>()

        box.appendChild(textFactory.wrapped(text, width, indent))
        box.style.setWidth(width)

        elementRuler.width(box)
    }

    override fun height(text: String, font: Font?) = fontHeights.getOrPut(font) {
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