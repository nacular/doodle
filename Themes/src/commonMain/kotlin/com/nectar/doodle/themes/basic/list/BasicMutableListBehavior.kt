package com.nectar.doodle.themes.basic.list

import com.nectar.doodle.controls.EditOperation
import com.nectar.doodle.controls.list.List
import com.nectar.doodle.controls.list.ListBehavior.RowGenerator
import com.nectar.doodle.controls.list.ListEditor
import com.nectar.doodle.controls.list.MutableList
import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.controls.text.TextFit
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.themes.basic.ListRow
import com.nectar.doodle.utils.Encoder
import com.nectar.doodle.utils.HorizontalAlignment
import com.nectar.doodle.utils.ObservableSet


open class MutableBasicItemGenerator<T>(focusManager         : FocusManager?,
                                        textMetrics          : TextMetrics,
                                        selectionColor       : Color? = green.lighter(),
                                        selectionBlurredColor: Color? = lightgray): BasicItemGenerator<T>(focusManager, textMetrics, selectionColor, selectionBlurredColor) {
    override fun invoke(list: List<T, *>, row: T, index: Int, current: View?) = super.invoke(list, row, index, current).also {
        if (current !is ListRow<*>) {
            val result = it as ListRow<*>

            it.mouseFilter += object: MouseListener {
                override fun mouseReleased(event: MouseEvent) {
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
                                       evenRowColor: Color? = Color.white,
                                       oddRowColor : Color? = lightgray.lighter().lighter(),
                                       rowHeight   : Double = 20.0): BasicListBehavior<T>(generator, evenRowColor, oddRowColor, rowHeight) {

    constructor(focusManager         : FocusManager?,
                textMetrics          : TextMetrics,
                evenRowColor         : Color? = Color.white,
                oddRowColor          : Color? = lightgray.lighter().lighter(),
                selectionColor       : Color? = Color.green.lighter(),
                selectionBlurredColor: Color? = lightgray): this(MutableBasicItemGenerator(focusManager, textMetrics, selectionColor, selectionBlurredColor), evenRowColor, oddRowColor)

    override fun keyPressed(event: KeyEvent) {
        when (event.code) {
            KeyEvent.VK_DELETE, KeyEvent.VK_BACKSPACE -> (event.source as MutableList<*, *>).let { list ->
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
                when (event.code) {
                    KeyEvent.VK_RETURN -> { list.completeEditing(); focusManager?.requestFocus(list) }
                    KeyEvent.VK_ESCAPE -> { list.cancelEditing  (); focusManager?.requestFocus(list) }
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
            this@TextEditOperation.backgroundColor?.let { canvas.rect(bounds.atOrigin.inset(Insets(top = 1.0)), ColorBrush(it)) }
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