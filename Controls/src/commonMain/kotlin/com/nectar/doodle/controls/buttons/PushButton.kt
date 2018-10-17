package com.nectar.doodle.controls.buttons

import com.nectar.doodle.core.Icon

/**
 * Created by Nicholas Eddy on 11/14/17.
 */
open class PushButton(
        text : String        = "",
        icon : Icon<Button>? = null,
        model: ButtonModel   = ButtonModelImpl()): Button(text, icon, model) {
    override fun click() {
        if (enabled) {
            model.armed   = true
            model.pressed = true
            model.pressed = false
            model.armed   = false
        }
    }
}