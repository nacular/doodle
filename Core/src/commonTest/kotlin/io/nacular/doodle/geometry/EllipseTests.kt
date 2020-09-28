package io.nacular.doodle.geometry

import io.nacular.doodle.geometry.Ellipse.Companion.Empty
import io.nacular.doodle.geometry.Ellipse.Companion.Unit
import io.nacular.doodle.geometry.Point.Companion.Origin
import kotlin.js.JsName
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/22/20.
 */
class EllipseTests {
    @Test @JsName("defaultsToOrigin")
    fun `defaults to origin`() = expect(Origin) { Ellipse(xRadius = 2.4, yRadius = 4.0).center }

    @Test @JsName("negativeRadiusFails")
    fun `negative radius fails`() {
        assertFailsWith(IllegalArgumentException::class) { Ellipse(xRadius = -20.0, yRadius =   1.0) }
        assertFailsWith(IllegalArgumentException::class) { Ellipse(xRadius =   1.0, yRadius = -20.0) }
    }

    @Test @JsName("emptyHasSizeZero")
    fun `empty has area 0`() = expect(0.0) { Empty.area }

    @Test @JsName("zeroXRadiusHasSizeZero")
    fun `zero xradius has area 0`() {
        Ellipse(xRadius = 0.0, yRadius = 100.0).apply {
            expect(0.0 ) { area  }
            expect(true) { empty }
        }
    }

    @Test @JsName("zeroYRadiusHasSizeZero")
    fun `zero yradius has area 0`() {
        Ellipse(xRadius = 100.0, yRadius = 0.0).apply {
            expect(0.0 ) { area  }
            expect(true) { empty }
        }
    }

    @Test @JsName("areaWorks")
    fun `area works`() = listOf(10 by 3, 0 by 56, 5 by 5, 0 by 0).forEach {
        val area = PI * it.xRadius * it.yRadius

        expect(area       ) { it.area  }
        expect(area == 0.0) { it.empty }
    }

    @Test @JsName("unitWorks")
    fun `unit works`() {
        Unit.apply {
            expect(1.0) { xRadius }
            expect(1.0) { yRadius }
        }
    }

    @Test @JsName("boundingRectWorks")
    fun `bounding rect works`() {
        val ellipse = Ellipse(center = Point(10, 10), xRadius = 4.0, yRadius = 2.0)

        expect(Rectangle(6, 8, 8, 4)) { ellipse.boundingRectangle }
    }

    private infix fun Number.by(that: Number) = Ellipse(xRadius = this.toDouble(), yRadius = that.toDouble())
}