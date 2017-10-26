package com.zinoti.jaz.dom

import org.w3c.dom.svg.SVGElement
import kotlin.browser.document

/**
 * Created by Nicholas Eddy on 10/24/17.
 */
interface SvgFactory {
    fun <T: SVGElement> create(tag: String): T
}

private val prototypes = mutableMapOf<String, SVGElement>()

class SvgFactoryImpl: SvgFactory {
    override fun <T : SVGElement> create(tag: String) = prototypes.getOrPut(tag) {
        document.createElementNS( "http://www.w3.org/2000/svg", tag) as T
    }.cloneNode(false) as T
}
