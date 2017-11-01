package com.nectar.doodle.controls.text

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Icon
import com.nectar.doodle.utils.Anchor.Leading
import com.nectar.doodle.utils.Location
import com.nectar.doodle.utils.Location.Center


class Label constructor(
        text               : String,
        icon               : Icon<Label>? = null,
        verticalAlignment  : Location     = Center,
        horizontalAlignment: Location     = Center): Gizmo() {


//    val uiClassKey: String
//        get() = "LabelUI"

    var text                = text
    var icon                = icon
    var iconAnchor          = Leading
    var disabledIcon        = null as Icon<Label>?
    var iconTextSpacing     = 0
    var verticalAlignment   = verticalAlignment
    var horizontalAlignment = horizontalAlignment

    init {
        focusable = false
    }
}
