package io.nacular.doodle.theme.native

import io.nacular.doodle.drawing.impl.TextMetricsImpl
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.FilterTileMode.CLAMP
import org.jetbrains.skia.FilterTileMode.MIRROR
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.GradientStyle
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PaintStrokeCap
import org.jetbrains.skia.PaintStrokeCap.BUTT
import org.jetbrains.skia.PaintStrokeCap.ROUND
import org.jetbrains.skia.PaintStrokeCap.SQUARE
import org.jetbrains.skia.PaintStrokeJoin
import org.jetbrains.skia.PaintStrokeJoin.BEVEL
import org.jetbrains.skia.PaintStrokeJoin.MITER
import org.jetbrains.skia.Path
import org.jetbrains.skia.PathEffect
import org.jetbrains.skia.PathFillMode
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Shader
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skiko.toImage
import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.BasicStroke.CAP_BUTT
import java.awt.BasicStroke.CAP_ROUND
import java.awt.BasicStroke.CAP_SQUARE
import java.awt.BasicStroke.JOIN_BEVEL
import java.awt.BasicStroke.JOIN_MITER
import java.awt.BasicStroke.JOIN_ROUND
import java.awt.Color
import java.awt.Composite
import java.awt.FontMetrics
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import java.awt.GraphicsDevice
import java.awt.Image
import java.awt.ImageCapabilities
import java.awt.LinearGradientPaint
import java.awt.MultipleGradientPaint.CycleMethod
import java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE
import java.awt.MultipleGradientPaint.CycleMethod.REFLECT
import java.awt.MultipleGradientPaint.CycleMethod.REPEAT
import java.awt.RadialGradientPaint
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.Stroke
import java.awt.Transparency.OPAQUE
import java.awt.Transparency.TRANSLUCENT
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform
import java.awt.geom.Arc2D
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.awt.geom.GeneralPath
import java.awt.geom.Line2D
import java.awt.geom.NoninvertibleTransformException
import java.awt.geom.Path2D
import java.awt.geom.PathIterator.SEG_CLOSE
import java.awt.geom.PathIterator.SEG_CUBICTO
import java.awt.geom.PathIterator.SEG_LINETO
import java.awt.geom.PathIterator.SEG_MOVETO
import java.awt.geom.PathIterator.SEG_QUADTO
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.awt.image.BufferedImageOp
import java.awt.image.ColorModel
import java.awt.image.DirectColorModel
import java.awt.image.ImageObserver
import java.awt.image.RenderedImage
import java.awt.image.VolatileImage
import java.awt.image.renderable.RenderableImage
import java.text.AttributedCharacterIterator
import java.util.*
import kotlin.math.max
import org.jetbrains.skia.Image as SkiaImage
import java.awt.Font as AwtFont
import java.awt.Paint as AwtPaint

/**
 * Classes are derived works from [https://github.com/jfree/skijagraphics2d/tree/main/src/main/java/org/jfree/skija],
 * which have the following copyright.
 *
 * Copyright (c) 2021, Object Refinery Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


internal class SkiaFontMetrics(private val skiaFont: Font, awtFont: java.awt.Font?, private val textMetrics: TextMetricsImpl): FontMetrics(awtFont) {
    private inner class AwtFontWrapper(f: java.awt.Font): java.awt.Font(f) {
        override fun getStringBounds(chars: CharArray, beginIndex: Int, limit: Int, frc: FontRenderContext?): Rectangle2D = textMetrics.size(
                String(chars, beginIndex, limit - beginIndex), skiaFont.textStyle()
        ).run { Rectangle2D.Double(0.0, 0.0, width, height) }
    }

    private val metrics: org.jetbrains.skia.FontMetrics = skiaFont.metrics

    override fun getFont   (                                   ): java.awt.Font = AwtFontWrapper(super.getFont())
    override fun getLeading(                                   ) = metrics.leading.toInt()
    override fun getAscent (                                   ) = (-metrics.ascent).toInt()
    override fun getDescent(                                   ) = metrics.descent.toInt()
    override fun charWidth (ch: Char                           ) = textMetrics.width(ch.toString(), skiaFont.textStyle()).toInt()
    override fun charsWidth(data: CharArray, off: Int, len: Int) = textMetrics.width(String(data, off, len), skiaFont.textStyle()).toInt()
}

private class SkiaGraphicsConfiguration(private val width: Int, private val height: Int): GraphicsConfiguration() {
    private class SkiaGraphicsDevice(private val id: String, private var defaultConfig: GraphicsConfiguration) : GraphicsDevice() {
        override fun getType(): Int = TYPE_RASTER_SCREEN

        override fun getIDstring(): String = id

        override fun getConfigurations(): Array<GraphicsConfiguration> = arrayOf(defaultConfiguration)

        override fun getDefaultConfiguration(): GraphicsConfiguration = defaultConfig
    }

    private val graphicsDevice: GraphicsDevice by lazy { SkiaGraphicsDevice("SkiaGraphicsDevice", this) }

    private lateinit var graphicsConfiguration: GraphicsConfiguration

    override fun getDevice(): GraphicsDevice = graphicsDevice

    override fun getColorModel(): ColorModel? = getColorModel(TRANSLUCENT)

    override fun getColorModel(transparency: Int): ColorModel? = when (transparency) {
        TRANSLUCENT -> ColorModel.getRGBdefault()
        OPAQUE      -> DirectColorModel(32, 0x00ff0000, 0x0000ff00, 0x000000ff)
        else        -> null
    }

    override fun getDefaultTransform() = AffineTransform()

    override fun getNormalizingTransform() = AffineTransform()

    override fun getBounds(): Rectangle = Rectangle(width, height)

    override fun createCompatibleVolatileImage(width: Int, height: Int, caps: ImageCapabilities, transparency: Int): VolatileImage {
        if (!this::graphicsConfiguration.isInitialized) {
            graphicsConfiguration = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics().deviceConfiguration
        }
        return graphicsConfiguration.createCompatibleVolatileImage(width, height, caps, transparency)
    }
}

internal class SkiaGraphics2D(
    private val fontManager   : FontMgr,
    private val fontCollection: FontCollection,
    private val defaultFont   : Font,
    private val canvas        : Canvas,
    private val textMetrics   : TextMetricsImpl
): Graphics2D() {

    private val hints  = RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT)
    private var width  = 0
    private var height = 0

    private var skiaFont     = defaultFont
    private val skiaPaint    = Paint().apply { color =-0x1000000 }
    private val typefaceMap  = mutableMapOf<Pair<String, FontStyle>, Typeface?>()
    private var restoreCount = 0

    private var awtFont    : AwtFont
    private var awtPaint   = null as AwtPaint?
    private var color      = Color.BLACK
    private var stroke     = BasicStroke(1.0f) as Stroke
    private var background = null as Color?
    private var transform  = AffineTransform()
    private var composite  = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f) as Composite

    private var clip                   = null as Shape?
    private val coords                 = DoubleArray(6)
    private val fontRenderContext      = FontRenderContext(null, false, true)

    private lateinit var line          : Line2D
    private lateinit var rectangle     : Rectangle2D
    private lateinit var roundRectangle: RoundRectangle2D
    private lateinit var oval          : Ellipse2D
    private lateinit var arc           : Arc2D
    private          val graphicsConfiguration: GraphicsConfiguration by lazy { SkiaGraphicsConfiguration(width, height) }

    init {
        awtFont      = skiaFont.toAwt()
        restoreCount = this.canvas.save()
    }

    override fun draw(shape: Shape) {
        skiaPaint.mode = PaintMode.STROKE

        when (shape) {
            is Line2D      -> canvas.drawLine(shape.x1.toFloat(), shape.y1.toFloat(), shape.x2.toFloat(), shape.y2.toFloat(), skiaPaint)
            is Rectangle2D -> if (shape.width >= 0.0 && shape.height >= 0.0) {
                canvas.drawRect(Rect.makeXYWH(shape.x.toFloat(), shape.y.toFloat(), shape.width.toFloat(), shape.height.toFloat()), skiaPaint)
            }
            else           -> canvas.drawPath(shape.path, skiaPaint)
        }
    }

    override fun drawImage(image: Image?, xform: AffineTransform?, observer: ImageObserver?): Boolean {
        val savedTransform = getTransform()

        if (xform != null) {
            transform(xform)
        }
        val result = drawImage(image, 0, 0, observer)
        if (xform != null) {
            setTransform(savedTransform)
        }
        return result
    }

    override fun drawImage(image: BufferedImage?, imageOp: BufferedImageOp?, x: Int, y: Int) {
        var imageToDraw = image
        if (imageOp != null) {
            imageToDraw = imageOp.filter(image, null)
        }
        drawImage(imageToDraw, AffineTransform(1.0, 0.0, 0.0, 1.0, x.toDouble(), y.toDouble()), null)
    }

    override fun drawRenderedImage(image: RenderedImage?, transform: AffineTransform) {
        if (image != null) {
            drawImage(image.bufferedImage, transform, null)
        }
    }

    override fun drawRenderableImage(image: RenderableImage, xform: AffineTransform): Unit = drawRenderedImage(image.createDefaultRendering(), xform)

    override fun drawString(string: String, x: Int, y: Int): Unit = drawString(string, x.toFloat(), y.toFloat())

    override fun drawString(string: String, x: Float, y: Float) {
        canvas.drawString(string, x, y, skiaFont, skiaPaint.apply { mode = PaintMode.FILL })
    }

    override fun drawString(characters: AttributedCharacterIterator, x: Int, y: Int): Unit = drawString(characters, x.toFloat(), y.toFloat())

    override fun drawString(characters: AttributedCharacterIterator, x: Float, y: Float) = when {
        characters.allAttributeKeys.isNotEmpty() -> TextLayout(characters, getFontRenderContext()).draw(this, x, y)
        else                                     -> {
            val strb = StringBuilder()
            characters.first()
            for (i in characters.beginIndex until characters.endIndex) {
                strb.append(characters.current())
                characters.next()
            }
            drawString(strb.toString(), x, y)
        }
    }

    override fun drawGlyphVector(vector: GlyphVector, x: Float, y: Float): Unit = fill(vector.getOutline(x, y))

    override fun fill(shape: Shape) {
        skiaPaint.mode = PaintMode.FILL

        when (shape) {
            is Rectangle2D -> {
                if (shape.width >= 0.0 && shape.height >= 0.0) {
                    canvas.drawRect(Rect.makeXYWH(shape.x.toFloat(), shape.y.toFloat(), shape.width.toFloat(), shape.height.toFloat()), skiaPaint)
                }
            }
            is Path2D -> {
                val path = shape.path.apply {
                    fillMode = when (shape.windingRule) {
                        Path2D.WIND_EVEN_ODD -> PathFillMode.EVEN_ODD
                        else                 -> PathFillMode.WINDING
                    }
                }
                canvas.drawPath(path, skiaPaint)
            }
            else  -> canvas.drawPath(shape.path, skiaPaint)
        }
    }

    override fun hit(rectangle: Rectangle, shape: Shape, onStroke: Boolean): Boolean {
        val transformedShape = when {
            onStroke -> transform.createTransformedShape(stroke.createStrokedShape(shape))
            else     -> transform.createTransformedShape(shape)
        }

        if (!rectangle.bounds2D.intersects(transformedShape.bounds2D)) {
            return false
        }

        return !Area(rectangle).also { it.intersect(Area(transformedShape)) }.isEmpty
    }

    override fun getDeviceConfiguration(): GraphicsConfiguration = graphicsConfiguration

    override fun setComposite(newComposite: Composite) {
        composite = newComposite

        if (newComposite is AlphaComposite) {
            skiaPaint.setAlphaf(newComposite.alpha)

            when (newComposite.rule) {
                AlphaComposite.CLEAR    -> skiaPaint.apply { blendMode = BlendMode.CLEAR    }
                AlphaComposite.SRC      -> skiaPaint.apply { blendMode = BlendMode.SRC      }
                AlphaComposite.SRC_OVER -> skiaPaint.apply { blendMode = BlendMode.SRC_OVER }
                AlphaComposite.DST_OVER -> skiaPaint.apply { blendMode = BlendMode.DST_OVER }
                AlphaComposite.SRC_IN   -> skiaPaint.apply { blendMode = BlendMode.SRC_IN   }
                AlphaComposite.DST_IN   -> skiaPaint.apply { blendMode = BlendMode.DST_IN   }
                AlphaComposite.SRC_OUT  -> skiaPaint.apply { blendMode = BlendMode.SRC_OUT  }
                AlphaComposite.DST_OUT  -> skiaPaint.apply { blendMode = BlendMode.DST_OUT  }
                AlphaComposite.DST      -> skiaPaint.apply { blendMode = BlendMode.DST      }
                AlphaComposite.SRC_ATOP -> skiaPaint.apply { blendMode = BlendMode.SRC_ATOP }
                AlphaComposite.DST_ATOP -> skiaPaint.apply { blendMode = BlendMode.DST_ATOP }
            }
        }
    }

    override fun setPaint(paint: AwtPaint?) {
        if (paint == null) {
            return
        }
//        if (paintsAreEqual(paint, awtPaint)) {
//            return
//        }
        awtPaint = paint

        when (paint) {
            is Color -> {
                color = paint
                skiaPaint.shader = Shader.makeColor(paint.rgb)
            }
            is LinearGradientPaint -> {
                val x0 = paint.startPoint.x.toFloat()
                val y0 = paint.startPoint.y.toFloat()
                val x1 = paint.endPoint.x.toFloat()
                val y1 = paint.endPoint.y.toFloat()
                val colors = IntArray(paint.colors.size)

                paint.colors.indices.forEach { i ->
                    colors[i] = paint.colors[i].rgb
                }
                skiaPaint.shader = Shader.makeLinearGradient(x0, y0, x1, y1, colors, paint.fractions, GradientStyle.DEFAULT.withTileMode(paint.cycleMethod.skia))
            }
            is RadialGradientPaint -> {
                val x = paint.centerPoint.x.toFloat()
                val y = paint.centerPoint.y.toFloat()
                val colors = IntArray(paint.colors.size)
                for (i in paint.colors.indices) {
                    colors[i] = paint.colors[i].rgb
                }
                skiaPaint.shader = Shader.makeRadialGradient(x, y, paint.radius, colors, paint.fractions, GradientStyle.DEFAULT.withTileMode(paint.cycleMethod.skia))
            }
            is GradientPaint -> {
                val x1 = paint.point1.x.toFloat()
                val y1 = paint.point1.y.toFloat()
                val x2 = paint.point2.x.toFloat()
                val y2 = paint.point2.y.toFloat()
                val colors = intArrayOf(paint.color1.rgb, paint.color2.rgb)
                val gradientStyle = when {
                    paint.isCyclic -> GradientStyle.DEFAULT.withTileMode(MIRROR)
                    else           -> GradientStyle.DEFAULT
                }
                skiaPaint.shader = Shader.makeLinearGradient(x1, y1, x2, y2, colors, null as FloatArray?, gradientStyle)
            }
        }
    }

    override fun setStroke(newStroke: Stroke) {
        if (newStroke === stroke) {
            return
        }

        if (stroke is BasicStroke && newStroke is BasicStroke) {
            skiaPaint.strokeWidth = max(newStroke.lineWidth.toDouble(), MIN_LINE_WIDTH).toFloat()
            skiaPaint.strokeCap   = awtToSkiaLineCap (newStroke.endCap  )
            skiaPaint.strokeJoin  = awtToSkiaLineJoin(newStroke.lineJoin)
            skiaPaint.strokeMiter = newStroke.miterLimit

            skiaPaint.pathEffect = when {
                newStroke.dashArray != null -> PathEffect.makeDash(newStroke.dashArray, newStroke.dashPhase)
                else                        -> null
            }
        }

        stroke = newStroke
    }

    override fun getRenderingHint(hintKey: RenderingHints.Key): Any? = hints[hintKey]

    override fun setRenderingHint(hintKey: RenderingHints.Key, hintValue: Any) {
        hints[hintKey] = hintValue
    }

    override fun setRenderingHints(hints: Map<*, *>?) {
        this.hints.clear()
        this.hints.putAll(hints!!)
    }

    override fun addRenderingHints(hints: Map<*, *>?) {
        this.hints.putAll(hints!!)
    }

    override fun getRenderingHints(): RenderingHints = hints.clone() as RenderingHints

    override fun translate(tx: Int, ty: Int) {
        translate(tx.toDouble(), ty.toDouble())
    }

    override fun translate(tx: Double, ty: Double) {
        transform.translate(tx, ty)
        canvas.translate(tx.toFloat(), ty.toFloat())
    }

    override fun rotate(angle: Double) {
        transform.rotate(angle)
        canvas.rotate(Math.toDegrees(angle).toFloat())
    }

    override fun rotate(angle: Double, x: Double, y: Double) {
        translate(x, y)
        rotate(angle)
        translate(-x, -y)
    }

    override fun scale(sx: Double, sy: Double) {
        transform.scale(sx, sy)
        canvas.scale(sx.toFloat(), sy.toFloat())
    }

    override fun shear(shx: Double, shy: Double) {
        transform.shear(shx, shy)
        canvas.skew(shx.toFloat(), shy.toFloat())
    }

    override fun transform(newTransform: AffineTransform) {
        val transform = getTransform()
        transform.concatenate(newTransform)
        setTransform(transform)
    }

    override fun getTransform(): AffineTransform = transform.clone() as AffineTransform

    override fun setTransform(newTransform: AffineTransform?) {
        transform = AffineTransform(newTransform ?: AffineTransform())

        val m33 = Matrix33(transform.scaleX.toFloat(), transform.shearX.toFloat(), transform.translateX.toFloat(),
                transform.shearY.toFloat(), transform.scaleY.toFloat(), transform.translateY.toFloat(), 0f, 0f, 1f)

        canvas.setMatrix(m33) //initialCanvasTransform.makeConcat(m33))
    }

    override fun getPaint(): AwtPaint? = awtPaint

    override fun getComposite(): Composite = composite

    override fun getBackground(): Color? = background

    override fun setBackground(color: Color?) {
        background = color
    }

    override fun getStroke(): Stroke = stroke

    override fun getFontRenderContext(): FontRenderContext = fontRenderContext

    override fun create(): Graphics = SkiaGraphics2D(fontManager, fontCollection, defaultFont, canvas, textMetrics).also {
        it.setRenderingHints(renderingHints )
        it.clip  = clip
        it.paint = paint
        it.setColor         (getColor     ())
        it.setComposite     (getComposite ())
        it.setStroke        (getStroke    ())
        it.font  = font
        it.setTransform     (getTransform ())
        it.setBackground    (getBackground())
    }

    override fun getColor(): Color = color

    override fun setColor(newColor: Color?) {
        if (newColor == null || newColor == color) {
            return
        }

        color    = newColor
        awtPaint = newColor
        paint    = newColor
    }

    override fun setPaintMode() {
        // not implemented
    }

    override fun setXORMode(c1: Color) {
        // not implemented
    }

    override fun getFont(): AwtFont = awtFont

    override fun setFont(font: AwtFont?) {
        if (font == null) {
            return
        }

        awtFont = font
        val skiaStyle = font.skiaStyle()

        val typeface = typefaceMap.getOrPut(font.family to skiaStyle) {
            fontManager.legacyMakeTypeface(font.family, skiaStyle)
        }

        skiaFont = Font(typeface, font.size.toFloat())
    }

    override fun getFontMetrics(f: AwtFont): FontMetrics = SkiaFontMetrics(skiaFont, awtFont, textMetrics)

    override fun getClipBounds(): Rectangle? = getClip()?.bounds

    override fun getClip(): Shape? = clip?.let {
        try {
            transform.createInverse().createTransformedShape(it)
        } catch (ex: NoninvertibleTransformException) {
            null
        }
    }

    override fun setClip(shape: Shape?) {
        canvas.restoreToCount(restoreCount)
        restoreCount = canvas.save()
        setTransform(getTransform())
        clip = transform.createTransformedShape(shape)
        if (shape != null) {
            canvas.clipPath(shape.path, antiAlias = true)
        }
    }

    override fun clipRect(x: Int, y: Int, width: Int, height: Int): Unit = clip(rect(x, y, width, height))

    override fun setClip(x: Int, y: Int, width: Int, height: Int): Unit = setClip(rect(x, y, width, height))

    override fun clip(newShape: Shape) {
        var shape = newShape

        if (shape is Line2D) {
            shape = shape.getBounds2D()
        }

        when {
            clip == null                       -> setClip(shape)
            !shape.intersects(getClip()!!.bounds2D) -> setClip(Rectangle2D.Double())
            else                               -> {
                val area = Area(shape)
                area.intersect(Area(getClip()!!))
                setClip(Path2D.Double(area))
                canvas.clipPath(shape.path, antiAlias = true)
            }
        }
    }

    override fun copyArea(x: Int, y: Int, width: Int, height: Int, dx: Int, dy: Int) {
        // FIXME: implement this, low priority
    }

    override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
        when {
            !this::line.isInitialized -> line = Line2D.Double(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble())
            else                      -> line.setLine(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble())
        }

        draw(line)
    }

    override fun fillRect(x: Int, y: Int, width: Int, height: Int) {
        fill(rect(x, y, width, height))
    }

    override fun clearRect(x: Int, y: Int, width: Int, height: Int) {
        if (getBackground() == null) {
            return
        }
        val saved = paint
        paint = getBackground()
        fillRect(x, y, width, height)
        paint = saved
    }

    private fun rect(x: Int, y: Int, width: Int, height: Int): Rectangle2D {
        when {
            !this::rectangle.isInitialized -> rectangle = Rectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
            else                           -> rectangle.setRect(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
        }
        return rectangle
    }

    override fun drawRoundRect    (x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int): Unit = draw(createRoundedRect(x, y, width, height, arcWidth, arcHeight))
    override fun fillRoundRect    (x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int): Unit = fill(createRoundedRect(x, y, width, height, arcWidth, arcHeight))
    private  fun createRoundedRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int): RoundRectangle2D {
        when {
            !this::roundRectangle.isInitialized -> roundRectangle = RoundRectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), arcWidth.toDouble(), arcHeight.toDouble())
            else                                -> roundRectangle.setRoundRect(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), arcWidth.toDouble(), arcHeight.toDouble())
        }
        return roundRectangle
    }

    override fun drawOval (x: Int, y: Int, width: Int, height: Int): Unit = draw(createOval(x, y, width, height))
    override fun fillOval (x: Int, y: Int, width: Int, height: Int): Unit = fill(createOval(x, y, width, height))
    private  fun createOval(x: Int, y: Int, width: Int, height: Int): Ellipse2D {
        when {
            !this::oval.isInitialized -> oval = Ellipse2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
            else                      -> oval.setFrame(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
        }
        return oval
    }

    override fun drawArc  (x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int): Unit = draw(createArc(x, y, width, height, startAngle, arcAngle))
    override fun fillArc  (x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int): Unit = fill(createArc(x, y, width, height, startAngle, arcAngle))
    private  fun createArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int): Arc2D {
        when {
            !this::arc.isInitialized -> arc = Arc2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), startAngle.toDouble(), arcAngle.toDouble(), Arc2D.OPEN)
            else                     -> arc.setArc(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), startAngle.toDouble(), arcAngle.toDouble(), Arc2D.OPEN)
        }
        return arc
    }

    override fun drawPolyline (xPoints: IntArray, yPoints: IntArray, nPoints: Int): Unit = draw(createPolygon(xPoints, yPoints, nPoints, false))
    override fun drawPolygon  (xPoints: IntArray, yPoints: IntArray, nPoints: Int): Unit = draw(createPolygon(xPoints, yPoints, nPoints, true))
    override fun fillPolygon  (xPoints: IntArray, yPoints: IntArray, nPoints: Int): Unit = fill(createPolygon(xPoints, yPoints, nPoints, true))
    private  fun createPolygon(xPoints: IntArray, yPoints: IntArray, nPoints: Int, close: Boolean): GeneralPath {
        val path = GeneralPath()

        path.moveTo(xPoints[0].toDouble(), yPoints[0].toDouble())

        for (i in 1 until nPoints) {
            path.lineTo(xPoints[i].toDouble(), yPoints[i].toDouble())
        }

        if (close) {
            path.closePath()
        }

        return path
    }

    override fun drawImage(image: Image?, x: Int, y: Int, observer: ImageObserver?): Boolean {
        if (image == null) {
            return true
        }
        val width = image.getWidth(observer)
        if (width < 0) {
            return false
        }
        val height = image.getHeight(observer)

        return when {
            height < 0 -> false
            else       -> drawImage(image, x, y, width, height, observer)
        }
    }

    override fun drawImage(image: Image?, x: Int, y: Int, width: Int, height: Int, observer: ImageObserver?) = when {
        image == null             -> true
        width <= 0 || height <= 0 -> true
        else                      -> {
            canvas.drawImageRect(image.skia, Rect(x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + height).toFloat()))
            true
        }
    }

    override fun drawImage(image: Image?, x: Int, y: Int, bgcolor: Color, observer: ImageObserver?) = image?.let { drawImage(it, x, y, image.getWidth(null), image.getHeight(null), bgcolor, observer) } ?: true

    override fun drawImage(image: Image?, x: Int, y: Int, width: Int, height: Int, bgcolor: Color, observer: ImageObserver?): Boolean {
        val saved = paint
        paint = bgcolor
        fillRect(x, y, width, height)
        paint = saved
        return drawImage(image, x, y, width, height, observer)
    }

    override fun drawImage(image: Image?, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Int, sy1: Int, sx2: Int, sy2: Int, observer: ImageObserver?): Boolean {
        val width  = dx2 - dx1
        val height = dy2 - dy1
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        bufferedImage.createGraphics().drawImage(image, 0, 0, width, height, sx1, sy1, sx2, sy2, null)
        return drawImage(bufferedImage, dx1, dy1, null)
    }

    override fun drawImage(image: Image?, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Int, sy1: Int, sx2: Int, sy2: Int, bgcolor: Color, observer: ImageObserver?): Boolean {
        val saved = paint
        paint = bgcolor
        fillRect(dx1, dy1, dx2 - dx1, dy2 - dy1)
        paint = saved
        return drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer)
    }

    override fun dispose() {
        // TODO: Figure out why this can throw an error
        canvas.restoreToCount(restoreCount)
    }

    private val Shape.path: Path get() {
        val path     = Path()
        val iterator = getPathIterator(null)

        while (!iterator.isDone) {
            when (val segType = iterator.currentSegment(coords)) {
                SEG_MOVETO  -> path.moveTo   (coords[0].toFloat(), coords[1].toFloat())
                SEG_LINETO  -> path.lineTo   (coords[0].toFloat(), coords[1].toFloat())
                SEG_QUADTO  -> path.quadTo   (coords[0].toFloat(), coords[1].toFloat(), coords[2].toFloat(), coords[3].toFloat())
                SEG_CUBICTO -> path.cubicTo  (coords[0].toFloat(), coords[1].toFloat(), coords[2].toFloat(), coords[3].toFloat(), coords[4].toFloat(), coords[5].toFloat())
                SEG_CLOSE   -> path.closePath()
                else        -> throw RuntimeException("Unrecognised segment type $segType")
            }
            iterator.next()
        }

        return path
    }

    /**
     * From a forum post by Jim Moore at: [http://www.jguru.com/faq/view.jsp?EID=114602](http://www.jguru.com/faq/view.jsp?EID=114602)
     */
    private val RenderedImage.bufferedImage: BufferedImage get() {
        if (this is BufferedImage) {
            return this
        }

        val raster = colorModel.createCompatibleWritableRaster(width, height)
        val properties: Hashtable<String, Any?> = Hashtable()

        if (propertyNames != null) {
            for (i in propertyNames.indices) {
                properties[propertyNames[i]] = getProperty(propertyNames[i])
            }
        }

        val result = BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied, properties)

        copyData(raster)

        return result
    }

    private val Image.skia: SkiaImage get() = when (this) {
        is BufferedImage -> this
        else             -> {
            BufferedImage(getWidth(null), getHeight(null), BufferedImage.TYPE_INT_ARGB).apply {
                val graphics = createGraphics()
                graphics.drawImage(this@skia, 0, 0, null)
                graphics.dispose()
            }
        }
    }.toImage()

    private fun awtToSkiaLineCap(cap: Int): PaintStrokeCap = when (cap) {
        CAP_BUTT   -> BUTT
        CAP_ROUND  -> ROUND
        CAP_SQUARE -> SQUARE
        else       -> throw IllegalArgumentException("Unrecognised cap code: $cap")
    }

    private fun awtToSkiaLineJoin(joint: Int): PaintStrokeJoin = when (joint) {
        JOIN_BEVEL -> BEVEL
        JOIN_MITER -> MITER
        JOIN_ROUND -> PaintStrokeJoin.ROUND
        else       -> throw IllegalArgumentException("Unrecognised join code: $joint")
    }

    private val CycleMethod.skia: FilterTileMode get() = when (this) {
        NO_CYCLE -> CLAMP
        REPEAT   -> FilterTileMode.REPEAT
        REFLECT  -> MIRROR
        else     -> CLAMP
    }

    private companion object {
        private const val MIN_LINE_WIDTH = 0.1
    }
}