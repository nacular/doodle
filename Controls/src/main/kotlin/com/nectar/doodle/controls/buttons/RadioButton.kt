package com.nectar.doodle.controls.buttons

import com.nectar.doodle.core.Icon

/**
 * Created by Nicholas Eddy on 2/2/18.
 */
class RadioButton(text: String = "", icon: Icon<Button>? = null): ToggleButton(text, icon) {
    constructor(icon: Icon<Button>): this("", icon)
}