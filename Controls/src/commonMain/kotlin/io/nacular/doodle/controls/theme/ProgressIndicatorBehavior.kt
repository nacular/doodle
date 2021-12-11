package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.ProgressIndicator
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
public abstract class ProgressIndicatorBehavior<in T: ProgressIndicator>: Behavior<T> {
    override fun install(view: T) {
        view.changed        += changed
        view.enabledChanged += enabledChanged
    }

    override fun uninstall(view: T) {
        view.changed        -= changed
        view.enabledChanged -= enabledChanged
    }

    private val changed       : (ProgressIndicator, Double,  Double ) -> Unit = { it,_,_-> it.rerender() }
    private val enabledChanged: (View,              Boolean, Boolean) -> Unit = { it,_,_-> it.rerender() }
}