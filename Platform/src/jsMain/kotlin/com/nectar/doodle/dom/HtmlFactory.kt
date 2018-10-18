package com.nectar.doodle.dom

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Node
import org.w3c.dom.Text
import kotlin.browser.document

/**
 * Created by Nicholas Eddy on 10/24/17.
 */

interface HtmlFactory {
    val body: HTMLElement

    fun <T: HTMLElement> create(           ): T
    fun <T: HTMLElement> create(tag: String): T

    fun createText (text  : String                 ): Text
    fun createImage(source: String                 ): HTMLImageElement
    fun createOrUse(tag   : String, possible: Node?): HTMLElement

    fun createInput (): HTMLInputElement
    fun createButton(): HTMLButtonElement
}

internal class HtmlFactoryImpl: HtmlFactory {
    override val body get() = document.body!!

    override fun <T: HTMLElement> create() = create("DIV") as T

    @Suppress("UNCHECKED_CAST")
    override fun <T: HTMLElement> create(tag: String) = prototypes.getOrPut(tag) {
        document.createElement(tag) as T
    }.cloneNode(false) as T

    override fun createText(text: String) = document.createTextNode(text)

    override fun createImage(source: String) = (create("IMG") as HTMLImageElement).also { it.src = source }

    override fun createOrUse(tag: String, possible: Node?): HTMLElement {
        var result = possible

        if (result == null || result !is HTMLElement || result.parentNode != null && !result.nodeName.equals(tag, ignoreCase = true)) {
            result = create(tag)
        } else {
            result.clearBoundStyles ()
            result.clearVisualStyles()
        }

        return result
    }

    override fun createInput(): HTMLInputElement = create("INPUT")

    override fun createButton(): HTMLButtonElement = create("BUTTON")

    private val prototypes = mutableMapOf<String, HTMLElement>()
}
