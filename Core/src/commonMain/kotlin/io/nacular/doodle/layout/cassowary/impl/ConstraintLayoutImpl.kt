package io.nacular.doodle.layout.cassowary.impl

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.height
import io.nacular.doodle.core.width
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.cassowary.Constraint
import io.nacular.doodle.layout.cassowary.ConstraintDslContext
import io.nacular.doodle.layout.cassowary.ConstraintLayout
import io.nacular.doodle.layout.cassowary.Constraints
import io.nacular.doodle.layout.cassowary.DuplicateEditVariableException
import io.nacular.doodle.layout.cassowary.Edges
import io.nacular.doodle.layout.cassowary.Expression
import io.nacular.doodle.layout.cassowary.Operator
import io.nacular.doodle.layout.cassowary.ParentConstraints
import io.nacular.doodle.layout.cassowary.Position
import io.nacular.doodle.layout.cassowary.Property
import io.nacular.doodle.layout.cassowary.Solver
import io.nacular.doodle.layout.cassowary.Strength
import io.nacular.doodle.layout.cassowary.Strength.Companion.Required
import io.nacular.doodle.layout.cassowary.Strength.Companion.Strong
import io.nacular.doodle.layout.cassowary.Term
import io.nacular.doodle.layout.cassowary.Variable
import kotlin.math.max
import kotlin.reflect.KMutableProperty0

internal class ConstraintLayoutImpl(view: View, vararg others: View, block: ConstraintDslContext.(List<Constraints>) -> Unit): ConstraintLayout() {
    private val commonParent     : View?
    private val parentConstraints: ParentConstraints

    @Suppress("PrivatePropertyName")
    private val parentChanged_ = ::parentChanged

    @Suppress("PrivatePropertyName")
    private val boundsChanged_ = ::boundsChanged

    @Suppress("UNUSED_PARAMETER")
    private fun parentChanged(child: View, old: View?, new: View?) {
        child.parentChange  -= parentChanged_
        child.boundsChanged -= boundsChanged_
    }

    private val updatedBounds = mutableSetOf<View>()

    @Suppress("UNUSED_PARAMETER")
    private fun boundsChanged(child: View, old: Rectangle, new: Rectangle) {
        updatedBounds += child
    }

    private val context: ConstraintDslContext

    private val solver          = Solver()
    private val syntheticLimits = mutableSetOf<Constraint>()

    init {
        context = ConstraintDslContext().apply {
            parent = (view.parent?.let  { ParentConstraintsImpl(it, this) } ?:
                      view.display?.let { DisplayConstraints   (it, this) } ?:
                      throw Exception("Must all share same parent")).also {
                parentConstraints = it
            }
        }

        commonParent = view.parent

        constrain(commonParent, view, others = others, block)
    }

    override fun layout(container: PositionableContainer) {
        solve(solver, parentConstraints, updatedBounds)
    }

    override fun constrain(a: View, block: ConstraintDslContext.(Constraints) -> Unit): ConstraintLayout {
        constrain(commonParent, a, others = emptyArray()) { (a) ->
            block(a)
        }

        return this
    }

    override fun constrain(a: View, b: View, vararg others: View, block: ConstraintDslContext.(List<Constraints>) -> Unit): ConstraintLayout {
        constrain(commonParent, a, others = others) {
            block(it)
        }

        return this
    }

    override fun unconstrain(view: View): ConstraintLayout {
        solver.variables.filterIsInstance<ReflectionVariable>().filter { it.target == view }.forEach {
            solver.remove(it)
        }

        return this
    }

    override fun add(constraint: Constraint, vararg others: Constraint) = add(listOf(constraint, *others))

    override fun add(constraints: Iterable<Constraint>): ConstraintLayout {
        constraints.forEach {
            try {
                solver.addConstraint(it)
            } catch (ignored: Exception) {

            }
        }

        return this
    }

    override fun remove(constraint: Constraint, vararg others: Constraint): ConstraintLayout = remove(listOf(constraint, *others))

    override fun remove(constraints: Iterable<Constraint>): ConstraintLayout {
        constraints.forEach { solver.removeConstraint(it) }

        // Remove synthetic constraints that are no longer relevant
        solver.variableToConstraints.values.filter { it.size == 1 && it.first() in syntheticLimits }.flatten().forEach {
            solver.removeConstraint(it)
        }

        return this
    }

    private fun constrain(parent: View?, child: View, vararg others: View, block: ConstraintDslContext.(List<Constraints>) -> Unit) {
        val constraints = parent?.let { commonParent ->
            val children = arrayOf(child) + others

            val constraints = children.filter { it.parent == commonParent }.map {
                it.parentChange  += parentChanged_
                it.boundsChanged += boundsChanged_

                if (it.position != Point.Origin || it.size != Size.Empty) {
                    updatedBounds += it
                }

                ConstraintsImpl(it, context)
            }

            if (constraints.size != children.size) {
                throw Exception("Must all share same parent")
            }

            return@let constraints
        } ?: child.display?.let { _ ->
            val children = arrayOf(child) + others

            val constraints = children.filter { it.parent == null && it.displayed }.map {
                it.parentChange  += parentChanged_
                it.boundsChanged += boundsChanged_

                if (it.position != Point.Origin || it.size != Size.Empty) {
                    updatedBounds += it
                }

                ConstraintsImpl(it, context)
            }

            if (constraints.size != children.size) {
                throw Exception("Must all be displayed")
            }

            return@let constraints
        } ?: throw Exception("Must all share same parent")

        setupSolver(solver, syntheticLimits, context, constraints, block)
    }

    companion object {
        fun setupSolver(solver         : Solver,
            syntheticLimits: MutableSet<Constraint>,
            context        : ConstraintDslContext,
            constraints    : List<Constraints>,
            block          : ConstraintDslContext.(List<Constraints>) -> Unit) {

            block(context, constraints)

            context.constraints.forEach {
                solver.addConstraint(it)
            }

            solver.variables.filter { it.name == "width" || it.name == "height" }.forEach {
                try {
                    val constraint = Constraint(Expression(Term(it, 1.0)), Operator.GE, Strength.Required)
                    solver.addConstraint(constraint)

                    syntheticLimits += constraint
                } catch (ignored: Exception) {

                }
            }

            context.constraints.clear()
        }

        fun solve(solver: Solver, parentConstraints: ParentConstraints, updatedBounds: MutableSet<View>) {
            //        println("""
//            |constraints
//            |-----------
//            |${context.constraints.joinToString("\n")}""".trimMargin())

//        println("""
//            |variables
//            |---------
//            |${solver.variables.joinToString("\n")}""".trimMargin())

            solver.variables.forEach {
                val strength = when {
                    it == parentConstraints.width || it == parentConstraints.height -> Strength(Required.value - 1) //Strong
                    it is ReflectionVariable && it.target in updatedBounds          -> Strength(100)
                    else                                                            -> Strength.Weak
                }

                try {
                    solver.addEditVariable(it, strength)
                } catch (ignore: DuplicateEditVariableException) {

                }

                solver.suggestValue(it, it())
            }

            solver.updateVariables   ()
            solver.clearEditVariables()

            updatedBounds.clear()
        }
    }
}

private open class ParentConstraintsImpl(val target: View, private val context: ConstraintDslContext): ParentConstraints {
//    private var width_  get() = target.width;  set(new) {}
//    private var height_ get() = target.height; set(new) {}

    override val height  by lazy { ReflectionVariable(target, target::height) { max(0.0, it) } } //ReflectionVariable(target, ::height_) { max(0.0, it) }
    override val bottom  by lazy { with(context) { 0 + height     } }
    override val centerY by lazy { with(context) { 0 + height / 2 } }

    override val width   by lazy { ReflectionVariable(target, target::width) { max(0.0, it) } } //ReflectionVariable(target, ::width_) { max(0.0, it) }
    override val right   by lazy { with(context) { 0 + width     } }
    override val centerX by lazy { with(context) { 0 + width / 2 } }

    override val center by lazy { Position(centerX, centerY) }
    override val edges  by lazy { Edges(right = right, bottom = bottom) }
}

private open class DisplayConstraints(private val display: Display, private val context: ConstraintDslContext): ParentConstraints {
    private var _width  get() = display.width;  set(new) {}
    private var _height get() = display.height; set(new) {}

    override val width   = ReflectionVariable(delegate = ::_width )
    override val height  = ReflectionVariable(delegate = ::_height)
    override val right   by lazy { with(context) { 0 + width      } }
    override val bottom  by lazy { with(context) { 0 + height     } }
    override val centerX by lazy { with(context) { 0 + width  / 2 } }
    override val centerY by lazy { with(context) { 0 + height / 2 } }

    override val center  by lazy { Position(centerX, centerY) }
    override val edges   by lazy { Edges(right = right, bottom = bottom) }
}

internal open class RectangleConstraints(private val rectangle: Rectangle, private val context: ConstraintDslContext): ParentConstraints {
    private var _width  get() = rectangle.width;  set(new) {}
    private var _height get() = rectangle.height; set(new) {}

    override val width   by lazy { ReflectionVariable(delegate = ::_width ) }
    override val height  by lazy { ReflectionVariable(delegate = ::_height) }
    override val right   by lazy { with(context) { 0 + width      } }
    override val bottom  by lazy { with(context) { 0 + height     } }
    override val centerX by lazy { with(context) { 0 + width  / 2 } }
    override val centerY by lazy { with(context) { 0 + height / 2 } }

    override val center  by lazy { Position(centerX, centerY) }
    override val edges   by lazy { Edges(right = right, bottom = bottom) }
}

internal open class ConstraintsImpl(private val target: View, private val context: ConstraintDslContext): Constraints {
    override val top     by lazy { ReflectionVariable(target, target::y     ) }
    override val left    by lazy { ReflectionVariable(target, target::x     ) }
    override val width   by lazy { ReflectionVariable(target, target::width ) { max(0.0, it) } }
    override val height  by lazy { ReflectionVariable(target, target::height) { max(0.0, it) } }

    override val right   by lazy { with(context) { left + width      } }
    override val centerX by lazy { with(context) { left + width  / 2 } }
    override val bottom  by lazy { with(context) { top  + height     } }
    override val centerY by lazy { with(context) { top  + height / 2 } }

    override val center  by lazy { Position(centerX, centerY) }
    override val edges   by lazy { with(context) { Edges(top + 0, left + 0, right, bottom) } }

//    override val idealWidth  = NullableMagnitudeConstraint(IgnoreTarget) { target.idealSize?.width  }
//    override val idealHeight = NullableMagnitudeConstraint(IgnoreTarget) { target.idealSize?.height }

//    override val minWidth  = MagnitudeConstraint(IgnoreTarget) { target.minimumSize.width  }
//    override val minHeight = MagnitudeConstraint(IgnoreTarget) { target.minimumSize.height }

//    override fun toString() = "C $target -> top: $top, left: $left, centerX: $centerX, centerY: $centerY, right: $right, bottom: $bottom"
}

internal data class ReflectionVariable(val target: View? = null, private val delegate: KMutableProperty0<Double>, private val mapper: (Double) -> Double = { it }): Property(), Variable {
    override val name = delegate.name
    override fun invoke() = delegate()

    override fun invoke(value: Double) {
        delegate.set(mapper(value))
    }

    override fun toString() = "$target.$name"
}