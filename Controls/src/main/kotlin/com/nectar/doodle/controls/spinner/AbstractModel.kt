package com.nectar.doodle.controls.spinner

import com.nectar.doodle.utils.ChangeObservers
import com.nectar.doodle.utils.ChangeObserversImpl

abstract class AbstractModel<T>: Model<T> {
    @Suppress("PrivatePropertyName")
    protected val onChanged_ = ChangeObserversImpl(this)

    override val onChanged: ChangeObservers<Model<T>> = onChanged_
}