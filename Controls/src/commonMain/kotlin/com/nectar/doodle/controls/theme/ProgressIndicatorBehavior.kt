package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.ProgressIndicator
import com.nectar.doodle.theme.Behavior

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
abstract class ProgressIndicatorBehavior<in T: ProgressIndicator>: Behavior<T> {
    override fun install(view: T) {
        view.changed += changed
    }

    override fun uninstall(view: T) {
        view.changed -= changed
    }

    private val changed: (ProgressIndicator, Double, Double) -> Unit = { it,_,_-> it.rerender() }
}