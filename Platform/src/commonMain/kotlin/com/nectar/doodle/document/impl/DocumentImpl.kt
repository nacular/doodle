package com.nectar.doodle.document.impl

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.controls.document.Document
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.PositionableContainer
import com.nectar.doodle.core.View
import com.nectar.doodle.dom.Block
import com.nectar.doodle.dom.Display
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.Inline
import com.nectar.doodle.dom.InlineBlock
import com.nectar.doodle.dom.Position
import com.nectar.doodle.dom.Static
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.setDisplay
import com.nectar.doodle.dom.setPosition
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.drawing.impl.CanvasImpl
import com.nectar.doodle.drawing.impl.RealGraphicsSurface
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.text.StyledText

/**
 * Created by Nicholas Eddy on 2/13/20.
 */
internal class DocumentImpl(
        private val scheduler     : Scheduler,
        private val textFactory   : TextFactory,
        private val graphicsDevice: GraphicsDevice<RealGraphicsSurface>,
                    htmlFactory   : HtmlFactory): Document() {
    private val root = htmlFactory.create<HTMLElement>().apply {
        style.setDisplay (Block ())
        style.setPosition(Static())
        // TODO: Enable text selection
//        style.userSelect = "text"
    }

    private var resizeTask = null as Task?

    init {
        height = 1.0 // FIXME: Remove once a better solution exists to allow first render

        layout = object: Layout() {
            override fun layout(container: PositionableContainer) {
                children.forEach {
                    it.bounds = it.bounds.at(graphicsDevice[it].rootElement.run { Point(offsetLeft, offsetTop) })
                }

                resizeTask = resizeTask ?: scheduler.now { resize() }
            }
        }
    }

    private fun resize() {
        height    = root.offsetHeight.toDouble()
        resizeTask = null
    }

    override fun render(canvas: Canvas) {
        if (canvas is CanvasImpl) {
            canvas.addData(listOf(root))

            height = root.offsetHeight.toDouble()

            resizeTask = resizeTask ?: scheduler.now { resize() }
        }
    }

    override fun inline(text: StyledText) {
        root.add(textFactory.wrapped(text).apply {
            style.setPosition(Static())
            style.setDisplay (Inline())
        })
    }

    override fun inline(text: String, font: Font?) {
        root.add(textFactory.wrapped(text, font).apply {
            style.setPosition(Static())
            style.setDisplay (Inline())
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
            position?.let { surface.rootElement.style.setPosition(it) }
            display?.let  { surface.rootElement.style.setDisplay (it) }
        }

        children += view

        // TODO: remove listener
        view.parentChange += { _,_,_ ->
            surface.rootElement.style.position = "initial"
            surface.rootElement.style.display  = "initial"
        }

        root.add(surface.rootElement)
    }
}