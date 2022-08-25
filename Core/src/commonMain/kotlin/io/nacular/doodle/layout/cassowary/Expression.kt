package io.nacular.doodle.layout.cassowary

import kotlin.math.abs

/**
 * Represents a relationship between a set of [Term]s and a [constant] value.
 * This allows the modeling of linear relationships of the form: `10x -5y + 11`.
 *
 * @property terms (or variable-coefficient pairs) within the expression
 * @property constant value for the expression
 */
public open class Expression internal constructor(internal vararg val terms: Term, internal val constant: Double = 0.0) {
    /**
     * Computed value of this expression
     */
    internal open val value: Double get() = terms.filterIsInstance<VariableTerm>().sumOf { it.value } + constant

    /**
     * `true` if the expression has no terms
     */
    internal open val isConstant: Boolean get() = terms.filterIsInstance<VariableTerm>().isEmpty()

    /**
     * Provides the Expression's value directly, and does not treat its contents as
     * variables when used in a [Constraint]. This means the Expression's variables won't be
     * altered to try and satisfy the constraint.
     *
     * ```
     * a eq (2 * b + c).readOnly
     * ```
     * will be treated as
     *
     * ```
     *
     * a eq (2 * 10 + 11)
     * ```
     *
     * if `b == 10` and `c == 11`
     */
    public val readOnly: Double get() = value

    override fun toString(): String = """
        ${terms.joinToString(" ")}${
        when {
            constant == 0.0 -> ""
            constant >= 0.0 -> " + $constant"
            else            -> " - ${abs(constant)}"
        }
    }""".trimIndent()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Expression

        if (!terms.contentEquals(other.terms)) return false
        if (constant != other.constant       ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = terms.contentHashCode()
        result = 31 * result + constant.hashCode()
        return result
    }

    internal fun reduce(): Expression {
        val vars = mutableMapOf<Variable, Double>()
        for (term in terms.filterIsInstance<VariableTerm>()) {
            var value = vars[term.variable]
            if (value == null) {
                value = 0.0
            }
            value += term.coefficient
            vars[term.variable] = value
        }
        val reducedTerms = mutableListOf<Term>()
        for (variable in vars.keys) {
            reducedTerms.add(VariableTerm(variable, vars[variable]!!))
        }
        return Expression(*reducedTerms.toTypedArray() + terms.filterIsInstance<ConstTerm>(), constant = constant)
    }
}

/**
 * A coefficient-value pair that is used to build [Expression]s
 */
public abstract class Term internal constructor(internal open val coefficient: Double = 1.0) {
    internal abstract val value: Double
    internal abstract operator fun times(value: Number): Term
}

internal data class VariableTerm(val variable: Variable, override val coefficient: Double = 1.0): Term(coefficient) {
    override val value: Double get() = coefficient * variable()

    override fun times(value: Number) = VariableTerm(variable, coefficient * value.toDouble())

    override fun toString(): String = "${if (coefficient >= 0) "+ $coefficient" else "- ${abs(coefficient)}"}($variable:${variable()})"
}

internal class ConstTerm(val property: Property, override val coefficient: Double = 1.0): Term(coefficient) {
    override val value: Double get() = coefficient * property.readOnly //block()

    override fun times(value: Number) = ConstTerm(property, coefficient * value.toDouble())

    override fun toString(): String = "${if (coefficient >= 0) "+ $coefficient" else "- ${abs(coefficient)}"}($property)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ConstTerm

        if (property    != other.property   ) return false
        if (coefficient != other.coefficient) return false

        return true
    }

    override fun hashCode(): Int {
        var result = property.hashCode()
        result = 31 * result + coefficient.hashCode()
        return result
    }
}

internal interface Variable {
    val name: String
    operator fun invoke(             ): Double
    operator fun invoke(value: Double)
}