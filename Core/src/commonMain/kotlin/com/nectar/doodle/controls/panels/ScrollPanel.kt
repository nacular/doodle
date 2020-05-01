package com.nectar.doodle.controls.panels

import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.PositionableContainer
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


/**
 * Configures how a [ScrollPanel] behaves.
 */
interface ScrollPanelBehavior: Behavior<ScrollPanel> {
    /**
     * Listener registered by [ScrollPanel] to listen for scroll events from
     * the behavior.  Behaviors should be used this instead of [ScrollPanel.scrollTo]
     * since it bi-passes validation to support things like bouncing.
     */
    var onScroll: ((Point) -> Unit)?

    /**
     * Called by the [ScrollPanel] that this behavior is installed in whenever
     * the panel scrolls.
     */
    fun scrollTo(point: Point)
}

/**
 * A panel that can contain a single [View] that is clips and scrolls.
 *
 * @constructor
 * @param content to host in the panel
 */
open class ScrollPanel(content: View? = null): View() {
    /** The content being shown within the panel */
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

    /** Notifies of changes to [content]. */
    val contentChanged: PropertyObservers<ScrollPanel, View?> by lazy { PropertyObserversImpl<ScrollPanel, View?>(this) }

    /** Allows vertical scrolling when set to `true`. Defaults to `true`. */
    var scrollsVertically = true

    /** Allows horizontal scrolling when set to `true`. Defaults to `true`. */
    var scrollsHorizontally = true

    /** The current scroll offset. */
    var scroll = Origin
        private set

    /** Behavior governing how the panel works */
    var behavior: ScrollPanelBehavior? = null
        set(new) {
            field?.onScroll = null

            field = new?.also { behavior ->
                behavior.onScroll = {
                    scrollTo(it, force = true)
                }

                behavior.install(this)
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

    /**
     * Scrolls the viewport horizontally so the given range is visible.
     *
     * @param range
     */
    fun scrollHorizontallyToVisible(range: ClosedRange<Double>) = moveHorizontallyToVisible(range)

    /**
     * Scrolls the viewport vertically so the given range is visible.
     *
     * @param range
     */
    fun scrollVerticallyToVisible(range: ClosedRange<Double>) = moveVerticallyToVisible(range)

    private fun moveToVisible(point: Point) {
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

    private fun moveToVisible(rect: Rectangle) {
        moveToVisible(rect.position)
        moveToVisible(Point(rect.right, rect.bottom))
    }

    private fun moveHorizontallyToVisible(range: ClosedRange<Double>) {
        moveToVisible(Point(range.start,        scroll.y))
        moveToVisible(Point(range.endInclusive, scroll.y))
    }

    private fun moveVerticallyToVisible(range: ClosedRange<Double>) {
        moveToVisible(Point(scroll.x, range.start       ))
        moveToVisible(Point(scroll.x, range.endInclusive))
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
        override fun layout(container: PositionableContainer) {
            container.children.forEach  {
                val width  = if (scrollsHorizontally) it.idealSize?.width  ?: it.width  else this@ScrollPanel.width
                val height = if (scrollsVertically  ) it.idealSize?.height ?: it.height else this@ScrollPanel.height

                it.bounds = Rectangle(-scroll.x, -scroll.y, width, height)
            }
        }
    }
}