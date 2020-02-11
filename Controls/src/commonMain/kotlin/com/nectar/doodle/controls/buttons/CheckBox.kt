package com.nectar.doodle.controls.buttons

import com.nectar.doodle.core.Icon

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