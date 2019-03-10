package com.nectar.doodle.drawing

import com.nectar.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 2/28/19.
 */
interface Renderable {
    val size: Size

    fun render(canvas: Canvas)
}