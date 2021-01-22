package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.ProgressIndicator
import io.nacular.doodle.core.Behavior

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
public abstract class ProgressIndicatorBehavior<in T: ProgressIndicator>: Behavior<T> {
    override fun install(view: T) {
        view.changed += changed
    }

    override fun uninstall(view: T) {
        view.changed -= changed
    }

    private val changed: (ProgressIndicator, Double, Double) -> Unit = { it,_,_-> it.rerender() }
}