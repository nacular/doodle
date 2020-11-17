package io.nacular.doodle.theme.basic

import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.Insets
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/29/20.
 */
class ListPositionerTests {
    @Test @JsName("rowForWorks")
    fun `rowFor works`() {
        val height  = 10.0
        val spacing =  2.0

        ListPositioner(height, spacing).apply {
            listOf(
                    Insets.None       to  1.0 to 0,
                    Insets.None       to 10.0 to 0,
                    Insets.None       to 11.0 to 0,
                    Insets.None       to 12.0 to 1,
                    Insets(top = 3.0) to  4.0 to 0,
                    Insets(top = 3.0) to 13.0 to 0,
                    Insets(top = 3.0) to 14.0 to 0,
                    Insets(top = 3.0) to 15.0 to 1
            ).forEach { (data, expected) ->
                expect(expected, "$data -> $expected") { rowFor(insets = data.first, y = data.second) }
            }
        }
    }

    @Test @JsName("rowBoundsWorks")
    fun `row bounds works`() {
        val height  =  10.0
        val width   = 100.0
        val spacing =   2.0

        ListPositioner(height, spacing).apply {
            listOf(
                    Insets.None       to 0 to Rectangle(0.0, spacing,                  width, height),
                    Insets.None       to 1 to Rectangle(0.0, height + 2 * spacing,     width, height),
                    Insets(top = 3.0) to 0 to Rectangle(0.0, 3.0 + spacing,            width, height),
                    Insets(top = 3.0) to 1 to Rectangle(0.0, height + 2 * spacing + 3, width, height)
            ).forEach { (data, expected) ->
                expect(expected) { rowBounds(width = width, insets = data.first, index = data.second) }
            }
        }
    }

    @Test @JsName("rowBoundsAndRowRowAgree")
    fun `row bounds and rowFor agree`() {
        val height  =  10.0
        val width   = 100.0
        val spacing =   2.0

        ListPositioner(height, spacing).apply {
            listOf(
                    Insets.None       to 0 to Rectangle(0.0, spacing,                  width, height),
                    Insets.None       to 1 to Rectangle(0.0, height + 2 * spacing,     width, height),
                    Insets(top = 3.0) to 0 to Rectangle(0.0, 3.0 + spacing,            width, height),
                    Insets(top = 3.0) to 1 to Rectangle(0.0, height + 2 * spacing + 3, width, height)
            ).forEach { (data, expected) ->
                val row = rowFor(data.first, expected.y)

                expect(data.second) { row }
                expect(expected) { rowBounds(width = width, insets = data.first, index = row) }
            }
        }
    }
}