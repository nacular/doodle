package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.ProgressBar
import com.nectar.doodle.theme.Renderer

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
abstract class ProgressBarUI: Renderer<ProgressBar> {
    override fun install(gizmo: ProgressBar) {
        gizmo.onChanged += this::onChanged
    }

    override fun uninstall(gizmo: ProgressBar) {
        gizmo.onChanged -= this::onChanged
    }

    private fun onChanged(progressBar: ProgressBar) = progressBar.rerender()
}