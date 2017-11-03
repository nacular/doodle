package com.nectar.doodle.layout

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Layout
import com.nectar.doodle.utils.observable

/**
 * Created by Nicholas Eddy on 11/1/17.
 */
interface ConstraintLayout: Layout {
    fun constrain(a: Gizmo, b: Gizmo, block: (Constraints, Constraints) -> Unit): ConstraintLayout
    fun constrain(a: Gizmo, b: Gizmo, c: Gizmo, block: (Constraints, Constraints, Constraints) -> Unit): ConstraintLayout
}

private class ConstraintLayoutImpl(vararg constraints: ConstraintsImpl): ConstraintLayout {
    val constraints = constraints.fold(mutableMapOf<Gizmo, ConstraintsImpl>()) { s, r -> s[r.target] = r; s }

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

    // FIXME: Gracefully handle circular dependencies
    override fun layout(gizmo: Gizmo) {
        constraints.filter { it.key.parent == gizmo }.forEach { (child, constraint) ->
            var top    = if (!constraint.top.default    ) constraint.top    () else null
            val middle = if (!constraint.centerY.default) constraint.centerY() else null
            val bottom = if (!constraint.bottom.default ) constraint.bottom () else null

            var height = child.height

            when {
                top    != null && middle != null && bottom != null -> {}
                top    != null && middle != null                   -> { height = (middle - top) * 2 }
                top    != null && bottom != null                   -> { height = (bottom - top)     }
                middle != null && bottom != null                   -> { top    =  bottom - (bottom - middle) * 2 }
                middle != null                                     -> { top    =  middle - height / 2}
                bottom != null                                     -> { top    =  bottom - height }
                else                                               -> { top    =  child.y }
            }

            var left   = if (!constraint.left.default   ) constraint.left   () else null
            val center = if (!constraint.centerX.default) constraint.centerX() else null
            val right  = if (!constraint.right.default  ) constraint.right  () else null

            var width = child.width

            when {
                left   != null && center != null && right != null -> {}
                left   != null && center != null                  -> { width = (center - left) * 2 }
                left   != null && right != null                   -> { width = (right  - left)     }
                center != null && right != null                   -> { left  =  right  - (right - center) * 2 }
                center != null                                    -> { left  =  center - width / 2}
                right  != null                                    -> { left  =  right  - width }
                else                                              -> { left  =  child.x  }
            }

            child.x      = left
            child.y      = top
            child.width  = width
            child.height = height
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

open class Constraint(val target: Gizmo, protected var block: (Gizmo) -> Double) {
    var default = true

    operator fun invoke() = block(target)
}

class VerticalConstraint(target: Gizmo, block: (Gizmo) -> Double): Constraint(target, block) {
    operator fun plus(value: Number) = VerticalConstraint(target) {
        block(it) + value.toDouble()
    }

    operator fun minus(value: Number) = VerticalConstraint(target) {
        block(it) - value.toDouble()
    }
}

class HorizontalConstraint(target: Gizmo, block: (Gizmo) -> Double): Constraint(target, block) {
    operator fun plus(value: Number) = HorizontalConstraint(target) {
        block(it) + value.toDouble()
    }

    operator fun minus(value: Number) = HorizontalConstraint(target) {
        block(it) - value.toDouble()
    }
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
}

private class ConstraintsImpl(target: Gizmo, override val parent: ParentConstraints): ParentConstraintsImpl(target), Constraints {
    override var top     by observable(VerticalConstraint  (target) { it.y                 }) { _,_,new -> new.default = false }
    override var left    by observable(HorizontalConstraint(target) { it.x                 }) { _,_,new -> new.default = false }
    override var centerY by observable(VerticalConstraint  (target) { it.y + it.height / 2 }) { _,_,new -> new.default = false }
    override var centerX by observable(HorizontalConstraint(target) { it.x + it.width  / 2 }) { _,_,new -> new.default = false }
    override var right   by observable(HorizontalConstraint(target) { it.x + it.width      }) { _,_,new -> new.default = false }
    override var bottom  by observable(VerticalConstraint  (target) { it.y + it.height     }) { _,_,new -> new.default = false }
}

fun constrain(a: Gizmo, b: Gizmo, block: (Constraints, Constraints) -> Unit): ConstraintLayout = ConstraintLayoutImpl().also { it.constrain(a, b, block) }
fun constrain(a: Gizmo, b: Gizmo, c: Gizmo, block: (Constraints, Constraints, Constraints) -> Unit): ConstraintLayout = ConstraintLayoutImpl().also { it.constrain(a, b, c, block) }