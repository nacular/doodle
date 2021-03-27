package io.nacular.doodle.controls.buttons

import io.nacular.doodle.accessibility.checkbox
import io.nacular.doodle.core.Icon

/**
 * Created by Nicholas Eddy on 2/2/18.
 */
public open class CheckBox(text: String = "", icon: Icon<Button>? = null): ToggleButton(text, icon, role = checkbox()) {
    public var indeterminate: Boolean by styleProperty(false) { true }
}