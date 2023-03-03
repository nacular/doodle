package io.nacular.doodle.controls.buttons

import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.observable

/**
 * Created by Nicholas Eddy on 11/10/17.
 */
public open class ButtonModelImpl: ButtonModel {
    override val fired: ChangeObserversImpl<ButtonModelImpl> by lazy { ChangeObserversImpl(this) }

    override val selectedChanged: PropertyObserversImpl<ButtonModel, Boolean> by lazy { PropertyObserversImpl(this) }
    override var selected       : Boolean                                     by observable(false, selectedChanged)

    override val armedChanged: PropertyObserversImpl<ButtonModel, Boolean> by lazy { PropertyObserversImpl(this) }
    override var armed       : Boolean                                     by observable(false, armedChanged)

    override val pressedChanged: PropertyObserversImpl<ButtonModel, Boolean> by lazy { PropertyObserversImpl(this) }
    override var pressed: Boolean by observable(false) { old,new ->
        // TODO: should this just call fire()?  It's strange that armed remains true after this
        if (!new && armed) { fired() }

        pressedChanged(old, new)
    }

    override val pointerOverChanged: PropertyObserversImpl<ButtonModel, Boolean> by lazy { PropertyObserversImpl(this) }
    override var pointerOver       : Boolean by observable(false, pointerOverChanged)

    override var buttonGroup: ButtonGroup? = null

    override fun fire() {
        if (armed) {
            armed = false

            fired()
        }
    }
}
