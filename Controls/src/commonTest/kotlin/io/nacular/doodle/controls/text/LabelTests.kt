@file:Suppress("FunctionName")

package io.nacular.doodle.controls.text

import io.mockk.every
import io.mockk.mockk
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.text.invoke
import io.nacular.doodle.text.rangeTo
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.expect


class LabelTests {
    @Test @JsName("setText")
    fun `set text`() {
        Label().let { label ->
            "foo bar some simple text".let {
                label.text = it

                assertEquals(it, label.text)
            }
        }
    }

    @Test @JsName("setStyledText")
    fun `set styled text`() {
        Label().let { label ->
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

        Label().let {
            it.behavior   = createBehavior(textSize)
            it.styledText = styledText()

            assertEquals(textSize, it.size)
        }
    }

    @Test @JsName("setsSizeToWrappedTextSize")
    fun `sets size to wrapped text size`() {
        val wrappedSize = Size(10.0, 1000.0)

        Label().let {
            it.behavior   = createBehavior(wrappedSize)
            it.styledText = styledText()
            it.wrapsWords = true

            assertEquals(wrappedSize, it.size)
        }
    }

    @Test @JsName("keepsSizeToTextSize")
    fun `keeps size to text size`() {
        val textSize = Size(100.0, 345.0)

        Label().let {
            it.behavior   = createBehavior(textSize)
            it.styledText = styledText()
            it.size       = Empty

            assertEquals(textSize, it.size)
        }
    }

    @Test @JsName("setsWidthToWrappedTextWhenFitting")
    fun `sets width to wrapped text when fitting`() {
        val wrappedSize = Size(10.0, 1000.0)

        Label().let {
            it.behavior   = createBehavior(wrappedSize)
            it.styledText = styledText()
            it.wrapsWords = true
            it.size       = Empty

            assertEquals(wrappedSize, it.size)
        }
    }

    @Test @JsName("setsWidthWhenNotFitting")
    fun `sets width when not fitting`() {
        val wrappedSize = Size(10.0, 1000.0)

        Label().let {
            it.behavior   = createBehavior(wrappedSize)
            it.styledText = styledText()
            it.fitText    = emptySet()
            it.wrapsWords = true
            it.size       = Empty

            assertEquals(Empty, it.size)
        }
    }

    @Test @JsName("foregroundColorFillsStyledTextMissingColor")
    fun `foreground color fills in styled text missing color`() {
        Label().apply {
            val rawStyledText = "blank ".. Red ("red") .. " blank"

            styledText = rawStyledText

            listOf(Red, Blue).forEach {
                foregroundColor = it

                expect(it { rawStyledText }) { styledText }
            }

            foregroundColor = null

            expect(rawStyledText) { styledText }
        }
    }

    @Test @JsName("fontFillsStyledTextMissingFont")
    fun `font fills in styled text missing font`() {
        Label().apply {
            val font1         = mockk<Font>()
            val font2         = mockk<Font>()
            val font3         = mockk<Font>()
            val rawStyledText = "blank ".. font1 ("font1") .. " blank"

            styledText = rawStyledText

            listOf(font2, font3).forEach {
                font = it

                expect(it { rawStyledText }) { styledText }
            }

            font = null

            expect(rawStyledText) { styledText }
        }
    }

    private fun styledText(): StyledText {
        val font = mockk<Font>()

        return "foo bar "..font("some simple").." text"
    }

    private fun createBehavior(size: Size = Empty) = mockk<LabelBehavior>(relaxed = true).apply {
        every { measureText(any()) } returns size
    }
}