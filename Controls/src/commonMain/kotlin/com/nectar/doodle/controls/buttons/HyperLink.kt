package com.nectar.doodle.controls.buttons

import com.nectar.doodle.core.Icon

/**
 * Created by Nicholas Eddy on 12/7/19.
 */
class HyperLink(
        text : String        = "",
        icon : Icon<Button>? = null,
        model: ButtonModel   = ButtonModelImpl()): PushButton(text, icon, model) {

    var visited = false

    init {
        model.fired += {
            visited = true
        }
    }
}