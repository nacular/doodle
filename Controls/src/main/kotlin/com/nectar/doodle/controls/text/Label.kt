package com.nectar.doodle.controls.text

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.utils.HorizontalAlignment
import com.nectar.doodle.utils.VerticalAlignment


class Label(
        text               : String,
        verticalAlignment  : VerticalAlignment   = VerticalAlignment.Center,
        horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Center): Gizmo() {


    var text                = text
    var verticalAlignment   = verticalAlignment
    var horizontalAlignment = horizontalAlignment

    init {
        focusable = false
    }
}
