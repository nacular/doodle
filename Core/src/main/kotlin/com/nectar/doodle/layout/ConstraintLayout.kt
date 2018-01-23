package com.nectar.doodle.layout

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.geometry.Rectangle
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 11/1/17.
 */
abstract class ConstraintLayout: Layout() {
    abstract fun constrain(a: Gizmo, block: (Constraints) -> Unit): ConstraintLayout
    abstract fun constrain(a: Gizmo, b: Gizmo, block: (Constraints, Constraints) -> Unit): ConstraintLayout
    abstract fun constrain(a: Gizmo, b: Gizmo, c: Gizmo, block: (Constraints, Constraints, Constraints) -> Unit): ConstraintLayout
    abstract fun constrain(a: Gizmo, b: Gizmo, c: Gizmo, d: Gizmo, block: (Constraints, Constraints, Constraints, Constraints) -> Unit): ConstraintLayout
}

private class ConstraintLayoutImpl(vararg constraints: ConstraintsImpl): ConstraintLayout() {
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

    override fun constrain(a: Gizmo, b: Gizmo, c: Gizmo, d: Gizmo, block: (Constraints, Constraints, Constraints, Constraints) -> Unit): ConstraintLayout {
        constraints(a, b, c, d).let { (a, b, c, d) -> block(a, b, c, d)
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

        // FIXME: Handle width and height
//        val height = process(constraints.height )
//        val width  = process(constraints.width)

        var rawHeight = child.height

        when {
            top    != null && middle != null && bottom != null -> {} // TODO: HANDLE
            top    != null && middle != null                   -> { rawHeight = (middle - top) * 2 }
            top    != null && bottom != null                   -> { rawHeight = (bottom - top)     }
            top    != null                                     -> {}
            middle != null && bottom != null                   -> { top    =  bottom - (bottom - middle) * 2 }
            middle != null                                     -> { top    =  middle - rawHeight / 2}
            bottom != null                                     -> { top    =  bottom - rawHeight }
            else                                               -> { top    =  child.y }
        }

        var left   = process(constraints.left   )
        val center = process(constraints.centerX)
        val right  = process(constraints.right  )

        var rawWidth = child.width

        when {
            left   != null && center != null && right != null -> {} // TODO: HANDLE
            left   != null && center != null                  -> { rawWidth = (center - left) * 2 }
            left   != null && right  != null                  -> { rawWidth = (right  - left)     }
            left   != null                                    -> {}
            center != null && right  != null                  -> { left  =  right  - (right - center) * 2 }
            center != null                                    -> { left  =  center - rawWidth / 2}
            right  != null                                    -> { left  =  right  - rawWidth }
            else                                              -> { left  =  child.x  }
        }

        rawWidth  = max(0.0,  rawWidth)
        rawHeight = max(0.0, rawHeight)

        child.bounds = Rectangle(left, top, rawWidth, rawHeight)

        processing -= child
        processed  += child
    }

    // FIXME: Gracefully handle circular dependencies
    override fun layout(positionable: Positionable) {
        processed.clear ()
        processing.clear()

        // FIXME: This check is pretty inefficient; but it.key.parent == positionable won't work
        constraints.filter { positionable.children.contains(it.key) }.forEach { (child, constraints) ->
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

    operator fun plus(value: VerticalConstraint) = MagnitudeConstraint(target) {
        block(it) + value()
    }

    operator fun minus(value: Number) = VerticalConstraint(target) {
        block(it) - value.toDouble()
    }

    operator fun minus(value: VerticalConstraint) = MagnitudeConstraint(target) {
        block(it) - value()
    }

    operator fun times(value: Number) = VerticalConstraint(target) {
        block(it) * value.toDouble()
    }

    operator fun div(value: Number) = VerticalConstraint(target) {
        block(it) / value.toDouble()
    }

//    override fun toString() = "V ($default) <- $dependencies"
}

class HorizontalConstraint(target: Gizmo, default: Boolean = true, block: (Gizmo) -> Double): Constraint(target, default, block) {
    operator fun plus(value: Number) = HorizontalConstraint(target) {
        block(it) + value.toDouble()
    }

    operator fun plus(value: HorizontalConstraint) = MagnitudeConstraint(target) {
        block(it) + value()
    }

    operator fun minus(value: Number) = HorizontalConstraint(target) {
        block(it) - value.toDouble()
    }

    operator fun minus(value: HorizontalConstraint) = MagnitudeConstraint(target) {
        block(it) - value()
    }

    operator fun times(value: Number) = HorizontalConstraint(target) {
        block(it) * value.toDouble()
    }

    operator fun div(value: Number) = HorizontalConstraint(target) {
        block(it) / value.toDouble()
    }

//    override fun toString() = "H ($default) <- $dependencies"
}

class MagnitudeConstraint(target: Gizmo, default: Boolean = true, block: (Gizmo) -> Double): Constraint(target, default, block) {
    operator fun plus(value: Number) = MagnitudeConstraint(target) {
        block(it) + value.toDouble()
    }

    operator fun minus(value: Number) = MagnitudeConstraint(target) {
        block(it) - value.toDouble()
    }

    operator fun times(value: Number) = MagnitudeConstraint(target) {
        block(it) * value.toDouble()
    }

    operator fun div(value: Number) = MagnitudeConstraint(target) {
        block(it) / value.toDouble()
    }

//    override fun toString() = "H ($default) <- $dependencies"
}

interface ParentConstraints {
    val top    : VerticalConstraint
    val centerY: VerticalConstraint
    val bottom : VerticalConstraint
    val height : MagnitudeConstraint

    val left   : HorizontalConstraint
    val centerX: HorizontalConstraint
    val right  : HorizontalConstraint
    val width  : MagnitudeConstraint
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

    val parent: ParentConstraints
}

private open class ParentConstraintsImpl(val target: Gizmo): ParentConstraints {
    override val top     = VerticalConstraint  (target) { 0.0           }
    override val left    = HorizontalConstraint(target) { 0.0           }
    override val centerY = VerticalConstraint  (target) { it.height / 2 }
    override val centerX = HorizontalConstraint(target) { it.width  / 2 }
    override val right   = HorizontalConstraint(target) { it.width      }
    override val bottom  = VerticalConstraint  (target) { it.height     }
    override val width   = MagnitudeConstraint (target) { it.width      }
    override val height  = MagnitudeConstraint (target) { it.height     }

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

    override var width = MagnitudeConstraint(target) { it.width }
        set(new) { field = MagnitudeConstraint(new.target, false, new.block) }

    override var height = MagnitudeConstraint(target) { it.height }
        set(new) { field = MagnitudeConstraint(new.target, false, new.block) }

//    override fun toString() = "C $target -> top: $top, left: $left, centerX: $centerX, centerY: $centerY, right: $right, bottom: $bottom"
}

fun constrain(a: Gizmo, block: (Constraints) -> Unit): ConstraintLayout = ConstraintLayoutImpl().also { it.constrain(a, block) }
fun constrain(a: Gizmo, b: Gizmo, block: (Constraints, Constraints) -> Unit): ConstraintLayout = ConstraintLayoutImpl().also { it.constrain(a, b, block) }
fun constrain(a: Gizmo, b: Gizmo, c: Gizmo, block: (Constraints, Constraints, Constraints) -> Unit): ConstraintLayout = ConstraintLayoutImpl().also { it.constrain(a, b, c, block) }
fun constrain(a: Gizmo, b: Gizmo, c: Gizmo, d: Gizmo, block: (Constraints, Constraints, Constraints, Constraints) -> Unit): ConstraintLayout = ConstraintLayoutImpl().also { it.constrain(a, b, c, d, block) }