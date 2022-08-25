package io.nacular.doodle.layout.cassowary.impl

import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.PositionableContainerWrapper
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.cassowary.Bounds
import io.nacular.doodle.layout.cassowary.ConstTerm
import io.nacular.doodle.layout.cassowary.Constraint
import io.nacular.doodle.layout.cassowary.ConstraintDslContext
import io.nacular.doodle.layout.cassowary.ConstraintLayout
import io.nacular.doodle.layout.cassowary.DuplicateEditVariableException
import io.nacular.doodle.layout.cassowary.Edges
import io.nacular.doodle.layout.cassowary.Expression
import io.nacular.doodle.layout.cassowary.Operator.GE
import io.nacular.doodle.layout.cassowary.ParentBounds
import io.nacular.doodle.layout.cassowary.Position
import io.nacular.doodle.layout.cassowary.Property
import io.nacular.doodle.layout.cassowary.Solver
import io.nacular.doodle.layout.cassowary.Strength
import io.nacular.doodle.layout.cassowary.Strength.Companion.Required
import io.nacular.doodle.layout.cassowary.Strength.Companion.Weak
import io.nacular.doodle.layout.cassowary.Term
import io.nacular.doodle.layout.cassowary.Variable
import io.nacular.doodle.layout.cassowary.VariableTerm
import io.nacular.doodle.utils.ObservableList
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

    override fun hashCode(): Int {
        var result = target?.hashCode() ?: 0
        result = 31 * result + id
        return result
    }
}

internal class ConstraintLayoutImpl(view: View, vararg others: View, block: ConstraintDslContext.(List<Bounds>) -> Unit): ConstraintLayout() {
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
        constrain(listOf(view) + others, block, block)
    }

    override fun layout(container: PositionableContainer) {
        context.apply {
            parent = when (container) {
                is View                         -> ParentBoundsImpl(container,      this).also { parent_ = container      }
                is PositionableContainerWrapper -> ParentBoundsImpl(container.view, this).also { parent_ = container.view }
                else                            -> DisplayBonds  (container, this)
            }
        }

        setupSolver(solver, context, updatedBounds, blockTracker.values.toList())
        solve      (solver, context, activeBounds, updatedBounds)
    }

    override fun constrain  (view: View, constraints: ConstraintDslContext.(Bounds) -> Unit) = constrain  (listOf(view), constraintBlock = constraints) { (a) -> constraints(a) }
    override fun unconstrain(view: View, constraints: ConstraintDslContext.(Bounds) -> Unit) = unconstrain(listOf(view), constraints)

    override fun constrain  (first: View, second: View, constraints: ConstraintDslContext.(Bounds, Bounds) -> Unit) = constrain  (listOf(first, second), constraintBlock = constraints) { (a, b) -> constraints(a, b) }
    override fun unconstrain(first: View, second: View, constraints: ConstraintDslContext.(Bounds, Bounds) -> Unit) = unconstrain(listOf(first, second), constraints)

    override fun constrain  (first: View, second: View, third: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds) -> Unit) = constrain  (listOf(first, second, third), constraints) { (a, b, c) -> constraints(a, b, c) }
    override fun unconstrain(first: View, second: View, third: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds) -> Unit) = unconstrain(listOf(first, second, third), constraints)

    override fun constrain  (first: View, second: View, third: View, fourth: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds) -> Unit) = constrain  (listOf(first, second, third, fourth), constraints) { (a, b, c, d) -> constraints(a, b, c, d) }
    override fun unconstrain(first: View, second: View, third: View, fourth: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds) -> Unit) = unconstrain(listOf(first, second, third, fourth), constraints)

    override fun constrain  (first: View, second: View, third: View, fourth: View, fifth: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds, Bounds) -> Unit) = constrain  (listOf(first, second, third, fourth, fifth), constraints) { (a, b, c, d, e) -> constraints(a, b, c, d, e) }
    override fun unconstrain(first: View, second: View, third: View, fourth: View, fifth: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds, Bounds) -> Unit) = unconstrain(listOf(first, second, third, fourth, fifth), constraints)

    override fun constrain  (first: View, second: View, vararg others: View, constraints: ConstraintDslContext.(List<Bounds>) -> Unit) = constrain  (listOf(first, second), constraintBlock = constraints, block = constraints)
    override fun unconstrain(first: View, second: View, vararg others: View, constraints: ConstraintDslContext.(List<Bounds>) -> Unit) = unconstrain(listOf(first, second) + others, constraints)

    private fun constrain  (views: List<View>, constraintBlock: Any, block: ConstraintDslContext.(List<Bounds>) -> Unit): ConstraintLayout {
        val newConstraints = constraints(views)
        blocks      += block
        blockTracker[views to constraintBlock] = BlockInfo(newConstraints, block)

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
            // FIXME NEED FASTER DIFFING
            val oldConstraints = ObservableList(context.constraints).apply {
                changed += { _, removed, added, _ ->
//                    println("updating solver")
                    solver.removeConstraints(removed.values)
                    solver.addConstraints(added.values)

                    solver.variables.filter { it.name == "width" || it.name == "height" }.forEach {
                        try {
                            val constraint = Constraint(Expression(VariableTerm(it, 1.0)), GE, Required)
                            solver.addConstraints(constraint)
                        } catch (ignored: Exception) {
                        }
                    }

                    val addedVariables = added.values.flatMap { it.expression.terms.filterIsInstance<VariableTerm>().map { it.variable } }.toSet()

                    addedVariables.forEach { variable ->
                        val strength = when {
                            variable == context.parent.width || variable == context.parent.height -> Strength(Required.value - 1)
                            variable is ReflectionVariable && variable.target in updatedBounds    -> Strength(100)
                            else                                                                  -> Weak
                        }

                        try {
                            solver.addEditVariable(variable, strength)
                        } catch (ignore: DuplicateEditVariableException) {
//                        ignore.printStackTrace()
                        }

                        if (variable() != 0.0) {
                            solver.suggestValue(variable, variable())//.apply { println("SETUP - SUGGESTING: $it -> ${variable()} ~$strength") }
                        }
                    }
                }
            }

            context.constraints.clear()

            blocks.forEach {
                it.block(context, it.constraints)
            }

//        if (context.constraints.size != oldConstraints.size) {
            oldConstraints.replaceAll(context.constraints)
//        }
        }

        fun solve(solver: Solver, context: ConstraintDslContext, activeBounds: MutableSet<ReflectionVariable> = mutableSetOf(), updatedBounds: MutableSet<ReflectionVariable> = mutableSetOf()) {
            activeBounds._removeAll { it !in updatedBounds }.forEach {
//                println("resetting edit: $it")
                solver.removeEditVariable(it)
                solver.addEditVariable(it, Weak)
                solver.suggestValue(it, it())
            }

            updatedBounds.filter { it !in activeBounds }.forEach {
//                println("adding edit: $it")

                try {
                    solver.removeEditVariable(it)
                } catch (ignore: Exception) {

                }

                solver.addEditVariable(it, Strength(100))
            }

            updatedBounds.forEach {
                solver.suggestValue(it, it())//.apply { println("SOLVE - SUGGESTING: $it -> ${it()}") }
            }

            activeBounds.addAll(updatedBounds)

            (context.parent.width as? Variable )?.let {
                try {
                    solver.suggestValue(it, context.parent.width.readOnly).apply { "SUGGEST Parent.width: ${context.parent.width.readOnly}" }
                } catch (ignore: Exception) {
                    ignore.printStackTrace()
                }
            }

            (context.parent.height as? Variable)?.let {
                try {
                    solver.suggestValue(it, context.parent.height.readOnly).apply { "SUGGEST parent.height: ${context.parent.height.readOnly}" }
                } catch (ignore: Exception) {
                    ignore.printStackTrace()
                }
            }

            solver.updateVariables()

            updatedBounds.clear()
        }
    }
}

@Suppress("FunctionName")
private fun <E> MutableIterable<E>._removeAll(predicate: (E) -> Boolean): List<E> {
    val result = mutableListOf<E>()
    this.removeAll {
        val r = predicate(it)
        if (r) { result += it }
        r
    }

    return result
}
