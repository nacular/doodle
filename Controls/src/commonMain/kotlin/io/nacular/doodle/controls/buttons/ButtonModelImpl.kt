package io.nacular.doodle.controls.buttons

import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.observable
import kotlin.properties.Delegates.observable

/**
 * Created by Nicholas Eddy on 11/10/17.
 */
open class ButtonModelImpl: ButtonModel {
    override val fired by lazy { ChangeObserversImpl(this) }

    override val selectedChanged by lazy { PropertyObserversImpl<ButtonModel, Boolean>(this) }
    override var selected        by observable(false, selectedChanged)

    override val armedChanged by lazy { PropertyObserversImpl<ButtonModel, Boolean>(this) }
    override var armed        by observable(false, armedChanged)

    override val pressedChanged by lazy { PropertyObserversImpl<ButtonModel, Boolean>(this) }
    override var pressed        by observable(false) { _,old,new ->
        // TODO: should this just call fire()?  It's strange that armed remains true after this
        if (!new && armed) { fired() }

        pressedChanged(old, new)
    }

    override val pointerOverChanged by lazy { PropertyObserversImpl<ButtonModel, Boolean>(this) }
    override var pointerOver        by observable(false, pointerOverChanged)

    override var buttonGroup: ButtonGroup? = null

    override fun fire() {
        if (armed) {
            armed = false

            fired()
        }
    }
}
