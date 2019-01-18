package com.nectar.doodle.dom

import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import kotlin.browser.document

/**
 * Created by Nicholas Eddy on 10/24/17.
 */
interface SvgFactory {
    val root: HTMLElement

    operator fun <T: SVGElement> invoke(tag: String): T
}

internal class SvgFactoryImpl(override val root: HTMLElement): SvgFactory {
    private val prototypes = mutableMapOf<String, SVGElement>()

    @Suppress("UNCHECKED_CAST")
    override operator fun <T : SVGElement> invoke(tag: String) = prototypes.getOrPut(tag) {
        document.createElementNS( "http://www.w3.org/2000/svg", tag) as T
    }.cloneNode(false) as T
}
