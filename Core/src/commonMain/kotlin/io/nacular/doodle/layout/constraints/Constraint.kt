package io.nacular.doodle.layout.constraints

import io.nacular.doodle.layout.constraints.Strength.Companion.Required
import kotlin.jvm.JvmInline
import kotlin.math.max
import kotlin.math.min

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

    public override fun toString(): String = when (this) {
        Required -> "Required"
        Strong   -> "Strong"
        Medium   -> "Medium"
        Weak     -> "Weak"
        else     -> "$value"
    }

    public companion object {
        /**
         * Indicates that a [Constraint] must be honored. This is the strongest possible value.
         */
        public val Required: Strength = Strength(1001001000)
        public val Strong  : Strength = Strength(   1000000)
        public val Medium  : Strength = Strength(      1000)
        public val Light   : Strength = Strength(       100)
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
 * @param value added to this Strength when creating the new one
 * @return a new [Strength] that is larger than this one by [value]
 */
public operator fun Strength.plus (value: Int): Strength = Strength(this.value + value)

/**
 * @param value subtracted from this Strength when creating the new one
 * @return a new [Strength] that is smaller than this one by [value]
 */
public inline operator fun Strength.minus(value: Int): Strength = this + -value

/**
 * Defines a linear relationship that captures the relationship between a set of variables and the degree to which
 * that relationship should be enforced.
 *
 * @property expression capturing the relative weights of a series of variables in the linear relationship
 * @property operator defining the relationship between the elements of [expression]
 * @property strength that determines the degree to which the relationship is enforced
 */
public class Constraint internal constructor(internal val expression: Expression, internal val operator: Operator, internal var strength: Strength = Required) {
    private val hashCode by lazy {
        var result = operator.hashCode()
        result = 31 * result + expression.hashCode()
        result = 31 * result + strength.hashCode()
        result
    }

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

    override fun hashCode(): Int = hashCode

    internal fun differsByConstantOnly(other: Constraint): Boolean =
        operator == Operator.EQ    &&
        operator == other.operator &&
        strength == other.strength &&
        strength == Required       &&
        expression.differsByConstantOnly(other.expression)
}
