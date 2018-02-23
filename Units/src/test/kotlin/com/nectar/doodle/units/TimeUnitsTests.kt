package com.nectar.doodle.units

import com.nectar.doodle.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 2/22/18.
 */
class TimeUnitsTests {
//      ms      s  min hr
//  ms  1  1/1000
//   s
// min
//  hr

    @Test @JsName("combinationsWork")
    fun `combinations work`() {
        testUnit(mapOf(
            milliseconds to mapOf(1.0        to milliseconds, 1.0/1000 to seconds, 1.0/60_000 to minutes, 1.0/3600_000 to hours),
            seconds      to mapOf(1000.0     to milliseconds, 1.0      to seconds, 1.0/60     to minutes, 1.0/3600     to hours),
            minutes      to mapOf(60_000.0   to milliseconds, 60.0     to seconds, 1.0        to minutes, 1.0/60       to hours),
            hours        to mapOf(3600_000.0 to milliseconds, 3600.0   to seconds, 60.0       to minutes, 1.0          to hours)
        ))
    }

    @Test @JsName("millisecondsConstants")
    fun `milliseconds constants`() {
        expect(10   * milliseconds) { 10.milliseconds   }
        expect(10.0 * milliseconds) { 10.0.milliseconds }
        expect(10f  * milliseconds) { 10f.milliseconds  }
        expect(10L  * milliseconds) { 10L.milliseconds  }
    }

    @Test @JsName("secondsConstants")
    fun `seconds constants`() {
        expect(10   * seconds) { 10.seconds   }
        expect(10.0 * seconds) { 10.0.seconds }
        expect(10f  * seconds) { 10f.seconds  }
        expect(10L  * seconds) { 10L.seconds  }
    }

    @Test @JsName("minutesConstants")
    fun `minutes constants`() {
        expect(10   * minutes) { 10.minutes   }
        expect(10.0 * minutes) { 10.0.minutes }
        expect(10f  * minutes) { 10f.minutes  }
        expect(10L  * minutes) { 10L.minutes  }
    }

    @Test @JsName("hoursConstants")
    fun `hours constants`() {
        expect(10   * hours) { 10.hours   }
        expect(10.0 * hours) { 10.0.hours }
        expect(10f  * hours) { 10f.hours  }
        expect(10L  * hours) { 10L.hours  }
    }
}