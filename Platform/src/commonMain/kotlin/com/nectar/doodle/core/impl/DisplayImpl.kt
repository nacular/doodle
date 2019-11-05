package com.nectar.doodle.core.impl

import com.nectar.doodle.Event
import com.nectar.doodle.HTMLElement
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.PositionableContainer
import com.nectar.doodle.core.View
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.height
import com.nectar.doodle.dom.insert
import com.nectar.doodle.dom.setBackgroundColor
import com.nectar.doodle.dom.setBackgroundImage
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.dom.width
import com.nectar.doodle.drawing.Brush
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.ImageBrush
import com.nectar.doodle.focus.FocusTraversalPolicy
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.nectar.doodle.layout.Insets.Companion.None
import com.nectar.doodle.onresize
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.utils.ObservableList
import com.nectar.doodle.utils.ObservableProperty
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import com.nectar.doodle.utils.observable


internal class DisplayImpl(htmlFactory: HtmlFactory, private val rootElement: HTMLElement): Display {

    val width  get() = size.width
    val height get() = size.height

    override var insets = None

    override var layout: Layout? by observable<Layout?>(null) { _,_,_ ->
        doLayout()
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
    private val positionableWrapper = PositionableWrapper()

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

    // FIXME: Make this work w/ actual rendering
    override fun fill(brush: Brush) {
        when (brush) {
            is ColorBrush -> {
                canvasElement.parentNode?.removeChild(canvasElement)

                rootElement.style.setBackgroundColor(brush.color)
            }
            is ImageBrush -> {
                canvasElement.parentNode?.removeChild(canvasElement)

                rootElement.style.setBackgroundImage(brush.image)
            }
            else          -> {
                rootElement.insert(canvasElement, 0)
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

    override fun child(at: Point): View? = (layout?.child(positionableWrapper, at) as? com.nectar.doodle.core.PositionableWrapper)?.view ?: {
        var result    = null as View?
        var topZOrder = 0

        children.reversed().forEach {
            if (it.visible && at in it && (result == null || it.zOrder > topZOrder)) {
                result    = it
                topZOrder = it.zOrder
            }
        }

        result
    }()

    override fun iterator() = children.iterator()

    override fun doLayout() {
        layout?.layout(positionableWrapper)
    }

    private fun onResize(@Suppress("UNUSED_PARAMETER") event: Event? = null) {
        size = Size(rootElement.width, rootElement.height)
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
        override val children    get() = this@DisplayImpl.children.map { com.nectar.doodle.core.PositionableWrapper(it) }
    }
}