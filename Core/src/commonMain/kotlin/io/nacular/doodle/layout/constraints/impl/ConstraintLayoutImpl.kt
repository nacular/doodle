package io.nacular.doodle.layout.constraints.impl

import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.PositionableContainerWrapper
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstTerm
import io.nacular.doodle.layout.constraints.Constraint
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.ConstraintLayout
import io.nacular.doodle.layout.constraints.Edges
import io.nacular.doodle.layout.constraints.Expression
import io.nacular.doodle.layout.constraints.Operator.GE
import io.nacular.doodle.layout.constraints.ParentBounds
import io.nacular.doodle.layout.constraints.Position
import io.nacular.doodle.layout.constraints.Property
import io.nacular.doodle.layout.constraints.Strength
import io.nacular.doodle.layout.constraints.Strength.Companion.Required
import io.nacular.doodle.layout.constraints.Strength.Companion.Weak
import io.nacular.doodle.layout.constraints.Term
import io.nacular.doodle.layout.constraints.Variable
import io.nacular.doodle.layout.constraints.VariableTerm
import io.nacular.doodle.utils._removeAll
import io.nacular.doodle.utils.diff.Operation.Delete
import io.nacular.doodle.utils.diff.Operation.Insert
import io.nacular.doodle.utils.diff.compare
import kotlin.math.max
import kotlin.reflect.KMutableProperty0

private open class ParentBoundsImpl(val target: View, private val context: ConstraintDslContext): ParentBounds {
    override val height  by lazy { ReflectionVariable(target, target::height, id = 0) { max(0.0, it) } }
    override val bottom  by lazy { with(context) { 0 + height     } }
    override val centerY by lazy { with(context) { 0 + height / 2 } }

    override val width   by lazy { ReflectionVariable(target, target::width, id = 1) { max(0.0, it) } }
    override val right   by lazy { with(context) { 0 + width     } }
    override val centerX by lazy { with(context) { 0 + width / 2 } }

    override val center by lazy { Position(centerX, centerY) }
    override val edges  by lazy { Edges(right = right, bottom = bottom) }
}

private class DisplayBonds(container: PositionableContainer, private val context: ConstraintDslContext): ParentBounds {
    override val width   = ReadOnlyProperty(container.width )
    override val height  = ReadOnlyProperty(container.height)
    override val right   by lazy { with(context) { 0 + width      } }
    override val bottom  by lazy { with(context) { 0 + height     } }
    override val centerX by lazy { with(context) { 0 + width  / 2 } }
    override val centerY by lazy { with(context) { 0 + height / 2 } }

    override val center  by lazy { Position(centerX, centerY) }
    override val edges   by lazy { Edges(right = right, bottom = bottom) }
}

internal open class RectangleBounds(rectangle: Rectangle, private val context: ConstraintDslContext): ParentBounds {
    override val width   = ReadOnlyProperty(rectangle.width )
    override val height  = ReadOnlyProperty(rectangle.height )
    override val right   by lazy { with(context) { 0 + width      } }
    override val bottom  by lazy { with(context) { 0 + height     } }
    override val centerX by lazy { with(context) { 0 + width  / 2 } }
    override val centerY by lazy { with(context) { 0 + height / 2 } }

    override val center  by lazy { Position(centerX, centerY) }
    override val edges   by lazy { Edges(right = right, bottom = bottom) }
}

internal open class BoundsImpl(private val target: View, private val context: ConstraintDslContext): Bounds {
    private val term: ReflectionVariable.() -> Term = {
        context.let {
            when {
                this@BoundsImpl.target.parent != it.parent_ -> ConstTerm(this)
                else                                        -> VariableTerm(this)
            }
        }
    }

    override val top     by lazy { ReflectionVariable(target, target::y,      id = 0, term) }
    override val left    by lazy { ReflectionVariable(target, target::x,      id = 1, term) }
    override val width   by lazy { ReflectionVariable(target, target::width,  id = 2, term) { max(0.0, it) } }
    override val height  by lazy { ReflectionVariable(target, target::height, id = 3, term) { max(0.0, it) } }

    override val right   by lazy { with(context) { left + width      } }
    override val centerX by lazy { with(context) { left + width  / 2 } }
    override val bottom  by lazy { with(context) { top  + height     } }
    override val centerY by lazy { with(context) { top  + height / 2 } }

    override val center  by lazy { Position(centerX, centerY) }
    override val edges   by lazy { with(context) { Edges(top + 0, left + 0, right, bottom) } }
}

internal data class ReadOnlyProperty(val value: Double): Property() {
    override val readOnly: Double get() = value
    override fun toTerm() = ConstTerm(this)

    override fun toString() = "$value"
}

internal class ReflectionVariable(
            val target  : Any? = null,
    private val delegate: KMutableProperty0<Double>,
    private val id      : Int, // ugly work-around for https://youtrack.jetbrains.com/issue/KT-15101
    private val term    : ReflectionVariable.() -> Term = { VariableTerm(this) },
    private val mapper  : (Double) -> Double = { it }
): Property(), Variable {
    private val hashCode by lazy {
        var result = target?.hashCode() ?: 0
        result = 31 * result + id
        result
    }

    override val name get() = delegate.name
    override fun invoke() = delegate()

    override val readOnly: Double get() = invoke()

    override fun invoke(value: Double) {
        delegate.set(mapper(value))
    }

    override fun toTerm(): Term = term(this)

    override fun toString() = "$target.$name"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ReflectionVariable

        if (target != other.target) return false
        if (id     != other.id    ) return false

        return true
    }

    override fun hashCode() = hashCode
}

internal class ConstraintLayoutImpl(view: View, vararg others: View, originalLambda: Any, block: ConstraintDslContext.(List<Bounds>) -> Unit): ConstraintLayout() {
    @Suppress("PrivatePropertyName")
    private val parentChanged_ = ::parentChanged

    @Suppress("PrivatePropertyName")
    private val boundsChanged_ = ::boundsChanged

    @Suppress("UNUSED_PARAMETER")
    private fun parentChanged(child: View, old: View?, new: View?) {
        child.parentChange  -= parentChanged_
        child.boundsChanged -= boundsChanged_
    }

    private val activeBounds  = mutableSetOf<ReflectionVariable>()
    private val updatedBounds = mutableSetOf<ReflectionVariable>()

    @Suppress("UNUSED_PARAMETER")
    private fun boundsChanged(child: View, old: Rectangle, new: Rectangle) {
        // FIXME: Get rid of magic numbers for equality
        updatedBounds += ReflectionVariable(child, child::y,      0)
        updatedBounds += ReflectionVariable(child, child::x,      1)
        updatedBounds += ReflectionVariable(child, child::width,  2)
        updatedBounds += ReflectionVariable(child, child::height, 3)
    }

    internal data class BlockInfo(val constraints: List<Bounds>, val block: ConstraintDslContext.(List<Bounds>) -> Unit)

    private val solver       = Solver()
    private val blocks       = mutableListOf(block)
    private val context      = ConstraintDslContext()
    private val blockTracker = mutableMapOf<Pair<List<View>, Any>, BlockInfo>()

    init {
        constrain(listOf(view) + others, originalLambda, block)
    }

    override fun layout(container: PositionableContainer) {
        context.apply {
            parent = when (container) {
                is View                         -> ParentBoundsImpl(container,      this).also { parent_ = container      }
                is PositionableContainerWrapper -> ParentBoundsImpl(container.view, this).also { parent_ = container.view }
                else                            -> DisplayBonds    (container,      this)
            }
        }

        setupSolver(solver, context, updatedBounds, blockTracker.values.toList())
        solve      (solver, context, activeBounds, updatedBounds)
    }

    override fun constrain  (view: View, constraints: ConstraintDslContext.(Bounds) -> Unit) = constrain  (listOf(view), originalLambda = constraints) { (a) -> constraints(a) }
    override fun unconstrain(view: View, constraints: ConstraintDslContext.(Bounds) -> Unit) = unconstrain(listOf(view), constraints)

    override fun constrain  (first: View, second: View, constraints: ConstraintDslContext.(Bounds, Bounds) -> Unit) = constrain  (listOf(first, second), originalLambda = constraints) { (a, b) -> constraints(a, b) }
    override fun unconstrain(first: View, second: View, constraints: ConstraintDslContext.(Bounds, Bounds) -> Unit) = unconstrain(listOf(first, second), constraints)

    override fun constrain  (first: View, second: View, third: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds) -> Unit) = constrain  (listOf(first, second, third), constraints) { (a, b, c) -> constraints(a, b, c) }
    override fun unconstrain(first: View, second: View, third: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds) -> Unit) = unconstrain(listOf(first, second, third), constraints)

    override fun constrain  (first: View, second: View, third: View, fourth: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds) -> Unit) = constrain  (listOf(first, second, third, fourth), constraints) { (a, b, c, d) -> constraints(a, b, c, d) }
    override fun unconstrain(first: View, second: View, third: View, fourth: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds) -> Unit) = unconstrain(listOf(first, second, third, fourth), constraints)

    override fun constrain  (first: View, second: View, third: View, fourth: View, fifth: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds, Bounds) -> Unit) = constrain  (listOf(first, second, third, fourth, fifth), constraints) { (a, b, c, d, e) -> constraints(a, b, c, d, e) }
    override fun unconstrain(first: View, second: View, third: View, fourth: View, fifth: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds, Bounds) -> Unit) = unconstrain(listOf(first, second, third, fourth, fifth), constraints)

    override fun constrain  (first: View, second: View, vararg others: View, constraints: ConstraintDslContext.(List<Bounds>) -> Unit) = constrain  (listOf(first, second), originalLambda = constraints, constraints = constraints)
    override fun unconstrain(first: View, second: View, vararg others: View, constraints: ConstraintDslContext.(List<Bounds>) -> Unit) = unconstrain(listOf(first, second) + others, constraints)

    private fun constrain(views: List<View>, originalLambda: Any, constraints: ConstraintDslContext.(List<Bounds>) -> Unit): ConstraintLayout {
        val newConstraints  = constraints(views)
        blocks             += constraints
        blockTracker[views to originalLambda] = BlockInfo(newConstraints, constraints)

        return this
    }

    private fun unconstrain(views: List<View>, constraintBlock: Any): ConstraintLayout {
        blockTracker.remove(views to constraintBlock)?.let {
            blocks -= it.block
        }

        return this
    }

    private fun constraints(views: List<View>): List<Bounds> = views.map {
        it.parentChange  += parentChanged_
        it.boundsChanged += boundsChanged_

        if (it.x != 0.0 || it.y != 0.0 || it.width != 0.0 || it.height != 0.0) {
            boundsChanged(it, Rectangle.Empty, it.bounds)
        }

        BoundsImpl(it, context)
    }

    internal companion object {

        fun setupSolver(solver: Solver, context: ConstraintDslContext, updatedBounds: MutableSet<ReflectionVariable> = mutableSetOf(), blocks: List<BlockInfo>) {
            val oldConstraints = context.constraints
            context.constraints = mutableListOf()

            blocks.forEach {
                it.block(context, it.constraints)
            }

            compare(oldConstraints, context.constraints) { a, b -> a.hashCode() == b.hashCode() }.forEach {
                when (it.operation) {
                    Delete -> {
                        solver.removeConstraints(it.items)

                        it.items.flatMap {
                            it.expression.terms.filterIsInstance<VariableTerm>().map { it.variable }
                        }.filter { it.name == "width" || it.name == "height" }.toSet().forEach { variable ->
                            try {
                                val constraint = Constraint(Expression(VariableTerm(variable, 1.0)), GE, Required)
                                solver.removeConstraints(constraint)
//                                    println("- synthetic constraint: $constraint")
                            } catch (ignored: Exception) {}
                        }
                    }
                    Insert -> {
                        solver.addConstraints(it.items)

                        val addedVariables = it.items.flatMap { it.expression.terms.filterIsInstance<VariableTerm>().map { it.variable } }.toSet()

                        addedVariables.forEach { variable ->

                            if (variable.name == "width" || variable.name == "height") {
                                try {
                                    val constraint = Constraint(Expression(VariableTerm(variable, 1.0)), GE, Required)
                                    solver.addConstraints(constraint)
//                                        println("+ synthetic constraint: $constraint")
                                } catch (ignore: Exception) {}
                            }

                            val strength = when {
                                variable == context.parent.width || variable == context.parent.height -> Strength(Required.value - 1)
                                variable is ReflectionVariable && variable.target in updatedBounds    -> Strength(100)
                                else                                                                  -> Weak
                            }

                            try {
                                solver.addEditVariable(variable, strength)
                            } catch (ignore: Exception) {}

                            if (variable() != 0.0) {
                                solver.suggestValue(variable, variable())
                            }
                        }
                    }
                    else             -> {}
                }
            }
        }

        fun solve(solver       : Solver,
                  context      : ConstraintDslContext,
                  activeBounds : MutableSet<ReflectionVariable> = mutableSetOf(),
                  updatedBounds: MutableSet<ReflectionVariable> = mutableSetOf()
        ) {
            activeBounds._removeAll { it !in updatedBounds }.forEach {
                try {
                    solver.removeEditVariable(it)
                } catch (ignore: Exception) {}

                try {
                    solver.addEditVariable(it, Weak)
                    solver.suggestValue(it, it())
                } catch (ignore: Exception) {}
            }

            updatedBounds.filter { it !in activeBounds }.forEach {
                try {
                    solver.removeEditVariable(it)
                } catch (ignore: Exception) {}

                try {
                    solver.addEditVariable(it, Strength(100))
                } catch (ignore: Exception) {}
            }

            updatedBounds.forEach {
                try {
                    solver.suggestValue(it, it())
                } catch (ignore: Exception) {}
            }

            activeBounds.addAll(updatedBounds)

            (context.parent.width as? Variable)?.let {
                try {
                    if (solver.containsEditVariable(it)) {
                        solver.suggestValue(it, context.parent.width.readOnly)
                    }
                } catch (ignore: Exception) {}
            }

            (context.parent.height as? Variable)?.let {
                try {
                    if (solver.containsEditVariable(it)) {
                        solver.suggestValue(it, context.parent.height.readOnly)
                    }
                } catch (ignore: Exception) {}
            }

            updatedBounds.clear()

            solver.updateVariables()

            // Hold anything that changes. These should all have Weak strength
            updatedBounds.filter { it !in activeBounds }.forEach {
                try {
                    solver.suggestValue(it, it())
                } catch (ignore: Exception) {}
            }

            // Cleanup holds
            updatedBounds.clear()
        }
    }
}
