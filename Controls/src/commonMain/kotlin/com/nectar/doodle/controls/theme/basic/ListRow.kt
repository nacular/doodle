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
class ListRow<T>(    textMetrics : TextMetrics,
                 var list        : Selectable<Int>,
                 var row         : T,
                 var index       : Int,
                 val evenRowColor: Color? = null,
                 val oddRowColor : Color? = evenRowColor?.darker()): Label(textMetrics, StyledText(row.toString())) {

    var colorPolicy: (ListRow<T>) -> Color? = {
        val color = when {
            it.index.isEven -> if (list.selected(index)) green           else evenRowColor
            else            -> if (list.selected(index)) green.lighter() else oddRowColor
        }

        if (it.mouseOver) color?.lighter(0.25f) else color
    }

    private  var mouseOver  = false

    init {
        fitText             = false
        horizontalAlignment = HorizontalAlignment.Left

        styleChanged += { rerender() }

        mouseChanged += object: MouseListener {
            private var pressed = false

            override fun mouseEntered(event: MouseEvent) {
                mouseOver       = true
                backgroundColor = colorPolicy(this@ListRow)
            }

            override fun mouseExited(event: MouseEvent) {
                mouseOver       = false
                backgroundColor = colorPolicy(this@ListRow)
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

    fun update(list: Selectable<Int>, row: T, index: Int) {
        this.list  = list
        this.row   = row
        this.index = index
        text       = row.toString()

        backgroundColor = colorPolicy(this)
    }
}

open class ListPositioner(private val height: Double) {
    fun rowFor(insets: Insets, y: Double) = max(0, ((y - insets.top) / height).toInt())

    fun invoke(list: View, insets: Insets, index: Int) = Rectangle(insets.left, insets.top + index * height, list.width - insets.run { left + right }, height)
}
