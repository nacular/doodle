@file:Suppress("NestedLambdaShadowedImplicitParameter", "FunctionName", "PropertyName")

package com.nectar.doodle.core

import com.nectar.doodle.datatransport.dragdrop.DragOperation
import com.nectar.doodle.datatransport.dragdrop.DragRecognizer
import com.nectar.doodle.datatransport.dragdrop.DropReceiver
import com.nectar.doodle.drawing.AffineTransform
import com.nectar.doodle.drawing.AffineTransform.Companion.Identity
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.RenderManager
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.KeyState
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.event.MouseScrollEvent
import com.nectar.doodle.event.MouseScrollListener
import com.nectar.doodle.focus.FocusTraversalPolicy
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Rectangle.Companion.Empty
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Insets.Companion.None
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.SystemMouseEvent.Type.Down
import com.nectar.doodle.system.SystemMouseEvent.Type.Drag
import com.nectar.doodle.system.SystemMouseEvent.Type.Enter
import com.nectar.doodle.system.SystemMouseEvent.Type.Exit
import com.nectar.doodle.system.SystemMouseEvent.Type.Move
import com.nectar.doodle.system.SystemMouseEvent.Type.Up
import com.nectar.doodle.utils.ChangeObserver
import com.nectar.doodle.utils.ChangeObserversImpl
import com.nectar.doodle.utils.ObservableList
import com.nectar.doodle.utils.ObservableProperty
import com.nectar.doodle.utils.Pool
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import com.nectar.doodle.utils.SetPool
import com.nectar.doodle.utils.observable
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
 * @constructor
 */
abstract class View protected constructor() {
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
    var size: Size
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

    // TODO: Add layoutBounds to allow for cases where Layouts should have a smaller/larger region to work with than the paint region
    //  this would allow for cases like shadows not being included in size for laying out

    /** Notifies changes to [transform] */
    val transformChanged: PropertyObservers<View, AffineTransform> by lazy { PropertyObserversImpl<View, AffineTransform>(this) }

    /**
     * Affine transform applied to the View.  This transform does not affect the View's [bounds] or how it is handled by [layout].
     * It does affect the [boundingBox], and how the View looks when rendered.  Hit-detection is handled correctly such that the mouse
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

    /** Size that would best display this View, or ```null``` if no preference */
    var idealSize: Size? = null
        get() = layout?.idealSize(positionableWrapper, field) ?: field

    /** Minimum size preferred by the View, default is [Empty][Size.Empty] */
    var minimumSize = Size.Empty
        get() = layout?.idealSize(positionableWrapper, field) ?: field

    /**
     * Current visible [Rectangle] for this View within it's coordinate space.  This accounts for clipping by ancestors,
     * but **NOT** cousins (siblings, anywhere in the hierarchy)
     */
    val displayRect get() = renderManager?.displayRect(this) ?: Empty

    /** Notifies changes to [zOrder] */
    internal val zOrderChanged: ZOrderObservers by lazy { PropertyObserversImpl<View, Int>(this) }

    /** Rendering order of this View within it's [parent], or [Display] if top-level.  The default is ```0```. */
    var zOrder by ObservableProperty(0, { this }, zOrderChanged as PropertyObserversImpl<View, Int>)

    /** Notifies changes to [visible] */
    val visibilityChanged: BooleanObservers by lazy { PropertyObserversImpl<View, Boolean>(this) }

    /** Whether this View is visible.  The default is ```true```. */
    var visible by ObservableProperty(true, { this }, visibilityChanged as PropertyObserversImpl<View, Boolean>)

    /** Notifies changes to [enabled] */
    val enabledChanged: BooleanObservers by lazy { PropertyObserversImpl<View, Boolean>(this) }

    /** Whether this View is enabled  The default is ```true```.  */
    var enabled by ObservableProperty(true, { this }, enabledChanged as PropertyObserversImpl<View, Boolean>)

    /** Notifies changes to [focusable] */
    val focusabilityChanged: BooleanObservers by lazy { PropertyObserversImpl<View, Boolean>(this) }

    /** Whether this View is focusable  The default is ```true```.  */
    open var focusable by ObservableProperty(true, { this }, focusabilityChanged as PropertyObserversImpl<View, Boolean>)

    /** Notifies changes to [hasFocus] */
    val focusChanged: BooleanObservers by lazy { PropertyObserversImpl<View, Boolean>(this) }

    /** Whether the View has focus or not  The default is ```false```.  */
    var hasFocus by ObservableProperty(false, { this }, focusChanged as PropertyObserversImpl<View, Boolean>)
        private set

    /**
     * View that contains this one as a child, or ```null```.  A top-level Views will also return ```null```.  But they will also have
     * [displayed] ```== true```; so parent alone isn't sufficient to determine whether a View is in the display heirarchy.
     */
    var parent: View? = null
        // [Performance]
        // No check to prevent setting self as parent since View is the only place where this method is called from and it already
        // prevents this by preventing a View from being added to itself.
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

    /** Is ```true``` if the View is currently within the [Display] */
    val displayed get() = renderManager != null

    /** The current text to display for tool-tips.  The default is the empty string.  */
    var toolTipText = ""

    /** Cursor that is displayed whenever the mouse is over this View. */
    var cursor: Cursor? = null
        get() = field ?: parent?.cursor
        set(new) {
            if (new == field) {
                return
            }

            val old = field

            field = new

            (cursorChanged as PropertyObserversImpl<View, Cursor?>)(old, new)
        }

    /** Notifies changes to [cursor] */
    val cursorChanged: PropertyObservers<View, Cursor?> by lazy { PropertyObserversImpl<View, Cursor?>(this) }

    /** Optional font that the View could use for rendering */
    var font: Font? = null; set(new) { field = new; styleChanged() }

    /** Optional color that the View could use for its foreground (i.e. text) */
    var foregroundColor: Color? = null; set(new) { field = new; styleChanged() }

    /** Optional color that the View could use for its background */
    var backgroundColor: Color? = null; set(new) { field = new; styleChanged() }

    /** Notifies changes to [font], [foregroundColor], or [backgroundColor] */
    val styleChanged: Pool<ChangeObserver<View>> by lazy { ChangeObserversImpl(this) }

    /** Determines whether the View will be affected by [Theme][com.nectar.doodle.theme.Theme]s set in [ThemeManager][com.nectar.doodle.theme.ThemeManager].  Defaults to ```true``` */
    var acceptsThemes = true

    val mouseFilter  by lazy { SetPool<MouseListener>() }
    val mouseChanged by lazy { SetPool<MouseListener>() }

    val keyChanged by lazy { SetPool<KeyListener>() }

    val mouseMotionFilter  by lazy { SetPool<MouseMotionListener>() }
    val mouseMotionChanged by lazy { SetPool<MouseMotionListener>() }

    val mouseScrollChanged by lazy { SetPool<MouseScrollListener>() }

    /** Recognizer used to determine whether a [MouseEvent] should result in a [DragOperation] */
    var dragRecognizer = null as DragRecognizer?

    /** Receiver that determines what drop operations are supported by the View */
    var dropReceiver = null as DropReceiver?

    val displayRectHandlingChanged: BooleanObservers by lazy { PropertyObserversImpl<View, Boolean>(this) }

    var monitorsDisplayRect by ObservableProperty(false, { this }, displayRectHandlingChanged as PropertyObserversImpl<View, Boolean>)

    @JsName("fireStyleChanged")
    protected fun styleChanged() = (styleChanged as ChangeObserversImpl)()

    fun mostRecentAncestor(filter: (View) -> Boolean): View? {
        var result = parent

        while (result != null && !filter(result)) {
            result = result.parent
        }

        return result
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
                    require(it !== this@View) { "cannot add to self"                }
                    require(!it.ancestorOf(this@View)) { "cannot add ancestor to descendant" }

                    it.parent = this@View
                }

                (childrenChanged_ as ChildObserversImpl).invoke(removed, added, moved)
            }
        }
    }

    protected val childrenChanged: Pool<ChildObserver> by lazy { ChildObserversImpl() }

    internal infix fun ancestorOf_(view: View) = this ancestorOf view

    /**
     * Tells whether this View is an ancestor of the given View.
     *
     * @param view The View
     * @return ```true``` if the given View is a descendant of this one
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

    private var renderManager: RenderManager? = null

    private val traversalKeys: MutableMap<TraversalType, Set<KeyState>> by lazy { mutableMapOf<TraversalType, Set<KeyState>>() }

    fun shouldYieldFocus() = true

    internal fun revalidate_() = revalidate()

    protected fun revalidate() {
        relayout()
        rerender()
    }

    /**
     * Tells whether this View is child's parent.  Unlike [ancestorOf], this checks only a parent-child relationship.
     *
     * @param child The View being tested
     * @return ```true``` IFF the View is a child of the View
     */
    protected operator fun contains(child: View) = child.parent == this

    /**
     * Prompts the View to layout its children if it has a Layout installed.
     */
    protected open fun relayout() = renderManager?.layout(this)

    internal fun doLayout_() = doLayout()
    protected open fun doLayout() = layout?.layout(positionableWrapper)

    /**
     * Gets the View at the given point.
     *
     * @param at The point being tested
     * @return The child (```null``` if no child contains the given point)
     */
    protected open fun child(at: Point): View? = (layout?.child(positionableWrapper, at) as? PositionableWrapper?)?.view ?: {
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

    internal fun child_(at: Point) = child(at)

    /**
     * Gives the View an opportunity to render itself to the given Canvas.
     *
     * @param canvas The canvas upon which drawing will be done
     */
    open fun render(canvas: Canvas) {}

    /**
     * Request the rendering subsystem to trigger a [render] call if needed.
     */
    fun rerender() = renderManager?.render(this)

    /**
     * Request the rendering subsystem to trigger a [render] call with no delay. Only use this method for time-sensitive drawing as is the case for animations.
     */
    fun rerenderNow() = renderManager?.renderNow(this) // TODO: Remove?

    /**
     * Gets the tool-tip text based on the given [MouseEvent].  Override this method to provide multiple tool-tip text values for a single View.
     *
     * @param for The mouse event to generate a tool-tip for
     * @return The text
     */
    open fun toolTipText(@Suppress("UNUSED_PARAMETER") `for`: MouseEvent): String = toolTipText

    /**
     * Checks whether a point is within the boundaries of a View.
     *
     * @param point The point to check
     * @return ```true``` IFF the point falls within the View
     */
    open operator fun contains(point: Point) = transform.inverse?.invoke(point)?.let { bounds.contains(it) } ?: false

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

    fun toLocal(point: Point, from: View?): Point = when {
        from ==  null        -> fromAbsolute(point)
        from === this        -> point
        from === this.parent -> (transform.inverse?.invoke(point) ?: point) - position
        else                 -> from.toAbsolute(point) - this.toAbsolute(Origin)
    }

    fun toAbsolute  (point: Point): Point = (parent?.toAbsolute  (point) ?: point).let { transform.inverse?.invoke(it) ?: it } + position
    fun fromAbsolute(point: Point): Point = (parent?.fromAbsolute(point) ?: point).let { transform.inverse?.invoke(it) ?: it } - position

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
    protected open fun handleKeyEvent(event: KeyEvent) = keyChanged.forEach {
        when(event.type) {
            KeyState.Type.Up    -> it.keyReleased(event)
            KeyState.Type.Down  -> it.keyPressed (event)
            KeyState.Type.Press -> it.keyTyped   (event)
        }
    }

    internal fun filterMouseEvent_(event: MouseEvent) = filterMouseEvent(event)

    /**
     * This is an event invoked on a View during the filter phase of a mouse event.
     *
     * @param event The event
     */
    protected open fun filterMouseEvent(event: MouseEvent) = mouseFilter.forEach {
        when(event.type) {
            Up    -> it.mouseReleased(event)
            Down  -> it.mousePressed (event)
            Exit  -> it.mouseExited  (event)
            Enter -> it.mouseEntered (event)
            else  -> return
        }
    }

    internal fun handleMouseEvent_(event: MouseEvent) = handleMouseEvent(event)

    /**
     * This is an event invoked on a View in response to a mouse event triggered in the subsystem.
     *
     * @param event The event
     */
    protected open fun handleMouseEvent(event: MouseEvent) = mouseChanged.forEach {
        when(event.type) {
            Up    -> it.mouseReleased(event)
            Down  -> it.mousePressed (event)
            Exit  -> it.mouseExited  (event)
            Enter -> it.mouseEntered (event)
            else  -> return
        }
    }

    internal fun filterMouseMotionEvent_(event: MouseEvent) = filterMouseMotionEvent(event)

    /**
     * This is an event invoked on a View during the filter phase of a mouse-motion event.
     *
     * @param event The event
     */
    protected open fun filterMouseMotionEvent(event: MouseEvent) = mouseMotionFilter.forEach {
        when(event.type) {
            Move -> it.mouseMoved  (event)
            Drag -> it.mouseDragged(event)
            else -> return
        }
    }

    internal fun handleMouseMotionEvent_(event: MouseEvent) = handleMouseMotionEvent(event)

    /**
     * This is an event invoked on a View in response to a mouse-motion event triggered in the subsystem.
     *
     * @param event The event
     */
    protected open fun handleMouseMotionEvent(event: MouseEvent) = mouseMotionChanged.forEach {
        when(event.type) {
            Move -> it.mouseMoved  (event)
            Drag -> it.mouseDragged(event)
            else -> return
        }
    }

    internal fun handleMouseScrollEvent_(event: MouseScrollEvent) = handleMouseScrollEvent(event)

    /**
     * This is an event invoked on a View in response to a mouse scroll event triggered in the subsystem.
     *
     * @param event The event
     */
    protected open fun handleMouseScrollEvent(event: MouseScrollEvent) {}

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
    internal fun addedToDisplay(renderManager: RenderManager) {
        this.renderManager = renderManager
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
        renderManager = null
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

//    operator fun plus (listener: MouseWheelListener ): View = this.also { listeners.add   (listener, MouseWheelListener::class.java ) }
//    operator fun minus(listener: MouseWheelListener ): View = this.also { listeners.remove(listener, MouseWheelListener::class.java ) }
}

val View.center get() = Point(width/2, height/2)