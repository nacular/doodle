package io.nacular.doodle.dom

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.geometry.Size

interface ElementRuler {
    fun size(element: HTMLElement): Size

    fun width (element: HTMLElement) = size(element).width
    fun height(element: HTMLElement) = size(element).height
}