package com.nectar.doodle.dom

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.geometry.Size

interface ElementRuler {
    fun size(element: HTMLElement): Size

    fun width (element: HTMLElement) = size(element).width
    fun height(element: HTMLElement) = size(element).height
}