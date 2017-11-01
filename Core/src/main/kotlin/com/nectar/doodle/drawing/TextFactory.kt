package com.nectar.doodle.drawing

import com.nectar.doodle.dom.Display
import com.nectar.doodle.dom.FontStyle
import com.nectar.doodle.dom.FontWeight.Bold
import com.nectar.doodle.dom.FontWeight.Normal
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.Position
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.setBackgroundColor
import com.nectar.doodle.dom.setColor
import com.nectar.doodle.dom.setDisplay
import com.nectar.doodle.dom.setFontFamily
import com.nectar.doodle.dom.setFontSize
import com.nectar.doodle.dom.setFontStyle
import com.nectar.doodle.dom.setFontWeight
import com.nectar.doodle.dom.setPosition
import com.nectar.doodle.text.StyledText
import org.w3c.dom.HTMLElement
import kotlin.dom.clear

/**
 * Created by Nicholas Eddy on 10/30/17.
 */

interface TextGlyph {
    val element: HTMLElement
}

interface TextFactory {
    fun create (text: String, font: Font? = null, possible: HTMLElement? = null): HTMLElement
    fun wrapped(text: String, font: Font? = null, indent: Double = 0.0, possible: HTMLElement? = null): HTMLElement
    fun styled (text: StyledText, possible: HTMLElement? = null): HTMLElement
}


class TextFactoryImpl(private val htmlFactory: HtmlFactory): TextFactory {
    override fun create(text: String, font: Font?, possible: HTMLElement?): HTMLElement {
        val element = htmlFactory.createOrUse("PRE", possible)

        if (element.innerHTML != text) {
            element.innerHTML = ""
            element.add(htmlFactory.createText(text))
        }

        font?.let {
            element.style.setFontSize  (it.size  )
            element.style.setFontFamily(it.family)

            if (it.isBold) {
                element.style.setFontWeight(Bold)
            } else {
                element.style.setFontWeight(Normal)
            }
            if (it.isItalic) {
                element.style.setFontStyle(FontStyle.Italic)
            }
        }

        return element
    }

    override fun wrapped(text: String, font: Font?, indent: Double, possible: HTMLElement?): HTMLElement {
        // FIXME: Portability
        return create(text, font, possible).also {
            it.style.whiteSpace = "pre-wrap"
            it.style.textIndent = "${indent}px"
        }
    }

    override fun styled(text: StyledText, possible: HTMLElement?): HTMLElement {
        if (text.count == 1) {
            text.first().also { (text, style) ->
                return create(text, style.font, possible)
            }
        }

        val element = htmlFactory.createOrUse("B", possible)

        element.clear()

        text.forEach { (text, style) ->
            element.add(create(text, style.font).also { element ->
                element.style.setDisplay (Display.Inline)
                element.style.setPosition(Position.Relative)
                element.style.setColor   (style.foreground)

                style.background?.let {
                    element.style.setBackgroundColor(it)
                }
            })
        }

        return element
    }
}