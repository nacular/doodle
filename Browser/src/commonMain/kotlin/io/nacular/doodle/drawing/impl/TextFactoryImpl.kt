package io.nacular.doodle.drawing.impl

import io.nacular.doodle.dom.HTMLAnchorElement
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Inline
import io.nacular.doodle.dom.Static
import io.nacular.doodle.dom.Visible
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.childAt
import io.nacular.doodle.dom.clear
import io.nacular.doodle.dom.numChildren
import io.nacular.doodle.dom.setBackgroundColor
import io.nacular.doodle.dom.setColor
import io.nacular.doodle.dom.setDisplay
import io.nacular.doodle.dom.setDomPosition
import io.nacular.doodle.dom.setFont
import io.nacular.doodle.dom.setLineHeight
import io.nacular.doodle.dom.setOverflow
import io.nacular.doodle.dom.setSelectable
import io.nacular.doodle.dom.setTextAlignment
import io.nacular.doodle.dom.setTextDecoration
import io.nacular.doodle.dom.setTextIndent
import io.nacular.doodle.dom.setTextSpacing
import io.nacular.doodle.dom.setWidth
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.TextFactory
import io.nacular.doodle.text.AnnotatedString
import io.nacular.doodle.text.AnnotatedString.Annotation
import io.nacular.doodle.text.TextSpacing
import io.nacular.doodle.utils.TextAlignment

internal class TextFactoryImpl(private val htmlFactory: HtmlFactory): TextFactory {
    override fun create(text: String, font: Font?, url: String?, textSpacing: TextSpacing, possible: HTMLElement?): HTMLElement {
        val element = htmlFactory.createOrUse(TEXT_ELEMENT, possible)

        val subElement = when (url) {
            null -> element
            else -> (htmlFactory.createOrUse(A_ELEMENT, possible?.firstChild as? HTMLElement) as HTMLAnchorElement).apply {
                href = url
                style.font     = "inherit"
                style.position = "initial"

                element.appendChild(this)
            }
        }

        if (subElement.textContent != text) {
            subElement.textContent = text
        }

        font?.let {
            element.style.setFont(it)
        }

        element.style.setTextSpacing(textSpacing)
        element.style.setSelectable (false      )

        if (url != null) {
            element.style.pointerEvents = "auto"
        }

        if (element == possible) {
            element.style.setTextDecoration(null)
            element.style.whiteSpace = ""
            element.style.textIndent = ""
            element.style.filter     = ""
        }

        return element
    }

    override fun create(text: String, font: Font?, url: String?, lineSpacing: Float, textSpacing: TextSpacing, possible: HTMLElement?): HTMLElement = create(
        text,
        font,
        url,
        textSpacing,
        possible
    ).also { it.style.setLineHeight(lineSpacing) }

    override fun create(text: AnnotatedString, textSpacing: TextSpacing, possible: HTMLElement?): HTMLElement {
        if (text.count == 1) {
            text.first().also { (text, annotation) ->
                return create(text, annotation.font, annotation.url, textSpacing, possible).also {
                    applyAnnotation(it, annotation)
                }
            }
        }

        return createBoxed(text, textSpacing, possible)
    }

    override fun create(
        text       : AnnotatedString,
        lineSpacing: Float,
        textSpacing: TextSpacing,
        possible   : HTMLElement?
    ): HTMLElement = create(text, textSpacing, possible).also { it.style.setLineHeight(lineSpacing) }

    override fun wrapped(
        text       : String,
        font       : Font?,
        url        : String?,
        width      : Double,
        indent     : Double,
        alignment  : TextAlignment,
        lineSpacing: Float,
        textSpacing: TextSpacing,
        possible   : HTMLElement?
    ) = wrapped(
        text        = text,
        url         = url,
        font        = font,
        indent      = indent,
        alignment   = alignment,
        lineSpacing = lineSpacing,
        textSpacing = textSpacing,
        possible    = possible
    ).also {
        it.style.setWidth(width)
    }

    override fun wrapped(
        text       : String,
        font       : Font?,
        url        : String?,
        indent     : Double,
        alignment  : TextAlignment,
        lineSpacing: Float,
        textSpacing: TextSpacing,
        possible   : HTMLElement?
    ) = create(
        text        = text,
        url         = url,
        font        = font,
        possible    = possible,
        textSpacing = textSpacing,
    ).also {
        it.style.setLineHeight   (lineSpacing)
        it.style.setTextAlignment(alignment  )
        it.style.setTextSpacing  (textSpacing)
        applyWrap(it, indent)
    }

    override fun wrapped(
        text       : AnnotatedString,
        width      : Double,
        indent     : Double,
        alignment  : TextAlignment,
        lineSpacing: Float,
        textSpacing: TextSpacing,
        possible   : HTMLElement?
    ) = wrapped(
        text        = text,
        indent      = indent,
        possible    = possible,
        alignment   = alignment,
        lineSpacing = lineSpacing,
        textSpacing = textSpacing,
    ).also {
        it.style.setWidth(width)
    }

    override fun wrapped(
        text       : AnnotatedString,
        indent     : Double,
        alignment  : TextAlignment,
        lineSpacing: Float,
        textSpacing: TextSpacing,
        possible   : HTMLElement?
    ) = create(
        text        = text,
        possible    = possible,
        textSpacing = textSpacing,
    ).also {
        it.style.setLineHeight   (lineSpacing)
        it.style.setTextAlignment(alignment  )
        it.style.setTextSpacing  (textSpacing)

        if (it.nodeName.equals(TEXT_ELEMENT, ignoreCase = true)) {
            applyWrap(it, indent)
        } else {
            it.style.setTextIndent(indent)
            (0 until it.numChildren).map { i -> it.childAt(i) }.filterIsInstance<HTMLElement>().forEach { applyWrap(it, null) }
        }
    }

    fun createBoxed(text: AnnotatedString, textSpacing: TextSpacing, possible: HTMLElement?): HTMLElement {
        val element = htmlFactory.createOrUse("B", possible)

        element.clear()
        element.style.width  = ""
        element.style.filter = ""
        element.style.setBackgroundColor(null)
        element.style.setOverflow(Visible())

        text.forEach { (text, annotation) ->
            element.add(create(text, annotation.font, annotation.url, textSpacing).also { element ->
                element.style.setDisplay    (Inline()   )
                element.style.setDomPosition(Static()   )
                element.style.setTextSpacing(textSpacing)

                applyAnnotation(element, annotation)
            })
        }

        return element
    }

    private fun applyWrap(element: HTMLElement, indent: Double?) {
        with(element.style) {
            whiteSpace = "pre-wrap"
            setTextIndent(indent)
        }
    }

    private fun applyAnnotation(element: HTMLElement, annotation: Annotation) {
        annotation.foreground?.let {
            when (it) {
                is ColorPaint -> element.style.setColor(it.color)
                else          -> { /* TODO: Implement */ }
            }
        } ?: run {
            element.style.setColor(null)
        }

        annotation.background?.let {
            when (it) {
                is ColorPaint -> element.style.setBackgroundColor(it.color)
                else          -> { /* TODO: Implement */ }
            }
        } ?: run {
            element.style.setBackgroundColor(null)
        }

        element.style.setTextDecoration(annotation.decoration)
        element.style.setSelectable    (annotation.selectable)

        if (annotation.url != null) {
            element.style.pointerEvents = "auto"
        }
    }

    private companion object {
        private const val A_ELEMENT    = "A"
        private const val TEXT_ELEMENT = "PRE"
    }
}