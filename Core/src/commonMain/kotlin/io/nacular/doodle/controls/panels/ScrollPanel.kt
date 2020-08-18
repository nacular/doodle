package io.nacular.doodle.controls.panels

import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.ConstraintLayout
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.MagnitudeConstraint
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
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
    private val sizePreferencesListener: (View, SizePreferences, SizePreferences) -> Unit = { _,_,new ->
        idealSize = new.idealSize
        doLayout()
    }

    /** The content being shown within the panel */
    var content = null as View?
        set(new) {
            if (new == this) {
                throw IllegalArgumentException("ScrollPanel cannot be added to its self")
            }

            field?.let {
                children -= it
                it.sizePreferencesChanged -= sizePreferencesListener
                (layout as? ViewLayout)?.clearConstrains()
            }

            if (field != new) {
                val old = field
                field   = new

                field?.let {
                    children += it
                    it.sizePreferencesChanged += sizePreferencesListener
                }

                (contentChanged as PropertyObserversImpl).forEach { it(this, old, new) }
            }
        }

    /** Notifies of changes to [content]. */
    val contentChanged: PropertyObservers<ScrollPanel, View?> by lazy { PropertyObserversImpl<ScrollPanel, View?>(this) }

    /** Determines how the [content] width changes as the panel resizes */
    var contentWidthConstraints: Constraints.() -> MagnitudeConstraint = { idealWidth or width }
        set(new) {
            field = new

            (layout as? ViewLayout)?.updateConstraints()
        }

    /** Determines how the [content] height changes as the panel resizes */
    var contentHeightConstraints: Constraints.() -> MagnitudeConstraint = { idealHeight or height }
        set(new) {
            field = new

            (layout as? ViewLayout)?.updateConstraints()
        }

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

        layout = ViewLayout()
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point) = super.contains(point) && behavior?.contains(this, point) ?: true

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

    private inner class ViewLayout: Layout {
        var delegate = null as ConstraintLayout?

        init {
            updateConstraints()
        }

        override fun layout(container: PositionableContainer) {
            delegate?.layout(container)

            container.children.forEach  {
                it.position = Point(-scroll.x, -scroll.y)
            }
        }

        fun clearConstrains() {
            content?.let { delegate?.unconstrain(it) }
        }

        fun updateConstraints() {
            delegate = content?.let { content ->
                constrain(content) {
                    val width  = contentWidthConstraints (it)
                    val height = contentHeightConstraints(it)

                    it.width  = width
                    it.height = height
                }
            }
        }
    }
}