package com.nectar.doodle.dom

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.Node
import org.w3c.dom.Text
import kotlin.browser.document

/**
 * Created by Nicholas Eddy on 10/24/17.
 */

interface HtmlFactory {
    val body: HTMLElement

    fun create     (tag   : String                 ): HTMLElement
    fun createText (text  : String                 ): Text
    fun createImage(source: String                 ): HTMLImageElement
    fun createOrUse(tag   : String, possible: Node?): HTMLElement
}

class HtmlFactoryImpl: HtmlFactory {
    override val body get() = document.body!!

    override fun create(tag: String) = prototypes.getOrPut(tag) {
        document.createElement(tag) as HTMLElement
    }.cloneNode(false) as HTMLElement

    override fun createText(text: String) = document.createTextNode(text)

    override fun createImage(source: String) = (create("IMG") as HTMLImageElement).also { it.src = source }

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

    private val prototypes = mutableMapOf<String, HTMLElement>()
}
