package com.zinoti.jaz.drawing

import com.zinoti.jaz.drawing.Color.Companion.Black


class SolidBrush private constructor(val color: Color): Brush() {

    private constructor(): this(Black)

    override val visible = color.opacity > 0

    companion object {
        fun create() = Default

        fun create(color: Color) = SolidBrush(color)

        private val Default = SolidBrush()
    }
}
