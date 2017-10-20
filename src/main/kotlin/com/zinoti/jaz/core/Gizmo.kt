//package com.zinoti.jaz.core
//
//import com.zinoti.jaz.datatransport.DataTransporter
//import com.zinoti.jaz.datatransport.dragdrop.DropHandler
//import com.zinoti.jaz.drawing.Canvas
//import com.zinoti.jaz.drawing.Color
//import com.zinoti.jaz.drawing.Font
//import com.zinoti.jaz.drawing.RenderManager
//import com.zinoti.jaz.event.BoundsEvent
//import com.zinoti.jaz.event.BoundsListener
//import com.zinoti.jaz.event.DisplayRectEvent
//import com.zinoti.jaz.event.DisplayRectListener
//import com.zinoti.jaz.event.FocusEvent
//import com.zinoti.jaz.event.FocusListener
//import com.zinoti.jaz.event.KeyEvent
//import com.zinoti.jaz.event.KeyListener
//import com.zinoti.jaz.event.MouseEvent
//import com.zinoti.jaz.event.MouseListener
//import com.zinoti.jaz.event.MouseMotionListener
//import com.zinoti.jaz.event.MouseWheelEvent
//import com.zinoti.jaz.event.MouseWheelListener
//import com.zinoti.jaz.event.PropertyListener
//import com.zinoti.jaz.focus.FocusTraversalPolicy
//import com.zinoti.jaz.geometry.Dimension
//import com.zinoti.jaz.geometry.Point
//import com.zinoti.jaz.geometry.Rectangle
//import com.zinoti.jaz.system.Cursor
//import com.zinoti.jaz.ui.UI
//import com.zinoti.jaz.ui.UIManager
//import com.zinoti.jaz.util.CollectionVisitHandler
//import com.zinoti.jaz.util.KeyState
//import com.zinoti.jaz.util.ListenerManager
//import com.zinoti.jaz.util.Visitor
//import kotlin.properties.Delegates
//
//
///**
// * The smallest unit of displayable, interactive content within the framework.
// * Gizmos are the visual entities used to display components for an application.
// * User input events are sent to all Gizmos that are configured to receive them.
// * This allows them to response to user interaction or convey such events to
// * other parts of an application.
// *
// * @author Nicholas Eddy
// */
//
//abstract class Gizmo protected constructor() {
//
//    var enabled: Boolean by Delegates.observable(true) { _,_,_ ->
//
//    }
//
//    var focusable: Boolean by Delegates.observable(true) { _,_,_ ->
//
//    }
//
//    var visible: Boolean by Delegates.observable(true) { _,_,_ ->
//
//    }
//
//    var displayRectHandlingEnabled: Boolean by Delegates.observable(true) { _,_,_ ->
//        setDisplayRectHandlingReqiured(displayRectHandlingEnabled, displayRectHandlingEnabled)
//    }
//
//    var isKeyHandlingEnabled: Boolean by Delegates.observable(true) { _,_,_ ->
//
//    }
//
//    var mouseHandlingEnabled: Boolean by Delegates.observable(true) { _,_,_ ->
//
//    }
//
//    var mouseMotionHandlingEnabled: Boolean by Delegates.observable(true) { _,_,_ ->
//
//    }
//
//    var mouseWheelHandlingEnabled: Boolean by Delegates.observable(true) { _,_,_ ->
//
//    }
//
//    var font: Font? = null
//        get() = field ?: parent?.font
//
//    var cursor: Cursor? = null
//        get() = field ?: parent?.cursor
//
//    var foregroundColor: Color? = null
//        get() = field ?: parent?.foregroundColor
//
//
//    var backgroundColor: Color? = null
//        get() = field ?: parent?.backgroundColor
//
//
//    var x: Double
//        get() = bounds.x
//        set(x) = setBounds(x, y, width, height)
//
//    var y: Double
//        get() = bounds.y
//        set(y) = setBounds(x, y, width, height)
//
//    var width: Double
//        get() = bounds.width
//        set(width) = setBounds(x, y, width, height)
//
//    var height: Double
//        get() = bounds.height
//        set(height) = setBounds(x, y, width, height)
//
//    var position: Point
//        get() = bounds.position
//        set(position) = setBounds(position.x, position.y, width, height)
//
//    var size: Dimension
//        get() = bounds.size
//        set(size) = setBounds(x, y, size.width, size.height)
//
//    var bounds: Rectangle = Rectangle.EMPTY
//        set(bounds) {
//            if (field != bounds) {
//                val aOldBounds = field
//
//                field = bounds
//
//                val aBoundsEvent = BoundsEvent(this, aOldBounds, field)
//
//                CollectionVisitHandler.visitElements(getListeners(BoundsListener::class.java)) { it.boundsChanged(aBoundsEvent) }
//            }
//        }
//
//    /**
//     * @return the Gizmo's current display rectangle (in Gizmo's coordinate system) based on clipping
//     * with ancestor display rectangles.
//     */
//
//    val displayRect: Rectangle get() = renderManager?.getDisplayRect(this) ?: Rectangle.EMPTY
//
//    /**
//     * Gets the closest ancestor that is a focus cycle root.
//     *
//     * @return The closest focus cycle root ancestor
//     */
//
//    val focusCycleRoot: Container? = null
//        get() {
//            var parent = field
//
//            while (parent != null && !parent.isFocusCycleRoot) {
//                parent = parent.parent
//            }
//
//            return parent
//        }
//
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
//
//    var name: String = ""
//
//    /**
//     * @return this Gizmo's parent if any
//     */
//
//    /**
//     * Set the Gizmo's parent.
//     *
//     * NOTE: only visible within package
//     *
//     * @param aParent
//     */
//
//    // [Performance]
//    // No check to prevent setting self as parent since Container
//    // is the only place where this method is called from and it already
//    // prevents this by preventing a Container from being added to itself
//    var parent: Container? = null
//        internal set(new) {
//            if (field === new) {
//                return
//            }
//
//            field?.children?.remove(this)
//            field = new
//        }
//
//    var hasFocus = false
//        private set
//
//    /**
//     * The ideal size (width, height).
//     */
//    open var idealSize: Dimension? = null
//
//    open var minimumSize: Dimension? = null
//
//    private var toolTipText: String = ""
//
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
//    var keyEventsEnabled         = true
//    var mouseEventsEnabled       = true
//    var mouseWheelEventsEnabled  = true
//    var mouseMotionEventsEnabled = true
//    var displayRectEventsEnabled = true
//
//    var uiManager: UIManager? = null
//        private set
//
//    private var renderManager: RenderManager? = null
//
//    private val listeners = ListenerManager()
//
//    private val traversalKeys: MutableMap<FocusTraversalPolicy.TraversalType, Set<KeyState>> by lazy { mutableMapOf<FocusTraversalPolicy.TraversalType, Set<KeyState>>() }
//
//    /**
//     * Gives the Gizmo an opportunity to render itself to the given Canvas.
//     * Rendering duties are passed to the UI delegate if one is present.
//     *
//     * @param aCanvas The canvas upon which drawing will be done
//     */
//
//    fun render(aCanvas: Canvas) {
//        if (ui != null) {
//            (ui as UI<Gizmo>).render(aCanvas, this)
//        }
//    }
//
//    /**
//     * A way of prompting a Gizmo to redraw itself. This results
//     * in a render request to the rendering subsystem that will
//     * result in a call to [.render] if the Gizmo needs
//     * repainting.
//     */
//
//    fun rerender() {
//        renderManager?.render(this)
//    }
//
//    /**
//     * A way of prompting a Gizmo to redraw itself immediately. This results in
//     * a render request to the rendering subsystem that will result in a call to
//     * Gizmo.Render with no delay. Only use this method for time-sensitive
//     * drawing as is the case for animations.
//     */
//
//    fun rerenderNow() {
//        renderManager?.renderNow(this)
//    }
//
//    /**
//     * This is an event invoked on a Gizmo in response to a change in the display rectangle.
//     *
//     * @param aEvent The event
//     */
//
//    fun handleDisplayRectEvent(aEvent: DisplayRectEvent) {
//        CollectionVisitHandler.visitElements(getListeners(DisplayRectListener::class.java)
//        ) { it.displayRectChanged(aEvent) }
//    }
//
//    /**
//     * This is an event invoked on a Gizmo in response to a key event triggered in the subsystem.
//     *
//     * @param aKeyEvent The event
//     */
//
//    fun handleKeyEvent(aKeyEvent: KeyEvent) {
//        var aVisitor: Visitor<KeyListener>? = null
//
//        when (aKeyEvent.type) {
//            KeyEvent.Type.UP    ->
//
//                aVisitor = Visitor { it.keyReleased(aKeyEvent) }
//
//            KeyEvent.Type.DOWN  ->
//
//                aVisitor = Visitor { it.keyPressed(aKeyEvent) }
//
//            KeyEvent.Type.PRESS ->
//
//                aVisitor = Visitor { it.keyTyped(aKeyEvent) }
//        }
//
//        CollectionVisitHandler.visitElements(getListeners(KeyListener::class.java), aVisitor)
//    }
//
//    /**
//     * This is an event invoked on a Gizmo in response to a mouse event triggered in the subsystem.
//     *
//     * @param event The event
//     */
//
//    fun handleMouseEvent(event: MouseEvent) {
//        val visitor: Visitor<MouseListener>? = when (event.type) {
//            MouseEvent.Type.UP    -> Visitor { it.mouseReleased(event) }
//            MouseEvent.Type.DOWN  -> Visitor { it.mousePressed (event) }
//            MouseEvent.Type.EXIT  -> Visitor { it.mouseExited  (event) }
//            MouseEvent.Type.ENTER -> Visitor { it.mouseEntered (event) }
//            else                  -> null
//        }
//
//        CollectionVisitHandler.visitElements(getListeners(MouseListener::class.java), visitor)
//    }
//
//    /**
//     * This is an event invoked on a Gizmo in response to a mouse-motion event triggered in the subsystem.
//     *
//     * @param event The event
//     */
//
//    fun handleMouseMotionEvent(event: MouseEvent) {
//        val visitor: Visitor<MouseMotionListener>? = when (event.type) {
//            MouseEvent.Type.MOVE -> Visitor { it.mouseMoved  (event) }
//            MouseEvent.Type.DRAG -> Visitor { it.mouseDragged(event) }
//            else                 -> null
//        }
//
//        CollectionVisitHandler.visitElements(getListeners(MouseMotionListener::class.java), visitor)
//    }
//
//    /**
//     * This is an event invoked on a Gizmo in response to a mouse wheel event triggered in the subsystem.
//     *
//     * @param aMouseWheelEvent The event
//     */
//
//    fun handleMouseWheelEvent(aMouseWheelEvent: MouseWheelEvent) {
//        CollectionVisitHandler.visitElements(getListeners(MouseWheelListener::class.java)) { it.mouseWheelMoved(aMouseWheelEvent) }
//    }
//
//    /**
//     * This is an event invoked on a Gizmo in response to a focus event triggered in the subsystem.
//     *
//     * @param aFocusEvent The event
//     */
//
//    fun handleFocusEvent(aFocusEvent: FocusEvent) {
//        var aVisitor: Visitor<FocusListener>? = null
//
//        when (aFocusEvent.type) {
//            FocusEvent.Type.GAINED ->
//
//                if (!hasFocus) {
//                    hasFocus = true
//
//                    aVisitor = Visitor { it.focusGained(aFocusEvent) }
//                }
//
//            FocusEvent.Type.LOST   ->
//
//                if (hasFocus) {
//                    hasFocus = false
//
//                    aVisitor = Visitor { it.focusLost(aFocusEvent) }
//                }
//        }
//
//        CollectionVisitHandler.visitElements(getListeners(FocusListener::class.java), aVisitor)
//    }
//
//    /**
//     * This method is invoked by the Render system when the Gizmo is first added
//     * to the Display hierarchy.  This happens when the Gizmo itself,
//     * or one of it's ancestors is added to the Display.
//     */
//
//    fun addedToDisplay(aRenderManager: RenderManager, aUIManager: UIManager) {
//        uiManager      = aUIManager
//        renderManager = aRenderManager
//    }
//
//    /**
//     * This method is invoked by the Render system when the Gizmo is no longer
//     * included in the Display hierarchy.  This happens when the Gizmo itself,
//     * or one of it's ancestors is removed from the Display.
//     */
//
//    fun removedFromDisplay() {
//        uiManager      = null
//        renderManager = null
//    }
//
//    /**
//     * Causes Gizmo to synchronize its UI delegate from the UIManager.
//     */
//
//    fun revalidateUI() {
//        uiManager?.installUI(this) { this@Gizmo.ui = it }
//    }
//
//    /**
//     * Sets the tool-tip text.
//     *
//     * @param aToolTipText The new text
//     */
//
//    fun setToolTipText(aToolTipText: String) {
//        toolTipText = aToolTipText
//    }
//
//    /**
//     * Gets the tool-tip text based on the given mouse event. Override this method to provide
//     * multiple tool-tip text values for a single Gizmo.
//     *
//     * @param mouseEvent The mouse event to generate a tool-tip for
//     * @return The text
//     */
//
//    fun getToolTipText(mouseEvent: MouseEvent): String = toolTipText
//
//    /**
//     * Checks whether a point is within the boundaries of a Gizmo. Returns UI.ContainsPoint if a UI delegate
//     * present; otherwise, returns true if the point is whithin the Gizmo's bounding rectangle.
//     *
//     * @param point The point to check
//     * @return true if the point falls within the Gizmo
//     */
//
//    fun containsPoint(point: Point): Boolean = (ui as UI<Gizmo>?)?.containsPoint(this, point) ?: bounds.contains(point)
//
//    /**
//     * Sets the bounding rectangle.
//     *
//     * @param aX      The new x position
//     * @param aY      The new y position
//     * @param aWidth  The new width
//     * @param aHeight The new height
//     */
//
//    private fun setBounds(aX: Double, aY: Double, aWidth: Double, aHeight: Double) {
//        bounds = Rectangle.create(aX, aY, aWidth, aHeight)
//    }
//
//    /**
//     * Sets the keys used to control focus traversals of the given type.
//     *
//     * @param traversalType
//     * The traversal type
//     * @param keyStates
//     * The set of keys that will trigger this type of traversal
//     */
//
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
//
//    /**
//     * Gets the set of keys used to trigger this type of focus traversal.
//     *
//     * @return The set of keys that will trigger this type of traversal
//     */
//
//    fun getFocusTraversalKeys(aTraversalType: FocusTraversalPolicy.TraversalType): Set<KeyState>? = traversalKeys[aTraversalType]
//
//    /**
//     * Adds a DisplayRectListener to the Gizmo to receive DisplayRectEvents.
//     *
//     * @param listener The listener
//     */
//
//    fun addDisplayRectListener(listener: DisplayRectListener) {
//        val aIsDisplayRectHandlingEnabled = displayRectHandlingEnabled
//
//        listeners.add(listener, DisplayRectListener::class.java)
//
//        setDisplayRectHandlingReqiured(aIsDisplayRectHandlingEnabled, displayRectHandlingEnabled)
//    }
//
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
//
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
//
//    operator fun plus (listener: PropertyListener   ): Gizmo = this.also { listeners.add   (listener, PropertyListener::class.java   ) }
//    operator fun minus(listener: PropertyListener   ): Gizmo = this.also { listeners.remove(listener, PropertyListener::class.java   ) }
//
//    operator fun plus (listener: BoundsListener     ): Gizmo = this.also { listeners.add   (listener, BoundsListener::class.java     ) }
//    operator fun minus(listener: BoundsListener     ): Gizmo = this.also { listeners.remove(listener, BoundsListener::class.java     ) }
//
//    /**
//     * @param aType
//     * @return the list of listeners of a given type
//     */
//
//    protected fun <T> getListeners(aType: Class<T>): Set<T>? = listeners.getListeners(aType)
//
//    /**
//     * Updates property that indicates display rect monitoring is required for this Gizmo.
//     * This requirement takes effect whenever [.isDisplayRectHandlingEnabled] becomes true.
//     *
//     * @param aOldValue
//     * @param aNewValue
//     */
//
//    private fun setDisplayRectHandlingReqiured(aOldValue: Boolean, aNewValue: Boolean) {
////        setProperty(NamedPropertyDecorator(DISPLAYRECT_HANDLING_REQUIRED, SimpleProperty(aOldValue)),
////                aNewValue)
//    }
//}
