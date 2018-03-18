package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.spinner.Spinner
import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.controls.theme.CommonSpinnerUI
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.layout.Insets

/**
 * Created by Nicholas Eddy on 3/18/18.
 */
class BasicSpinnerUI(labelFactory  : LabelFactory,
        private val borderColor    : Color,
        private val backgroundColor: Color): CommonSpinnerUI(Insets(1.0), labelFactory) {
    override fun render(gizmo: Spinner<Any>, canvas: Canvas) {
        canvas.rect(gizmo.bounds.atOrigin, Pen(borderColor), ColorBrush(backgroundColor))
    }
}