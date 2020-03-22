package com.nectar.doodle.drawing.impl

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.HTMLImageElement
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.setBackgroundColor
import com.nectar.doodle.dom.setBorderRadius
import com.nectar.doodle.dom.setColor
import com.nectar.doodle.dom.setOpacity
import com.nectar.doodle.dom.setSize
import com.nectar.doodle.dom.setTransform
import com.nectar.doodle.dom.translate
import com.nectar.doodle.drawing.AffineTransform
import com.nectar.doodle.drawing.AffineTransform.Companion.Identity
import com.nectar.doodle.drawing.Brush
import com.nectar.doodle.drawing.Color.Companion.red
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Ellipse
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.image.impl.ImageImpl
import com.nectar.doodle.text.StyledText
import com.nectar.measured.units.Angle.Companion.degrees
import com.nectar.measured.units.times
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlin.js.JsName
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 8/11/19.
 */

@Suppress("FunctionName")
class CanvasImplTests {
    @Test @JsName("defaultsValid") fun `defaults valid`() {
        mapOf(
                CanvasImpl::size to Size.Empty
        ).forEach { validateDefault(it.key, it.value) }
    }

    @Test @JsName("invisibleToolsNoOp") fun `invisible tools no-op`() {
        val rect = Rectangle(100, 100)
        val circle = Circle(100.0)

        listOf<CanvasImpl.(Pen, Brush) -> Unit>(
                { _, brush -> rect(rect, brush) },
                { _, brush -> rect(rect, 10.0, brush) },
                { pen, brush -> rect(rect, pen, brush) },
                { pen, brush -> rect(rect, 10.0, pen, brush) },

                { _, brush -> circle(circle, brush) },
                { pen, brush -> circle(circle, pen, brush) },

                { _, brush -> ellipse(circle, brush) },
                { pen, brush -> ellipse(circle, pen, brush) },

                { _, brush -> text("text", null, Point.Origin, brush) },
                { _, brush -> wrapped("text", null, Point.Origin, 0.0, 100.0, brush) }
        ).forEach {
            nothingRendered(it)
        }
    }

//    @Test @JsName("emptyShapesNoOp")
//    fun `empty shapes no-op`() {
//        val pen    = Pen()
//        val brush  = ColorBrush(red)
//        val rect   = Rectangle.Empty
//        val circle = Circle.Empty
//
//        listOf<CanvasImpl.() -> Unit>(
//            { rect(rect,            brush) },
//            { rect(rect, 10.0,      brush) },
//            { rect(rect,       pen, brush) },
//            { rect(rect, 10.0, pen, brush) },
//
//            { circle(circle,      brush) },
//            { circle(circle, pen, brush) },
//
//            { ellipse(circle,      brush) },
//            { ellipse(circle, pen, brush) },
//
//            { text   ("text", null, Point.Origin,             brush) },
//            { wrapped("text", null, Point.Origin, 0.0, 100.0, brush) }
//        ).forEach {
//            nothingRendered(it)
//        }
//    }

    @Test @JsName("rendersSimpleRect") fun `renders simple rect`() {
        val brush = ColorBrush(red)
        val rect  = Rectangle(100, 100)

        validateRender { renderParent, htmlFactory, _, _ ->
            val b = mockk<HTMLElement>(relaxed = true)
            every { htmlFactory.createOrUse("B", any()) } returns b

            rect(rect, brush)

            val style = b.style

            verify (exactly = 1) { style.setSize           (rect.size                        ) }
            verify (exactly = 1) { style.setBackgroundColor(brush.color                      ) }
            verify               { style.setTransform      (Identity.translate(rect.position)) }

            verify (exactly = 1) { renderParent.appendChild(b) }
        }
    }

    @Test @JsName("rendersSimpleRoundedRect") fun `renders simple rounded-rect`() {
        val brush  = ColorBrush(red)
        val rect   = Rectangle(100, 100)
        val radius = 12.0

        validateRender { renderParent, htmlFactory, _, _ ->
            val b = mockk<HTMLElement>(relaxed = true)
            every { htmlFactory.createOrUse("B", any()) } returns b

            rect(rect, radius, brush)

            val style = b.style

            verify (exactly = 1) { style.setBorderRadius   (radius                           ) }
            verify (exactly = 1) { style.setSize           (rect.size                        ) }
            verify (exactly = 1) { style.setBackgroundColor(brush.color                      ) }
            verify               { style.setTransform      (Identity.translate(rect.position)) }

            verify (exactly = 1) { renderParent.appendChild(b) }
        }
    }

    @Test @JsName("rendersSimpleCircle") fun `renders simple circle`() {
        val brush  = ColorBrush(red)
        val circle = Circle(center = Point(10, 10), radius = 100.0)

        validateRender { renderParent, htmlFactory, _, _ ->
            val b = mockk<HTMLElement>(relaxed = true)
            every { htmlFactory.createOrUse("B", any()) } returns b

            circle(circle, brush)

            val style = b.style

            verify (exactly = 1) { style.setBorderRadius   (circle.radius                                        ) }
            verify (exactly = 1) { style.setSize           (circle.boundingRectangle.size                        ) }
            verify (exactly = 1) { style.setBackgroundColor(brush.color                                          ) }
            verify               { style.setTransform      (Identity.translate(circle.boundingRectangle.position)) }

            verify (exactly = 1) { renderParent.appendChild(b) }
        }
    }

    @Test @JsName("rendersSimpleEllipse") fun `renders simple ellipse`() {
        val brush   = ColorBrush(red)
        val ellipse = Ellipse(center = Point(10, 10), xRadius = 100.0, yRadius = 45.0)

        validateRender { renderParent, htmlFactory, _, _ ->
            val b = mockk<HTMLElement>(relaxed = true)
            every { htmlFactory.createOrUse("B", any()) } returns b

            ellipse(ellipse, brush)

            val style = b.style

            verify (exactly = 1) { style.setBorderRadius   (ellipse.xRadius, ellipse.yRadius                      ) }
            verify (exactly = 1) { style.setSize           (ellipse.boundingRectangle.size                        ) }
            verify (exactly = 1) { style.setBackgroundColor(brush.color                                           ) }
            verify               { style.setTransform      (Identity.translate(ellipse.boundingRectangle.position)) }

            verify (exactly = 1) { renderParent.appendChild(b) }
        }
    }

    @Test @JsName("rendersSimpleText") fun `renders simple text`() {
        val brush = ColorBrush(red)
        val text  = "some text"
        val font  = mockk<Font>(relaxed = true)
        val at    = Point(34, 89)

        validateRender { renderParent, _, textFactory, _ ->
            val t = mockk<HTMLElement>(relaxed = true)
            every { textFactory.create(text, font, null) } returns t

            text(text, font, at, brush)

            val style = t.style

            verify (exactly = 1) { style.setOpacity(brush.color.opacity) }
            verify (exactly = 1) { style.setColor  (brush.color        ) }
            verify (exactly = 1) { style.translate (at                 ) }

            verify (exactly = 1) { renderParent.appendChild(t) }
        }
    }

    @Test @JsName("rendersSimpleStyledText") fun `renders simple styled text`() {
        val text = StyledText("some text")
        val at   = Point(34, 89)

        validateRender { renderParent, _, textFactory, _ ->
            val t = mockk<HTMLElement>(relaxed = true)
            every { textFactory.create(text, null) } returns t

            text(text, at)

            val style = t.style

            verify (exactly = 1) { style.translate(at) }

            verify (exactly = 1) { renderParent.appendChild(t) }
        }
    }

    @Test @JsName("rendersSimpleWrappedText") fun `renders simple wrapped text`() {
        val brush = ColorBrush(red)
        val text  = "some text"
        val font  = mockk<Font>(relaxed = true)
        val at    = Point(150, 89)

        validateRender { renderParent, _, textFactory, _ ->
            val t = mockk<HTMLElement>(relaxed = true)
            every { textFactory.wrapped(text, font, 100.0, 50.0, null) } returns t

            wrapped(text, font, at, 100.0, 200.0, brush)

            val style = t.style

            verify (exactly = 1) { style.setOpacity(brush.color.opacity) }
            verify (exactly = 1) { style.setColor  (brush.color        ) }
            verify (exactly = 1) { style.translate (at                 ) }

            verify (exactly = 1) { renderParent.appendChild(t) }
        }
    }

    @Test @JsName("rendersSimpleWrappedStyledText") fun `renders simple wrapped styled text`() {
        val text  = StyledText("some text")
        val at    = Point(150, 89)

        validateRender { renderParent, _, textFactory, _ ->
            val t = mockk<HTMLElement>(relaxed = true)
            every { textFactory.wrapped(text, 100.0, 50.0, null) } returns t

            wrapped(text, at, 100.0, 200.0)

            val style = t.style

            verify (exactly = 1) { style.translate(at) }

            verify (exactly = 1) { renderParent.appendChild(t) }
        }
    }

    @Test @JsName("rendersImage") fun `renders image`() {
        val image = mockk<ImageImpl>(relaxed = true).apply {
            every { size } returns Size(130.0, 46.0)
        }
        val at    = Point(150, 89)

        validateRender { renderParent, _, _, _ ->
            val clone = mockk<HTMLImageElement>(relaxed = true)
            val img   = mockk<HTMLImageElement>(relaxed = true).apply {
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

    private fun validateTransform(block: CanvasImpl.() -> AffineTransform) {
        val htmlFactory  = mockk<HtmlFactory>(relaxed = true)
        val renderParent = spyk<HTMLElement> (              )

        canvas(renderParent, htmlFactory).apply {
            val frame = mockk<HTMLElement>(relaxed = true).apply { every { parentNode } returns null andThen renderParent }

            every { htmlFactory.createOrUse("B", any()) } returns frame

            val transform = block(this)
            val style     = frame.style

            verify { style.setTransform(transform) }
            verify (exactly = 1) { renderParent.appendChild(frame) }
        }
    }

    private fun validateRender(block: CanvasImpl.(renderParent: HTMLElement, htmlFactory: HtmlFactory, textFactory: TextFactory, rendererFactory: VectorRendererFactory) -> Unit) {
        val renderer        = mockk<VectorRenderer>(relaxed = true)
        val htmlFactory     = mockk<HtmlFactory>   (relaxed = true)
        val textFactory     = mockk<TextFactory>   (relaxed = true)
        val renderParent    = spyk<HTMLElement>    (              )
        val rendererFactory = rendererFactory      (renderer      )

        canvas(renderParent, htmlFactory, textFactory, rendererFactory).apply {
            block(this, renderParent, htmlFactory, textFactory, rendererFactory)
        }
    }

    private fun nothingRendered(block: CanvasImpl.() -> Unit) {
        val renderParent = spyk<HTMLElement>()
        val renderer     = mockk<VectorRenderer>(relaxed = true)

        canvas(renderParent, rendererFactory = rendererFactory(renderer)).apply {
            block(this)
        }

//        verify { renderer wasNot Called }
        verify(exactly = 0) { renderParent.appendChild(any()) }
    }

    private fun nothingRendered(block: CanvasImpl.(Pen, Brush) -> Unit) {
        val renderParent = spyk<HTMLElement>()
        val renderer     = mockk<VectorRenderer>(relaxed = true)

        canvas(renderParent, rendererFactory = rendererFactory(renderer)).apply {
            block(this, mockk<Pen>(relaxed = true).apply { every { visible } returns false }, mockk<Brush>(relaxed = true).apply { every { visible } returns false })
        }

//        verify { renderer wasNot Called }
        verify(exactly = 0) { renderParent.appendChild(any()) }
    }

    private fun rendererFactory(renderer: VectorRenderer = mockk(relaxed = true)) = mockk<VectorRendererFactory>(relaxed = true).apply {
        every { this@apply.invoke(any()) } returns renderer
    }

    private fun canvas(renderParent   : HTMLElement           = mockk(relaxed = true),
                       htmlFactory    : HtmlFactory           = mockk(relaxed = true),
                       textFactory    : TextFactory           = mockk(relaxed = true),
                       rendererFactory: VectorRendererFactory = rendererFactory()) = CanvasImpl(renderParent, htmlFactory, textFactory, rendererFactory)

    private fun <T> validateDefault(p: KProperty1<CanvasImpl, T>, default: T?) {
        expect(default, "$p defaults to $default") { p.get(canvas()) }
    }
}