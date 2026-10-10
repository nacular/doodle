package io.nacular.doodle.layout.constraints.impl

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.View
import io.nacular.doodle.core.View.PositionableView
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Area
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.BoundsArea
import io.nacular.doodle.layout.constraints.Constraint
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.ConstraintException
import io.nacular.doodle.layout.constraints.ConstraintLayout
import io.nacular.doodle.layout.constraints.Edges
import io.nacular.doodle.layout.constraints.Expression
import io.nacular.doodle.layout.constraints.HeightProperty
import io.nacular.doodle.layout.constraints.ParentBounds
import io.nacular.doodle.layout.constraints.Position
import io.nacular.doodle.layout.constraints.Property
import io.nacular.doodle.layout.constraints.Strength
import io.nacular.doodle.layout.constraints.Strength.Companion.Required
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.UnsatisfiableConstraintException
import io.nacular.doodle.layout.constraints.Variable
import io.nacular.doodle.layout.constraints.VariableTerm
import io.nacular.doodle.layout.constraints.WidthProperty
import io.nacular.doodle.layout.constraints.impl.ReflectionVariable.Companion.HEIGHT_ID
import io.nacular.doodle.layout.constraints.impl.ReflectionVariable.Companion.WIDTH_ID
import io.nacular.doodle.layout.constraints.impl.ReflectionVariable.Companion.X_ID
import io.nacular.doodle.layout.constraints.impl.ReflectionVariable.Companion.Y_ID
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool
import kotlin.Double.Companion.POSITIVE_INFINITY

@Suppress("PrivatePropertyName")
internal open class BoundsImpl(private val target: Positionable, private val context: ConstraintDslContext, private val solver: Solver): Bounds, Layout {
    private inner class WidthPropertyImpl: WidthProperty() {
        private  val delegate       = ReflectionVariable(target, ReadWriteValue("width",  { width_  }, { width_  = it }), id = WIDTH_ID,  needsSynthetic = true)
        override val bounds   get() = this@BoundsImpl
        override val variable get() = delegate
        override val terms    get() = delegate.terms
        override val constant get() = delegate.constant

        override fun toTerm() = delegate.toTerm()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is WidthPropertyImpl) return false
            if (!super.equals(other)) return false

            if (delegate != other.delegate) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + delegate.hashCode()
            return result
        }
    }

    private inner class HeightPropertyImpl: HeightProperty() {
        private  val delegate       = ReflectionVariable(target, ReadWriteValue("height", { height_ }, { height_ = it }), id = HEIGHT_ID, needsSynthetic = true)
        override val bounds   get() = this@BoundsImpl
        override val variable get() = delegate
        override val terms    get() = delegate.terms
        override val constant get() = delegate.constant

        override fun toTerm() = delegate.toTerm()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is HeightPropertyImpl) return false
            if (!super.equals(other)) return false

            if (delegate != other.delegate) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + delegate.hashCode()
            return result
        }
    }

    private var x_      : Double = target.x
    private var y_      : Double = target.y
    private var width_  : Double = target.width
    private var height_ : Double = target.height

    override val top     = ReflectionVariable(target, ReadWriteValue("y",      { y_      }, { y_      = it }), id = Y_ID                            )
    override val left    = ReflectionVariable(target, ReadWriteValue("x",      { x_      }, { x_      = it }), id = X_ID                            )
    override val width   = WidthPropertyImpl () as WidthProperty
    override val height  = HeightPropertyImpl() as HeightProperty

    override val right   = with(context) { left + width      }
    override val bottom  = with(context) { top  + height     }
    override val centerX = with(context) { left + width  / 2 }
    override val centerY = with(context) { top  + height / 2 }

    override val position = Position(top, left)
    override val size     = BoundsArea(width, height)
    override val edges    = with(context) { Edges(top + 0, left + 0, right, bottom) }
    override val center   = Position(centerX, centerY)

    override val idealSize get() = target.idealSize

    override fun preferredSize(min: Size, max: Size) = target.preferredSize(min, max)

    private val ReflectionVariable.targeted get() = constrained || edited

    fun commit() {
        if (top.targeted ) target.y = y_
        if (left.targeted) target.x = x_

        when {
            width.variable.constrained -> {
                target.width = width_

                solver.computeRange(width.variable, min = 0.0).let {
                    target.minSize = Size(it.start,        target.minSize.height)
                    target.maxSize = Size(it.endInclusive, target.maxSize.height)
                }
            }
            width.variable.edited -> {
                val oldMin = target.minSize
                val oldMax = target.maxSize

                target.width = width_

                target.minSize = oldMin
                target.maxSize = oldMax
            }
        }

        when {
            height.variable.constrained -> {
                target.height = height_

                solver.computeRange(height.variable, min = 0.0).let {
                    target.minSize = Size(target.minSize.width, it.start       )
                    target.maxSize = Size(target.maxSize.width, it.endInclusive)
                }
            }
            height.variable.edited -> {
                val oldMin = target.minSize
                val oldMax = target.maxSize

                target.height = height_

                target.minSize = oldMin
                target.maxSize = oldMax
            }
        }
    }

    fun reset() {
        x_      = target.bounds.x
        y_      = target.bounds.y
        width_  = target.bounds.width
        height_ = target.bounds.height
    }

    override fun layout(
        views  : Sequence<Positionable>,
        min    : Size,
        current: Size,
        max    : Size,
        insets : Insets
    ): Size = Size.Empty
}

internal class ReadWriteValue(val name: String, private val getter: () -> Double, private val setter: (Double) -> Unit) {
    operator fun invoke() = getter()
    fun set(value: Double) { setter(value) }
}

internal open class ReflectionVariable(
             val target        : Positionable? = null,
    private  val delegate      : ReadWriteValue,
    private  val id            : Int, // ugly work-around for https://youtrack.jetbrains.com/issue/KT-15101
    override val needsSynthetic: Boolean = false,
    private  val mapper        : (Double) -> Double = { it }
): Property(), Variable {
    private val hashCode = 31 * (target?.hashCode() ?: 1) +id

    override val name     get() = delegate.name
    override val variable get() = this
    override var isReference    = false
    override var constrained    = false
    override var edited         = false

    override fun invoke() = delegate()

    override fun invoke(value: Double) {
        delegate.set(mapper(value))
    }

    override fun toTerm(): VariableTerm = VariableTerm(this)
    override val terms: Array<VariableTerm> = arrayOf(toTerm())
    override val constant = 0.0

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

    override fun hashCode() = hashCode

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

    override var insets = Insets.None

    fun update(size: Size, min: Size, max: Size, insets: Insets) {
        width_      = size.width
        height_     = size.height
        this.min    = min
        this.max    = max
        this.insets = insets
    }

    override val width   = ReflectionVariable(delegate = ReadWriteValue("width",  { width_  }, { width_  = it }), id = WIDTH_ID )
    override val height  = ReflectionVariable(delegate = ReadWriteValue("height", { height_ }, { height_ = it }), id = HEIGHT_ID)

    override val right   = with(context) { 0 + width      }
    override val centerX = with(context) { 0 + width  / 2 }
    override val bottom  = with(context) { 0 + height     }
    override val centerY = with(context) { 0 + height / 2 }

    override val center  = Position(centerX, centerY      )
    override val edges   = Edges(null, null, right, bottom)
    override val size    = Area (width, height            )
}

internal open class ConstraintLayoutImpl(
       views          : List<View>,
       originalLambda: Any,
       block         : ConstraintDslContext.(List<Bounds>) -> Unit
): ConstraintLayout() {
    internal constructor(
               view          : View,
        vararg others: View,
               originalLambda: Any,
               block         : ConstraintDslContext.(List<Bounds>) -> Unit
    ): this(listOf(view) + others, originalLambda = originalLambda, block = block)

    private var layingOut = false

    internal data class BlockInfo(val constraints: List<BoundsImpl>, val block: ConstraintDslContext.(List<Bounds>) -> Unit)

    private val solver       = Solver()
    private val blocks       = mutableListOf<ConstraintDslContext.(List<Bounds>) -> Unit>()
    private val context      = ConstraintDslContext(solver)
    private val blockTracker = mutableMapOf<Pair<List<View>, Any>, BlockInfo>()
    private val viewBounds   = mutableMapOf<View, BoundsImpl>()

    override val exceptionThrown: Pool<(ConstraintLayout, ConstraintException) -> Unit> = SetPool()

    init {
        if (views.isNotEmpty()) {
            constrain(views, originalLambda, block)
        }
    }

    override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets) = layoutInternal(
        views                    = views,
        min                      = min,
        current                  = current,
        max                      = max,
        insets                   = insets,
        parentWidthPreservation  = Required,
        parentHeightPreservation = Required
    )

    override fun preferredSize(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets) = layoutInternal(
        views                    = views,
        min                      = min,
        current                  = current,
        max                      = max,
        insets                   = insets,
        parentWidthPreservation  = parentWidthPreservation,
        parentHeightPreservation = parentHeightPreservation
    )

    private fun layoutInternal(
        @Suppress("unused") views  : Sequence<Positionable>,
                            min    : Size,
                            current: Size,
                            max    : Size,
                            insets : Insets,
                            parentWidthPreservation : Strength,
                            parentHeightPreservation: Strength
    ): Size {
        layingOut = true

        context.updateParent(current, min = min, max = max, insets = insets)

        viewBounds.values.forEach { it.reset() }

        setupSolver(solver, context, blockTracker.values, parentWidthPreservation, parentHeightPreservation, ::notifyOfErrors)
        solve      (solver, blockTracker.values.asSequence().flatMap { it.constraints })

        layingOut = false

        return Size(context.parent.width.value, context.parent.height.value)
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

        val remaining = blockTracker.flatMap { entry -> entry.key.first }.toSet()

        views.filter { it !in remaining }.forEach {
            viewBounds -= it

            it.resetConstraints()
        }

        return this
    }

    private fun constraints(views: List<View>): List<BoundsImpl> = views.map { view ->
        viewBounds.getOrPut(view) {
            BoundsImpl(view.positionable, context, solver)
        }
    }

    private fun notifyOfErrors(exception: ConstraintException) {
        (exceptionThrown as SetPool).forEach { it(this, exception) }
    }

    internal companion object {
        fun setupSolver(
            solver                  : Solver,
            context                 : ConstraintDslContext,
            blocks                  : Collection<BlockInfo>,
            parentWidthPreservation : Strength = Strong,
            parentHeightPreservation: Strength = Strong,
            errorHandler            : (ConstraintException) -> Unit
        ) {
            val oldConstraints = context.constraints
            val oldEdits       = context.edits
            val oldIdealEdits  = context.idealEdits

            context.reset()

            context.apply {
                (parent as ParentBoundsImpl).apply {
                    constrainParent(width, parentWidthPreservation, height, parentHeightPreservation, min, max)
                }
            }

            blocks.forEach {
                it.block(context, it.constraints)
            }

            context.pendingAdd?.let {
                context.constraints.add(it)
                context.pendingAdd = null
            }

            // TODO: use updateConstant for constraints that only changed by a constant factor

            for (constraint in oldConstraints) {
                if (constraint !in context.constraints) {
                    handleDelete(solver, constraint)
                }
            }

            val failedInserts = mutableListOf<Constraint>()

            for (constraint in context.constraints) {
                if (constraint !in oldConstraints) {
                    if (!handleInsert(solver, constraint, errorHandler)) {
                        failedInserts += constraint
                    }
                }
            }

            failedInserts.forEach {
                context.constraints -= it
            }

            for (edit in oldEdits) {
                if (edit !in context.edits) {
                    doTrying({}) {
                        solver.removeEditVariable(edit.property)
                    }
                }
            }

            for (edit in oldIdealEdits) {
                if (edit !in context.idealEdits) {
                    doTrying({}) {
                        solver.removeIdealEdit(edit)
                    }
                }
            }

            for (edit in context.edits) {
                when (edit) {
                    !in oldEdits -> doTrying(errorHandler) {
                        edit.property.edited = true
                        solver.addEditVariable(edit.property, edit.strength)
                        solver.suggestValue(edit.property, edit.value)
                    }
                    else -> doTrying(errorHandler) {
                        solver.suggestValue(edit.property, edit.value)
                    }
                }
            }

            for (edit in context.idealEdits) {
                if (edit !in oldIdealEdits) {
                    doTrying(errorHandler) {
                        edit.property.edited = true
                        solver.addIdealEdit(edit)
                    }
                }
            }
        }

        fun solve(solver: Solver, bounds: Sequence<BoundsImpl>) {
            solver.updateVariables()

            bounds.forEach { it.commit() }
        }

        private fun handleInsert(
            solver            : Solver,
            insertedConstraint: Constraint,
            errorHandler      : (ConstraintException) -> Unit = {}
        ): Boolean {
            try {
                val newVariables = insertedConstraint.expression.terms.filter { it.variable !in solver.variables }

                newVariables.filter { it.variable.constrained = true; it.variable.needsSynthetic }.forEach {
                    try {
                        // Add synthetic constraints that keep width and height positive
                        solver.addNegativeGuard(it.variable)
                    } catch (_: Exception) {}
                }

                solver.addConstraint(insertedConstraint)
            } catch (exception: UnsatisfiableConstraintException) {
                errorHandler(exception)
                cleanupSynthetics(solver, insertedConstraint)
                return false
            } catch (exception: ConstraintException) {
                errorHandler(exception)
                cleanupSynthetics(solver, insertedConstraint)
            }

            return true
        }

        private fun handleDelete(solver: Solver, constraint: Constraint) {
            try {
                solver.removeConstraint(constraint)
            } catch (_: Exception) {}

            cleanupSynthetics(solver, constraint)
        }

        private fun cleanupSynthetics(solver: Solver, constraint: Constraint) {
            constraint.expression.terms.asSequence().map { it.variable }.filter { it.needsSynthetic }.forEach {
                solver.cleanupNegativeGuard(it)
            }
        }

        private fun doTrying(errorHandler : (ConstraintException) -> Unit, block: () -> Unit) {
            try {
                block()
            } catch (exception: ConstraintException) {
                errorHandler(exception)
            }
        }

        private fun ConstraintDslContext.constrainParent(
            width         : Expression,
            widthStrength : Strength,
            height        : Expression,
            heightStrength: Strength,
            min           : Size,
            max           : Size
        ) {
            constrainSide(width,  min.width,  max.width,  widthStrength )
            constrainSide(height, min.height, max.height, heightStrength)
        }

        private fun ConstraintDslContext.constrainSide(
            property : Expression,
            minExtent: Double,
            maxExtent: Double,
            strength : Strength,
        ) {
            when (minExtent) {
                maxExtent -> property eq maxExtent
                else      -> {
                    property preserve strength

                    if (strength != Required) {
                        if (minExtent > 0.0              ) { property greaterEq minExtent }
                        if (maxExtent < POSITIVE_INFINITY) { property lessEq    maxExtent }
                    }
                }
            }
        }
    }
}
