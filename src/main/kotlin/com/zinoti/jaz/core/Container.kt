//package com.zinoti.jaz.core
//
//import com.zinoti.jaz.containers.Padding
//import com.zinoti.jaz.event.ContainerEvent
//import com.zinoti.jaz.event.ContainerListener
//import com.zinoti.jaz.focus.FocusTraversalPolicy
//import com.zinoti.jaz.geometry.Dimension
//import com.zinoti.jaz.geometry.Point
//import com.zinoti.jaz.util.CollectionVisitHandler
//import com.zinoti.jaz.util.Visitor
//import kotlin.properties.ObservableProperty
//import kotlin.reflect.KProperty
//
//
//private typealias ListObserver<E> = (List<E>, List<E>) -> Unit
//
//private typealias ChangeListener<T> = (T, T) -> Unit
//
//class ObservableProperty<T>(initial: T): ObservableProperty<T>(initial) {
//    private val listeners: MutableSet<ChangeListener<T>> by lazy { mutableSetOf<ChangeListener<T>>() }
//
//    operator fun plus(listener: ChangeListener<T>): com.zinoti.jaz.core.ObservableProperty<T> {
//        listeners + listener
//
//        return this
//    }
//
//    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
//        super.afterChange(property, oldValue, newValue)
//
//        listeners.forEach {
//            it(newValue, oldValue)
//        }
//    }
//}
//
//class ObservableList<E>(val l: MutableList<E>): MutableList<E> by l {
//
//    val observers: MutableList<ListObserver<E>> by lazy { mutableListOf<ListObserver<E>>() }
//
//    override fun add(element: E): Boolean = execute { l.add(element) }
//
//    override fun remove(element: E): Boolean = execute { l.remove(element) }
//
//    override fun addAll(elements: Collection<E>): Boolean = execute { l.addAll(elements) }
//
//    override fun addAll(index: Int, elements: Collection<E>): Boolean = execute { l.addAll(index, elements) }
//
//    override fun removeAll(elements: Collection<E>): Boolean = execute { l.removeAll(elements) }
//    override fun retainAll(elements: Collection<E>): Boolean = execute { l.retainAll(elements) }
//    override fun clear() = execute { l.clear() }
//
//    override operator fun set(index: Int, element: E): E = execute { l.set(index, element) }
//
//    override fun add(index: Int, element: E) = execute { l.add(index, element) }
//
//    override fun removeAt(index: Int): E = execute { l.removeAt(index) }
//
//    private fun <T> execute(block: () -> T): T {
//        if (observers.isEmpty()) {
//            return block()
//        } else {
//            val old = ArrayList(l)
//
//            return block().also {
//                if (old != this) {
//                    observers.forEach {
//                        it(this, old)
//                    }
//                }
//            }
//        }
//    }
//}
//
//class Container: Gizmo(), Iterable<Gizmo> {
//
//    /**
//     * Gets the Padding.
//     *
//     * @return The Padding
//     */
//
//    /**
//     * Sets the Padding.
//     *
//     * @param aPadding The new Padding
//     */
//
//    var padding: Padding = Padding.NO_PADDING
//
//    /**
//     * Gets the Gizmos contained sorted by z-index.
//     * @return The children
//     */
//
//    val childrenByZIndex: Sequence<Gizmo> get() = childrenZ.asSequence()
//
//    val children: ObservableList<Gizmo> by lazy {
//        ObservableList(mutableListOf<Gizmo>()).also {
//            it.observers + { new, _ ->
//                new.forEach {
//                    require(it !== this                              ) { "cannot add to self"                 }
//                    require(!(it is Container && it.isAncestor(this))) { "cannot add ancestor to descendant"  }
//                }
//
////                informContainerListeners(ContainerEvent(this, ContainerEvent.Type.REMOVED, aChanges))
//            }
//        }
//    }
//
//    private val childrenZ: MutableList<Gizmo> by lazy { mutableListOf<Gizmo>() }
//
//    var layout: Layout? = null
//        set(new) {
//            if (field != new) {
//                field?.uninstall(this)
//
//                field = new
//
//                field?.layout(this)
//            }
//        }
//
//    /**
//     * Indicates whether this is a focus-cycle root.
//     *
//     * @return true if Container is a focus cycle root
//     */
//
//    var isFocusCycleRoot: Boolean = false
//
//    private val containerListeners: MutableList<ContainerListener> by lazy { mutableListOf<ContainerListener>() }
//    /**
//     * Gets the FocusTraversalPolicy used to govern focus traversal over children.
//     *
//     * @return The traversal policy
//     */
//
//    /**
//     * Sets the FocusTraversalPolicy used to govern focus traversal over children.
//     *
//     * @param aPolicy The new traversal policy
//     */
//
//    var focusTraversalPolicy: FocusTraversalPolicy? = null
//        get() {
//            if (!isFocusCycleRoot) {
//                return null
//            }
//
//            return field ?: focusCycleRoot?.focusTraversalPolicy ?: Service.locator().focusManager.defaultFocusTraversalPolicy
//        }
//
//    init {
//        focusable = false
//    }
//
//    /**
//     * Causes the Container to layout and re-draw itself.
//     */
//
//    fun revalidate() {
//        doLayout()
//        rerender()
//    }
//
//    /**
//     * Tells whether this Container is an ancestor of the Gizmo.
//     *
//     * @param gizmo The Gizmo
//     * @return true if the Gizmo is a descendant of the Container
//     */
//
//    fun isAncestor(gizmo: Gizmo): Boolean {
//        if (children.isNotEmpty()) {
//            var parent: Container? = gizmo.parent
//
//            while (parent != null) {
//                if (this === parent) {
//                    return true
//                }
//
//                parent = parent.parent
//            }
//        }
//
//        return false
//    }
//
//    /**
//     * Tells whether this Container contains this Gizmo.
//     *
//     * @param gizmo The Gizmo
//     * @return true if the Gizmo is a child of the Container
//     */
//
//    operator fun contains(gizmo: Gizmo): Boolean = gizmo.parent == this
//
//    /**
//     * Causes Container to layout its children if it has a Layout installed.
//     */
//
//    fun doLayout() = layout?.layout(this)
//
//
//    override var idealSize  : Dimension? = layout?.idealSize  (this) ?: super.idealSize
//    override var minimumSize: Dimension? = layout?.minimumSize(this) ?: super.minimumSize
//
//    /**
//     * Gets an iterator over the Container's children.
//     *
//     * @return The iterator
//     */
//
//    override fun iterator(): Iterator<Gizmo> = children.iterator()
//
//    /**
//     * Sets the z-index for the given Gizmo.
//     *
//     * @param gizmo The Gizmo
//     * @param index the new z-index
//     *
//     * @throws IndexOutOfBoundsException if `aIndex < 0 || aIndex > this.getNumChildren()`
//     */
//
//    fun setChildZIndex(gizmo: Gizmo, index: Int) {
////        Preconditions.checkElementIndex(aIndex, numChildren)
//
//        if (childrenZ.contains(gizmo) && index != getChildZIndex(gizmo)) {
//            childrenZ.remove(gizmo)
//            childrenZ.add(index, gizmo)
//
//            val aChanges = ArrayList<Pair<Gizmo, Int>>()
//
//            aChanges.add(Pair(gizmo, index))
//
////            informContainerListeners(ContainerEvent(this, ContainerEvent.Type.Z_INDEX, aChanges))
//        }
//    }
//
//    /**
//     * Gets the Gizmo's z-index.
//     *
//     * @param aGizmo The Gizmo
//     * @return The z-index (-1 if the Gizmo is not a child)
//     */
//
//    fun getChildZIndex(aGizmo: Gizmo): Int {
//        return childrenZ.indexOf(aGizmo)
//    }
//
//    /**
//     * Gets the Gizmo at the given point.
//     *
//     * @param aPoint The point
//     * @return The child (null if no child contains the given point)
//     */
//
//    fun child(at: Point): Gizmo? = layout?.childAtPoint(this, at) ?: childrenZ.firstOrNull { it.visible && it.containsPoint(at) }
//
//    /**
//     * Adds a ContainerListener to receive ContainerEvents.
//     *
//     * @param aListener The listener
//     */
//
//    fun addContainerListener(aListener: ContainerListener) {
//        containerListeners.add(aListener)
//    }
//
//    /**
//     * Removes a ContainerListener.
//     *
//     * @param aListener The listener
//     */
//
//    fun removeContainerListener(aListener: ContainerListener) {
//        containerListeners.remove(aListener)
//    }
//
//    /**
//     * Informs ContainerListeners of an event.
//     *
//     * @param aContainerEvent The event
//     */
//
//    protected fun informContainerListeners(aContainerEvent: ContainerEvent) {
//        var aVisitor: Visitor<ContainerListener>? = null
//
//        when (aContainerEvent.type) {
//            ContainerEvent.Type.ADDED   ->
//
//                aVisitor = Visitor { aListener -> aListener.itemsAdded(aContainerEvent) }
//
//            ContainerEvent.Type.REMOVED ->
//
//                aVisitor = Visitor { aListener -> aListener.itemsRemoved(aContainerEvent) }
//
//            ContainerEvent.Type.Z_INDEX ->
//
//                aVisitor = Visitor { aListener -> aListener.itemsZIndexChanged(aContainerEvent) }
//        }
//
//        CollectionVisitHandler.visitElements(containerListeners, aVisitor)
//    }
//
//    private interface Delegate<V, T> {
//        fun getValue(aInstance: T): V?
//    }
//
//}