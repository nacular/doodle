package io.nacular.doodle.controls.buttons

import io.nacular.doodle.accessibility.ButtonRole
import io.nacular.doodle.core.Icon

/**
 * Created by Nicholas Eddy on 11/14/17.
 */
public open class PushButton protected constructor(
        text : String        = "",
        icon : Icon<Button>? = null,
        model: ButtonModel   = ButtonModelImpl(),
        role : ButtonRole): Button(text, icon, model, role) {

    public constructor(
            text : String        = "",
            icon : Icon<Button>? = null,
            model: ButtonModel   = ButtonModelImpl()): this(text, icon, model, ButtonRole())

    override fun click() {
        if (enabled) {
            model.armed   = true
            model.pressed = true
            model.pressed = false
            model.armed   = false
        }
    }
}