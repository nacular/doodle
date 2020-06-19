package io.nacular.doodle.theme.basic.list

import io.nacular.doodle.controls.TextItemVisualizer
import io.nacular.doodle.controls.ignoreIndex
import io.nacular.doodle.controls.list.List
import io.nacular.doodle.controls.list.ListBehavior
import io.nacular.doodle.controls.list.ListBehavior.RowGenerator
import io.nacular.doodle.controls.list.ListBehavior.RowPositioner
import io.nacular.doodle.controls.toString
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Green
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.horizontalStripedFill
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.theme.basic.ListPositioner
import io.nacular.doodle.theme.basic.ListRow
import io.nacular.doodle.theme.basic.SelectableListKeyHandler

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
                rowHeight            : Double,
                evenRowColor         : Color? = White,
                oddRowColor          : Color? = Lightgray.lighter().lighter(),
                selectionColor       : Color? = Green.lighter(),
                selectionBlurredColor: Color? = Lightgray): this(BasicItemGenerator(focusManager, textMetrics, selectionColor, selectionBlurredColor), evenRowColor, oddRowColor, rowHeight)

    private val patternFill = if (evenRowColor != null || oddRowColor != null) horizontalStripedFill(rowHeight, evenRowColor, oddRowColor) else null

    override val positioner: RowPositioner<T> = BasicListPositioner(rowHeight)

    override fun install(view: List<T, *>) {
        view.keyChanged += this

        view.rerender()
    }

    override fun uninstall(view: List<T, *>) {
        view.keyChanged -= this
    }

    override fun render(view: List<T, *>, canvas: Canvas) {
        patternFill?.let { canvas.rect(view.bounds.atOrigin, it) }
    }

    override fun keyPressed(event: KeyEvent) {
        super<SelectableListKeyHandler>.keyPressed(event)
    }
}