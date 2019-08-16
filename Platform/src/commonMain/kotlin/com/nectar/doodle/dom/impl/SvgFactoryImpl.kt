package com.nectar.doodle.dom

import com.nectar.doodle.Document
import com.nectar.doodle.HTMLElement
import com.nectar.doodle.SVGElement


internal class SvgFactoryImpl(override val root: HTMLElement, private val document: Document): SvgFactory {
    private val prototypes = mutableMapOf<String, SVGElement>()

    @Suppress("UNCHECKED_CAST")
    override operator fun <T: SVGElement> invoke(tag: String) = prototypes.getOrPut(tag) {
        document.createElementNS("http://www.w3.org/2000/svg", tag) as T
    }.cloneNode(false) as T
}
