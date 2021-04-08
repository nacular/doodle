package io.nacular.doodle.controls.buttons

import io.nacular.doodle.accessibility.ToggleButtonRole
import io.nacular.doodle.controls.Binding
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.PropertyObservers

/**
 * Created by Nicholas Eddy on 11/10/17.
 */

public interface ButtonModel {
    public val armedChanged      : PropertyObservers<ButtonModel, Boolean>
    public val pressedChanged    : PropertyObservers<ButtonModel, Boolean>
    public val selectedChanged   : PropertyObservers<ButtonModel, Boolean>
    public val pointerOverChanged: PropertyObservers<ButtonModel, Boolean>

    public var armed      : Boolean
    public var pressed    : Boolean
    public var selected   : Boolean
    public var pointerOver: Boolean
    public var buttonGroup: ButtonGroup?

    public val fired      : ChangeObservers<ButtonModel>

    public fun fire()
}

public fun ToggleButtonRole.bind(model: ButtonModel): Binding {
    pressed = model.selected

    return object: Binding {
        val selectedChanged: PropertyObserver<ButtonModel, Boolean> = { _,_,new -> pressed = new }

        init { model.selectedChanged += selectedChanged }

        override fun unbind() { model.selectedChanged -= selectedChanged }
    }
}