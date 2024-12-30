package io.nacular.doodle.layout.constraints.impl

import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.View
import io.nacular.doodle.core.View.PositionableView
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Area
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
import io.nacular.doodle.layout.constraints.Strength
import io.nacular.doodle.layout.constraints.Strength.Companion.Required
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.Strength.Companion.Weak
import io.nacular.doodle.layout.constraints.Term
import io.nacular.doodle.layout.constraints.UnsatisfiableConstraintException
import io.nacular.doodle.layout.constraints.Variable
import io.nacular.doodle.layout.constraints.VariableTerm
import io.nacular.doodle.layout.constraints.impl.ReflectionVariable.Companion.HEIGHT_ID
import io.nacular.doodle.layout.constraints.impl.ReflectionVariable.Companion.WIDTH_ID
import io.nacular.doodle.layout.constraints.impl.ReflectionVariable.Companion.X_ID
import io.nacular.doodle.layout.constraints.impl.ReflectionVariable.Companion.Y_ID
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.diff.Delete
import io.nacular.doodle.utils.diff.Insert
import io.nacular.doodle.utils.diff.compare
import io.nacular.doodle.utils.ifNull
import io.nacular.doodle.utils.removeAll
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.reflect.KMutableProperty0

internal interface BoundsAttemptObserver {
    fun boundsChangeAttempted(view: View, old: Rectangle, new: Rectangle, relayout: Boolean)
}

@Suppress("PrivatePropertyName")
internal open class BoundsImpl(private val target: Positionable, private val context: ConstraintDslContext): Bounds {
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

    val top_     by lazy { ReflectionVariable(target, ::y_,      id = Y_ID                            ) }
    val left_    by lazy { ReflectionVariable(target, ::x_,      id = X_ID                            ) }

    override val top     by lazy { with(context) { 0 + top_  } }
    override val left    by lazy { with(context) { 0 + left_ } }
    override val width   by lazy {                     ReflectionVariable(target, ::width_,  id = WIDTH_ID,  needsSynthetic = true) }
    override val height  by lazy {                     ReflectionVariable(target, ::height_, id = HEIGHT_ID, needsSynthetic = true) }

    override val right   by lazy { with(context) { left + width      } }
    override val bottom  by lazy { with(context) { top  + height     } }
    override val centerX by lazy { with(context) { left + width  / 2 } }
    override val centerY by lazy { with(context) { top  + height / 2 } }

    override val center  by lazy { Position(centerX, centerY) }
    override val edges   by lazy { with(context) { Edges(top   + 0, left   + 0, right, bottom) } }
    override val size    by lazy { with(context) { Area (width + 0, height + 0               ) } }

    override val preferredSize get() = target.idealSize

    fun commit() {
        val minWidth  = if (width.constrained ) width__  else 0.0
        val maxWidth  = if (width.constrained ) width__  else POSITIVE_INFINITY
        val minHeight = if (height.constrained) height__ else 0.0
        val maxHeight = if (height.constrained) height__ else POSITIVE_INFINITY

        when {
            !(width.constrained || height.constrained) -> target.updatePosition(x__, y__)
            else                                       -> target.updateBounds  (x__, y__, Size(minWidth, minHeight), Size(maxWidth, maxHeight)).let {
                width__  = it.width
                height__ = it.height
            }
        }
    }
}

internal class ReflectionVariable(
             val target        : Positionable? = null,
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

    override val name     get() = delegate.name
    override val readOnly get() = invoke()
    override var constrained = false

    override fun invoke() = delegate()

    override fun invoke(value: Double) {
        delegate.set(mapper(value))
    }

    override fun toTerm(): Term = VariableTerm(this)

    override fun toString(): String = when (target) {
        is PositionableView -> target.view.toString().takeIf { it != "[object Object]" } ?: target.view::class.simpleName ?: "??"
        null -> "parent"
        else -> target.toString()
    } + ".$name"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        when (other) {
            is ReflectionVariable -> {}
            else                  -> return false
        }

        if (target != other.target) return false
        if (id     != other.id    ) return false

        return true
    }

    override fun hashCode() = hashCode_

    companion object {
        const val X_ID      = 0
        const val Y_ID      = 1
        const val WIDTH_ID  = 2
        const val HEIGHT_ID = 3
    }
}

@Suppress("PropertyName")
internal open class ParentBoundsImpl(private val context: ConstraintDslContext): ParentBounds {
    var width_  = 0.0
    var height_ = 0.0

    var min = Size.Empty; private set
    var max = min;        private set

    fun update(size: Size, min: Size, max: Size) {
        width_  = size.width
        height_ = size.height
        this.min = min
        this.max = max
    }

    override val width   by lazy { with(context) { 0 + ReflectionVariable(delegate = ::width_,  id = WIDTH_ID ) } }
    override val height  by lazy { with(context) { 0 + ReflectionVariable(delegate = ::height_, id = HEIGHT_ID) } }

    override val right   by lazy { with(context) { 0 + width      } }
    override val centerX by lazy { with(context) { 0 + width  / 2 } }
    override val bottom  by lazy { with(context) { 0 + height     } }
    override val centerY by lazy { with(context) { 0 + height / 2 } }

    override val center  by lazy { Position(centerX, centerY) }
    override val edges   by lazy { Edges(null, null, right, bottom) }
    override val size    by lazy { with(context) { Area (width + 0, height + 0) } }
}

internal class ConstraintLayoutImpl(
    view: View,
    vararg others: View,
    originalLambda: Any,
    block: ConstraintDslContext.(List<Bounds>) -> Unit
): ConstraintLayout(), BoundsAttemptObserver {
    private var layingOut     = false
    private val activeBounds  = mutableMapOf<ReflectionVariable, Double>()
    private val updatedBounds = mutableMapOf<ReflectionVariable, Double>()

    override fun boundsChangeAttempted(view: View, old: Rectangle, new: Rectangle, relayout: Boolean) {
        if (layingOut) return

        viewBounds[view]?.let {
            updatedBounds[it.top_  ] = new.y
            updatedBounds[it.left_ ] = new.x
            updatedBounds[it.width ] = new.width
            updatedBounds[it.height] = new.height

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

    override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets): Size {
        layingOut = true

        context.updateParent(current, min = min, max = max)

        setupSolver(solver, context, updatedBounds, blockTracker.values, ::notifyOfErrors)
        solve      (solver, activeBounds, updatedBounds, ::notifyOfErrors)

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
            it.boundsChangeAttempted -= this
            viewBounds -= it

            it.resetConstraints()
        }

        blockTracker.remove(views to constraintBlock)?.let {
            blocks -= it.block
        }

        return this
    }

    private fun constraints(views: List<View>): List<BoundsImpl> = views.map { view ->
        view.boundsChangeAttempted += this

        viewBounds.getOrPut(view) {
            BoundsImpl(view.positionable, context).also { bounds ->
                viewBounds[view] = bounds

                view.positionable.bounds.let {
                    if (it.y != 0.0 || it.x != 0.0 || it.width != 0.0 || it.height != 0.0) {
                        updatedBounds[bounds.top_  ] = it.y
                        updatedBounds[bounds.left_ ] = it.x
                        updatedBounds[bounds.width ] = it.width
                        updatedBounds[bounds.height] = it.height
                    }
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
            updatedBounds: MutableMap<ReflectionVariable, Double> = mutableMapOf(),
            blocks       : Collection<BlockInfo>,
            errorHandler : (ConstraintException) -> Unit
        ) {
            val oldConstraints = context.constraints

            context.constraints.forEach { it.expression.terms.map { it.variable.constrained = false } }

            context.constraints = mutableListOf()

            context.apply {
                (parent as ParentBoundsImpl).apply {
                    constrainParent(width, height, min, max)
                }
            }

            blocks.forEach {
                it.block(context, it.constraints)
            }

            context.constraints.forEach { it.expression.terms.map { it.variable.constrained = true } }

            val failedInserts = mutableListOf<Constraint>()

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
                                    } catch (_: UnknownConstraintException) {}
                                } else {
                                    // handle delete
                                    handlePreviousDelete(solver, previousDelete).also { previousDelete = null }
                                    // handle insert
                                    if (!handleInsert(solver, insertedConstraint, updatedBounds) {}) {
                                        failedInserts += insertedConstraint
                                    }
                                }
                            }.ifNull {
                                // handle insert
                                if (!handleInsert(solver, insertedConstraint, updatedBounds) {}) {
                                    failedInserts += insertedConstraint
                                }
                            }
                        }

                        previousDelete = null
                    }
                    else             -> {}
                }
            }

            handlePreviousDelete(solver, previousDelete)

            // try again in case some items were unsatisfiable due to the order of delete vs insert
            failedInserts.forEach {
                handleInsert(solver, it, updatedBounds, errorHandler)
            }
        }

        fun solve(
            solver       : Solver,
            activeBounds : MutableMap<ReflectionVariable, Double> = mutableMapOf(),
            updatedBounds: MutableMap<ReflectionVariable, Double> = mutableMapOf(),
            errorHandler : (ConstraintException) -> Unit
        ) {
            activeBounds.removeAll { k,_ -> k !in updatedBounds }.forEach { (variable, value) ->
                try {
                    solver.removeEditVariable(variable)
                } catch (_: Exception) {}

                try {
                    solver.addEditVariable(variable, Weak)
                    solver.suggestValue(variable, value)
                } catch (_: Exception) {}
            }

            updatedBounds.filter { it.key !in activeBounds }.forEach { (variable,_) ->
                try {
                    solver.removeEditVariable(variable)
                } catch (_: Exception) {}

                try {
                    solver.addEditVariable(variable, Strength(100))
                } catch (_: Exception) {}
            }

            updatedBounds.forEach { (variable, value) ->
                try {
                    solver.suggestValue(variable, value)
                } catch (exception: ConstraintException) {
                    errorHandler(exception)
                } catch (_: UnknownEditVariableException) {}
            }

            activeBounds.putAll(updatedBounds)

            updatedBounds.clear()

            solver.updateVariables()

            // Hold anything that changes. These should all have Weak strength
            updatedBounds.filter { it.key !in activeBounds }.forEach { (variable, value) ->
                try {
                    solver.suggestValue(variable, value)
                } catch (_: Exception) {}
            }

            // Cleanup holds
            updatedBounds.clear()
        }

        private fun handleInsert(
            solver            : Solver,
            insertedConstraint: Constraint,
            updatedBounds     : MutableMap<ReflectionVariable, Double>,
            errorHandler      : (ConstraintException) -> Unit
        ): Boolean {
            try {
                solver.addConstraint(insertedConstraint)
                insertedConstraint.expression.terms.asSequence().map { it.variable }.forEach { variable ->

                    if (variable.needsSynthetic) {
                        // Add synthetic constraints that keep width and height positive
                        try {
                            solver.addConstraint(ensureNonNegative(variable))
                        } catch (_: Exception) { }
                    }

                    val strength = when {
                        variable in updatedBounds -> Strength(100)
                        else                      -> Weak
                    }

                    try {
                        solver.addEditVariable(variable, strength)
                    } catch (_: Exception) { }

                    if (variable() != 0.0) {
                        try {
                            solver.suggestValue(variable, variable())
                        } catch (exception: ConstraintException) {
                            errorHandler(exception)
                        } catch (_: UnknownEditVariableException) { }
                    }
                }
            } catch (exception: UnsatisfiableConstraintException) {
                errorHandler(exception)
                return false
            } catch (exception: ConstraintException) {
                errorHandler(exception)
            }

            return true
        }

        private fun handleDelete(solver: Solver, constraint: Constraint) {
            try {
                solver.removeConstraint(constraint)
            } catch (_: Exception) {}

            constraint.expression.terms.asSequence().map { it.variable }.filter { it.needsSynthetic }.forEach { variable ->
                // Remove synthetic constraints that keep width and height positive
                try {
                    solver.removeConstraint(ensureNonNegative(variable))
                } catch (_: Exception) {}
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

        private fun ConstraintDslContext.constrainParent(width: Expression, height: Expression, min: Size, max: Size) {
            constrainSide(width,  min.width,  max.width )
            constrainSide(height, min.height, max.height)
        }

        private fun ConstraintDslContext.constrainSide(property: Expression, minExtent: Double, maxExtent: Double) {
            when (minExtent) {
                maxExtent -> property eq maxExtent
                else      -> {
                    // FIXME: Need to find a way to do this since updating a constraint that isn't REQUIRED leaks memory
                    property eq        property.readOnly strength Strong
                    property greaterEq minExtent

                    if (maxExtent < POSITIVE_INFINITY) { property lessEq maxExtent }
                }
            }
        }
    }
}
