package io.nacular.doodle.controls.icons

import JsName
import io.mockk.mockk
import io.mockk.verify
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.image.Image
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 3/23/20.
 */
class ImageIconTests {
    @Test @JsName("rendersCorrectly")
    fun `renders correctly`() {
        val image = object: Image {
            override val size        = Size(34)
            override val source      = "foo"
            override val description = "foo"
        }

        val canvas = mockk<Canvas>(relaxed = true)

        val icon = ImageIcon<View>(image)

        listOf(
                mockk<View>(relaxed = true) to Point(6, 19),
                mockk<View>(relaxed = true) to Origin
        ).forEach {

            icon.render(it.first, canvas, it.second)

            verify(exactly = 1) { canvas.image(image = image, destination = Rectangle(position = it.second, size = image.size)) }
        }
    }

    @Test @JsName("sizeMatchesImage")
    fun `size matches image`() {
        listOf(
                Size(34),
                Size(1, 3),
                Empty
        ).forEach {
            ImageIcon<View>(object: Image {
                override val size        = it
                override val source      = "foo"
                override val description = "foo"
            }).apply {
                expect(it) { size(mockk()) }
            }
        }
    }
}