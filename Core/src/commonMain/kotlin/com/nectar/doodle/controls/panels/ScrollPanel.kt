package com.nectar.doodle.controls.panels

import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import kotlin.math.max
import kotlin.math.min


interface ScrollPanelBehavior: Behavior<ScrollPanel> {
    var onScroll: ((Point) -> Unit)?

    fun scrollTo(point: Point)
}

open class ScrollPanel(content: View? = null): View() {

    var content = null as View?
        set(new) {
            if (new == this) {
                throw IllegalArgumentException("ScrollPanel cannot be added to its self")
            }

            field?.let {
                children -= it
            }

            if (field != new) {
                val old = field
                field   = new

                field?.let {
                    children += it
                }

                (contentChanged as PropertyObserversImpl).forEach { it(this, old, new) }
            }
        }

    val contentChanged: PropertyObservers<ScrollPanel, View?> by lazy { PropertyObserversImpl<ScrollPanel, View?>(this) }

    var scrollsVertically   = true
    var scrollsHorizontally = true

    var scroll = Origin
        private set

    var behavior: ScrollPanelBehavior? = null
        set(new) {
            field?.onScroll = null

            field = new?.also {
                it.onScroll = {
                    scrollTo(it, force = true)
                }
            }
        }

    init {
        this.content = content
        layout       = ViewLayout()
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point) = behavior?.contains(this, point) ?: super.contains(point)

    /**
     * Scrolls the viewport so the top-left is at point, or as close as possible.
     *
     * @param point
     */
    fun scrollTo(point: Point) {
        scrollTo(point, false)

        behavior?.scrollTo(point)
    }

    /**
     * Scrolls the viewport by point.x and and point.y in the x and y directions respectively.
     *
     * @param delta
     */
    fun scrollBy(delta: Point) = scrollTo(scroll + delta)

    /**
     * Scrolls the viewport so the given point is visible.
     *
     * @param point
     */
    fun scrollToVisible(point: Point) = moveToVisible(point)

    /**
     * Scrolls the viewport so the given rectangle is visible.
     *
     * @param rect
     */
    fun scrollToVisible(rect: Rectangle) = moveToVisible(rect)

    protected fun moveToVisible(point: Point) {
        var farSide = scroll.x + width

        val x = when {
            point.x > farSide  -> point.x - farSide
            point.x < scroll.x -> point.x - scroll.x
            else               -> 0.0
        }

        farSide = scroll.y + height

        val y = when {
            point.y > farSide  -> point.y - farSide
            point.y < scroll.y -> point.y - scroll.y
            else               -> 0.0
        }

        scrollBy(Point(x, y))
    }

    protected fun moveToVisible(rect: Rectangle) {
        moveToVisible(rect.position)
        moveToVisible(Point(rect.right, rect.bottom))
    }

    private fun scrollTo(point: Point, force: Boolean = false) {
        content?.let {
            if (scroll != point) {
                val newScroll = when (force) {
                    true  -> point
                    false -> Point(max(0.0, min(point.x, it.width - width)), max(0.0, min(point.y, it.height - height)))
                }

                scroll      =  newScroll
                it.position = -newScroll
            }
        }
    }

    private inner class ViewLayout: Layout() {
        override fun layout(positionable: Positionable) {
            positionable.children.forEach  {
                var width  = it.width
                var height = it.height

                it.bounds = Rectangle(-scroll.x, -scroll.y, width, height)
            }
        }
    }
}