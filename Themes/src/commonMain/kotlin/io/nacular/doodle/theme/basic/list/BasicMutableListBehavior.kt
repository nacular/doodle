package io.nacular.doodle.theme.basic.list

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.list.List
import io.nacular.doodle.controls.list.ListBehavior.RowGenerator
import io.nacular.doodle.controls.list.ListEditor
import io.nacular.doodle.controls.list.MutableList
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.text.TextFit.Width
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorFill
import io.nacular.doodle.drawing.PatternFill
import io.nacular.doodle.drawing.horizontalStripedFill
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.KeyText
import io.nacular.doodle.event.KeyText.Companion.Backspace
import io.nacular.doodle.event.KeyText.Companion.Delete
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.theme.basic.ListRow
import io.nacular.doodle.utils.Encoder
import io.nacular.doodle.utils.HorizontalAlignment
import io.nacular.doodle.utils.ObservableSet


open class BasicMutableItemGenerator<T>(selectionColor: Color? = null, selectionBlurredColor: Color? = null): BasicItemGenerator<T>(selectionColor, selectionBlurredColor) {
    override fun invoke(list: List<T, *>, row: T, index: Int, current: View?) = super.invoke(list, row, index, current).also {
        if (current !is ListRow<*>) {
            val result = it as ListRow<*>

            it.pointerFilter += object: PointerListener {
                override fun released(event: PointerEvent) {
                    if (list.selected(result.index)) {
                        (list as? MutableList)?.startEditing(result.index)
                        event.consume()
                    }
                }
            }
        }
    }
}

open class BasicMutableListBehavior<T>(focusManager: FocusManager? = null,
                                       generator   : RowGenerator<T>,
                                       patternFill : PatternFill? = null,
                                       rowHeight   : Double): BasicListBehavior<T>(focusManager, generator, patternFill, rowHeight) {
    override fun keyPressed(event: KeyEvent) {
        when (event.key) {
            Delete, Backspace -> (event.source as MutableList<*, *>).let { list ->
                list.selection.sortedByDescending { it }.forEach { list.removeAt(it) }
            }
            else              -> super.keyPressed(event)
        }
    }

    companion object {
        operator fun <T> invoke(
                focusManager: FocusManager?,
                generator   : RowGenerator<T>,
                evenRowColor: Color?,
                oddRowColor : Color?,
                rowHeight   : Double) = BasicMutableListBehavior(
                    focusManager,
                    generator,
                    when {
                        evenRowColor != null || oddRowColor != null -> horizontalStripedFill(rowHeight, evenRowColor, oddRowColor)
                        else                                        -> null
                    },
                    rowHeight)

        operator fun <T> invoke(
                focusManager         : FocusManager? = null,
                evenRowColor         : Color?        = null,
                oddRowColor          : Color?        = null,
                selectionColor       : Color?        = null,
                selectionBlurredColor: Color?        = null,
                rowHeight            : Double) = BasicMutableListBehavior<T>(focusManager, BasicMutableItemGenerator(selectionColor, selectionBlurredColor), evenRowColor, oddRowColor, rowHeight)
    }
}

open class TextEditOperation<T>(
        private val focusManager: FocusManager?,
        private val mapper      : Encoder<T, String>,
        private val list        : MutableList<T, *>,
                    row         : T,
                    current     : View): EditOperation<T> {

    protected open val cancelOnFocusLost = true

    protected val textField = TextField().apply {
        text                = mapper.encode(row) ?: ""
        fitText             = setOf(Width)
        borderVisible       = false
        font                = current.font
        foregroundColor     = current.foregroundColor
        backgroundColor     = current.backgroundColor
        horizontalAlignment = HorizontalAlignment.Left

        styleChanged += { rerender() }

        focusChanged += { _,_,_ ->
            if (!hasFocus && cancelOnFocusLost) {
                list.cancelEditing()
            }
        }

        keyChanged += object: KeyListener {
            override fun keyReleased(event: KeyEvent) {
                when (event.key) {
                    KeyText.Enter  -> { list.completeEditing(); focusManager?.requestFocus(list) }
                    KeyText.Escape -> { list.cancelEditing  (); focusManager?.requestFocus(list) }
                }
            }
        }

        displayChange += { _,_, displayed ->
            if (displayed) {
                focusManager?.requestFocus(this)
                selectAll()
            }
        }
    }

    private val listSelectionChanged = { _: ObservableSet<Int>,_: Set<Int>,_:  Set<Int> ->
        list.cancelEditing()
    }

    init {
        list.selectionChanged += listSelectionChanged
    }

    override fun invoke() = object: View() {
        init {
            children += textField

            layout = constrain(textField) {
                it.height = parent.height - 1
                it.bottom = parent.bottom
            }
        }

        override fun render(canvas: Canvas) {
            textField.backgroundColor?.let { canvas.rect(bounds.atOrigin.inset(Insets(top = 1.0)), ColorFill(it)) }
        }
    }

    override fun complete() = mapper.decode(textField.text)

    override fun cancel() {
        list.selectionChanged -= listSelectionChanged
    }
}

open class ListTextEditor<T>(private val focusManager: FocusManager?, private val encoder: Encoder<T, String>): ListEditor<T> {
    override fun edit(list: MutableList<T, *>, row: T, index: Int, current: View): EditOperation<T> = TextEditOperation(focusManager, encoder, list, row, current)
}