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
import com.nectar.doodle.utils.MutableTreeSet
import com.nectar.doodle.utils.observable
import org.w3c.dom.HTMLElement
import kotlin.math.max


class RealGraphicsSurface private constructor(
        private val htmlFactory        : HtmlFactory,
        private val canvasFactory      : CanvasFactory,
        private var parent             : RealGraphicsSurface?,
                    isContainer        : Boolean,
                    canvasElement      : HTMLElement,
                    addToRootIfNoParent: Boolean): GraphicsSurface {

    constructor(htmlFactory: HtmlFactory,canvasFactory: CanvasFactory, element: HTMLElement): this(htmlFactory,canvasFactory, null, false, element, false)
    constructor(htmlFactory: HtmlFactory, canvasFactory: CanvasFactory, parent: RealGraphicsSurface? = null, isContainer: Boolean = false): this(htmlFactory, canvasFactory, parent, isContainer, htmlFactory.create(), true)

    override var visible = true
        set(new) {
            field = new

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

    lateinit var canvas: Canvas
        private set

    private var numChildren   = 0
    private var canvasElement = canvasElement as HTMLElement?

    private var isContainer = false
        set(new) {
            if (field == new) { return }

            field = new

            canvasElement = if (field) {
                htmlFactory.create<HTMLElement>().apply {
                    style.setWidthPercent (100.0)
                    style.setHeightPercent(100.0)

                    while(rootElement.numChildren > 0) {
                        rootElement.firstChild?.let { rootElement.remove(it); add(it) }
                    }

                    rootElement.insert(this, 0)

                    canvas      = canvasFactory(this)
                    zIndexStart = 1
                }
            } else {
                canvasElement?.let { it.parent?.remove(it) }
                canvas      = canvasFactory(rootElement) // TODO: Import contents from old canvas
                zIndexStart = 0
                null
            }
        }

    private  var zIndexSet   = MutableTreeSet<Int>()
    private  var zIndexStart = 0
    internal val rootElement = canvasElement

    init {
        this.isContainer = isContainer

        if (!isContainer) {
            canvas = canvasFactory(rootElement)
        }

        if (parent != null) {
            parent?.add(this)
            parent?.rootElement?.add(rootElement)
        } else if (addToRootIfNoParent) {
            htmlFactory.root.add(rootElement)
        }
    }

    override fun render(block: (Canvas) -> Unit) {
        canvas.clear()
        canvas.optimization = Quality

        if (isContainer) {
            canvasElement?.let {
                if (it.numChildren == 0) {
                    it.parent?.remove(it)
                    zIndexStart = 0
                }
            }
        }

        block(canvas)

        canvas.flush()

        if (isContainer) {
            canvasElement?.let {
                if (it.numChildren > 0) {
                    rootElement.insert(it, 0)
                    zIndexStart = 1
                }
            }
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

    override fun release() {
        if (parent != null) {
            parent?.remove(this)
        } else {
            htmlFactory.root.remove(rootElement)
        }
    }

    private fun add(@Suppress("UNUSED_PARAMETER") child: RealGraphicsSurface) {
        if (++numChildren == 1) {
            isContainer = true
        }
    }

    private fun remove(child: RealGraphicsSurface) {
        if (child.parent === this) {
            rootElement.remove(child.rootElement)
            zIndexSet.remove(child.zIndex)

            child.parent = null

            --numChildren // TODO: is it worth reverting to not container?
        }
    }

    private fun setZIndex(child: RealGraphicsSurface, index: Int) {
        if (child.rootElement.parentNode == rootElement) rootElement.remove(child.rootElement)

        zIndexSet.add(index)

        rootElement.insert(child.rootElement, max(zIndexStart, numChildren - zIndexSet.indexOf(index) - 1))
    }
}
