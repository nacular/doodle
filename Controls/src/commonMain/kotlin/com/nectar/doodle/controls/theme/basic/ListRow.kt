package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.ItemGenerator
import com.nectar.doodle.controls.Selectable
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.layout.Constraints
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Meta
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift
import com.nectar.doodle.utils.isEven
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 4/8/19.
 */
class ListRow<T>(private var list          : Selectable<Int>,
                 private var row           : T,
                         var index         : Int,
                 private val itemGenerator : ItemGenerator<T>,
                 private val evenRowColor  : Color? = null,
                 private val oddRowColor   : Color? = evenRowColor?.darker(),
                 private val selectionColor: Color? = green): View() {

    var colorPolicy: (ListRow<T>) -> Color? = {
        val color = when {
            it.index.isEven -> if (list.selected(index) && selectionColor != null) selectionColor.lighter() else evenRowColor
            else            -> if (list.selected(index) && selectionColor != null) selectionColor           else oddRowColor
        }

        if (it.mouseOver) color?.lighter(0.25f) else color
    }

    var positioner: Constraints.() -> Unit = { centerY = parent.centerY }
        set(new) {
            field = new

            layout = constrain(children[0]) {
                positioner(it)
            }
        }

    private var mouseOver = false

    init {
        children += itemGenerator(row)

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
                                Ctrl in event.modifiers || Meta in event.modifiers -> toggleSelection(it)
                                Shift in event.modifiers && lastSelection != null  -> {
                                    selectionAnchor?.let { anchor ->
                                        val current = index
                                        when {
                                            current < anchor  -> setSelection((current .. anchor ).reversed().toSet())
                                            anchor  < current -> setSelection((anchor  .. current).           toSet())
                                        }
                                    }
                                }
                                else                                               -> setSelection(it)
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

        children[0] = itemGenerator(row, children.getOrNull(0))

        layout = constrain(children[0]) {
            positioner(it)
        }

        backgroundColor = colorPolicy(this)
    }

    override fun render(canvas: Canvas) {
        backgroundColor?.let { canvas.rect(bounds.atOrigin, ColorBrush(it)) }
    }
}

open class ListPositioner(private val height: Double) {
    fun rowFor(insets: Insets, y: Double) = max(0, ((y - insets.top) / height).toInt())

    operator fun invoke(list: View, insets: Insets, index: Int) = Rectangle(insets.left, insets.top + index * height, list.width - insets.run { left + right }, height)
}