package io.nacular.doodle.controls.buttons

import io.nacular.doodle.accessibility.ToggleButtonRole
import io.nacular.doodle.controls.binding
import io.nacular.doodle.core.Icon
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl

public interface ToggleButtonModel: ButtonModel

@Suppress("PrivatePropertyName")
public open class ToggleButton protected constructor(
        text: String = "",
        icon: Icon<Button>? = null,
        model: ToggleButtonModel = ToggleButtonModelImpl(),
        private val role: ToggleButtonRole
): PushButton(text, icon, model, role) {
    public constructor(text: String = "",
            icon: Icon<Button>? = null,
            model: ToggleButtonModel = ToggleButtonModelImpl()): this(text, icon, model, ToggleButtonRole())

    protected class ToggleButtonModelImpl: ButtonModelImpl(), ToggleButtonModel {
        override var selected: Boolean
            get(   ) = super.selected
            set(new) {
                if (tempSelected != new) {
                    tempSelected = new

                    buttonGroup?.let {
                        callCount++
                        it.setSelected(this, new)
                        callCount--

                        if (callCount > 0) {
                            return
                        }
                    }

                    super.selected = tempSelected
                }
            }

        private var callCount    = 0
        private var tempSelected = super.selected

        override var pressed: Boolean
            get(   ) = super.pressed
            set(new) {
                if (super.pressed != new) {
                    super.pressed = new

                    if (!pressed && armed) {
                        selected = !selected
                    }
                }
            }
    }

    private var roleBinding by binding(role.bind(model))

    public val selectedChanged: PropertyObservers<ToggleButton, Boolean> = PropertyObserversImpl<ToggleButton, Boolean>(this)

    private val selectedChanged_ = { _: ButtonModel, old: Boolean, new: Boolean -> (selectedChanged as PropertyObserversImpl)(old, new) }

    init {
        super.model.selectedChanged += selectedChanged_
    }

    // FIXME: It is possible to assign a non-ToggleButtonModel to these types.  Is that desirable?
    override var model: ButtonModel
        get() = super.model
        set(new) {
            super.model.selectedChanged -= selectedChanged_

            super.model = new

            roleBinding = role.bind(new)

            super.model.selectedChanged += selectedChanged_
        }
}
