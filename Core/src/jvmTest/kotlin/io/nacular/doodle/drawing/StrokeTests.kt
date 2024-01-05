package io.nacular.doodle.drawing

import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Green
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.Color.Companion.Transparent
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 3/21/20.
 */
private infix fun DoubleArray.contentEquals2(other: DoubleArray?): Boolean = when(other) {
    null -> false
    else -> this contentEquals other
}

class PenTests {
    @Test fun `defaults correct`() {
        Stroke().apply {
            expect(Black.paint) { fill      }
            expect(1.0        ) { thickness }
            expect(null       ) { dashes    }
            expect(true       ) { visible   }
        }
    }

    @Test fun `handles dash var args`() {
        Stroke(dash = 1.0, remainingDashes = doubleArrayOf(2.0, 3.0, 4.0)).apply {
            expect(true) { doubleArrayOf(1.0,2.0,3.0,4.0) contentEquals2 dashes }
        }
    }

    @Test fun `visibility correct`() {
        listOf(
                Stroke(                            ) to true,
                Stroke(color     = Red             ) to true,
                Stroke(color     = Transparent     ) to false,
                Stroke(color     = Green opacity 0f) to false,
                Stroke(thickness = 0.0             ) to false
        ).forEach {
            expect(it.second) { it.first.visible }
        }
    }
}