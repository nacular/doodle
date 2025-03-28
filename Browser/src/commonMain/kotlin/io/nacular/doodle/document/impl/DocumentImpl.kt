package io.nacular.doodle.document.impl

import io.nacular.doodle.controls.document.Document
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.Block
import io.nacular.doodle.dom.Display
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Inline
import io.nacular.doodle.dom.InlineBlock
import io.nacular.doodle.dom.Position
import io.nacular.doodle.dom.Static
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.setDisplay
import io.nacular.doodle.dom.setDomPosition
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.TextFactory
import io.nacular.doodle.drawing.impl.NativeCanvas
import io.nacular.doodle.drawing.impl.RealGraphicsSurface
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.scheduler.Scheduler
import io.nacular.doodle.scheduler.Task
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.text.TextSpacing.Companion.default
import io.nacular.doodle.utils.TextAlignment.Start

/**
 * Created by Nicholas Eddy on 2/13/20.
 */
internal class DocumentImpl(
        private val scheduler     : Scheduler,
        private val textFactory   : TextFactory,
        private val graphicsDevice: GraphicsDevice<RealGraphicsSurface>,
                    htmlFactory   : HtmlFactory): Document() {
    private val root = htmlFactory.create<HTMLElement>().apply {
        style.setDisplay    (Block ())
        style.setDomPosition(Static())
        // TODO: Enable text selection
//        style.userSelect = "text"
    }

    private var resizeTask = null as Task?

    init {
        suggestHeight(1.0) // FIXME: Remove once a better solution exists to allow first render

        layout = simpleLayout { _,_,current,_,_ ->
            children.forEach {
                it.suggestBounds(it.bounds.at(graphicsDevice[it].rootElement.run { Point(offsetLeft, offsetTop) }))
            }

            resizeTask = resizeTask ?: scheduler.now { resize() }

            current
        }
    }

    private fun resize() {
        suggestHeight(root.offsetHeight.toDouble())
        resizeTask = null
    }

    override fun render(canvas: Canvas) {
        if (canvas is NativeCanvas) {
            canvas.addData(listOf(root))

            suggestHeight(root.offsetHeight.toDouble())

            resizeTask = resizeTask ?: scheduler.now { resize() }
        }
    }

    override fun inline(text: StyledText) {
        root.add(textFactory.wrapped(text, alignment = Start, lineSpacing = 1f, textSpacing = default).apply {
            style.setDomPosition(Static())
            style.setDisplay (Inline())
        })
    }

    override fun inline(text: String, font: Font?) {
        root.add(textFactory.wrapped(text, font, alignment = Start, lineSpacing = 1f, textSpacing = default).apply {
            style.setDomPosition(Static())
            style.setDisplay    (Inline())
        })
    }

    override fun inline(view: View) {
        add(view, Static(), InlineBlock())
    }

    override fun wrapText(view: View) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun breakText(view: View) {
        add(view, Static(), Block())
    }

    private fun add(view: View, position: Position?, display: Display?) {
        // ensure that we have a headless graphics surface
        val surface = graphicsDevice.create(view).also { surface ->
            position?.let { surface.rootElement.style.setDomPosition(it) }
            display?.let  { surface.rootElement.style.setDisplay (it) }
        }

        children += view

        // TODO: remove listener
        view.parentChanged += { _, _, _ ->
            surface.rootElement.style.position  = "initial"
            surface.rootElement.style.display   = "initial"
            surface.rootElement.style.transform = "translate3d(0,0,0)"
        }

        root.add(surface.rootElement)
    }
}