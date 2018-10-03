package com.nectar.doodle.drawing

import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.setWidth
import com.nectar.doodle.text.StyledText
import org.w3c.dom.HTMLElement
import kotlin.math.max


private data class WrappedInfo     (val text: String,     val width: Double, val indent: Double, val font: Font?)
private data class WrappedStyleInfo(val text: StyledText, val width: Double, val indent: Double                 )

class TextMetricsImpl(private val textFactory: TextFactory, private val elementFactory: HtmlFactory, private val elementRuler: ElementRuler): TextMetrics {

    // FIXME: These should be caches with limited storage
    private val widths              = mutableMapOf<Pair<String, Font?>, Double>()
    private val styledWidths        = mutableMapOf<StyledText, Double>()
    private val wrappedWidths       = mutableMapOf<WrappedInfo, Double>()
    private val wrappedStyledWidths = mutableMapOf<WrappedStyleInfo, Double>()

    private val fontHeights = mutableMapOf<Font?, Double>()

    override fun width(text: String, font: Font?) = widths.getOrPut(text to font) {
        elementRuler.size(textFactory.create(text, font)).also {
            fontHeights[font] = it.height
        }.width
    }

    override fun width(text: StyledText) = styledWidths.getOrPut(text) {
        elementRuler.width(textFactory.create(text)).also {
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