package io.nacular.doodle.controls

import io.mockk.mockk
import io.mockk.verify
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import kotlin.js.JsName
import kotlin.test.Test

/**
 * Created by Nicholas Eddy on 3/21/20.
 */
class PhotoTests {
    @Test @JsName("rendersCorrectly")
    fun `renders correctly`() {
        val image = object: Image {
            override val size   = Size(34)
            override val source = "foo"
        }

        val canvas = mockk<Canvas>(relaxed = true)

        val photo = Photo(image)

        photo.render(canvas)

        verify(exactly = 1) { canvas.image(image = image, source = Rectangle(size = image.size), destination = photo.bounds.atOrigin) }

        photo.size = Size(49, 56)

        photo.render(canvas)

        verify(exactly = 1) { canvas.image(image = image, source = Rectangle(size = image.size), destination = photo.bounds.atOrigin) }
    }
}