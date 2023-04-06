package io.nacular.doodle.controls.buttons

import io.nacular.doodle.accessibility.ButtonRole
import io.nacular.doodle.core.Icon

/**
 * Simple component that responds to the pointer and keyboard, allowing a user to "click".
 *
 * @author Nicholas Eddy
 */
public open class PushButton protected constructor(
        text : String        = "",
        icon : Icon<Button>? = null,
        model: ButtonModel   = ButtonModelImpl(),
        role : ButtonRole): Button(text, icon, model, role) {

    /**
     * Creates a button.
     *
     * @param text displayed on the button
     * @param icon displayed on the button
     * @param model used to store the button's state
     */
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