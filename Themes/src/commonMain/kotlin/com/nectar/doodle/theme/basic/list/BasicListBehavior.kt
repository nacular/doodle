package com.nectar.doodle.theme.basic.list

import com.nectar.doodle.controls.TextItemVisualizer
import com.nectar.doodle.controls.ignoreIndex
import com.nectar.doodle.controls.list.List
import com.nectar.doodle.controls.list.ListBehavior
import com.nectar.doodle.controls.list.ListBehavior.RowGenerator
import com.nectar.doodle.controls.list.ListBehavior.RowPositioner
import com.nectar.doodle.controls.toString
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.Green
import com.nectar.doodle.drawing.Color.Companion.Lightgray
import com.nectar.doodle.drawing.Color.Companion.White
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.drawing.horizontalStripedBrush
import com.nectar.doodle.drawing.lighter
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.PointerEvent
import com.nectar.doodle.event.PointerListener
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.theme.basic.ListPositioner
import com.nectar.doodle.theme.basic.ListRow
import com.nectar.doodle.theme.basic.SelectableListKeyHandler

/**
 * Created by Nicholas Eddy on 3/20/18.
 */

open class BasicItemGenerator<T>(private val focusManager         : FocusManager?,
                                 private val textMetrics          : TextMetrics,
                                 private val selectionColor       : Color? = Green.lighter(),
                                 private val selectionBlurredColor: Color? = Lightgray): RowGenerator<T> {
    override fun invoke(list: List<T, *>, row: T, index: Int, current: View?): View = when (current) {
        is ListRow<*> -> (current as ListRow<T>).apply { update(list, row, index) }
        else          -> ListRow(list, row, index, list.itemVisualizer ?: ignoreIndex(toString(TextItemVisualizer(textMetrics))), backgroundSelectionColor = selectionColor, backgroundSelectionBlurredColor = selectionBlurredColor).apply {
            pointerChanged += object: PointerListener {
                override fun released(event: PointerEvent) {
                    focusManager?.requestFocus(list)
                }
            }
        }
    }
}

private class BasicListPositioner<T>(height: Double): ListPositioner(height), RowPositioner<T> {
    override fun rowFor(list: List<T, *>, y: Double) = super.rowFor(list.insets, y)

    override fun invoke(list: List<T, *>, row: T, index: Int) = super.invoke(list, list.insets, index)
}

open class BasicListBehavior<T>(override val generator   : RowGenerator<T>,
                                             evenRowColor: Color? = White,
                                             oddRowColor : Color? = Lightgray.lighter().lighter(),
                                             rowHeight   : Double = 20.0): ListBehavior<T>, KeyListener, SelectableListKeyHandler {
    constructor(focusManager         : FocusManager?,
                textMetrics          : TextMetrics,
                evenRowColor         : Color? = White,
                oddRowColor          : Color? = Lightgray.lighter().lighter(),
                selectionColor       : Color? = Green.lighter(),
                selectionBlurredColor: Color? = Lightgray): this(BasicItemGenerator(focusManager, textMetrics, selectionColor, selectionBlurredColor), evenRowColor, oddRowColor)

    private val patternBrush = if (evenRowColor != null || oddRowColor != null) horizontalStripedBrush(rowHeight, evenRowColor, oddRowColor) else null

    override val positioner: RowPositioner<T> = BasicListPositioner(rowHeight)

    override fun install(view: List<T, *>) {
        view.keyChanged += this

        view.rerender()
    }

    override fun uninstall(view: List<T, *>) {
        view.keyChanged -= this
    }

    override fun render(view: List<T, *>, canvas: Canvas) {
        patternBrush?.let { canvas.rect(view.bounds.atOrigin, it) }
    }

    override fun keyPressed(event: KeyEvent) {
        super<SelectableListKeyHandler>.keyPressed(event)
    }
}