package io.nacular.doodle.drawing.impl;

import java.awt.Color
import java.awt.Composite
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.image.BufferedImage
import java.awt.image.BufferedImageOp
import java.awt.image.ImageObserver
import java.awt.image.RenderedImage
import java.awt.image.renderable.RenderableImage
import java.text.AttributedCharacterIterator

internal open class DelegatingGraphics2D(private val delegate: Graphics2D) : Graphics2D() {
    override fun create(): Graphics = delegate.create()

    override fun translate(x: Int, y: Int) = delegate.translate(x, y)

    override fun translate(tx: Double, ty: Double) = delegate.translate(tx, ty)

    override fun getColor(): Color = delegate.color

    override fun setColor(c: Color?) {
        delegate.color = c
    }

    override fun setPaintMode() = delegate.setPaintMode()

    override fun setXORMode(c1: Color?) = delegate.setXORMode(c1)

    override fun getFont(): java.awt.Font = delegate.font

    override fun setFont(font: java.awt.Font?) {
        delegate.font = font
    }

    override fun getFontMetrics(f: java.awt.Font?): FontMetrics = delegate.fontMetrics

    override fun getClipBounds(): java.awt.Rectangle = delegate.clipBounds

    override fun clipRect(x: Int, y: Int, width: Int, height: Int) = delegate.clipRect(x, y, width, height)

    override fun setClip(x: Int, y: Int, width: Int, height: Int) = delegate.setClip(x, y, width, height)

    override fun setClip(clip: Shape?) {
        delegate.clip = clip
    }

    override fun getClip(): Shape = delegate.clip

    override fun copyArea(x: Int, y: Int, width: Int, height: Int, dx: Int, dy: Int) {
        delegate.copyArea(x, y, width, height, dx, dy)
    }

    override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
        delegate.drawLine(x1, y1, x2, y2)
    }

    override fun fillRect(x: Int, y: Int, width: Int, height: Int) = delegate.fillRect(x, y, width, height)
    override fun clearRect(x: Int, y: Int, width: Int, height: Int) = delegate.clearRect(x, y, width, height)
    override fun drawRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int) =
        delegate.drawRoundRect(x, y, width, height, arcWidth, arcHeight)

    override fun fillRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int) =
        delegate.fillRoundRect(x, y, width, height, arcWidth, arcHeight)

    override fun drawOval(x: Int, y: Int, width: Int, height: Int) = delegate.drawOval(x, y, width, height)
    override fun fillOval(x: Int, y: Int, width: Int, height: Int) = delegate.fillOval(x, y, width, height)
    override fun drawArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int) =
        delegate.drawArc(x, y, width, height, startAngle, arcAngle)

    override fun fillArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int) =
        delegate.fillArc(x, y, width, height, startAngle, arcAngle)

    override fun drawPolyline(xPoints: IntArray?, yPoints: IntArray?, nPoints: Int) =
        delegate.drawPolygon(xPoints, yPoints, nPoints)

    override fun drawPolygon(xPoints: IntArray?, yPoints: IntArray?, nPoints: Int) =
        delegate.drawPolygon(xPoints, yPoints, nPoints)

    override fun fillPolygon(xPoints: IntArray?, yPoints: IntArray?, nPoints: Int) =
        delegate.fillPolygon(xPoints, yPoints, nPoints)

    override fun drawString(str: String, x: Int, y: Int) = delegate.drawString(str, x, y)
    override fun drawString(str: String?, x: Float, y: Float) = delegate.drawString(str, x, y)
    override fun drawString(iterator: AttributedCharacterIterator?, x: Int, y: Int) =
        delegate.drawString(iterator, x, y)

    override fun drawString(iterator: AttributedCharacterIterator?, x: Float, y: Float) =
        delegate.drawString(iterator, x, y)

    override fun drawImage(
        img: java.awt.Image?,
        xform: java.awt.geom.AffineTransform?,
        obs: ImageObserver?
    ): Boolean = delegate.drawImage(img, xform, obs)

    override fun drawImage(img: BufferedImage?, op: BufferedImageOp?, x: Int, y: Int) =
        delegate.drawImage(img, op, x, y)

    override fun drawImage(img: java.awt.Image?, x: Int, y: Int, observer: ImageObserver?): Boolean =
        delegate.drawImage(img, x, y, observer)

    override fun drawImage(
        img: java.awt.Image?,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        observer: ImageObserver?
    ): Boolean = delegate.drawImage(img, x, y, width, height, observer)

    override fun drawImage(
        img: java.awt.Image?,
        x: Int,
        y: Int,
        bgcolor: Color?,
        observer: ImageObserver?
    ): Boolean = delegate.drawImage(img, x, y, bgcolor, observer)

    override fun drawImage(
        img: java.awt.Image?,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        bgcolor: Color?,
        observer: ImageObserver?
    ): Boolean = delegate.drawImage(img, x, y, width, height, bgcolor, observer)

    override fun drawImage(
        img: java.awt.Image?,
        dx1: Int,
        dy1: Int,
        dx2: Int,
        dy2: Int,
        sx1: Int,
        sy1: Int,
        sx2: Int,
        sy2: Int,
        observer: ImageObserver?
    ): Boolean = delegate.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer)

    override fun drawImage(
        img: java.awt.Image?,
        dx1: Int,
        dy1: Int,
        dx2: Int,
        dy2: Int,
        sx1: Int,
        sy1: Int,
        sx2: Int,
        sy2: Int,
        bgcolor: Color?,
        observer: ImageObserver?
    ): Boolean = delegate.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer)

    override fun dispose() = delegate.dispose()

    override fun draw(s: Shape?) = delegate.draw(s)

    override fun drawRenderedImage(img: RenderedImage?, xform: java.awt.geom.AffineTransform?) =
        delegate.drawRenderedImage(img, xform)

    override fun drawRenderableImage(img: RenderableImage?, xform: java.awt.geom.AffineTransform?) =
        delegate.drawRenderableImage(img, xform)

    override fun drawGlyphVector(g: GlyphVector?, x: Float, y: Float) = delegate.drawGlyphVector(g, x, y)

    override fun fill(s: Shape?) = delegate.fill(s)

    override fun hit(rect: java.awt.Rectangle?, s: Shape?, onStroke: Boolean): Boolean = delegate.hit(rect, s, onStroke)

    override fun getDeviceConfiguration(): GraphicsConfiguration = delegate.deviceConfiguration

    override fun setComposite(comp: Composite?) {
        delegate.composite = comp
    }

    override fun setPaint(paint: java.awt.Paint?) {
        delegate.paint = paint
    }

    override fun setStroke(s: java.awt.Stroke?) {
        delegate.stroke = s
    }

    override fun setRenderingHint(hintKey: RenderingHints.Key?, hintValue: Any?) {
        delegate.setRenderingHint(hintKey, hintValue)
    }

    override fun getRenderingHint(hintKey: RenderingHints.Key?): Any = delegate.getRenderingHint(hintKey)

    override fun setRenderingHints(hints: MutableMap<*, *>?) {
        delegate.setRenderingHints(hints)
    }

    override fun addRenderingHints(hints: MutableMap<*, *>?) {
        delegate.addRenderingHints(hints)
    }

    override fun getRenderingHints(): RenderingHints = delegate.renderingHints

    override fun rotate(theta: Double) {
        delegate.rotate(theta)
    }

    override fun rotate(theta: Double, x: Double, y: Double) {
        delegate.rotate(theta, x, y)
    }

    override fun scale(sx: Double, sy: Double) {
        delegate.scale(sx, sy)
    }

    override fun shear(shx: Double, shy: Double) {
        delegate.shear(shx, shy)
    }

    override fun transform(transform: java.awt.geom.AffineTransform?) {
        delegate.transform(transform)
    }

    override fun setTransform(transform: java.awt.geom.AffineTransform?) {
        delegate.transform = transform
    }

    override fun getTransform(): java.awt.geom.AffineTransform = delegate.transform

    override fun getPaint(): java.awt.Paint = delegate.paint

    override fun getComposite(): Composite = delegate.composite

    override fun setBackground(color: Color?) {
        delegate.background = color
    }

    override fun getBackground(): Color = delegate.background

    override fun getStroke(): java.awt.Stroke = delegate.stroke

    override fun clip(s: Shape?) = delegate.clip(s)

    override fun getFontRenderContext(): FontRenderContext = delegate.fontRenderContext
}