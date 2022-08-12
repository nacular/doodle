package io.nacular.doodle.layout.cassowary

import io.nacular.doodle.layout.cassowary.Strength.Companion.Required
import kotlin.jvm.JvmInline
import kotlin.math.max
import kotlin.math.min

/**
 * Set of operators that can be used when defining a linear relationship for a [Constraint].
 */
internal enum class Operator {
    LE, GE, EQ;

    override fun toString(): String = when (this) {
        LE -> "<="
        GE -> ">="
        EQ -> "=="
    }
}

/**
 * Defines the weight or priority applied to a given [Constraint]. This determines the likelihood
 * of the [Constraint] being honored.
 */
@JvmInline
public value class Strength private constructor(public val value: Int): Comparable<Strength> {
    public override operator fun compareTo(other: Strength): Int = this.value.compareTo(other.value)

    public override fun toString(): String = "$value"

    public companion object {
        /**
         * Indicates that a [Constraint] must be honored. This is the strongest possible value.
         */
        public val Required: Strength = Strength(1001001000)
        public val Strong  : Strength = Strength(   1000000)
        public val Medium  : Strength = Strength(      1000)
        public val Weak    : Strength = Strength(         1)

        /**
         * Create a new [Strength] with a value in [0 .. [Required.value][Required]]
         *
         * @param value for the Strength
         */
        public operator fun invoke(value: Int): Strength = Strength(max(0, min(Required.value, value)))
    }
}

/**
 * Defines a linear relationship that captures the relationship between a set of variables and the degree to which
 * that relationship should be enforced.
 *
 * @property expression capturing the relative weights of a series of variables in the linear relationship
 * @property operator defining the relationship between the elements of [expression]
 * @property strength that determines the degree to which the relationship is enforced
 */
public class Constraint internal constructor(expression: Expression, internal val operator: Operator, strength: Strength = Required) {
    public constructor(other: Constraint, strength: Strength): this(other.expression, other.operator, strength)

    internal val expression: Expression = reduce(expression)
    internal var strength  : Strength = strength; internal set

    override fun toString(): String = "$expression $operator ~ $strength"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Constraint

        if (operator   != other.operator  ) return false
        if (expression != other.expression) return false
        if (strength   != other.strength  ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = operator.hashCode()
        result = 31 * result + expression.hashCode()
        result = 31 * result + strength.hashCode()
        return result
    }

    private fun reduce(expr: Expression): Expression {
        val vars = mutableMapOf<Variable, Double>()
        for (term in expr.terms) {
            var value = vars[term.variable]
            if (value == null) {
                value = 0.0
            }
            value += term.coefficient
            vars[term.variable] = value
        }
        val reducedTerms = mutableListOf<Term>()
        for (variable in vars.keys) {
            reducedTerms.add(Term(variable, vars[variable]!!))
        }
        return Expression(*reducedTerms.toTypedArray(), constant = expr.constant)
    }
}
