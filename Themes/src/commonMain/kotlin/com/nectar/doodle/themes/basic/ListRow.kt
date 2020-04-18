package com.nectar.doodle.themes.basic

import com.nectar.doodle.controls.IndexedItemVisualizer
import com.nectar.doodle.controls.list.ListLike
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.blue
import com.nectar.doodle.drawing.Color.Companion.white
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
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 4/8/19.
 */
open class ListRow<T>(private var list                           : ListLike,
                      private var row                            : T,
                              var index                          : Int,
                      private val itemVisualizer                 : IndexedItemVisualizer<T>,
                      private val foregroundSelectionColor       : Color? = white,
                      private val foregroundSelectionBlurredColor: Color? = foregroundSelectionColor,
                      private val backgroundSelectionColor       : Color? = blue,
                      private val backgroundSelectionBlurredColor: Color? = backgroundSelectionColor): View() {

    var positioner: Constraints.() -> Unit = { centerY = parent.centerY }
        set(new) {
            if (field == new) {
                return
            }

            field = new

            layout = constrain(children[0]) {
                positioner(it)
            }
        }

    private var mouseOver = false

    private val listFocusChanged = { _:View, _:Boolean, new:Boolean ->
        if (list.selected(index)) {
            backgroundColor                         = if (new) backgroundSelectionColor else backgroundSelectionBlurredColor
            children.firstOrNull()?.foregroundColor = if (new) foregroundSelectionColor else foregroundSelectionBlurredColor
        }
    }

    init {
        childrenChanged += { _,_,_,_ ->
            layout = constrain(children[0]) {
                positioner(it)
            }
        }

        children += itemVisualizer(row, index)

        styleChanged += { rerender() }
        mouseChanged += object: MouseListener {
            private var pressed = false

            override fun mouseEntered(event: MouseEvent) {
                mouseOver = true
            }

            override fun mouseExited(event: MouseEvent) {
                mouseOver = false
            }

            override fun mousePressed(event: MouseEvent) {
                pressed = true
            }

            override fun mouseReleased(event: MouseEvent) {
                if (mouseOver && pressed) {
                    setOf(index).also {
                        list.apply {
                            when {
                                Ctrl  in event.modifiers || Meta in event.modifiers -> toggleSelection(it)
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

    fun update(list: ListLike, row: T, index: Int) {
        this.list  = list
        this.row   = row
        this.index = index

        val listSelected = list.selected(index)

        children[0] = itemVisualizer(row, index, children.firstOrNull()) { listSelected }

        when {
            listSelected -> {
                list.focusChanged += listFocusChanged

                backgroundColor                         = if (list.hasFocus) backgroundSelectionColor else backgroundSelectionBlurredColor
                children.firstOrNull()?.foregroundColor = if (list.hasFocus) foregroundSelectionColor else foregroundSelectionBlurredColor
            }
            else         -> {
                list.focusChanged -= listFocusChanged

                backgroundColor                         = null
                children.firstOrNull()?.foregroundColor = null
            }
        }
    }

    override fun render(canvas: Canvas) {
        backgroundColor?.let { canvas.rect(bounds.atOrigin.inset(Insets(top = 1.0)), ColorBrush(it)) }
    }
}

open class ListPositioner(private val height: Double) {
    fun rowFor(insets: Insets, y: Double) = max(0, ((y - insets.top) / height).toInt())

    operator fun invoke(list: View, insets: Insets, index: Int) = Rectangle(insets.left, insets.top + index * height, list.width - insets.run { left + right }, height)
}