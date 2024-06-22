package io.nacular.doodle.geometry

import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 4/30/24.
 */
class PolygonTests {
    @Test
    fun `bounding box is rect`() {
        val rect = Rectangle(10.0, 45.0, 67.8, 1809.3)

        expect(rect) { rect.boundingRectangle }
    }

    @Test
    fun `bounding box works for star`() {
        val radius = 10.0
        val circle = Circle(Point(radius, radius), radius)
        val star   = star(circle, points = 5, innerCircle = circle.inset(radius * 0.5))!!

        val minX = star.points.minBy { it.x }.x
        val minY = star.points.minBy { it.y }.y
        val maxX = star.points.maxBy { it.x }.x
        val maxY = star.points.maxBy { it.y }.y

        expect(Rectangle(minX, minY, (maxX - minX), (maxY - minY))) { star.boundingRectangle }
    }
}