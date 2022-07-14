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
import io.nacular.doodle.layout.ConstraintLayout
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.HorizontalConstraint
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.MagnitudeConstraint
import io.nacular.doodle.layout.ParentConstraints
import io.nacular.doodle.layout.VerticalConstraint
import io.nacular.doodle.layout.constant
import io.nacular.doodle.layout.constrain
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
    /**
     * Constraints representing a parent [ScrollPanel].
     */
    public interface ScrollPanelConstraints: ParentConstraints {
        /** The panel's scroll bar width */
        public val scrollBarWidth: MagnitudeConstraint

        /** The panel's scroll bar height */
        public val scrollBarHeight: MagnitudeConstraint
    }

    /**
     * Constraints representing the contents of a [ScrollPanel].
     */
    public interface ContentConstraints: ParentConstraints {
        override var top    : VerticalConstraint
        override var centerY: VerticalConstraint
        override var bottom : VerticalConstraint
        override var height : MagnitudeConstraint

        override var left   : HorizontalConstraint
        override var centerX: HorizontalConstraint
        override var right  : HorizontalConstraint
        override var width  : MagnitudeConstraint

        override var center: Pair<HorizontalConstraint, VerticalConstraint> get() = centerX to centerY
            set(value) {
                centerX = value.first
                centerY = value.second
            }

        public val parent: ScrollPanelConstraints
    }

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
    public var contentWidthConstraints: ContentConstraints.() -> MagnitudeConstraint = { idealWidth or width }
        set(new) {
            field = new

            (layout as? ViewLayout)?.updateConstraints()
        }

    /** Determines how the [content] height changes as the panel resizes */
    public var contentHeightConstraints: ContentConstraints.() -> MagnitudeConstraint = { idealHeight or height }
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
            content?.let { delegate?.unconstrain(it) }
        }

        fun updateConstraints() {
            delegate = content?.let { content ->
                constrain(content) {
                    val width  = contentWidthConstraints (it.map())
                    val height = contentHeightConstraints(it.map())

                    it.width  = width
                    it.height = height
                }
            }
        }

        private fun Constraints.map() = object: ContentConstraints {
            override var top         get() = this@map.top;     set(new) { this@map.top     = new }
            override var centerY     get() = this@map.centerY; set(new) { this@map.centerY = new }
            override var bottom      get() = this@map.bottom;  set(new) { this@map.bottom  = new }
            override var height      get() = this@map.height;  set(new) { this@map.height  = new }
            override var left        get() = this@map.left;    set(new) { this@map.left    = new }
            override var centerX     get() = this@map.centerX; set(new) { this@map.centerX = new }
            override var right       get() = this@map.right;   set(new) { this@map.right   = new }
            override var width       get() = this@map.width;   set(new) { this@map.width   = new }
            override val idealHeight get() = this@map.idealHeight
            override val idealWidth  get() = this@map.idealWidth

            override val parent  get() = this@map.parent.map()
        }

        private fun ParentConstraints.map() = object: ScrollPanelConstraints {
            override val scrollBarWidth  get() = constant(0.0) + { verticalScrollBarSize   }
            override val scrollBarHeight get() = constant(0.0) + { horizontalScrollBarSize }

            override val top         get() = this@map.top
            override val centerY     get() = this@map.centerY
            override val bottom      get() = this@map.bottom
            override val height      get() = this@map.height
            override val left        get() = this@map.left
            override val centerX     get() = this@map.centerX
            override val right       get() = this@map.right
            override val width       get() = this@map.width
            override val idealHeight get() = this@map.idealHeight
            override val idealWidth  get() = this@map.idealWidth
        }
    }
}