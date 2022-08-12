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
    internal open val value: Double = terms.sumOf { it.value } + constant

    /**
     * `true` if the expression has no terms
     */
    internal open val isConstant: Boolean get() = terms.isEmpty()

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
        if (value    != other.value          ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = terms.contentHashCode()
        result = 31 * result + constant.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}

/**
 * A variable-coefficient pair that is used to build [Expression]s
 */
public data class Term internal constructor(internal var variable: Variable, internal val coefficient: Double = 1.0) {
    internal val value: Double get() = coefficient * variable()

    override fun toString(): String = "${if (coefficient >= 0) "+ $coefficient" else "- ${abs(coefficient)}"}($variable:${variable()})"
}

internal interface Variable {
    val name: String
    operator fun invoke(             ): Double
    operator fun invoke(value: Double)
}