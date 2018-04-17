package com.nectar.doodle.utils

import com.nectar.doodle.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 4/11/18.
 */
class TreeSetJsTests {
    @Test @JsName("isSorted")
    fun `is sorted`() {
        val set      = TreeSetJs(listOf(2, 1, 5, 8, 101, -5))
        val expected = listOf(-5, 1, 2, 5, 8, 101)

        set.forEachIndexed { index, i ->
            expect(true) { i == expected[index] }
        }

        expect(6) { set.size }
    }
}