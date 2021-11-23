package io.nacular.doodle.controls.buttons

import io.nacular.doodle.accessibility.SwitchRole

public class Switch(
        text : String = "",
        model: ToggleButtonModel = ToggleButtonModelImpl()
): ToggleButton(text, model = model, role = SwitchRole())