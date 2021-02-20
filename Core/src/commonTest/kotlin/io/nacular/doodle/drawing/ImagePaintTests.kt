package io.nacular.doodle.drawing

import io.mockk.every
import io.mockk.mockk
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.image.Image
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/24/20.
 */
class ImagePaintTests {
    @Test @JsName("visibleWorks")
    fun `visible works`() {
        listOf(
                Triple(image(          ), Size(10, 0), 0f),
                Triple(image(          ), Size(10, 0), 1f),
                Triple(image(          ), Size(10, 1), 0f),
                Triple(image(          ), Size(10, 1), 1f),
                Triple(image(Size(5, 6)), Size(10, 0), 0f),
                Triple(image(Size(5, 6)), Size(10, 0), 1f),
                Triple(image(Size(5, 6)), Size(10, 1), 0f),
                Triple(image(Size(5, 6)), Size(10, 1), 1f)
        ).forEach { (image, size, opacity) ->
            val expected = !image.size.empty && !size.empty && opacity > 0f

            ImagePaint(image, size, opacity).apply {
                expect(expected, "${this}.visible") { visible }
            }
        }
    }

    private fun image(size: Size = Empty): Image = mockk<Image>().apply {
        every { this@apply.size } returns size
    }
}