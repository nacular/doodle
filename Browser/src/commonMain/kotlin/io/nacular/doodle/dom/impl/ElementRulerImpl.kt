package io.nacular.doodle.dom.impl

import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.insert
import io.nacular.doodle.dom.parent
import io.nacular.doodle.geometry.Size

internal class ElementRulerImpl(htmlFactory: HtmlFactory): ElementRuler {

    override fun width (element: HTMLElement) = measure(element) { getBoundingClientRect().width                       }
    override fun height(element: HTMLElement) = measure(element) { getBoundingClientRect().height                      }
    override fun size  (element: HTMLElement) = measure(element) { getBoundingClientRect().run { Size(width, height) } }

    private val ruler = htmlFactory.root

    fun <T> measure(element: HTMLElement, block: HTMLElement.() -> T): T = when (element.parent) {
        null -> {
            ruler.insert(element, 0)

            element.run(block).also {
                ruler.removeChild(element)
            }
        }
        else -> element.run(block)
    }
}