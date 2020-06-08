package io.nacular.doodle.controls.spinner

import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl

abstract class CommonSpinnerModel<T>: Model<T> {
    @Suppress("PrivatePropertyName")
    protected val changed_ = ChangeObserversImpl(this)

    override val changed: ChangeObservers<Model<T>> = changed_
}