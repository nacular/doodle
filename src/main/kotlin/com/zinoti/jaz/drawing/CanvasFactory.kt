package com.zinoti.jaz.drawing

import org.w3c.dom.HTMLElement

/**
 * Created by Nicholas Eddy on 10/23/17.
 */
interface CanvasFactory {
    fun create(region: HTMLElement): Canvas
}