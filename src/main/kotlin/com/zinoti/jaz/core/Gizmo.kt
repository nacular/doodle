package com.zinoti.jaz.core

import com.zinoti.jaz.containers.Padding
import com.zinoti.jaz.drawing.Canvas
import com.zinoti.jaz.drawing.Color
import com.zinoti.jaz.drawing.Font
import com.zinoti.jaz.drawing.RenderManager
import com.zinoti.jaz.event.DisplayRectEvent
import com.zinoti.jaz.event.DisplayRectListener
import com.zinoti.jaz.event.FocusEvent
import com.zinoti.jaz.event.FocusListener
import com.zinoti.jaz.event.KeyEvent
import com.zinoti.jaz.event.KeyListener
import com.zinoti.jaz.event.MouseEvent
import com.zinoti.jaz.event.MouseListener
import com.zinoti.jaz.event.MouseMotionListener
import com.zinoti.jaz.event.MouseWheelEvent
import com.zinoti.jaz.geometry.Point
import com.zinoti.jaz.geometry.Rectangle
import com.zinoti.jaz.geometry.Size
import com.zinoti.jaz.system.Cursor
import com.zinoti.jaz.utils.ObservableList
import com.zinoti.jaz.utils.PropertyObservers
import com.zinoti.jaz.utils.PropertyObserversImpl
import kotlin.properties.Delegates
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

private class ObservableProperty<T>(initial: T, val observers: PropertyObserversImpl<T>): ObservableProperty<T>(initial) {
    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        super.afterChange(property, oldValue, newValue)

        observers.forEach { it(oldValue, newValue) }
    }
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

abstract class Gizmo protected constructor() {

    var hasFocus = false
        private set

    var name       = ""

    var enabled    : Boolean by Delegates.observable(true) { _,_,_ ->

    }
    var visible    : Boolean by Delegates.observable(true) { _,_,_ ->

    }
    var focusable  : Boolean by Delegates.observable(true) { _,_,_ ->

    }
    var idealSize  : Size? = null
        get() = layout?.idealSize(this) ?: field
    var minimumSize: Size = Size.Empty
        get() = layout?.idealSize(this) ?: field
    val displayRect: Rectangle get() = renderManager?.displayRect(this) ?: Rectangle.Empty

    var toolTipText: String = ""
        private set

    var monitorsMouse      : Boolean by Delegates.observable(true) { _,_,_ ->

    }
    var monitorsKeyboard   : Boolean by Delegates.observable(true) { _,_,_ ->

    }
    var monitorsMouseWheel : Boolean by Delegates.observable(true) { _,_,_ ->

    }
    var monitorsMouseMotion: Boolean by Delegates.observable(true) { _,_,_ ->

    }
    var monitorsDisplayRect: Boolean by Delegates.observable(true) { _,_,_ ->
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

    val boundsChange: PropertyObservers<Rectangle> = PropertyObserversImpl(mutableSetOf())

    var bounds: Rectangle by ObservableProperty(Rectangle.Empty, boundsChange as PropertyObserversImpl<Rectangle>)

    // ================= Container ================= //
    protected open var padding: Padding = Padding.NO_PADDING

    protected open var layout: Layout? by Delegates.observable<Layout?>(null) { _, old, new ->
        new?.layout(this)
    }

    protected open val children: ObservableList<Gizmo> by lazy {
        ObservableList(mutableListOf<Gizmo>()).also {
            it.onChange + { new, _ ->
                new.forEach {
                    require(it !== this         ) { "cannot add to self"                 }
                    require(!it.isAncestor(this)) { "cannot add ancestor to descendant"  }
                }
            }
        }
    }

    protected val childrenByZIndex: Sequence<Gizmo> get() = childrenZ.asSequence()

    /**
     * Tells whether this Container is an ancestor of the Gizmo.
     *
     * @param gizmo The Gizmo
     * @return true if the Gizmo is a descendant of the Container
     */

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

    private val childrenZ: MutableList<Gizmo> by lazy { mutableListOf<Gizmo>() }

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

    /**
     * @return this Gizmo's parent if any
     */

    /**
     * Set the Gizmo's parent.
     *
     * NOTE: only visible within package
     *
     * @param aParent
     */

    // [Performance]
    // No check to prevent setting self as parent since Container
    // is the only place where this method is called from and it already
    // prevents this by preventing a Container from being added to itself
    internal var parent: Gizmo? = null
        set(new) {
            if (field === new) {
                return
            }

            field?.children?.remove(this)
            field = new
        }

    protected fun revalidate() {
        doLayout()
        rerender()
    }

    /**
     * Tells whether this Container is an ancestor of the Gizmo.
     *
     * @param gizmo The Gizmo
     * @return true if the Gizmo is a descendant of the Container
     */

    /**
     * Tells whether this Container contains this Gizmo.
     *
     * @param gizmo The Gizmo
     * @return true if the Gizmo is a child of the Container
     */

    protected operator fun contains(gizmo: Gizmo): Boolean = gizmo.parent == this

    /**
     * Causes Container to layout its children if it has a Layout installed.
     */

    protected fun doLayout() = layout?.layout(this)

    /**
     * Sets the z-index for the given Gizmo.
     *
     * @param gizmo The Gizmo
     * @param index the new z-index
     *
     * @throws IndexOutOfBoundsException if `aIndex < 0 || aIndex > this.getNumChildren()`
     */

    protected fun setChildZIndex(gizmo: Gizmo, index: Int) {
//        Preconditions.checkElementIndex(aIndex, numChildren)

        if (childrenZ.contains(gizmo) && index != getChildZIndex(gizmo)) {
            childrenZ.remove(gizmo)
            childrenZ.add(index, gizmo)

            val aChanges = ArrayList<Pair<Gizmo, Int>>()

            aChanges.add(Pair(gizmo, index))

//            informContainerListeners(ContainerEvent(this, ContainerEvent.Type.Z_INDEX, aChanges))
        }
    }

    /**
     * Gets the Gizmo's z-index.
     *
     * @param aGizmo The Gizmo
     * @return The z-index (-1 if the Gizmo is not a child)
     */

    protected fun getChildZIndex(aGizmo: Gizmo): Int {
        return childrenZ.indexOf(aGizmo)
    }

    /**
     * Gets the Gizmo at the given point.
     *
     * @param aPoint The point
     * @return The child (null if no child contains the given point)
     */

    protected fun child(at: Point): Gizmo? = layout?.childAtPoint(this, at) ?: childrenZ.firstOrNull { it.visible && it.containsPoint(at) }

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
//    var uiManager: UIManager? = null
//        private set

    private var renderManager: RenderManager? = null

//    private val traversalKeys: MutableMap<FocusTraversalPolicy.TraversalType, Set<KeyState>> by lazy { mutableMapOf<FocusTraversalPolicy.TraversalType, Set<KeyState>>() }

    /**
     * Gives the Gizmo an opportunity to render itself to the given Canvas.
     * Rendering duties are passed to the UI delegate if one is present.
     *
     * @param aCanvas The canvas upon which drawing will be done
     */

    fun render(aCanvas: Canvas) {
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
        renderManager?.renderNow(this)
    }

    /**
     * This is an event invoked on a Gizmo in response to a change in the display rectangle.
     *
     * @param event The event
     */

    internal fun handleDisplayRectEvent(event: DisplayRectEvent) {
//        CollectionVisitHandler.visitElements(getListeners(DisplayRectListener::class.java)) { it.displayRectChanged(aEvent) }
    }

    /**
     * This is an event invoked on a Gizmo in response to a key event triggered in the subsystem.
     *
     * @param event The event
     */

    internal fun handleKeyEvent(event: KeyEvent) {
        val visitor: (KeyListener) -> Unit = when (event.type) {
            KeyEvent.Type.UP    -> { l -> l.keyReleased(event) }
            KeyEvent.Type.DOWN  -> { l -> l.keyPressed (event) }
            KeyEvent.Type.PRESS -> { l -> l.keyTyped   (event) }
        }

//        CollectionVisitHandler.visitElements(getListeners(KeyListener::class.java), visitor)
    }

    /**
     * This is an event invoked on a Gizmo in response to a mouse event triggered in the subsystem.
     *
     * @param event The event
     */

    internal fun handleMouseEvent(event: MouseEvent) {
        val visitor: (MouseListener) -> Unit = when (event.type) {
            MouseEvent.Type.Up    -> { l -> l.mouseReleased(event) }
            MouseEvent.Type.Down  -> { l -> l.mousePressed (event) }
            MouseEvent.Type.Exit  -> { l -> l.mouseExited  (event) }
            MouseEvent.Type.Enter -> { l -> l.mouseEntered (event) }
            else                  -> return
        }
//
//        CollectionVisitHandler.visitElements(getListeners(MouseListener::class.java), visitor)
    }

    /**
     * This is an event invoked on a Gizmo in response to a mouse-motion event triggered in the subsystem.
     *
     * @param event The event
     */

    internal fun handleMouseMotionEvent(event: MouseEvent) {
        val visitor: (MouseMotionListener) -> Unit = when (event.type) {
            MouseEvent.Type.Move -> { l -> l.mouseMoved  (event) }
            MouseEvent.Type.Drag -> { l -> l.mouseDragged(event) }
            else                 -> return
        }

//        CollectionVisitHandler.visitElements(getListeners(MouseMotionListener::class.java), visitor)
    }

    /**
     * This is an event invoked on a Gizmo in response to a mouse wheel event triggered in the subsystem.
     *
     * @param event The event
     */

    internal fun handleMouseWheelEvent(event: MouseWheelEvent) {
//        CollectionVisitHandler.visitElements(getListeners(MouseWheelListener::class.java)) { it.mouseWheelMoved(aMouseWheelEvent) }
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

    internal fun addedToDisplay(aRenderManager: RenderManager/*, aUIManager: UIManager*/) {
//        uiManager      = aUIManager
        renderManager = aRenderManager
    }

    /**
     * This method is invoked by the Render system when the Gizmo is no longer
     * included in the Display hierarchy.  This happens when the Gizmo itself,
     * or one of it's ancestors is removed from the Display.
     */

    internal fun removedFromDisplay() {
//        uiManager      = null
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
     * @param mouseEvent The mouse event to generate a tool-tip for
     * @return The text
     */
    fun getToolTipText(mouseEvent: MouseEvent): String = toolTipText

    /**
     * Checks whether a point is within the boundaries of a Gizmo. Returns UI.ContainsPoint if a UI delegate
     * present; otherwise, returns true if the point is whithin the Gizmo's bounding rectangle.
     *
     * @param point The point to check
     * @return true if the point falls within the Gizmo
     */

    fun containsPoint(point: Point): Boolean = false //(ui as UI<Gizmo>?)?.containsPoint(this, point) ?: bounds.contains(point)

    /**
     * Sets the bounding rectangle.
     *
     * @param x      The new x position
     * @param y      The new y position
     * @param width  The new width
     * @param height The new height
     */

    private fun setBounds(x: Double, y: Double, width: Double, height: Double) {
        bounds = Rectangle.create(x, y, width, height)
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

    /**
     * Adds a DisplayRectListener to the Gizmo to receive DisplayRectEvents.
     *
     * @param listener The listener
     */

    fun addDisplayRectListener(listener: DisplayRectListener) {
//        val aIsDisplayRectHandlingEnabled = displayRectHandlingEnabled
//
//        listeners.add(listener, DisplayRectListener::class.java)
//
//        setDisplayRectHandlingReqiured(aIsDisplayRectHandlingEnabled, displayRectHandlingEnabled)
    }

    /**
     * Removes a DisplayRectListener to the Gizmo.
     *
     * @param listener The listener
     */

    fun removeDisplayRectListener(listener: DisplayRectListener) {
//        val aIsDisplayRectHandlingEnabled = displayRectHandlingEnabled
//
//        listeners.remove(listener, DisplayRectListener::class.java)
//
//        setDisplayRectHandlingReqiured(aIsDisplayRectHandlingEnabled, displayRectHandlingEnabled)
    }

//    operator fun plus (listener: KeyListener        ): Gizmo = this.also { listeners.add   (listener, KeyListener::class.java        ) }
//    operator fun minus(listener: KeyListener        ): Gizmo = this.also { listeners.remove(listener, KeyListener::class.java        ) }
//
//    operator fun plus (listener: FocusListener      ): Gizmo = this.also { listeners.add   (listener, FocusListener::class.java      ) }
//    operator fun minus(listener: FocusListener      ): Gizmo = this.also { listeners.remove(listener, FocusListener::class.java      ) }
//
//    operator fun plus (listener: MouseListener      ): Gizmo = this.also { listeners.add   (listener, MouseListener::class.java      ) }
//    operator fun minus(listener: MouseListener      ): Gizmo = this.also { listeners.remove(listener, MouseListener::class.java      ) }
//
//    operator fun plus (listener: MouseMotionListener): Gizmo = this.also { listeners.add   (listener, MouseMotionListener::class.java) }
//    operator fun minus(listener: MouseMotionListener): Gizmo = this.also { listeners.remove(listener, MouseMotionListener::class.java) }
//
//    operator fun plus (listener: MouseWheelListener ): Gizmo = this.also { listeners.add   (listener, MouseWheelListener::class.java ) }
//    operator fun minus(listener: MouseWheelListener ): Gizmo = this.also { listeners.remove(listener, MouseWheelListener::class.java ) }

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
