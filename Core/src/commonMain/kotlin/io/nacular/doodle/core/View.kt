@file:Suppress("NestedLambdaShadowedImplicitParameter", "FunctionName", "PropertyName")

package io.nacular.doodle.core

import io.nacular.doodle.accessibility.AccessibilityManager
import io.nacular.doodle.accessibility.AccessibilityRole
import io.nacular.doodle.core.ContentDirection.LeftRight
import io.nacular.doodle.core.ContentDirection.RightLeft
import io.nacular.doodle.core.LookupResult.Empty
import io.nacular.doodle.core.LookupResult.Found
import io.nacular.doodle.core.LookupResult.Ignored
import io.nacular.doodle.datatransport.dragdrop.DragOperation
import io.nacular.doodle.datatransport.dragdrop.DragRecognizer
import io.nacular.doodle.datatransport.dragdrop.DropReceiver
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.RenderManager
import io.nacular.doodle.drawing.Renderable
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.KeyState
import io.nacular.doodle.event.KeyState.Type
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.focus.FocusTraversalPolicy
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets.Companion.None
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.SystemPointerEvent.Type.Down
import io.nacular.doodle.system.SystemPointerEvent.Type.Drag
import io.nacular.doodle.system.SystemPointerEvent.Type.Enter
import io.nacular.doodle.system.SystemPointerEvent.Type.Exit
import io.nacular.doodle.system.SystemPointerEvent.Type.Move
import io.nacular.doodle.system.SystemPointerEvent.Type.Up
import io.nacular.doodle.utils.ChangeObserver
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.ObservableProperty
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.observable
import kotlin.js.JsName
import kotlin.reflect.KProperty

private typealias BooleanObservers = PropertyObservers<View, Boolean>

private typealias ZOrderObservers = PropertyObservers<View, Int>

internal typealias ChildObserver = (source: View, removed: Map<Int, View>, added: Map<Int, View>, moved: Map<Int, Pair<Int, View>>) -> Unit

/**
 * The smallest unit of displayable, interactive content within doodle.  Views are the visual entities used to display components for an application.
 * User input events are sent to all Views that are configured to receive them. This allows them to response to user interaction or convey such events to
 * other parts of an application.
 *
 * @author Nicholas Eddy
 *
 * @constructor
 * @property accessibilityRole indicates the View's role for screen readers
 */
abstract class View protected constructor(val accessibilityRole: AccessibilityRole? = null): Renderable {
    private inner class ChildObserversImpl(mutableSet: MutableSet<ChildObserver> = mutableSetOf()): SetPool<ChildObserver>(mutableSet) {
        operator fun invoke(removed: Map<Int, View>, added: Map<Int, View>, moved: Map<Int, Pair<Int, View>>) = delegate.forEach { it(this@View, removed, added, moved) }
    }

    /** Left edge of [bounds] */
    var x: Double
        get( ) = bounds.x
        set(x) = setBounds(x, y, width, height)

    /** Top edge of [bounds] */
    var y: Double
        get( ) = bounds.y
        set(y) = setBounds(x, y, width, height)

    /** Top-left corner of [bounds] */
    var position: Point
        get(        ) = bounds.position
        set(position) = setBounds(position.x, position.y, width, height)

    /** Horizontal extent of [bounds] */
    var width: Double
        get(     ) = bounds.width
        set(width) = setBounds(x, y, width, height)

    /** Vertical extent of [bounds] */
    var height: Double
        get(      ) = bounds.height
        set(height) = setBounds(x, y, width, height)

    /** Width-height of [bounds]*/
    override var size: Size
        get(    ) = bounds.size
        set(size) = setBounds(x, y, size.width, size.height)

    /** Notifies changes to [bounds]: [x], [y], [width], [height] */
    val boundsChanged: PropertyObservers<View, Rectangle> by lazy { PropertyObserversImpl<View, Rectangle>(this) }

    /**
     * The top, left, width, and height with respect to [parent], or the [Display] if top-level.  Unlike [boundingBox], this value isn't affected
     * by any applied [transform].
     */
    var bounds: Rectangle by object: ObservableProperty<View, Rectangle>(Empty, { this }, boundsChanged as PropertyObserversImpl) {
        override fun afterChange(property: KProperty<*>, oldValue: Rectangle, newValue: Rectangle) {
            boundingBox = transform(newValue).boundingRectangle

            super.afterChange(property, oldValue, newValue)
        }
    }

    internal val clipCanvasToBounds_ get() = clipCanvasToBounds

    /**
     * Indicates whether the View's [Canvas] will be clipped so that nothing rendered shows beyond its [bounds].  Set this to `false` to support
     * things like shadows or glows that aren't intended to be included in the normal bounding box.
     *
     * This property does not affect the clipping of child Views and their descendants; these are always clipped to the parent bounds.
     *
     * The default is `true`
     */
    protected var clipCanvasToBounds = true
        set(new) {
            if (field != new) {
                field = new

                rerender() // TODO: Should this notify instead?
            }
        }

    /**
     * A [Polygon] used to further clip the View's children within its [bounds]. The View's children cannot extend
     * beyond its [bounds], so specifying a value larger than it will not enable that.
     *
     * The default is `null`.
     */
    protected var childrenClipPoly: Polygon? = null
        set(new) {
            if (field != new) {
                field = new

                rerender() // TODO: Should this notify instead?
            }
        }

    internal val childrenClipPoly_ get() = childrenClipPoly

    /** Notifies changes to [transform] */
    val transformChanged: PropertyObservers<View, AffineTransform> by lazy { PropertyObserversImpl<View, AffineTransform>(this) }

    /**
     * Affine transform applied to the View.  This transform does not affect the View's [bounds] or how it is handled by [Layout].
     * It does affect the [boundingBox], and how the View looks when rendered.  Hit-detection is handled correctly such that the pointer
     * intersects with the View as expected after transformation.  So no additional handling is necessary in general.
     * The default is [Identity]
     */
    open var transform by object: ObservableProperty<View, AffineTransform>(Identity, { this }, transformChanged as PropertyObserversImpl) {
        override fun afterChange(property: KProperty<*>, oldValue: AffineTransform, newValue: AffineTransform) {
            boundingBox = newValue(bounds).boundingRectangle

            super.afterChange(property, oldValue, newValue)
        }
    }

    /** Smallest enclosing [Rectangle] around the View's [bounds] given it's [transform]. */
    var boundingBox = bounds; private set

    /** Size that would best display this View, or `null` if no preference */
    var idealSize: Size? = null
        get(   ) = layout?.idealSize(positionableWrapper, field) ?: field
        set(new) {
            if (field == new) return
            val old = field
            field = new
            (sizePreferencesChanged as PropertyObserversImpl).forEach {
                it(this, SizePreferences(old, minimumSize), SizePreferences(new, minimumSize))
            }
        }

    /** Minimum size preferred by the View, default is [Empty][Size.Empty] */
    var minimumSize = Size.Empty
        get(   ) = layout?.minimumSize(positionableWrapper, field) ?: field
        set(new) {
            if (field == new) return
            val old = field
            field = new
            (sizePreferencesChanged as PropertyObserversImpl).forEach {
                it(this, SizePreferences(idealSize, old), SizePreferences(idealSize, new))
            }
        }

    /** Indicates the minimum and ideal sizes for a View. */
    data class SizePreferences(val idealSize: Size?, val minimumSize: Size)

    /** Notifies changes to [idealSize] or [minimumSize] */
    val sizePreferencesChanged: PropertyObservers<View, SizePreferences> by lazy { PropertyObserversImpl<View, SizePreferences>(this) }

    /**
     * Current visible [Rectangle] for this View within it's coordinate space.  This accounts for clipping by ancestors,
     * but **NOT** cousins (siblings, anywhere in the hierarchy)
     */
    val displayRect get() = renderManager?.displayRect(this) ?: Empty

    /** Notifies changes to [zOrder] */
    internal val zOrderChanged: ZOrderObservers by lazy { PropertyObserversImpl<View, Int>(this) }

    /**
     * Rendering order of this View within it's [parent], or [Display] if top-level.
     * Views with higher values are rendered above those with lower ones. The default is `0`.
     */
    var zOrder by ObservableProperty(0, { this }, zOrderChanged as PropertyObserversImpl<View, Int>)

    /** Notifies changes to [visible] */
    val visibilityChanged: BooleanObservers by lazy { PropertyObserversImpl<View, Boolean>(this) }

    /** Whether this View is visible.  The default is `true`. */
    var visible by ObservableProperty(true, { this }, visibilityChanged as PropertyObserversImpl<View, Boolean>)

    /** Notifies changes to [enabled] */
    val enabledChanged: BooleanObservers by lazy { PropertyObserversImpl<View, Boolean>(this) }

    /** Whether this View is enabled.  The default is `true`.  */
    var enabled by ObservableProperty(true, { this }, enabledChanged as PropertyObserversImpl<View, Boolean>)

    /** Notifies changes to [focusable] */
    val focusabilityChanged: BooleanObservers by lazy { PropertyObserversImpl<View, Boolean>(this) }

    /** Whether this View is focusable  The default is `true`.  */
    open var focusable by ObservableProperty(true, { this }, focusabilityChanged as PropertyObserversImpl<View, Boolean>)

    /** Notifies changes to [hasFocus] */
    val focusChanged: BooleanObservers by lazy { PropertyObserversImpl<View, Boolean>(this) }

    /** Whether the View has focus or not.  The default is `false`.  */
    var hasFocus by ObservableProperty(false, { this }, focusChanged as PropertyObserversImpl<View, Boolean>)
        private set

    /**
     * View that contains this one as a child, or `null`.  A top-level Views will also return `null`; but they will also have
     * [displayed] `== true`; so parent alone isn't sufficient to determine whether a View is in the display hierarchy.
     */
    var parent: View? = null
        // [Performance]
        // No check to prevent setting self as parent since View is the only place where this method is called and this is already
        // prevented by checks when adding to children.
        private set(new) {
            if (field === new) {
                return
            }

            field?.children?.remove(this)

            val old = field
            field   = new

            (parentChange as PropertyObserversImpl)(old, new)
        }

    /** Notifies changes to [parent] */
    val parentChange: PropertyObservers<View, View?> by lazy { PropertyObserversImpl<View, View?>(this) }

    /** Notifies changes to [displayed] */
    val displayChange: BooleanObservers by lazy { PropertyObserversImpl<View, Boolean>(this) }

    /** Is `true` if the View is currently within the [Display] */
    val displayed get() = renderManager != null

    /** The current text to display for tool-tips.  The default is the empty string.  */
    var toolTipText = ""

    private var actualCursor: Cursor? = null

    /** Cursor that is displayed whenever the pointer is over this View. This falls back to the [parent]'s Cursor if not set. */
    var cursor: Cursor? = null
        get(   ) = actualCursor ?: parent?.cursor
        set(new) {
            if (actualCursor == new) return

            val old = field

            actualCursor = new

            cursorChanged(old, new)
        }

    /** Notifies changes to [cursor] */
    val cursorChanged: PropertyObservers<View, Cursor?> by lazy { PropertyObserversImpl<View, Cursor?>(this) }

    private var actualFont: Font? = null

    /** Optional font that the View could use for rendering.  This falls back to [parent]'s font if not set. */
    var font: Font?
        get(   ) = actualFont ?: parent?.font
        set(new) {
            if (actualFont == new) return

            actualFont = new

            styleChanged { it.actualFont == null }
        }

    /** Optional color that the View could use for its foreground (i.e. text) */
    var foregroundColor: Color? = null; set(new) { field = new; styleChanged { it.foregroundColor == null } }

    /** Optional color that the View could use for its background */
    var backgroundColor: Color? = null; set(new) { field = new; styleChanged { it.backgroundColor == null } }

    /** Notifies changes to [font], [foregroundColor], or [backgroundColor] */
    val styleChanged: Pool<ChangeObserver<View>> by lazy { ChangeObserversImpl(this) }

    /** Notifies changes to [localContentDirection] */
    val contentDirectionChanged: Pool<ChangeObserver<View>> by lazy { ChangeObserversImpl(this) }

    /**
     * Determines whether the View will be affected by [Theme][io.nacular.doodle.theme.Theme]s set in [ThemeManager][io.nacular.doodle.theme.ThemeManager].
     * Defaults to `true`
     */
    var acceptsThemes = true

    private val pointerFilter_ by lazy { SetPool<PointerListener>() }

    /** [PointerListener]s that are notified during the sinking phase of pointer event handling. */
    val pointerFilter: Pool<PointerListener> get() = pointerFilter_

    private val pointerChanged_ by lazy { SetPool<PointerListener>() }

    /** [PointerListener]s that are notified during the bubbling phase of pointer event handling. */
    val pointerChanged: Pool<PointerListener> get() = pointerChanged_

    private val keyChanged_ by lazy { SetPool<KeyListener>() }

    /** [KeyListener]s that are notified during of [KeyEvent]s sent to the View. */
    val keyChanged: Pool<KeyListener> get() = keyChanged_

    private val pointerMotionFilter_ by lazy { SetPool<PointerMotionListener>() }

    /** [PointerMotionListener]s that are notified during the sinking phase of pointer-motion event handling. */
    val pointerMotionFilter: Pool<PointerMotionListener> get() = pointerMotionFilter_

    private val pointerMotionChanged_ by lazy { SetPool<PointerMotionListener>() }

    /** [PointerMotionListener]s that are notified during the sinking phase of pointer-motion event handling. */
    val pointerMotionChanged: Pool<PointerMotionListener> get() = pointerMotionChanged_

    /** Recognizer used to determine whether a [PointerEvent] should result in a [DragOperation] */
    var dragRecognizer = null as DragRecognizer?

    /** Receiver that determines what drop operations are supported by the View */
    var dropReceiver = null as DropReceiver?

    /** Notifies changes to [monitorsDisplayRect] */
    val displayRectHandlingChanged: BooleanObservers by lazy { PropertyObserversImpl<View, Boolean>(this) }

    /**
     * Indicates whether the framework should notify the View of changes to its visible region as a result of
     * changes to bounds in its ancestor chain.  The events for this require monitoring potentially large sets
     * of Views in the hierachy and the events can be frequent during layout changes.  So the default value is
     * `false`.  But it is useful for things like efficient rendering of sub-portions (i.e. long list-like Views)
     * where the cost is outweighed.
     *
     * NOTE: the framework does not notify of clipping due to siblings that overlap with a View (or ancestors).
     * That means a View can be notified of a display rect change and still not be visible to the user.
     */
    var monitorsDisplayRect by ObservableProperty(false, { this }, displayRectHandlingChanged as PropertyObserversImpl<View, Boolean>)

    /**
     * Indicates the direction of content within the View; used to support right-to-left locales.
     * Setting this property to `null` will allow the View to inherit the value from its [parent].
     * The resolved value is obtained via [contentDirection]; and it will never be `null`.
     */
    var localContentDirection: ContentDirection? = null
        set(new) {
            if (field == new) return

            field = new

            contentDirectionChanged()
        }

    /**
     * The resolved content direction for this View, including any value inherited from its [parent] if
     * [localContentDirection] is `null`. The final fallback value is [LeftRight].
     */
    val contentDirection: ContentDirection
        get() = localContentDirection ?: parent?.contentDirection ?: display?.contentDirection ?: LeftRight

    /**
     * Indicates whether the View should be mirrored (as though transformed using [AffineTransform.flipHorizontally]),
     * when the [contentDirection] is [RightLeft][ContentDirection.RightLeft].
     *
     * Views should set this to `false` if the want to handle right-left flow themselves. This includes those
     * that render text.
     *
     * Defaults to `true`
     */
    var mirrorWhenRightLeft = true
        protected set(new) {
            if (field == new) return
            field = new

            updateNeedsMirror()
        }

    /**
     * Indicates whether the framework should apply an additional [AffineTransform]
     * to ensure the View is properly oriented based on its [mirrored] state relative
     * to that of its parent.
     */
    internal var needsMirrorTransform = mirrored != (parent?.mirrored == true)
        private set(new) {
            if (field == new) return

            field = new

            rerender()

            children.forEach { it.updateNeedsMirror() }
        }

    internal fun contentDirectionChanged_() = contentDirectionChanged()

    @JsName("fireContentDirectionChanged")
    protected fun contentDirectionChanged() {
        updateNeedsMirror()

        (contentDirectionChanged as ChangeObserversImpl)()

        // Notify relevant children that their content direction has changed
        children.filter { it.localContentDirection == null }.forEach { it.contentDirectionChanged() }
    }

    @JsName("fireStyleChanged")
    protected fun styleChanged(filter: (View) -> Boolean) {
        (styleChanged as ChangeObserversImpl)()

        // Notify relevant children that their style has changed
        children.filter(filter).forEach { it.styleChanged(filter) }
    }

    @JsName("fireCursorChanged")
    protected fun cursorChanged(old: Cursor?, new: Cursor?) {
        (cursorChanged as PropertyObserversImpl<View, Cursor?>)(old, new)

        // Notify relevant children that their cursor has changed
        children.filter { it.actualCursor == null }.forEach {
            it.cursorChanged(old, new)
        }
    }

    /**
     * `true` if the View's [contentDirection] is [RightLeft] and [mirrorWhenRightLeft] is `true`.
     */
    private val mirrored get() = contentDirection == RightLeft && mirrorWhenRightLeft

    /**
     * Refresh [needsMirrorTransform] flag to allow rerender if needed.
     */
    internal fun updateNeedsMirror() {
        needsMirrorTransform = mirrored != (parent?.mirrored ?: display?.mirrored == true)
    }

    // ================= Container ================= //
    internal val insets_ get() = insets

    /** Insets used to control how [Layout]s set the View's children away from its edge. */
    protected open var insets = None

    internal val layout_ get() = layout

    /** Layout responsible for positioning of this View's children */
    protected open var layout: Layout? by observable<Layout?>(null) { _,_,_ ->
        // TODO: Have RenderManager manage the layout?
        relayout()
    }

    internal val childrenChanged_ get() = childrenChanged

    internal val children_ get() = children

    /** List of child Views within this one */
    protected open val children by lazy {
        ObservableList<View>().apply {
            changed += { _, removed, added, moved ->
                removed.values.forEach {
                    it.parent   = null
                    it.zOrder   = 0
                    it.position = Origin
                }
                added.values.forEach {
                    require(it !== this@View         ) { "cannot add to self"                }
                    require(!it.ancestorOf(this@View)) { "cannot add ancestor to descendant" }

                    it.parent = this@View
                }

                (childrenChanged_ as ChildObserversImpl).invoke(removed, added, moved)
            }
        }
    }

    /** Notifies changes to [children] */
    protected val childrenChanged: Pool<ChildObserver> by lazy { ChildObserversImpl() }

    /**
     * Tells whether this View is an ancestor of the given View.  A View is not considered an ancestor of itself.
     *
     * @param view The View
     * @return `true` if the given View is a descendant of this one
     */
    protected open infix fun ancestorOf(view: View): Boolean {
        if (children.isNotEmpty()) {
            var parent = view.parent

            while (parent != null) {
                if (this === parent) {
                    return true
                }

                parent = parent.parent
            }
        }

        return false
    }

    internal infix fun ancestorOf_(view: View) = this ancestorOf view

    internal open var isFocusCycleRoot_ get() = isFocusCycleRoot
        set(new) { isFocusCycleRoot = new }

    protected open var isFocusCycleRoot = false

    internal val focusCycleRoot_ get() = focusCycleRoot
    protected val focusCycleRoot: View? get() {
        var result = parent

        while (result != null && !result.isFocusCycleRoot) {
            result = result.parent
        }

        return result
    }

    internal val focusTraversalPolicy_ get() = focusTraversalPolicy
    protected open var focusTraversalPolicy = null as FocusTraversalPolicy?

    internal var display             : Display?              = null; private set
    private  var renderManager       : RenderManager?        = null
    private  var accessibilityManager: AccessibilityManager? = null

    private val traversalKeys: MutableMap<TraversalType, Set<KeyState>> by lazy { mutableMapOf<TraversalType, Set<KeyState>>() }

    internal fun revalidate_() = revalidate()

    protected fun revalidate() {
        relayout()
        rerender()
    }

    /**
     * Tells whether this View is child's parent.  Unlike [ancestorOf], this checks only a parent-child relationship.
     *
     * @param child The View being tested
     * @return `true` IFF the View is a child of the View
     */
    protected operator fun contains(child: View) = child.parent == this

    /** Prompts the View to layout its children if it has a Layout installed. */
    protected open fun relayout() = renderManager?.layout(this)

    internal fun doLayout_() = doLayout()

    /** Causes the [layout] (if any) to re-layout the View's [children] */
    protected open fun doLayout() = layout?.layout(positionableWrapper)

    /**
     * Gets the child (if any) at the given point in the View's coordinate system (relative to the View).
     *
     * @param at The point being tested
     * @return The child (`null` if no child contains the given point)
     */
    protected open fun child(at: Point): View? = when {
        false == childrenClipPoly?.contains(at) -> null
        else                                    -> when (val result = layout?.child(positionableWrapper, at)) {
            null, Ignored -> {
                var child = null as View?
                var topZOrder = 0

                children.reversed().forEach {
                    if (it.visible && at in it && (child == null || it.zOrder > topZOrder)) {
                        child = it
                        topZOrder = it.zOrder
                    }
                }

                child
            }
            is Found      -> (result.child as PositionableWrapper).view
            is Empty      -> null
        }
    }

    internal fun child_(at: Point) = child(at)

    /**
     * Gives the View an opportunity to render itself to the given Canvas.
     *
     * @param canvas The canvas upon which drawing will be done
     */
    override fun render(canvas: Canvas) {}

    /**
     * Request the rendering subsystem to trigger a [render] call if needed.
     */
    fun rerender() = renderManager?.render(this)

    /**
     * Request the rendering subsystem to trigger a [render] call with no delay. Only use this method for time-sensitive drawing as is the case for animations.
     */
    fun rerenderNow() = renderManager?.renderNow(this) // TODO: Remove?

    /**
     * Gets the tool-tip text based on the given [PointerEvent].  Override this method to provide multiple tool-tip text values for a single View.
     *
     * @param for The pointer event to generate a tool-tip for
     * @return The text
     */
    open fun toolTipText(@Suppress("UNUSED_PARAMETER") `for`: PointerEvent): String = toolTipText

    private val resolvedTransform get() = when {
        needsMirrorTransform -> transform.flipHorizontally(at = center.x)
        else                 -> transform
    }

    /**
     * Checks whether a point (relative to [parent] or [Display] if top-level) is within the View's bounds.  This check accounts for [transforms][AffineTransform]
     * within the View's hierarchy as well.
     *
     * @param point The point to check
     * @return `true` IFF the point falls within the View
     */
    open operator fun contains(point: Point) = resolvedTransform.inverse?.invoke(point)?.let { it in bounds } ?: false

    /**
     * Gets the set of keys used to trigger this type of focus traversal.
     *
     * @return The set of keys that will trigger this type of traversal
     */
    operator fun get(traversalType: TraversalType): Set<KeyState>? {
        return traversalKeys[traversalType]
    }

    /**
     * Sets the keys used to control focus traversals of the given type.
     *
     * @param traversalType The traversal type
     * @param keyStates     The set of keys that will trigger this type of traversal
     */
    operator fun set(traversalType: TraversalType, keyStates: Set<KeyState>?) {
        if (keyStates != null) {
            traversalKeys[traversalType] = keyStates
        } else {
            traversalKeys.remove(traversalType)
        }
    }

    /**
     * Maps a [Point] within from View into this View's coordinate-space.
     *
     * @param   point within [from]
     * @param   from The View being mapped from
     * @returns a Point relative to this View's [position]
     */
    fun toLocal(point: Point, from: View?): Point = when {
        from ==  null        -> fromAbsolute(point)
        from === this        -> point
        from === this.parent -> (resolvedTransform.inverse?.invoke(point) ?: point) - position
        else                 -> fromAbsolute(from.toAbsolute(point))
    }

    /**
     * Maps a [Point] within the View to absolute coordinate-space.
     *
     * @param point to be mapped
     * @returns a Point relative to the un-transformed [Display]
     */
    fun toAbsolute(point: Point): Point = transform(point + position).let { parent?.toAbsolute(it) ?: display?.toAbsolute(it) ?: it }

    /**
     * Maps a [Point] from absolute coordinate-space: relative to the un-transformed [Display], into this View's coordinate-space.
     *
     * @param point to be mapped
     * @returns a Point relative to this View's [position]
     */
    fun fromAbsolute(point: Point): Point = (
            parent?.fromAbsolute (point) ?:
            display?.fromAbsolute(point) ?:
            point
    ).let { resolvedTransform.inverse?.invoke(it) ?: it } - position

    /**
     * Checked by the focus system before the focus is moved from a View.  Returning `false` will prevent focus from
     * moving.  The default is `true`.
     */
    open fun shouldYieldFocus() = true

    /**
     * Called by render system whenever [monitorsDisplayRect] == `true` and the View's display rect changes.
     *
     * @param old display rect
     * @param new display rect
     */
    internal fun handleDisplayRectEvent_(old: Rectangle, new: Rectangle) = handleDisplayRectEvent(old, new)

    /**
     * This is an event invoked on a View in response to a change in the display rectangle.
     *
     * @param old the old display rectangle
     * @param new the new display rectangle
     */
    @Suppress("UNUSED_PARAMETER")
    protected open fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {}

    internal fun handleKeyEvent_(event: KeyEvent) = handleKeyEvent(event)

    /**
     * This is an event invoked on a View in response to a key event triggered in the subsystem.
     *
     * @param event The event
     */
    protected open fun handleKeyEvent(event: KeyEvent) = keyChanged_.forEach {
        when(event.type) {
            Type.Up   -> it.keyReleased(event)
            Type.Down -> it.keyPressed (event)
        }
    }

    internal fun filterPointerEvent_(event: PointerEvent) = filterPointerEvent(event)

    /**
     * This is an event invoked on a View during the filter phase of a pointer event.
     *
     * @param event The event
     */
    protected open fun filterPointerEvent(event: PointerEvent) = pointerFilter_.forEach {
        when(event.type) {
            Up    -> it.released(event)
            Down  -> it.pressed (event)
            Exit  -> it.exited  (event)
            Enter -> it.entered (event)
            else  -> return
        }
    }

    internal fun handlePointerEvent_(event: PointerEvent) = handlePointerEvent(event)

    /**
     * This is an event invoked on a View in response to a pointer event triggered in the subsystem.
     *
     * @param event The event
     */
    protected open fun handlePointerEvent(event: PointerEvent) = pointerChanged_.forEach {
        when(event.type) {
            Up    -> it.released(event)
            Down  -> it.pressed (event)
            Exit  -> it.exited  (event)
            Enter -> it.entered (event)
            else  -> return
        }
    }

    internal fun filterPointerMotionEvent_(event: PointerEvent) = filterPointerMotionEvent(event)

    /**
     * This is an event invoked on a View during the filter phase of a pointer-motion event.
     *
     * @param event The event
     */
    protected open fun filterPointerMotionEvent(event: PointerEvent) = pointerMotionFilter_.forEach {
        when(event.type) {
            Move -> it.moved  (event)
            Drag -> it.dragged(event)
            else -> return
        }
    }

    internal fun handlePointerMotionEvent_(event: PointerEvent) = handlePointerMotionEvent(event)

    /**
     * This is an event invoked on a View in response to a pointer-motion event triggered in the subsystem.
     *
     * @param event The event
     */
    protected open fun handlePointerMotionEvent(event: PointerEvent) = pointerMotionChanged_.forEach {
        when(event.type) {
            Move -> it.moved  (event)
            Drag -> it.dragged(event)
            else -> return
        }
    }

    /**
     * This is invoked on a View in response to a focus event triggered in the subsystem.
     *
     * @param previous The previous View--if any--that had focus
     */
    internal fun focusGained(@Suppress("UNUSED_PARAMETER") previous: View?) {
        hasFocus = true
    }

    /**
     * This is invoked on a View in response to a focus event triggered in the subsystem.
     *
     * @param new The new View--if any--that will have focus
     */
    internal fun focusLost(@Suppress("UNUSED_PARAMETER") new: View?) {
        hasFocus = false
    }

    /**
     * This method is invoked by the render system when the View is first added to the [Display] hierarchy.  This happens when the View itself--
     * or one of it's ancestors--is added to the [Display].
     */
    protected open fun addedToDisplay() {}

    /**
     * This method is invoked by the render system when the View is first added to the [Display] hierarchy.  This happens when the View itself--
     * or one of it's ancestors--is added to the [Display].
     *
     * @param renderManager The RenderManager that will handle all renders for the view
     */
    internal fun addedToDisplay(display: Display, renderManager: RenderManager, accessibilityManager: AccessibilityManager?) {
        this.display              = display
        this.renderManager        = renderManager
        this.accessibilityManager = accessibilityManager

        accessibilityRole?.let {
            accessibilityManager?.roleAdopted(this)
        }

        addedToDisplay()

        (displayChange as PropertyObserversImpl<View, Boolean>).forEach { it(this, false, true) }
    }

    /**
     * This method is invoked by the Render system when the View is no longer included in the [Display] hierarchy.  This happens when the View itself--
     * or one of it's ancestors--is removed from the [Display].
     */
    protected open fun removedFromDisplay() {}

    /**
     * This method is invoked by the Render system when the View is no longer included in the [Display] hierarchy.  This happens when the View itself--
     * or one of it's ancestors--is removed from the [Display].
     */
    internal fun removedFromDisplay_() {
        accessibilityRole?.let {
            accessibilityManager?.roleAbandoned(this)
        }

        display              = null
        renderManager        = null
        accessibilityManager = null
        removedFromDisplay()

        (displayChange as PropertyObserversImpl<View, Boolean>).forEach { it(this, true, false) }
    }

    /**
     * Sets the bounding rectangle.
     *
     * @param x      The new x position
     * @param y      The new y position
     * @param width  The new width
     * @param height The new height
     */
    private fun setBounds(x: Double, y: Double, width: Double, height: Double) {
        bounds = Rectangle(x, y, width, height)
    }

    private val positionableWrapper by lazy { PositionableContainerWrapper(this) }
}

val View.center get() = position + Point(width/2, height/2)

fun View.mostRecentAncestor(filter: (View) -> Boolean): View? {
    var result = parent

    while (result != null && !filter(result)) {
        result = result.parent
    }

    return result
}
