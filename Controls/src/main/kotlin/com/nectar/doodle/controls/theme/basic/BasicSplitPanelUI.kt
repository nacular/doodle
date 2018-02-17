package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.panels.SplitPanel
import com.nectar.doodle.controls.panels.SplitPanelRenderer
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.red
import com.nectar.doodle.drawing.ColorBrush

/**
 * Created by Nicholas Eddy on 2/16/18.
 */
class BasicSplitPanelUI(private val defaultBackgroundColor: Color): SplitPanelRenderer {
    override fun divider(panel: SplitPanel): Gizmo? = object: Gizmo() {
        init {
            width = panel.panelSpacing
        }

        override fun render(canvas: Canvas) {
            canvas.rect(bounds.atOrigin(), ColorBrush(red))
        }
    }

    override fun render(gizmo: SplitPanel, canvas: Canvas) {
//        canvas.rect(gizmo.bounds.atOrigin(), ColorBrush(defaultBackgroundColor))
    }
}