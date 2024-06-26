package io.nacular.doodle.layout.constraints.impl

import io.nacular.doodle.core.Positionable2
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.Constraint
import io.nacular.doodle.layout.constraints.ConstraintDslContext2
import io.nacular.doodle.layout.constraints.ConstraintException
import io.nacular.doodle.layout.constraints.ConstraintLayout2
import io.nacular.doodle.layout.constraints.Edges
import io.nacular.doodle.layout.constraints.Expression
import io.nacular.doodle.layout.constraints.Operator.GE
import io.nacular.doodle.layout.constraints.ParentBounds2
import io.nacular.doodle.layout.constraints.Position
import io.nacular.doodle.layout.constraints.Property
import io.nacular.doodle.layout.constraints.Strength
import io.nacular.doodle.layout.constraints.Strength.Companion.Required
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.Term
import io.nacular.doodle.layout.constraints.Variable
import io.nacular.doodle.layout.constraints.VariableTerm
import io.nacular.doodle.layout.constraints.impl.ConstraintLayoutImpl.Companion.solve
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
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.reflect.KMutableProperty0

@Suppress("PropertyName")
internal open class BoundsImpl2(private val target: Positionable2, bounds: Rectangle, private val context: ConstraintDslContext2): Bounds {
    private val term: ReflectionVariable.() -> Term = {
        VariableTerm(this)
    }

    var x_      = bounds.x
    var y_      = bounds.y
    var width_  = bounds.width
    var height_ = bounds.height

    override val top     by lazy { ReflectionVariable(target, ::y_,      id = yId,      term) }
    override val left    by lazy { ReflectionVariable(target, ::x_,      id = xId,      term) }
    override val width   by lazy { ReflectionVariable(target, ::width_,  id = widthId,  term) }
    override val height  by lazy { ReflectionVariable(target, ::height_, id = heightId, term) }

    override val right   by lazy { with(context) { left + width      } }
    override val centerX by lazy { with(context) { left + width  / 2 } }
    override val bottom  by lazy { with(context) { top  + height     } }
    override val centerY by lazy { with(context) { top  + height / 2 } }

    override val center  by lazy { Position(centerX, centerY) }
    override val edges   by lazy { with(context) { Edges(top + 0, left + 0, right, bottom) } }

    fun commit() {
        val size = Size(width_, height_)

        target.setBounds(Rectangle(Point(x_, y_), target.preferredSize(min = size, max = size)))
    }
}

internal class SimpleVariable(val value: KMutableProperty0<Double>): Property(), Variable {
    override val readOnly: Double get() = value()
    override fun toTerm() = VariableTerm(this)

    override val name: String = "$value"

    override fun invoke(): Double = value()

    override fun invoke(value: Double) {
        this.value.set(value)
    }

    override fun toString() = name
}

@Suppress("PropertyName")
internal open class ParentBoundsImpl2(private val context: ConstraintDslContext2, size: Size, var min: Size, var max: Size): ParentBounds2 {
    var width_  = size.width
    var height_ = size.height

    override val width  = SimpleVariable(::width_ )
    override val height = SimpleVariable(::height_)

    override val right   by lazy { with(context) { 0 + width      } }
    override val centerX by lazy { with(context) { 0 + width  / 2 } }
    override val bottom  by lazy { with(context) { 0 + height     } }
    override val centerY by lazy { with(context) { 0 + height / 2 } }

    override val center  by lazy { Position(centerX, centerY) }
    override val edges   by lazy { Edges(null, null, right, bottom) }
}

internal class ConstraintLayoutImpl2(view: View, vararg others: View, originalLambda: Any, block: ConstraintDslContext2.(List<Bounds>) -> Unit): ConstraintLayout2() {
    private var layingOut = false

    @Suppress("PrivatePropertyName")
    private val boundsChangeAttempted_ = ::boundsChangeAttempted

    @Suppress("PrivatePropertyName")
    private val boundsChanged_ = ::boundsChanged

    private val activeBounds  = mutableSetOf<ReflectionVariable>()
    private val updatedBounds = mutableSetOf<ReflectionVariable>()

    private fun boundsChanged(child: View, old: Rectangle, new: Rectangle) {
        if (layingOut) return

        viewBounds[child]?.let {
            it.x_  = new.x
            it.y_  = new.y

            if (old.y != new.y) updatedBounds += it.top
            if (old.x != new.x) updatedBounds += it.left
        }
    }

    private fun boundsChangeAttempted(view: View, old: Rectangle, new: Rectangle) {
        if (layingOut) return

        viewBounds[view]?.let {
            it.x_      = new.x
            it.y_      = new.y
            it.width_  = new.width
            it.height_ = new.height
//            var doLayout = false

            /*if (old.y      != new.y     )  */ updatedBounds += it.top
            /*if (old.x      != new.x     )  */ updatedBounds += it.left
            /*if (old.width  != new.width ) {*/ updatedBounds += it.width //;  doLayout = true }
            /*if (old.height != new.height) {*/ updatedBounds += it.height//; doLayout = true }

            if (old.size != new.size) {
                view.parent?.doLayout2_()
            }
        }
    }

    internal data class BlockInfo(val constraints: List<BoundsImpl2>, val block: ConstraintDslContext2.(List<Bounds>) -> Unit)

    private val solver       = Solver()
    private val blocks       = mutableListOf(block)
    private val context      = ConstraintDslContext2()
    private val blockTracker = mutableMapOf<Pair<List<View>, Any>, BlockInfo>()
    private val viewBounds   = mutableMapOf<View, BoundsImpl2>()

    override val exceptionThrown: Pool<(ConstraintLayout2, ConstraintException) -> Unit> = SetPool()

    init {
        constrain(listOf(view) + others, originalLambda, block)
    }

    override fun layout(views: Sequence<Positionable2>, min: Size, current: Size, max: Size): Size {
        layingOut = true

        with(context.parent_) {
            width_   = current.width
            height_  = current.height
            this.min = min
            this.max = max
        }

        setupSolver(solver, context, updatedBounds, blockTracker.values, ::notifyOfErrors)
        solve      (solver, activeBounds, updatedBounds, ::notifyOfErrors)

        blockTracker.values.asSequence().flatMap { it.constraints }.forEach {
            it.commit()
        }

        layingOut = false

        return Size(context.parent_.width_, context.parent_.height_)
    }

    override fun constrain  (view: View, constraints: ConstraintDslContext2.(Bounds) -> Unit) = constrain  (listOf(view), originalLambda = constraints) { (a) -> constraints(a) }
    override fun unconstrain(view: View, constraints: ConstraintDslContext2.(Bounds) -> Unit) = unconstrain(listOf(view), constraints)

    override fun constrain  (first: View, second: View, constraints: ConstraintDslContext2.(Bounds, Bounds) -> Unit) = constrain  (listOf(first, second), originalLambda = constraints) { (a, b) -> constraints(a, b) }
    override fun unconstrain(first: View, second: View, constraints: ConstraintDslContext2.(Bounds, Bounds) -> Unit) = unconstrain(listOf(first, second), constraints)

    override fun constrain  (first: View, second: View, third: View, constraints: ConstraintDslContext2.(Bounds, Bounds, Bounds) -> Unit) = constrain  (listOf(first, second, third), constraints) { (a, b, c) -> constraints(a, b, c) }
    override fun unconstrain(first: View, second: View, third: View, constraints: ConstraintDslContext2.(Bounds, Bounds, Bounds) -> Unit) = unconstrain(listOf(first, second, third), constraints)

    override fun constrain  (first: View, second: View, third: View, fourth: View, constraints: ConstraintDslContext2.(Bounds, Bounds, Bounds, Bounds) -> Unit) = constrain  (listOf(first, second, third, fourth), constraints) { (a, b, c, d) -> constraints(a, b, c, d) }
    override fun unconstrain(first: View, second: View, third: View, fourth: View, constraints: ConstraintDslContext2.(Bounds, Bounds, Bounds, Bounds) -> Unit) = unconstrain(listOf(first, second, third, fourth), constraints)

    override fun constrain  (first: View, second: View, third: View, fourth: View, fifth: View, constraints: ConstraintDslContext2.(Bounds, Bounds, Bounds, Bounds, Bounds) -> Unit) = constrain  (listOf(first, second, third, fourth, fifth), constraints) { (a, b, c, d, e) -> constraints(a, b, c, d, e) }
    override fun unconstrain(first: View, second: View, third: View, fourth: View, fifth: View, constraints: ConstraintDslContext2.(Bounds, Bounds, Bounds, Bounds, Bounds) -> Unit) = unconstrain(listOf(first, second, third, fourth, fifth), constraints)

    override fun constrain  (first: View, second: View, vararg others: View, constraints: ConstraintDslContext2.(List<Bounds>) -> Unit) = constrain  (listOf(first, second), originalLambda = constraints, constraints = constraints)
    override fun unconstrain(first: View, second: View, vararg others: View, constraints: ConstraintDslContext2.(List<Bounds>) -> Unit) = unconstrain(listOf(first, second) + others, constraints)

    private fun constrain(views: List<View>, originalLambda: Any, constraints: ConstraintDslContext2.(List<Bounds>) -> Unit): ConstraintLayout2 {
        val newConstraints  = constraints(views)
        blocks             += constraints
        blockTracker[views to originalLambda] = BlockInfo(newConstraints, constraints)

        return this
    }

    @Suppress("SpellCheckingInspection")
    private fun unconstrain(views: List<View>, constraintBlock: Any): ConstraintLayout2 {
        views.forEach {
            it.boundsChanged         -= boundsChanged_
            it.boundsChangeAttempted -= boundsChangeAttempted_

            it.resetConstraints()
        }

        blockTracker.remove(views to constraintBlock)?.let {
            blocks -= it.block
        }

        return this
    }

    private fun constraints(views: List<View>): List<BoundsImpl2> = views.map { view ->
        view.boundsChanged         += boundsChanged_
        view.boundsChangeAttempted += boundsChangeAttempted_

        BoundsImpl2(view.positionable2Wrapper, view.bounds, context).also {
            viewBounds[view] = it

            if (view.y != 0.0 || view.x != 0.0 || view.width  != 0.0 || view.height != 0.0) {
                updatedBounds += it.top
                updatedBounds += it.left
                updatedBounds += it.width
                updatedBounds += it.height
            }
        }
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
            insertedConstraint: Constraint,
            updatedBounds     : MutableSet<ReflectionVariable>,
            errorHandler      : (ConstraintException) -> Unit
        ) {
            try {
                solver.addConstraint(insertedConstraint)
            } catch (exception: ConstraintException) {
                errorHandler(exception)
            }

            insertedConstraint.expression.terms.map { it.variable }.toSet().forEach { variable ->
                if (variable.isSynthetic) {
                    // Add synthetic constraints that keep width and height positive
                    try {
                        solver.addConstraint(Constraint(Expression(VariableTerm(variable, 1.0)), GE, Required))
                    } catch (ignore: Exception) { }
                }

                val strength = when {
                    variable is ReflectionVariable && variable in updatedBounds -> Strength(100)
                    else                                                        -> Strength.Weak
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

        private val Variable.isSynthetic get() = name == "width_" || name == "height_"

        private fun handleDelete(solver: Solver, constraint: Constraint) {
            try {
                solver.removeConstraint(constraint)
            } catch (ignore: Exception) {}

            constraint.expression.terms.map { it.variable }.filter { it.isSynthetic }.toSet().forEach { variable ->
                // Remove synthetic constraints that keep width and height positive
                try {
                    solver.removeConstraint(Constraint(Expression(VariableTerm(variable, 1.0)), GE, Required))
                } catch (ignore: Exception) {}
            }
        }

        fun setupSolver(
            solver       : Solver,
            context      : ConstraintDslContext2,
            updatedBounds: MutableSet<ReflectionVariable> = mutableSetOf(),
            blocks       : Collection<BlockInfo>,
            errorHandler : (ConstraintException) -> Unit
        ) {
            val oldConstraints = context.constraints
            context.constraints = mutableListOf()

            with(context) {
                if (parent_.max == parent_.min) {
                    parent.width  eq parent_.max.width
                    parent.height eq parent_.max.height
                } else {
                    (parent.width  eq parent.width.readOnly ) .. Strong
                    (parent.height eq parent.height.readOnly) .. Strong

                    parent.width  greaterEq parent_.min.width
                    parent.height greaterEq parent_.min.height

                    if (parent_.max.width  < POSITIVE_INFINITY) { parent.width  lessEq parent_.max.width  }
                    if (parent_.max.height < POSITIVE_INFINITY) { parent.height lessEq parent_.max.height }
                }
            }

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
                                    handleInsert(solver, insertedConstraint, updatedBounds, errorHandler)
                                }
                            }.ifNull {
                                // handle insert
                                handleInsert(solver, insertedConstraint, updatedBounds, errorHandler)
                            }
                        }

                        previousDelete = null
                    }
                    else             -> {}
                }
            }

            handlePreviousDelete(solver, previousDelete)
        }

        fun solve(
            solver       : Solver,
            activeBounds : MutableSet<ReflectionVariable> = mutableSetOf(),
            updatedBounds: MutableSet<ReflectionVariable> = mutableSetOf(),
            errorHandler : (ConstraintException) -> Unit
        ) {
            activeBounds._removeAll { it !in updatedBounds }.forEach {
                try {
                    solver.removeEditVariable(it)
                } catch (ignore: Exception) {}

                try {
                    solver.addEditVariable(it, Strength.Weak)
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
