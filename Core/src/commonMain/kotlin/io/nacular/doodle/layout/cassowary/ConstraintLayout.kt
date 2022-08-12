package io.nacular.doodle.layout.cassowary

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.cassowary.Operator.EQ
import io.nacular.doodle.layout.cassowary.Operator.GE
import io.nacular.doodle.layout.cassowary.Operator.LE
import io.nacular.doodle.layout.cassowary.Strength.Companion.Required
import io.nacular.doodle.layout.cassowary.impl.ConstraintLayoutImpl
import io.nacular.doodle.layout.cassowary.impl.ConstraintsImpl
import io.nacular.doodle.layout.cassowary.impl.RectangleConstraints
import kotlin.math.max

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
     * Add constraints for a single View.
     *
     * @param a View being constrained
     * @param block with constraint details
     * @return Layout with additional constraints
     */
    public abstract fun constrain(a: View, block: ConstraintDslContext.(Constraints) -> Unit): ConstraintLayout

    /**
     * Add constraints for 2 Views.
     *
     * @param a first View being constrained
     * @param b second View being constrained
     * @param block with constraint details
     * @return Layout with additional constraints
     */
    public fun constrain(a: View, b: View, block: ConstraintDslContext.(Constraints, Constraints) -> Unit): ConstraintLayout = constrain(a, b, others = emptyArray()) { (a, b) -> block(a, b) }

    /**
     * Add constraints for 3 Views.
     *
     * @param a first View being constrained
     * @param b second View being constrained
     * @param c third View being constrained
     * @param block with constraint details
     * @return Layout with additional constraints
     */
    public fun constrain(a: View, b: View, c: View, block: ConstraintDslContext.(Constraints, Constraints, Constraints) -> Unit): ConstraintLayout = constrain(a, b, c) { (a, b, c) -> block(a, b, c) }

    /**
     * Add constraints for 4 Views.
     *
     * @param a first View being constrained
     * @param b second View being constrained
     * @param c third View being constrained
     * @param d fourth View being constrained
     * @param block with constraint details
     * @return Layout with additional constraints
     */
    public fun constrain(a: View, b: View, c: View, d: View, block: ConstraintDslContext.(Constraints, Constraints, Constraints, Constraints) -> Unit): ConstraintLayout = constrain(a, b, c, d) { (a, b, c, d) -> block(a, b, c, d) }

    /**
     * Add constraints for 5 Views.
     *
     * @param a first View being constrained
     * @param b second View being constrained
     * @param c third View being constrained
     * @param d fourth View being constrained
     * @param e fifth View being constrained
     * @param block with constraint details
     * @return Layout with additional constraints
     */
    public fun constrain(a: View, b: View, c: View, d: View, e: View, block: ConstraintDslContext.(Constraints, Constraints, Constraints, Constraints, Constraints) -> Unit): ConstraintLayout = constrain(a, b, c, d, e) { (a, b, c, d, e) -> block(a, b, c, d, e) }

    /**
     * Add constraints for several Views.
     *
     * @param a first View being constrained
     * @param b remaining View being constrained
     * @param block with constraint details
     * @return Layout with additional constraints
     */
    public abstract fun constrain(a: View, b: View, vararg others: View, block: ConstraintDslContext.(List<Constraints>) -> Unit): ConstraintLayout

    /**
     * Remove all constrains that refer to a given View.
     *
     * @param view being removed
     * @return Layout with constraints removed
     */
    public abstract fun unconstrain(view: View): ConstraintLayout

    /**
     * Adds a single constraint
     *
     * @param constraint being added
     * @return Layout with constraint added
     */
    public abstract fun add(constraint: Constraint, vararg others: Constraint): ConstraintLayout

    /**
     * Adds a series of constraints
     *
     * @param constraints being added
     * @return Layout with constraints added
     */
    public abstract fun add(constraints: Iterable<Constraint>): ConstraintLayout

    /**
     * Removes a single constraint
     *
     * @param constraint being removed
     * @return Layout with constraints removed
     */
    public abstract fun remove(constraint: Constraint, vararg others: Constraint): ConstraintLayout

    /**
     * Removes a series of constraints
     *
     * @param constraints being removed
     * @return Layout with constraints removed
     */
    public abstract fun remove(constraints: Iterable<Constraint>): ConstraintLayout
}

/**
 * Simple value within a [Constraints] set.
 */
public abstract class Property internal constructor()

/**
 * 2-Dimensional value within a [Constraints] set.
 */
public class Position internal constructor(internal val top: Expression, internal val left: Expression)

/**
 * External boundaries of a constrained item.
 */
public class Edges internal constructor(
    internal val top   : Expression? = null,
    internal val left  : Expression? = null,
    internal val right : Expression,
    internal val bottom: Expression
)

/**
 * Common constrain properties
 */
public interface BasConstraints {
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
 * [Constraints] the refer to the external, bounding rectangle for a View that is being constrained.
 */
public interface ParentConstraints: BasConstraints

/**
 * The rectangular bounds for a View that is being constrained.
 */
public interface Constraints: BasConstraints {
    /** The rectangle's top edge */
    public val top: Property

    /** The rectangle's left edge */
    public val left: Property
}

/**
 * Block within which constraints can be defined and captured.
 */
@Suppress("MemberVisibilityCanBePrivate", "NOTHING_TO_INLINE")
public class ConstraintDslContext internal constructor() {
    /**
     * The common parent of all Views being constrained
     */
    public lateinit var parent: ParentConstraints internal set

    internal val constraints = mutableListOf<Constraint>()

    private var strength = Required

    private fun add(constraint: Constraint) {
        constraints += constraint
    }

    public operator fun Property.plus(expression: Expression): Expression = expression + this
    public operator fun Property.plus(term      : Term      ): Expression = term + this
    public operator fun Property.plus(other     : Property  ): Expression = Term(this as Variable) + other
    public operator fun Property.plus(constant  : Number    ): Expression = Term(this as Variable) + constant.toDouble()

    public operator fun Property.minus(expression: Expression): Expression = this + -expression
    public operator fun Property.minus(term      : Term      ): Expression = this + -term
    public operator fun Property.minus(other     : Property  ): Expression = this + -other
    public operator fun Property.minus(constant  : Number    ): Expression = this + -constant.toDouble()

    public operator fun Property.times     (coefficient: Number): Term = Term(this as Variable, coefficient.toDouble())
    public operator fun Property.div       (denominator: Number): Term = this * 1.0 / denominator
    public operator fun Property.unaryMinus(                   ): Term = this * -1.0

    private data class MaxProperty(val a: Variable, val b: Variable): Property(), Variable {
        override val name get() = "max(${a.name}, ${b.name})"

        override fun invoke() = max(a.invoke(), b.invoke())

        override fun invoke(value: Double) {
            when {
                a() >= b() -> a(value)
                else       -> b(value)
            }
        }

        override fun toString() = name
    }

    public fun max(a: Property, b: Property): Property = when {
        a is Variable && b is Variable -> {
            MaxProperty(a, b)
        }
        else -> a
    }

    public inline fun max(a: Property, b: Term      ): Term       = max(1 * a, b)
    public inline fun max(a: Property, b: Expression): Expression = max(1 * a, b)
    public        fun max(a: Property, b: Number    ): Expression = max(1 * a, Expression(constant = b.toDouble()))

    public operator fun Term.plus(expression: Expression): Expression = expression + this
    public operator fun Term.plus(term      : Term      ): Expression = Expression(this, term)
    public operator fun Term.plus(property  : Property  ): Expression = this + Term(property as Variable)
    public operator fun Term.plus(value     : Number    ): Expression = Expression(this, constant = value.toDouble())

    public operator fun Term.minus(expression: Expression): Expression = -expression + this
    public operator fun Term.minus(term      : Term      ): Expression = this + -term
    public operator fun Term.minus(property  : Property  ): Expression = this + -property
    public operator fun Term.minus(constant  : Number    ): Expression = this + -constant.toDouble()

    public operator fun Term.times     (constant   : Number): Term = Term(variable, coefficient * constant.toDouble())
    public operator fun Term.div       (denominator: Number): Term = this * (1.0 / denominator.toDouble())
    public operator fun Term.unaryMinus(                   ): Term = this * -1.0

    public        fun max(a: Term, b: Term      ): Term       = Term(MaxProperty(a.variable, b.variable), max(a.coefficient, b.coefficient))
    public inline fun max(a: Term, b: Property  ): Term       = max(a, 1 * b)
    public inline fun max(a: Term, b: Expression): Expression = max(a + 0, b)
    public        fun max(a: Term, b: Number    ): Expression = max(a, Expression(constant = b.toDouble()))

    public operator fun Expression.plus(other   : Expression): Expression = Expression(*(terms.asList() + other.terms).toTypedArray(), constant = constant + other.constant) // TODO do we need to copy term objects?
    public operator fun Expression.plus(term    : Term      ): Expression = Expression(*(terms.asList() + term       ).toTypedArray(), constant = constant                 ) // TODO do we need to copy term objects?
    public operator fun Expression.plus(property: Property  ): Expression = this + Term(property as Variable)
    public operator fun Expression.plus(constant: Number    ): Expression = Expression(*terms, constant = this.constant + constant.toDouble())

    public operator fun Expression.minus(other   : Expression): Expression = this + -other
    public operator fun Expression.minus(term    : Term      ): Expression = this + -term
    public operator fun Expression.minus(property: Property  ): Expression = this + -property
    public operator fun Expression.minus(value   : Number    ): Expression = this + -value.toDouble()

    public operator fun Expression.times(coefficient: Number    ): Expression = Expression(*terms.map { it * coefficient.toDouble() }.toTypedArray(), constant = constant * coefficient.toDouble()) // TODO Do we need to make a copy of the term objects in the array?
    public operator fun Expression.times(other      : Expression): Expression = when {
        isConstant       -> constant       * other
        other.isConstant -> other.constant * this
        else             -> throw NonlinearExpressionException()
    }

    public operator fun Expression.div  (denominator: Double    ): Expression = this * (1.0 / denominator)
    public operator fun Expression.div  (other      : Expression): Expression = when {
        other.isConstant -> this / other.constant
        else             -> throw NonlinearExpressionException()
    }

    public operator fun Expression.unaryMinus(): Expression = this * -1.0

    public fun max(a: Expression, b: Expression): Expression = object: Expression() {
        override val value: Double get() = max(a.value, b.value)
        override val isConstant get() = a.isConstant && b.isConstant
        override fun toString  () = "max($a, $b)"
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

    public inline infix fun Number.eq(expression: Expression): Constraint = expression eq this
    public inline infix fun Number.eq(term      : Term      ): Constraint = term       eq this
    public inline infix fun Number.eq(variable  : Property  ): Constraint = variable   eq this

    public inline fun max(a: Number, b: Property  ): Expression = max(b, a)
    public inline fun max(a: Number, b: Term      ): Expression = max(b, a)
    public inline fun max(a: Number, b: Expression): Expression = max(b, a)

    public class NonlinearExpressionException: Exception()

    public infix fun Expression.eq(other   : Expression): Constraint = Constraint(this - other, EQ, strength).also { add(it) }
    public infix fun Expression.eq(term    : Term      ): Constraint = this eq Expression(term    )
    public infix fun Expression.eq(property: Property  ): Constraint = this eq Term      (property as Variable)
    public infix fun Expression.eq(constant: Number    ): Constraint = this eq Expression(constant = constant.toDouble())

    public infix fun Expression.lessEq(second  : Expression): Constraint = Constraint(this - second, LE, strength).also { add(it) }
    public infix fun Expression.lessEq(term    : Term      ): Constraint = this lessEq Expression(term    )
    public infix fun Expression.lessEq(property: Property  ): Constraint = this lessEq Term      (property as Variable)
    public infix fun Expression.lessEq(constant: Number    ): Constraint = this lessEq Expression(constant = constant.toDouble())

    public infix fun Expression.greaterEq(second  : Expression): Constraint = Constraint(this - second, GE, strength).also { add(it) }
    public infix fun Expression.greaterEq(term    : Term      ): Constraint = this greaterEq Expression(term    )
    public infix fun Expression.greaterEq(property: Property  ): Constraint = this greaterEq Term      (property as Variable)
    public infix fun Expression.greaterEq(constant: Number    ): Constraint = this greaterEq Expression(constant = constant.toDouble())

    public infix fun Term.eq(expression: Expression): Constraint = expression       eq this
    public infix fun Term.eq(term      : Term      ): Constraint = Expression(this) eq term
    public infix fun Term.eq(variable  : Property  ): Constraint = Expression(this) eq variable
    public infix fun Term.eq(constant  : Number    ): Constraint = Expression(this) eq constant.toDouble()

    public infix fun Term.lessEq(expression: Expression): Constraint = Expression(this) lessEq expression
    public infix fun Term.lessEq(term      : Term      ): Constraint = Expression(this) lessEq term
    public infix fun Term.lessEq(property  : Property  ): Constraint = Expression(this) lessEq property
    public infix fun Term.lessEq(constant  : Number    ): Constraint = Expression(this) lessEq constant.toDouble()

    public infix fun Term.greaterEq(expression: Expression): Constraint = Expression(this) greaterEq expression
    public infix fun Term.greaterEq(second    : Term      ): Constraint = Expression(this) greaterEq second
    public infix fun Term.greaterEq(property  : Property  ): Constraint = Expression(this) greaterEq property
    public infix fun Term.greaterEq(constant  : Number    ): Constraint = Expression(this) greaterEq constant.toDouble()

    public infix fun Property.eq(expression: Expression): Constraint = expression eq this
    public infix fun Property.eq(term      : Term      ): Constraint = term       eq this
    public infix fun Property.eq(property  : Property  ): Constraint = Term(this as Variable) eq property
    public infix fun Property.eq(constant  : Number    ): Constraint = Term(this as Variable) eq constant.toDouble()

//    @Suppress("INVALID_CHARACTERS") @JvmName("lessEq1") @JsName("lessEq1")
//    public infix fun Property.`<=`(expression: Expression               ): Constraint = Term(this as Variable) lessEq expression

    public infix fun Property.lessEq(expression: Expression): Constraint = Term(this as Variable) lessEq expression
    public infix fun Property.lessEq(term      : Term      ): Constraint = Term(this as Variable) lessEq term
    public infix fun Property.lessEq(second    : Property  ): Constraint = Term(this as Variable) lessEq second
    public infix fun Property.lessEq(constant  : Number    ): Constraint = Term(this as Variable) lessEq constant.toDouble()

    public infix fun Property.greaterEq(expression: Expression): Constraint = Term(this as Variable) greaterEq expression
    public infix fun Property.greaterEq(term      : Term      ): Constraint = term                   greaterEq this
    public infix fun Property.greaterEq(second    : Property  ): Constraint = Term(this as Variable) greaterEq second
    public infix fun Property.greaterEq(constant  : Number    ): Constraint = Term(this as Variable) greaterEq constant.toDouble()

    public infix fun Position.eq(other: Position) {
        top  eq other.top
        left eq other.left
    }

    public infix fun Position.eq(point: Point) {
        top  eq point.y
        left eq point.x
    }

    public operator fun Edges.plus (value: Number): Edges = this + Insets(-(value.toDouble()))
    public operator fun Edges.minus(value: Number): Edges = this + Insets(  value.toDouble() )

    public operator fun Edges.plus (insets: Insets): Edges = Edges(
        top?.plus (insets.top ) ?: Expression(constant = insets.top ),
        left?.plus(insets.left) ?: Expression(constant = insets.left),
        right  - insets.right,
        bottom - insets.bottom
    )

    public infix fun Edges.eq(other: Edges) {
        if (top != null) {
            when (other.top) {
                null -> top eq 0
                else -> top eq other.top
            }
        }
        if (left != null) {
            when (other.left) {
                null -> left eq 0
                else -> left eq other.left
            }
        }

        right  eq other.right
        bottom eq other.bottom
    }

    public infix fun Double.eq(expression: Expression): Constraint = expression eq this
    public infix fun Double.eq(term      : Term      ): Constraint = term       eq this
    public infix fun Double.eq(property  : Property  ): Constraint = property   eq this

    public infix fun Double.lessEq(expression: Expression): Constraint = Expression(constant = this) lessEq expression
    public infix fun Double.lessEq(term      : Term      ): Constraint = this                        lessEq Expression(term                )
    public infix fun Double.lessEq(property  : Property  ): Constraint = this                        lessEq Term      (property as Variable)

    public infix fun Double.greaterEq(expression: Expression): Constraint = expression                  lessEq    this
    public infix fun Double.greaterEq(term    : Term        ): Constraint = Expression(constant = this) greaterEq term
    public infix fun Double.greaterEq(property: Property    ): Constraint = this                        greaterEq Term(property as Variable)

    public fun capture(block: () -> Unit): List<Constraint> {
        val initialSize = constraints.size
        block()

        return listOf(*constraints.subList(initialSize, constraints.size).toTypedArray())
    }

    public operator fun Constraint.rangeTo(strength: Strength): Constraint {
        this.strength = strength
        return this
    }
}

/**
 * Creates a [ConstraintLayout] that constrains a single View.
 *
 * @param a View being constrained
 * @param block with constraint details
 * @return Layout that constrains the given view
 */
public fun constrain(a: View, block: ConstraintDslContext.(Constraints) -> Unit): ConstraintLayout = ConstraintLayoutImpl(a) { (a) -> block(a) }

/**
 * Creates a [ConstraintLayout] that constrains 2 Views.
 *
 * @param a first View being constrained
 * @param b second View being constrained
 * @param block with constraint details
 * @return Layout that constrains the given views
 */
public inline fun constrain(a: View, b: View, crossinline block: ConstraintDslContext.(Constraints, Constraints) -> Unit): ConstraintLayout = constrain(a, b, others = emptyArray()) { (a, b) -> block(a, b) }

/**
 * Creates a [ConstraintLayout] that constrains 3 Views.
 *
 * @param a first View being constrained
 * @param b second View being constrained
 * @param c third View being constrained
 * @param block with constraint details
 * @return Layout that constrains the given views
 */
public inline fun constrain(a: View, b: View, c: View, crossinline block: ConstraintDslContext.(Constraints, Constraints, Constraints) -> Unit): ConstraintLayout = constrain(a, b, c) { (a, b, c) -> block(a, b, c) }

/**
 * Creates a [ConstraintLayout] that constrains 4 Views.
 *
 * @param a first View being constrained
 * @param b second View being constrained
 * @param c third View being constrained
 * @param d fourth View being constrained
 * @param block with constraint details
 * @return Layout that constrains the given views
 */
public inline fun constrain(a: View, b: View, c: View, d: View, crossinline block: ConstraintDslContext.(Constraints, Constraints, Constraints, Constraints) -> Unit): ConstraintLayout = constrain(a, b, c, d) { (a, b, c, d) -> block(a, b, c, d) }

/**
 * Creates a [ConstraintLayout] that constrains 5 Views.
 *
 * @param a first View being constrained
 * @param b second View being constrained
 * @param c third View being constrained
 * @param d fourth View being constrained
 * @param e fifth View being constrained
 * @param block with constraint details
 * @return Layout that constrains the given views
 */
public inline fun constrain(a: View, b: View, c: View, d: View, e: View, crossinline block: ConstraintDslContext.(Constraints, Constraints, Constraints, Constraints, Constraints) -> Unit): ConstraintLayout = constrain(a, b, c, d, e) { (a, b, c, d, e) -> block(a, b, c, d, e) }

/**
 * Creates a [ConstraintLayout] that constrains several Views.
 *
 * @param a first View being constrained
 * @param b remaining Views being constrained
 * @param block with constraint details
 * @return Layout that constrains the given views
 */
public fun constrain(a: View, b: View, vararg others: View, block: ConstraintDslContext.(List<Constraints>) -> Unit): ConstraintLayout = ConstraintLayoutImpl(a, *listOf(b, *others).toTypedArray(), block = block)

/**
 * Creates a [ConstraintLayout] that constrains a View within a Rectangle.
 *
 * @param view being constrained
 * @param within this rectangle
 * @param block with constraint details
 * @return Layout that constrains the given view
 */
//@kotlin.contracts.ExperimentalContracts
public fun constrain(view: View, within: Rectangle, block: ConstraintDslContext.(Constraints) -> Unit) {
//    contract {
//        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
//    }

    val solver      = Solver()
    val context     = ConstraintDslContext()
    val constraints = listOf(ConstraintsImpl(view, context))

    context.parent = RectangleConstraints(within, context)

    ConstraintLayoutImpl.setupSolver(solver, mutableSetOf(), context, constraints) { (a) -> block(a) }
    ConstraintLayoutImpl.solve(solver, context.parent, mutableSetOf())

    view.position += within.position
}