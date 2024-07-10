package io.nacular.doodle.layout.constraints

import io.nacular.doodle.utils.fastMutableMapOf
import kotlin.math.abs

/**
 * Classes are derived works from [https://github.com/alexbirkett/kiwi-java/tree/master/src/main/java/no/birkett/kiwi],
 * which have the following copyright.
 *
 * Copyright (c) 2015, Alex Birkett
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 *  * Neither the name of kiwi-java nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Represents a relationship between a set of [Term]s and a [constant] value.
 * This allows the modeling of linear relationships of the form: `10x -5y + 11`.
 *
 * @property terms (or variable-coefficient pairs) within the expression
 * @property constant value for the expression
 */
public open class Expression internal constructor(terms: Array<out Term>, constant: Double = 0.0) {
    internal val constant = constant + terms.asSequence().filter { it.isConst }.sumOf { it.value }

    internal val terms = terms.filterIsInstance<VariableTerm>().toTypedArray()

    /**
     * Computed value of this expression
     */
    internal open val value: Double get() = terms.sumOf { it.value } + constant

    /**
     * `true` if the expression has no terms
     */
    internal open val isConstant: Boolean by lazy { terms.isEmpty() }

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
        val vars = fastMutableMapOf<Variable, Double>()
        for (term in terms) {
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
        return Expression(*reducedTerms.toTypedArray(), constant = constant)
    }

    internal fun differsByConstantOnly(other: Expression): Boolean = constant != other.constant && terms.contentEquals(other.terms)

    internal companion object {
        operator fun invoke(vararg terms: Term, constant: Double = 0.0) = Expression(terms, constant)
    }
}

/**
 * A coefficient-value pair that is used to build [Expression]s
 */
public abstract class Term internal constructor(internal open val coefficient: Double = 1.0) {
    internal abstract val value: Double
    internal abstract val isConst: Boolean
    internal abstract operator fun times(value: Number): Term
}

internal data class VariableTerm(val variable: Variable, override val coefficient: Double = 1.0): Term(coefficient) {
    override val value: Double get() = coefficient * variable()
    override val isConst = false

    override fun times(value: Number) = VariableTerm(variable, coefficient * value.toDouble())

    override fun toString(): String = "${if (coefficient >= 0) "+ $coefficient" else "- ${abs(coefficient)}"}($variable:${variable()})"
}

internal class ConstTerm(val property: Property, override val coefficient: Double = 1.0): Term(coefficient) {
    override val value: Double get() = coefficient * property.readOnly
    override val isConst = true

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