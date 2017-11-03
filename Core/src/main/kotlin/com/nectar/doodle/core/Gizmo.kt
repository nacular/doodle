package com.nectar.doodle.core

import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.RenderManager
import com.nectar.doodle.event.DisplayRectEvent
import com.nectar.doodle.event.FocusEvent
import com.nectar.doodle.event.FocusListener
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.event.MouseWheelEvent
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.SystemMouseEvent.Type.Down
import com.nectar.doodle.system.SystemMouseEvent.Type.Drag
import com.nectar.doodle.system.SystemMouseEvent.Type.Enter
import com.nectar.doodle.system.SystemMouseEvent.Type.Exit
import com.nectar.doodle.system.SystemMouseEvent.Type.Move
import com.nectar.doodle.system.SystemMouseEvent.Type.Up
import com.nectar.doodle.ui.UIManager
import com.nectar.doodle.utils.ObservableList
import com.nectar.doodle.utils.Pool
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import com.nectar.doodle.utils.SetPool
import com.nectar.doodle.utils.observable
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

private class ObservableProperty<T>(initial: T, val owner: () -> Gizmo, val observers: PropertyObserversImpl<Gizmo, T>): ObservableProperty<T>(initial) {
    override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T) = newValue != oldValue

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        super.afterChange(property, oldValue, newValue)

        observers.forEach { it(owner(), oldValue, newValue) }
    }
}


interface EventSource {
    val mouseChanged      : Pool<MouseListener>
    val mouseMotionChanged: Pool<MouseMotionListener>
}

/**
 * The smallest unit of displayable, interactive content within the framework.
 * Gizmos are the visual entities used to display components for an application.
 * User input events are sent to all Gizmos that are configured to receive them.
 * This allows them to response to user interaction or convey such events to
 * other parts of an application.
 *
 * @author Nicholas Eddy
 */
abstract class Gizmo protected constructor(): EventSource {

    var hasFocus = false
        private set

    var name       = ""

    var enabled    : Boolean by observable(true) { _,_,_ ->

    }
    var visible    : Boolean by observable(true) { _,_,_ ->

    }
    var focusable  : Boolean by observable(true) { _,_,_ ->

    }
    var idealSize  : Size? = null
        get() = layout?.idealSize(this, field) ?: field
    var minimumSize: Size = Size.Empty
        get() = layout?.idealSize(this, field) ?: field

    val displayRect get() = renderManager?.displayRect(this) ?: Rectangle.Empty

    var toolTipText: String = ""
        private set

    var monitorsMouse      : Boolean by observable(true ) { _,_,_ ->

    }
    var monitorsKeyboard   : Boolean by observable(true ) { _,_,_ ->

    }
    var monitorsMouseWheel : Boolean by observable(true ) { _,_,_ ->

    }
    var monitorsMouseMotion: Boolean by observable(true ) { _,_,_ ->

    }
    var monitorsDisplayRect: Boolean by observable(false) { _,_,_ ->
        setDisplayRectHandlingReqiured(monitorsDisplayRect, monitorsDisplayRect)
    }

    var font: Font? = null
        get() = field ?: parent?.font

    var cursor: Cursor? = null
        get() = field ?: parent?.cursor

    var foregroundColor: Color? = null
        get() = field ?: parent?.foregroundColor

    var backgroundColor: Color? = null
        get() = field ?: parent?.backgroundColor

    var x: Double
        get( ) = bounds.x
        set(x) = setBounds(x, y, width, height)

    var y: Double
        get( ) = bounds.y
        set(y) = setBounds(x, y, width, height)

    var position: Point
        get(        ) = bounds.position
        set(position) = setBounds(position.x, position.y, width, height)

    var width: Double
        get(     ) = bounds.width
        set(width) = setBounds(x, y, width, height)

    var height: Double
        get(      ) = bounds.height
        set(height) = setBounds(x, y, width, height)

    var size: Size
        get(    ) = bounds.size
        set(size) = setBounds(x, y, size.width, size.height)

    val boundsChange: PropertyObservers<Gizmo, Rectangle> = PropertyObserversImpl(mutableSetOf())

    var bounds: Rectangle by ObservableProperty(Rectangle.Empty, { this }, boundsChange as PropertyObserversImpl<Gizmo, Rectangle>)

    // ================= Container ================= //
    internal val insets_ get() = insets
    protected open var insets  = Insets.None

    internal val layout_ get() = layout
    protected open var layout: Layout? by observable<Layout?>(null) { _, _, new ->
        new?.layout(this)
    }

    internal val children_ get() = children
    protected open val children: ObservableList<Gizmo, Gizmo> by lazy {
        ObservableList(this, mutableListOf<Gizmo>()).also {
            it.onChange += { _, removed, added ->
                removed.map { children[it] }.forEach { it.parent = null }
                added.values.forEach {
                    require(it !== this         ) { "cannot add to self"                 }
                    require(!it.isAncestor(this)) { "cannot add ancestor to descendant"  }

                    it.parent = this
                }
            }
        }
    }

    /**
     * Tells whether this Container is an ancestor of the Gizmo.
     *
     * @param gizmo The Gizmo
     * @return true if the Gizmo is a descendant of the Container
     */
    internal fun isAncestor_(of: Gizmo) = isAncestor(of)
    protected open fun isAncestor(of: Gizmo): Boolean {
        if (children.isNotEmpty()) {
            var parent = of.parent

            while (parent != null) {
                if (this === parent) {
                    return true
                }

                parent = parent.parent
            }
        }

        return false
    }

    protected open var isFocusCycleRoot: Boolean = false

    protected val focusCycleRoot: Gizmo? = null
        get() {
            var parent = field

            while (parent != null && !parent.isFocusCycleRoot) {
                parent = parent.parent
            }

            return parent
        }

//    var ui: UI<*>? = null
//        protected set(aUI) {
//            (field as UI<Gizmo>?)?.uninstall(this)
//
//            field = aUI
//
//            (field as UI<Gizmo>?)?.install(this)
//
//            rerender()
//        }

    // [Performance]
    // No check to prevent setting self as parent since Container
    // is the only place where this method is called from and it already
    // prevents this by preventing a Container from being added to itself
    var parent: Gizmo? = null
        private set(new) {
            if (field === new) {
                return
            }

            field?.children?.remove(this)

            (parentChange as PropertyObserversImpl).forEach { it(this, field, new) }

            field = new
        }

    val parentChange: PropertyObservers<Gizmo, Gizmo?> = PropertyObserversImpl(mutableSetOf())


    internal fun revalidate_() = revalidate()
    protected fun revalidate() {
        doLayout()
        rerender()
    }

    /**
     * Tells whether this Gizmo contains the given child.
     *
     * @param child The Gizmo
     * @return true if the Gizmo is a child of the Container
     */
    protected operator fun contains(child: Gizmo): Boolean = child.parent == this

    /**
     * Causes Container to layout its children if it has a Layout installed.
     */
    internal fun doLayout_() = doLayout()
    protected fun doLayout() = layout?.layout(this)

    /**
     * Sets the z-index for the given Gizmo.
     *
     * @param of The Gizmo
     * @param index the new z-index
     *
     * @throws IndexOutOfBoundsException if `index !in 0 until this.children.size`
     */
    protected fun setZIndex(of: Gizmo, index: Int) {
        // TODO: Make this a bit more efficient
        if (children.contains(of) && index != zIndex(of)) {
            children.remove(of)
            children.add(index, of)
        }
    }

    /**
     * Gets the Gizmo's z-index.
     *
     * @param aGizmo The Gizmo
     * @return The z-index (-1 if the Gizmo is not a child)
     */
    internal fun zIndex_(of: Gizmo) = zIndex(of)
    protected fun zIndex(of: Gizmo) = children.size - children.indexOf(of)

    /**
     * Gets the Gizmo at the given point.
     *
     * @param aPoint The point
     * @return The child (null if no child contains the given point)
     */
    internal fun child_(at: Point) = child(at)
    protected open fun child(at: Point): Gizmo? = layout?.child(this, at) ?: children.lastOrNull { it.visible && it.contains(at) }

//    var dropHandler: DropHandler? = null
//        set(new) {
//            if (field === new) {
//                return
//            }
//
//            field?.target = null
//
//            field = new?.also {
//                it.target = this
//            }
//        }
//
//    var inputVerifier: InputVerifier<*>? = null
//
//    var dataTransporter: DataTransporter? = null
//
    private var uiManager    : UIManager?     = null
    private var renderManager: RenderManager? = null

//    private val traversalKeys: MutableMap<FocusTraversalPolicy.TraversalType, Set<KeyState>> by lazy { mutableMapOf<FocusTraversalPolicy.TraversalType, Set<KeyState>>() }

    /**
     * Gives the Gizmo an opportunity to render itself to the given Canvas.
     * Rendering duties are passed to the UI delegate if one is present.
     *
     * @param canvas The canvas upon which drawing will be done
     */

    open fun render(canvas: Canvas) {
//        if (ui != null) {
//            (ui as UI<Gizmo>).render(aCanvas, this)
//        }
    }

    /**
     * A way of prompting a Gizmo to redraw itself. This results
     * in a render request to the rendering subsystem that will
     * result in a call to [.render] if the Gizmo needs
     * repainting.
     */
    fun rerender() {
        renderManager?.render(this)
    }

    /**
     * A way of prompting a Gizmo to redraw itself immediately. This results in
     * a render request to the rendering subsystem that will result in a call to
     * Gizmo.Render with no delay. Only use this method for time-sensitive
     * drawing as is the case for animations.
     */
    fun rerenderNow() {
        renderManager?.renderNow(this) // TODO: Remove?
    }

    /**
     * This is an event invoked on a Gizmo in response to a change in the display rectangle.
     *
     * @param event The event
     */
    internal fun handleDisplayRectEvent(event: DisplayRectEvent) {
    }

    internal fun handleKeyEvent_(event: KeyEvent) = handleKeyEvent(event)

    /**
     * This is an event invoked on a Gizmo in response to a key event triggered in the subsystem.
     *
     * @param event The event
     */
    protected open fun handleKeyEvent(event: KeyEvent) {
        val visitor: (KeyListener) -> Unit = when (event.type) {
            KeyEvent.Type.UP    -> { l -> l.keyReleased(event) }
            KeyEvent.Type.DOWN  -> { l -> l.keyPressed (event) }
            KeyEvent.Type.PRESS -> { l -> l.keyTyped   (event) }
        }
    }

    internal fun handleMouseEvent_(event: MouseEvent) = handleMouseEvent(event)

    /**
     * This is an event invoked on a Gizmo in response to a mouse event triggered in the subsystem.
     *
     * @param event The event
     */
    protected open fun handleMouseEvent(event: MouseEvent) {
        val visitor: (MouseListener) -> Unit = when (event.type) {
            Up    -> { l -> l.mouseReleased(event) }
            Down  -> { l -> l.mousePressed (event) }
            Exit  -> { l -> l.mouseExited  (event) }
            Enter -> { l -> l.mouseEntered (event) }
            else  -> return
        }

        mouseChanged.forEach(visitor)
    }

    internal fun handleMouseMotionEvent_(event: MouseEvent) = handleMouseMotionEvent(event)

    /**
     * This is an event invoked on a Gizmo in response to a mouse-motion event triggered in the subsystem.
     *
     * @param event The event
     */
    protected open fun handleMouseMotionEvent(event: MouseEvent) {
        val visitor: (MouseMotionListener) -> Unit = when (event.type) {
            Move -> { l -> l.mouseMoved  (event) }
            Drag -> { l -> l.mouseDragged(event) }
            else -> return
        }

        mouseMotionChanged.forEach(visitor)
    }

    internal fun handleMouseWheelEvent_(event: MouseWheelEvent) = handleMouseWheelEvent(event)

    /**
     * This is an event invoked on a Gizmo in response to a mouse wheel event triggered in the subsystem.
     *
     * @param event The event
     */
    protected open fun handleMouseWheelEvent(event: MouseWheelEvent) {
    }

    /**
     * This is an event invoked on a Gizmo in response to a focus event triggered in the subsystem.
     *
     * @param event The event
     */

    internal fun handleFocusEvent(event: FocusEvent) {
        val aVisitor: (FocusListener) -> Unit = when {
            event.type == FocusEvent.Type.Gained && !hasFocus -> {
                hasFocus = true

                { l -> l.focusGained(event) }
            }

            event.type == FocusEvent.Type.Lost && hasFocus -> {
                hasFocus = false

                { l -> l.focusLost(event) }
            }
            else -> return
        }

//        CollectionVisitHandler.visitElements(getListeners(FocusListener::class.java), aVisitor)
    }

    /**
     * This method is invoked by the Render system when the Gizmo is first added
     * to the Display hierarchy.  This happens when the Gizmo itself,
     * or one of it's ancestors is added to the Display.
     */

    internal fun addedToDisplay(renderManager: RenderManager, uiManager: UIManager) {
        this.uiManager     = uiManager
        this.renderManager = renderManager
    }

    /**
     * This method is invoked by the Render system when the Gizmo is no longer
     * included in the Display hierarchy.  This happens when the Gizmo itself,
     * or one of it's ancestors is removed from the Display.
     */

    internal fun removedFromDisplay() {
        uiManager     = null
        renderManager = null
    }

    /**
     * Causes Gizmo to synchronize its UI delegate from the UIManager.
     */

    internal fun revalidateUI() {
//        uiManager?.installUI(this) { this@Gizmo.ui = it }
    }

    /**
     * Sets the tool-tip text.
     *
     * @param toolTipText The new text
     */

    fun setToolTipText(toolTipText: String) {
        this.toolTipText = toolTipText
    }

    /**
     * Gets the tool-tip text based on the given mouse event. Override this method to provide
     * multiple tool-tip text values for a single Gizmo.
     *
     * @param event The mouse event to generate a tool-tip for
     * @return The text
     */
    fun getToolTipText(event: MouseEvent): String = toolTipText

    /**
     * Checks whether a point is within the boundaries of a Gizmo. Returns UI.ContainsPoint if a UI delegate
     * present; otherwise, returns true if the point is whithin the Gizmo's bounding rectangle.
     *
     * @param point The point to check
     * @return true if the point falls within the Gizmo
     */
    open fun contains(point: Point): Boolean = bounds.contains(point) //(ui as UI<Gizmo>?)?.contains(this, point) ?: bounds.contains(point)

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

    /**
     * Sets the keys used to control focus traversals of the given type.
     *
     * @param traversalType
     * The traversal type
     * @param keyStates
     * The set of keys that will trigger this type of traversal
     */

//    fun setFocusTraversalKeys(traversalType: FocusTraversalPolicy.TraversalType, keyStates: Set<KeyState>?) {
//        //        if( aSet == null )
//        //        {
//        //            Container aParent = mParent;
//        //
//        //            while( aParent != null && aParent.getFocusTraversalKeys( aTraversalType ) == null )
//        //            {
//        //                aParent = aParent.getParent();
//        //            }
//        //
//        //            if( aParent != null && aParent.getFocusTraversalKeys( aTraversalType ) != null )
//        //            {
//        //                aSet = aParent.getFocusTraversalKeys( aTraversalType );
//        //            }
//        //        }
//
//        keyStates?.let { traversalKeys?.put(traversalType, it) } ?: traversalKeys?.remove(traversalType)
//    }

    /**
     * Gets the set of keys used to trigger this type of focus traversal.
     *
     * @return The set of keys that will trigger this type of traversal
     */

//    fun getFocusTraversalKeys(aTraversalType: FocusTraversalPolicy.TraversalType): Set<KeyState>? = traversalKeys[aTraversalType]

//    /**
//     * Adds a DisplayRectListener to the Gizmo to receive DisplayRectEvents.
//     *
//     * @param listener The listener
//     */
//    fun addDisplayRectListener(listener: DisplayRectListener) {
//        val aIsDisplayRectHandlingEnabled = displayRectHandlingEnabled
//
//        listeners.add(listener, DisplayRectListener::class.java)
//
//        setDisplayRectHandlingReqiured(aIsDisplayRectHandlingEnabled, displayRectHandlingEnabled)
//    }

//    /**
//     * Removes a DisplayRectListener to the Gizmo.
//     *
//     * @param listener The listener
//     */
//
//    fun removeDisplayRectListener(listener: DisplayRectListener) {
//        val aIsDisplayRectHandlingEnabled = displayRectHandlingEnabled
//
//        listeners.remove(listener, DisplayRectListener::class.java)
//
//        setDisplayRectHandlingReqiured(aIsDisplayRectHandlingEnabled, displayRectHandlingEnabled)
//    }

    fun toLocal(point: Point, from: Gizmo): Point {
        val source      = from.toAbsolute(point       )
        val destination = this.toAbsolute(Point.Origin)

        return source - destination
    }

    fun toAbsolute  (point: Point) = modifyHierarchicically(point) { p, gizmo -> p + gizmo.position }
    fun fromAbsolute(point: Point) = modifyHierarchicically(point) { p, gizmo -> p - gizmo.position }

    private fun modifyHierarchicically(point: Point, operation: (Point, Gizmo) -> Point): Point {
        var gizmo  = this as Gizmo?
        var result = point

        while (gizmo != null) {
            result = operation(result, gizmo)
            gizmo  = gizmo.parent
        }

        return result
    }

//    operator fun plus (listener: KeyListener        ): Gizmo = this.also { listeners.add   (listener, KeyListener::class.java        ) }
//    operator fun minus(listener: KeyListener        ): Gizmo = this.also { listeners.remove(listener, KeyListener::class.java        ) }
//
//    operator fun plus (listener: FocusListener      ): Gizmo = this.also { listeners.add   (listener, FocusListener::class.java      ) }
//    operator fun minus(listener: FocusListener      ): Gizmo = this.also { listeners.remove(listener, FocusListener::class.java      ) }
//
//    operator fun plusAssign (listener: MouseListener) { mouseEvents += listener }
//    operator fun minusAssign(listener: MouseListener) { mouseEvents -= listener }
//
//    operator fun plus (listener: MouseMotionListener): Gizmo = this.also { listeners.add   (listener, MouseMotionListener::class.java) }
//    operator fun minus(listener: MouseMotionListener): Gizmo = this.also { listeners.remove(listener, MouseMotionListener::class.java) }
//
//    operator fun plus (listener: MouseWheelListener ): Gizmo = this.also { listeners.add   (listener, MouseWheelListener::class.java ) }
//    operator fun minus(listener: MouseWheelListener ): Gizmo = this.also { listeners.remove(listener, MouseWheelListener::class.java ) }

    override val mouseChanged       = SetPool<MouseListener      >(mutableSetOf())
    override val mouseMotionChanged = SetPool<MouseMotionListener>(mutableSetOf())

    /**
     * @param aType
     * @return the list of listeners of a given type
     */

//    protected fun <T> getListeners(aType: Class<T>): Set<T>? = listeners.getListeners(aType)

    /**
     * Updates property that indicates display rect monitoring is required for this Gizmo.
     * This requirement takes effect whenever [.isDisplayRectHandlingEnabled] becomes true.
     *
     * @param oldValue
     * @param newValue
     */

    private fun setDisplayRectHandlingReqiured(oldValue: Boolean, newValue: Boolean) {
//        setProperty(NamedPropertyDecorator(DISPLAYRECT_HANDLING_REQUIRED, SimpleProperty(aOldValue)),
//                aNewValue)
    }
}
