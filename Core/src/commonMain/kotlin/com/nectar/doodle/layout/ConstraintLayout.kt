@file:Suppress("NestedLambdaShadowedImplicitParameter")

package com.nectar.doodle.layout

import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.PositionableContainer
import com.nectar.doodle.core.PositionableWrapper
import com.nectar.doodle.core.View
import com.nectar.doodle.geometry.Rectangle
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 11/1/17.
 */

val center              : (Constraints.() -> Unit) = { center = parent.center                                                             }
val fill                : (Constraints.() -> Unit) = { top = parent.top; left = parent.left; width = parent.width; height = parent.height }
fun fill(insets: Insets): (Constraints.() -> Unit) = {
    top    = parent.top    + insets.top
    left   = parent.left   + insets.left
    right  = parent.right  - insets.right
    bottom = parent.bottom - insets.bottom
}

abstract class ConstraintLayout: Layout {
    abstract fun constrain(a: View,                                     block: ConstraintBlockContext.(Constraints                                                    ) -> Unit): ConstraintLayout
    abstract fun constrain(a: View, b: View,                            block: ConstraintBlockContext.(Constraints, Constraints                                       ) -> Unit): ConstraintLayout
    abstract fun constrain(a: View, b: View, c: View,                   block: ConstraintBlockContext.(Constraints, Constraints, Constraints                          ) -> Unit): ConstraintLayout
    abstract fun constrain(a: View, b: View, c: View, d: View,          block: ConstraintBlockContext.(Constraints, Constraints, Constraints, Constraints             ) -> Unit): ConstraintLayout
    abstract fun constrain(a: View, b: View, c: View, d: View, e: View, block: ConstraintBlockContext.(Constraints, Constraints, Constraints, Constraints, Constraints) -> Unit): ConstraintLayout

    abstract fun unconstrain(vararg views: View): ConstraintLayout
}

private class ConstraintLayoutImpl(private val display: Display? = null, vararg constraints: ConstraintsImpl): ConstraintLayout() {
    private val constraints by lazy { constraints.fold(mutableMapOf<View, ConstraintsImpl>()) { s, r -> s[r.target] = r; s } }
    private val processed   by lazy { mutableSetOf<View>()                                                                   }
    private val processing  by lazy { mutableSetOf<View>()                                                                   }

    private var displayConstraints: DisplayConstraints? = null

    fun constrain(view: View, within: Rectangle, block: ConstraintBlockContext.(Constraints) -> Unit) {
        constraints(view, within).apply { block(ConstraintBlockContextImpl(parent), this) }

        constraints[view]?.let { layoutChild(view, it) }
    }

    override fun constrain(a: View, block: ConstraintBlockContext.(Constraints) -> Unit): ConstraintLayout {
        constraints(a).let { (a) -> block(ConstraintBlockContextImpl(a.parent), a)
            return this
        }
    }

    override fun constrain(a: View, b: View, block: ConstraintBlockContext.(Constraints, Constraints) -> Unit): ConstraintLayout {
        constraints(a, b).let { (a, b) -> block(ConstraintBlockContextImpl(a.parent), a, b)
            return this
        }
    }

    override fun constrain(a: View, b: View, c: View, block: ConstraintBlockContext.(Constraints, Constraints, Constraints) -> Unit): ConstraintLayout {
        constraints(a, b, c).let { (a, b, c) -> block(ConstraintBlockContextImpl(a.parent), a, b, c)
            return this
        }
    }

    override fun constrain(a: View, b: View, c: View, d: View, block: ConstraintBlockContext.(Constraints, Constraints, Constraints, Constraints) -> Unit): ConstraintLayout {
        constraints(a, b, c, d).let { (a, b, c, d) -> block(ConstraintBlockContextImpl(a.parent), a, b, c, d)
            return this
        }
    }

    override fun constrain(a: View, b: View, c: View, d: View, e: View, block: ConstraintBlockContext.(Constraints, Constraints, Constraints, Constraints, Constraints) -> Unit): ConstraintLayout {
        constraints(a, b, c, d, e).let { (a, b, c, d, e) -> block(ConstraintBlockContextImpl(a.parent), a, b, c, d, e)
            return this
        }
    }

    override fun unconstrain(vararg views: View): ConstraintLayout {
        views.forEach { constraints.remove(it) }

        return this
    }

    private fun process(constraint: Constraint): Double? {
        if (constraint.default) {
            return null
        }

        constraint.dependencies.forEach { child ->
            if (child !in processed) {
                constraints[child]?.let { layoutChild(child, it) }
            }
        }

        return constraint()
    }

    private fun layoutChild(child: View, constraints: Constraints) {
        if (child in processing) {
            throw Exception("Circular dependency")
        }

        processing += child

        var top    = process(constraints.top    )
        var height = process(constraints.height )
        val middle = process(constraints.centerY)
        val bottom = process(constraints.bottom )

        top = top ?: when {
            middle != null && height != null -> middle - height / 2
            middle != null && bottom != null -> bottom - (bottom - middle) * 2
            height != null && bottom != null -> bottom - height
            middle != null                   -> middle - child.height / 2
            bottom != null                   -> bottom - child.height
            else                             -> child.y
        }

        height = height ?: when {
            middle != null -> (middle - top) * 2
            bottom != null -> bottom - top
            else           -> child.height
        }

        // TODO: Fully handle width/height

        var left   = process(constraints.left   )
        var width  = process(constraints.width  )
        val center = process(constraints.centerX)
        val right  = process(constraints.right  )

        left = left ?: when {
            center != null && width != null -> center - width / 2
            center != null && right != null -> right  - (right - center) * 2
            width  != null && right != null -> right  - width
            center != null                  -> center - child.width / 2
            right  != null                  -> right  - child.width
            else                            -> child.x
        }

        width = width ?: when {
            center != null -> (center - left) * 2
            right  != null -> right - left
            else           -> child.width
        }

        child.bounds = Rectangle(left, top, max(0.0, width), max(0.0, height))

        processing -= child
        processed  += child
    }

    // FIXME: Gracefully handle circular dependencies
    override fun layout(container: PositionableContainer) {
        processed.clear ()
        processing.clear()

        // FIXME: This check is pretty inefficient; but it.key.parent == positionable won't work
        constraints.filter { it.key.parent == (container as? PositionableWrapper?)?.view && it.key !in processed }.forEach { (child, constraints) ->
            layoutChild(child, constraints)
        }
    }

    private fun constraints(view: View, within: Rectangle): Constraints {
        val parent = RectangleConstraints(within)

        return constraints.getOrPut(view) { ConstraintsImpl(view, parent) }
    }

    private fun constraints(child: View, vararg others: View): List<Constraints> {
        child.parent?.let {
            val parent = ParentConstraintsImpl(it)

            val children = arrayOf(child) + others

            val constraints = children.filter { it.parent == parent.target } .map {
                it.parentChange += parentChanged_

                constraints.getOrPut(it) { ConstraintsImpl(it, parent) }
            }

            if (constraints.size != children.size) { throw Exception("Must all share same parent") }

            return constraints
        } ?: if (child.displayed && display != null) {
            val parent = displayConstraints ?: DisplayConstraints(display).also { displayConstraints = it }

            val children = arrayOf(child) + others

            val constraints = children.filter { it.parent == null && it.displayed } .map {
                it.parentChange += parentChanged_

                constraints.getOrPut(it) { ConstraintsImpl(it, parent) }
            }

            if (constraints.size != children.size) { throw Exception("Must all be displayed") }

            return constraints
        } else throw Exception("Must all share same parent")
    }

    private val parentChanged_ = ::parentChanged

    @Suppress("UNUSED_PARAMETER")
    private fun parentChanged(child: View, old: View?, new: View?) {
        constraints.remove(child)

        child.parentChange -= parentChanged_
    }
}

open class Constraint(internal val target: View, dependencies: Set<View> = emptySet(), internal val default: Boolean = true, internal var block: (View) -> Double) {
    internal val dependencies by lazy { mutableSetOf(target) + dependencies }

    internal operator fun invoke() = block(target)
}

class VerticalConstraint(target: View, dependencies: Set<View> = emptySet(), default: Boolean = true, block: (View) -> Double): Constraint(target, dependencies, default, block) {
    operator fun plus(value: Number) = plus { value }

    operator fun plus(value: () -> Number) = VerticalConstraint(target, dependencies) {
        block(it) + value().toDouble()
    }

    operator fun plus(value: MagnitudeConstraint) = VerticalConstraint(target, dependencies + value.dependencies) {
        block(it) + value()
    }

    operator fun plus(value: VerticalConstraint) = MagnitudeConstraint(target, dependencies + value.dependencies) {
        block(it) + value()
    }

    operator fun minus(value: Number) = minus { value }

    operator fun minus(value: () -> Number) = VerticalConstraint(target, dependencies) {
        block(it) - value().toDouble()
    }

    operator fun minus(value: VerticalConstraint) = MagnitudeConstraint(target, dependencies + value.dependencies) {
        block(it) - value()
    }

    operator fun minus(value: MagnitudeConstraint) = VerticalConstraint(target, dependencies + value.dependencies) {
        block(it) - value()
    }

    operator fun times(value: Number) = times { value }

    operator fun times(value: () -> Number) = VerticalConstraint(target, dependencies) {
        block(it) * value().toDouble()
    }

    operator fun div(value: Number) = div { value }

    operator fun div(value: () -> Number) = VerticalConstraint(target, dependencies) {
        block(it) / value().toDouble()
    }

    operator fun times(value: MagnitudeConstraint) = VerticalConstraint(target, dependencies + value.dependencies) {
        block(it) * value()
    }

    operator fun div(value: MagnitudeConstraint) = VerticalConstraint(target, dependencies + value.dependencies) {
        block(it) / value()
    }

//    override fun toString() = "V ($default) <- $dependencies"
}

class HorizontalConstraint(target: View, dependencies: Set<View> = emptySet(), default: Boolean = true, block: (View) -> Double): Constraint(target, dependencies, default, block) {
    operator fun plus(value: Number) = plus { value }

    operator fun plus(value: () -> Number) = HorizontalConstraint(target, dependencies) {
        block(it) + value().toDouble()
    }

    operator fun plus(value: MagnitudeConstraint) = HorizontalConstraint(target, dependencies + value.dependencies) {
        block(it) + value()
    }

    operator fun plus(value: HorizontalConstraint) = MagnitudeConstraint(target, dependencies + value.dependencies) {
        block(it) + value()
    }

    operator fun minus(value: Number) = minus { value }

    operator fun minus(value: () -> Number) = HorizontalConstraint(target, dependencies) {
        block(it) - value().toDouble()
    }

    operator fun minus(value: MagnitudeConstraint) = HorizontalConstraint(target, dependencies + value.dependencies) {
        block(it) - value()
    }

    operator fun minus(value: HorizontalConstraint) = MagnitudeConstraint(target, dependencies + value.dependencies) {
        block(it) - value()
    }

    operator fun times(value: Number) = times { value }

    operator fun times(value: () -> Number) = HorizontalConstraint(target, dependencies) {
        block(it) * value().toDouble()
    }

    operator fun div(value: Number) = div { value }

    operator fun div(value: () -> Number) = HorizontalConstraint(target, dependencies) {
        block(it) / value().toDouble()
    }

    operator fun times(value: MagnitudeConstraint) = HorizontalConstraint(target, dependencies + value.dependencies) {
        block(it) * value()
    }

    operator fun div(value: MagnitudeConstraint) = HorizontalConstraint(target, dependencies + value.dependencies) {
        block(it) / value()
    }

//    override fun toString() = "H ($default) <- $dependencies"
}

private object IgnoreTarget: View()

fun constant(value: Double) = MagnitudeConstraint(IgnoreTarget, block = { value })

interface Nullable<T: Constraint> {
    infix fun or(other: T): T = other
}

private open class NullableMagnitudeConstraint(private val target: View, private val dependencies: Set<View> = emptySet(), private val default: Boolean = true, private val optionalBlock: (View) -> Double?): Nullable<MagnitudeConstraint> {
    override infix fun or(other: MagnitudeConstraint): MagnitudeConstraint = MagnitudeConstraint(target, dependencies, default) {
        optionalBlock(it) ?: other()
    }
}

open class MagnitudeConstraint(target: View, dependencies: Set<View> = emptySet(), default: Boolean = true, block: (View) -> Double): Constraint(target, dependencies, default, block) {
    operator fun plus(value: Number) = plus { value }

    operator fun plus(value: () -> Number) = MagnitudeConstraint(target, dependencies) {
        block(it) + value().toDouble()
    }

    operator fun plus(value: MagnitudeConstraint) = MagnitudeConstraint(target, dependencies + value.dependencies){
        block(it) + value()
    }

    operator fun minus(value: Number) = minus { value }

    operator fun minus(value: () -> Number) = MagnitudeConstraint(target, dependencies) {
        block(it) - value().toDouble()
    }

    operator fun minus(value: MagnitudeConstraint) = MagnitudeConstraint(target, dependencies + value.dependencies){
        block(it) - value()
    }

    operator fun times(value: Number) = times { value }

    operator fun times(value: () -> Number) = MagnitudeConstraint(target, dependencies) {
        block(it) * value().toDouble()
    }

    operator fun times(value: MagnitudeConstraint) = MagnitudeConstraint(target, dependencies + value.dependencies){
        block(it) * value()
    }

    operator fun div(value: Number) = div { value }

    operator fun div(value: () -> Number) = MagnitudeConstraint(target, dependencies) {
        block(it) / value().toDouble()
    }

    operator fun div(value: MagnitudeConstraint) = MagnitudeConstraint(target, dependencies + value.dependencies){
        block(it) / value()
    }

//    override fun toString() = "H ($default) <- $dependencies"
}

interface ParentConstraints {
    val top        : VerticalConstraint
    val centerY    : VerticalConstraint
    val bottom     : VerticalConstraint
    val height     : MagnitudeConstraint
    val minHeight  : MagnitudeConstraint get() = constant(0.0)
    val idealHeight: Nullable<MagnitudeConstraint> get() = object: Nullable<MagnitudeConstraint> {}

    val left      : HorizontalConstraint
    val centerX   : HorizontalConstraint
    val right     : HorizontalConstraint
    val width     : MagnitudeConstraint
    val minWidth  : MagnitudeConstraint get() = constant(0.0)
    val idealWidth: Nullable<MagnitudeConstraint> get() = object: Nullable<MagnitudeConstraint> {}

    val center: Pair<HorizontalConstraint, VerticalConstraint> get() = centerX to centerY
}

interface Constraints: ParentConstraints {
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

    val parent: ParentConstraints
}

private open class ParentConstraintsImpl(val target: View): ParentConstraints {
    override val top       = VerticalConstraint (target      ) { 0.0                       }
    override val centerY   = VerticalConstraint (target      ) { it.height / 2             }
    override val bottom    = VerticalConstraint (target      ) { it.height                 }
    override val height    = MagnitudeConstraint(target      ) { it.height                 }
    override val minHeight = MagnitudeConstraint(IgnoreTarget) { target.minimumSize.height }

    override val left     = HorizontalConstraint(target      ) { 0.0                      }
    override val centerX  = HorizontalConstraint(target      ) { it.width  / 2            }
    override val right    = HorizontalConstraint(target      ) { it.width                 }
    override val width    = MagnitudeConstraint (target      ) { it.width                 }
    override val minWidth = MagnitudeConstraint (IgnoreTarget) { target.minimumSize.width }
}

private open class DisplayConstraints(private val display: Display): ParentConstraints {
    val target = object: View() {}

    init {
        display.sizeChanged += { _,_,new ->
            target.size = new
        }
    }

    override val top       = VerticalConstraint  (target) { 0.0                     }
    override val left      = HorizontalConstraint(target) { 0.0                     }
    override val centerY   = VerticalConstraint  (target) { display.size.height / 2 }
    override val centerX   = HorizontalConstraint(target) { display.size.width  / 2 }
    override val right     = HorizontalConstraint(target) { display.size.width      }
    override val bottom    = VerticalConstraint  (target) { display.size.height     }
    override val width     = MagnitudeConstraint (target) { display.size.width      }
    override val height    = MagnitudeConstraint (target) { display.size.height     }

//    override fun toString() = "D $target -> top: $top, left: $left, centerX: $centerX, centerY: $centerY, right: $right, bottom: $bottom"
}

private open class RectangleConstraints(private val rectangle: Rectangle): ParentConstraints {
    val target = object: View() {}

    override val top     = VerticalConstraint  (target) { rectangle.y        }
    override val centerY = VerticalConstraint  (target) { rectangle.center.y }
    override val bottom  = VerticalConstraint  (target) { rectangle.bottom   }
    override val height  = MagnitudeConstraint (target) { rectangle.height   }

    override val left    = HorizontalConstraint(target) { rectangle.x        }
    override val centerX = HorizontalConstraint(target) { rectangle.center.x }
    override val right   = HorizontalConstraint(target) { rectangle.right    }
    override val width   = MagnitudeConstraint (target) { rectangle.width    }
}

private class ConstraintsImpl(target: View, override val parent: ParentConstraints): ParentConstraintsImpl(target), Constraints {
    override var top = VerticalConstraint(target) { it.y }
        set(new) { field = VerticalConstraint(new.target, new.dependencies, false, new.block) }

    override var left = HorizontalConstraint(target) { it.x }
        set(new) { field = HorizontalConstraint(new.target, new.dependencies, false, new.block) }

    override var centerY = VerticalConstraint(target) { top() + it.height / 2 }
        set(new) { field = VerticalConstraint(new.target, new.dependencies, false, new.block) }

    override var centerX = HorizontalConstraint(target) { left() + it.width / 2 }
        set(new) { field = HorizontalConstraint(new.target, new.dependencies, false, new.block) }

    override var right = HorizontalConstraint(target) { left() + it.width }
        set(new) { field = HorizontalConstraint(new.target, new.dependencies, false, new.block) }

    override var bottom = VerticalConstraint(target) { top() + it.height }
        set(new) { field = VerticalConstraint(new.target, new.dependencies, false, new.block) }

    override var width = MagnitudeConstraint(target) { it.width }
        set(new) { field = MagnitudeConstraint(new.target, new.dependencies, false, new.block) }

    override var height = MagnitudeConstraint(target) { it.height }
        set(new) { field = MagnitudeConstraint(new.target, new.dependencies, false, new.block) }

    override val idealWidth  = NullableMagnitudeConstraint(IgnoreTarget) { target.idealSize?.width  }
    override val idealHeight = NullableMagnitudeConstraint(IgnoreTarget) { target.idealSize?.height }

    override val minWidth  = MagnitudeConstraint(IgnoreTarget) { target.minimumSize.width  }
    override val minHeight = MagnitudeConstraint(IgnoreTarget) { target.minimumSize.height }

//    override fun toString() = "C $target -> top: $top, left: $left, centerX: $centerX, centerY: $centerY, right: $right, bottom: $bottom"
}

fun constrain(view: View, within: Rectangle, block: (Constraints) -> Unit) {
    ConstraintLayoutImpl().let { layout ->
        layout.constrain(view, within) { block(it) }
    }
}

interface ConstraintBlockContext {
    val parent: ParentConstraints
}

private class ConstraintBlockContextImpl(override val parent: ParentConstraints): ConstraintBlockContext

fun constrain(a: View,                                     block: ConstraintBlockContext.(Constraints                                                    ) -> Unit): ConstraintLayout = ConstraintLayoutImpl().also { it.constrain(a,             block) }
fun constrain(a: View, b: View,                            block: ConstraintBlockContext.(Constraints, Constraints                                       ) -> Unit): ConstraintLayout = ConstraintLayoutImpl().also { it.constrain(a, b,          block) }
fun constrain(a: View, b: View, c: View,                   block: ConstraintBlockContext.(Constraints, Constraints, Constraints                          ) -> Unit): ConstraintLayout = ConstraintLayoutImpl().also { it.constrain(a, b, c,       block) }
fun constrain(a: View, b: View, c: View, d: View,          block: ConstraintBlockContext.(Constraints, Constraints, Constraints, Constraints             ) -> Unit): ConstraintLayout = ConstraintLayoutImpl().also { it.constrain(a, b, c, d,    block) }
fun constrain(a: View, b: View, c: View, d: View, e: View, block: ConstraintBlockContext.(Constraints, Constraints, Constraints, Constraints, Constraints) -> Unit): ConstraintLayout = ConstraintLayoutImpl().also { it.constrain(a, b, c, d, e, block) }

fun Display.constrain(a: View,                                     block: ConstraintBlockContext.(Constraints                                                    ) -> Unit): ConstraintLayout = ConstraintLayoutImpl(this).also { it.constrain(a,             block) }
fun Display.constrain(a: View, b: View,                            block: ConstraintBlockContext.(Constraints, Constraints                                       ) -> Unit): ConstraintLayout = ConstraintLayoutImpl(this).also { it.constrain(a, b,          block) }
fun Display.constrain(a: View, b: View, c: View,                   block: ConstraintBlockContext.(Constraints, Constraints, Constraints                          ) -> Unit): ConstraintLayout = ConstraintLayoutImpl(this).also { it.constrain(a, b, c,       block) }
fun Display.constrain(a: View, b: View, c: View, d: View,          block: ConstraintBlockContext.(Constraints, Constraints, Constraints, Constraints             ) -> Unit): ConstraintLayout = ConstraintLayoutImpl(this).also { it.constrain(a, b, c, d,    block) }
fun Display.constrain(a: View, b: View, c: View, d: View, e: View, block: ConstraintBlockContext.(Constraints, Constraints, Constraints, Constraints, Constraints) -> Unit): ConstraintLayout = ConstraintLayoutImpl(this).also { it.constrain(a, b, c, d, e, block) }

fun max(a: Double, b: HorizontalConstraint) = HorizontalConstraint(b.target, b.dependencies, false) { max(a, b.invoke()) }
inline fun max(a: HorizontalConstraint, b: Double) = max(b, a)

fun max(a: HorizontalConstraint, b: HorizontalConstraint) = HorizontalConstraint(b.target, b.dependencies, false) { max(a.invoke(), b.invoke()) }

fun min(a: Double, b: HorizontalConstraint) = HorizontalConstraint(b.target, b.dependencies, false) { min(a, b.invoke()) }
inline fun min(a: HorizontalConstraint, b: Double) = min(b, a)

fun min(a: HorizontalConstraint, b: HorizontalConstraint) = HorizontalConstraint(b.target, b.dependencies, false) { min(a.invoke(), b.invoke()) }

fun max(a: Double, b: VerticalConstraint) = VerticalConstraint(b.target, b.dependencies, false) { max(a, b.invoke()) }
inline fun max(a: VerticalConstraint, b: Double) = max(b, a)

fun max(a: VerticalConstraint, b: VerticalConstraint) = VerticalConstraint(b.target, b.dependencies, false) { max(a.invoke(), b.invoke()) }

fun min(a: Double, b: VerticalConstraint) = VerticalConstraint(b.target, b.dependencies, false) { min(a, b.invoke()) }
inline fun min(a: VerticalConstraint, b: Double) = min(b, a)

fun min(a: VerticalConstraint, b: VerticalConstraint) = VerticalConstraint(b.target, b.dependencies, false) { min(a.invoke(), b.invoke()) }