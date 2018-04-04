package com.nectar.doodle.controls.spinner

import com.nectar.doodle.utils.ChangeObservers
import com.nectar.doodle.utils.ChangeObserversImpl

abstract class AbstractModel<T>: Model<T> {
    @Suppress("PrivatePropertyName")
    protected val changed_ = ChangeObserversImpl(this)

    override val changed: ChangeObservers<Model<T>> = changed_
}