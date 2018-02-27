package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.ProgressIndicator
import com.nectar.doodle.controls.theme.ProgressIndicatorUI
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.units.degrees
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
class CircularProgressUI(private val defaultBackgroundColor: Color, private val darkBackgroundColor: Color): ProgressIndicatorUI<ProgressIndicator>() {
    override fun render(gizmo: ProgressIndicator, canvas: Canvas) {
        val border = 1.0
        val radius = min(gizmo.width, gizmo.height) / 2
        val center = (gizmo.size / 2.0).run { Point(width, height) }
        val brush  = ColorBrush(gizmo.backgroundColor ?: defaultBackgroundColor)

        // Draw background with optional outline
        when {
            radius > 2 -> canvas.circle(Circle(center, radius - border / 2), Pen(darkBackgroundColor, border), brush)
            else       -> canvas.circle(Circle(center, radius),                                                brush)
        }

        val sweep = 360.degrees * gizmo.progress

        canvas.wedge(center = center, radius = radius, sweep = sweep, rotation = 90.degrees - sweep, brush = ColorBrush(darkBackgroundColor))

        // TODO: Make this transparent
        canvas.circle(Circle(center, radius - radius / 4), Pen(darkBackgroundColor, border), ColorBrush(Color.white))
    }
}
