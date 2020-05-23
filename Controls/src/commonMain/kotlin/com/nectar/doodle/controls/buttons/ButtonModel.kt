package com.nectar.doodle.controls.buttons

import com.nectar.doodle.utils.ChangeObservers
import com.nectar.doodle.utils.PropertyObservers

/**
 * Created by Nicholas Eddy on 11/10/17.
 */

interface ButtonModel {
    val armedChanged      : PropertyObservers<ButtonModel, Boolean>
    val pressedChanged    : PropertyObservers<ButtonModel, Boolean>
    val selectedChanged   : PropertyObservers<ButtonModel, Boolean>
    val pointerOverChanged: PropertyObservers<ButtonModel, Boolean>

    var armed      : Boolean
    var pressed    : Boolean
    var selected   : Boolean
    var pointerOver: Boolean
    var buttonGroup: ButtonGroup?

    val fired      : ChangeObservers<ButtonModel>

    fun fire()
}
