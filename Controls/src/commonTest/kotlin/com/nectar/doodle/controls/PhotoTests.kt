package com.nectar.doodle.controls

import com.nectar.doodle.JsName
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.image.Image
import io.mockk.mockk
import io.mockk.verify
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