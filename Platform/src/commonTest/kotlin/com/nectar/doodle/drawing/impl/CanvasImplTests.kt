package com.nectar.doodle.drawing.impl

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.JsName
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.drawing.Brush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.drawing.Renderer.Optimization.Quality
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 8/11/19.
 */

@Suppress("FunctionName")
class CanvasImplTests {
    @Test @JsName("defaults")
    fun `defaults valid`() {
        mapOf(
            CanvasImpl::size         to Size.Empty,
            CanvasImpl::optimization to Quality
        ).forEach { validateDefault(it.key, it.value) }
    }

    @Test @JsName("invisibleToolsNoOp")
    fun `invisible tools no-op`() {
        val rect   = Rectangle(100, 100)
        val circle = Circle   (100.0   )

        listOf<CanvasImpl.(Pen, Brush) -> Unit>(
            { _,   brush -> rect(rect,            brush) },
            { _,   brush -> rect(rect, 10.0,      brush) },
            { pen, brush -> rect(rect,       pen, brush) },
            { pen, brush -> rect(rect, 10.0, pen, brush) },

            { _,   brush -> circle(circle,      brush) },
            { pen, brush -> circle(circle, pen, brush) },

            { _,   brush -> ellipse(circle,      brush) },
            { pen, brush -> ellipse(circle, pen, brush) },

            { _,   brush -> text   ("text", null, Point.Origin,             brush) },
            { _,   brush -> wrapped("text", null, Point.Origin, 0.0, 100.0, brush) }
        ).forEach {
            nothingRendered(it)
        }
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