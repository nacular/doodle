package io.nacular.doodle.drawing.impl

import JsName
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import io.nacular.doodle.HTMLElement
import io.nacular.doodle.HTMLImageElement
import io.nacular.doodle.Node
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.childAt
import io.nacular.doodle.dom.index
import io.nacular.doodle.dom.numChildren
import io.nacular.doodle.dom.parent
import io.nacular.doodle.dom.remove
import io.nacular.doodle.dom.setBackgroundColor
import io.nacular.doodle.dom.setBorderRadius
import io.nacular.doodle.dom.setColor
import io.nacular.doodle.dom.setOpacity
import io.nacular.doodle.dom.setSize
import io.nacular.doodle.dom.setTransform
import io.nacular.doodle.dom.translate
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.ImagePaint
import io.nacular.doodle.drawing.LinearGradientPaint
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.PatternPaint
import io.nacular.doodle.drawing.Renderer.FillRule
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextFactory
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.ConvexPolygon
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.impl.ImageImpl
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.text.TextSpacing
import io.nacular.doodle.utils.TextAlignment.Start
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.times
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 8/11/19.
 */

class CanvasImplTests {
    init {
        mockkStatic("io.nacular.doodle.dom.ElementKt")
    }

    @Test @JsName("defaultsValid") fun `defaults valid`() {
        mapOf(
                CanvasImpl::size to Size.Empty
        ).forEach { validateDefault(it.key, it.value) }
    }

    @Test @JsName("invisibleToolsNoOp") fun `invisible tools no-op`() {
        val rect = Rectangle(100, 100)
        val circle = Circle(100.0)

        nothingRendered2 { _,      fill -> rect(rect, fill              ) }
        nothingRendered2 { _,      fill -> rect(rect, 10.0, fill        ) }
        nothingRendered2 { stroke, fill -> rect(rect, stroke, fill      ) }
        nothingRendered2 { stroke, fill -> rect(rect, 10.0, stroke, fill) }

        nothingRendered2 { _,      fill -> circle(circle, fill        ) }
        nothingRendered2 { stroke, fill -> circle(circle, stroke, fill) }

        nothingRendered2 { _,      fill -> ellipse(circle, fill        ) }
        nothingRendered2 { stroke, fill -> ellipse(circle, stroke, fill) }

        nothingRendered2 { _, fill -> text   ("text", null, Origin, fill            ) }
        nothingRendered2 { _, fill -> wrapped("text", null, Origin, 0.0, 100.0, fill) }
    }

    @Test @JsName("emptyShapesNoOp") fun `empty shapes no-op`() {
        val stroke = Stroke()
        val fill   = Red.paint
        val rect   = Rectangle.Empty
        val circle = Circle.Empty

        val image  = ImageImpl(mockk<HTMLImageElement>().apply {
            every { offsetWidth  } returns 10
            every { offsetHeight } returns 10
            every { src          } returns "foo.jpg"
        })

        val zeroSizeImage = ImageImpl(mockk<HTMLImageElement>().apply {
            every { offsetWidth  } returns 0
            every { offsetHeight } returns 0
            every { src          } returns "empty.jpg"
        })

        nothingRendered { rect(rect,               fill) }
        nothingRendered { rect(rect, 10.0,         fill) }
        nothingRendered { rect(rect,       stroke, fill) }
        nothingRendered { rect(rect, 10.0, stroke, fill) }

        nothingRendered { circle(circle,         fill) }
        nothingRendered { circle(circle, stroke, fill) }

        nothingRendered { ellipse(circle,         fill) }
        nothingRendered { ellipse(circle, stroke, fill) }

        nothingRendered { text   ("", null, Origin,             fill) }
        nothingRendered { wrapped("", null, Origin, 0.0, 100.0, fill) }

        nothingRendered { image(zeroSizeImage                       ) }
        nothingRendered { image(image, opacity     = 0f             ) }
        nothingRendered { image(image, source      = Rectangle.Empty) }
        nothingRendered { image(image, destination = Rectangle.Empty) }
    }

    @Test @JsName("rendersSimpleRect") fun `renders simple rect`() {
        val fill = Red.paint
        val rect = Rectangle(100, 100)

        validateRender { renderParent, htmlFactory, _, _, _ ->
            val b = mockk<HTMLElement>()
            every { htmlFactory.createOrUse("B", any()) } returns b

            rect(rect, fill)

            val style = b.style

            verify (exactly = 1) { style.setSize           (rect.size                        ) }
            verify (exactly = 1) { style.setBackgroundColor(fill.color                       ) }
            verify               { style.setTransform      (Identity.translate(rect.position)) }

            verify (exactly = 1) { renderParent.appendChild(b) }
        }
    }

    @Test @JsName("recyclesSimpleRect") fun `recycles simple rect`() {
        val rect = Rectangle(100, 100)

        validateRender { renderParent, htmlFactory, _, _, _ ->
            val b = mockk<HTMLElement>()

            every { htmlFactory.createOrUse("B", any()) } answers { b } andThenAnswer { mockk() }
            every { htmlFactory.createOrUse("B", b    ) } returns   b

            listOf(Red, Black, Blue).forEach {
                rect(rect, it.paint)

                clear()
            }

            expect(1) { renderParent.numChildren }
            expect(b) { renderParent.firstChild  }

            val style = b.style

            verifyOrder {
                style.setBackgroundColor(Red  )
                style.setBackgroundColor(Black)
                style.setBackgroundColor(Blue )
            }

            verify(exactly = 1) { htmlFactory.createOrUse("B", null) }
            verify(exactly = 2) { htmlFactory.createOrUse("B", b   ) }
        }
    }

    @Test @JsName("renderComplexRect") fun `renders complex rect`() = validateRender { _,_,_,renderer,_ ->
        val rect = Rectangle(100, 100)

        complexFills.forEach { fill ->
            rect(rect, fill)

            verify { renderer.rect(rect, fill) }
        }
    }

    @Test @JsName("rendersSimpleRoundedRect") fun `renders simple rounded-rect`() {
        val fill   = Red.paint
        val rect   = Rectangle(100, 100)
        val radius = 12.0

        validateRender { renderParent, htmlFactory, _, _, _ ->
            val b = mockk<HTMLElement>()
            every { htmlFactory.createOrUse("B", any()) } returns b

            rect(rect, radius, fill)

            val style = b.style

            verify(exactly = 1) { style.setBorderRadius   (radius                           ) }
            verify(exactly = 1) { style.setSize           (rect.size                        ) }
            verify(exactly = 1) { style.setBackgroundColor(fill.color                       ) }
            verify              { style.setTransform      (Identity.translate(rect.position)) }

            verify(exactly = 1) { renderParent.appendChild(b) }
        }
    }

    @Test @JsName("renderComplexRoundedRect") fun `renders complex rounded rect`() = validateRender { _,_,_,renderer,_ ->
        val rect = Rectangle(100, 100)

        complexFills.forEach { fill ->
            val radius = 12.0
            rect(rect, radius, fill)

            verify { renderer.rect(rect, radius, fill) }
        }
    }

    @Test @JsName("rendersSimpleCircle") fun `renders simple circle`() {
        val fill  = Red.paint
        val circle = Circle(center = Point(10, 10), radius = 100.0)

        validateRender { renderParent, htmlFactory, _, _, _ ->
            val b = mockk<HTMLElement>()
            every { htmlFactory.createOrUse("B", any()) } returns b

            circle(circle, fill)

            val style = b.style

            verify (exactly = 1) { style.setBorderRadius   (circle.radius                                        ) }
            verify (exactly = 1) { style.setSize           (circle.boundingRectangle.size                        ) }
            verify (exactly = 1) { style.setBackgroundColor(fill.color                                          ) }
            verify               { style.setTransform      (Identity.translate(circle.boundingRectangle.position)) }

            verify (exactly = 1) { renderParent.appendChild(b) }
        }
    }

    @Test @JsName("renderComplexCircle") fun `renders complex circle`() = validateRender { _,_,_,renderer,_ ->
        val circle = Circle(center = Point(10, 10), radius = 100.0)

        complexFills.forEach { fill ->
            circle(circle, fill)

            verify { renderer.circle(circle, fill) }
        }
    }

    @Test @JsName("rendersSimpleEllipse") fun `renders simple ellipse`() {
        val fill   = Red.paint
        val ellipse = Ellipse(center = Point(10, 10), xRadius = 100.0, yRadius = 45.0)

        validateRender { renderParent, htmlFactory, _, _, _ ->
            val b = mockk<HTMLElement>()
            every { htmlFactory.createOrUse("B", any()) } returns b

            ellipse(ellipse, fill)

            val style = b.style

            verify (exactly = 1) { style.setBorderRadius   (ellipse.xRadius, ellipse.yRadius                      ) }
            verify (exactly = 1) { style.setSize           (ellipse.boundingRectangle.size                        ) }
            verify (exactly = 1) { style.setBackgroundColor(fill.color                                           ) }
            verify               { style.setTransform      (Identity.translate(ellipse.boundingRectangle.position)) }

            verify (exactly = 1) { renderParent.appendChild(b) }
        }
    }

    @Test @JsName("renderComplexEllipse") fun `renders complex ellipse`() = validateRender { _,_,_,renderer,_ ->
        val ellipse = Ellipse(center = Point(10, 10), xRadius = 100.0, yRadius = 45.0)

        complexFills.forEach { fill ->
            ellipse(ellipse, fill)

            verify { renderer.ellipse(ellipse, fill) }
        }
    }

    @Test @JsName("rendersSimpleText") fun `renders simple text`() {
        val fill = Red.paint
        val text  = "some text"
        val font  = mockk<Font>()
        val at    = Point(34, 89)

        validateRender { renderParent, _, textFactory, _, _ ->
            val t = mockk<HTMLElement>()
            every { textFactory.create(text, font, any(), null) } returns t

            text(text, font, at, fill)

            val style = t.style

            verify (exactly = 1) { style.setOpacity(fill.color.opacity) }
            verify (exactly = 1) { style.setColor  (fill.color        ) }
            verify (exactly = 1) { style.translate (at                ) }

            verify (exactly = 1) { renderParent.appendChild(t) }
        }
    }

    @Test @JsName("rendersComplexText") fun `renders complex text`() = validateRender { _,_,_,renderer,_ ->
        complexFills.forEach { fill ->
            val text  = "some text"
            val font  = mockk<Font>()
            val at    = Point(34, 89)

            text(text, font, at, fill)

            verify { renderer.text(text, font, at, fill, TextSpacing()) }
        }
    }

    @Test @JsName("rendersSimpleStyledText") fun `renders simple styled text`() {
        val text = StyledText("some text")
        val at   = Point(34, 89)

        validateRender { renderParent, _, textFactory, _, _ ->
            val t = mockk<HTMLElement>()
            every { textFactory.create(text, any(), null) } returns t

            text(text, at)

            val style = t.style

            verify (exactly = 1) { style.translate(at) }

            verify (exactly = 1) { renderParent.appendChild(t) }
        }
    }

    @Test @JsName("rendersComplexStyledText") fun `renders complex styled text`() = validateRender { _,_,_,renderer,_ ->
        complexFills.forEach { fill ->
            val text1 = StyledText("some text", foreground = fill)
            val text2 = StyledText("some text", background = fill)
            val at    = Point(34, 89)

            text(text1, at)
            text(text2, at)

            verify { renderer.text(text1, at, TextSpacing()) }
            verify { renderer.text(text2, at, TextSpacing()) }
        }
    }

    @Test @JsName("rendersSimpleWrappedText") fun `renders simple wrapped text`() {
        val fill = Red.paint
        val text = "some text"
        val font = mockk<Font>()
        val at   = Point(150, 89)

        validateRender { renderParent, _, textFactory, _, _ ->
            val t = mockk<HTMLElement>()
            every {
                textFactory.wrapped(
                    text        = text,
                    font        = font,
                    width       = 100.0,
                    indent      = 50.0,
                    alignment   = Start,
                    lineSpacing = 1f,
                    textSpacing = any(),
                    possible    = null
                )
            } returns t

            wrapped(text, font, at, 100.0, 200.0, fill)

            val style = t.style

            verify (exactly = 1) { style.setOpacity(fill.color.opacity) }
            verify (exactly = 1) { style.setColor  (fill.color        ) }
            verify (exactly = 1) { style.translate (at                ) }

            verify (exactly = 1) { renderParent.appendChild(t) }
        }
    }

    @Test @JsName("rendersComplexWrappedText") fun `renders complex wrapped text`() = validateRender { _,_,_,renderer,_ ->
        complexFills.forEach { fill ->
            val text = "some text"
            val font = mockk<Font>()
            val at   = Point(150, 89)

            wrapped(text = text, font = font, at = at, leftMargin = 100.0, rightMargin = 200.0, fill = fill)

            verify(exactly = 1) {
                renderer.wrapped(
                    text        = text,
                    at          = at,
                    width       = 100.0,
                    font        = font,
                    indent      = 50.0,
                    fill       = fill,
                    alignment   = Start,
                    lineSpacing = 1f,
                    textSpacing = TextSpacing()
                )
            }
        }
    }

    @Test @JsName("rendersSimpleWrappedStyledText") fun `renders simple wrapped styled text`() {
        val text  = StyledText("some text")
        val at    = Point(150, 89)

        validateRender { renderParent, _, textFactory, _, _ ->
            val t = mockk<HTMLElement>()
            every {
                textFactory.wrapped(
                    text        = text,
                    width       = 100.0,
                    indent      = 50.0,
                    alignment   = Start,
                    lineSpacing = 1f,
                    textSpacing = TextSpacing(),
                    possible    = null
                )
            } returns t

            wrapped(text, at, 100.0, 200.0)

            val style = t.style

            verify(exactly = 1) { style.translate(at)         }
            verify(exactly = 1) { renderParent.appendChild(t) }
        }
    }

    @Test @JsName("rendersComplexWrappedStyledText") fun `renders complex wrapped styled text`() = validateRender { _,_,_,renderer,_ ->
        complexFills.forEach { fill ->
            val text1 = StyledText("some text", foreground = fill)
            val text2 = StyledText("some text", background = fill)
            val at    = Point(150, 89)

            wrapped(text1, at, 100.0, 200.0)
            wrapped(text2, at, 100.0, 200.0)

            verify(exactly = 1) {
                renderer.wrapped(
                    text        = text1,
                    at          = at,
                    width       = 100.0,
                    indent      =  50.0,
                    alignment   = Start,
                    lineSpacing = 1f,
                    textSpacing = TextSpacing()
                )
            }
            verify(exactly = 1) {
                renderer.wrapped(
                    text        = text2,
                    at          = at,
                    width       = 100.0,
                    indent      = 50.0,
                    alignment   = Start,
                    lineSpacing = 1f,
                    textSpacing = TextSpacing()
                )
            }
        }
    }

    @Test @JsName("rendersLines") fun `renders lines`() = validateRender { _,_,_,renderer,_ ->
            val start  = Point(3, 6)
            val end    = Point(150, 89)
            val stroke = mockk<Stroke>()

            line(start, end, stroke)

            verify(exactly = 1) { renderer.line(start, end, stroke) }
    }

    @Test @JsName("rendersPathPoints") fun `renders path points`() = validateRender { _,_,_,renderer,_ ->
        val points   = listOf(Point(3, 6), Point(150, 89))
        val fill     = mockk<Paint>()
        val stroke   = mockk<Stroke>()
        val fillRule = mockk<FillRule>()

        path(points, stroke                )
        path(points, stroke, fill, null    )
        path(points, stroke, fill, fillRule)
        path(points,         fill, null    )
        path(points,         fill, fillRule)

        verify(exactly = 1) { renderer.path(points, stroke                ) }
        verify(exactly = 1) { renderer.path(points, stroke, fill, null    ) }
        verify(exactly = 1) { renderer.path(points, stroke, fill, fillRule) }
        verify(exactly = 1) { renderer.path(points,         fill, null    ) }
        verify(exactly = 1) { renderer.path(points,         fill, fillRule) }
    }

    @Test @JsName("rendersPath") fun `renders path`() = validateRender { _,_,_,renderer,_ ->
        val path     = mockk<Path>    ()
        val fill     = mockk<Paint>   ()
        val stroke   = mockk<Stroke>  ()
        val fillRule = mockk<FillRule>()

        path(path, stroke                )
        path(path, stroke, fill, null    )
        path(path, stroke, fill, fillRule)
        path(path,         fill, null    )
        path(path,         fill, fillRule)

        verify(exactly = 1) { renderer.path(path, stroke                ) }
        verify(exactly = 1) { renderer.path(path, stroke, fill, null    ) }
        verify(exactly = 1) { renderer.path(path, stroke, fill, fillRule) }
        verify(exactly = 1) { renderer.path(path,         fill, null    ) }
        verify(exactly = 1) { renderer.path(path,         fill, fillRule) }
    }

    @Test @JsName("rendersPoly") fun `renders poly`() = validateRender { _,_,_,renderer,_ ->
        val polygon  = ConvexPolygon(Point(3, 6), Point(150, 89), Origin)
        val fill     = mockk<Paint>()
        val stroke   = mockk<Stroke>()

        poly(polygon, stroke      )
        poly(polygon, stroke, null)
        poly(polygon,         fill)

        verify(exactly = 2) { renderer.poly(polygon, stroke, null) }
        verify(exactly = 1) { renderer.poly(polygon,         fill) }
    }

    @Test @JsName("rendersArc") fun `renders arc`() = validateRender { _,_,_,renderer,_ ->
        val center   = Point(3, 6)
        val radius   = 59.3
        val sweep    = 42 * degrees
        val rotation = 12 * degrees
        val fill     = mockk<Paint>()
        val stroke   = mockk<Stroke>()

        arc(center, radius, sweep, rotation,         fill)
        arc(center, radius, sweep, rotation, stroke      )
        arc(center, radius, sweep, rotation, stroke, null)
        arc(center, radius, sweep, rotation, stroke, fill)

        verify(exactly = 2) { renderer.arc(center, radius, sweep, rotation, stroke, null) }
        verify(exactly = 1) { renderer.arc(center, radius, sweep, rotation,         fill) }
        verify(exactly = 1) { renderer.arc(center, radius, sweep, rotation, stroke, fill) }
    }

    @Test @JsName("rendersWedge") fun `renders wedge`() = validateRender { _,_,_,renderer,_ ->
        val center   = Point(3, 6)
        val radius   = 59.3
        val sweep    = 42 * degrees
        val rotation = 12 * degrees
        val fill     = mockk<Paint>()
        val stroke   = mockk<Stroke>()

        wedge(center, radius, sweep, rotation,         fill)
        wedge(center, radius, sweep, rotation, stroke      )
        wedge(center, radius, sweep, rotation, stroke, null)
        wedge(center, radius, sweep, rotation, stroke, fill)

        verify(exactly = 2) { renderer.wedge(center, radius, sweep, rotation, stroke, null) }
        verify(exactly = 1) { renderer.wedge(center, radius, sweep, rotation,         fill) }
        verify(exactly = 1) { renderer.wedge(center, radius, sweep, rotation, stroke, fill) }
    }

    @Test @JsName("rendersImage") fun `renders image`() {
        val image = mockk<ImageImpl>().apply {
            every { size } returns Size(130.0, 46.0)
        }
        val at    = Point(150, 89)

        validateRender { renderParent, _, _, _, _ ->
            val clone = mockk<HTMLImageElement>()
            val img   = mockk<HTMLImageElement>().apply {
                every { cloneNode(false) } returns clone
            }

            every { image.image } returns img

            image(image, Rectangle(position = at, size = image.size))

            val style = clone.style
            val size  = image.size

            verify (exactly = 1) { style.setOpacity(1f  ) }
            verify (exactly = 1) { style.setSize   (size) }
            verify (exactly = 1) { style.translate (at  ) }

            verify (exactly = 1) { renderParent.appendChild(clone) }
        }
    }

    @Test @JsName("ignoresUnknownImageType") fun `ignores unknown image type`() {
        val image = mockk<Image>().apply {
            every { size } returns Size(130.0, 46.0)
        }
        val at    = Point(150, 89)

        validateRender { renderParent, _, _, _, _ ->
            image(image, Rectangle(position = at, size = image.size))

            verify { renderParent wasNot Called }
        }
    }

    @Test @JsName("transformsWork") fun `transforms work`() {
        listOf<CanvasImpl.() -> AffineTransform>(
            // TODO: Verify other forms for these APIs
            { scale           (      10.0, 4.5 )         {}; Identity.scale    (10.0, 4.5   ) },
            { rotate          (23 * degrees    )         {}; Identity.rotate   (23 * degrees) },
            { translate       (Point(10.0, 4.5))         {}; Identity.translate(10.0,  4.5  ) },
            { transform       (Identity.skew(46.0, 0.0)) {}; Identity.skew     (46.0,  0.0  ) },
            { flipVertically                             {}; Identity.scale    ( 1.0, -1.0  ) },
            { flipHorizontally                           {}; Identity.scale    (-1.0,  1.0  ) }
        ).forEach {
            validateTransform(it)
        }
    }

    @Test @JsName("clearWorks") fun `clear works`() = validateRender { _, _, _, renderer, _ ->
        rect(Rectangle(10, 10), Red.paint)

        clear()

        verify(exactly = 1) { renderer.clear() }
    }

    @Test @JsName("clearThenFlushWorks") fun `clear then flush works`() = validateRender { renderParent, _, _, renderer, _ ->
        rect(Rectangle(10, 10), Red.paint)
        text("hello", Origin, Black.paint)

        expect(2) { renderParent.numChildren }

        clear()
        flush()

        verifyOrder {
            renderer.clear()
            renderer.flush()
        }

        expect(0) { renderParent.numChildren }
    }

    @Test @JsName("clearAllowsNewContent") fun `clear allows new content`() = validateRender { renderParent, htmlFactory, textFactory,_,_ ->
        val hello = mockk<HTMLElement>()
        val world = mockk<HTMLElement>()
        val r1    = mockk<HTMLElement>()
        val r2    = mockk<HTMLElement>()

        every { htmlFactory.createOrUse("B", any()) } returns r1 andThen r2

        every { textFactory.create("hello", any(), any(), any()) } returns hello
        every { textFactory.create("world", any(), any(), any()) } returns world

        rect(Rectangle(10, 10), Red.paint)
        text("world", Origin, Black.paint)
        rect(Rectangle(100, 10), Red.paint)

        expect(3) { renderParent.numChildren }

        expect(r1   ) { renderParent.childAt(0) }
        expect(world) { renderParent.childAt(1) }
        expect(r2   ) { renderParent.childAt(2) }

        clear()

        text("hello", Origin, Black.paint) // Replaces first node
        text("world", Origin, Black.paint)

        expect(3) { renderParent.numChildren }

        expect(hello) { renderParent.childAt(0) }
        expect(world) { renderParent.childAt(1) }
        expect(r2   ) { renderParent.childAt(2) }

        flush()

        expect(2) { renderParent.numChildren }

        expect(hello) { renderParent.childAt(0) }
        expect(world) { renderParent.childAt(1) }
    }

    private val complexFills = listOf(
            mockk<Paint>              ().apply { every { visible } returns true },
            mockk<ImagePaint>         ().apply { every { visible } returns true },
            mockk<PatternPaint>       ().apply { every { visible } returns true },
            mockk<LinearGradientPaint>().apply { every { visible } returns true }
    )

    private fun validateTransform(block: CanvasImpl.() -> AffineTransform) {
        val htmlFactory  = mockk<HtmlFactory>()
        val renderParent = spyk<HTMLElement> ()

        canvas(renderParent, htmlFactory).apply {
            val frame = mockk<HTMLElement>().apply { every { parentNode } returns null andThen renderParent }

            every { htmlFactory.createOrUse("B", any()) } returns frame

            val transform = block(this)
            val style     = frame.style

            verify(exactly = 1) { style.setTransform(transform) }
            verify(exactly = 1) { renderParent.appendChild(frame) }
        }
    }

    private fun validateRender(block: CanvasImpl.(
            renderParent   : HTMLElement,
            htmlFactory    : HtmlFactory,
            textFactory    : TextFactory,
            renderer       : VectorRenderer,
            rendererFactory: VectorRendererFactory) -> Unit) {
        val renderer        = mockk<VectorRenderer>()
        val htmlFactory     = mockk<HtmlFactory>   ()
        val textFactory     = mockk<TextFactory>   ()
        val textMetrics     = mockk<TextMetrics>   ()
        val renderParent    = htmlElement          ()
        val rendererFactory = rendererFactory      (renderer)

        canvas(renderParent, htmlFactory, textFactory, textMetrics, rendererFactory).apply {
            block(this, renderParent, htmlFactory, textFactory, renderer, rendererFactory)
        }
    }

    private fun htmlElement() = mockk<HTMLElement>().apply {
        val node     = slot<Node>()
        val needle   = slot<Node>()
        val index    = slot<Int> ()
        val children = mutableListOf<Node>()

        val mockAdd: (Node) -> Unit = { n ->
            every { n.parent      } returns this@apply
            every { n.nextSibling } answers {
                children.indexOf(n).takeIf { it >= 0 }?.let {
                    children.getOrNull(it + 1)
                }
            }
        }

        every { add(capture(node)) } answers {
            node.captured.also { n ->
                children += n

                mockAdd(n)
            }
        }
        every { remove(capture(node)) } answers {
            node.captured.also {
                children -= it

                every { it.parent      } returns null
                every { it.nextSibling } returns null
            }
        }
//        every { removeChild(capture(node)) } answers {
//            node.captured.also {
//                children -= it
//
//                every { it.parent      } returns null
//                every { it.nextSibling } returns null
//            }
//        }
        every { index(capture(node)) } answers {
            children.indexOf(node.captured)
        }
        every { firstChild } answers {
            children.firstOrNull()
        }
        every { numChildren } answers {
            children.size
        }
        every { index(capture(node)) } answers {
            children.indexOf(node.captured)
        }
        every { childAt(capture(index)) } answers {
            children.getOrNull(index.captured)
        }
        every { replaceChild(capture(node), capture(needle)) } answers {
            needle.captured.also {
                val i = children.indexOf(it)

                when {
                    i >= 0 -> children[i] = node.captured.also(mockAdd)
                }
            }
        }
    }

    private fun nothingRendered(block: CanvasImpl.() -> Unit) {
        val renderParent = spyk<HTMLElement>()
        val renderer     = mockk<VectorRenderer>()

        canvas(renderParent, rendererFactory = rendererFactory(renderer)).apply {
            block(this)
        }

//        verify { renderer wasNot Called }
        verify(exactly = 0) { renderParent.appendChild(any()) }
    }

    private fun nothingRendered2(block: CanvasImpl.(Stroke, Paint) -> Unit) {
        val renderParent = spyk<HTMLElement>()
        val renderer     = mockk<VectorRenderer>()

        canvas(renderParent, rendererFactory = rendererFactory(renderer)).apply {
            block(this, mockk<Stroke>().apply { every { visible } returns false }, mockk<Paint>().apply { every { visible } returns false })
        }

//        verify { renderer wasNot Called }
        verify(exactly = 0) { renderParent.appendChild(any()) }
    }

    private fun rendererFactory(renderer: VectorRenderer = mockk()) = mockk<VectorRendererFactory>().apply {
        every { this@apply.invoke(any()) } returns renderer
    }

    private fun canvas(renderParent   : HTMLElement           = mockk(),
                       htmlFactory    : HtmlFactory           = mockk(),
                       textFactory    : TextFactory           = mockk(),
                       textMetrics    : TextMetrics           = mockk(),
                       rendererFactory: VectorRendererFactory = rendererFactory()) = CanvasImpl(
        renderParent    = renderParent,
        htmlFactory     = htmlFactory,
        textFactory     = textFactory,
        textMetrics     = textMetrics,
        useShadowHack   = false,
        rendererFactory = rendererFactory
    )

    private fun <T> validateDefault(p: KProperty1<CanvasImpl, T>, default: T?) {
        expect(default, "$p defaults to $default") { p.get(canvas()) }
    }
}