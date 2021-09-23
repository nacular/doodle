package io.nacular.doodle.core

import JsName
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import org.junit.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/17/21.
 */
class DisplayTests {
    @Test @JsName("widthHeightWorks") fun `width + height works`() {
        val size    = Size(100, 97)
        val display = mockk<Display>().apply {
            every { this@apply.size } returns size
        }

        expect(size.width ) { display.width  }
        expect(size.height) { display.height }
    }

    @Test @JsName("fillWorks") fun `fill works`() {
        val display = mockk<Display>()

        display.fill(Red)

        verify (exactly = 1) { display.fill(Red.paint) }
    }
    @Test @JsName("centerWorks") fun `center works`() {
        val size    = Size(100, 97)
        val display = mockk<Display>().apply {
            every { this@apply.size } returns size
        }

        expect(Point(size.width / 2, size.height / 2)) { display.center  }
    }

    @Test @JsName("plusAssignWorks") fun `+= works`() {
        val view    = mockk<View>   ()
        val display = mockk<Display>(relaxed = true)

        display += view

        verify (exactly = 1) { display.children += view }
    }

    @Test @JsName("minusAssignWorks") fun `-= works`() {
        val view    = mockk<View>   ()
        val display = mockk<Display>(relaxed = true)

        display -= view

        verify (exactly = 1) { display.children -= view }
    }

    @Test @JsName("plusAssignIterableWorks") fun `+= iterable works`() {
        val views   = listOf<View>(mockk(), mockk())
        val display = mockk<Display>(relaxed = true)

        display += views

        verify (exactly = 1) { display.children += views }
    }

    @Test @JsName("minusAssignIterableWorks") fun `-= iterable works`() {
        val views   = listOf<View>(mockk(), mockk())
        val display = mockk<Display>(relaxed = true)

        display -= views

        verify (exactly = 1) { display.children -= views }
    }
}