package com.nectar.doodle.drawing

import com.nectar.doodle.dom.FontStyle
import com.nectar.doodle.dom.FontWeight.Bold
import com.nectar.doodle.dom.FontWeight.Normal
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.setFontFamily
import com.nectar.doodle.dom.setFontSize
import com.nectar.doodle.dom.setFontStyle
import com.nectar.doodle.dom.setFontWeight
import org.w3c.dom.HTMLElement

/**
 * Created by Nicholas Eddy on 10/30/17.
 */

interface TextGlyph {
    val element: HTMLElement
}

interface TextFactory {
    fun create (text: String, font: Font, possible: HTMLElement? = null): HTMLElement
    fun wrapped(text: String, font: Font, indent: Double = 0.0, possible: HTMLElement? = null): HTMLElement
}


class TextFactoryImpl(private val htmlFactory: HtmlFactory): TextFactory {
    override fun create(text: String, font: Font, possible: HTMLElement?): HTMLElement {
        val element = htmlFactory.createOrUse("PRE", possible)

        if (element.innerHTML != text) {
            element.innerHTML = ""
            element.add(htmlFactory.createText(text))
        }

        element.style.setFontSize  (font.size  )
        element.style.setFontFamily(font.family)

        if (font.isBold) {
            element.style.setFontWeight(Bold)
        } else {
            element.style.setFontWeight(Normal)
        }
        if (font.isItalic) {
            element.style.setFontStyle(FontStyle.Italic)
        }

        return element
    }

    override fun wrapped(text: String, font: Font, indent: Double, possible: HTMLElement?): HTMLElement {
        // FIXME: Portability
        return create(text, font, possible).also {
            it.style.whiteSpace = "pre-wrap"
            it.style.textIndent = "${indent}px"
        }
    }
}