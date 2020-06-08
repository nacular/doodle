package io.nacular.doodle.utils

import io.nacular.doodle.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 4/11/18.
 */
class TreeSetJsTests {
    @Test @JsName("isSorted")
    fun `is sorted`() {
        listOf(
                listOf(),
                listOf(1),
                listOf(2, 1, 5, 8, 101, -5),
                listOf(2, 0, 0, 8, 101, -5)
        ).forEach {
            expect(true) { isSorted(TreeSetJs(it)) }
        }
    }

    @Test @JsName("sizeWorks")
    fun `size works`() {
        listOf(
                listOf(),
                listOf(1),
                listOf(2, 1, 5, 8, 101, -5),
                listOf(2, 0, 8, 101, -5)
        ).forEach {
            val set = TreeSetJs(it)

            expect(it.size) { set.size }

            expect(it.isNotEmpty()) { set.isNotEmpty() }
        }
    }

    @Test @JsName("containsWorks")
    fun `contains works`() {
        listOf(
                listOf(),
                listOf(1),
                listOf(2, 1, 5, 8, 101, -5),
                listOf(2, 0, 8, 101, -5)
        ).forEach {
            val set = TreeSetJs(it)

            it.forEach {
                expect(true) { it in set }
            }

            expect(it.isNotEmpty()) { set.containsAll(it) }
        }
    }

    @Test @JsName("defaultsToEmpty")
    fun `defaults to empty`() {
        expect(true) { TreeSetJs<Int>().isEmpty() }
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