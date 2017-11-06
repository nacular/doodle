package com.nectar.doodle.drawing

import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.insert
import com.nectar.doodle.dom.setWidth
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.text.StyledText
import org.w3c.dom.HTMLElement


private data class WrappedInfo(val font: Font, val text: String, val width: Double, val indent: Double)
private data class WrappedStyleInfo(val text: StyledText, val width: Double, val indent: Double)

class TextMetricsImpl(private val htmlFactory: HtmlFactory, private val textFactory: TextFactory, private val elementRuler: ElementRuler): TextMetrics {
    private val sizes              = mutableMapOf<Pair<Font, String>, Size>()
    private val styledSizes        = mutableMapOf<StyledText, Size>()
    private val wrappedSizes       = mutableMapOf<WrappedInfo, Size>()
    private val wrappedStyledSizes = mutableMapOf<WrappedStyleInfo, Size>()

    override fun width (font: Font, text: String) = size(font, text).width
    override fun height(font: Font, text: String) = size(font, text).height

    override fun size(font: Font, text: String) = sizes.getOrPut(Pair(font, text)) {
        measure(textFactory.create(text, font))
    }

    override fun size(font: Font, text: String, width: Double, indent: Double) = wrappedSizes.getOrPut(WrappedInfo(font, text, width, indent)) {
        measure(textFactory.wrapped(text, font, indent).also { it.style.setWidth(width) })
    }

    override fun width (text: StyledText) = size(text).width
    override fun height(text: StyledText) = size(text).height

    override fun size(text: StyledText) = styledSizes.getOrPut(text) {
        measure(textFactory.create(text))
    }

    override fun size(text: StyledText, width: Double, indent: Double) = wrappedStyledSizes.getOrPut(WrappedStyleInfo(text, width, indent)) {
        measure(textFactory.wrapped(text, indent).also { it.style.setWidth(width) })
    }

    private fun measure(element: HTMLElement): Size {
        htmlFactory.body.insert(element, 0)

        val size = Size(elementRuler.width(element), elementRuler.height(element))

        htmlFactory.body.removeChild(element)

        return size
    }
}