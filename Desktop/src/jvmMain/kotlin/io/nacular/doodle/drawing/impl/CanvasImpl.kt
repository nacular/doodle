package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.Renderer
import io.nacular.doodle.drawing.Shadow
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.impl.ImageImpl
import io.nacular.doodle.skia.rrect
import io.nacular.doodle.skia.skija
import io.nacular.doodle.text.StyledText
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Measure
import org.jetbrains.skija.TextLine
import org.jetbrains.skija.Canvas as SkijaCanvas
import org.jetbrains.skija.Font as SkijaFont


/**
 * Created by Nicholas Eddy on 5/19/21.
 */
internal class CanvasImpl(private val skiaCanvas: SkijaCanvas, private val defaultFont: SkijaFont): Canvas {
    override var size: Size = Size.Empty

    override fun transform(transform: AffineTransform, block: Canvas.() -> Unit) {
        TODO("Not yet implemented")
    }

    override fun rect(rectangle: Rectangle, fill: Paint) {
        skiaCanvas.drawRect(rectangle.skija(), fill.skija())
    }

    override fun rect(rectangle: Rectangle, stroke: Stroke, fill: Paint?) {
        if (fill != null) {
            skiaCanvas.drawRect(rectangle.skija(), fill.skija())
        }
        skiaCanvas.drawRect(rectangle.skija(), stroke.skija())
    }

    override fun rect(rectangle: Rectangle, radius: Double, fill: Paint) {
        skiaCanvas.drawRRect(rectangle.rrect(radius.toFloat()), fill.skija())
    }

    override fun rect(rectangle: Rectangle, radius: Double, stroke: Stroke, fill: Paint?) {
        if (fill != null) {
            skiaCanvas.drawRRect(rectangle.rrect(radius.toFloat()), fill.skija())
        }
        skiaCanvas.drawRRect(rectangle.rrect(radius.toFloat()), stroke.skija())
    }

    override fun circle(circle: Circle, fill: Paint) {
        skiaCanvas.drawCircle(circle.center.x.toFloat(), circle.center.y.toFloat(), circle.radius.toFloat(), fill.skija())
    }

    override fun circle(circle: Circle, stroke: Stroke, fill: Paint?) {
        if (fill != null) {
            skiaCanvas.drawCircle(circle.center.x.toFloat(), circle.center.y.toFloat(), circle.radius.toFloat(), fill.skija())
        }
        skiaCanvas.drawCircle(circle.center.x.toFloat(), circle.center.y.toFloat(), circle.radius.toFloat(), stroke.skija())
    }

    override fun ellipse(ellipse: Ellipse, fill: Paint) {
        skiaCanvas.drawOval(ellipse.boundingRectangle.skija(), fill.skija())
    }

    override fun ellipse(ellipse: Ellipse, stroke: Stroke, fill: Paint?) {
        if (fill != null) {
            skiaCanvas.drawOval(ellipse.boundingRectangle.skija(), fill.skija())
        }
        skiaCanvas.drawOval(ellipse.boundingRectangle.skija(), stroke.skija())
    }

    private val Font?.skia get() = when (this) {
        is FontImpl -> skiaFont
        else        -> defaultFont
    }

    override fun text(text: String, font: Font?, at: Point, fill: Paint) {
        // FIXME: Text offset-y not exactly equal to browser
        skiaCanvas.drawTextLine(TextLine.make(text, font.skia), at.x.toFloat(), at.y.toFloat() + font.skia.size, fill.skija())
    }

    override fun text(text: StyledText, at: Point) {
        TODO("Not yet implemented")
    }

    override fun wrapped(text: String, font: Font?, at: Point, leftMargin: Double, rightMargin: Double, fill: Paint) {
        TODO("Not yet implemented")
    }

    override fun wrapped(text: StyledText, at: Point, leftMargin: Double, rightMargin: Double) {
        TODO("Not yet implemented")
    }

    override fun image(image: Image, destination: Rectangle, opacity: Float, radius: Double, source: Rectangle) {
        if (image is ImageImpl) {
            skiaCanvas.drawImageRect(image.skiaImage, source.skija(), destination.skija())
        }
    }

    override fun clip(rectangle: Rectangle, radius: Double, block: Canvas.() -> Unit) {
        TODO("Not yet implemented")
    }

    override fun clip(polygon: Polygon, block: Canvas.() -> Unit) {
        TODO("Not yet implemented")
    }

    override fun clip(ellipse: Ellipse, block: Canvas.() -> Unit) {
        TODO("Not yet implemented")
    }

    override fun shadow(shadow: Shadow, block: Canvas.() -> Unit) {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun flush() {
        TODO("Not yet implemented")
    }

    override fun line(start: Point, end: Point, stroke: Stroke) {
        TODO("Not yet implemented")
    }

    override fun path(points: List<Point>, fill: Paint, fillRule: Renderer.FillRule?) {
        TODO("Not yet implemented")
    }

    override fun path(path: Path, fill: Paint, fillRule: Renderer.FillRule?) {
        TODO("Not yet implemented")
    }

    override fun path(points: List<Point>, stroke: Stroke) {
        TODO("Not yet implemented")
    }

    override fun path(path: Path, stroke: Stroke) {
        TODO("Not yet implemented")
    }

    override fun path(points: List<Point>, stroke: Stroke, fill: Paint, fillRule: Renderer.FillRule?) {
        TODO("Not yet implemented")
    }

    override fun path(path: Path, stroke: Stroke, fill: Paint, fillRule: Renderer.FillRule?) {
        TODO("Not yet implemented")
    }

    override fun poly(polygon: Polygon, fill: Paint) {
        TODO("Not yet implemented")
    }

    override fun poly(polygon: Polygon, stroke: Stroke, fill: Paint?) {
        TODO("Not yet implemented")
    }

    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, fill: Paint) {
        TODO("Not yet implemented")
    }

    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke, fill: Paint?) {
        TODO("Not yet implemented")
    }

    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, fill: Paint) {
        TODO("Not yet implemented")
    }

    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke, fill: Paint?) {
        TODO("Not yet implemented")
    }
}