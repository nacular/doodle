package com.nectar.doodle.drawing.impl


import com.nectar.doodle.dom.Display.None
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.hasAutoOverflow
import com.nectar.doodle.dom.insert
import com.nectar.doodle.dom.numChildren
import com.nectar.doodle.dom.parent
import com.nectar.doodle.dom.remove
import com.nectar.doodle.dom.setDisplay
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setSize
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.dom.translate
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.CanvasFactory
import com.nectar.doodle.drawing.GraphicsSurface
import com.nectar.doodle.drawing.Renderer.Optimization.Quality
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.nectar.doodle.utils.observable
import org.w3c.dom.HTMLElement
import kotlin.math.max


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
                rootElement.style.setDisplay(None)
            }
        }

    override var zIndex = 0
        set(new) {
            parent?.setZIndex(this, new)
        }

    override val canvas: Canvas

    private  val children    by lazy { mutableListOf<RealGraphicsSurface>() }
    private  var zIndexSet   = mutableSetOf<Int>()
    private  var zIndexStart = 0
    internal val rootElement = canvasElement

    init {
        if (isContainer) {
            canvasElement = htmlFactory.create()

            canvasElement.style.setWidthPercent (100.0)
            canvasElement.style.setHeightPercent(100.0)

            rootElement.insert(canvasElement, 0)
        }

        canvas = canvasFactory(canvasElement)

        if (parent != null) {
            parent?.rootElement?.add(rootElement)
        } else if (addToDocumentIfNoParent) {
            htmlFactory.body.add(rootElement)
        }
    }

    override fun render(block: (Canvas) -> Unit) {
        canvas.clear()
        canvas.optimization = Quality

        if (isContainer && canvasElement.numChildren == 0) {
            canvasElement.parent?.remove(canvasElement)
        }

        block(canvas)

        canvas.flush()

        if (isContainer && canvasElement.numChildren > 0) {
            rootElement.insert(canvasElement, 0)
            zIndexStart = 1
        }
    }

    override var position: Point by observable(Origin) { _,_,new ->
        rootElement.parent?.let { it.takeUnless { (it as HTMLElement).hasAutoOverflow }?.let {
            rootElement.style.translate(new)
        } }
    }

    override var size: Size by observable(Empty) { _,_,new ->
        rootElement.parent?.let {
            rootElement.style.setSize(new)

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

    private fun remove(child: RealGraphicsSurface) {
        if (child.parent === this) {
            children.remove(child)
            rootElement.remove(child.rootElement)
            zIndexSet.remove(child.zIndex)

            child.parent = null
        }
    }

    private fun setZIndex(child: RealGraphicsSurface, index: Int) {
        if (child.rootElement.parentNode == rootElement) rootElement.remove(child.rootElement)

        if (zIndexSet.add(index)) {
            zIndexSet = zIndexSet.sorted().toMutableSet()
        }

        rootElement.insert(child.rootElement, max(zIndexStart, zIndexSet.indexOf(index)))
    }
}
