package io.nacular.doodle.theme.basic

import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/29/20.
 */
class ListPositionerTests {
    @Test
    fun `rowFor works`() {
        val height  = 10.0
        val spacing =  2.0

        VerticalListPositioner(height, spacing = spacing).apply {
            listOf(
                    Insets.None       to Point(0.0,  1.0) to 0,
                    Insets.None       to Point(0.0, 10.0) to 0,
                    Insets.None       to Point(0.0, 11.0) to 0,
                    Insets.None       to Point(0.0, 12.0) to 1,
                    Insets(top = 3.0) to Point(0.0,  4.0) to 0,
                    Insets(top = 3.0) to Point(0.0, 13.0) to 0,
                    Insets(top = 3.0) to Point(0.0, 14.0) to 0,
                    Insets(top = 3.0) to Point(0.0, 15.0) to 1
            ).forEach { (data, expected) ->
                expect(expected, "$data -> $expected") { itemFor(Size(100), insets = data.first, at = data.second) }
            }
        }
    }

    @Test
    fun `row bounds works`() {
        val height  =  10.0
        val width   = 100.0
        val spacing =   2.0

        VerticalListPositioner(height, spacing = spacing).apply {
            listOf(
                    Insets.None       to 0 to Rectangle(0.0, spacing,                  width, height),
                    Insets.None       to 1 to Rectangle(0.0, height + 2 * spacing,     width, height),
                    Insets(top = 3.0) to 0 to Rectangle(0.0, 3.0 + spacing,            width, height),
                    Insets(top = 3.0) to 1 to Rectangle(0.0, height + 2 * spacing + 3, width, height)
            ).forEach { (data, expected) ->
                expect(expected) { itemBounds(size = Size(width, height), insets = data.first, index = data.second) }
            }
        }
    }

    @Test
    fun `row bounds and rowFor agree`() {
        val height  =  10.0
        val width   = 100.0
        val spacing =   2.0

        VerticalListPositioner(height, spacing = spacing).apply {
            listOf(
                    Insets.None       to 0 to Rectangle(0.0, spacing,                  width, height),
                    Insets.None       to 1 to Rectangle(0.0, height + 2 * spacing,     width, height),
                    Insets(top = 3.0) to 0 to Rectangle(0.0, 3.0 + spacing,            width, height),
                    Insets(top = 3.0) to 1 to Rectangle(0.0, height + 2 * spacing + 3, width, height)
            ).forEach { (data, expected) ->
                val row = itemFor(Size.Empty, data.first, expected.position)

                expect(data.second) { row }
                expect(expected) { itemBounds(size = Size(width, height), insets = data.first, index = row) }
            }
        }
    }
}