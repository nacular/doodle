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
}