package io.nacular.doodle.controls.table

import io.nacular.doodle.core.Box
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import kotlin.math.max

internal class TableHeader(columns: List<InternalColumn<*,*,*>>, private val renderBlock: (Canvas) -> Unit): Box() {
    init {
        focusable = false
        layout = object: Layout {
            override fun layout(container: PositionableContainer) {
                var x          = 0.0
                var totalWidth = 0.0

                container.children.forEachIndexed { index, view ->
                    view.bounds = Rectangle(Point(x, 0.0), Size(columns[index].width, container.height))

                    x += view.width
                    totalWidth += view.width
                }

                container.width = totalWidth + columns[columns.size - 1].width
            }
        }
    }

    override fun render(canvas: Canvas) {
        renderBlock(canvas)
    }

    public override fun doLayout() = super.doLayout()
}

internal class TablePanel(columns: List<InternalColumn<*,*,*>>, private val renderBlock: (Canvas) -> Unit): Box() {
    init {
        focusable = false
        children += columns.map { it.view }

        layout = object: Layout {
            override fun layout(container: PositionableContainer) {
                var x          = 0.0
                var height     = 0.0
                var totalWidth = 0.0

                container.children.forEachIndexed { index, view ->
                    view.bounds = Rectangle(Point(x, 0.0), Size(columns[index].width, view.minimumSize.height))

                    x          += view.width
                    height      = max(height, view.height)
                    totalWidth += view.width
                }

                container.parent?.let {
                    container.size = Size(max(it.width, totalWidth), max(it.height, height))
                }

                container.children.forEach {
                    it.height = container.height
                }
            }
        }
    }

    override fun render(canvas: Canvas) {
        renderBlock(canvas)
    }

    public override fun doLayout() = super.doLayout()
}