package com.nectar.doodle.controls.theme.basic.list

import com.nectar.doodle.controls.Selectable
import com.nectar.doodle.controls.ToStringItemGenerator
import com.nectar.doodle.controls.list.EditOperation
import com.nectar.doodle.controls.list.List
import com.nectar.doodle.controls.list.ListBehavior
import com.nectar.doodle.controls.list.ListBehavior.RowGenerator
import com.nectar.doodle.controls.list.ListBehavior.RowPositioner
import com.nectar.doodle.controls.list.ListEditor
import com.nectar.doodle.controls.ListModel
import com.nectar.doodle.controls.list.MutableList
import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.controls.theme.basic.ListPositioner
import com.nectar.doodle.controls.theme.basic.ListRow
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.CanvasBrush
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyEvent.Companion.VK_A
import com.nectar.doodle.event.KeyEvent.Companion.VK_BACKSPACE
import com.nectar.doodle.event.KeyEvent.Companion.VK_DELETE
import com.nectar.doodle.event.KeyEvent.Companion.VK_DOWN
import com.nectar.doodle.event.KeyEvent.Companion.VK_RETURN
import com.nectar.doodle.event.KeyEvent.Companion.VK_UP
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Meta
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift
import com.nectar.doodle.utils.Encoder
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.ObservableSet

/**
 * Created by Nicholas Eddy on 3/20/18.
 */

private open class BasicItemGenerator<T>(private val focusManager: FocusManager?, private val textMetrics: TextMetrics): RowGenerator<T> {
    override fun invoke(list: List<T, *>, row: T, index: Int, current: View?): View = when (current) {
        is ListRow<*> -> (current as ListRow<T>).apply { update(list, row, index) }
        else          -> ListRow(list, row, index, list.itemGenerator ?: ToStringItemGenerator(textMetrics)).apply {
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

private class MutableBasicItemGenerator<T>(focusManager: FocusManager?, textMetrics: TextMetrics): BasicItemGenerator<T>(focusManager, textMetrics) {
    override fun invoke(list: List<T, *>, row: T, index: Int, current: View?) = super.invoke(list, row, index, current).also {
        if (current !is ListRow<*>) {
            val result = it as ListRow<*>

            it.mouseChanged += object: MouseListener {
                override fun mouseReleased(event: MouseEvent) {
                    if (event.clickCount == 2) {
                        (list as MutableList).startEditing(result.index)
                    }
                }
            }
        }
    }
}

open class BasicListBehavior<T>(focusManager: FocusManager?, textMetrics: TextMetrics, private val rowHeight: Double = 20.0): ListBehavior<T>, KeyListener {
    override val generator : RowGenerator<T>  = BasicItemGenerator (focusManager, textMetrics)
    override val positioner: RowPositioner<T> = BasicListPositioner(rowHeight                )

    override fun install(view: List<T, *>) {
        view.keyChanged += this
    }

    override fun uninstall(view: List<T, *>) {
        view.keyChanged -= this
    }

    override fun render(view: List<T, *>, canvas: Canvas) {
        canvas.rect(view.bounds.atOrigin, CanvasBrush(Size(rowHeight, 2 * rowHeight)) {
            rect(Rectangle(                rowHeight, rowHeight), ColorBrush(lightgray.lighter()))
            rect(Rectangle(0.0, rowHeight, rowHeight, rowHeight), ColorBrush(lightgray          ))
        })
    }

    override fun keyPressed(event: KeyEvent) {
        (event.source as Selectable<Int>).let { list ->
            when (event.code) {
                VK_UP, VK_DOWN -> {
                    when (Shift) {
                        in event -> {
                            list.selectionAnchor?.let { anchor ->
                                list.lastSelection?.let { if (event.code == VK_UP) list.previous(it) else list.next(it) }?.let { current ->
                                    when {
                                        current < anchor  -> list.setSelection((current .. anchor ).reversed().toSet())
                                        anchor  < current -> list.setSelection((anchor  .. current).           toSet())
                                        else              -> list.setSelection(setOf(current))
                                    }
                                }
                            }
                        }
                        else -> list.lastSelection?.let { if (event.code == KeyEvent.VK_UP) list.previous(it) else list.next(it) }?.let { list.setSelection(setOf(it)) }
                    }?.let { Unit } ?: Unit
                }

                VK_A -> {
                    if (Ctrl in event || Meta in event) {
                        list.selectAll()
                    }
                }
            }
        }
    }
}

class BasicMutableListBehavior<T>(focusManager: FocusManager?, textMetrics: TextMetrics): BasicListBehavior<T>(focusManager, textMetrics) {
    override val generator : RowGenerator<T>  = MutableBasicItemGenerator(focusManager, textMetrics)
    override val positioner: RowPositioner<T> = BasicListPositioner(20.0)

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

    private val listSelectionChanged = { _:ObservableSet<out List<*, ListModel<*>>, *>,_: Set<Int>,_:  Set<Int> ->
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
                if (event.code == VK_RETURN) {
                    list.completeEditing()
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