package io.nacular.doodle.controls.spinbutton

import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl

public abstract class CommonSpinButtonModel<T>: SpinButtonModel<T> {
    @Suppress("PrivatePropertyName")
    protected val changed_: ChangeObserversImpl<CommonSpinButtonModel<T>> = ChangeObserversImpl(this)

    override val changed: ChangeObservers<SpinButtonModel<T>> = changed_
}