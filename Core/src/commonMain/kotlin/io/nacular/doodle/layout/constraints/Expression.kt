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
public abstract class Expression internal constructor() {
    internal abstract val terms: Array<VariableTerm>

    internal abstract val constant: Double

    /**
     * Computed value of this expression
     */
    public open val value: Double get() = terms.sumOf { it.value } + constant

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
        if (this     === other         ) return true
        if (other    !is Expression    ) return false
        if (constant !=  other.constant) return false

        val mine   = terms
        val theirs = other.terms
        if (mine.size != theirs.size) return false

        for (i in mine.indices) {
            if (mine[i] != theirs[i]) return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = terms.contentHashCode()
        result = 31 * result + constant.hashCode()
        return result
    }

    internal fun reduce(): Expression = when {
        terms.size < REDUCE_THRESHOLD -> reduceLoop()
        else                          -> reduceHash()
    }

    private fun reduceLoop(): Expression {
        val variables    = arrayOfNulls<Variable>(terms.size)
        val coefficients = DoubleArray(terms.size)
        var count = 0

        for ((variable, coefficient) in terms) {
            var i = 0
            while (i < count && variables[i] !== variable) i++

            when {
                i < count -> coefficients[i] += coefficient
                else      -> { variables[count] = variable; coefficients[count] = coefficient; count++ }
            }
        }

        val reducedTerms = arrayOfNulls<Term>(count)
        for (i in 0 until count) reducedTerms[i] = VariableTerm(variables[i]!!, coefficients[i])

        @Suppress("UNCHECKED_CAST")
        return Expression(*(reducedTerms as Array<Term>), constant = constant)
    }

    private fun reduceHash(): Expression {
        val vars = fastMutableMapOf<Variable, Double>()
        for ((variable, coefficient) in terms) {
            vars[variable] = (vars[variable] ?: 0.0) + coefficient
        }
        return Expression(vars.map { (variable, coefficient) -> VariableTerm(variable, coefficient) }, constant)
    }

    internal companion object {
        private const val REDUCE_THRESHOLD = 12

        operator fun invoke(vararg terms: Term,       constant: Double = 0.0) = ExpressionImpl(terms.toList(), constant)
        operator fun invoke(       terms: List<Term>, constant: Double = 0.0) = ExpressionImpl(terms,          constant)
    }
}

internal class ExpressionImpl(terms: List<Term>, override val constant: Double = 0.0) : Expression() {
//    override val terms: Array<VariableTerm> = terms.filterIsInstance<VariableTerm>().toTypedArray()

    override val terms: Array<VariableTerm> = run {
        val buffer = arrayOfNulls<VariableTerm>(terms.size)   // upper bound: at most this many survive
        var count = 0
        for (term in terms) if (term is VariableTerm) buffer[count++] = term

        @Suppress("UNCHECKED_CAST")
        when (count) {
            buffer.size -> buffer as Array<VariableTerm>   // nothing filtered — reuse buffer as-is
            else        -> buffer.copyOf(count) as Array<VariableTerm>
        }
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

internal interface Variable {
    val name          : String
    var edited        : Boolean
    var isReference   : Boolean
    var constrained   : Boolean
    val needsSynthetic: Boolean

    operator fun invoke(             ): Double
    operator fun invoke(value: Double)
}

internal interface ReferenceVariable: Variable {
    override var isReference get() = true; set(value) {}

    fun initial(): Double
}