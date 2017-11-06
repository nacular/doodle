package com.nectar.doodle.layout

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Layout
import com.nectar.doodle.geometry.Rectangle
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 11/1/17.
 */
interface ConstraintLayout: Layout {
    fun constrain(a: Gizmo, block: (Constraints) -> Unit): ConstraintLayout
    fun constrain(a: Gizmo, b: Gizmo, block: (Constraints, Constraints) -> Unit): ConstraintLayout
    fun constrain(a: Gizmo, b: Gizmo, c: Gizmo, block: (Constraints, Constraints, Constraints) -> Unit): ConstraintLayout
}

private class ConstraintLayoutImpl(vararg constraints: ConstraintsImpl): ConstraintLayout {
    private val constraints = constraints.fold(mutableMapOf<Gizmo, ConstraintsImpl>()) { s, r -> s[r.target] = r; s }
    private val processed   = mutableSetOf<Gizmo>()
    private val processing  = mutableSetOf<Gizmo>()

    override fun constrain(a: Gizmo, block: (Constraints) -> Unit): ConstraintLayout {
        constraints(a).let { (a) -> block(a)
            return this
        }
    }

    override fun constrain(a: Gizmo, b: Gizmo, block: (Constraints, Constraints) -> Unit): ConstraintLayout {
        constraints(a, b).let { (a, b) -> block(a, b)
            return this
        }
    }

    override fun constrain(a: Gizmo, b: Gizmo, c: Gizmo, block: (Constraints, Constraints, Constraints) -> Unit): ConstraintLayout {
        constraints(a, b, c).let { (a, b, c) -> block(a, b, c)
            return this
        }
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

    private fun layoutChild(child: Gizmo, constraints: Constraints) {
        if (child in processing) {
            throw Exception("Circular dependency")
        }

        processing += child

        var top    = process(constraints.top    )
        val middle = process(constraints.centerY)
        val bottom = process(constraints.bottom )

        var height = child.height

        when {
            top    != null && middle != null && bottom != null -> {} // TODO: HANDLE
            top    != null && middle != null                   -> { height = (middle - top) * 2 }
            top    != null && bottom != null                   -> { height = (bottom - top)     }
            top    != null                                     -> {}
            middle != null && bottom != null                   -> { top    =  bottom - (bottom - middle) * 2 }
            middle != null                                     -> { top    =  middle - height / 2}
            bottom != null                                     -> { top    =  bottom - height }
            else                                               -> { top    =  child.y }
        }

        var left   = process(constraints.left   )
        val center = process(constraints.centerX)
        val right  = process(constraints.right  )

        var width = child.width

        when {
            left   != null && center != null && right != null -> {} // TODO: HANDLE
            left   != null && center != null                  -> { width = (center - left) * 2 }
            left   != null && right != null                   -> { width = (right  - left)     }
            left   != null                                    -> {}
            center != null && right != null                   -> { left  =  right  - (right - center) * 2 }
            center != null                                    -> { left  =  center - width / 2}
            right  != null                                    -> { left  =  right  - width }
            else                                              -> { left  =  child.x  }
        }

        width  = max(0.0,  width)
        height = max(0.0, height)

        child.bounds = Rectangle(left, top, width, height)

        processing -= child
        processed  += child
    }

    // FIXME: Gracefully handle circular dependencies
    override fun layout(gizmo: Gizmo) {
        processed.clear ()
        processing.clear()

        constraints.filter { it.key.parent == gizmo }.forEach { (child, constraints) ->
            layoutChild(child, constraints)
        }
    }

    private fun constraints(child: Gizmo, vararg others: Gizmo): List<Constraints> {
        child.parent?.let {
            val parent = ParentConstraintsImpl(it)

            val children = arrayOf(child) + others

            val constraints = children.filter { it.parent == parent.target } .map {
                it.parentChange += this::parentChanged

                constraints.getOrPut(it) { ConstraintsImpl(it, parent) }
            }

            if (constraints.size != children.size) { throw Exception("Must all share same parent") }

            return constraints
        } ?: throw Exception("Must all share same parent")
    }

    private fun parentChanged(child: Gizmo, old: Gizmo?, new: Gizmo?) {
        constraints.remove(child)

        child.parentChange -= this::parentChanged
    }
}

open class Constraint(internal val target: Gizmo, internal val default: Boolean = true, internal var block: (Gizmo) -> Double) {
    internal val dependencies = mutableSetOf(target)

    internal operator fun invoke() = block(target)
}

class VerticalConstraint(target: Gizmo, default: Boolean = true, block: (Gizmo) -> Double): Constraint(target, default, block) {
    operator fun plus(value: Number) = VerticalConstraint(target) {
        block(it) + value.toDouble()
    }

    operator fun minus(value: Number) = VerticalConstraint(target) {
        block(it) - value.toDouble()
    }

//    override fun toString() = "V ($default) <- $dependencies"
}

class HorizontalConstraint(target: Gizmo, default: Boolean = true, block: (Gizmo) -> Double): Constraint(target, default, block) {
    operator fun plus(value: Number) = HorizontalConstraint(target) {
        block(it) + value.toDouble()
    }

    operator fun minus(value: Number) = HorizontalConstraint(target) {
        block(it) - value.toDouble()
    }

//    override fun toString() = "H ($default) <- $dependencies"
}

interface ParentConstraints {
    val top    : VerticalConstraint
    val centerY: VerticalConstraint
    val bottom : VerticalConstraint

    val left   : HorizontalConstraint
    val centerX: HorizontalConstraint
    val right  : HorizontalConstraint
}

interface Constraints: ParentConstraints {
    override var top    : VerticalConstraint
    override var centerY: VerticalConstraint
    override var bottom : VerticalConstraint

    override var left   : HorizontalConstraint
    override var centerX: HorizontalConstraint
    override var right  : HorizontalConstraint

    val parent: ParentConstraints
}

private open class ParentConstraintsImpl(val target: Gizmo): ParentConstraints {
    override val top     = VerticalConstraint  (target) { 0.0           }
    override val left    = HorizontalConstraint(target) { 0.0           }
    override val centerY = VerticalConstraint  (target) { it.height / 2 }
    override val centerX = HorizontalConstraint(target) { it.width  / 2 }
    override val right   = HorizontalConstraint(target) { it.width      }
    override val bottom  = VerticalConstraint  (target) { it.height     }

//    override fun toString() = "P $target -> top: $top, left: $left, centerX: $centerX, centerY: $centerY, right: $right, bottom: $bottom"
}

private class ConstraintsImpl(target: Gizmo, override val parent: ParentConstraints): ParentConstraintsImpl(target), Constraints {
    override var top = VerticalConstraint(target) { it.y }
        set(new) { field = VerticalConstraint(new.target, false, new.block) }

    override var left = HorizontalConstraint(target) { it.x }
        set(new) { field = HorizontalConstraint(new.target, false, new.block) }

    override var centerY = VerticalConstraint(target) { top () + it.height / 2 }
        set(new) { field = VerticalConstraint(new.target, false, new.block) }

    override var centerX = HorizontalConstraint(target) { left() + it.width  / 2 }
        set(new) { field = HorizontalConstraint(new.target, false, new.block) }

    override var right = HorizontalConstraint(target) { left() + it.width }
        set(new) { field = HorizontalConstraint(new.target, false, new.block) }

    override var bottom = VerticalConstraint(target) { top () + it.height }
        set(new) { field = VerticalConstraint(new.target, false, new.block) }

//    override fun toString() = "C $target -> top: $top, left: $left, centerX: $centerX, centerY: $centerY, right: $right, bottom: $bottom"
}

fun constrain(a: Gizmo, block: (Constraints) -> Unit): ConstraintLayout = ConstraintLayoutImpl().also { it.constrain(a, block) }
fun constrain(a: Gizmo, b: Gizmo, block: (Constraints, Constraints) -> Unit): ConstraintLayout = ConstraintLayoutImpl().also { it.constrain(a, b, block) }
fun constrain(a: Gizmo, b: Gizmo, c: Gizmo, block: (Constraints, Constraints, Constraints) -> Unit): ConstraintLayout = ConstraintLayoutImpl().also { it.constrain(a, b, c, block) }