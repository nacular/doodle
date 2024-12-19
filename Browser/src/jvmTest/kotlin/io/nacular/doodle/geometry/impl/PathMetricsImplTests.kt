package io.nacular.doodle.geometry.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.nacular.doodle.dom.DOMRect
import io.nacular.doodle.dom.SVGElement
import io.nacular.doodle.dom.SVGPathElement
import io.nacular.doodle.dom.SvgFactory
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import kotlin.test.Test
import kotlin.test.expect

class PathMetricsImplTests {
    init {
        mockkStatic(SVGElement::getBBox)
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

    private fun svgFactory(bounds: Rectangle, length: Double = 23.0) = mockk<SvgFactory> {
        every { invoke<SVGElement>    ("svg" ) } returns mockk()
        every { invoke<SVGPathElement>("path") } returns svgPathElement(bounds = bounds, length = length)
    }

    private fun svgPathElement(bounds: Rectangle, length: Double = 23.0) = mockk<SVGPathElement> {
        every { getBBox(match { it.stroke == true && it.markers == true }) } returns domRect(bounds)
        every { getTotalLength() } returns length.toFloat()
    }

    private fun domRect(bounds: Rectangle) = mockk<DOMRect> {
        every { x      } returns bounds.x
        every { y      } returns bounds.y
        every { width  } returns bounds.width
        every { height } returns bounds.height
    }
}