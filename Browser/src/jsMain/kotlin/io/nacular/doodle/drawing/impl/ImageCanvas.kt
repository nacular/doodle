package io.nacular.doodle.drawing.impl

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
//    private fun visible(stroke: Stroke?, fill: Fill?) = (stroke?.visible ?: false) || (fill?.visible ?: false)
//
//    private fun createPattern(fill: PatternFill): CanvasPattern? = ImageCanvas(htmlFactory.create(), htmlFactory).let {
//        it.size = fill.size
//        fill.fill(it)
//
//        renderingContext.createPattern(it.renderingContext.canvas, "repeat")
//    }
//
//    private fun configureFill(fill: Fill): Boolean {
//        if (!fill.visible) {
//            return false
//        }
//
//        when (fill) {
//            is ColorFill          -> renderingContext.fillColor = fill.color
//            is PatternFill        -> renderingContext.fillStyle = createPattern(fill)
//            is LinearGradientFill -> renderingContext.apply {
//                fillStyle = createLinearGradient(fill.start.x, fill.start.y, fill.end.x, fill.end.y).apply {
//                    fill.colors.forEach {
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
//    private fun present(stroke: Stroke? = null, fill: Fill?, fillRule: FillRule? = null, block: CanvasRenderingContext2D.() -> Unit) {
//        if (visible(stroke, fill)) {
//            renderingContext.beginPath()
//
//            block(renderingContext)
//
//            if (fill != null && configureFill(fill)) {
//                if (fill is PatternFill) {
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
//                if (fill is PatternFill) {
//                    renderingContext.scale(scale, scale)
//                }
//            }
//
//            if (stroke != null) {
//                renderingContext.lineWidth   = stroke.thickness
//                renderingContext.strokeColor = stroke.color
//
//                stroke.dashes?.run { renderingContext.setLineDash(map { it.toDouble() }.toTypedArray()) }
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
//    override fun rect(rectangle: Rectangle,                            fill: Fill ) = rect(rectangle, null,        fill)
//    override fun rect(rectangle: Rectangle,                 stroke: Stroke,  fill: Fill?) = rect(rectangle, stroke as Stroke?, fill)
//    private  fun rect(rectangle: Rectangle,                 stroke: Stroke?, fill: Fill?) = present(stroke, fill) { rectangle.apply { rect(x, y, width, height) } }
//
//    override fun rect(rectangle: Rectangle, radius: Double,            fill: Fill ) = rect(rectangle, radius, null,        fill)
//    override fun rect(rectangle: Rectangle, radius: Double, stroke: Stroke,  fill: Fill?) = rect(rectangle, radius, stroke as Stroke?, fill)
//    private  fun rect(rectangle: Rectangle, radius: Double, stroke: Stroke?, fill: Fill?) = present(stroke, fill) { rectangle.apply { roundedRect(rectangle, radius) } }
//
//    private fun path(block: CanvasRenderingContext2D.() -> Unit) {
//        renderingContext.beginPath()
//
//        block(renderingContext)
//
//        renderingContext.closePath()
//    }
//
//    override fun circle(circle: Circle,            fill: Fill ) = circle(circle, null,        fill)
//    override fun circle(circle: Circle, stroke: Stroke,  fill: Fill?) = circle(circle, stroke as Stroke?, fill)
//    private  fun circle(circle: Circle, stroke: Stroke?, fill: Fill?) = present(stroke, fill) { path { circle.apply { arc(center.x, center.y, radius, 0.0, 2 * PI, false) } } }
//
//    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>,            fill: Fill ) = arc(center, radius, sweep, rotation, null,        fill)
//    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke,  fill: Fill?) = arc(center, radius, sweep, rotation, stroke as Stroke?, fill)
//    private  fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke?, fill: Fill?) = present(stroke, fill) { path { arc(center, radius, sweep, rotation) } }
//    private  fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>                          ) = renderingContext.apply { arc(center.x, center.y, radius, (rotation - 90 * degrees) `in` radians, (sweep - 90 * degrees) `in` radians, false) }
//
//    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>,            fill: Fill ) = wedge(center, radius, sweep, rotation, null,        fill)
//    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke,  fill: Fill?) = wedge(center, radius, sweep, rotation, stroke as Stroke?, fill)
//    private  fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke?, fill: Fill?) = present(stroke, fill) {
//        path {
//            arc(center, radius, sweep, rotation)
//            lineTo(center.x, center.y)
//        }
//    }
//
//    override fun ellipse(ellipse: Ellipse,            fill: Fill ) = ellipse(ellipse, null,        fill)
//    override fun ellipse(ellipse: Ellipse, stroke: Stroke,  fill: Fill?) = ellipse(ellipse, stroke as Stroke?, fill)
//    private  fun ellipse(ellipse: Ellipse, stroke: Stroke?, fill: Fill?) = present(stroke, fill) { path { ellipse.apply { ellipse(center.x, center.y, xRadius, yRadius, 0.0, 0.0, 2 * PI, false) } } }
//
//    override fun text(text: String, font: Font?, at: Point, fill: Fill) {
//        if (text.isEmpty()) {
//            return
//        }
//
//        renderingContext.font = fontSerializer(font)
//
//        text(text, at, fill)
//    }
//
//    private fun text(text: String, at: Point, fill: Fill) {
//        configureFill(fill)
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
//            text(text, at = offset, fill = style.foreground ?: ColorFill(black))
//
//            offset += Point(metrics.width, 0.0)
//        }
//    }
//
//    override fun wrapped(text: String, font: Font?, at: Point, leftMargin: Double, rightMargin: Double, fill: Fill) {
//        StyledText(text, font, foreground = fill).first().let { (text, style) ->
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
//    override fun line(point1: Point, point2: Point, stroke: Stroke) = present(stroke, fill = null) {
//        path {
//            moveTo(point1.x, point1.y)
//            lineTo(point2.x, point2.y)
//        }
//    }
//
//    override fun path(points: List<Point>, stroke: Stroke                                     ) = path(points, stroke as Stroke?, null,  null    )
//    override fun path(points: List<Point>,            fill: Fill,  fillRule: FillRule?) = path(points, null,        fill, fillRule)
//    override fun path(points: List<Point>, stroke: Stroke,  fill: Fill,  fillRule: FillRule?) = path(points, stroke as Stroke?, fill, fillRule)
//    private  fun path(points: List<Point>, stroke: Stroke?, fill: Fill?, fillRule: FillRule?) = present(stroke, fill, fillRule) {
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
//    override fun path(path: Path,            fill: Fill,  fillRule: FillRule?) = path(path, null,        fill, fillRule)
//    override fun path(path: Path, stroke: Stroke                                     ) = path(path, stroke as Stroke?, null,  null    )
//    override fun path(path: Path, stroke: Stroke,  fill: Fill,  fillRule: FillRule?) = path(path, stroke as Stroke?, fill, fillRule)
//    private  fun path(path: Path, stroke: Stroke?, fill: Fill?, fillRule: FillRule?) {
//        // TODO: Unify with present
//        if (visible(stroke, fill)) {
//            val path2d = Path2D(path.data)
//
//            if (fill != null && configureFill(fill)) {
//                if (fill is PatternFill) {
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
//                if (fill is PatternFill) {
//                    renderingContext.scale(scale, scale)
//                }
//            }
//
//            if (stroke != null) {
//                renderingContext.lineWidth   = stroke.thickness
//                renderingContext.strokeColor = stroke.color
//
//                stroke.dashes?.run { renderingContext.setLineDash(map { it.toDouble() }.toTypedArray()) }
//
//                renderingContext.stroke(path2d)
//            }
//        }
//    }
//
//    override fun poly(polygon: ConvexPolygon,            fill: Fill ) = poly(polygon, null, fill)
//    override fun poly(polygon: ConvexPolygon, stroke: Stroke,  fill: Fill?) = poly(polygon, stroke as Stroke?, fill)
//    private  fun poly(polygon: ConvexPolygon, stroke: Stroke?, fill: Fill?) = present(stroke, fill) {
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