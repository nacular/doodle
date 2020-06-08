package io.nacular.doodle.utils

import io.nacular.doodle.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 4/11/18.
 */
class MutableTreeSetJsTests {
    @Test @JsName("isSorted")
    fun `is sorted`() {
        listOf(
                listOf(),
                listOf(1),
                listOf(2, 1, 5, 8, 101, -5),
                listOf(2, 0, 0, 8, 101, -5)
        ).forEach {
            expect(true) { isSorted(MutableTreeSetJs(it)) }
        }
    }

    @Test @JsName("addAllWorks")
    fun `add all works`() {
        listOf(
                listOf(),
                listOf(1),
                listOf(2, 1, 5, 8, 101, -5),
                listOf(2, 0, 8, 101, -5)
        ).forEach {
            val set = MutableTreeSetJs<Int>()

            expect(it.isNotEmpty(), "$it") { set.addAll(it) }
            expect(it.size        ) { set.size }
            expect(it.isNotEmpty()) { set.containsAll(it) }
            it.forEach {
                expect(true) { it in set }
            }
        }
    }

    @Test @JsName("removeAllWorks")
    fun `remove all works`() {
        listOf(
                listOf(),
                listOf(1),
                listOf(2, 1, 5, 8, 101, -5),
                listOf(2, 0, 8, 101, -5)
        ).forEach {
            val set = MutableTreeSetJs(it)

            expect(it.isNotEmpty(), "$it") { set.removeAll(it) }
            expect(0) { set.size }
            expect(false) { set.containsAll(it) }
            it.forEach {
                expect(false) { it in set }
            }
        }
    }

    @Test @JsName("clearWorks")
    fun `clear works`() {
        listOf(
                listOf(),
                listOf(1),
                listOf(2, 1, 5, 8, 101, -5),
                listOf(2, 0, 8, 101, -5)
        ).forEach {
            val set = MutableTreeSetJs(it)

            set.clear()
            expect(0) { set.size }
            expect(false) { set.containsAll(it) }
            it.forEach {
                expect(false) { it in set }
            }
        }
    }

    @Test @JsName("retainAllWorks")
    fun `retain all works`() {
        listOf(
                listOf(                     ),
                listOf(1                    ),
                listOf(2, 1, 5,   8, 101, -5),
                listOf(2, 0, 8, 101,  -5    )
        ).forEach {
            val set = MutableTreeSetJs(it).apply {
                this.firstOrNull()?.let {
                    this += it - 1
                }

                this.lastOrNull()?.let {
                    this += it + 1
                }
            }

            expect(it.isNotEmpty()) { set.retainAll(it) }
            expect(it.size        ) { set.size }
            expect(it.isNotEmpty()) { set.containsAll(it) }
            it.forEach {
                expect(true) { it in set }
            }
        }
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

        repeat(10) {
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

        repeat(10) {
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

        repeat(10) {
            expect(false) { set.remove(9) }
        }

        expect(0   ) { set.size      }
        expect(true) { set.isEmpty() }
    }

    private fun <T: Comparable<T>> isSorted(set: Set<T>): Boolean {
        set.iterator().let { iterator ->
            while (iterator.hasNext()) {
                val current = iterator.next()

                if (iterator.hasNext()) {
                    if (current > iterator.next()) {
                        return false
                    }
                }
            }
        }

        return true
    }
}