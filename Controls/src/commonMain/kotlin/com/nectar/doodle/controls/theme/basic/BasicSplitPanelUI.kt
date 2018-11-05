package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.theme.AbstractSplitPanelUI
import com.nectar.doodle.controls.panels.SplitPanel
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.ColorBrush

/**
 * Created by Nicholas Eddy on 2/16/18.
 */
private class Resizer(color: Color? = null): View() {
    init {
        backgroundColor = color
    }

    override fun render(canvas: Canvas) {
        backgroundColor?.let {
            canvas.rect(bounds.atOrigin, ColorBrush(it))
        }
    }
}

class BasicSplitPanelUI(darkBackgroundColor: Color): AbstractSplitPanelUI(divider = Resizer(darkBackgroundColor) /*, resizer = Resizer(darkBackgroundColor.with(opacity = 0.2f))*/) {
    override fun render(view: SplitPanel, canvas: Canvas) {
//        canvas.rect(view.bounds.atOrigin(), ColorBrush(defaultBackgroundColor))
    }
}