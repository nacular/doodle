package com.zinoti.jaz.dom

import org.w3c.dom.svg.SVGElement
import kotlin.browser.document

/**
 * Created by Nicholas Eddy on 10/24/17.
 */
typealias SvgFactory = (tag: String) -> SVGElement


private val prototypes = mutableMapOf<String, SVGElement>()

fun defaultSvgFactory(tag: String) = prototypes.getOrPut(tag) {
    document.createElementNS( "http://www.w3.org/2000/svg", tag) as SVGElement
}.cloneNode(false) as SVGElement
