package io.nacular.doodle.theme.basic.list

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.list.List
import io.nacular.doodle.controls.list.ListBehavior.RowGenerator
import io.nacular.doodle.controls.list.ListEditor
import io.nacular.doodle.controls.list.MutableList
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.text.TextFit
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Green
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.ColorFill
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyText.Companion.Backspace
import io.nacular.doodle.event.KeyText.Companion.Delete
import io.nacular.doodle.event.KeyText.Companion.Enter
import io.nacular.doodle.event.KeyText.Companion.Escape
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.theme.basic.ListRow
import io.nacular.doodle.utils.Encoder
import io.nacular.doodle.utils.HorizontalAlignment
import io.nacular.doodle.utils.ObservableSet


open class MutableBasicItemGenerator<T>(focusManager         : FocusManager?,
                                        textMetrics          : TextMetrics,
                                        selectionColor       : Color? = Green.lighter(),
                                        selectionBlurredColor: Color? = Lightgray): BasicItemGenerator<T>(focusManager, textMetrics, selectionColor, selectionBlurredColor) {
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

open class BasicMutableListBehavior<T>(generator   : RowGenerator<T>,
                                       evenRowColor: Color? = Color.White,
                                       oddRowColor : Color? = Lightgray.lighter().lighter(),
                                       rowHeight   : Double = 20.0): BasicListBehavior<T>(generator, evenRowColor, oddRowColor, rowHeight) {

    constructor(focusManager         : FocusManager?,
                textMetrics          : TextMetrics,
                evenRowColor         : Color? = Color.White,
                oddRowColor          : Color? = Lightgray.lighter().lighter(),
                selectionColor       : Color? = Green.lighter(),
                selectionBlurredColor: Color? = Lightgray): this(MutableBasicItemGenerator(focusManager, textMetrics, selectionColor, selectionBlurredColor), evenRowColor, oddRowColor)

    override fun keyPressed(event: KeyEvent) {
        when (event.key) {
            Delete, Backspace -> (event.source as MutableList<*, *>).let { list ->
                list.selection.sortedByDescending { it }.forEach { list.removeAt(it) }
            }
            else                                      -> super.keyPressed(event)
        }
    }
}

open class TextEditOperation<T>(
        private val focusManager: FocusManager?,
        private val encoder     : Encoder<T, String>,
        private val list        : MutableList<T, *>,
                    row         : T,
        private var index       : Int,
                    current     : View): TextField(), EditOperation<T> {

    private val listSelectionChanged = { _: ObservableSet<Int>,_: Set<Int>,_:  Set<Int> ->
        list.cancelEditing()
    }

    init {
        text                = encoder.encode(row) ?: ""
        fitText             = setOf(TextFit.Width, TextFit.Height)
        borderVisible       = false
        foregroundColor     = current.foregroundColor
        backgroundColor     = current.backgroundColor
        horizontalAlignment = HorizontalAlignment.Left

        styleChanged += { rerender() }

        focusChanged += { _,_,_ ->
            if (!hasFocus) {
                list.cancelEditing()
            }
        }

        keyChanged += object: KeyListener {
            override fun keyReleased(event: KeyEvent) {
                when (event.key) {
                    Enter  -> { list.completeEditing(); focusManager?.requestFocus(list) }
                    Escape -> { list.cancelEditing  (); focusManager?.requestFocus(list) }
                }
            }
        }

        list.selectionChanged += listSelectionChanged
    }

    override fun addedToDisplay() {
        focusManager?.requestFocus(this)
        selectAll()
    }

    override fun invoke() = object: View() {
        init {
            children += this@TextEditOperation

            layout = constrain(this@TextEditOperation) {
                it.centerY = it.parent.centerY
            }
        }

        override fun render(canvas: Canvas) {
            this@TextEditOperation.backgroundColor?.let { canvas.rect(bounds.atOrigin.inset(Insets(top = 1.0)), ColorFill(it)) }
        }
    }

    override fun complete() = encoder.decode(text)

    override fun cancel() {
        list.selectionChanged -= listSelectionChanged
    }
}

open class ListTextEditor<T>(private val focusManager: FocusManager?, private val encoder: Encoder<T, String>): ListEditor<T> {
    override fun edit(list: MutableList<T, *>, row: T, index: Int, current: View): EditOperation<T> = TextEditOperation(focusManager, encoder, list, row, index, current)
}