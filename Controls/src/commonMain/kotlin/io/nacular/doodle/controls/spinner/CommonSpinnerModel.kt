package io.nacular.doodle.controls.spinner

import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl

public abstract class CommonSpinnerModel<T>: SpinnerModel<T> {
    @Suppress("PrivatePropertyName")
    protected val changed_: ChangeObserversImpl<CommonSpinnerModel<T>> = ChangeObserversImpl(this)

    override val changed: ChangeObservers<SpinnerModel<T>> = changed_
}