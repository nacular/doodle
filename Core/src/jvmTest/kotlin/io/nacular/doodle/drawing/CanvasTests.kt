package io.nacular.doodle.drawing

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.nacular.doodle.core.Camera
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.image.Image
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.text.TextSpacing
import io.nacular.doodle.utils.TextAlignment
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times
import kotlin.test.Test

/**
 * Created by Nicholas Eddy on 6/25/20.
 */
private class TestCanvas: Canvas {
    override var size = Empty

    override fun transform(transform: AffineTransform, block: Canvas.() -> Unit) {}

    override fun transform(transform: AffineTransform, camera: Camera, block: Canvas.() -> Unit) {}

    override fun rect(rectangle: Rectangle, fill: Paint) {}

    override fun rect(rectangle: Rectangle, stroke: Stroke, fill: Paint?) {}

    override fun rect(rectangle: Rectangle, radius: Double, fill: Paint) {}

    override fun rect(rectangle: Rectangle, radius: Double, stroke: Stroke, fill: Paint?) {}

    override fun circle(circle: Circle, fill: Paint) {}

    override fun circle(circle: Circle, stroke: Stroke, fill: Paint?) {}

    override fun ellipse(ellipse: Ellipse, fill: Paint) {}

    override fun ellipse(ellipse: Ellipse, stroke: Stroke, fill: Paint?) {}

    override fun text(text: String, font: Font?, at: Point, fill: Paint, textSpacing: TextSpacing) {}

    override fun text(text: StyledText, at: Point, textSpacing: TextSpacing) {}

    override fun wrapped(text: String, at: Point, width: Double, fill: Paint, font: Font?, indent: Double, alignment: TextAlignment, lineSpacing: Float, textSpacing: TextSpacing) {}

    override fun wrapped(text: StyledText, at: Point, width: Double, indent: Double, alignment: TextAlignment, lineSpacing: Float, textSpacing: TextSpacing) {}

    override fun image(image: Image, destination: Rectangle, opacity: Float, radius: Double, source: Rectangle) {}

    override fun clip(rectangle: Rectangle, radius: Double, block: Canvas.() -> Unit) {}

    override fun clip(polygon: Polygon, block: Canvas.() -> Unit) {}

    override fun clip(ellipse: Ellipse, block: Canvas.() -> Unit) {}

    override fun clip(path: Path, block: Canvas.() -> Unit) {}

    override fun shadow(shadow: Shadow, block: Canvas.() -> Unit) {}

    override fun clear() {}

    override fun flush() {}

    override fun line(start: Point, end: Point, stroke: Stroke) {}

    override fun path(points: List<Point>, fill: Paint, fillRule: Renderer.FillRule?) {}

    override fun path(path: Path, fill: Paint, fillRule: Renderer.FillRule?) {}

    override fun path(points: List<Point>, stroke: Stroke) {}

    override fun path(path: Path, stroke: Stroke) {}

    override fun path(points: List<Point>, stroke: Stroke, fill: Paint, fillRule: Renderer.FillRule?) {}

    override fun path(path: Path, stroke: Stroke, fill: Paint, fillRule: Renderer.FillRule?) {}

    override fun poly(polygon: Polygon, fill: Paint) {}

    override fun poly(polygon: Polygon, stroke: Stroke, fill: Paint?) {}

    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, fill: Paint) {}

    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke, fill: Paint?) {}

    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, fill: Paint) {}

    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke, fill: Paint?) {}
}

class CanvasTests {
    @Test fun `scale with defaults works`() {
        val canvas = spyk(TestCanvas())
        val block  = mockk<Canvas.() -> Unit>()

        canvas.scale(block = block)

        verify { block(canvas) }
    }

    @Test fun `scale works`() {
        val canvas = spyk(TestCanvas())
        val x      = 45.0
        val y      = 1.0
        val block  = mockk<Canvas.() -> Unit>()

        canvas.scale(x = x, y = y, block = block)

        verify { canvas.transform(Identity.scale(x, y), block) }
    }

    @Test fun `scale around with defaults works`() {
        val canvas = spyk(TestCanvas())
        val block  = mockk<Canvas.() -> Unit>()

        canvas.scale(around = Point(4.0, 2.9), block = block)

        verify { block(canvas) }
    }

    @Test fun `scale around works`() {
        val canvas = spyk(TestCanvas()).apply {
            every { size } returns Size(457)
        }
        val x      = 45.0
        val y      = 1.0
        val around = Point(4.0, 2.9)
        val block  = mockk<Canvas.() -> Unit>()

        canvas.scale(around = around, x = x, y = y, block = block)

        verify { canvas.transform(Identity.translate(around).scale(x, y).translate(-around), block) }
    }

    @Test fun `rotate works`() {
        val canvas = spyk(TestCanvas())
        val block  = mockk<Canvas.() -> Unit>()
        val by     = 56 * degrees

        canvas.rotate(by = by, block = block)

        verify { canvas.transform(Identity.rotate(by), block) }
    }

    @Test fun `rotate around works`() {
        val canvas = spyk(TestCanvas())
        val by     = 56 * degrees
        val around = Point(4.0, 2.9)
        val block  = mockk<Canvas.() -> Unit>()

        canvas.rotate(around = around, by = by, block = block)

        verify { canvas.transform(Identity.translate(around).rotate(by).translate(-around), block) }
    }

    @Test fun `translate works`() {
        val canvas = spyk(TestCanvas())
        val by     = Point(4.0, 2.9)
        val block  = mockk<Canvas.() -> Unit>()

        canvas.translate(by = by, block = block)

        verify { canvas.transform(Identity.translate(by), block) }
    }

    @Test fun `flip vertically works`() {
        val canvas = spyk(TestCanvas())
        val block  = mockk<Canvas.() -> Unit>()

        canvas.flipVertically(block)

        verify { canvas.scale(1.0, -1.0, block) }
    }

    @Test fun `flip vertically around works`() {
        val canvas = spyk(TestCanvas())
        val block  = mockk<Canvas.() -> Unit>()
        val around = 4.5

        canvas.flipVertically(around, block)

        verify { canvas.transform(Identity.translate(y = around).scale(1.0, -1.0).translate(y = -around), block) }
    }

    @Test fun `flip horizontally works`() {
        val canvas = spyk(TestCanvas())
        val block  = mockk<Canvas.() -> Unit>()

        canvas.flipHorizontally(block)

        verify { canvas.scale(-1.0, 1.0, block) }
    }

    @Test fun `flip horizontally around works`() {
        val canvas = spyk(TestCanvas())
        val block  = mockk<Canvas.() -> Unit>()
        val around = 4.5

        canvas.flipHorizontally(around, block)

        verify { canvas.transform(Identity.translate(x = around).scale(-1.0, 1.0).translate(x = -around), block) }
    }
}