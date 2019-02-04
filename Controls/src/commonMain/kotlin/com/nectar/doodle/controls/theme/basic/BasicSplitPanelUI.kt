package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.panels.SplitPanel
import com.nectar.doodle.controls.theme.AbstractSplitPanelUI
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas

/**
 * Created by Nicholas Eddy on 2/16/18.
 */
class BasicSplitPanelUI: AbstractSplitPanelUI(divider = object: View() {}.apply { width = 7.0 }) {
    override fun render(view: SplitPanel, canvas: Canvas) {}
}