package com.nectar.doodle.drawing

import com.nectar.doodle.JsName
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.Color.Companion.red
import com.nectar.doodle.drawing.Color.Companion.transparent
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.nectar.doodle.geometry.times
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 3/21/20.
 */
class PatternBrushTests {
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

    @Test @JsName("horizontalStripesEmptyIfColorsTransparent")
    fun `horizontal stripes empty if colors transparent`() {
        listOf(
            red opacity 0f to transparent,
            null to null
        ).forEach {
            expect(true) { horizontalStripedBrush(rowHeight = 10.0, evenRowColor = it.first, oddRowColor = it.second).size.empty }
        }
    }

    @Test @JsName("horizontalStripesSizeCorrect")
    fun `horizontal stripes size correct`() {
        listOf(20.0, 1.0, 3.4).forEach {
            expect(Size(it, it * 2)) { horizontalStripedBrush(rowHeight = it, evenRowColor = red).size }
        }
    }

    @Test @JsName("horizontalStripesRendersCorrectly")
    fun `horizontal stripes renders correctly`() {
        data class Attributes(val rowHeight: Double, val evenColor: Color?, val oddColor: Color?)

        listOf(
                Attributes(45.6, red, green),
                Attributes( 0.6, red,  null),
                Attributes( 0.6, null,  red),
                Attributes( 0.0, red, green)
        ).forEach { test ->
            val canvas = mockk<Canvas>(relaxed = true)

            horizontalStripedBrush(rowHeight = test.rowHeight, evenRowColor = test.evenColor, oddRowColor = test.oddColor).apply {
                canvas.apply(fill)
            }

            test.evenColor?.let {
                verify(exactly = 1) { canvas.rect(Rectangle(width = test.rowHeight, height = test.rowHeight), ColorBrush(it)) }
            }

            test.oddColor?.let {
                verify(exactly = 1) { canvas.rect(Rectangle(y = test.rowHeight, width = test.rowHeight, height = test.rowHeight), ColorBrush(it)) }
            }
        }
    }

    @Test @JsName("verticalStripesEmptyIfColorsTransparent")
    fun `vertical stripes empty if colors transparent`() {
        listOf(
            red opacity 0f to transparent,
            null to null
        ).forEach {
            expect(true) { verticalStripedBrush(colWidth = 10.0, evenRowColor = it.first, oddRowColor = it.second).size.empty }
        }
    }

    @Test @JsName("verticalStripesSizeCorrect")
    fun `vertical stripes size correct`() {
        listOf(20.0, 1.0, 3.4).forEach {
            expect(Size(it * 2, it)) { verticalStripedBrush(colWidth = it, evenRowColor = red).size }
        }
    }

    @Test @JsName("verticalStripesRendersCorrectly")
    fun `vertical stripes renders correctly`() {
        data class Attributes(val colWidth: Double, val evenColor: Color?, val oddColor: Color?)

        listOf(
                Attributes(45.6, red, green),
                Attributes( 0.6, red,  null),
                Attributes( 0.6, null,  red),
                Attributes( 0.0, red, green)
        ).forEach { test ->
            val canvas = mockk<Canvas>(relaxed = true)

            verticalStripedBrush(colWidth = test.colWidth, evenRowColor = test.evenColor, oddRowColor = test.oddColor).apply {
                canvas.apply(fill)
            }

            test.evenColor?.let {
                verify(exactly = 1) { canvas.rect(Rectangle(width = test.colWidth, height = test.colWidth), ColorBrush(it)) }
            }

            test.oddColor?.let {
                verify(exactly = 1) { canvas.rect(Rectangle(x = test.colWidth, width = test.colWidth, height = test.colWidth), ColorBrush(it)) }
            }
        }
    }

    @Test @JsName("checkerEmptyIfColorsTransparent")
    fun `checker empty if colors transparent`() {
        listOf(
            red opacity 0f to transparent,
            null to null
        ).forEach {
            expect(true) { checkerBrush(checkerSize = Size(10.0), firstColor = it.first, secondColor = it.second).size.empty }
        }
    }

    @Test @JsName("checkerSizeCorrect")
    fun `checker size correct`() {
        listOf(Size(20.0, 10.0), Size(1.0), Size(3.4, 0.0), Empty).forEach {
            expect(it * 2) { checkerBrush(checkerSize = it, firstColor = red).size }
        }
    }

    @Test @JsName("checkerRendersCorrectly")
    fun `checker renders correctly`() {
        data class Attributes(val checkerSize: Size, val firstColor: Color?, val secondColor: Color?)

        listOf(
                Attributes(Size(45.6), red, green),
                Attributes(Size( 0.6), red,  null),
                Attributes(Size( 0.6), null,  red)//,
//                Attributes(Size( 0.0), red, green)
        ).forEach { test ->
            val canvas = mockk<Canvas>(relaxed = true)

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