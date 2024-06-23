package io.nacular.doodle.core

import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.focus.FocusTraversalPolicy
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.ObservableList
import kotlin.js.JsName

/**
 * Represents a View that can have children and [Layout].
 */
public open class Container: View(), PositionableContainer, Iterable<View> {
    /** Hint to [layout] that children within the Container should be inset from the edges */
    public override var insets: Insets
        get(   ) = super.insets
        set(new) { super.insets = new }

    /**
     * Manages the positioning of children within the Container.  The framework will call use this
     * to reposition the children whenever the Container's bounds change, or the bounds, visibility
     * of a child changes.
     */
    public override var layout: Layout?
        get(   ) = super.layout
        set(new) { super.layout = new }

    public override var layout2: Layout2?
        get(   ) = super.layout2
        set(new) { super.layout2 = new }

    public override var isFocusCycleRoot: Boolean
        get(   ) = super.isFocusCycleRoot
        set(new) { super.isFocusCycleRoot = new }

    /** The list of children within the Container */
    public override val children: ObservableList<View> = super.children

    /**
     * @param view in question
     * @return `true` IFF [view] is a descendant of this Container
     */
    public override fun ancestorOf(view: View): Boolean = super.ancestorOf(view)

    /**
     * @param at the x,y within the Container's coordinate-space
     * @return a View if one is found to contain the given point
     */
    public override fun child(at: Point): View? = super.child(at)

    public override fun relayout() { super.relayout() }

    /**
     * Adds [view] to the Container.
     *
     * @param view to be added
     */
    public operator fun plusAssign(view: View): Unit = children.plusAssign(view)

    /**
     * Adds the given [views] to the Container.
     *
     * @param views to be added
     */
    public operator fun plusAssign(views: Collection<View>): Unit = children.plusAssign(views)

    /**
     * Removes [view] from the Container.
     *
     * @param view to be removed
     */
    public operator fun minusAssign(view: View): Unit = children.minusAssign(view)

    /**
     * Removes the given [views] from the Container.
     *
     * @param views to be removed
     */
    public operator fun minusAssign(views: Collection<View>): Unit = children.minusAssign(views)

    override fun iterator(): MutableIterator<View> = children.iterator()
}

/**
 * Class to enable `container { ... }` DSL.
 * @property render operations to perform
 */
public class ContainerBuilder: Container() {
    /** @see View.render */
    public var render: Canvas.() -> Unit = {}

    /** @see View.focusTraversalPolicy */
    public override var focusTraversalPolicy: FocusTraversalPolicy? get() = super.focusTraversalPolicy; set(new) {
        super.focusTraversalPolicy = new
    }

    /** @see View.isFocusCycleRoot */
    public override var isFocusCycleRoot: Boolean; get() = super.isFocusCycleRoot; set(value) {
        super.isFocusCycleRoot = value
    }

    /** @see View.addedToDisplay */
    @JsName("addedToDisplayLambda")
    public var addedToDisplay: () -> Unit = {}

    /** @see View.removedFromDisplay */
    @JsName("removedFromDisplayLambda")
    public var removedFromDisplay:() -> Unit = {}

    /** @see View.shouldYieldFocus */
    @JsName("shouldYieldFocusLambda")
    public var shouldYieldFocus:() -> Boolean = { super.shouldYieldFocus() }

    /** @see View.contains */
    public var contains: (point: Point) -> Boolean = { super.contains(it) }

    /** @see View.intersects */
    public var intersects: (point: Point) -> Boolean = { super.intersects(it) }

    /** Adds a child to this View */
    public operator fun View.unaryPlus () { children += this }

    /** Removes a child to this View */
    public operator fun View.unaryMinus() { children -= this }

    /** Adds a collection of children to this View */
    public operator fun Collection<View>.unaryPlus() { children += this }

    /** Removes a collection of children to this View */
    public operator fun Collection<View>.unaryMinus() { children -= this.toSet() }

    override fun render            (canvas: Canvas): Unit    = render.invoke            (canvas)
    override fun removedFromDisplay(              ): Unit    = removedFromDisplay.invoke(      )
    override fun addedToDisplay    (              ): Unit    = addedToDisplay.invoke    (      )
    override fun shouldYieldFocus  (              ): Boolean = shouldYieldFocus.invoke  (      )
    override fun contains          (point: Point  ): Boolean = contains.invoke          (point )
    override fun intersects        (point: Point  ): Boolean = intersects.invoke        (point )
}

/**
 * DSL for creating a custom [Container].
 *
 * @param block used to configure the View
 */
public fun container(block: ContainerBuilder.() -> Unit): Container = ContainerBuilder().also(block)