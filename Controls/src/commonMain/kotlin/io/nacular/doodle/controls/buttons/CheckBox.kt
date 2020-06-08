package io.nacular.doodle.controls.buttons

import io.nacular.doodle.core.Icon

/**
 * Created by Nicholas Eddy on 2/2/18.
 */
open class CheckBox(text: String = "", icon: Icon<Button>? = null): ToggleButton(text, icon) {
    var indeterminate = false
        set(value) {
            if (field == value) {
                return
            }

            field = value

            styleChanged()
        }
}