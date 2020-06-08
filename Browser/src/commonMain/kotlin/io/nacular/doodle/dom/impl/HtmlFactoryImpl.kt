package io.nacular.doodle.dom.impl

import io.nacular.doodle.Document
import io.nacular.doodle.HTMLButtonElement
import io.nacular.doodle.HTMLElement
import io.nacular.doodle.HTMLImageElement
import io.nacular.doodle.HTMLInputElement
import io.nacular.doodle.Node
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.clearBoundStyles
import io.nacular.doodle.dom.clearVisualStyles

internal class HtmlFactoryImpl(override val root: HTMLElement, private val document: Document): HtmlFactory {
    override fun <T: HTMLElement> create() = create("DIV") as T

    @Suppress("UNCHECKED_CAST")
    override fun <T: HTMLElement> create(tag: String) = document.createElement(tag) as T

    override fun createText(text: String) = document.createTextNode(text)

    override fun createImage(source: String) = create<HTMLImageElement>("IMG").apply { src = source; draggable = false }

    override fun createOrUse(tag: String, possible: Node?): HTMLElement = when {
        possible == null || possible !is HTMLElement || possible.parentNode != null && !possible.nodeName.equals(tag, ignoreCase = true) -> create(tag)
        else -> possible.apply {
            clearBoundStyles ()
            clearVisualStyles()
        }
    }

    override fun createInput (): HTMLInputElement  = create("INPUT" )
    override fun createButton(): HTMLButtonElement = create("BUTTON")
}
