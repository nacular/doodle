package io.nacular.doodle.image.impl

import JsName
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.nacular.doodle.HTMLImageElement
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HtmlFactory
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 4/19/21.
 */
public class ImageLoaderImplTests {
    @Test @JsName("loadsValidImage") fun `loads valid image`() {
        val onload = slot<(Event) -> Any>()

        val imageElement = mockk<HTMLImageElement>().apply {
            every { this@apply.onload = capture(onload) } answers {
                onload.captured(mockk()) // trigger load
            }
        }

        val htmlFactory = mockk<HtmlFactory>().apply {
            every { createImage(any()) } returns imageElement
        }

        val loader = ImageLoaderImpl(htmlFactory, mockk())

        expect(imageElement) {
            runBlocking { (loader.load("foo") as ImageImpl).image }
        }
    }

    @Test @JsName("nullOnError") fun `returns null on error`() {
        val onerror = slot<(Any, String, Int, Int, Any?) -> Any>()

        val imageElement = mockk<HTMLImageElement>().apply {
            every { this@apply.onerror = capture(onerror) } answers {
                onerror.captured(mockk(), "", 0, 0, mockk()) // trigger error
            }
        }

        val htmlFactory = mockk<HtmlFactory>().apply {
            every { createImage(any()) } returns imageElement
        }

        val loader = ImageLoaderImpl(htmlFactory, mockk())

        expect(null) {
            runBlocking { loader.load("foo") }
        }
    }
}