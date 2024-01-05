package io.nacular.doodle.drawing

import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Vector3D
import io.nacular.measured.units.Angle.Companion.cos
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Angle.Companion.sin
import io.nacular.measured.units.times
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/13/20.
 */
class AffineTransformTests {
    @Test fun `defaults valid`() {
        val transform = AffineTransform2D()

        transform.apply {
            expect(true) { isIdentity }
            expect(1.0 ) { scaleX     }
            expect(0.0 ) { shearX     }
            expect(0.0 ) { translateX }
            expect(1.0 ) { scaleY     }
            expect(0.0 ) { shearY     }
            expect(0.0 ) { translateY }
        }
    }

    @Test fun `operations invertible`() {
        testInversion { it.translate       (x = 10.0, y = 5.5) }
        testInversion { it.rotate          (45  * degrees    ) }
        testInversion { it.rotate          (180 * degrees    ) }
        testInversion { it.flipHorizontally(                 ) }
        testInversion { it.rotateX         (45 * degrees     ) }
        testInversion { it.rotateY         (57 * degrees     ) }
    }

    @Test fun `point transforms work`() {
        listOf(
                Point(1, 1) to Vector3D( 1, 1, 0) to Identity,
                Point(1, 1) to Vector3D( 2, 1, 1) to Identity.scale           (x = 2.0),
                Point(1, 1) to Vector3D( 1,-1, 1) to Identity.flipVertically  (       ),
                Point(1, 1) to Vector3D(-1, 1, 1) to Identity.flipHorizontally(       ),
        ).forEach { (points, transform) ->
            expect(points.second) {
                transform(points.first)
            }
        }
    }

    @Test fun `rotations work`() {
        val point = Point(0, 1)
        val angle = 90 * degrees
        val cos   = cos(angle)
        val sin   = sin(angle)

        expect(point.run { Vector3D(x * cos - y * sin, x * sin + y * cos, 1.0) }) {
            Identity.rotate(angle)(point)
        }
    }

    @Test fun `equals works`() {
        listOf(
            Identity to AffineTransform(1.0, 0.0, 0.0, 0.0,
                                        0.0, 1.0, 0.0, 0.0,
                                        0.0, 0.0, 1.0, 0.0) to true,
        ).forEach { (transforms, expectation) ->
            expect(expectation) {
                transforms.first == transforms.second
            }
        }
    }

    @Test fun foo() {
        expect(Vector3D(5, 5, 3)) { Vector3D(10, 10, 6) / 2 }
    }

    private fun testInversion(block: (AffineTransform) -> AffineTransform) {
        expect(Identity) {
            val transform = block(Identity)

            transform.inverse?.let {
                transform * it
            }
        }
    }
}