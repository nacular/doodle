package io.nacular.doodle.drawing

import io.nacular.doodle.HTMLElement

/**
 * Created by Nicholas Eddy on 10/23/17.
 */
interface CanvasFactory {
    operator fun invoke(region: HTMLElement): Canvas
}