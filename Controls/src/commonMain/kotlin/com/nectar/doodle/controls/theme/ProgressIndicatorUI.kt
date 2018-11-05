package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.ProgressIndicator
import com.nectar.doodle.theme.Renderer

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
abstract class ProgressIndicatorUI<in T: ProgressIndicator>: Renderer<T> {
    override fun install(view: T) {
        view.changed += changed
    }

    override fun uninstall(view: T) {
        view.changed -= changed
    }

    private val changed: (indicator: ProgressIndicator) -> Unit = { it.rerender() }
}