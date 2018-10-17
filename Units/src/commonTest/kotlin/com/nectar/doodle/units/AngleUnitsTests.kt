package com.nectar.doodle.units

import com.nectar.doodle.JsName
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 2/22/18.
 */
class AngleUnitsTests {

    @Test @JsName("combinationsWork")
    fun `combinations work`() {
        testUnit(mapOf(
            radians to mapOf(1.0    to radians, 180/PI to degrees),
            degrees to mapOf(PI/180 to radians, 1.0    to degrees)
        ))
    }

    @Test @JsName("radiansConstants")
    fun `radians constants`() {
        expect(10   * radians) { 10.radians   }
        expect(10.0 * radians) { 10.0.radians }
        expect(10f  * radians) { 10f.radians  }
        expect(10L  * radians) { 10L.radians  }
    }

    @Test @JsName("degreesConstants")
    fun `degrees constants`() {
        expect(10   * degrees) { 10.degrees   }
        expect(10.0 * degrees) { 10.0.degrees }
        expect(10f  * degrees) { 10f.degrees  }
        expect(10L  * degrees) { 10L.degrees  }
    }

    @Test @JsName("mathWorks")
    fun `math works`() {
        val angle1 = 79.degrees
        val angle2 = 19.degrees

        expect(kotlin.math.sin  (angle1.`in`(radians))) { sin  (angle1)                               }
        expect(kotlin.math.cos  (angle1.`in`(radians))) { cos  (angle1)                               }
        expect(kotlin.math.tan  (angle1.`in`(radians))) { tan  (angle1)                               }
        expect(kotlin.math.asin (angle1.`in`(radians))) { asin (angle1)                               }
        expect(kotlin.math.acos (angle1.`in`(radians))) { acos (angle1)                               }
        expect(kotlin.math.atan (angle1.`in`(radians))) { atan (angle1)                               }
        expect(kotlin.math.atan2(angle1.`in`(radians) ,angle2.`in`(radians))) { atan2(angle1, angle2) }
        expect(kotlin.math.sinh (angle1.`in`(radians))) { sinh (angle1)                               }
        expect(kotlin.math.cosh (angle1.`in`(radians))) { cosh (angle1)                               }
        expect(kotlin.math.tanh (angle1.`in`(radians))) { tanh (angle1)                               }
        expect(kotlin.math.asinh(angle1.`in`(radians))) { asinh(angle1)                               }
        expect(kotlin.math.acosh(angle1.`in`(radians))) { acosh(angle1)                               }
        expect(kotlin.math.atanh(angle1.`in`(radians))) { atanh(angle1)                               }
    }
}