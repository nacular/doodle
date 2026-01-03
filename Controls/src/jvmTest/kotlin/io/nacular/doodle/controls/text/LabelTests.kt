@file:Suppress("FunctionName")

package io.nacular.doodle.controls.text

import JsName
import io.mockk.every
import io.mockk.mockk
import io.nacular.doodle.core.forceSize
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.text.AnnotatedString
import io.nacular.doodle.text.invoke
import io.nacular.doodle.text.rangeTo
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
            annotatedString().let {
                label.annotatedText = it

                assertEquals(it.text, label.text)
                assertEquals(it, label.annotatedText)
            }
        }
    }

    @Test @JsName("setsSizeToTextSize")
    fun `sets ideal size to text size`() {
        val textSize = Size(100.0, 345.0)

        Label().let {
            it.behavior   = createBehavior(textSize)
            it.annotatedText = annotatedString()

            assertEquals(textSize, it.idealSize)
        }
    }

    @Test @JsName("setsSizeToWrappedTextSize")
    fun `sets ideal size to wrapped text size`() {
        val wrappedSize = Size(10.0, 1000.0)

        Label().let {
            it.behavior   = createBehavior(wrappedSize)
            it.annotatedText = annotatedString()
            it.wrapsWords = true

            assertEquals(wrappedSize, it.idealSize)
        }
    }

    @Test @JsName("keepsSizeToTextSize")
    fun `keeps ideal size to text size`() {
        val textSize = Size(100.0, 345.0)

        Label().let {
            it.behavior   = createBehavior(textSize)
            it.annotatedText = annotatedString()
            it.suggestSize(Empty)

            assertEquals(textSize, it.idealSize)
        }
    }

    @Test @JsName("setsWidthToWrappedTextWhenFitting")
    fun `sets width to wrapped text when fitting`() {
        val wrappedSize = Size(10.0, 1000.0)

        Label().let {
            it.behavior   = createBehavior(wrappedSize)
            it.annotatedText = annotatedString()
            it.wrapsWords = true
            it.suggestSize(Empty)

            assertEquals(wrappedSize, it.idealSize)
        }
    }

    @Test @JsName("setsWidthWhenNotFitting")
    fun `sets width when not fitting`() {
        val wrappedSize = Size(10.0, 1000.0)

        Label().let {
            it.behavior   = createBehavior(wrappedSize)
            it.annotatedText = annotatedString()
            it.wrapsWords = true
            it.forceSize(Empty)

            assertEquals(Empty, it.size)
        }
    }

    @Test @JsName("foregroundColorFillsStyledTextMissingColor")
    fun `foreground color fills in styled text missing color`() {
        Label().apply {
            val rawStyledText = "blank ".. Red { "red" } .. " blank"

            annotatedText = rawStyledText

            listOf(Red, Blue).forEach {
                foregroundColor = it

                expect(it { rawStyledText }) { annotatedText }
            }

            foregroundColor = null

            expect(rawStyledText) { annotatedText }
        }
    }

    @Test @JsName("fontFillsStyledTextMissingFont")
    fun `font fills in styled text missing font`() {
        Label().apply {
            val font1         = mockk<Font>()
            val font2         = mockk<Font>()
            val font3         = mockk<Font>()
            val rawStyledText = "blank ".. font1 { "font1" } .. " blank"

            annotatedText = rawStyledText

            listOf(font2, font3).forEach {
                font = it

                expect(it { rawStyledText }) { annotatedText }
            }

            font = null

            expect(rawStyledText) { annotatedText }
        }
    }

    private fun annotatedString(): AnnotatedString {
        val font = mockk<Font>()

        return "foo bar "..font {"some simple" }.." text"
    }

    private fun createBehavior(size: Size = Empty) = mockk<LabelBehavior>(relaxed = true).apply {
        every { measureText(any()) } returns size
    }
}