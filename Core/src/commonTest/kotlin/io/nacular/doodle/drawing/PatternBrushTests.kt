package io.nacular.doodle.drawing

import io.nacular.doodle.JsName
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Color.Companion.Green
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.geometry.times
import com.nectar.measured.units.Angle.Companion.degrees
import com.nectar.measured.units.times
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.expect

// FIXME: Remove once Mockk updates js library to support this function
expect inline fun mockkStatic(vararg classes: String)

/**
 * Created by Nicholas Eddy on 3/21/20.
 */
class PatternBrushTests {
    init {
        mockkStatic("io.nacular.doodle.drawing.PatternBrushKt")
    }

    @Test @JsName("sizeCorrect")
    fun `size correct`() {
        listOf(
            Empty,
            Size(  1    ),
            Size(100, 23)
        ).forEach {
            expect(it) { PatternBrush(size = it) {}.size }
        }
    }

    @Test @JsName("fillCorrect")
    fun `fill correct`() {
        listOf<(Canvas) -> Unit>(
            {                             },
            { it.rect(Rectangle(), Pen()) }
        ).forEach {
            expect(it) { PatternBrush(size = Empty, fill = it).fill }
        }
    }

    @Test @JsName("visibilityCorrect")
    fun `visibility correct`() {
        listOf(
            Empty,
            Size(  1    ),
            Size(100, 23)
        ).forEach {
            expect(!it.empty) { PatternBrush(size = it) {}.visible }
        }
    }

    @Test @JsName("transformCorrect")
    fun `transform correct`() {
        listOf(
                Identity,
                Identity.rotate(30 * degrees)
        ).forEach {
            expect(it) { PatternBrush(size = Empty, transform = it) {}.transform }
        }
    }

    @Test @JsName("stripesEmptyIfColorsTransparent")
    fun `stripes empty if colors transparent`() {
        listOf(
                Red opacity 0f to Transparent,
                null to null
        ).forEach {
            expect(true) { stripedBrush(stripWidth = 10.0, evenRowColor = it.first, oddRowColor = it.second).size.empty }
        }
    }

    @Test @JsName("stripesSizeCorrect")
    fun `stripes size correct`() {
        listOf(20.0, 1.0, 3.4).forEach {
            expect(Size(it, it * 2)) { stripedBrush(stripWidth = it, evenRowColor = Red).size }
        }
    }

    @Test @JsName("stripesRendersCorrectly")
    fun `stripes renders correctly`() {
        data class Attributes(val stripWidth: Double, val evenColor: Color?, val oddColor: Color?, val transform: AffineTransform)

        listOf(
                Attributes(45.6, Red, Green, Identity                     ),
                Attributes( 0.6, Red,  null, Identity.rotate(30 * degrees)),
                Attributes( 0.6, null,  Red, Identity                     ),
                Attributes( 0.0, Red, Green, Identity.rotate(30 * degrees))
        ).forEach { test ->
            val canvas = mockk<Canvas>()

            stripedBrush(stripWidth = test.stripWidth, evenRowColor = test.evenColor, oddRowColor = test.oddColor, transform = test.transform).apply {
                expect(transform) { test.transform }

                canvas.apply(fill)
            }

            test.evenColor?.let {
                verify(exactly = 1) {
                    canvas.rect(Rectangle(width = test.stripWidth, height = test.stripWidth), ColorBrush(it))
                }
            }

            test.oddColor?.let {
                verify(exactly = 1) { canvas.rect(Rectangle(y = test.stripWidth, width = test.stripWidth, height = test.stripWidth), ColorBrush(it)) }
            }
        }
    }

    @Test @JsName("horizontalStripesUsesStripes")
    fun `horizontal stripes uses stripes`() {
        data class Attributes(val rowHeight: Double, val evenColor: Color?, val oddColor: Color?)

        listOf(
                Attributes(45.6, Red, Green),
                Attributes( 0.6, Red,  null),
                Attributes( 0.6, null,  Red),
                Attributes( 0.0, Red, Green)
        ).forEach { test ->
            val canvas = mockk<Canvas>()

            horizontalStripedBrush(rowHeight = test.rowHeight, evenRowColor = test.evenColor, oddRowColor = test.oddColor).apply {
                canvas.apply(fill)
            }

            verify(exactly = 1) { stripedBrush(stripWidth = test.rowHeight, evenRowColor = test.evenColor, oddRowColor = test.oddColor) }
        }
    }

    @Test @JsName("verticalStripesUsesStripes")
    fun `vertical stripes empty if colors transparent`() {
        data class Attributes(val colWidth: Double, val evenColor: Color?, val oddColor: Color?)

        listOf(
                Attributes(45.6, Red, Green),
                Attributes( 0.6, Red,  null),
                Attributes( 0.6, null,  Red),
                Attributes( 0.0, Red, Green)
        ).forEach { test ->
            val canvas = mockk<Canvas>()

            verticalStripedBrush(colWidth = test.colWidth, evenRowColor = test.evenColor, oddRowColor = test.oddColor).apply {
                canvas.apply(fill)
            }

            verify(exactly = 1) {
                stripedBrush(stripWidth = test.colWidth, evenRowColor = test.evenColor, oddRowColor = test.oddColor, transform = Identity.rotate(90 * degrees))
            }
        }
    }

    @Test @JsName("checkerEmptyIfColorsTransparent")
    fun `checker empty if colors transparent`() {
        listOf(
            Red opacity 0f to Transparent,
            null to null
        ).forEach {
            expect(true) { checkerBrush(checkerSize = Size(10.0), firstColor = it.first, secondColor = it.second).size.empty }
        }
    }

    @Test @JsName("checkerSizeCorrect")
    fun `checker size correct`() {
        listOf(Size(20.0, 10.0), Size(1.0), Size(3.4, 0.0), Empty).forEach {
            expect(it * 2) { checkerBrush(checkerSize = it, firstColor = Red).size }
        }
    }

    @Test @JsName("checkerRendersCorrectly")
    fun `checker renders correctly`() {
        data class Attributes(val checkerSize: Size, val firstColor: Color?, val secondColor: Color?)

        listOf(
                Attributes(Size(45.6), Red, Green),
                Attributes(Size( 0.6), Red,  null),
                Attributes(Size( 0.6), null,  Red)//,
//                Attributes(Size( 0.0), red, green)
        ).forEach { test ->
            val canvas = mockk<Canvas>()

            checkerBrush(checkerSize = test.checkerSize, firstColor = test.firstColor, secondColor = test.secondColor).apply {
                canvas.apply(fill)
            }

            test.firstColor?.let {
                verify(exactly = 1) { canvas.rect(Rectangle(size     = test.checkerSize), ColorBrush(it)) }
                verify(exactly = 1) { canvas.rect(Rectangle(position = Point(test.checkerSize.width, test.checkerSize.height), size = test.checkerSize), ColorBrush(it)) }
            }

            test.secondColor?.let {
                verify(exactly = 1) { canvas.rect(Rectangle(position = Point(test.checkerSize.width, 0.0                   ), size = test.checkerSize), ColorBrush(it)) }
                verify(exactly = 1) { canvas.rect(Rectangle(position = Point(0.0,                    test.checkerSize.width), size = test.checkerSize), ColorBrush(it)) }
            }
        }
    }
}