package io.nacular.doodle.controls.list

import JsName
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.nacular.doodle.controls.IntProgressionModel
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.list.ListBehavior.ItemPositioner
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.ObservableList
import org.junit.Test
import kotlin.math.max
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 1/21/22.
 */
class ListTests {
    private class CustomList<T, M: ListModel<T>>(model: M, scrollCache: Int): List<T, M>(model, scrollCache = scrollCache) {
        public override val children: ObservableList<View> get() = super.children

        public override fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {
            super.handleDisplayRectEvent(old, new)
        }
    }

    @Test @JsName("scrollCaches")
    fun `scroll caches`() {
        val itemHeight = 10.0

        val positionerMock = mockk<ItemPositioner<Int>>(relaxed = true).apply {
            val at    = slot<Point>()
            val index = slot<Int>()
            val list  = slot<List<Int, *>>()

            every { item(of = any(), at = capture(at)) } answers {
                max(0, (at.captured.y / itemHeight).toInt())
            }

            every { itemBounds(of = capture(list), item = any(), index = capture(index), view = any()) } answers {
                Rectangle(
                    x      = index.captured * list.captured.size.width,
                    y      = index.captured * itemHeight + (index.captured + 1),
                    width  = max(0.0, list.captured.size.width),
                    height = itemHeight
                )
            }
        }

        val behavior = mockk<ListBehavior<Int>>(relaxed = true).apply {
            every { positioner } returns positionerMock
        }

        val model = IntProgressionModel(1 .. 10)

        val list = CustomList(model, 0).apply {
            size          = Size(100, 0)
            this.behavior = behavior
        }

        expect(1) { list.children.size }

        list.handleDisplayRectEvent(Empty, Rectangle(size = Size(100)))

        expect(model.size) { list.children.size }
    }
}