package io.nacular.doodle.layout.constraints.impl

import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.PositionableExtended
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constraints.Bounds
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
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
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
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.reflect.KMutableProperty0

internal interface BoundsAttemptObserver {
    fun boundsChangeAttempted(view: View, old: Rectangle, new: Rectangle, relayout: Boolean)
}

@Suppress("PrivatePropertyName")
internal open class BoundsImpl(private val target: PositionableExtended, /*private val bounds: Rectangle,*/ private val context: ConstraintDslContext): Bounds {
    private var x__      = target.bounds.x
    private var y__      = target.bounds.y
    private var width__  = target.bounds.width
    private var height__ = target.bounds.height

    // This looks strange, cut it works b/c the Solver reads variables and only updates them after computing their new
    // values. Essentially, it never does read, ..., write, ..., read; only read, ..., read, write
    private var x_      get() = target.bounds.x;      set(value) { x__      = value }
    private var y_      get() = target.bounds.y;      set(value) { y__      = value }
    private var width_  get() = target.bounds.width;  set(value) { width__  = value }
    private var height_ get() = target.bounds.height; set(value) { height__ = value }

    override val top     by lazy { ReflectionVariable(target, ::y_,      id = yId                            ) }
    override val left    by lazy { ReflectionVariable(target, ::x_,      id = xId                            ) }
    override val width   by lazy { ReflectionVariable(target, ::width_,  id = widthId,  needsSynthetic = true) }
    override val height  by lazy { ReflectionVariable(target, ::height_, id = heightId, needsSynthetic = true) }

    override val right   by lazy { with(context) { left + width      } }
    override val bottom  by lazy { with(context) { top  + height     } }
    override val centerX by lazy { with(context) { left + width  / 2 } }
    override val centerY by lazy { with(context) { top  + height / 2 } }

    override val center  by lazy { Position(centerX, centerY) }
    override val edges   by lazy { with(context) { Edges(top + 0, left + 0, right, bottom) } }

    override val preferredSize get() = target.idealSize

    fun commit() {
        val widthChanged  = target.bounds.width  != width__
        val heightChanged = target.bounds.height != height__

        val minWidth  = if (widthChanged ) width__  else 0.0
        val maxWidth  = if (widthChanged ) width__  else POSITIVE_INFINITY
        val minHeight = if (heightChanged) height__ else 0.0
        val maxHeight = if (heightChanged) height__ else POSITIVE_INFINITY

        target.updateBounds(x__, y__, Size(minWidth, minHeight), Size(maxWidth, maxHeight))
    }
}

internal class ReflectionVariable(
             val target        : PositionableExtended? = null,
    private  val delegate      : KMutableProperty0<Double>,
    private  val id            : Int, // ugly work-around for https://youtrack.jetbrains.com/issue/KT-15101
    override val needsSynthetic: Boolean = false,
    private  val mapper        : (Double) -> Double = { it }
): Property(), Variable {
    @Suppress("PrivatePropertyName")
    private val hashCode_ by lazy {
        var result = target?.hashCode() ?: 1
        result = 31 * result + id
        result
    }

    override val name get() = delegate.name
    override fun invoke() = delegate()

    override val readOnly: Double get() = invoke()

    override fun invoke(value: Double) {
        println("$delegate -> $value")

        delegate.set(mapper(value))
    }

    override fun toTerm(): Term = VariableTerm(this)

    override fun toString() = "${(target as? View.PositionableView)?.let { it.view::class.simpleName }}.$name"

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

@Suppress("PropertyName")
internal open class ParentBoundsImpl(private val context: ConstraintDslContext, size: Size, val min: Size, val max: Size): ParentBounds {
    var width_  = size.width
    var height_ = size.height

    override val width   by lazy { ReflectionVariable(delegate = ::width_,  id = widthId ) }
    override val height  by lazy { ReflectionVariable(delegate = ::height_, id = heightId) }

    override val right   by lazy { with(context) { 0 + width      } }
    override val centerX by lazy { with(context) { 0 + width  / 2 } }
    override val bottom  by lazy { with(context) { 0 + height     } }
    override val centerY by lazy { with(context) { 0 + height / 2 } }

    override val center  by lazy { Position(centerX, centerY) }
    override val edges   by lazy { Edges(null, null, right, bottom) }
}

internal class ConstraintLayoutImpl(
    view: View,
    vararg others: View,
    originalLambda: Any,
    block: ConstraintDslContext.(List<Bounds>) -> Unit
): ConstraintLayout(), BoundsAttemptObserver {
    private var layingOut     = false
    private val activeBounds  = mutableSetOf<ReflectionVariable>()
    private val updatedBounds = mutableSetOf<ReflectionVariable>()

    override fun boundsChangeAttempted(view: View, old: Rectangle, new: Rectangle, relayout: Boolean) {
        if (layingOut) return

        viewBounds[view]?.let {
//            it.x_      = new.x
//            it.y_      = new.y
//            it.width_  = new.width
//            it.height_ = new.height

            updatedBounds += it.top
            updatedBounds += it.left
            updatedBounds += it.width
            updatedBounds += it.height

            if (old.size != new.size && relayout && view.displayed) {
                when (val p = view.parent) {
                    null -> view.display?.relayout()
                    else -> p.relayout_()
                }
            }
        }
    }

    internal data class BlockInfo(val constraints: List<BoundsImpl>, val block: ConstraintDslContext.(List<Bounds>) -> Unit)

    private val solver       = Solver()
    private val blocks       = mutableListOf(block)
    private val context      = ConstraintDslContext()
    private val blockTracker = mutableMapOf<Pair<List<View>, Any>, BlockInfo>()
    private val viewBounds   = mutableMapOf<View, BoundsImpl>()

    override val exceptionThrown: Pool<(ConstraintLayout, ConstraintException) -> Unit> = SetPool()

    init {
        constrain(listOf(view) + others, originalLambda, block)
    }

    override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size): Size {
        layingOut = true

        context.apply {
            parent = ParentBoundsImpl(this, size = current, min = min, max = max)
        }

        setupSolver(solver, context, updatedBounds, blockTracker.values, ::notifyOfErrors)
        solve      (solver, activeBounds, updatedBounds, ::notifyOfErrors)

//        println("${context.constraints.map { it.toString() }}")

        blockTracker.values.asSequence().flatMap { it.constraints }.forEach {
            it.commit()
        }

        layingOut = false

        return Size(context.parent.width.readOnly, context.parent.height.readOnly)
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

    @Suppress("SpellCheckingInspection")
    private fun unconstrain(views: List<View>, constraintBlock: Any): ConstraintLayout {
        views.forEach {
            --it.numBoundsAttemptObservers

            it.resetConstraints()
        }

        blockTracker.remove(views to constraintBlock)?.let {
            blocks -= it.block
        }

        return this
    }

    private fun constraints(views: List<View>): List<BoundsImpl> = views.map { view ->
        ++view.numBoundsAttemptObservers

        viewBounds.getOrPut(view) {
            BoundsImpl(view.positionable, context).also {
                viewBounds[view] = it

                if (view.y != 0.0 || view.x != 0.0 || view.width  != 0.0 || view.height != 0.0) {
                    updatedBounds += it.top
                    updatedBounds += it.left
                    updatedBounds += it.width
                    updatedBounds += it.height
                }
            }
        }
    }

    private fun notifyOfErrors(exception: ConstraintException) {
        (exceptionThrown as SetPool).forEach { it(this, exception) }
    }

    internal companion object {
        fun setupSolver(
            solver       : Solver,
            context      : ConstraintDslContext,
            updatedBounds: MutableSet<ReflectionVariable> = mutableSetOf(),
            blocks       : Collection<BlockInfo>,
            errorHandler : (ConstraintException) -> Unit
        ) {
            val oldConstraints = context.constraints
            context.constraints = mutableListOf()

            context.apply {
                val p = (parent as ParentBoundsImpl)

                if (p.max == p.min) {
                    parent.width  eq p.max.width
                    parent.height eq p.max.height
                } else {
                    // FIXME: Need to find a way to do this since updating a constraint that isn't REQUIRED leaks memory
                    (parent.width  eq parent.width.readOnly ) .. Strong
                    (parent.height eq parent.height.readOnly) .. Strong

                    parent.width  greaterEq p.min.width
                    parent.height greaterEq p.min.height

                    if (p.max.width  < POSITIVE_INFINITY) { parent.width  lessEq p.max.width  }
                    if (p.max.height < POSITIVE_INFINITY) { parent.height lessEq p.max.height }
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

            insertedConstraint.expression.terms.asSequence().map { it.variable }.forEach { variable ->

                if (variable.needsSynthetic) {
                    // Add synthetic constraints that keep width and height positive
                    try {
                        solver.addConstraint(ensureNonNegative(variable))
                    } catch (ignore: Exception) { }
                }

                val strength = when {
                    variable in updatedBounds -> Strength(100)
                    else                      -> Weak
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

            constraint.expression.terms.asSequence().map { it.variable }.filter { it.needsSynthetic }.forEach { variable ->
                // Remove synthetic constraints that keep width and height positive
                try {
                    solver.removeConstraint(ensureNonNegative(variable))
                } catch (ignore: Exception) {}
            }
        }

        private fun ensureNonNegative(variable: Variable) = Constraint(Expression(VariableTerm(variable, 1.0)), GE, Required)

        private fun handlePreviousDelete(solver: Solver, previousDelete: Delete<Constraint>?) {
            previousDelete?.let {
                it.items.forEach { constraint ->
                    handleDelete(solver, constraint)
                }
            }
        }
    }
}
