package com.zinoti.jaz.drawing.impl


import com.zinoti.jaz.dom.Display
import com.zinoti.jaz.dom.add
import com.zinoti.jaz.dom.create
import com.zinoti.jaz.dom.insert
import com.zinoti.jaz.dom.numChildren
import com.zinoti.jaz.dom.parent
import com.zinoti.jaz.dom.remove
import com.zinoti.jaz.dom.setDisplay
import com.zinoti.jaz.dom.setHeightPercent
import com.zinoti.jaz.dom.setWidthPercent
import com.zinoti.jaz.drawing.Canvas
import com.zinoti.jaz.drawing.CanvasFactory
import com.zinoti.jaz.drawing.GraphicsSurface
import com.zinoti.jaz.geometry.Point
import com.zinoti.jaz.geometry.Size
import org.w3c.dom.HTMLElement
import kotlin.browser.document


class RealGraphicsSurface private constructor(
        private val canvasFactory: CanvasFactory,
        private var parent: RealGraphicsSurface?,
        private val isContainer: Boolean,
        private var canvasElement: HTMLElement,
        addToDocumentIfNoParent: Boolean): GraphicsSurface, Iterable<RealGraphicsSurface> {

    constructor(canvasFactory: CanvasFactory, element: HTMLElement): this(canvasFactory, null, false, element, false)
    constructor(canvasFactory: CanvasFactory, parent: RealGraphicsSurface? = null, isContainer: Boolean = false): this(canvasFactory, parent, isContainer, create("b"), true)

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

    private val children    = mutableListOf<RealGraphicsSurface>()
    private val rootElement = canvasElement

    init {
        if (isContainer) {
            canvasElement = create("b")

            canvasElement.style.setWidthPercent (100.0)
            canvasElement.style.setHeightPercent(100.0)

            rootElement.add(canvasElement)
        }

        canvas = canvasFactory.create(canvasElement)

        if (parent != null) {
            parent?.add(this)
        } else if (addToDocumentIfNoParent) {
            document.body?.add(rootElement)
        }
    }

    override fun endRender() {
        canvas.flush()

        if (isContainer && canvasElement.numChildren > 0) {
            rootElement.insert(canvasElement, 0)
        }
    }

    override fun beginRender() {
        canvas.clear()
        canvas.optimization = Canvas.Optimization.Quality

        if (isContainer && canvasElement.numChildren == 0 && canvasElement.parent != null) {
            canvasElement.parent!!.remove(canvasElement)
        }
    }

    override var position = Point.Origin
        set(new) {
            rootElement.parent?.let { it.takeIf { !hasAutoOverflow(it) }?.let {
                rootElement.style.left = "${new.x}"
                rootElement.style.top  = "${new.y}"
            } }
        }

    override var size = Size.Empty
        set(new) {
            rootElement.parent?.let { it.takeIf { !hasAutoOverflow(it) }?.let {
                rootElement.style.width  = "${new.width}"
                rootElement.style.height = "${new.height}"
            } }
        }

    override fun iterator() = children.iterator()

    internal fun release() {
        if (parent != null) {
            parent!!.remove(this)
        } else {
            document.body?.remove(rootElement)
        }
    }

    private fun add(child: RealGraphicsSurface) {
        if (child.parent != null) {
            if (child.parent === this) {
                return
            }

            child.parent!!.remove(child)
        }

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
        rootElement.remove(child.rootElement)
        rootElement.insert(child.rootElement, rootElement.numChildren - index)
    }

    private fun hasAutoOverflow(element: HTMLElement) = element.style.overflowWrap != ""
}
