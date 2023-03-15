package io.nacular.doodle.examples.contacts

import io.nacular.doodle.core.View
import io.nacular.doodle.core.renderProperty
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.text
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import kotlin.math.min

/**
 * Renders the avatar icon used for a contact
 */
open class Avatar(private val textMetrics: TextMetrics, name: String): View() {
    var name by renderProperty(name)

    override fun render(canvas: Canvas) {
        val circleColor  = name.toColor()
        val firstInitial = "${name.first()}"
        val textSize     = textMetrics.size(firstInitial, font)

        canvas.circle(Circle(radius = min(width, height) / 2, center = Point(width / 2, height / 2)), fill = name.toColor().paint)
        canvas.scale(around = Point(width / 2, height / 2), 4.0, 4.0) {
            text(
                firstInitial,
                at    = Point((width - textSize.width) / 2, (height - textSize.height) / 2),
                color = Color.blackOrWhiteContrast(circleColor),
                font  = font
            )
        }
    }
}