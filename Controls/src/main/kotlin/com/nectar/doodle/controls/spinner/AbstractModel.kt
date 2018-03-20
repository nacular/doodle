package com.nectar.doodle.controls.spinner

import com.nectar.doodle.controls.ChangeObservers
import com.nectar.doodle.controls.ChangeObserversImpl

abstract class AbstractModel<T>: Model<T> {
    @Suppress("PrivatePropertyName")
    protected val onChanged_ = ChangeObserversImpl<Model<T>>()

    override val onChanged: ChangeObservers<Model<T>> = onChanged_
}