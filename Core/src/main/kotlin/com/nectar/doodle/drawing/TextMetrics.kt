package com.nectar.doodle.drawing

import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.insert
import com.nectar.doodle.dom.setWidth
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.text.StyledText
import org.w3c.dom.HTMLElement

/**
 * Created by Nicholas Eddy on 10/30/17.
 */

interface TextMetrics {
    fun width (font: Font, text: String): Double
    fun height(font: Font, text: String): Double

    fun size(font: Font, text: String) = Size(width(font, text), height(font, text))

    fun wrappedSize(font: Font, text: String, width: Double, indent: Double = 0.0): Size

    fun width (text: StyledText): Double
    fun height(text: StyledText): Double

    fun size(text: StyledText) = Size(width(text), height(text))

//    fun wrappedSize(font: Font, text: String, width: Double, indent: Double = 0.0): Size
}

private data class WrapedInfo(val font: Font, val text: String, val width: Double)

class TextMetricsImpl(private val htmlFactory: HtmlFactory, private val textFactory: TextFactory, private val elementRuler: ElementRuler): TextMetrics {

    private val sizes        = mutableMapOf<Pair<Font, String>, Size>()
    private val styledSizes  = mutableMapOf<StyledText, Size>()
    private val wrappedSizes = mutableMapOf<WrapedInfo, Size>()

    override fun width (font: Font, text: String) = size(font, text).width
    override fun height(font: Font, text: String) = size(font, text).height

    override fun size(font: Font, text: String) = sizes.getOrPut(Pair(font, text)) {
        measure(textFactory.create(text, font))
    }

    override fun wrappedSize(font: Font, text: String, width: Double, indent: Double) = wrappedSizes.getOrPut(WrapedInfo(font, text, width)) {
        measure(textFactory.wrapped(text, font, indent).also { it.style.setWidth(width) })
    }

    override fun width (text: StyledText) = size(text).width
    override fun height(text: StyledText) = size(text).height

    override fun size(text: StyledText) = styledSizes.getOrPut(text) {
        measure(textFactory.styled(text))
    }

    private fun measure(element: HTMLElement): Size {
        htmlFactory.body.insert(element, 0)

        val size = Size(elementRuler.width (element), elementRuler.height(element))

        htmlFactory.body.removeChild(element)

        return size
    }
}