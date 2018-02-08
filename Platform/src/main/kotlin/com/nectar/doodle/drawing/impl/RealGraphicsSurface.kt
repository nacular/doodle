package com.nectar.doodle.drawing.impl


import com.nectar.doodle.dom.Display
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.hasAutoOverflow
import com.nectar.doodle.dom.insert
import com.nectar.doodle.dom.numChildren
import com.nectar.doodle.dom.parent
import com.nectar.doodle.dom.remove
import com.nectar.doodle.dom.setDisplay
import com.nectar.doodle.dom.setHeight
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setWidth
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.dom.translate
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.CanvasFactory
import com.nectar.doodle.drawing.GraphicsSurface
import com.nectar.doodle.drawing.Renderer.Optimization.Quality
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.nectar.doodle.utils.observable
import org.w3c.dom.HTMLElement


class RealGraphicsSurface private constructor(
        private val htmlFactory  : HtmlFactory,
                    canvasFactory: CanvasFactory,
        private var parent       : RealGraphicsSurface?,
        private val isContainer  : Boolean,
        private var canvasElement: HTMLElement,
        addToDocumentIfNoParent  : Boolean): GraphicsSurface {

    constructor(htmlFactory: HtmlFactory,canvasFactory: CanvasFactory, element: HTMLElement): this(htmlFactory,canvasFactory, null, false, element, false)
    constructor(htmlFactory: HtmlFactory,canvasFactory: CanvasFactory, parent: RealGraphicsSurface? = null, isContainer: Boolean = false): this(htmlFactory, canvasFactory, parent, isContainer, htmlFactory.create(), true)

    override var visible = true
        set(new) {
            if (new) {
                rootElement.style.setDisplay()
            } else {
                rootElement.style.setDisplay(Display.None)
            }
        }

    override var zIndex = 0
        set(new) {
            parent?.setZIndex(this, new)
        }

    override val canvas: Canvas

    private  val children    = mutableListOf<RealGraphicsSurface>()
    internal val rootElement = canvasElement

    init {
        if (isContainer) {
            canvasElement = htmlFactory.create()

            canvasElement.style.setWidthPercent (100.0)
            canvasElement.style.setHeightPercent(100.0)

            rootElement.add(canvasElement)
        }

        canvas = canvasFactory(canvasElement)

        if (parent != null) {
            parent?.add(this)
        } else if (addToDocumentIfNoParent) {
            htmlFactory.body.add(rootElement)
        }
    }

    override fun render(block: (Canvas) -> Unit) {
        canvas.clear()
        canvas.optimization = Quality

        if (isContainer && canvasElement.numChildren == 0 && canvasElement.parent != null) {
            canvasElement.parent!!.remove(canvasElement)
        }

        block(canvas)

        canvas.flush()

        if (isContainer && canvasElement.numChildren > 0) {
            rootElement.insert(canvasElement, 0)
        }
    }

    override var position: Point by observable(Point.Origin) { _,_,new ->
        rootElement.parent?.let { it.takeUnless { (it as HTMLElement).hasAutoOverflow }?.let {
            rootElement.style.translate(new)
        } }
    }

    override var size: Size by observable(Empty) { _,_,new ->
        rootElement.parent?.let {
            rootElement.style.setWidth (new.width )
            rootElement.style.setHeight(new.height)

            canvas.size = new
        }
    }

//    override fun iterator() = children.iterator()

    override fun release() {
        if (parent != null) {
            parent!!.remove(this)
        } else {
            htmlFactory.body.remove(rootElement)
        }
    }

    private fun add(child: RealGraphicsSurface) {
        if (child.parent === this) {
            return
        }

        child.parent?.remove(child)
        children.add(child)
        rootElement.add(child.rootElement)

        child.parent = this
    }

    private fun remove(child: RealGraphicsSurface) {
        if (child.parent === this) {
            children.remove(child)
            rootElement.remove(child.rootElement)

            child.parent = null
        }
    }

    private fun setZIndex(child: RealGraphicsSurface, index: Int) {
        if (child.rootElement.parentNode == rootElement) rootElement.remove(child.rootElement)
        rootElement.insert(child.rootElement, rootElement.numChildren - index)
    }
}
