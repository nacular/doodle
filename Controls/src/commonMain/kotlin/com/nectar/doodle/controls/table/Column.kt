package com.nectar.doodle.controls.table

import com.nectar.doodle.core.View
import com.nectar.doodle.layout.Constraints

interface Column<T> {
    val header        : View?
    val width         : Double
    val minWidth      : Double
    val maxWidth      : Double?
    var preferredWidth: Double?

    val cellPosition  : (Constraints.() -> Unit)?
    val headerPosition: (Constraints.() -> Unit)?

    fun moveBy(x: Double)
    fun resetPosition()
}