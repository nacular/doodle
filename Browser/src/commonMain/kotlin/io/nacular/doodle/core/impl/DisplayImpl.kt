package io.nacular.doodle.core.impl

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.clear
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.LookupResult.Empty
import io.nacular.doodle.core.LookupResult.Found
import io.nacular.doodle.core.LookupResult.Ignored
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.height
import io.nacular.doodle.core.width
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.addIfNotPresent
import io.nacular.doodle.dom.clearVisualStyles
import io.nacular.doodle.dom.height
import io.nacular.doodle.dom.setBackgroundColor
import io.nacular.doodle.dom.setBackgroundImage
import io.nacular.doodle.dom.setBackgroundSize
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setOpacity
import io.nacular.doodle.dom.setTransform
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.dom.width
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Brush
import io.nacular.doodle.drawing.CanvasFactory
import io.nacular.doodle.drawing.ColorBrush
import io.nacular.doodle.drawing.ImageBrush
import io.nacular.doodle.focus.FocusTraversalPolicy
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.layout.Insets.Companion.None
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.ObservableProperty
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.observable


internal class DisplayImpl(htmlFactory: HtmlFactory, canvasFactory: CanvasFactory, private val rootElement: HTMLElement): InternalDisplay {

    override var insets = None

    override var layout: Layout? by observable<Layout?>(null) { _,_,_ ->
        relayout()
    }

    override val children by lazy { ObservableList<View>().apply {
        changed += { _,_,added,_ ->
            added.forEach { item ->
                filterIndexed { index, view -> index != item.key && view == item.value }.forEach { remove(it) }
            }
        }
    } }

    override val sizeChanged  : PropertyObservers<Display, Size>    by lazy { PropertyObserversImpl<Display, Size>(this) }
    override val cursorChanged: PropertyObservers<Display, Cursor?> by lazy { PropertyObserversImpl<Display, Cursor?>(this) }
    override var size                                               by ObservableProperty(Empty, { this }, sizeChanged as PropertyObserversImpl<Display, Size>)
        private set
    override var cursor: Cursor?                                    by ObservableProperty(null, { this }, cursorChanged as PropertyObserversImpl<Display, Cursor?>)

    override var focusTraversalPolicy: FocusTraversalPolicy? = null

    private val canvasElement       = htmlFactory.create<HTMLElement>()
    private val canvas              = canvasFactory(canvasElement)
    private val positionableWrapper = PositionableWrapper()
    private var brush               = null as Brush?

    override var transform = Identity
        set (new) {
            field = new

            refreshAugmentedTransform()
        }

    private var augmentedTransform = Identity
        set (new) {
            field = new

            updateTransform()
        }

    init {
        rootElement.onresize = ::onResize

        onResize()

//        isFocusCycleRoot = true

        canvasElement.style.setWidthPercent (100.0)
        canvasElement.style.setHeightPercent(100.0)
    }

//    var focusTraversalPolicy: FocusTraversalPolicy
//        get() = ROOT_CONTAINER.getFocusTraversalPolicy()
//        set(aPolicy) {
//            ROOT_CONTAINER.setFocusTraversalPolicy(aPolicy)
//        }

    override fun fill(brush: Brush) {
        when (brush) {
            is ColorBrush -> {
                canvasElement.parentNode?.removeChild(canvasElement)

                rootElement.style.setBackgroundColor(brush.color)
            }
            is ImageBrush -> {
                when (brush.opacity) {
                    1.0f -> {
                        canvasElement.parentNode?.removeChild(canvasElement)
                        rootElement.style.setBackgroundSize (brush.size )
                        rootElement.style.setBackgroundImage(brush.image)
                    }
                    else -> {
                        rootElement.addIfNotPresent(canvasElement, 0)

                        canvasElement.clear()
                        canvasElement.style.setOpacity        (brush.opacity)
                        canvasElement.style.setBackgroundSize (brush.size   )
                        canvasElement.style.setBackgroundImage(brush.image  )
                    }
                }
            }
            else          -> {
                this.brush = brush
                rootElement.addIfNotPresent(canvasElement, 0)

                repaint()
            }
        }
    }

    override infix fun ancestorOf(view: View): Boolean = if (children.isNotEmpty()) {
        var child: View? = view

        while (child?.parent != null) {
            child = child.parent
        }

        child in children
    } else false

    override fun child(at: Point): View? = (transform.inverse?.invoke(at) ?: at).let { point ->
        when (val result = layout?.child(positionableWrapper, point)) {
            null, Ignored -> {
                var child     = null as View?
                var topZOrder = 0

                children.reversed().forEach {
                    if (it.visible && point in it && (child == null || it.zOrder > topZOrder)) {
                        child     = it
                        topZOrder = it.zOrder
                    }
                }

                child
            }
            is Found      -> (result.child as io.nacular.doodle.core.PositionableWrapper).view
            is Empty      -> null
        }
    }

    override fun iterator() = children.iterator()

    private var layingOut = false

    override fun relayout() {
        if (!layingOut) {
            layingOut = true

            layout?.layout(positionableWrapper)

            layingOut= false
        }
    }

    fun shutdown() {
        children.clear()

        brush = null

        rootElement.apply {
            style.setTransform(null)

            clearVisualStyles()
            clear            ()
        }
    }

    private fun onResize(@Suppress("UNUSED_PARAMETER") event: Event? = null) {
        size = Size(rootElement.width, rootElement.height)

        refreshAugmentedTransform()
        repaint()
    }

    override fun repaint() {
        brush?.let {
            canvas.clear()
            canvas.rect (Rectangle(size = size), it)
            canvas.flush()
        }
    }

    private fun refreshAugmentedTransform() {
        val point          = -Point(size.width / 2, size.height / 2)
        augmentedTransform = ((Identity translate point) * transform) translate -point
    }

    private fun updateTransform() {
        rootElement.style.setTransform(augmentedTransform)
    }

    private inner class PositionableWrapper: PositionableContainer {
        override var size        get() = this@DisplayImpl.size;   set(_) {}
        override var width       get() = this@DisplayImpl.width;  set(_) {}
        override var height      get() = this@DisplayImpl.height; set(_) {}
        override var idealSize   get() = null as Size?;           set(_) {}
        override var minimumSize get() = Empty;                   set(_) {}

        override val insets      get() = this@DisplayImpl.insets
        override val layout      get() = this@DisplayImpl.layout
        override val parent      get() = null as PositionableContainer?
        override val children    get() = this@DisplayImpl.children.map { io.nacular.doodle.core.PositionableWrapper(it) }
    }
}