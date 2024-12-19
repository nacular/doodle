package io.nacular.doodle.geometry

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/22/20.
 */
class EllipseTests {
    @Test
    fun `defaults to origin`() = expect(Point.Origin) { Ellipse(xRadius = 2.4, yRadius = 4.0).center }

    private fun Ellipse.equal(other: Ellipse) = center == other.center && xRadius == other.xRadius && yRadius == other.yRadius

    @Test
    fun `negative radius works`() {
        assertTrue { Ellipse(xRadius = -20.0, yRadius =   1.0).equal(Ellipse(0.0, 1.0)) }
        assertTrue { Ellipse(xRadius =   1.0, yRadius = -20.0).equal(Ellipse(1.0, 0.0)) }
        assertTrue { Ellipse(xRadius =  -1.0, yRadius = -20.0).equal(Ellipse.Empty    ) }
    }

    @Test
    fun `empty has area 0`() = expect(0.0) { Ellipse.Empty.area }

    @Test
    fun `zero xRadius has area 0`() {
        Ellipse(xRadius = 0.0, yRadius = 100.0).apply {
            expect(0.0) { area }
            expect(true) { empty }
        }
    }

    @Test
    fun `zero yRadius has area 0`() {
        Ellipse(xRadius = 100.0, yRadius = 0.0).apply {
            expect(0.0) { area }
            expect(true) { empty }
        }
    }

    @Test
    fun `area works`() = listOf(10 by 3, 0 by 56, 5 by 5, 0 by 0).forEach {
        val area = PI * it.xRadius * it.yRadius

        expect(area) { it.area }
        expect(area == 0.0) { it.empty }
    }

    @Test
    fun `unit works`() {
        Ellipse.Unit.apply {
            expect(1.0) { xRadius }
            expect(1.0) { yRadius }
        }
    }

    @Test
    fun `bounding rect works`() {
        val ellipse = Ellipse(center = Point(10, 10), xRadius = 4.0, yRadius = 2.0)

        expect(Rectangle(6, 8, 8, 4)) { ellipse.boundingRectangle }
    }

    private infix fun Number.by(that: Number) = Ellipse(xRadius = this.toDouble(), yRadius = that.toDouble())
}