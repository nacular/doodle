package com.nectar.doodle.controls.theme.basic.list

import com.nectar.doodle.controls.EditOperation
import com.nectar.doodle.controls.ToStringItemGenerator
import com.nectar.doodle.controls.list.List
import com.nectar.doodle.controls.list.ListBehavior
import com.nectar.doodle.controls.list.ListBehavior.RowGenerator
import com.nectar.doodle.controls.list.ListBehavior.RowPositioner
import com.nectar.doodle.controls.list.ListEditor
import com.nectar.doodle.controls.list.MutableList
import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.controls.theme.basic.ListPositioner
import com.nectar.doodle.controls.theme.basic.ListRow
import com.nectar.doodle.controls.theme.basic.SelectableListKeyHandler
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.drawing.stripedBrush
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyEvent.Companion.VK_BACKSPACE
import com.nectar.doodle.event.KeyEvent.Companion.VK_DELETE
import com.nectar.doodle.event.KeyEvent.Companion.VK_RETURN
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.utils.Encoder
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.ObservableSet

/**
 * Created by Nicholas Eddy on 3/20/18.
 */

private open class BasicItemGenerator<T>(private val focusManager         : FocusManager?,
                                         private val textMetrics          : TextMetrics,
                                         private val selectionColor       : Color? = green.lighter(),
                                         private val selectionBlurredColor: Color? = lightgray): RowGenerator<T> {
    override fun invoke(list: List<T, *>, row: T, index: Int, current: View?): View = when (current) {
        is ListRow<*> -> (current as ListRow<T>).apply { update(list, row, index) }
        else          -> ListRow(list, row, index, list.itemGenerator ?: ToStringItemGenerator(textMetrics), selectionColor = selectionColor, selectionBluredColor = selectionBlurredColor).apply {
            mouseChanged += object: MouseListener {
                override fun mouseReleased(event: MouseEvent) {
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

private class MutableBasicItemGenerator<T>(focusManager         : FocusManager?,
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

open class BasicListBehavior<T>(override val generator   : RowGenerator<T>,
                                             evenRowColor: Color? = Color.white,
                                             oddRowColor : Color? = lightgray.lighter().lighter(),
                                             rowHeight   : Double = 20.0): ListBehavior<T>, KeyListener, SelectableListKeyHandler {
    constructor(focusManager         : FocusManager?,
                textMetrics          : TextMetrics,
                evenRowColor         : Color? = Color.white,
                oddRowColor          : Color? = lightgray.lighter().lighter(),
                selectionColor       : Color? = Color.green.lighter(),
                selectionBlurredColor: Color? = lightgray): this(BasicItemGenerator(focusManager, textMetrics, selectionColor, selectionBlurredColor), evenRowColor, oddRowColor)

    private val patternBrush = stripedBrush(rowHeight, evenRowColor, oddRowColor)

    override val positioner: RowPositioner<T> = BasicListPositioner(rowHeight)

    override fun install(view: List<T, *>) {
        view.keyChanged += this
    }

    override fun uninstall(view: List<T, *>) {
        view.keyChanged -= this
    }

    override fun render(view: List<T, *>, canvas: Canvas) {
        canvas.rect(view.bounds.atOrigin, patternBrush)
    }

    override fun keyPressed(event: KeyEvent) {
        super<SelectableListKeyHandler>.keyPressed(event)
    }
}

class BasicMutableListBehavior<T>(generator   : RowGenerator<T>,
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
            VK_DELETE, VK_BACKSPACE -> (event.source as MutableList<*,*>).let { list ->
                list.selection.sortedByDescending { it }.forEach { list.removeAt(it) }
            }
            else -> super.keyPressed(event)
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

    private val listSelectionChanged = { _:ObservableSet<Int>,_: Set<Int>,_:  Set<Int> ->
        list.cancelEditing()
    }

    init {
        text                = encoder.encode(row) ?: ""
        fitText             = setOf(TextFit.Width, TextFit.Height)
        borderVisible       = false
        backgroundColor     = current.backgroundColor
        horizontalAlignment = Left

        styleChanged += { rerender() }

        focusChanged += { _,_,_ ->
            if (!hasFocus) {
                list.cancelEditing()
            }
        }

        keyChanged += object: KeyListener {
            override fun keyReleased(event: KeyEvent) {
                when (event.code) {
                    VK_RETURN          -> { list.completeEditing(); focusManager?.requestFocus(list) }
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
            this@TextEditOperation.backgroundColor?.let { canvas.rect(bounds.atOrigin, ColorBrush(it)) }
        }
    }
    override fun complete() = encoder.decode(text)

    override fun cancel() {
        list.selectionChanged -= listSelectionChanged
    }
}

class ListTextEditor<T>(private val focusManager: FocusManager?, private val encoder: Encoder<T, String>): ListEditor<T> {
    override fun edit(list: MutableList<T, *>, row: T, index: Int, current: View): EditOperation<T> = TextEditOperation(focusManager, encoder, list, row, index, current)
}