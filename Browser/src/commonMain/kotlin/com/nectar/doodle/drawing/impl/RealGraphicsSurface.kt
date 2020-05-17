package com.nectar.doodle.drawing.impl

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.core.View
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.None
import com.nectar.doodle.dom.Overflow.Visible
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.childAt
import com.nectar.doodle.dom.hasAutoOverflow
import com.nectar.doodle.dom.insert
import com.nectar.doodle.dom.numChildren
import com.nectar.doodle.dom.parent
import com.nectar.doodle.dom.remove
import com.nectar.doodle.dom.setDisplay
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setOverflow
import com.nectar.doodle.dom.setSize
import com.nectar.doodle.dom.setTransform
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.dom.translate
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

// TODO: provide different elements (i.e. HTMLButtonElement) based on type of View?
private fun canvasElement(view: View, htmlFactory: HtmlFactory): HTMLElement = htmlFactory.create()

internal class RealGraphicsSurface private constructor(
        private val htmlFactory        : HtmlFactory,
        private val canvasFactory      : CanvasFactory,
        private var parent             : RealGraphicsSurface?,
                    isContainer        : Boolean,
                    canvasElement      : HTMLElement,
                    addToRootIfNoParent: Boolean): GraphicsSurface {

    constructor(htmlFactory: HtmlFactory, canvasFactory: CanvasFactory, element: HTMLElement): this(htmlFactory, canvasFactory, null, false, element, true)
    constructor(htmlFactory: HtmlFactory, canvasFactory: CanvasFactory, parent: RealGraphicsSurface? = null, view: View, isContainer: Boolean = false, addToRootIfNoParent: Boolean = true): this(
            htmlFactory, canvasFactory, parent, isContainer, canvasElement(view, htmlFactory), addToRootIfNoParent)

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

    override var clipToBounds = true
        set(new) {
            if (field == new) {
                return
            }

            field = new

            when (field) {
                false -> {
                    rootElement.style.setOverflow    (Visible())
                    canvasElement?.style?.setOverflow(Visible())

                    if (isContainer) {
                        childrenElement = htmlFactory.create<HTMLElement>().apply {
                            style.setWidthPercent (100.0)
                            style.setHeightPercent(100.0)

                            while(rootElement.numChildren > indexStart) {
                                rootElement.childAt(indexStart)?.let { rootElement.remove(it); add(it) }
                            }

                            rootElement.appendChild(this)

                            indexStart = 0
                        }
                    }
                }
                else  -> {
                    rootElement.style.setOverflow    (null)
                    canvasElement?.style?.setOverflow(null)

                    if (isContainer && childrenElement != rootElement) {
                        // Move all children into rootNode
                        while(childrenElement.numChildren > 0) {
                            childrenElement.firstChild?.let { childrenElement.remove(it); rootElement.add(it) }
                        }

                        rootElement.remove(childrenElement)
                        childrenElement = rootElement
                        indexStart = 1
                    }
                }
            }
        }

    private var augmentedTransform = Identity
        set (new) {
            field = new

            updateTransform(position)
        }

    override var size: Size by observable(Empty) { _,old,new ->
        if (old == new) { return@observable }

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

            val oldClipping = clipToBounds

            clipToBounds = true

            canvasElement = if (field) {
                htmlFactory.create<HTMLElement>().apply {
                    style.setWidthPercent (100.0)
                    style.setHeightPercent(100.0)

                    while(rootElement.numChildren > 0) {
                        rootElement.firstChild?.let { rootElement.remove(it); add(it) }
                    }

                    rootElement.insert(this, 0)

                    canvas = canvasFactory(this).also { it.size = size }
                }
            } else {
                canvasElement?.let { it.parent?.remove(it) }
                canvas          = canvasFactory(rootElement) // TODO: Import contents from old canvas
                indexStart      = 0
                childrenElement = rootElement
                null
            }

            clipToBounds = oldClipping
        }

    private  var indexSet        = MutableTreeSet<Int>()
    private  var indexStart      = 0
    internal val rootElement     = canvasElement
    private  var childrenElement = rootElement

    override var position: Point by observable(Origin) { _,old,new ->
        if (new != old) {
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
            parent?.childrenElement?.add(rootElement)
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
        val point          = -Point(canvas.size.width / 2, canvas.size.height / 2)
        augmentedTransform = ((Identity translate point) * transform) translate -point
    }

    private fun updateTransform(new: Point) {
        if (rootElement.style.position.isNotBlank()) {
            return
        }

        rootElement.parent?.takeUnless { (it as HTMLElement).hasAutoOverflow }?.let {
            when {
                augmentedTransform.isIdentity -> rootElement.style.translate(new)
                else                          -> rootElement.style.setTransform(augmentedTransform.translate(new))
            }
        }
    }

    override fun release() {
        if (parent != null) {
            parent?.remove(this)
        } else {
            try {
                htmlFactory.root.remove(rootElement)
            } catch (ignore: Throwable) {}
        }
    }

    private fun add(@Suppress("UNUSED_PARAMETER") child: RealGraphicsSurface) {
        if (++numChildren == 1) {
            isContainer = true
        }
    }

    private fun remove(child: RealGraphicsSurface) {
        if (child.parent === this) {
            try {
                childrenElement.remove(child.rootElement)
            } catch (ignore: Throwable) {}

            indexSet.remove(child.index)

            child.parent = null

            --numChildren // TODO: is it worth reverting to not container?
        }
    }

    private fun setIndex(child: RealGraphicsSurface, index: Int) {
        val numChildren = childrenElement.numChildren

        if (child.rootElement.parentNode == rootElement) childrenElement.remove(child.rootElement)

        indexSet.add(index)

        childrenElement.insert(child.rootElement, max(indexStart, numChildren - indexSet.indexOf(index) - 1))
    }
}
