package io.nacular.doodle.layout.constraints

import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Operator.EQ
import io.nacular.doodle.layout.constraints.Operator.GE
import io.nacular.doodle.layout.constraints.Operator.LE
import io.nacular.doodle.layout.constraints.Strength.Companion.Required
import io.nacular.doodle.layout.constraints.impl.BoundsImpl
import io.nacular.doodle.layout.constraints.impl.ConstraintLayoutImpl
import io.nacular.doodle.layout.constraints.impl.ConstraintLayoutImpl.BlockInfo
import io.nacular.doodle.layout.constraints.impl.ConstraintLayoutImpl.Companion.setupSolver
import io.nacular.doodle.layout.constraints.impl.ConstraintLayoutImpl.Companion.solve
import io.nacular.doodle.layout.constraints.impl.ImmutableSizeBounds
import io.nacular.doodle.layout.constraints.impl.ReflectionVariable
import io.nacular.doodle.layout.constraints.impl.Solver
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.observable


public val fill  : (ConstraintDslContext.(Bounds) -> Unit) = { it.edges  eq parent.edges  }
public val center: (ConstraintDslContext.(Bounds) -> Unit) = { it.center eq parent.center }

public fun fill  (                strength: Strength           ): (ConstraintDslContext.(Bounds) -> Unit) = { (it.edges  eq parent.edges         ) .. strength }
public fun fill  (insets: Insets, strength: Strength = Required): (ConstraintDslContext.(Bounds) -> Unit) = { (it.edges  eq parent.edges + insets) .. strength }
public fun center(                strength: Strength           ): (ConstraintDslContext.(Bounds) -> Unit) = { (it.center eq parent.center        ) .. strength }

/**
 * A [Layout] that positions Views using a set of constraints. These layouts are created using
 * the [constrain] functions and follow the form:
 *
 * ```
 *
 * val layout = constrain(view1, view2) { v1, v2 ->
 *     v1.left  eq     10
 *     v1.width lessEq parent.width / 2
 *
 *     v2.edges eq     parent.edges - 10
 * }
 * ```
 */
public abstract class ConstraintLayout: Layout {
    /**
     * Add constraints for [view].
     *
     * @param view being constrained
     * @param constraints being applied
     * @return Layout with additional constraints
     */
    public abstract fun constrain(view: View, constraints: ConstraintDslContext.(Bounds) -> Unit): ConstraintLayout

    /**
     * Remove all constraints for [view] that were created by [constraints].
     *
     * @param view being unconstrained
     * @param constraints being removed
     * @return Layout with constraints removed
     */
    public abstract fun unconstrain(view: View, constraints: ConstraintDslContext.(Bounds) -> Unit): ConstraintLayout

    /**
     * Add constraints for 2 Views.
     *
     * @param first being constrained
     * @param second being constrained
     * @param constraints being applied
     * @return Layout with additional constraints
     */
    public abstract fun constrain(first: View, second: View, constraints: ConstraintDslContext.(Bounds, Bounds) -> Unit): ConstraintLayout

    /**
     * Remove all constraints for the given Views that were created by [constraints].
     *
     * @param first View being unconstrained
     * @param second View being unconstrained
     * @param constraints being removed
     * @return Layout with constraints removed
     */
    public abstract fun unconstrain(first: View, second: View, constraints: ConstraintDslContext.(Bounds, Bounds) -> Unit): ConstraintLayout

    /**
     * Add constraints for 3 Views.
     *
     * @param first View being constrained
     * @param second View being constrained
     * @param third View being constrained
     * @param constraints being applied
     * @return Layout with additional constraints
     */
    public abstract fun constrain(first: View, second: View, third: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds) -> Unit): ConstraintLayout

    /**
     * Remove all constraints for the given Views that were created by [constraints].
     *
     * @param first View being unconstrained
     * @param second View being unconstrained
     * @param third View being unconstrained
     * @param constraints being removed
     * @return Layout with constraints removed
     */
    public abstract fun unconstrain(first: View, second: View, third: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds) -> Unit): ConstraintLayout

    /**
     * Add constraints for 4 Views.
     *
     * @param first first View being constrained
     * @param second second View being constrained
     * @param third third View being constrained
     * @param fourth fourth View being constrained
     * @param constraints being applied
     * @return Layout with additional constraints
     */
    public abstract fun constrain(first: View, second: View, third: View, fourth: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds) -> Unit): ConstraintLayout

    /**
     * Remove all constraints for the given Views that were created by [constraints].
     *
     * @param first View being unconstrained
     * @param second View being unconstrained
     * @param third View being unconstrained
     * @param fourth View being unconstrained
     * @param constraints being removed
     * @return Layout with constraints removed
     */
    public abstract fun unconstrain(first: View, second: View, third: View, fourth: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds) -> Unit): ConstraintLayout

    /**
     * Add constraints for 5 Views.
     *
     * @param first View being constrained
     * @param second View being constrained
     * @param third View being constrained
     * @param fourth View being constrained
     * @param fifth View being constrained
     * @param constraints being applied
     * @return Layout with additional constraints
     */
    public abstract fun constrain(first: View, second: View, third: View, fourth: View, fifth: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds, Bounds) -> Unit): ConstraintLayout

    /**
     * Remove all constraints for the given Views that were created by [constraints].
     *
     * @param first View being unconstrained
     * @param second View being unconstrained
     * @param third View being unconstrained
     * @param fourth View being unconstrained
     * @param fifth View being unconstrained
     * @param constraints being removed
     * @return Layout with constraints removed
     */
    public abstract fun unconstrain(first: View, second: View, third: View, fourth: View, fifth: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds, Bounds) -> Unit): ConstraintLayout

    /**
     * Add constraints for several Views.
     *
     * @param first View being constrained
     * @param second View being constrained
     * @param others Views being constrained
     * @param constraints being applied
     * @return Layout with additional constraints
     */
    public abstract fun constrain(first: View, second: View, vararg others: View, constraints: ConstraintDslContext.(List<Bounds>) -> Unit): ConstraintLayout

    /**
     * Remove all constraints for the given Views that were created by [constraints].
     *
     * @param first View being unconstrained
     * @param second View being unconstrained
     * @param others being unconstrained
     * @param constraints being removed
     * @return Layout with constraints removed
     */
    public abstract fun unconstrain(first: View, second: View, vararg others: View, constraints: ConstraintDslContext.(List<Bounds>) -> Unit): ConstraintLayout

    /**
     * Notified whenever an unhandled [ConstraintException] is thrown during layout.
     */
    public abstract val exceptionThrown: Pool<(ConstraintLayout, ConstraintException) -> Unit>
}

/**
 * Simple value within a [Bounds] set.
 */
public abstract class Property internal constructor() {
    /**
     * Provides the Property's value directly, and does not treat it as
     * a variable when used in a [Constraint]. This means the Property won't be
     * altered to try and satisfy the constraint.
     */
    public abstract val readOnly: Double

    internal abstract fun toTerm(): Term
}

/**
 * 2-Dimensional value within a [Bounds] set.
 */
public class Position internal constructor(internal val left: Expression, internal val top: Expression) {
    /**
     * Provides the Position's value directly, and does not treat it as
     * a variable when used in a [Constraint]. This means the Position won't be
     * altered to try and satisfy the constraint.
     */
    public val readOnly: Point get() = Point(x = left.value, y = top.value)
}

/**
 * External boundaries of a constrained item.
 */
public class Edges internal constructor(
    internal val top   : Expression? = null,
    internal val left  : Expression? = null,
    internal val right : Expression,
    internal val bottom: Expression
) {
    /**
     * Provides the Edges' value directly, and does not treat it as
     * a variable when used in a [Constraint]. This means the Edges won't be
     * altered to try and satisfy the constraint.
     */
    public val readOnly: Rectangle get() {
        val x = left?.value ?: 0.0
        val y = top?.value  ?: 0.0
        return Rectangle(x = x, y = y, width = right.value - x, height = bottom.value - y)
    }
}

/**
 * Common bounding properties
 */
public interface BaseBounds {
    /** The rectangle's vertical extent */
    public val height: Property

    /** The rectangle's bottom edge */
    public val bottom: Expression

    /** The rectangle's vertical center */
    public val centerY: Expression

    /** The rectangle's horizontal extent */
    public val width: Property

    /** The rectangle's right side */
    public val right: Expression

    /** The rectangle's horizontal center */
    public val centerX: Expression

    /** The rectangle's 4 sides */
    public val edges: Edges

    /** The rectangle's center */
    public val center: Position
}

/**
 * [Bounds] the refer to the external, bounding rectangle for a View that is being constrained.
 */
public interface ParentBounds: BaseBounds

/**
 * The rectangular bounds for a View that is being constrained.
 */
public interface Bounds: BaseBounds {
    /** The rectangle's top edge */
    public val top: Property

    /** The rectangle's left edge */
    public val left: Property
}

/**
 * Any error that can be raised when applying constrains.
 */
public open class ConstraintException internal constructor(message: String?): Exception(message)

/**
 * Thrown when a duplicate [Constraint] is added.
 */
public class DuplicateConstraintException(public val constraint: Constraint): ConstraintException(constraint.toString())

/**
 * Indicates a [Constraint] is unsatisfiable.
 */
public class UnsatisfiableConstraintException(public val constraint: Constraint): ConstraintException(constraint.toString())

/**
 * Block within which constraints can be defined and captured.
 */
@Suppress("MemberVisibilityCanBePrivate", "NOTHING_TO_INLINE")
public open class ConstraintDslContext internal constructor() {
    /**
     * The common parent of all Views being constrained
     */
    @Suppress("PropertyName")
    internal var parent_: View? by observable(null) {_,_ ->
        constraints.clear()
    }

    public lateinit var parent: ParentBounds internal set

    internal var constraints = mutableListOf<Constraint>()

    private fun add(constraint: Constraint) = when {
        constraint.expression.isConstant -> Result.failure(UnsatisfiableConstraintException(constraint))
        else                             -> Result.success(constraint).also { constraints += constraint }
    }

    public operator fun Property.plus(expression: Expression): Expression = expression + this
    public operator fun Property.plus(term      : Term      ): Expression = term + this
    public operator fun Property.plus(other     : Property  ): Expression = this.toTerm() + other
    public operator fun Property.plus(constant  : Number    ): Expression = this.toTerm() + constant.toDouble()

    public operator fun Property.minus(expression: Expression): Expression = this + -expression
    public operator fun Property.minus(term      : Term      ): Expression = this + -term
    public operator fun Property.minus(other     : Property  ): Expression = this + -other
    public operator fun Property.minus(constant  : Number    ): Expression = this + -constant.toDouble()

    public operator fun Property.times     (coefficient: Number): Term = this.toTerm() * coefficient.toDouble()
    public operator fun Property.div       (denominator: Number): Term = this * 1.0 / denominator
    public operator fun Property.unaryMinus(                   ): Term = this * -1.0

    /**
     * Creates a [Constraint] that keeps the Property's current value. This is equivalent to:
     *
     * ```
     * this eq this.readOnly
     * ```
     */
    public val Property.preserve: Result<Constraint> get() = this eq this.readOnly

    public        fun min(a: Property, b: Property  ): Term       = min(a.toTerm(), b.toTerm())
    public inline fun min(a: Property, b: Term      ): Term       = min(1 * a, b)
    public inline fun min(a: Property, b: Expression): Expression = min(1 * a, b)
    public        fun min(a: Property, b: Number    ): Expression = min(1 * a, Expression(constant = b.toDouble()))

    public        fun max(a: Property, b: Property  ): Term       = max(a.toTerm(), b.toTerm())
    public inline fun max(a: Property, b: Term      ): Term       = max(1 * a, b)
    public inline fun max(a: Property, b: Expression): Expression = max(1 * a, b)
    public        fun max(a: Property, b: Number    ): Expression = max(1 * a, Expression(constant = b.toDouble()))

    public operator fun Term.plus(expression: Expression): Expression = expression + this
    public operator fun Term.plus(term      : Term      ): Expression = Expression(this, term)
    public operator fun Term.plus(property  : Property  ): Expression = this + property.toTerm()
    public operator fun Term.plus(value     : Number    ): Expression = Expression(this, constant = value.toDouble())

    public operator fun Term.minus(expression: Expression): Expression = -expression + this
    public operator fun Term.minus(term      : Term      ): Expression = this + -term
    public operator fun Term.minus(property  : Property  ): Expression = this + -property
    public operator fun Term.minus(constant  : Number    ): Expression = this + -constant.toDouble()

    public operator fun Term.div       (denominator: Number): Term = this * (1.0 / denominator.toDouble())
    public operator fun Term.unaryMinus(                   ): Term = this * -1.0

    public inline fun min(a: Term, b: Property  ): Term       = min(a, 1 * b)
    public inline fun min(a: Term, b: Expression): Expression = min(a + 0, b)
    public        fun min(a: Term, b: Number    ): Expression = min(a, Expression(constant = b.toDouble()))

    public inline fun max(a: Term, b: Property  ): Term       = max(a, 1 * b)
    public inline fun max(a: Term, b: Expression): Expression = max(a + 0, b)
    public        fun max(a: Term, b: Number    ): Expression = max(a, Expression(constant = b.toDouble()))

    public operator fun Expression.plus(other   : Expression): Expression = Expression(*terms, *other.terms, constant = constant + other.constant)
    public operator fun Expression.plus(term    : Term      ): Expression = Expression(*terms, term,         constant = constant                 )
    public operator fun Expression.plus(property: Property  ): Expression = this + property.toTerm()
    public operator fun Expression.plus(constant: Number    ): Expression = Expression(*terms, constant = this.constant + constant.toDouble())

    public operator fun Expression.minus(other   : Expression): Expression = this + -other
    public operator fun Expression.minus(term    : Term      ): Expression = this + -term
    public operator fun Expression.minus(property: Property  ): Expression = this + -property
    public operator fun Expression.minus(value   : Number    ): Expression = this + -value.toDouble()

    public operator fun Expression.times(coefficient: Number    ): Expression = Expression(*Array(terms.size) { terms[it] * coefficient.toDouble() }, constant = constant * coefficient.toDouble()) // TODO Do we need to make a copy of the term objects in the array?
    public operator fun Expression.times(other      : Expression): Expression = when {
        isConstant       -> constant       * other
        other.isConstant -> other.constant * this
        else             -> throw NonlinearExpressionException()
    }

    public operator fun Expression.div  (denominator: Number    ): Expression = this * (1.0 / denominator.toDouble())
    public operator fun Expression.div  (other      : Expression): Expression = when {
        other.isConstant -> this / other.constant
        else             -> throw NonlinearExpressionException()
    }

    public operator fun Expression.unaryMinus(): Expression = this * -1.0

    public fun min(a: Term, b: Term): Term = when {
        a.value < b.value -> a
        else              -> b
    }

    public fun min(a: Expression, b: Expression): Expression = when {
        a.value < b.value -> a
        else              -> b
    }

    public inline fun min(a: Expression, b: Term    ): Expression = min(a, b + 0)
    public inline fun min(a: Expression, b: Property): Expression = min(a, 1 * b)
    public        fun min(a: Expression, b: Number  ): Expression = min(a, Expression(constant = b.toDouble()))

    public fun max(a: Term, b: Term): Term = when {
        a.value > b.value -> a
        else              -> b
    }

    public fun max(a: Expression, b: Expression): Expression = when {
        a.value > b.value -> a
        else              -> b
    }

    public inline fun max(a: Expression, b: Term    ): Expression = max(a, b + 0)
    public inline fun max(a: Expression, b: Property): Expression = max(a, 1 * b)
    public        fun max(a: Expression, b: Number  ): Expression = max(a, Expression(constant = b.toDouble()))

    public operator fun Number.plus(expression: Expression): Expression = expression + this.toDouble()
    public operator fun Number.plus(term      : Term      ): Expression = term       + this.toDouble()
    public operator fun Number.plus(variable  : Property  ): Expression = variable   + this.toDouble()

    public operator fun Number.minus(expression: Expression): Expression = -expression + this.toDouble()
    public operator fun Number.minus(term      : Term      ): Expression = -term       + this.toDouble()
    public operator fun Number.minus(variable  : Property  ): Expression = -variable   + this.toDouble()

    public operator fun Number.times(expression: Expression): Expression = expression * this.toDouble()
    public operator fun Number.times(term      : Term      ): Term       = term       * this.toDouble()
    public operator fun Number.times(variable  : Property  ): Term       = variable   * this.toDouble()

    public inline fun min(a: Number, b: Property  ): Expression = min(b, a)
    public inline fun min(a: Number, b: Term      ): Expression = min(b, a)
    public inline fun min(a: Number, b: Expression): Expression = min(b, a)

    public inline fun max(a: Number, b: Property  ): Expression = max(b, a)
    public inline fun max(a: Number, b: Term      ): Expression = max(b, a)
    public inline fun max(a: Number, b: Expression): Expression = max(b, a)

    public class NonlinearExpressionException: Exception()

    public infix fun Expression.eq(other   : Expression): Result<Constraint> = add(Constraint(this - other, EQ, Required))
    public infix fun Expression.eq(term    : Term      ): Result<Constraint> = this eq Expression(term)
    public infix fun Expression.eq(property: Property  ): Result<Constraint> = this eq property.toTerm()
    public infix fun Expression.eq(constant: Number    ): Result<Constraint> = this eq Expression(constant = constant.toDouble())

    public infix fun Expression.lessEq(second  : Expression): Result<Constraint> = add(Constraint(this - second, LE, Required))
    public infix fun Expression.lessEq(term    : Term      ): Result<Constraint> = this lessEq Expression(term)
    public infix fun Expression.lessEq(property: Property  ): Result<Constraint> = this lessEq property.toTerm()
    public infix fun Expression.lessEq(constant: Number    ): Result<Constraint> = this lessEq Expression(constant = constant.toDouble())

    public infix fun Expression.greaterEq(second  : Expression): Result<Constraint> = add(Constraint(this - second, GE, Required))
    public infix fun Expression.greaterEq(term    : Term      ): Result<Constraint> = this greaterEq Expression(term)
    public infix fun Expression.greaterEq(property: Property  ): Result<Constraint> = this greaterEq property.toTerm()
    public infix fun Expression.greaterEq(constant: Number    ): Result<Constraint> = this greaterEq Expression(constant = constant.toDouble())

    public infix fun Term.eq(expression: Expression): Result<Constraint> = expression       eq this
    public infix fun Term.eq(term      : Term      ): Result<Constraint> = Expression(this) eq term
    public infix fun Term.eq(variable  : Property  ): Result<Constraint> = Expression(this) eq variable
    public infix fun Term.eq(constant  : Number    ): Result<Constraint> = Expression(this) eq constant.toDouble()

    public infix fun Term.lessEq(expression: Expression): Result<Constraint> = Expression(this) lessEq expression
    public infix fun Term.lessEq(term      : Term      ): Result<Constraint> = Expression(this) lessEq term
    public infix fun Term.lessEq(property  : Property  ): Result<Constraint> = Expression(this) lessEq property
    public infix fun Term.lessEq(constant  : Number    ): Result<Constraint> = Expression(this) lessEq constant.toDouble()

    public infix fun Term.greaterEq(expression: Expression): Result<Constraint> = Expression(this) greaterEq expression
    public infix fun Term.greaterEq(second    : Term      ): Result<Constraint> = Expression(this) greaterEq second
    public infix fun Term.greaterEq(property  : Property  ): Result<Constraint> = Expression(this) greaterEq property
    public infix fun Term.greaterEq(constant  : Number    ): Result<Constraint> = Expression(this) greaterEq constant.toDouble()

    public infix fun Property.eq(expression: Expression): Result<Constraint> = expression eq this
    public infix fun Property.eq(term      : Term      ): Result<Constraint> = term       eq this
    public infix fun Property.eq(property  : Property  ): Result<Constraint> = this.toTerm() eq property
    public infix fun Property.eq(constant  : Number    ): Result<Constraint> = this.toTerm() eq constant.toDouble()

    public infix fun Property.lessEq(expression: Expression): Result<Constraint> = this.toTerm() lessEq expression
    public infix fun Property.lessEq(term      : Term      ): Result<Constraint> = this.toTerm() lessEq term
    public infix fun Property.lessEq(second    : Property  ): Result<Constraint> = this.toTerm() lessEq second
    public infix fun Property.lessEq(constant  : Number    ): Result<Constraint> = this.toTerm() lessEq constant.toDouble()

    public infix fun Property.greaterEq(expression: Expression): Result<Constraint> = this.toTerm() greaterEq expression
    public infix fun Property.greaterEq(term      : Term      ): Result<Constraint> = this.toTerm() greaterEq term
    public infix fun Property.greaterEq(second    : Property  ): Result<Constraint> = this.toTerm() greaterEq second
    public infix fun Property.greaterEq(constant  : Number    ): Result<Constraint> = this.toTerm() greaterEq constant.toDouble()

    public infix fun Position.eq(other: Position): List<Result<Constraint>> = listOf(
        top  eq other.top,
        left eq other.left
    )

    public infix fun Position.eq(point: Point): List<Result<Constraint>> = listOf(
        top  eq point.y,
        left eq point.x
    )

    public operator fun Edges.plus (value: Number): Edges = this + Insets(-(value.toDouble()))
    public operator fun Edges.minus(value: Number): Edges = this + Insets(  value.toDouble() )

    public operator fun Edges.plus (insets: Insets): Edges = Edges(
        top?.plus (insets.top ) ?: Expression(constant = insets.top ),
        left?.plus(insets.left) ?: Expression(constant = insets.left),
        right  - insets.right,
        bottom - insets.bottom
    )

    public infix fun Edges.eq(other: Edges): List<Result<Constraint>> {
        val result = mutableListOf<Result<Constraint>>()

        if (top != null) {
            when (other.top) {
                null -> (top eq 0        ).also { result += it }
                else -> (top eq other.top).also { result += it }
            }
        }
        if (left != null) {
            when (other.left) {
                null -> (left eq 0         ).also { result += it }
                else -> (left eq other.left).also { result += it }
            }
        }

        (right  eq other.right ).also { result += it }
        (bottom eq other.bottom).also { result += it }

        return result
    }

    public infix fun Edges.eq(rectangle: Rectangle): List<Result<Constraint>> {
        val result = mutableListOf<Result<Constraint>>()

        if (top != null) {
            (top eq rectangle.y).also { result += it }
        }
        if (left != null) {
            (left eq rectangle.x).also { result += it }
        }

        (right  eq rectangle.right ).also { result += it }
        (bottom eq rectangle.bottom).also { result += it }

        return result
    }

    public inline infix fun Number.eq(expression: Expression): Result<Constraint> = expression eq this
    public inline infix fun Number.eq(term      : Term      ): Result<Constraint> = term       eq this
    public inline infix fun Number.eq(variable  : Property  ): Result<Constraint> = variable   eq this

    public infix fun Number.lessEq(expression: Expression): Result<Constraint> = Expression(constant = this.toDouble()) lessEq expression
    public infix fun Number.lessEq(term      : Term      ): Result<Constraint> = this                                   lessEq Expression(term)
    public infix fun Number.lessEq(property  : Property  ): Result<Constraint> = this                                   lessEq property.toTerm()

    public inline infix fun Number.greaterEq(expression: Expression): Result<Constraint> = expression                             lessEq    this
    public        infix fun Number.greaterEq(term    : Term        ): Result<Constraint> = Expression(constant = this.toDouble()) greaterEq term
    public        infix fun Number.greaterEq(property: Property    ): Result<Constraint> = this                                   greaterEq property.toTerm()

    public operator fun Result<Constraint>.rangeTo(strength: Strength): Result<Constraint> {
        this.getOrNull()?.strength = strength
        return this
    }

    public operator fun List<Result<Constraint>>.rangeTo(strength: Strength): List<Result<Constraint>> {
        this.forEach { it.getOrNull()?.strength = strength }
        return this
    }

    public fun Bounds.withOffset(top: Double? = null, left: Double? = null): Bounds = when {
        top == 0.0 && left == 0.0 -> this
        else                      -> withOffset(top = top?.let { OffsetTransformer(it) }, left = left?.let { OffsetTransformer(it) })
    }

    private fun Bounds.withOffset(
        top : Transformer<Double>? = null,
        left: Transformer<Double>? = null,
    ): Bounds = object: Bounds by this {
        override val top  = adapter(this@withOffset.top  as ReflectionVariable, top )
        override val left = adapter(this@withOffset.left as ReflectionVariable, left)

        val t = this.top
        val l = this.left

        override val right   by lazy { with(this@ConstraintDslContext) { l + width      } }
        override val centerX by lazy { with(this@ConstraintDslContext) { l + width  / 2 } }
        override val bottom  by lazy { with(this@ConstraintDslContext) { t + height     } }
        override val centerY by lazy { with(this@ConstraintDslContext) { t + height / 2 } }

        override val center  by lazy { with(this@ConstraintDslContext) { Position(left = centerX, top = centerY) } }
        override val edges   by lazy { with(this@ConstraintDslContext) { Edges(top = t + 0, left = l + 0, right = right, bottom = bottom) } }
    }
}

internal interface Transformer<T> {
    fun set(value: T): T
    fun get(value: T): T
}

public class OffsetTransformer(private val offset: Double): Transformer<Double> {
    public override fun set(value: Double): Double = value + offset
    public override fun get(value: Double): Double = value - offset

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OffsetTransformer) return false

        if (offset != other.offset) return false

        return true
    }

    override fun toString(): String = "+/- $offset"

    override fun hashCode(): Int = offset.hashCode()
}

internal class PropertyWrapper(
    internal val variable   : ReflectionVariable,
    private  val transformer: Transformer<Double>,
): Property(), Variable {
    private val isConst      by lazy { variableTerm is ConstTerm }
    private val variableTerm by lazy { variable.toTerm()         }

    override val name     get() = variable.name
    override val readOnly get() = invoke()

    override fun toTerm() = when {
        isConst -> ConstTerm   (this, variableTerm.coefficient)
        else    -> VariableTerm(this, variableTerm.coefficient)
    }
    override fun invoke  (             ) = transformer.get(variable())
    override fun invoke  (value: Double) = variable(transformer.set(value))
    override fun equals  (other: Any?  ) = variable == other
    override fun hashCode(             ) = variable.hashCode()
    override fun toString(             ) = "$variable [$transformer]"
}

internal class ConstPropertyWrapper(
    internal val variable   : ReflectionVariable,
    private  val transformer: Transformer<Double>,
): Property(), Variable {
    override val name     get() = variable.name
    override val readOnly get() = invoke()

    override fun toTerm  (             ) = ConstTerm(this, variable.toTerm().coefficient)
    override fun invoke  (             ) = transformer.get(variable())
    override fun invoke  (value: Double) = variable(transformer.set(value))
    override fun toString(             ) = "$variable [$transformer]"
}

private fun adapter(variable: ReflectionVariable, transformer: Transformer<Double>?, forceConst: Boolean = false): Property = transformer?.let {
    if (forceConst) ConstPropertyWrapper(variable, transformer) else PropertyWrapper(variable, transformer)
} ?: variable

public fun ConstraintDslContext.withSizeInsets(
    width : Double? = null,
    height: Double? = null,
    block : ConstraintDslContext.() -> Unit
): Unit = when {
    width == 0.0 && height == 0.0 -> block(this)
    else                          -> this@withSizeInsets.withSizeInsets(width = width?.let { OffsetTransformer(it) }, height = height?.let { OffsetTransformer(it) }, block)
}

private fun ConstraintDslContext.withSizeInsets(
    width : Transformer<Double>? = null,
    height: Transformer<Double>? = null,
    block : ConstraintDslContext.() -> Unit
) {
    this.apply {
        val oldParent = parent
        parent = object: ParentBounds {
            override val width = adapter(this@withSizeInsets.parent.width as ReflectionVariable, width, forceConst = true)
            override val right   by lazy { 0 + this.width }
            override val centerX by lazy { 0 + this.width / 2 }

            override val height = adapter(this@withSizeInsets.parent.height as ReflectionVariable, height, forceConst = true)
            override val bottom  by lazy { 0 + this.height }
            override val centerY by lazy { 0 + this.height / 2 }

            override val center by lazy { Position(left = centerX, top = centerY) }
            override val edges  by lazy { Edges(right = right, bottom = bottom) }
        }

        block(this)

        parent = oldParent
    }
}

/**
 * Creates a [ConstraintLayout] that constrains a single View.
 *
 * @param a View being constrained
 * @param constraints with constraint details
 * @return Layout that constrains the given view
 */
public fun constrain(a: View, constraints: ConstraintDslContext.(Bounds) -> Unit): ConstraintLayout = ConstraintLayoutImpl(a, originalLambda = constraints) { (a) -> constraints(a) }

/**
 * Creates a [ConstraintLayout] that constrains 2 Views.
 *
 * @param a first View being constrained
 * @param b second View being constrained
 * @param constraints with constraint details
 * @return Layout that constrains the given views
 */
public fun constrain(a: View, b: View, constraints: ConstraintDslContext.(Bounds, Bounds) -> Unit): ConstraintLayout = ConstraintLayoutImpl(a, b, originalLambda = constraints) { (a, b) -> constraints(a, b) }

/**
 * Creates a [ConstraintLayout] that constrains 3 Views.
 *
 * @param a first View being constrained
 * @param b second View being constrained
 * @param c third View being constrained
 * @param constraints with constraint details
 * @return Layout that constrains the given views
 */
public fun constrain(a: View, b: View, c: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds) -> Unit): ConstraintLayout = ConstraintLayoutImpl(a, b, c, originalLambda = constraints) { (a, b, c) -> constraints(a, b, c) }

/**
 * Creates a [ConstraintLayout] that constrains 4 Views.
 *
 * @param a first View being constrained
 * @param b second View being constrained
 * @param c third View being constrained
 * @param d fourth View being constrained
 * @param constraints with constraint details
 * @return Layout that constrains the given views
 */
public fun constrain(a: View, b: View, c: View, d: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds) -> Unit): ConstraintLayout = ConstraintLayoutImpl(a, b, c, d, originalLambda = constraints) { (a, b, c, d) -> constraints(a, b, c, d) }

/**
 * Creates a [ConstraintLayout] that constrains 5 Views.
 *
 * @param a first View being constrained
 * @param b second View being constrained
 * @param c third View being constrained
 * @param d fourth View being constrained
 * @param e fifth View being constrained
 * @param constraints with constraint details
 * @return Layout that constrains the given views
 */
public fun constrain(a: View, b: View, c: View, d: View, e: View, constraints: ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds, Bounds) -> Unit): ConstraintLayout = ConstraintLayoutImpl(a, b, c, d, e, originalLambda = constraints) { (a, b, c, d, e) -> constraints(a, b, c, d, e) }

/**
 * Creates a [ConstraintLayout] that constrains several Views.
 *
 * @param a first View being constrained
 * @param b remaining Views being constrained
 * @param constraints with constraint details
 * @return Layout that constrains the given views
 */
public fun constrain(a: View, b: View, vararg others: View, constraints: ConstraintDslContext.(List<Bounds>) -> Unit): ConstraintLayout = ConstraintLayoutImpl(a, *listOf(b, *others).toTypedArray(), originalLambda = constraints, block = constraints)

/**
 * Applies the given constraints to the View as though the View's parent were the given rectangle.
 *
 * @param view being constrained
 * @param within this rectangle
 * @param constraints to be applied
 * @throws ConstraintException
 */
//@kotlin.contracts.ExperimentalContracts
@Internal
public fun constrain(view: View, within: Rectangle, constraints: ConstraintDslContext.(Bounds) -> Unit) {
//    contract {
//        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
//    }

    val solver  = Solver()
    val context = ConstraintDslContext()

    context.parent  = ImmutableSizeBounds(width_ = within::width, height_ = within::height, context)
    context.parent_ = view.parent

    setupSolver(solver, context, blocks = listOf(BlockInfo(listOf(BoundsImpl(view, context))) { (a) -> constraints(a) })) { throw  it }
    solve(solver, context) { throw  it }

    view.position += within.position
}