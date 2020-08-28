package io.nacular.doodle.drawing.impl

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.None
import io.nacular.doodle.dom.Overflow.Visible
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.childAt
import io.nacular.doodle.dom.hasAutoOverflow
import io.nacular.doodle.dom.insert
import io.nacular.doodle.dom.numChildren
import io.nacular.doodle.dom.parent
import io.nacular.doodle.dom.remove
import io.nacular.doodle.dom.setClipPath
import io.nacular.doodle.dom.setDisplay
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setOverflow
import io.nacular.doodle.dom.setSize
import io.nacular.doodle.dom.setTransform
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.dom.translate
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.CanvasFactory
import io.nacular.doodle.drawing.GraphicsSurface
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.utils.MutableTreeSet
import io.nacular.doodle.utils.observable
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

    override var mirrored = false
        set(new) {
            field = new

            updateTransform(position)
        }

    private fun setupChildrenClipRect() {
        val needsClipping = !(clipCanvasToBounds && childdrenClipPoly == null)

        when {
            needsClipping -> if (isContainer && childrenElement == rootElement) {
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
            else -> if(isContainer && childrenElement != rootElement) {
                // Move all children into rootNode
                while(childrenElement.numChildren > 0) {
                    childrenElement.firstChild?.let { childrenElement.remove(it); rootElement.add(it) }
                }

                rootElement.remove(childrenElement)
                childrenElement = rootElement
                indexStart = 1
            }
        }

        if (childrenElement != rootElement) {
            childrenElement.style.setClipPath(childdrenClipPoly)
        }
    }

    override var clipCanvasToBounds = true
        set(new) {
            if (field != new) {
                field = new

                setupChildrenClipRect()

                when (field) {
                    true -> {
                        rootElement.style.setOverflow    (null)
                        canvasElement?.style?.setOverflow(null)
                    }
                    else -> {
                        rootElement.style.setOverflow    (Visible())
                        canvasElement?.style?.setOverflow(Visible())
                    }
                }
            }
        }

    override var childdrenClipPoly: Polygon? = null
        set(new) {
            if (field != new) {
                field = new

                setupChildrenClipRect()
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

            val oldClipping = clipCanvasToBounds

            clipCanvasToBounds = true

            canvasElement = if (field) {
                htmlFactory.create<HTMLElement>().apply {
                    style.setWidthPercent (100.0)
                    style.setHeightPercent(100.0)

                    while(rootElement.numChildren > 0) {
                        rootElement.firstChild?.let { rootElement.remove(it); add(it) }
                    }

                    rootElement.insert(this, 0)

                    canvas = canvasFactory(this).also { it.size = size }

                    indexStart = 1
                }
            } else {
                canvasElement?.let { it.parent?.remove(it) }
                canvas          = canvasFactory(rootElement) // TODO: Import contents from old canvas
                indexStart      = 0
                childrenElement = rootElement
                null
            }

            clipCanvasToBounds = oldClipping
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

        when {
            (rootElement.parent as HTMLElement).hasAutoOverflow -> {
                when {
                    mirrored -> rootElement.style.setTransform(Identity.flipHorizontally())
                    else     -> rootElement.style.setTransform(null)
                }
            }
            else                                                -> {
                when {
                    !mirrored && augmentedTransform.isIdentity -> rootElement.style.translate(new)
                    else                                       -> {
                        val transform = when {
                            mirrored -> (augmentedTransform translate new).flipHorizontally()
                            else     ->  augmentedTransform translate new
                        }

                        rootElement.style.setTransform(transform)
                    }
                }
            }
        }

        rootElement.parent?.takeUnless { (it as HTMLElement).hasAutoOverflow }?.also {
            when {
                !mirrored && augmentedTransform.isIdentity -> rootElement.style.translate(new)
                else                                       -> {
                    val transform = when {
                        mirrored -> (augmentedTransform translate new).flipHorizontally()
                        else     ->  augmentedTransform translate new
                    }

                    rootElement.style.setTransform(transform)
                }
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
