package com.nectar.doodle.units

import com.nectar.doodle.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 2/18/18.
 */

private interface Something

class UnitTests {
    @Test
    @JsName("defaultMultiplierIs1")
    fun `default multiplier is 1`() {
        val a = Unit<Something>("a")

        expect(1.0, "$a.multiplier") { a.multiplier }
    }


    @Test
    @JsName("divWorks")
    fun `div works`() {
        val a = Unit<Something>("a", 10.0)
        val b = Unit<Something>("b",  1.0)

        expect(10.0, "$a / $b") { a / b }
    }

    @Test
    @JsName("toStringWorks")
    fun `toString works`() {
        val a = Unit<Something>("description", 10.0)

        expect("description", "$a.toString()") { a.toString() }
    }

    @Test
    @JsName("comparisonsWork")
    fun `comparisons work`() {
        val a = Unit<Something>("a", 10.0)
        val b = Unit<Something>("b",  1.0)
        val c = Unit<Something>("a", 10.0)

        expect(true,  "$a > $b" ) { a  > b }
        expect(false, "$a < $b" ) { a  < b }
        expect(true,  "$a == $a") { a == a }
        expect(false, "$a == $b") { a == b }
        expect(true,  "$a == $c") { a == c }
    }
}

class MeasureTests {
    @Test
    @JsName("comparisonsWork")
    fun `comparisons work`() {
        val unitA = Unit<Something>("a", 10.0)
        val unitB = Unit<Something>("b",  1.0)

        val measureA = Measure(10.0, unitA)
        val measureB = Measure(10.0, unitB)

        expect(true,  "$measureA > $measureB" ) { measureA  > measureB }
        expect(false, "$measureA < $measureB" ) { measureA  < measureB }
        expect(false, "$measureA == $measureB") { measureA == measureB }
    }

    @Test
    @JsName("plusMinusOperatorsWork")
    fun `+ -`() {
        val unitA = Unit<Something>("a", 10.0)
        val unitB = Unit<Something>("b",  1.0)

        val measureA = Measure(10.0, unitA)
        val measureB = Measure(10.0, unitB)

        expect(Measure(110.0, unitB)) { measureA + measureB }
        expect(Measure( 90.0, unitB)) { measureA - measureB }
    }

    @Test
    @JsName("unaryMinusOperatorsWork")
    fun `unary -`() {
        val unit = Unit<Something>("a")

        val measure = Measure(10.0, unit)

        expect(Measure(-10.0, unit)) { -measure }
    }

    @Test
    @JsName("timesDivideOperatorsWork")
    fun `* ÷`() {
        val op: (Operation<Something>) -> kotlin.Unit = {
            val unit = Unit<Something>("a")
            val start = 10.0
            val value = 2.3
            val measure = Measure(start, unit)

            expect(Measure(it((measure `in` unit), value), unit), { it(measure, value) })
        }

        listOf(times, divide).forEach(op)
    }
}

interface Operation<T> {
    operator fun invoke(first: Double,     second: Double): Double
    operator fun invoke(first: Measure<T>, second: Double): Measure<T>
}

private val times: Operation<Something> = object: Operation<Something> {
    override fun invoke(first: Double,             second: Double) = first * second
    override fun invoke(first: Measure<Something>, second: Double) = first * second
}

private val divide: Operation<Something> = object: Operation<Something> {
    override fun invoke(first: Double,             second: Double) = first / second
    override fun invoke(first: Measure<Something>, second: Double) = first / second
}