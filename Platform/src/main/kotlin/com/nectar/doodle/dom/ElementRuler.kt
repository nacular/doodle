package com.nectar.doodle.dom

import org.w3c.dom.HTMLElement


interface ElementRuler {
    fun width (element: HTMLElement): Double
    fun height(element: HTMLElement): Double
}

class ElementRulerImpl: ElementRuler {
    override fun width (element: HTMLElement) = element.width
    override fun height(element: HTMLElement) = element.height
}