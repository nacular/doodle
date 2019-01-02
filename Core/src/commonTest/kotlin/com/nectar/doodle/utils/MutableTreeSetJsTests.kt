package com.nectar.doodle.utils

import com.nectar.doodle.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 4/11/18.
 */
class MutableTreeSetJsTests {
    @Test @JsName("isSorted")
    fun `is sorted`() {
        val set      = MutableTreeSetJs<Int>()
        val expected = listOf(-5, 1, 2, 5, 8, 101)

        expected.forEach { set += it }

        set.forEachIndexed { index, i ->
            expect(true) { i == expected[index] }
        }

        expect(6) { set.size }
    }

    @Test
    fun remove() {
        val set      = MutableTreeSetJs<Int>()
        val expected = listOf(-5, 1, 2, 5, 8, 101)

        expected.forEach { set += it }

        expect(6) { set.size }

        expected.forEachIndexed { index, i ->
            expect(true) { set.remove(i) }

            expect(expected.size - (index + 1)) { set.size }
        }

        expect(0) { set.size }
    }

    @Test @JsName("removeNotPresent")
    fun `remove not present`() {
        val set      = MutableTreeSetJs<Int>()
        val expected = listOf(-5, 1, 2, 5, 8, 101)

        expected.forEach { set += it }

        expect(6) { set.size }

        expect(false) { set.remove(1000) }

        expect(6) { set.size }
    }

    @Test @JsName("removeAllFromRoots")
    fun `remove all from roots`() {
        val set = MutableTreeSetJs(listOf(10,9,11))

        expect(true) { set.remove(10) }
        expect(true) { set.remove(11) }
        expect(true) { set.remove( 9) }
        expect(0   ) { set.size       }

        (0..10).forEach {
            expect(false) { set.remove(9) }
        }

        expect(0   ) { set.size      }
        expect(true) { set.isEmpty() }
    }

    @Test @JsName("removeAllLefts")
    fun `remove all lefts`() {
        val set = MutableTreeSetJs(listOf(10,9,11))

        expect(true) { set.remove( 9) }
        expect(true) { set.remove(10) }
        expect(true) { set.remove(11) }
        expect(0   ) { set.size       }

        (0..10).forEach {
            expect(false) { set.remove(9) }
        }

        expect(0   ) { set.size      }
        expect(true) { set.isEmpty() }
    }

    @Test @JsName("removeAllRights")
    fun `remove all rights`() {
        val set = MutableTreeSetJs(listOf(10,9,11))

        expect(true) { set.remove(11) }
        expect(true) { set.remove(10) }
        expect(true) { set.remove( 9) }
        expect(0   ) { set.size       }

        (0..10).forEach {
            expect(false) { set.remove(9) }
        }

        expect(0   ) { set.size      }
        expect(true) { set.isEmpty() }
    }
}