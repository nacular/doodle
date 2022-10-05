package io.nacular.doodle.controls.panels

import io.nacular.doodle.controls.panels.ScrollPanelBehavior.ScrollBarType.Horizontal
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.ConstraintLayout
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.observable
import kotlin.math.max
import kotlin.math.min

/**
 * Configures how a [ScrollPanel] behaves.
 */
public interface ScrollPanelBehavior: Behavior<ScrollPanel> {
    public enum class ScrollBarType { Horizontal, Vertical }

    public val ScrollPanel.children        : ObservableList<View> get() = _children
    public var ScrollPanel.insets          : Insets               get() = _insets;           set(new) { _insets           = new }
    public var ScrollPanel.layout          : Layout?              get() = _layout;           set(new) { _layout           = new }
    public var ScrollPanel.isFocusCycleRoot: Boolean              get() = _isFocusCycleRoot; set(new) { _isFocusCycleRoot = new }

    /**
     * Listener registered by [ScrollPanel] to listen for scroll events from
     * the behavior.  Behaviors should be used this instead of [ScrollPanel.scrollTo]
     * since it bi-passes validation to support things like bouncing.
     */
    public var onScroll: ((Point) -> Unit)?

    /**
     * Listener registered by [ScrollPanel] to listen for scroll bar visibility events from
     * the behavior.
     */
    public var scrollBarSizeChanged: ((ScrollBarType, Double) -> Unit)?

    /**
     * Called by the [ScrollPanel] that this behavior is installed in whenever
     * the panel scrolls.
     *
     * @param panel being scrolled
     * @param point being scrolled to
     */
    public fun scrollTo(panel: ScrollPanel, point: Point)
}

/**
 * A panel that can contain a single [View] that is clips and scrolls.
 *
 * @constructor
 * @param content to host in the panel
 */
@Suppress("PropertyName", "LeakingThis")
public open class ScrollPanel(content: View? = null): View() {
    private val parentChanged: (View, View?, View?) -> Unit = { view,old,new ->
        if (old == this) this.content = null
        if (new == this) this.content = view
    }

    private val sizePreferencesListener: (View, SizePreferences, SizePreferences) -> Unit = { _,_,new ->
        if (matchContentIdealSize) idealSize = new.idealSize
    }

    private var verticalScrollBarSize  : Double by observable(0.0) { _,_ -> relayout() }
    private var horizontalScrollBarSize: Double by observable(0.0) { _,_ -> relayout() }

    /** The content being shown within the panel */
    public var content: View? = null
        set(new) {
            if (new == this) {
                throw IllegalArgumentException("ScrollPanel cannot be added to its self")
            }

            if (field == new) {
                return
            }

            field?.let {
                it.parentChange           -= parentChanged
                it.sizePreferencesChanged -= sizePreferencesListener
                children -= it
                (layout as? ViewLayout)?.clearConstrains()
            }

            val old = field
            field   = new

            field?.let {
                children += it
                it.parentChange           +=  parentChanged
                it.sizePreferencesChanged += sizePreferencesListener
                (layout as? ViewLayout)?.updateConstraints()
            }

            (contentChanged as PropertyObserversImpl).forEach { it(this, old, new) }
        }

    /** Notifies of changes to [content]. */
    public val contentChanged: PropertyObservers<ScrollPanel, View?> by lazy { PropertyObserversImpl(this) }

    /** Determines how the [content] width changes as the panel resizes */
    public var contentWidthConstraints: ScrollPanelConstraintDslContext.(Bounds) -> Double = { content?.idealSize?.width ?: it.width.readOnly }
        set(new) {
            field = new

            (layout as? ViewLayout)?.updateConstraints()
        }

    /** Determines how the [content] height changes as the panel resizes */
    public var contentHeightConstraints: ScrollPanelConstraintDslContext.(Bounds) -> Double = { content?.idealSize?.height ?: it.height.readOnly }
        set(new) {
            field = new

            (layout as? ViewLayout)?.updateConstraints()
        }

    /** The current scroll offset. */
    public var scroll: Point = Origin
        private set

    /** Behavior governing how the panel works */
    public var behavior: ScrollPanelBehavior? by behavior { old, new ->
        mirrorWhenRightLeft = false

        old?.onScroll             = null
        old?.scrollBarSizeChanged = null

        new?.onScroll = {
            scrollTo(it, force = true)
        }
        new?.scrollBarSizeChanged = { type, size ->
            when (type) {
                Horizontal -> horizontalScrollBarSize = size
                else       -> verticalScrollBarSize   = size
            }
        }
    }

    /**
     * The Panel will match its content's ideal size if this set to `true`
     */
    @Internal
    public var matchContentIdealSize: Boolean = true

    // Expose container APIs for behavior
    internal val _children         get() = children
    internal var _insets           get() = insets; set(new) { insets = new }
    internal var _layout           get() = layout; set(new) { layout = new }
    internal var _isFocusCycleRoot get() = isFocusCycleRoot; set(new) { isFocusCycleRoot = new }

    public inner class ScrollPanelConstraintDslContext internal constructor(delegate: ConstraintDslContext): ConstraintDslContext() {
        public val scrollBarWidth : Double get() = this@ScrollPanel.verticalScrollBarSize
        public val scrollBarHeight: Double get() = this@ScrollPanel.horizontalScrollBarSize

        init {
            parent      = delegate.parent
            parent_     = delegate.parent_
            constraints = delegate.constraints
        }
    }

    private val contentConstraints: ConstraintDslContext.(Bounds) -> Unit = {
        it.width  eq max(0.0, contentWidthConstraints (ScrollPanelConstraintDslContext(this), it))
        it.height eq max(0.0, contentHeightConstraints(ScrollPanelConstraintDslContext(this), it))
    }

    init {
        mirrorWhenRightLeft = false

        this.content = content

        layout = ViewLayout()
    }

    public override var focusable: Boolean = false

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point): Boolean = super.contains(point) && behavior?.contains(this, point) ?: true

    /**
     * Scrolls the viewport so the top-left is at point, or as close as possible.
     *
     * @param point
     */
    public fun scrollTo(point: Point) {
        scrollTo(point, false)

        behavior?.scrollTo(this, point)
    }

    /**
     * Scrolls the viewport by [delta].x and [delta].y in the x and y directions respectively.
     *
     * @param delta
     */
    public fun scrollBy(delta: Point): Unit = scrollTo(scroll + delta)

    /**
     * Scrolls the viewport so the given point is visible.
     *
     * @param point
     */
    public fun scrollToVisible(point: Point): Unit = moveToVisible(point)

    /**
     * Scrolls the viewport so the given rectangle is visible.
     *
     * @param rect
     */
    public fun scrollToVisible(rect: Rectangle): Unit = moveToVisible(rect)

    /**
     * Scrolls the viewport horizontally so the given range is visible.
     *
     * @param range
     */
    public fun scrollHorizontallyToVisible(range: ClosedRange<Double>): Unit = moveHorizontallyToVisible(range)

    /**
     * Scrolls the viewport vertically so the given range is visible.
     *
     * @param range
     */
    public fun scrollVerticallyToVisible(range: ClosedRange<Double>): Unit = moveVerticallyToVisible(range)

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

        override fun requiresLayout(child: Positionable, of: PositionableContainer, old: SizePreferences, new: SizePreferences) = new.idealSize != old.idealSize && matchContentIdealSize

        override fun layout(container: PositionableContainer) {
            delegate?.layout(container)

            container.children.forEach  {
                it.position = Point(-scroll.x, -scroll.y)
            }
        }

        fun clearConstrains() {
            content?.let { delegate?.unconstrain(it, contentConstraints) }
        }

        fun updateConstraints() {
            delegate = content?.let { content ->
                constrain(content, contentConstraints)
            }
        }
    }
}