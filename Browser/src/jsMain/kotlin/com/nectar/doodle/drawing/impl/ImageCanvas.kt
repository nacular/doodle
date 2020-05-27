package com.nectar.doodle.drawing.impl

/**
 * Created by Nicholas Eddy on 6/22/19.
 */
//class ImageCanvas(renderParent: HTMLElement, private val htmlFactory: HtmlFactory, private val handleRetina: Boolean = true): Canvas {
//    private interface CSSFontSerializer {
//        operator fun invoke(font: Font?): String
//    }
//
//    private class CSSFontSerializerImpl(htmlFactory: HtmlFactory): CSSFontSerializer {
//        private val element = htmlFactory.create<HTMLElement>()
//
//        override fun invoke(font: Font?): String = when {
//            font != null -> element.run {
//                style.setFontSize  (font.size  )
//                style.setFontFamily(font.family.toLowerCase())
//                style.setFontWeight(font.weight)
//
//                style.run { "$fontStyle $fontVariant $fontWeight $fontSize $fontFamily" }
//            }
//            else -> "$defaultFontWeight ${defaultFontSize}px $defaultFontFamily"
//        }
//    }
//
////    val image: Image get() = object: Image {
////        override val size   = this@ImageCanvas.size
////        override val source = this@ImageCanvas.renderingContext.canvas.toDataURL("image/png", 1.0)
////    }
//
//    override var size = Empty
//        set(new) {
//            field = new
//
//            renderingContext.canvas.apply {
//                style.setSize(new)
//
//                width  = (new.width  * scale).toInt()
//                height = (new.height * scale).toInt()
//            }
//
//            renderingContext.scale(scale, scale)
//
//            currentTransform = Identity.scale(scale, scale)
//        }
//
//
//    private val scale get() = if (handleRetina) window.devicePixelRatio else 1.0 // Address issues on Retina displays
//
//    private val renderingContext: CanvasRenderingContext2D = htmlFactory.create<HTMLCanvasElement>("canvas").getContext("2d") as CanvasRenderingContext2D
//
//    private var currentTransform = Identity
//
//    private val fontSerializer = CSSFontSerializerImpl(htmlFactory)
//
//    private val shadows = mutableListOf<Shadow>()
//
//    init {
//        renderParent.add(renderingContext.canvas)
//    }
//
//    override fun clear() = renderingContext.clearRect(0.0, 0.0, size.width, size.height)
//
//    override fun flush() {}
//
//    override fun scale(x: Double, y: Double, block: Canvas.() -> Unit) = scale((size / 2.0).run { Point(width, height) }, x, y, block)
//
//    override fun scale(around: Point, x: Double, y: Double, block: Canvas.() -> Unit) = when {
//        x == 1.0 && y == 1.0 -> block()
//        else                 -> transform(Identity.translate(around).scale(x, y).translate(-around), block)
//    }
//
//    override fun rotate(by    : Measure<Angle>,                     block: Canvas.() -> Unit) = rotate((size / 2.0).run { Point(width, height) }, by, block)
//    override fun rotate(around: Point,          by: Measure<Angle>, block: Canvas.() -> Unit) {
//        transform(Identity.translate(around).rotate(by).translate(-around), block)
//    }
//
//    override fun transform(transform: AffineTransform, block: Canvas.() -> Unit) = when (transform.isIdentity) {
//        true -> block()
//        else -> {
//            val old = currentTransform
//
//            transform.apply { renderingContext.transform(scaleX, shearY, shearX, scaleY, translateX, translateY) }
//
//            block(this)
//
//            old.apply { renderingContext.setTransform(scaleX, shearY, shearX, scaleY, translateX, translateY) }
//
//            Unit
//        }
//    }
//
//    private fun visible(pen: Pen?, brush: Brush?) = (pen?.visible ?: false) || (brush?.visible ?: false)
//
//    private fun createPattern(brush: PatternBrush): CanvasPattern? = ImageCanvas(htmlFactory.create(), htmlFactory).let {
//        it.size = brush.size
//        brush.fill(it)
//
//        renderingContext.createPattern(it.renderingContext.canvas, "repeat")
//    }
//
//    private fun configureFill(brush: Brush): Boolean {
//        if (!brush.visible) {
//            return false
//        }
//
//        when (brush) {
//            is ColorBrush          -> renderingContext.fillColor = brush.color
//            is PatternBrush        -> renderingContext.fillStyle = createPattern(brush)
//            is LinearGradientBrush -> renderingContext.apply {
//                fillStyle = createLinearGradient(brush.start.x, brush.start.y, brush.end.x, brush.end.y).apply {
//                    brush.colors.forEach {
//                        addColorStop(it.offset.toDouble(), it.color.rgbaString)
//                    }
//                }
//            }
//            else                   -> return false
//        }
//
//        return true
//    }
//
//    private fun shadows(block: CanvasRenderingContext2D.() -> Unit): Sequence<HTMLCanvasElement> = shadows.asSequence().map {
//        val tempCanvas = ImageCanvas(htmlFactory.create(), htmlFactory).also { it.size = size }
//
//        tempCanvas.renderingContext.run {
//            save()
//
//            /*
//             * Shadows require a stroked/filled path to be visible.  This offset moves that shadow path out of view so it doesn't show up after the shadow
//             * canvas is composited.
//             */
//            val offset = 100000.0
//            translate(offset, 0.0)
//
//            beginPath()
//
//            block(this)
//
//            closePath()
//
//            shadowBlur    = it.blurRadius
//            shadowColor   = it.color.rgbaString
//            shadowOffsetX = it.horizontal - 2 * offset
//            shadowOffsetY = it.vertical
//            lineWidth     = 0.5
//
//            stroke()
//
//            globalCompositeOperation = when (it) {
//                is InnerShadow -> "destination-out"
//                else           -> "source-out"
//            }
//
//            restore()
//
//            canvas
//        }
//    }
//
//    private fun present(pen: Pen? = null, brush: Brush?, fillRule: FillRule? = null, block: CanvasRenderingContext2D.() -> Unit) {
//        if (visible(pen, brush)) {
//            renderingContext.beginPath()
//
//            block(renderingContext)
//
//            if (brush != null && configureFill(brush)) {
//                if (brush is PatternBrush) {
//                    renderingContext.resetTransform()
//                }
//
//                when (fillRule) {
//                    null -> renderingContext.fill()
//                    else -> renderingContext.fill(fillRule.let {
//                        when (it) {
//                            FillRule.EvenOdd -> CanvasFillRule.EVENODD
//                            else             -> CanvasFillRule.NONZERO
//                        }
//                    })
//                }
//
//                if (brush is PatternBrush) {
//                    renderingContext.scale(scale, scale)
//                }
//            }
//
//            if (pen != null) {
//                renderingContext.lineWidth   = pen.thickness
//                renderingContext.strokeColor = pen.color
//
//                pen.dashes?.run { renderingContext.setLineDash(map { it.toDouble() }.toTypedArray()) }
//
//                renderingContext.stroke()
//            }
//            renderingContext.closePath()
//
//            shadows(block).forEach {
//                renderingContext.resetTransform()
//                renderingContext.drawImage(it, 0.0, 0.0)
//                renderingContext.scale(scale, scale)
//            }
//        }
//    }
//
//    override fun rect(rectangle: Rectangle,                            brush: Brush ) = rect(rectangle, null,        brush)
//    override fun rect(rectangle: Rectangle,                 pen: Pen,  brush: Brush?) = rect(rectangle, pen as Pen?, brush)
//    private  fun rect(rectangle: Rectangle,                 pen: Pen?, brush: Brush?) = present(pen, brush) { rectangle.apply { rect(x, y, width, height) } }
//
//    override fun rect(rectangle: Rectangle, radius: Double,            brush: Brush ) = rect(rectangle, radius, null,        brush)
//    override fun rect(rectangle: Rectangle, radius: Double, pen: Pen,  brush: Brush?) = rect(rectangle, radius, pen as Pen?, brush)
//    private  fun rect(rectangle: Rectangle, radius: Double, pen: Pen?, brush: Brush?) = present(pen, brush) { rectangle.apply { roundedRect(rectangle, radius) } }
//
//    private fun path(block: CanvasRenderingContext2D.() -> Unit) {
//        renderingContext.beginPath()
//
//        block(renderingContext)
//
//        renderingContext.closePath()
//    }
//
//    override fun circle(circle: Circle,            brush: Brush ) = circle(circle, null,        brush)
//    override fun circle(circle: Circle, pen: Pen,  brush: Brush?) = circle(circle, pen as Pen?, brush)
//    private  fun circle(circle: Circle, pen: Pen?, brush: Brush?) = present(pen, brush) { path { circle.apply { arc(center.x, center.y, radius, 0.0, 2 * PI, false) } } }
//
//    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>,            brush: Brush ) = arc(center, radius, sweep, rotation, null,        brush)
//    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, pen: Pen,  brush: Brush?) = arc(center, radius, sweep, rotation, pen as Pen?, brush)
//    private  fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, pen: Pen?, brush: Brush?) = present(pen, brush) { path { arc(center, radius, sweep, rotation) } }
//    private  fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>                          ) = renderingContext.apply { arc(center.x, center.y, radius, (rotation - 90 * degrees) `in` radians, (sweep - 90 * degrees) `in` radians, false) }
//
//    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>,            brush: Brush ) = wedge(center, radius, sweep, rotation, null,        brush)
//    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, pen: Pen,  brush: Brush?) = wedge(center, radius, sweep, rotation, pen as Pen?, brush)
//    private  fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, pen: Pen?, brush: Brush?) = present(pen, brush) {
//        path {
//            arc(center, radius, sweep, rotation)
//            lineTo(center.x, center.y)
//        }
//    }
//
//    override fun ellipse(ellipse: Ellipse,            brush: Brush ) = ellipse(ellipse, null,        brush)
//    override fun ellipse(ellipse: Ellipse, pen: Pen,  brush: Brush?) = ellipse(ellipse, pen as Pen?, brush)
//    private  fun ellipse(ellipse: Ellipse, pen: Pen?, brush: Brush?) = present(pen, brush) { path { ellipse.apply { ellipse(center.x, center.y, xRadius, yRadius, 0.0, 0.0, 2 * PI, false) } } }
//
//    override fun text(text: String, font: Font?, at: Point, brush: Brush) {
//        if (text.isEmpty()) {
//            return
//        }
//
//        renderingContext.font = fontSerializer(font)
//
//        text(text, at, brush)
//    }
//
//    private fun text(text: String, at: Point, brush: Brush) {
//        configureFill(brush)
//
//        renderingContext.textBaseline = CanvasTextBaseline.TOP
//
//        renderingContext.fillText(text, at.x, at.y)
//    }
//
//    override fun text(text: StyledText, at: Point) {
//        var offset = at
//
//        text.forEach { (text, style) ->
//            renderingContext.font = fontSerializer(style.font)
//
//            val metrics = renderingContext.measureText(text)
//
//            style.background?.let {
//                rect(Rectangle(position = offset, size = Size(metrics.width, (style.font?.size ?: defaultFontSize).toDouble())), it)
//            }
//
//            text(text, at = offset, brush = style.foreground ?: ColorBrush(black))
//
//            offset += Point(metrics.width, 0.0)
//        }
//    }
//
//    override fun wrapped(text: String, font: Font?, at: Point, leftMargin: Double, rightMargin: Double, brush: Brush) {
//        StyledText(text, font, foreground = brush).first().let { (text, style) ->
//            wrappedText(text, style, at, leftMargin, rightMargin)
//        }
//    }
//
//    override fun wrapped(text: StyledText, at: Point, leftMargin: Double, rightMargin: Double) {
//        var offset = at
//
//        text.forEach { (text, style) ->
//            offset = wrappedText(text, style, offset, leftMargin, rightMargin)
//        }
//    }
//
//    private fun wrappedText(text: String, style: Style, at: Point, leftMargin: Double, rightMargin: Double): Point {
//        val lines        = mutableListOf<Pair<String, Point>>()
//        val words        = text.splitMatches("""\s""".toRegex())
//        var line         = ""
//        var lineTest     : String
//        var currentPoint = at
//        var endX         = currentPoint.x
//
//        renderingContext.font = fontSerializer(style.font)
//
//        words.forEach { (word, delimiter) ->
//            lineTest = line + word + delimiter
//
//            val metric = renderingContext.measureText(lineTest)
//
//            endX = currentPoint.x + metric.width
//
//            if (endX > rightMargin) {
//                lines += line to currentPoint
//                line   = word + delimiter
//
//                currentPoint = Point(leftMargin, at.y + lines.size * (1 + (style.font?.size ?: defaultFontSize).toDouble()))
//                endX         = leftMargin
//            } else {
//                line = lineTest
//            }
//        }
//
//        if (line.isNotEmpty()) {
//            endX   = currentPoint.x + renderingContext.measureText(line).width
//            lines += line to currentPoint
//        }
//
//        lines.forEach { (text, at) ->
//            text(StyledText(text, style.font, foreground = style.foreground, background = style.background), at)
//        }
//
//        return Point(endX, currentPoint.y)
//    }
//
//    override fun image(image: Image, destination: Rectangle, opacity: Float, radius: Double, source: Rectangle) {
//        if (image is ImageImpl && opacity > 0 && !destination.empty) {
//            if (radius > 0) {
//                renderingContext.roundedRect(destination, radius)
//
//                renderingContext.clip()
//
//                drawImage(image, Rectangle(size = image.size), destination, opacity)
//
//                renderingContext.resetClip()
//            } else {
//                drawImage(image, Rectangle(size = image.size), destination, opacity)
//            }
//        }
//    }
//
//    private fun drawImage(image: ImageImpl, source: Rectangle, destination: Rectangle, opacity: Float) {
//        renderingContext.globalAlpha = opacity.toDouble()
//
//        renderingContext.drawImage(image.image,
//                sx = source.x,      sy = source.y,      sw = source.width,      sh = source.height,
//                dx = destination.x, dy = destination.y, dw = destination.width, dh = destination.height)
//
//        renderingContext.globalAlpha = 1.0
//    }
//
//    override fun clip(rectangle: Rectangle, radius: Double, block: Canvas.() -> Unit) {
//        when {
//            radius > 0.0 -> renderingContext.roundedRect(rectangle, radius)
//            else         -> rectangle.apply { renderingContext.rect(x, y, width, height) }
//        }
//
//        renderingContext.save()
//        renderingContext.clip()
//
//        block(this)
//
//        renderingContext.restore()
//    }
//
//    override fun shadow(shadow: Shadow, block: Canvas.() -> Unit) {
//        shadows += shadow
//
//        apply(block)
//
//        shadows -= shadow
//    }
//
//    override fun line(point1: Point, point2: Point, pen: Pen) = present(pen, brush = null) {
//        path {
//            moveTo(point1.x, point1.y)
//            lineTo(point2.x, point2.y)
//        }
//    }
//
//    override fun path(points: List<Point>, pen: Pen                                     ) = path(points, pen as Pen?, null,  null    )
//    override fun path(points: List<Point>,            brush: Brush,  fillRule: FillRule?) = path(points, null,        brush, fillRule)
//    override fun path(points: List<Point>, pen: Pen,  brush: Brush,  fillRule: FillRule?) = path(points, pen as Pen?, brush, fillRule)
//    private  fun path(points: List<Point>, pen: Pen?, brush: Brush?, fillRule: FillRule?) = present(pen, brush, fillRule) {
//        path {
//            points.firstOrNull()?.apply {
//                moveTo(x, y)
//            }
//            points.drop(1).forEach {
//                lineTo(it.x, it.y)
//            }
//        }
//    }
//
//    override fun path(path: Path,            brush: Brush,  fillRule: FillRule?) = path(path, null,        brush, fillRule)
//    override fun path(path: Path, pen: Pen                                     ) = path(path, pen as Pen?, null,  null    )
//    override fun path(path: Path, pen: Pen,  brush: Brush,  fillRule: FillRule?) = path(path, pen as Pen?, brush, fillRule)
//    private  fun path(path: Path, pen: Pen?, brush: Brush?, fillRule: FillRule?) {
//        // TODO: Unify with present
//        if (visible(pen, brush)) {
//            val path2d = Path2D(path.data)
//
//            if (brush != null && configureFill(brush)) {
//                if (brush is PatternBrush) {
//                    renderingContext.resetTransform()
//                }
//
//                when (fillRule) {
//                    null -> renderingContext.fill(path2d)
//                    else -> renderingContext.fill(path2d, fillRule.let {
//                        when (it) {
//                            FillRule.EvenOdd -> CanvasFillRule.EVENODD
//                            else             -> CanvasFillRule.NONZERO
//                        }
//                    })
//                }
//
//                if (brush is PatternBrush) {
//                    renderingContext.scale(scale, scale)
//                }
//            }
//
//            if (pen != null) {
//                renderingContext.lineWidth   = pen.thickness
//                renderingContext.strokeColor = pen.color
//
//                pen.dashes?.run { renderingContext.setLineDash(map { it.toDouble() }.toTypedArray()) }
//
//                renderingContext.stroke(path2d)
//            }
//        }
//    }
//
//    override fun poly(polygon: ConvexPolygon,            brush: Brush ) = poly(polygon, null, brush)
//    override fun poly(polygon: ConvexPolygon, pen: Pen,  brush: Brush?) = poly(polygon, pen as Pen?, brush)
//    private  fun poly(polygon: ConvexPolygon, pen: Pen?, brush: Brush?) = present(pen, brush) {
//        path {
//            polygon.points.firstOrNull()?.apply {
//                moveTo(x, y)
//            }
//            polygon.points.drop(1).forEach {
//                lineTo(it.x, it.y)
//            }
//        }
//    }
//}
//
//private fun CanvasRenderingContext2D.roundedRect(rectangle: Rectangle, radius: Double) {
//    rectangle.apply {
//        val r = when {
//            width  < 2 * radius -> width  / 2
//            height < 2 * radius -> height / 2
//            else                -> radius
//        }
//
//        beginPath()
//        moveTo(x + radius, y)
//        arcTo (x + width, y,          x + width, y + height, r)
//        arcTo (x + width, y + height, x,         y + height, r)
//        arcTo (x,         y + height, x,         y,          r)
//        arcTo (x,         y,          x + width, y,          r)
//        closePath()
//    }
//}
//
//private var CanvasRenderingContext2D.strokeColor: Color
//    get() = black
//    set(new) {
//        this.strokeStyle = new.rgbaString
//    }
//
//private var CanvasRenderingContext2D.fillColor: Color
//    get() = black
//    set(new) {
//        this.fillStyle = new.rgbaString
//    }