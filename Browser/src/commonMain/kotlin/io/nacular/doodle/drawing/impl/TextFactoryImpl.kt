package io.nacular.doodle.drawing.impl

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.clear
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Inline
import io.nacular.doodle.dom.Static
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.childAt
import io.nacular.doodle.dom.numChildren
import io.nacular.doodle.dom.setBackgroundColor
import io.nacular.doodle.dom.setColor
import io.nacular.doodle.dom.setDisplay
import io.nacular.doodle.dom.setFont
import io.nacular.doodle.dom.setPosition
import io.nacular.doodle.dom.setTextDecoration
import io.nacular.doodle.dom.setTextIndent
import io.nacular.doodle.dom.setWidth
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.TextFactory
import io.nacular.doodle.text.Style
import io.nacular.doodle.text.StyledText

internal class TextFactoryImpl(private val htmlFactory: HtmlFactory): TextFactory {
    override fun create(text: String, font: Font?, possible: HTMLElement?): HTMLElement {
        val element = htmlFactory.createOrUse(TEXT_ELEMENT, possible)

        if (element.innerHTML != text) {
            element.innerHTML = ""
            element.add(htmlFactory.createText(text))
        }

        font?.let {
            element.style.setFont(it)
        }

        if (element == possible) {
            element.style.setTextDecoration(null)
        }

        return element
    }

    override fun create(text: StyledText, possible: HTMLElement?): HTMLElement {
        if (text.count == 1) {
            text.first().also { (text, style) ->
                return create(text, style.font, possible).also {
                    applyStyle(it, style)
                }
            }
        }

        val element = htmlFactory.createOrUse("B", possible)

        element.clear()

        text.forEach { (text, style) ->
            element.add(create(text, style.font).also { element ->
                element.style.setDisplay (Inline())
                element.style.setPosition(Static())

                applyStyle(element, style)
            })
        }

        return element
    }

    override fun wrapped(text: String, font: Font?, width: Double, indent: Double, possible: HTMLElement?) = wrapped(text, font, indent, possible).also {
        it.style.setWidth(width)
    }

    override fun wrapped(text: String, font: Font?, indent: Double, possible: HTMLElement?) = create(text, font, possible).also {
        applyWrap(it, indent)
    }

    override fun wrapped(text: StyledText, width: Double, indent: Double, possible: HTMLElement?) = wrapped(text, indent, possible).also {
        it.style.setWidth(width)
    }

    override fun wrapped(text: StyledText, indent: Double, possible: HTMLElement?) = create(text, possible).also {
        if (it.nodeName.equals(TEXT_ELEMENT, ignoreCase = true)) {
            applyWrap(it, indent)
        } else {
            (0 until it.numChildren).map { i -> it.childAt(i) }.filterIsInstance<HTMLElement>().forEach { applyWrap(it, indent) }
        }
    }

    private fun applyWrap(element: HTMLElement, indent: Double) {
        element.style.whiteSpace = "pre-wrap"
        element.style.setTextIndent(indent)
    }

    private fun applyStyle(element: HTMLElement, style: Style) {
        style.foreground?.let {
            if (it is ColorPaint) {
                element.style.setColor(it.color)
            } else {
                // TODO: Implement
            }
        } ?: {
            element.style.setColor(null)
        }()

        style.background?.let {
            if (it is ColorPaint) {
                element.style.setBackgroundColor(it.color)
            } else {
                // TODO: Implement
            }
        } ?: {
            element.style.setBackgroundColor(null)
        }()

        element.style.setTextDecoration(style.decoration)
    }

    private companion object {
        private const val TEXT_ELEMENT = "PRE"
    }
}