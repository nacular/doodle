package io.nacular.doodle.drawing

import io.nacular.doodle.core.center
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Vector3d
import io.nacular.measured.units.Angle.Companion.cos
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Angle.Companion.sin
import io.nacular.measured.units.times
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/13/20.
 */
class AffineTransformTests {
    @Test @JsName("defaults")
    fun `defaults valid`() {
        val transform = AffineTransform()

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

    @Test @JsName("operationsInvertible")
    fun `operations invertible`() {
        testInversion { it.translate       (x = 10.0, y = 5.5) }
        testInversion { it.rotate          (45  * degrees    ) }
        testInversion { it.rotate          (180 * degrees    ) }
        testInversion { it.flipHorizontally(                 ) }
        testInversion { it.rotateX         (45 * degrees     ) }
        testInversion { it.rotateY         (57 * degrees     ) }
    }

    @Test @JsName("pointTransformsWork")
    fun `point transforms work`() {
        listOf(
                Point(1, 1) to Vector3d( 1, 1, 0) to Identity,
                Point(1, 1) to Vector3d( 2, 1, 1) to Identity.scale           (x = 2.0),
                Point(1, 1) to Vector3d( 1,-1, 1) to Identity.flipVertically  (       ),
                Point(1, 1) to Vector3d(-1, 1, 1) to Identity.flipHorizontally(       ),
        ).forEach { (points, transform) ->
            expect(points.second) {
                transform(points.first)
            }
        }
    }

    @Test @JsName("rotationsWork")
    fun `rotations work`() {
        val point = Point(0, 1)
        val angle = 90 * degrees
        val cos   = cos(angle)
        val sin   = sin(angle)

        expect(point.run { Vector3d(x * cos - y * sin, x * sin + y * cos, 1.0) }) {
            Identity.rotate(angle)(point)
        }
    }

    @Test @JsName("equalsWorks")
    fun `equals works`() {
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

    @Test
    fun foo() {
//        val c = cos(45 * degrees)
//        val s = sin(45 * degrees)
//
//        val matrix = AffineMatrix3D(
//            1.0, 0.0, 0.0, 50.0,
//            0.0, 1.0, 0.0, 50.0,
//            0.0, 0.0, 1.0,  0.0
//        ) * AffineMatrix3D(
//              c, 0.0,   s, 0.0,
//            0.0, 1.0, 0.0, 0.0,
//             -s, 0.0,   c, 0.0,
//        ) * AffineMatrix3D(
//            1.0, 0.0, 0.0, 50.0,
//            0.0, 1.0, 0.0, 50.0,
//            0.0, 0.0, 1.0,  0.0
//        ).inverse!!
//
//        val inverse = matrix.inverse
//
        val view = view { bounds = Rectangle(100, 100) }

        view.transform = Identity.rotateY(around = view.center, 45 * degrees)

        expect(false, "${Point(100, 0)} in $view") {
            Point(100, 0) in view
        }

        expect(true, "${Point(40, 0)} in $view") {
            Point(40, 0) in view
        }

//        val transform = Identity.rotateY(around = Point(50, 50), 45 * degrees)
//        val rect      = transform(Rectangle(100, 100).points.map { Point3d(it.x, it.y, 1.0) })
////        val point     = transform.inverse!!.invoke(Point(100, 0))
//
//        val plane = Plane(rect[0], (rect[1] - rect[0] cross rect[2] - rect[1]))
//        val ray   = Ray  (Point3d(100.0, 0.0, 0.0), Point3d(0.0, 0.0, -1.0))
//
//        val intersection = plane intersection ray
//
//        val transformedPoint = intersection?.let { transform.inverse!!.invoke(it) }
//
//        val check     = transformedPoint!!.point2d in Rectangle(100, 100)
//
//        println(check)

//        val m2 = MatrixImpl(arrayOf(
//            arrayOf(1.0,0.0,0.0,0.0),
//            arrayOf(0.0,1.0,0.0,0.0),
//            arrayOf(0.0,0.0,0.0,1.0),
//        )) * transform.matrix
//
//        val transform2 = AffineTransform(
//            m2[0,0], m2[0,1], m2[0,2], m2[0,3],
//            m2[1,0], m2[1,1], m2[1,2], m2[1,3],
//            m2[2,0], m2[2,1], m2[2,2], m2[2,3],
//        )
//
//        println(transform.matrix)
//        println("================")
//        println(m2)
//        println("================")
//        println(transform2)

//        val point     = transform2.inverse!!.invoke(Point(100, 0))
//        val check     = point in rect

//        println("check: $check")
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