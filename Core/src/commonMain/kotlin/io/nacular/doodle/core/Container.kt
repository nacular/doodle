package io.nacular.doodle.core

import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.ObservableList

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

    override fun iterator(): MutableIterator<View> = children.iterator()
}

/**
 * Adds [view] to the Container.
 *
 * @param view to be added
 */
public inline operator fun Container.plusAssign(view: View): Unit = children.plusAssign(view)


/**
 * Adds the given [views] to the Container.
 *
 * @param views to be added
 */
public inline operator fun Container.plusAssign(views: Iterable<View>): Unit = children.plusAssign(views)

/**
 * Removes [view] from the Container.
 *
 * @param view to be removed
 */
public inline operator fun Container.minusAssign(view: View): Unit = children.minusAssign(view)

/**
 * Removes the given [views] from the Container.
 *
 * @param views to be removed
 */
public inline operator fun Container.minusAssign(views: Iterable<View>): Unit = children.minusAssign(views)

/**
 * Class to enable `container { ... }` DSL.
 * @property render operations to perform
 */
public class ContainerBuilder: Container() {
    private var render_: Canvas.() -> Unit = {}

    public var render: Canvas.() -> Unit get() = render_; set(new) { render_ = new }

    override fun render(canvas: Canvas) {
        render_(canvas)
    }
}

/**
 * DSL for creating a custom [Container].
 *
 * @param block used to configure the View
 */
public fun container(block: ContainerBuilder.() -> Unit): Container = ContainerBuilder().also(block)