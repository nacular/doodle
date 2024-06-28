package io.nacular.doodle.controls.spinner

import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl

@Deprecated("Use CommonSpinButtonModelModel", replaceWith = ReplaceWith("CommonSpinButtonModelModel<T>"))
public typealias CommonSpinnerModel<T> = CommonSpinButtonModelModel<T>

public abstract class CommonSpinButtonModelModel<T>: SpinButtonModel<T> {
    @Suppress("PropertyName")
    protected val changed_: ChangeObserversImpl<CommonSpinButtonModelModel<T>> = ChangeObserversImpl(this)

    override val changed: ChangeObservers<SpinButtonModel<T>> = changed_
}