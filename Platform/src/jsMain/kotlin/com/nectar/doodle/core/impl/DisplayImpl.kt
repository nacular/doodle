package com.nectar.doodle.core.impl

import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
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
import com.nectar.doodle.drawing.TextureBrush
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.nectar.doodle.layout.Insets.Companion.None
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.utils.ObservableList
import com.nectar.doodle.utils.ObservableProperty
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import com.nectar.doodle.utils.observable
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event


internal class DisplayImpl(htmlFactory: HtmlFactory, private val rootElement: HTMLElement): Display {
    private fun onResize(@Suppress("UNUSED_PARAMETER") event: Event? = null) {
        size = Size(rootElement.width, rootElement.height)
    }

    private val canvasElement = htmlFactory.create<HTMLElement>()

    val width  get() = size.width
    val height get() = size.height

    override var insets = None

    override var layout: Layout? by observable<Layout?>(null) { _,_,_ ->
        doLayout()
    }

    override val children by lazy { ObservableList<Display, View>(this) }

    override val cursorChanged: PropertyObservers<Display, Cursor?> by lazy { PropertyObserversImpl<Display, Cursor?>(this) }

    override var cursor: Cursor? by ObservableProperty(null, { this }, cursorChanged as PropertyObserversImpl<Display, Cursor?>)

    override val sizeChanged: PropertyObservers<Display, Size> by lazy { PropertyObserversImpl<Display, Size>(this) }

    override var size by ObservableProperty(Empty, { this }, sizeChanged as PropertyObserversImpl<Display, Size>)

    init {
        rootElement.onresize = ::onResize

        onResize()

//        isFocusCycleRoot = true

        canvasElement.style.setWidthPercent (100.0)
        canvasElement.style.setHeightPercent(100.0)
    }

    override fun zIndex(of: View) = children.size - children.indexOf(of) - 1

    override fun setZIndex(of: View, to: Int)  {
        children.move(of, to)
    }

//    var focusTraversalPolicy: FocusTraversalPolicy
//        get() = ROOT_CONTAINER.getFocusTraversalPolicy()
//        set(aPolicy) {
//            ROOT_CONTAINER.setFocusTraversalPolicy(aPolicy)
//        }

    override fun fill(brush: Brush) {
        when (brush) {
            is ColorBrush   -> {
                canvasElement.parentNode?.removeChild(canvasElement)

                rootElement.style.setBackgroundColor(brush.color)
            }
            is TextureBrush -> {
                canvasElement.parentNode?.removeChild(canvasElement)

                rootElement.style.setBackgroundImage(brush.image)
            }
            else            -> {
                rootElement.insert(canvasElement, 0)
            }
        }
    }

    override infix fun ancestorOf(view: View): Boolean {
        if (children.isNotEmpty()) {
            var child: View? = view

            while (child?.parent != null) {
                child = child.parent
            }

            return child in children
        }

        return false
    }

    override fun child(at: Point): View? = layout?.child(positionableWrapper, at) ?: children.lastOrNull { it.visible && at in it }

    operator fun contains(view: View) = view in children

    override fun iterator() = children.iterator()

    override fun doLayout() {
        layout?.layout(positionableWrapper)
    }

    private inner class PositionableWrapper: Positionable {
        override var size        get() = this@DisplayImpl.size
            set(value) { this@DisplayImpl.size = value }
        override val width       get() = this@DisplayImpl.width
        override val height      get() = this@DisplayImpl.height
        override val insets      get() = this@DisplayImpl.insets
        override val parent            = null as View?
        override val children    get() = this@DisplayImpl.children
        override var idealSize         = null as Size?
        override var minimumSize       = Empty
    }

    private val positionableWrapper = PositionableWrapper()
}