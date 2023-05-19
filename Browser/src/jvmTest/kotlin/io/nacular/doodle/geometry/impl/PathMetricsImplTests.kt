package io.nacular.doodle.geometry.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.nacular.doodle.DOMRect
import io.nacular.doodle.SVGElement
import io.nacular.doodle.SVGGraphicsElement
import io.nacular.doodle.SVGPathElement
import io.nacular.doodle.dom.SvgFactory
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.getBBox
import kotlin.test.Test
import kotlin.test.expect

class PathMetricsImplTests {
    init {
        mockkStatic(SVGGraphicsElement::getBBox)
    }

    @Test fun `width correct`() {
        val size    = Size(100, 45)
        val metrics = PathMetricsImpl(svgFactory(size))

        expect(metrics.width(mockk())) { 100.0 }
    }

    @Test fun `height correct`() {
        val size    = Size(100, 45)
        val metrics = PathMetricsImpl(svgFactory(size))

        expect(metrics.height(mockk())) { 45.0 }
    }

    @Test fun `size correct`() {
        val size    = Size(100, 45)
        val metrics = PathMetricsImpl(svgFactory(size))

        expect(metrics.size(mockk())) { size }
    }

    @Test fun `bounds correct`() {
        val bounds  = Rectangle(10, 2, 45, 67)
        val metrics = PathMetricsImpl(svgFactory(bounds))

        expect(metrics.bounds(mockk())) { bounds }
    }

    @Test fun `length correct`() {
        val metrics = PathMetricsImpl(svgFactory(Size.Empty, 89.0))

        expect(metrics.length(mockk())) { 89.0 }
    }

    private fun svgFactory(size: Size, length: Double = 23.0) = svgFactory(Rectangle(size = size), length)

    @Suppress("LABEL_NAME_CLASH")
    private fun svgFactory(bounds: Rectangle, length: Double = 23.0) = mockk<SvgFactory>().apply {
        every { this@apply.invoke<SVGElement>    ("svg" ) } returns mockk<SVGElement>()
        every { this@apply.invoke<SVGPathElement>("path") } returns mockk<SVGPathElement>().apply {
            every { this@apply.getBBox(match { it.stroke == true && it.markers == true }) } returns mockk<DOMRect>().apply {
                every { this@apply.x      } returns bounds.x
                every { this@apply.y      } returns bounds.y
                every { this@apply.width  } returns bounds.width
                every { this@apply.height } returns bounds.height
            }
            every { this@apply.getTotalLength() } returns length.toFloat()
        }
    }
}