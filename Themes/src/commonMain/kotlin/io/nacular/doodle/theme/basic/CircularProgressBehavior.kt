package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.ProgressIndicator
import io.nacular.doodle.controls.theme.ProgressIndicatorBehavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.div
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.times
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
@Deprecated(message = "Switch to BasicCircularProgressIndicatorBehavior", replaceWith = ReplaceWith("BasicCircularProgressIndicatorBehavior", imports = arrayOf("io.nacular.doodle.theme.basic.BasicCircularProgressIndicatorBehavior")))
public class CircularProgressBehavior(private val defaultBackgroundColor: Color, private val darkBackgroundColor: Color): ProgressIndicatorBehavior<ProgressIndicator>() {
    override fun render(view: ProgressIndicator, canvas: Canvas) {
        val border = 1.0
        val radius = min(view.width, view.height) / 2
        val center = (view.size / 2.0).run { Point(width, height) }
        val fill  = ColorPaint(view.backgroundColor ?: defaultBackgroundColor)

        // Draw background with optional outline
        when {
            radius > 2 -> canvas.circle(Circle(center, radius - border / 2), Stroke(darkBackgroundColor, border), fill)
            else       -> canvas.circle(Circle(center, radius),                                                fill)
        }

        val sweep = 360 * degrees * view.progress

        canvas.wedge(center = center, radius = radius, sweep = sweep, rotation = 90 * degrees - sweep, fill = ColorPaint(darkBackgroundColor))

        // TODO: Make this transparent
        canvas.circle(Circle(center, radius - radius / 4), Stroke(darkBackgroundColor, border), ColorPaint(Color.White))
    }
}
