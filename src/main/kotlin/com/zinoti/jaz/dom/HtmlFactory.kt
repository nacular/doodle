package com.zinoti.jaz.dom

import org.w3c.dom.HTMLElement
import org.w3c.dom.Text
import kotlin.browser.document

/**
 * Created by Nicholas Eddy on 10/24/17.
 */

interface HtmlFactory {
    fun create    (tag : String): HTMLElement
    fun createText(text: String): Text
}


class HtmlFactoryImpl: HtmlFactory {
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

    companion object {
        private val prototypes = mutableMapOf<String, HTMLElement>()
    }
}
