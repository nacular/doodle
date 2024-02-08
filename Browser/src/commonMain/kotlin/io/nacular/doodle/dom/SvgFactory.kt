package io.nacular.doodle.dom

internal interface SvgFactory {
    val root: HTMLElement

    operator fun <T: SVGElement> invoke(tag: String): T
}