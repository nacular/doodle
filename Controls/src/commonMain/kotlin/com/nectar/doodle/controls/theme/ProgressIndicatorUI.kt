package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.ProgressIndicator
import com.nectar.doodle.theme.Renderer

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
abstract class ProgressIndicatorUI<in T: ProgressIndicator>: Renderer<T> {
    override fun install(gizmo: T) {
        gizmo.changed += changed
    }

    override fun uninstall(gizmo: T) {
        gizmo.changed -= changed
    }

    private val changed: (indicator: ProgressIndicator) -> Unit = { it.rerender() }
}