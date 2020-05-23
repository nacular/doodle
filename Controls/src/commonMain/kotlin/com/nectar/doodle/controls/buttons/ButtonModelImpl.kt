package com.nectar.doodle.controls.buttons

import com.nectar.doodle.utils.ChangeObserversImpl
import com.nectar.doodle.utils.ObservableProperty
import com.nectar.doodle.utils.PropertyObserversImpl
import com.nectar.doodle.utils.observable

/**
 * Created by Nicholas Eddy on 11/10/17.
 */
open class ButtonModelImpl: ButtonModel {
    override val fired by lazy { ChangeObserversImpl(this) }

    override val selectedChanged by lazy { PropertyObserversImpl<ButtonModel, Boolean>(this) }
    override var selected        by ObservableProperty(false, { this }, selectedChanged)

    override val armedChanged by lazy { PropertyObserversImpl<ButtonModel, Boolean>(this) }
    override var armed        by ObservableProperty(false, { this }, armedChanged)

    override val pressedChanged by lazy { PropertyObserversImpl<ButtonModel, Boolean>(this) }
    override var pressed        by observable(false) { _,old,new ->
        // TODO: should this just call fire()?  It's strange that armed remains true after this
        if (!new && armed) { fired() }

        pressedChanged(old, new)
    }

    override val pointerOverChanged by lazy { PropertyObserversImpl<ButtonModel, Boolean>(this) }
    override var pointerOver        by ObservableProperty(false, { this }, pointerOverChanged)

    override var buttonGroup: ButtonGroup? = null

    override fun fire() {
        if (armed) {
            armed = false

            fired()
        }
    }
}
