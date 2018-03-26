package com.nectar.doodle.controls.buttons

import com.nectar.doodle.utils.ChangeObserversImpl
import com.nectar.doodle.utils.ObservableProperty
import com.nectar.doodle.utils.PropertyObserversImpl
import com.nectar.doodle.utils.observable

/**
 * Created by Nicholas Eddy on 11/10/17.
 */
open class ButtonModelImpl: ButtonModel {
    override val onAction by lazy { ChangeObserversImpl(this) }

    override val selectedChanged by lazy { PropertyObserversImpl<ButtonModel, Boolean>(mutableSetOf()) }
    override var selected        by ObservableProperty(false, { this }, selectedChanged)

    override val armedChanged by lazy { PropertyObserversImpl<ButtonModel, Boolean>(mutableSetOf()) }
    override var armed        by ObservableProperty(false, { this }, armedChanged)

    override val pressedChanged by lazy { PropertyObserversImpl<ButtonModel, Boolean>(mutableSetOf()) }
    override var pressed        by observable(false) { _,old,new ->
        // TODO: should this just call fire()?  It's strange that armed remains true after this
        if (!new && armed) { onAction() }

        pressedChanged.forEach { it(this, old, new) }
    }

    override val mouseOverChanged by lazy { PropertyObserversImpl<ButtonModel, Boolean>(mutableSetOf()) }
    override var mouseOver        by ObservableProperty(false, { this }, mouseOverChanged)

    override var buttonGroup: ButtonGroup? = null

    override fun fire() {
        if (armed) {
            armed = false

            onAction()
        }
    }
}
