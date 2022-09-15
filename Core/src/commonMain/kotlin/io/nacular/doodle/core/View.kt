@file:Suppress("NestedLambdaShadowedImplicitParameter", "FunctionName", "PropertyName", "PrivatePropertyName")

package io.nacular.doodle.core

import io.nacular.doodle.accessibility.AccessibilityManager
import io.nacular.doodle.accessibility.AccessibilityRole
import io.nacular.doodle.controls.panels.ScrollPanel
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
import io.nacular.doodle.drawing.invoke
import io.nacular.doodle.drawing.is3d
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.KeyState
import io.nacular.doodle.event.KeyState.Type
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.focus.FocusTraversalPolicy
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.ConvexPolygon
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Plane
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Ray
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Vector3D
import io.nacular.doodle.geometry.toPath
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.Insets.Companion.None
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.SystemPointerEvent.Type.Click
import io.nacular.doodle.system.SystemPointerEvent.Type.Down
import io.nacular.doodle.system.SystemPointerEvent.Type.Drag
import io.nacular.doodle.system.SystemPointerEvent.Type.Enter
import io.nacular.doodle.system.SystemPointerEvent.Type.Exit
import io.nacular.doodle.system.SystemPointerEvent.Type.Move
import io.nacular.doodle.system.SystemPointerEvent.Type.Up
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.observable
import kotlin.js.JsName
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private typealias BooleanObservers = PropertyObservers<View, Boolean>
private typealias ZOrderObservers = PropertyObservers<View, Int>

/**
 * The smallest unit of displayable, interactive content within doodle.  Views are the visual entities used to display components for an application.
 * User input events are sent to all Views that are configured to receive them. This allows them to response to user interaction or convey such events to
 * other parts of an application.
 *
 * @author Nicholas Eddy
 *
 * @constructor
 */
public abstract class View protected constructor(accessibilityRole: AccessibilityRole? = null): Renderable, Positionable {
    /**
     * Defines a clipping path for a View's children.
     *
     * @constructor
     * @property path used for clipping
     */
    public abstract class ClipPath(public val path: Path) {
        /**
         * Indicates whether [point] falls within [path]
         *
         * @param point being checked
         */
        public abstract operator fun contains(point: Point): Boolean
    }

    /**
     * [ClipPath] based on a [Polygon]. The contains check defaults to [Polygon.contains].
     *
     * @constructor
     * @param polygon used for clipping
     */
    public class PolyClipPath(private val polygon: Polygon): ClipPath(polygon.toPath()) {
        override fun contains(point: Point): Boolean = point in polygon
    }

    /**
     * [ClipPath] based on a [Ellipse]. The contains check defaults to [Ellipse.contains].
     *
     * @constructor
     * @param ellipse used for clipping
     */
    public class EllipseClipPath(private val ellipse: Ellipse): ClipPath(ellipse.toPath()) {
        public constructor(center: Point,  radius: Double                 ): this(Circle(center, radius))
        public constructor(center: Point, xRadius: Double, yRadius: Double): this(Ellipse(center, xRadius, yRadius))

        override fun contains(point: Point): Boolean = point in ellipse
    }

    private inner class ChildObserversImpl(mutableSet: MutableSet<ChildObserver<View>> = mutableSetOf()): SetPool<ChildObserver<View>>(mutableSet) {
        operator fun invoke(removed: Map<Int, View>, added: Map<Int, View>, moved: Map<Int, Pair<Int, View>>) = delegate.forEach { it(this@View, removed, added, moved) }
    }

    // region Accessibility

    /** indicates the View's role for assistive technologies */
    public var accessibilityRole: AccessibilityRole? = accessibilityRole
        internal set(new) {
            if (field != new) {
                accessibilityManager?.roleAbandoned(this)
                field = new
                accessibilityManager?.roleAdopted(this)
            }
        }

    /** Provides a recognizable name for assistive technologies. */
    public var accessibilityLabel: String? by observable(null) { _,_ ->
        accessibilityManager?.syncLabel(this)
    }

    /**
     * Indicates the [View] that contains a recognizable name for assistive technologies.
     * This property overrides [accessibilityLabel].
     */
    public var accessibilityLabelProvider: View? by observable(null) { _,_ ->
        accessibilityManager?.syncLabel(this)
    }

    /**
     * Indicates the [View] that contains a more detailed description for assistive technologies.
     * This property overrides [accessibilityLabel].
     */
    public var accessibilityDescriptionProvider: View? by observable(null) { _,_ ->
        accessibilityManager?.syncDescription(this)
    }

    /**
     * Overrides the next item read by assistive technologies when they move from this View.
     * This is helpful when the display order (i.e. ordering of children in a parent) does not
     * match the order they should be read in.
     */
    public var nextInAccessibleReadOrder: View? by observable(null) { _,_ ->
        accessibilityManager?.syncNextReadOrder(this)
    }

    // endregion
    // region Bounds

    /** Left edge of [bounds] */
    final override var x: Double
        get( ) = bounds.x
        set(x) { if (x != bounds.x) setBounds(x, y, width, height) }

    /** Top edge of [bounds] */
    final override var y: Double
        get( ) = bounds.y
        set(y) { if (y != bounds.y) setBounds(x, y, width, height) }

    /** Top-left corner of [bounds] */
    final override var position: Point
        get(        ) = bounds.position
        set(position) = setBounds(position.x, position.y, width, height)

    /** Horizontal extent of [bounds] */
    final override var width: Double
        get(     ) = bounds.width
        set(width) { if (width != bounds.width) setBounds(x, y, width, height) }

    /** Vertical extent of [bounds] */
    final override var height: Double
        get(      ) = bounds.height
        set(height) { if (height != bounds.height) setBounds(x, y, width, height) }

    /** Width-height of [bounds]*/
    final override var size: Size
        get(    ) = bounds.size
        set(size) = setBounds(x, y, size.width, size.height)

    /** Notifies changes to [bounds]: [x], [y], [width], [height] */
    public val boundsChanged: PropertyObservers<View, Rectangle> by lazy { PropertyObserversImpl(this) }

    /**
     * The top, left, width, and height with respect to [parent], or the [Display] if top-level.  Unlike [boundingBox], this value isn't affected
     * by any applied [transform].
     */
    final override var bounds: Rectangle by observable(Empty, boundsChanged as PropertyObserversImpl) { old, new ->
        boundingBox = getBoundingBox(new)
        renderManager?.boundsChanged(this, old, new)
    }

    internal var clipCanvasToBounds_ get() = clipCanvasToBounds; set(new) { clipCanvasToBounds = new }
    /**
     * Indicates whether the View's [Canvas] will be clipped so that nothing rendered shows beyond its [bounds].  Set this to `false` to support
     * things like shadows or glows that aren't intended to be included in the normal bounding box.
     *
     * This property does not affect the clipping of child Views and their descendants; these are always clipped to the parent bounds.
     *
     * The default is `true`
     */
    protected var clipCanvasToBounds: Boolean by renderProperty(true)

    /**
     * A [Path] used to further clip the View's children within its [bounds]. The View's children cannot extend
     * beyond its [bounds], so specifying a value larger than it will not enable that.
     *
     * The default is `null`.
     */
    protected var childrenClipPath: ClipPath? by renderProperty(null)

    internal var childrenClipPath_ get() = childrenClipPath; set(new) { childrenClipPath = new }

    /** Notifies changes to [transform] */
    public val transformChanged: PropertyObservers<View, AffineTransform> by lazy { PropertyObserversImpl(this) }

    /**
     * Affine transform applied to the View.  This transform does not affect the View's [bounds] or how it is handled by [Layout].
     * It does affect the [boundingBox], and how the View looks when rendered.  Hit-detection is handled correctly such that the pointer
     * intersects with the View as expected after transformation.  So no additional handling is necessary in general.
     * The default is [Identity]
     */
    public open var transform: AffineTransform by observable(Identity, transformChanged as PropertyObserversImpl) { old, new ->
        boundingBox = getBoundingBox(bounds)
        renderManager?.transformChanged(this, old, new)
    }

    public val cameraChanged: PropertyObservers<View, Camera> by lazy { PropertyObserversImpl(this) }

    /**
     * Camera within the View's parent that affects how it is projected onto the screen.
     */
    public var camera: Camera by observable(Camera.Identity, cameraChanged as PropertyObserversImpl) { old, new -> renderManager?.cameraChanged(this, old, new) }

    /** Smallest enclosing [Rectangle] around the View's [bounds] given it's [transform]. */
    public var boundingBox: Rectangle = bounds; private set

    /** Size that would best display this View, or `null` if no preference */
    final override var idealSize: Size? = null
        get(   ) = layout?.idealSize(positionableWrapper, field) ?: field
        set(new) {
            if (field == new) return
            val old = field
            field = new

            notifySizePreferencesChanged(SizePreferences(old, minimumSize), SizePreferences(new, minimumSize))
        }

    /** Minimum size preferred by the View, default is [Empty][Size.Empty] */
    final override var minimumSize: Size = Size.Empty
        get(   ) = layout?.minimumSize(positionableWrapper, field) ?: field
        set(new) {
            if (field == new) return
            val old = field
            field = new

            notifySizePreferencesChanged(SizePreferences(idealSize, old), SizePreferences(idealSize, new))
        }

    /** Indicates the minimum and ideal sizes for a View. */
    public class SizePreferences(public val idealSize: Size?, public val minimumSize: Size)

    /** Notifies changes to [idealSize] or [minimumSize] */
    public val sizePreferencesChanged: PropertyObservers<View, SizePreferences> by lazy { PropertyObserversImpl(this) }

    /**
     * Current visible [Rectangle] for this View within it's coordinate space.  This accounts for clipping by ancestors,
     * but **NOT** cousins (siblings, anywhere in the hierarchy)
     */
    public val displayRect: Rectangle get() = renderManager?.displayRect(this) ?: Empty

    private val plane: Plane get() {
        val rect = resolvedTransform.invoke(bounds.points.map { it.as3d() })
        return Plane(rect[0], (rect[1] - rect[0]) cross (rect[2] - rect[1]))
    }

    // endregion

    /** Notifies changes to [zOrder] */
    internal val zOrderChanged: ZOrderObservers by lazy { PropertyObserversImpl(this) }

    /**
     * Rendering order of this View within it's [parent], or [Display] if top-level.
     * Views with higher values are rendered above those with lower ones. The default is `0`.
     */
    public var zOrder: Int by observable(0, zOrderChanged as PropertyObserversImpl<View, Int>) { old, new ->
        renderManager?.zOrderChanged(this, old, new)
    }

    /** Notifies changes to [visible] */
    public val visibilityChanged: BooleanObservers by lazy { PropertyObserversImpl(this) }

    /** Whether this View is visible. The default is `true`. */
    final override var visible: Boolean by observable(true, visibilityChanged as PropertyObserversImpl<View, Boolean>) { old, new ->
        renderManager?.visibilityChanged(this, old, new)
        accessibilityManager?.syncVisibility(this)
    }

    public val opacityChanged: PropertyObservers<View, Float> by lazy { PropertyObserversImpl(this) }

    /**
     * Indicates how opaque the View is. A value of 1 means the View should be fully opaque and
     * 0 means fully transparent. Setting this property to 0 does not make a View invisible (i.e.
     * [visible] might still be `true`). This property also does not affect how events (i.e.
     * pointer and keyboard) are sent to the View.
     */
    public var opacity: Float by observable(1f, opacityChanged as PropertyObserversImpl<View, Float>) { old, new ->
        renderManager?.opacityChanged(this, old, new)
    }

    /** Notifies changes to [enabled] */
    public val enabledChanged: BooleanObservers by lazy { PropertyObserversImpl(this) }

    private var actualEnabled: Boolean = true
    /** Whether this View is enabled.  The default is `true`.  */
    public var enabled: Boolean
        get(   ) = actualEnabled && (parent?.enabled != false)
        set(new) {
            if (actualEnabled == new) return

            val old = actualEnabled

            actualEnabled = new

            if (parent?.enabled != false) {
                enabledChanged(old, new) { it.actualEnabled }
            }
        }

    /** Notifies changes to [focusable] */
    public val focusabilityChanged: BooleanObservers by lazy { PropertyObserversImpl(this) }

    /** Whether this View is focusable  The default is `true`.  */
    public open var focusable: Boolean by observable(true, focusabilityChanged as PropertyObserversImpl<View, Boolean>)

    /** Notifies changes to [hasFocus] */
    public val focusChanged: BooleanObservers by lazy { PropertyObserversImpl(this) }

    /** Whether the View has focus or not.  The default is `false`.  */
    public var hasFocus: Boolean by observable(false, focusChanged as PropertyObserversImpl<View, Boolean>)
        private set

    /**
     * View that contains this one as a child, or `null`.  A top-level Views will also return `null`; but they will also have
     * [displayed] `== true`; so parent alone isn't sufficient to determine whether a View is in the display hierarchy.
     */
    public var parent: View? = null
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
    public val parentChange: PropertyObservers<View, View?> by lazy { PropertyObserversImpl(this) }

    /** Notifies changes to [displayed] */
    public val displayChange: BooleanObservers by lazy { PropertyObserversImpl(this) }

    /**
     * Is `true` if the View is currently within the [Display].
     * NOTE: this does not mean the View has been rendered yet.
     * @see [rendered]
     */
    public val displayed: Boolean get() = renderManager != null

    /** Notifies on the View's first render */
    public val firstRender: ChangeObservers<View> by lazy { ChangeObserversImpl(this) }

    /**
     * Is `true` if the View has been rendered at least once.
     */
    public var rendered: Boolean = false
        internal set(new) {
            if (field != new) {
                field = new
                if (field) {
                    (firstRender as ChangeObserversImpl).forEach { it(this) }
                }
            }
        }

    /** The current text to display for tool-tips.  The default is the empty string.  */
    public var toolTipText: String = ""

    private var actualCursor: Cursor? = null

    /** Cursor that is displayed whenever the pointer is over this View. This falls back to the [parent]'s Cursor if not set. */
    public var cursor: Cursor?
        get(   ) = actualCursor ?: parent?.cursor
        set(new) {
            if (actualCursor == new) return

            val old = cursor

            actualCursor = new

            cursorChanged(old, new)
        }

    /** Notifies changes to [cursor] */
    public val cursorChanged: PropertyObservers<View, Cursor?> by lazy { PropertyObserversImpl(this) }

    private var actualFont: Font? = null

    /** Optional font that the View could use for rendering.  This falls back to [parent]'s font if not set. */
    public var font: Font?
        get(   ) = actualFont ?: parent?.font
        set(new) {
            if (actualFont == new) return

            actualFont = new

            styleChanged { it.actualFont == null }
        }

    /** Optional color that the View could use for its foreground (i.e. text) */
    public var foregroundColor: Color? by styleProperty(null) { it.foregroundColor == null }

    /** Optional color that the View could use for its background */
    public var backgroundColor: Color? by styleProperty(null) { it.backgroundColor == null }

    /** Notifies changes to [font], [foregroundColor], or [backgroundColor] */
    public val styleChanged: ChangeObservers<View> by lazy { ChangeObserversImpl(this) }

    /** Notifies changes to [localContentDirection] */
    public val contentDirectionChanged: ChangeObservers<View> by lazy { ChangeObserversImpl(this) }

    /**
     * Determines whether the View will be affected by [Theme][io.nacular.doodle.theme.Theme]s set in [ThemeManager][io.nacular.doodle.theme.ThemeManager].
     * Defaults to `true`
     */
    public var acceptsThemes: Boolean = true

    private val pointerFilter_ by lazy { SetPool<PointerListener>() }

    /** [PointerListener]s that are notified during the sinking phase of pointer event handling. */
    public val pointerFilter: Pool<PointerListener> get() = pointerFilter_

    private val pointerChanged_ by lazy { SetPool<PointerListener>() }

    /** [PointerListener]s that are notified during the bubbling phase of pointer event handling. */
    public val pointerChanged: Pool<PointerListener> get() = pointerChanged_

    private val keyChanged_ by lazy { SetPool<KeyListener>() }

    /** [KeyListener]s that are notified during of [KeyEvent]s sent to the View. */
    public val keyChanged: Pool<KeyListener> get() = keyChanged_

    private val pointerMotionFilter_ by lazy { SetPool<PointerMotionListener>() }

    /** [PointerMotionListener]s that are notified during the sinking phase of pointer-motion event handling. */
    public val pointerMotionFilter: Pool<PointerMotionListener> get() = pointerMotionFilter_

    private val pointerMotionChanged_ by lazy { SetPool<PointerMotionListener>() }

    /** [PointerMotionListener]s that are notified during the sinking phase of pointer-motion event handling. */
    public val pointerMotionChanged: Pool<PointerMotionListener> get() = pointerMotionChanged_

    /** Recognizer used to determine whether a [PointerEvent] should result in a [DragOperation] */
    public var dragRecognizer: DragRecognizer? = null

    /** Receiver that determines what drop operations are supported by the View */
    public var dropReceiver: DropReceiver? = null

    /** Notifies changes to [monitorsDisplayRect] */
    public val displayRectHandlingChanged: BooleanObservers by lazy { PropertyObserversImpl(this) }

    /**
     * Indicates whether the framework should notify the View of changes to its visible region as a result of
     * changes to bounds in its ancestor chain.  The events for this require monitoring potentially large sets
     * of Views in the hierarchy and the events can be frequent during layout changes.  So the default value is
     * `false`.  But it is useful for things like efficient rendering of sub-portions (i.e. long list-like Views)
     * where the cost is outweighed.
     *
     * NOTE: the framework does not notify of clipping due to siblings that overlap with a View (or ancestors).
     * That means a View can be notified of a display rect change and still not be visible to the user.
     */
    public var monitorsDisplayRect: Boolean by observable(false, displayRectHandlingChanged as PropertyObserversImpl<View, Boolean>) { old, new ->
        renderManager?.displayRectHandlingChanged(this, old, new)
    }

    /**
     * Indicates the direction of content within the View; used to support right-to-left locales.
     * Setting this property to `null` will allow the View to inherit the value from its [parent].
     * The resolved value is obtained via [contentDirection]; and it will never be `null`.
     */
    public var localContentDirection: ContentDirection? = null
        set(new) {
            if (field == new) return

            field = new

            contentDirectionChanged()
        }

    /**
     * The resolved content direction for this View, including any value inherited from its [parent] if
     * [localContentDirection] is `null`. The final fallback value is [LeftRight].
     */
    public val contentDirection: ContentDirection
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
    public var mirrorWhenRightLeft: Boolean = true
        protected set(new) {
            if (field == new) return
            field = new

            updateNeedsMirror()
        }

    internal var mirrorWhenRightLeft_ get() = mirrorWhenRightLeft; set(new) { mirrorWhenRightLeft = new }

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

    @JsName("fireEnabledChanged")
    protected fun enabledChanged(old: Boolean, new: Boolean, filter: (View) -> Boolean) {
        accessibilityManager?.syncEnabled(this)

        (enabledChanged as PropertyObserversImpl<View, Boolean>)(old, new)

        // Notify relevant children that their enabled state has changed
        children.filter(filter).forEach { it.enabledChanged(old, new, filter) }
    }

    /**
     * `true` if the View's [contentDirection] is [RightLeft] and [mirrorWhenRightLeft] is `true`.
     */
    public val mirrored: Boolean get() = contentDirection == RightLeft && mirrorWhenRightLeft

    /**
     * Refresh [needsMirrorTransform] flag to allow rerender if needed.
     */
    internal fun updateNeedsMirror() {
        needsMirrorTransform = mirrored != ((parent?.mirrored ?: display?.mirrored) == true)
    }

    // ================= Container ================= //
    internal val insets_ get() = insets

    /** Insets used to control how [Layout]s set the View's children away from its edge. */
    protected open var insets: Insets = None

    internal val layout_ get() = layout

    /** Layout responsible for positioning of this View's children */
    protected open var layout: Layout? by observable(null) { _,_ ->
        relayout()
    }

    internal val childrenChanged_ get() = childrenChanged

    internal val children_ get() = children

    /** List of child Views within this one */
    protected open val children: ObservableList<View> by lazy {
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

                renderManager?.childrenChanged(this@View, removed, added, moved)
                (childrenChanged_ as ChildObserversImpl).invoke(removed, added, moved)
            }
        }
    }

    /** Notifies changes to [children] */
    protected val childrenChanged: Pool<ChildObserver<View>> by lazy { ChildObserversImpl() }

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

    protected open var isFocusCycleRoot: Boolean = false

    internal val focusCycleRoot_ get() = focusCycleRoot
    protected val focusCycleRoot: View? get() {
        var result = parent

        while (result != null && !result.isFocusCycleRoot) {
            result = result.parent
        }

        return result
    }

    internal val focusTraversalPolicy_ get() = focusTraversalPolicy
    protected open var focusTraversalPolicy: FocusTraversalPolicy? = null

    internal var display             : Display?              = null; private set
    private  var renderManager       : RenderManager?        = null
    private  var accessibilityManager: AccessibilityManager? = null

    private val traversalKeys: MutableMap<TraversalType, Set<KeyState>> by lazy { mutableMapOf() }

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
    protected operator fun contains(child: View): Boolean = child.parent == this

    /** Prompts the View to layout its children if it has a Layout installed. */
    protected open fun relayout() { renderManager?.layout(this) }

    internal fun doLayout_() = doLayout()

    /** Causes the [layout] (if any) to re-layout the View's [children] */
    protected open fun doLayout() { layout?.layout(positionableWrapper) }

    /**
     * Gets the child (if any) at the given point in the View's coordinate system (relative to the View).
     *
     * @param at The point being tested
     * @return The child (`null` if no child contains the given point)
     */
    protected open fun child(at: Point): View? = when {
        false == childrenClipPath?.contains(at) -> null
        else                                    -> when (val result = layout?.child(positionableWrapper, at)) {
            null, Ignored -> {
                var child     = null as View?
                var topZOrder = 0

                children.asReversed().forEach {
                    if (it.visible && at in it && (child == null || it.zOrder > topZOrder)) {
                        child = it
                        topZOrder = it.zOrder
                    }
                }

                child
            }
            is Found      -> result.child as? View
            is Empty      -> null
        }
    }

    internal fun child_(at: Point) = child(at)

    /**
     * Gives the View an opportunity to render itself (excludes any children) to the given Canvas.
     *
     * @param canvas The canvas upon which drawing will be done
     */
    override fun render(canvas: Canvas) {}

    /**
     * Request the rendering subsystem to trigger a [render] call if needed.
     */
    public fun rerender() { renderManager?.render(this) }

    /**
     * Request the rendering subsystem to trigger a [render] call with no delay. Only use this method for time-sensitive drawing as is the case for animations.
     */
    public fun rerenderNow() { renderManager?.renderNow(this) } // TODO: Remove?

    /**
     * Gets the tool-tip text based on the given [PointerEvent].  Override this method to provide multiple tool-tip text values for a single View.
     *
     * @param for The pointer event to generate a tool-tip for
     * @return The text
     */
    public open fun toolTipText(`for`: PointerEvent): String = toolTipText

    // TODO: Cache this?
    private val resolvedTransform get() = when {
        needsMirrorTransform -> transform.flipHorizontally(at = center.x)
        else                 -> transform
    }.let {
        when {
            it.is3d -> ((camera.projection * it).matrix)
            else    -> it.matrix
        }
    }

    /**
     * Checks whether a point (relative to [parent] or [Display] if top-level) is within the View's bounds. This method must account for
     * any [transform] and [camera] applied to the View. The default implementation does this and delegates to [intersects].
     *
     * @param point within the View's parent
     * @return `true` IFF the point falls within the View
     */
    override operator fun contains(point: Point): Boolean = resolvedTransform.inverse?.invoke(toPlane(point))?.let { intersects(it.as2d()) } ?: false

    /**
     * Checks whether a point on the View's plane, but relative to its parent, intersects (is "touching") the View. This enables custom
     * hit detection logic for Views that have non-rectangular shapes.
     *
     * The given [point] is already modified to account for the View's [transform] and [camera], so implementations can simply work
     * with the point directly. The default implementation checks:
     *
     * ```kotlin
     * point in bounds
     * ```
     *
     * @param point on the View's plane
     * @return `true` IFF the point falls within the View
     */
    public open infix fun intersects(point: Point): Boolean = point in bounds

    /**
     * Gets the set of keys used to trigger this type of focus traversal.
     *
     * @return The set of keys that will trigger this type of traversal
     */
    public operator fun get(traversalType: TraversalType): Set<KeyState>? {
        return traversalKeys[traversalType]
    }

    /**
     * Sets the keys used to control focus traversals of the given type.
     *
     * @param traversalType The traversal type
     * @param keyStates     The set of keys that will trigger this type of traversal
     */
    public operator fun set(traversalType: TraversalType, keyStates: Set<KeyState>?) {
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
    public fun toLocal(point: Point, from: View?): Point = when {
        from ==  null        -> fromAbsolute(point)
        from === this        -> point
        from === this.parent -> (resolvedTransform.inverse?.invoke(toPlane(point))?.as2d() ?: point) - position
        else                 -> fromAbsolute(from.toAbsolute(point))
    }

    /**
     * Maps a [Point] within the View to absolute coordinate-space.
     *
     * @param point to be mapped
     * @returns a Point relative to the un-transformed [Display]
     */
    public fun toAbsolute(point: Point): Point = resolvedTransform(toPlane(point + position)).let { parent?.toAbsolute(it.as2d()) ?: display?.toAbsolute(it.as2d()) ?: it.as2d() }

    /**
     * Maps a [Point] from absolute coordinate-space: relative to the un-transformed [Display], into this View's coordinate-space.
     *
     * @param point to be mapped
     * @returns a Point relative to this View's [position]
     */
    public fun fromAbsolute(point: Point): Point = (
            parent?.fromAbsolute (point) ?:
            display?.fromAbsolute(point) ?:
            point
    ).let { resolvedTransform.inverse?.invoke(toPlane(it))?.as2d() ?: it } - position

    /**
     * Checked by the focus system before the focus is moved from a View.  Returning `false` will prevent focus from
     * moving.  The default is `true`.
     */
    public open fun shouldYieldFocus(): Boolean = true

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
    protected open fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {}

    internal fun handleKeyEvent_(event: KeyEvent) = handleKeyEvent(event)

    /**
     * This is an event invoked on a View in response to a key event triggered in the subsystem.
     *
     * @param event The event
     */
    protected open fun handleKeyEvent(event: KeyEvent): Unit = keyChanged_.forEach {
        when(event.type) {
            Type.Up   -> it.released(event)
            Type.Down -> it.pressed (event)
        }
    }

    internal fun filterPointerEvent_(event: PointerEvent) = filterPointerEvent(event)

    /**
     * This is an event invoked on a View during the filter phase of a pointer event.
     *
     * @param event The event
     */
    protected open fun filterPointerEvent(event: PointerEvent): Unit = pointerFilter_.forEach {
        when(event.type) {
            Up    -> it.released(event)
            Down  -> it.pressed (event)
            Exit  -> it.exited  (event)
            Enter -> it.entered (event)
            Click -> it.clicked (event)
            else  -> return
        }
    }

    internal fun handlePointerEvent_(event: PointerEvent) = handlePointerEvent(event)

    /**
     * This is an event invoked on a View in response to a pointer event triggered in the subsystem.
     *
     * @param event The event
     */
    protected open fun handlePointerEvent(event: PointerEvent): Unit = pointerChanged_.forEach {
        when(event.type) {
            Up    -> it.released(event)
            Down  -> it.pressed (event)
            Exit  -> it.exited  (event)
            Enter -> it.entered (event)
            Click -> it.clicked (event)
            else  -> return
        }
    }

    internal fun filterPointerMotionEvent_(event: PointerEvent) = filterPointerMotionEvent(event)

    /**
     * This is an event invoked on a View during the filter phase of a pointer-motion event.
     *
     * @param event The event
     */
    protected open fun filterPointerMotionEvent(event: PointerEvent): Unit = pointerMotionFilter_.forEach {
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
    protected open fun handlePointerMotionEvent(event: PointerEvent): Unit = pointerMotionChanged_.forEach {
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
        this.accessibilityManager = accessibilityManager?.also {
            it.syncLabel        (this)
            it.syncEnabled      (this)
            it.syncDescription  (this)
            it.syncNextReadOrder(this)
        }

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

    private fun toPlane(point: Point): Vector3D = when {
        // TODO: Cache this to make it more efficient
        resolvedTransform.is3d -> (plane intersection Ray(point.as3d(), Vector3D(0.0, 0.0, -1.0))) ?: point.as3d()
        else                   -> point.as3d()
    }

    private fun getBoundingBox(bounds: Rectangle): Rectangle = when {
        resolvedTransform.isIdentity -> bounds
        else                         -> {
            val transformedPoints = resolvedTransform(bounds.points.map { it.as3d() }).map { it.as2d() }

            ConvexPolygon(transformedPoints[0], transformedPoints[1], transformedPoints[2], transformedPoints[3]).boundingRectangle
        }
    }

    internal val positionableWrapper by lazy { PositionableContainerWrapper(this) }

    private fun notifySizePreferencesChanged(old: SizePreferences, new: SizePreferences) {
        renderManager?.sizePreferencesChanged(this, old, new)

        (sizePreferencesChanged as PropertyObserversImpl).forEach {
            it(this, old, new)
        }
    }

    public companion object {
        /**
         * Delegate for properties that should trigger [View.styleChanged] when changed.
         *
         * @param initial value of the property
         * @param filter passed to [View.styleChanged] when it is called
         */
        public fun <T> styleProperty(initial: T, filter: (View) -> Boolean = { true }): ReadWriteProperty<View, T> = observable(initial) { _,_ ->
            styleChanged(filter)
        }

        public fun <T> observableStyleProperty(initial: T, filter: (View) -> Boolean = { true }, onChanged: (old: T, new: T) -> Unit): ReadWriteProperty<View, T> = observable(initial) { old, new ->
            styleChanged(filter)
            onChanged(old, new)
        }
    }
}

/**
 * Delegate for properties that should trigger [View.rerender] when changed.
 *
 * @param initial value of the property
 * @param onChange called when the property changes. There's no need to call rerender in [onChange].
 */
public inline fun <T> renderProperty(initial: T, noinline onChange: View.(old: T, new: T) -> Unit = { _,_ -> }): ReadWriteProperty<View, T> = observable(initial) { old, new ->
    rerender()
    onChange(old, new)
}

/**
 * The View's center point in its **parent's** coordinate system.
 */
public val View.center: Point get() = position + Point(width/2, height/2)

/**
 * @param filter used in search
 * @return the most recent ancestor that matches the [filter]
 */
public fun View.mostRecentAncestor(filter: (View) -> Boolean): View? {
    var result = parent

    while (result != null && !filter(result)) {
        result = result.parent
    }

    return result
}

/**
 * Delegate that manages installation and uninstallation of a [Behavior] and calls [beforeChange]
 * before applying changes.
 *
 * @param beforeChange is called before a change is applied
 */
public fun <T: View, B: Behavior<T>> behavior(beforeChange: (old: B?, new: B?) -> Unit = { _,_ -> }): ReadWriteProperty<T, B?> = BehaviorDelegateImpl(beforeChange, { _,_ -> })

public fun <T: View, B: Behavior<T>> behavior(beforeChange: (old: B?, new: B?) -> Unit = { _,_ -> }, afterChange: (old: B?, new: B?) -> Unit = { _,_ -> }): ReadWriteProperty<T, B?> = BehaviorDelegateImpl(beforeChange, afterChange)

private class BehaviorDelegateImpl<T: View, B: Behavior<T>>(private val beforeChange: (old: B?, new: B?) -> Unit, private val afterChange: (old: B?, new: B?) -> Unit): ReadWriteProperty<T, B?> {
    private var behavior: B? = null

    override operator fun getValue(thisRef: T, property: KProperty<*>): B? = behavior

    override operator fun setValue(thisRef: T, property: KProperty<*>, value: B?) {
        thisRef.childrenClipPath_    = null
        thisRef.clipCanvasToBounds_  = true
        thisRef.mirrorWhenRightLeft_ = true

        beforeChange(behavior, value)

        behavior?.uninstall(thisRef)

        val old = behavior

        behavior = value?.also { behavior ->
            behavior.install(thisRef)
            thisRef.childrenClipPath_    = behavior.childrenClipPath     (thisRef)
            thisRef.clipCanvasToBounds_  = behavior.clipCanvasToBounds   (thisRef)
            thisRef.mirrorWhenRightLeft_ = behavior.mirrorWhenRightToLeft(thisRef)
        }

        afterChange(old, behavior)
    }
}

/**
 * Class to enable `panel { ... }` DSL.
 * @property render operations to perform
 */
public class ViewBuilder internal constructor(): View() {
    private var render_: Canvas.() -> Unit = {}

    public var render: Canvas.() -> Unit get() = render_; set(new) { render_ = new }

    override fun render(canvas: Canvas) {
        render_(canvas)
    }
}

/**
 * DSL for creating a custom [View].
 *
 * @param block used to configure the View
 */
public fun view(block: ViewBuilder.() -> Unit): View = ViewBuilder().also(block)

/**
 * Scrolls the View to the given region if it is within a [ScrollPanel].
 *
 * @param rectangle within the View's coordinate space
 */
public fun View.scrollTo(rectangle: Rectangle) {
    val ancestor = if (parent is ScrollPanel) this else mostRecentAncestor { it.parent is ScrollPanel }

    ancestor?.let {
        val pointInAncestor = it.toLocal(rectangle.position, from = this)

        (it.parent as? ScrollPanel)?.scrollToVisible(rectangle.at(pointInAncestor))
    }
}