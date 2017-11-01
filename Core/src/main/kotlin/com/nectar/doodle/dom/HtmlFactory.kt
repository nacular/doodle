package com.nectar.doodle.dom

import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.Text
import kotlin.browser.document

/**
 * Created by Nicholas Eddy on 10/24/17.
 */

interface HtmlFactory {
    val body: HTMLElement

    fun create     (tag : String): HTMLElement
    fun createText (text: String): Text
    fun createOrUse(tag : String, possible: Node?): HTMLElement
}


class HtmlFactoryImpl: HtmlFactory {
    override val body get() = document.body!!

    override fun create(tag: String): HTMLElement {
        var master: HTMLElement? = prototypes[tag]

        // No cached version exists

        if (master == null) {
            master = document.createElement(tag) as HTMLElement

            prototypes.put(tag, master)
        }

        return master.cloneNode(false) as HTMLElement
    }

    override fun createText(text: String) = document.createTextNode(text)

    override fun createOrUse(tag: String, possible: Node?): HTMLElement {
        var result = possible

        if (result == null || result !is HTMLElement || result.parentNode != null && result.nodeName != tag) {
            result = create(tag)
        } else {
            result.clearBoundStyles ()
            result.clearVisualStyles()
        }

        return result
    }

    companion object {
        private val prototypes = mutableMapOf<String, HTMLElement>()
    }
}
