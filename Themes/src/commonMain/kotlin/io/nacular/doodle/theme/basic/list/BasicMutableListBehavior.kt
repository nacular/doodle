package io.nacular.doodle.theme.basic.list

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.list.List
import io.nacular.doodle.controls.list.ListBehavior.RowGenerator
import io.nacular.doodle.controls.list.ListEditor
import io.nacular.doodle.controls.list.MutableList
import io.nacular.doodle.controls.text.TextFit.Width
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.PatternPaint
import io.nacular.doodle.drawing.horizontalStripedFill
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyText.Companion.Backspace
import io.nacular.doodle.event.KeyText.Companion.Delete
import io.nacular.doodle.event.PointerListener.Companion.released
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.theme.basic.GenericTextEditOperation
import io.nacular.doodle.theme.basic.ListRow
import io.nacular.doodle.utils.Encoder
import io.nacular.doodle.utils.HorizontalAlignment.Left
import io.nacular.doodle.utils.ObservableSet


public open class BasicMutableItemGenerator<T>(selectionColor: Color? = null, selectionBlurredColor: Color? = null): BasicItemGenerator<T>(selectionColor, selectionBlurredColor) {
    override fun invoke(list: List<T, *>, row: T, index: Int, current: View?): View = super.invoke(list, row, index, current).also {
        if (current !is ListRow<*>) {
            val result = it as ListRow<*>

            it.pointerFilter += released { event ->
                if (list.selected(result.index)) {
                    (list as? MutableList)?.startEditing(result.index)
                    event.consume()
                }
            }
        }
    }
}

public open class BasicMutableListBehavior<T>(focusManager: FocusManager? = null,
                                              generator   : RowGenerator<T>,
                                              patternFill : PatternPaint? = null,
                                              rowHeight   : Double): BasicListBehavior<T>(focusManager, generator, patternFill, rowHeight) {
    override fun pressed(event: KeyEvent) {
        when (event.key) {
            Delete, Backspace -> (event.source as MutableList<*, *>).let { list ->
                list.selection.sortedByDescending { it }.forEach { list.removeAt(it) }
            }
            else              -> super.pressed(event)
        }
    }

    public companion object {
        public operator fun <T> invoke(
                focusManager: FocusManager?,
                generator   : RowGenerator<T>,
                evenRowColor: Color?,
                oddRowColor : Color?,
                rowHeight   : Double): BasicMutableListBehavior<T> = BasicMutableListBehavior(
                    focusManager,
                    generator,
                    when {
                        evenRowColor != null || oddRowColor != null -> horizontalStripedFill(rowHeight, evenRowColor, oddRowColor)
                        else                                        -> null
                    },
                    rowHeight)

        public operator fun <T> invoke(
                focusManager         : FocusManager? = null,
                evenRowColor         : Color?        = null,
                oddRowColor          : Color?        = null,
                selectionColor       : Color?        = null,
                selectionBlurredColor: Color?        = null,
                rowHeight            : Double): BasicMutableListBehavior<T> = BasicMutableListBehavior<T>(focusManager, BasicMutableItemGenerator(selectionColor, selectionBlurredColor), evenRowColor, oddRowColor, rowHeight)
    }
}

public open class TextEditOperation<T>(
                    focusManager: FocusManager?,
                    mapper      : Encoder<T, String>,
        private val list        : MutableList<T, *>,
                    row         : T,
                    current     : View): GenericTextEditOperation<T, MutableList<T, *>>(focusManager, mapper, list, row, current) {

    private val listSelectionChanged = { _: ObservableSet<Int>,_: Set<Int>,_:  Set<Int> ->
        list.cancelEditing()
    }

    init {
        list.selectionChanged += listSelectionChanged

        textField.horizontalAlignment = Left
        textField.fitText             = setOf(Width)
        textField.backgroundColor     = current.backgroundColor
    }

    override fun invoke(): View = object: View() {
        init {
            children += textField

            layout = constrain(textField) {
                it.height = parent.height - 1
                it.bottom = parent.bottom
            }
        }

        override fun render(canvas: Canvas) {
            textField.backgroundColor?.let { canvas.rect(bounds.atOrigin.inset(Insets(top = 1.0)), ColorPaint(it)) }
        }
    }

    override fun cancel() {
        list.selectionChanged -= listSelectionChanged
    }
}

public open class ListTextEditor<T>(private val focusManager: FocusManager?, private val encoder: Encoder<T, String>): ListEditor<T> {
    override fun edit(list: MutableList<T, *>, row: T, index: Int, current: View): EditOperation<T> = TextEditOperation(focusManager, encoder, list, row, current)
}