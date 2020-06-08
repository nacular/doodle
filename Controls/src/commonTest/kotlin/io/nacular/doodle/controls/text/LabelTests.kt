@file:Suppress("FunctionName")

package io.nacular.doodle.controls.text

import io.nacular.doodle.JsName
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.text.invoke
import io.nacular.doodle.text.rangeTo
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals


class LabelTests {
    @Test @JsName("setText")
    fun `set text`() {
        Label(createTextMetrics()).let { label ->
            "foo bar some simple text".let {
                label.text = it

                assertEquals(it, label.text)
            }
        }
    }

    @Test @JsName("setStyledText")
    fun `set styled text`() {
        Label(createTextMetrics()).let { label ->
            styledText().let {
                label.styledText = it

                assertEquals(it.text, label.text)
                assertEquals(it, label.styledText)
            }
        }
    }

    @Test @JsName("setsSizeToTextSize")
    fun `sets size to text size`() {
        val textSize = Size(100.0, 345.0)

        Label(createTextMetrics(textSize)).let {
            it.styledText = styledText()

            assertEquals(textSize, it.size)
        }
    }

    @Test @JsName("setsSizeToWrappedTextSize")
    fun `sets size to wrapped text size`() {
        val textSize = Size(100.0, 345.0)
        val wrappedSize = Size(10.0, 1000.0)

        Label(createTextMetrics(textSize, wrappedSize)).let {
            it.styledText = styledText()
            it.wrapsWords = true

            assertEquals(wrappedSize, it.size)
        }
    }

    @Test @JsName("keepsSizeToTextSize")
    fun `keeps size to text size`() {
        val textSize = Size(100.0, 345.0)

        Label(createTextMetrics(textSize)).let {
            it.styledText = styledText()
            it.size       = Empty

            assertEquals(textSize, it.size)
        }
    }

    @Test @JsName("setsWidthToWrappedTextWhenFitting")
    fun `sets width to wrapped text when fitting`() {
        val textSize    = Size(100.0, 345.0)
        val wrappedSize = Size(10.0, 1000.0)

        Label(createTextMetrics(textSize, wrappedSize)).let {
            it.styledText = styledText()
            it.wrapsWords = true
            it.size       = Empty

            assertEquals(wrappedSize, it.size)
        }
    }

    @Test @JsName("setsWidthWhenNotFitting")
    fun `sets width when not fitting`() {
        val textSize    = Size(100.0, 345.0)
        val wrappedSize = Size(10.0, 1000.0)

        Label(createTextMetrics(textSize, wrappedSize)).let {
            it.styledText = styledText()
            it.fitText    = emptySet()
            it.wrapsWords = true
            it.size       = Empty

            assertEquals(Empty, it.size)
        }
    }

    private fun styledText(): StyledText {
        val font = mockk<Font>()

        return "foo bar "..font("some simple").." text"
    }

    private fun createTextMetrics(size: Size = Empty, wrappedSize: Size = Empty) = mockk<TextMetrics>(relaxed = true).apply {
        every { size(any<String>      ()               ) } returns size
        every { width(any<String>     ()               ) } returns size.width
        every { height(any<String>    ()               ) } returns size.height
        every { size(any<String>      (), any<Double>()) } returns wrappedSize
        every { width(any<String>     (), any<Double>()) } returns wrappedSize.width
        every { height(any<String>    (), any<Double>()) } returns wrappedSize.height

        every { size(any<StyledText>  ()                 ) } returns size
        every { width(any<StyledText> ()                 ) } returns size.width
        every { height(any<StyledText>()                 ) } returns size.height
        every { size(any<StyledText>  (), any()          ) } returns wrappedSize
        every { width(any<StyledText> (), any()          ) } returns wrappedSize.width
        every { height(any<StyledText>(), any()          ) } returns wrappedSize.height
    }
}