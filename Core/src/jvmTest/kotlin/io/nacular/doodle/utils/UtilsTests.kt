package io.nacular.doodle.utils

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/13/20.
 */
class UtilsTests {
    @Test
    fun `Int is even works`() {
        (-100 .. 100 step 2).forEach {
            expect(true) { it.isEven }
        }
    }

    @Test
    fun `Int is odd works`() {
        (-99 .. 99 step 2).forEach {
            expect(true) { it.isOdd }
        }
    }

    @Test
    fun `Long is even works`() {
        (-100L .. 100L step 2).forEach {
            expect(true) { it.isEven }
        }
    }

    @Test
    fun `Long is odd works`() {
        (-99L .. 99L step 2).forEach {
            expect(true) { it.isOdd }
        }
    }

    @Test
    fun `if true works`() {
        var hits = 0

        expect(true) {
            true.ifTrue {
                hits++
            }
        }

        expect(1) { hits }

        expect(true) {
            true.ifFalse {
                hits++
            }
        }

        expect(1) { hits }

        expect(false) {
            false.ifTrue {
                hits++
            }
        }

        expect(1) { hits }

        expect(false) {
            false.ifFalse {
                hits++
            }
        }

        expect(2) { hits }
    }

    @Test
    fun `round to nearest works`() {
        listOf(
             145.36 to  0.0 to   145.36,
             145.36 to  1.0 to   145.00,
             145.36 to  0.1 to   145.40,
            1023.00 to 20.0  to 1020.00,
               1.00 to 10.0 to     0.00,
             145.36 to  5.0 to   145.00
        ).forEach { (data, expectation) ->
            expect(expectation) {
                data.first.roundToNearest(data.second)
            }
        }
    }

    @Test
    fun `contains for nullable collection works`() {
        val collection: Collection<Int>? = null

        expect(false) { 40 in collection }
    }

    @Test
    fun `add or append works`() {
        val list = mutableListOf(1,2,3)

        list.addOrAppend(at = 5, value = 4)
        list.addOrAppend(at = 0, value = 6)

        expect(listOf(6,1,2,3,4)) { list }
    }

    @Test
    fun `splitting string matches works`() {
        listOf(
                "hello, this is a test. how should we proceed?" to "[.]" to (listOf("hello, this is a test" to ".") to " how should we proceed?"),
                "foo,bar&blah?" to "[,&]" to (listOf("foo" to ",", "bar" to "&") to "blah?")
        ).forEach { (data, expectation) ->
            expect(MatchResult(expectation.first.map { MatchedChunk(it.first, it.second) }, expectation.second)) {
                data.first.splitMatches(data.second.toRegex())
            }
        }
    }

    @Test
    fun `splitting string limit works`() {
        listOf(
                ("a b c d e" to """\s""" to 0) to (listOf("a" to " ", "b" to " ", "c" to " ", "d" to " ") to "e"),
                ("a b c d e" to """\s""" to 1) to (listOf<Pair<String, String>>() to "a b c d e"),
                ("a b c d e" to """\s""" to 3) to (listOf("a" to " ", "b" to " ") to "c d e"),
                ("a b c d e" to """\s""" to 9) to (listOf("a" to " ", "b" to " ", "c" to " ", "d" to " ") to "e")
        ).forEach { (data, expectation) ->
            expect(MatchResult(expectation.first.map { MatchedChunk(it.first, it.second) }, expectation.second)) {
                val (text, regexInfo) = data.first

                text.splitMatches(regexInfo.toRegex(), limit = data.second)
            }
        }
    }

    @Test
    fun `invalid limit fails`() {
        assertFailsWith<IllegalArgumentException> {
            "foo bar".splitMatches(".".toRegex(), -2)
        }
    }
}