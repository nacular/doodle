package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.Selectable
import com.nectar.doodle.controls.text.Label
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.system.SystemInputEvent
import com.nectar.doodle.text.StyledText
import com.nectar.doodle.utils.HorizontalAlignment
import com.nectar.doodle.utils.isEven
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 4/8/19.
 */
class ListRow<T>(textMetrics: TextMetrics,
        list: Selectable<Int>,
        row: T,
        var index: Int,
        val evenRowColor: Color? = null,
        val oddRowColor : Color? = evenRowColor?.darker()): Label(textMetrics, StyledText(row.toString())) {

    var colorPolicy: (ListRow<T>) -> Color? = {
        val color = when {
            list.selected(index) -> green
            it.index.isEven      -> evenRowColor
            else                 -> oddRowColor
        }

        if (it.mouseOver) color?.lighter(0.25f) else color
    }

    private  var mouseOver  = false
//    private  var background = null as Color?

    init {
        fitText             = false
        horizontalAlignment = HorizontalAlignment.Left

        styleChanged += { rerender() }

        mouseChanged += object: MouseListener {
            private var pressed = false

            override fun mouseEntered(event: MouseEvent) {
                mouseOver       = true
                backgroundColor = colorPolicy(this@ListRow) //backgroundColor?.let(colorPolicy)
            }

            override fun mouseExited(event: MouseEvent) {
                mouseOver       = false
                backgroundColor = colorPolicy(this@ListRow) //background
            }

            override fun mousePressed(event: MouseEvent) {
                pressed = true
            }

            override fun mouseReleased(event: MouseEvent) {
                if (mouseOver && pressed) {
                    setOf(index).also {
                        list.apply {
                            when {
                                SystemInputEvent.Modifier.Ctrl in event.modifiers || SystemInputEvent.Modifier.Meta in event.modifiers -> toggleSelection(it)
                                SystemInputEvent.Modifier.Shift in event.modifiers && lastSelection != null                            -> {
                                    selectionAnchor?.let { anchor ->
                                        val current = index
                                        when {
                                            current < anchor  -> setSelection((current .. anchor ).reversed().toSet())
                                            anchor  < current -> setSelection((anchor  .. current).           toSet())
                                        }
                                    }
                                }
                                else                                                                                                   -> setSelection(it)
                            }
                        }
                    }
                }
                pressed = false
            }
        }

        update(list, row, index)
    }

    fun update(list: Selectable<Int>, row: Any?, index: Int) {
        text       = row.toString()
        this.index = index

//        background      = if (list.selected(index)) striped(green) else null
        backgroundColor = colorPolicy(this@ListRow) //background ?: if (mouseOver) (if (index.isEven) evenRowColor else oddRowColor)?.let(colorPolicy) else null
    }

//    private fun striped(color: Color): Color = when {
//        index.isEven -> color.lighter()
//        else         -> color
//    }
}

open class ListPositioner(private val height: Double) {
    fun rowFor(insets: Insets, y: Double) = max(0, ((y - insets.top) / height).toInt())

    fun invoke(list: View, insets: Insets, index: Int) = Rectangle(insets.left, insets.top + index * height, list.width - insets.run { left + right }, height)
}
