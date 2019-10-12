package com.nectar.doodle.drawing.impl

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.None
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.hasAutoOverflow
import com.nectar.doodle.dom.insert
import com.nectar.doodle.dom.numChildren
import com.nectar.doodle.dom.parent
import com.nectar.doodle.dom.remove
import com.nectar.doodle.dom.setDisplay
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setSize
import com.nectar.doodle.dom.setTransform
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.drawing.AffineTransform.Companion.Identity
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.CanvasFactory
import com.nectar.doodle.drawing.GraphicsSurface
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.nectar.doodle.utils.MutableTreeSet
import com.nectar.doodle.utils.observable
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

            rootElement.style.setDisplay(if (new) null else None())
        }

    override var index = 0
        set(new) {
            if (field != new) {
                field = new
                parent?.setIndex(this, new)
            }
        }

    override var zOrder = 0
        set(new) {
            field = new
            rootElement.style.zIndex = if (new == 0) "" else "$new"
        }

    lateinit var canvas: Canvas
        private set

    private var numChildren   = 0
    private var canvasElement = canvasElement as HTMLElement?

    override var transform = Identity
        set (new) {
            field = new

            refreshAugmentedTransform()
        }

    private var augmentedTransform = Identity
        set (new) {
            field = new

            updateTransform(position)
        }

    override var size: Size by observable(Empty) { _,_,new ->
        rootElement.parent?.let {
            rootElement.style.setSize(new)

            canvas.size = new

            refreshAugmentedTransform() // Need to incorporate new size in transform calculation
        }
    }

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

                    canvas     = canvasFactory(this).also { it.size = size }
                    indexStart = 1
                }
            } else {
                canvasElement?.let { it.parent?.remove(it) }
                canvas     = canvasFactory(rootElement) // TODO: Import contents from old canvas
                indexStart = 0
                null
            }
        }

    private  var indexSet    = MutableTreeSet<Int>()
    private  var indexStart  = 0
    internal val rootElement = canvasElement

    override var position: Point by observable(Origin) { _,old,new ->
        if (new != old) {

//        rootElement.parent?.let {
//            when {
//                (it as HTMLElement).hasAutoOverflow -> {} //rootElement.style.apply { setTop(new.y); setLeft(new.x) }
//                else                                -> rootElement.style.translate(new)
//            }
//        }

            updateTransform(new)
        }
    }

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

        block(canvas)

        canvas.flush()

        if (isContainer) {
            canvasElement?.let {
                indexStart = if (it.numChildren == 0) {
                    it.parent?.remove(it)
                    0
                } else {
                    if (it.parent == null) rootElement.insert(it, 0)
                    1
                }
            }
        }
    }

    private fun refreshAugmentedTransform() {
        val point          = - Point(canvas.size.width / 2, canvas.size.height / 2)
        augmentedTransform = (Identity.translate(point) * transform).translate(-point)
    }

    private fun updateTransform(position: Point) {
        rootElement.parent?.let { it.takeUnless { (it as HTMLElement).hasAutoOverflow }?.let {
            rootElement.style.setTransform(augmentedTransform.translate(position))
        } }
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
            indexSet.remove(child.index)

            child.parent = null

            --numChildren // TODO: is it worth reverting to not container?
        }
    }

    private fun setIndex(child: RealGraphicsSurface, index: Int) {
        val numChildren = rootElement.numChildren

        if (child.rootElement.parentNode == rootElement) rootElement.remove(child.rootElement)

        indexSet.add(index)

        rootElement.insert(child.rootElement, max(indexStart, numChildren - indexSet.indexOf(index) - 1))
    }
}