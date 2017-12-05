package com.nectar.doodle.dom

import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import kotlin.browser.document

/**
 * Created by Nicholas Eddy on 10/24/17.
 */
interface SvgFactory {
    val body: HTMLElement

    fun <T: SVGElement> create(tag: String): T
}

private val prototypes = mutableMapOf<String, SVGElement>()

class SvgFactoryImpl: SvgFactory {
    override val body get() = document.body!!

    override fun <T : SVGElement> create(tag: String) = prototypes.getOrPut(tag) {
        document.createElementNS( "http://www.w3.org/2000/svg", tag) as T
    }.cloneNode(false) as T
}
