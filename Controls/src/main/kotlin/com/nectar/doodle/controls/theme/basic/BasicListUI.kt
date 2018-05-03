package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.list.ItemPositioner
import com.nectar.doodle.controls.list.ItemUIGenerator
import com.nectar.doodle.controls.list.List
import com.nectar.doodle.controls.list.ListRenderer
import com.nectar.doodle.controls.text.Label
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color.Companion.gray
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Meta
import com.nectar.doodle.text.StyledText
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.isEven
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 3/20/18.
 */

private class ListRow<T>(textMetrics: TextMetrics, list: List<*, *>, row: T, private var index: Int): Label(textMetrics, StyledText(row.toString())) {

    private var background = lightgray

    init {
        fitText             = false
        horizontalAlignment = Left

        styleChanged += { rerender() }

        mouseChanged += object: MouseListener {
            private var pressed   = false
            private var mouseOver = false

            override fun mouseEntered(event: MouseEvent) {
                mouseOver       = true
                backgroundColor = backgroundColor?.lighter(0.25f)
            }

            override fun mouseExited(event: MouseEvent) {
                mouseOver       = false
                backgroundColor = background
            }

            override fun mousePressed(event: MouseEvent) {
                pressed = true
            }

            override fun mouseReleased(event: MouseEvent) {
                if (mouseOver && pressed) {
                    setOf(index).also {
                        list.apply {
                            when {
                                selected(index)         -> removeSelection(it)
                                Ctrl in event.modifiers ||
                                Meta in event.modifiers -> addSelection   (it)
                                else                    -> setSelection   (it)
                            }
                        }
                    }
                }
                pressed = false
            }
        }

        update(list, row, index)
    }

    fun update(list: List<*, *>, row: Any?, index: Int) {
        text       = row.toString()
        this.index = index
        background = if (list.selected(index)) green else lightgray

        background = when {
            index.isEven -> background.lighter()
            else         -> background
        }

        backgroundColor = background
    }
}

class LabelItemUIGenerator<T>(private val textMetrics: TextMetrics): ItemUIGenerator<T> {
    override fun invoke(list: List<T, *>, row: T, index: Int, current: Gizmo?): Gizmo = when (current) {
        is ListRow<*> -> current.apply { update(list, row, index) }
        else          -> ListRow(textMetrics, list, row, index)
    }
}

private class BasicPositioner<T>(private val height: Double): ItemPositioner<T> {
    override fun rowFor(list: List<T, *>, y: Double): Int {
        return max(0, ((y - list.insets.top) / height).toInt())
    }

    override fun invoke(list: List<T, *>, row: T, index: Int): Rectangle {
        return Rectangle(list.insets.left, list.insets.top + index * height, list.width - list.insets.run { left + right }, height)
    }
}

class BasicListUI<T>(textMetrics: TextMetrics): ListRenderer<T> {
    override val positioner: ItemPositioner<T> = BasicPositioner(20.0)

    override val uiGenerator = LabelItemUIGenerator<T>(textMetrics)

    override fun render(gizmo: List<T, *>, canvas: Canvas) {
        canvas.rect(gizmo.bounds.atOrigin, Pen(gray))
    }

    override fun install(gizmo: List<T, *>) {
        gizmo.insets = Insets(2.0)
    }
}