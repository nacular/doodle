package io.nacular.doodle.dom.impl

import io.nacular.doodle.dom.Document
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.SVGElement
import io.nacular.doodle.dom.SvgFactory
import io.nacular.doodle.dom.cloneNode_
import io.nacular.doodle.dom.createElementNS_

internal class SvgFactoryImpl(override val root: HTMLElement, private val document: Document): SvgFactory {
    private val prototypes = mutableMapOf<String, SVGElement>()

    @Suppress("UNCHECKED_CAST")
    override operator fun <T: SVGElement> invoke(tag: String) = prototypes.getOrPut(tag) {
        document.createElementNS_("http://www.w3.org/2000/svg", tag) as T
    }.cloneNode_(false) as T
}
