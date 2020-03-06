package com.nectar.doodle.themes.basic

import com.nectar.doodle.controls.spinner.Model
import com.nectar.doodle.controls.spinner.Spinner
import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.controls.theme.CommonSpinnerBehavior
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.layout.Insets

/**
 * Created by Nicholas Eddy on 3/18/18.
 */
class BasicSpinnerBehavior(            labelFactory   : LabelFactory,
                           private val borderColor    : Color,
                           private val backgroundColor: Color): CommonSpinnerBehavior(Insets(1.0), labelFactory) {
    override fun render(view: Spinner<Any, Model<Any>>, canvas: Canvas) {
        val penWidth = 1.0

        canvas.rect(view.bounds.atOrigin.inset(penWidth / 2), Pen(borderColor, penWidth), ColorBrush(backgroundColor))
    }
}