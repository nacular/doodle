package io.nacular.doodle.controls.spinner

import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl

public abstract class CommonSpinnerModel<T>: SpinButtonModel<T> {
    @Suppress("PrivatePropertyName")
    protected val changed_: ChangeObserversImpl<CommonSpinnerModel<T>> = ChangeObserversImpl(this)

    override val changed: ChangeObservers<SpinButtonModel<T>> = changed_
}