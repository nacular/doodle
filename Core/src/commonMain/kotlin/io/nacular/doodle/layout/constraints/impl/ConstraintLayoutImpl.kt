package io.nacular.doodle.layout.constraints.impl

import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.PositionableContainerWrapper
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstEdges
import io.nacular.doodle.layout.constraints.ConstExpression
import io.nacular.doodle.layout.constraints.ConstPosition
import io.nacular.doodle.layout.constraints.ConstProperty
import io.nacular.doodle.layout.constraints.ConstTerm
import io.nacular.doodle.layout.constraints.Constraint
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.ConstraintException
import io.nacular.doodle.layout.constraints.ConstraintLayout
import io.nacular.doodle.layout.constraints.Edges
import io.nacular.doodle.layout.constraints.Expression
import io.nacular.doodle.layout.constraints.Operator.GE
import io.nacular.doodle.layout.constraints.ParentBounds
import io.nacular.doodle.layout.constraints.Position
import io.nacular.doodle.layout.constraints.Property
import io.nacular.doodle.layout.constraints.PropertyWrapper
import io.nacular.doodle.layout.constraints.Strength
import io.nacular.doodle.layout.constraints.Strength.Companion.Required
import io.nacular.doodle.layout.constraints.Strength.Companion.Weak
import io.nacular.doodle.layout.constraints.Term
import io.nacular.doodle.layout.constraints.Variable
import io.nacular.doodle.layout.constraints.VariableTerm
import io.nacular.doodle.layout.constraints.impl.ReflectionVariable.Companion.heightId
import io.nacular.doodle.layout.constraints.impl.ReflectionVariable.Companion.widthId
import io.nacular.doodle.layout.constraints.impl.ReflectionVariable.Companion.xId
import io.nacular.doodle.layout.constraints.impl.ReflectionVariable.Companion.yId
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils._removeAll
import io.nacular.doodle.utils.diff.Delete
import io.nacular.doodle.utils.diff.Insert
import io.nacular.doodle.utils.diff.compare
import io.nacular.doodle.utils.ifNull
import kotlin.math.max
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0

private open class ParentBoundsImpl(val target: View, private val context: ConstraintDslContext): ParentBounds {
    private val width_  by lazy { ReflectionVariable(target, target::width,  id = widthId ) { max(0.0, it) } }
    private val height_ by lazy { ReflectionVariable(target, target::height, id = heightId) { max(0.0, it) } }

    override val height  by lazy { ConstProperty  (height_)                           }
    override val bottom  by lazy { ConstExpression(with(context) { 0 + height_     }) }
    override val centerY by lazy { ConstExpression(with(context) { 0 + height_ / 2 }) }

    override val width   by lazy { ConstProperty  (width_)                           }
    override val right   by lazy { ConstExpression(with(context) { 0 + width_     }) }
    override val centerX by lazy { ConstExpression(with(context) { 0 + width_ / 2 }) }

    override val center by lazy { ConstPosition(Position(centerX.writable, centerY.writable)) }
    override val edges  by lazy { ConstEdges(Edges(right = right.writable, bottom = bottom.writable)) }
}

internal class ImmutableSizeBounds(widthProperty: KProperty0<Double>, heightProperty: KProperty0<Double>, private val context: ConstraintDslContext): ParentBounds {
    private val width_   = ReadOnlyProperty(widthProperty )
    private val height_  = ReadOnlyProperty(heightProperty)

    override val width   = ConstProperty(width_ )
    override val height  = ConstProperty(height_)
    override val right   by lazy { ConstExpression(with(context) { 0 + width_      }) }
    override val bottom  by lazy { ConstExpression(with(context) { 0 + height_     }) }
    override val centerX by lazy { ConstExpression(with(context) { 0 + width_  / 2 }) }
    override val centerY by lazy { ConstExpression(with(context) { 0 + height_ / 2 }) }

    override val center  by lazy { ConstPosition(Position(centerX.writable, centerY.writable)) }
    override val edges   by lazy { ConstEdges(Edges(right = right.writable, bottom = bottom.writable)) }
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

    override val top     by lazy { ReflectionVariable(target, target::y,      id = yId,      term) }
    override val left    by lazy { ReflectionVariable(target, target::x,      id = xId,      term) }
    override val width   by lazy { ReflectionVariable(target, target::width,  id = widthId,  term) { max(0.0, it) } }
    override val height  by lazy { ReflectionVariable(target, target::height, id = heightId, term) { max(0.0, it) } }

    override val right   by lazy { with(context) { left + width      } }
    override val centerX by lazy { with(context) { left + width  / 2 } }
    override val bottom  by lazy { with(context) { top  + height     } }
    override val centerY by lazy { with(context) { top  + height / 2 } }

    override val center  by lazy { Position(centerX, centerY) }
    override val edges   by lazy { with(context) { Edges(top + 0, left + 0, right, bottom) } }
}

internal class ReadOnlyProperty(val value: KProperty0<Double>): Property() {
    override val readOnly: Double get() = value()
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
    @Suppress("PrivatePropertyName")
    private val hashCode_ by lazy {
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

        when (other) {
            is ReflectionVariable -> {}
            is PropertyWrapper    -> return other.variable == this
            else                  -> return false
        }

        if (target != other.target) return false
        if (id     != other.id    ) return false

        return true
    }

    override fun hashCode() = hashCode_

    companion object {
        const val yId      = 0
        const val xId      = 1
        const val widthId  = 2
        const val heightId = 3
    }
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
        updatedBounds += ReflectionVariable(child, child::y,      yId     )
        updatedBounds += ReflectionVariable(child, child::x,      xId     )
        updatedBounds += ReflectionVariable(child, child::width,  widthId )
        updatedBounds += ReflectionVariable(child, child::height, heightId)
    }

    internal data class BlockInfo(val constraints: List<Bounds>, val block: ConstraintDslContext.(List<Bounds>) -> Unit)

    private val solver       = Solver()
    private val blocks       = mutableListOf(block)
    private val context      = ConstraintDslContext()
    private val blockTracker = mutableMapOf<Pair<List<View>, Any>, BlockInfo>()

    override val exceptionThrown: Pool<(ConstraintLayout, ConstraintException) -> Unit> = SetPool()

    init {
        constrain(listOf(view) + others, originalLambda, block)
    }

    override fun layout(container: PositionableContainer) {
        context.apply {
            parent = when (container) {
                is View                         -> ParentBoundsImpl   (container,      this                                        ).also { parent_ = container      }
                is PositionableContainerWrapper -> ParentBoundsImpl   (container.view, this                                        ).also { parent_ = container.view }
                else                            -> ImmutableSizeBounds(widthProperty = container::width, heightProperty = container::height, this)
            }
        }

        setupSolver(solver, context, updatedBounds, blockTracker.values, ::notifyOfErrors)
        solve      (solver, context, activeBounds, updatedBounds, ::notifyOfErrors)
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
            boundsChanged(it, Empty, it.bounds)
        }

        BoundsImpl(it, context)
    }

    private fun notifyOfErrors(exception: ConstraintException) {
        (exceptionThrown as SetPool).forEach { it(this, exception) }
    }

    internal companion object {
        private fun handlePreviousDelete(solver: Solver, previousDelete: Delete<Constraint>?) {
            previousDelete?.let {
                it.items.forEach { constraint ->
                    handleDelete(solver, constraint)
                }
            }
        }

        private fun handleInsert(
            solver            : Solver,
            context           : ConstraintDslContext,
            updatedBounds     : MutableSet<ReflectionVariable>,
            insertedConstraint: Constraint,
            errorHandler      : (ConstraintException) -> Unit
        ) {
            try {
                solver.addConstraint(insertedConstraint)
            } catch (exception: ConstraintException) {
                errorHandler(exception)
            }

            insertedConstraint.expression.terms.map { it.variable }.toSet().forEach { variable ->

                if (variable.name == "width" || variable.name == "height") {
                    // Add synthetic constraints that keep width and height positive
                    try {
                        solver.addConstraint(Constraint(Expression(VariableTerm(variable, 1.0)), GE, Required))
                    } catch (ignore: Exception) { }
                }

                val strength = when {
                    variable == context.parent.width.writable || variable == context.parent.height.writable -> Strength(Required.value - 1).also { println("parent property: $variable") }
                    variable is ReflectionVariable && variable.target in updatedBounds                      -> Strength(100)
                    else                                                                                    -> Weak
                }

                try {
                    solver.addEditVariable(variable, strength)
                } catch (ignore: Exception) { }

                if (variable() != 0.0) {
                    try {
                        solver.suggestValue(variable, variable())
                    } catch (exception: ConstraintException) {
                        errorHandler(exception)
                    } catch (ignore: UnknownEditVariableException) { }
                }
            }
        }

        private fun handleDelete(solver: Solver, constraint: Constraint) {
            try {
                solver.removeConstraint(constraint)
            } catch (ignore: Exception) {}

            constraint.expression.terms.map { it.variable }.filter { it.name == "width" || it.name == "height" }.toSet().forEach { variable ->
                // Remove synthetic constraints that keep width and height positive
                try {
                    solver.removeConstraint(Constraint(Expression(VariableTerm(variable, 1.0)), GE, Required))
                } catch (ignore: Exception) {}
            }
        }

        fun setupSolver(
            solver       : Solver,
            context      : ConstraintDslContext,
            updatedBounds: MutableSet<ReflectionVariable> = mutableSetOf(),
            blocks       : Collection<BlockInfo>,
            errorHandler : (ConstraintException) -> Unit
        ) {
            val oldConstraints = context.constraints
            context.constraints = mutableListOf()

            blocks.forEach {
                it.block(context, it.constraints)
            }

            var previousDelete: Delete<Constraint>? = null

            compare(oldConstraints, context.constraints) { a, b -> a.hashCode() == b.hashCode() }.forEach { difference ->
                when (difference) {
                    is Delete -> {
                        handlePreviousDelete(solver, previousDelete)
                        previousDelete = difference
                    }
                    is Insert -> {
                        difference.items.forEachIndexed { index, insertedConstraint ->
                            previousDelete?.let {
                                if (index < it.items.size && insertedConstraint.differsByConstantOnly(it.items[index])) {
                                    // update constraint
                                    try {
                                        solver.updateConstant(it.items[index], insertedConstraint)
                                    } catch (exception: ConstraintException) {
                                        errorHandler(exception)
                                    } catch (ignore: UnknownConstraintException) {}
                                } else {
                                    // handle delete
                                    handlePreviousDelete(solver, previousDelete).also { previousDelete = null }
                                    // handle insert
                                    handleInsert(solver, context, updatedBounds, insertedConstraint, errorHandler)
                                }
                            }.ifNull {
                                // handle insert
                                handleInsert(solver, context, updatedBounds, insertedConstraint, errorHandler)
                            }
                        }

                        previousDelete = null
                    }
                    else             -> {}
                }
            }

            handlePreviousDelete(solver, previousDelete)
        }

        fun solve(solver       : Solver,
                  context      : ConstraintDslContext,
                  activeBounds : MutableSet<ReflectionVariable> = mutableSetOf(),
                  updatedBounds: MutableSet<ReflectionVariable> = mutableSetOf(),
                  errorHandler : (ConstraintException) -> Unit
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
                } catch (exception: ConstraintException) {
                    errorHandler(exception)
                } catch (ignore: UnknownEditVariableException) {}
            }

            activeBounds.addAll(updatedBounds)

            (context.parent.width.writable as? Variable)?.let {
                try {
                    if (solver.containsEditVariable(it)) {
                        solver.suggestValue(it, context.parent.width.writable.readOnly)
                    }
                } catch (ignore: Exception) {}
            }

            (context.parent.height.writable as? Variable)?.let {
                try {
                    if (solver.containsEditVariable(it)) {
                        solver.suggestValue(it, context.parent.height.writable.readOnly)
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
