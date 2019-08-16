package com.nectar.doodle.dom

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.SVGElement

/**
 * Created by Nicholas Eddy on 10/24/17.
 */
interface SvgFactory {
    val root: HTMLElement

    operator fun <T: SVGElement> invoke(tag: String): T
}