package com.nectar.doodle.controls.theme.basic.list

import com.nectar.doodle.controls.list.EditOperation
import com.nectar.doodle.controls.list.List
import com.nectar.doodle.controls.list.ListBehavior
import com.nectar.doodle.controls.list.ListBehavior.ItemGenerator
import com.nectar.doodle.controls.list.ListBehavior.ItemPositioner
import com.nectar.doodle.controls.list.ListEditor
import com.nectar.doodle.controls.list.Model
import com.nectar.doodle.controls.list.MutableList
import com.nectar.doodle.controls.text.Label
import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyEvent.Companion.VK_BACKSPACE
import com.nectar.doodle.event.KeyEvent.Companion.VK_DELETE
import com.nectar.doodle.event.KeyEvent.Companion.VK_RETURN
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Meta
import com.nectar.doodle.text.StyledText
import com.nectar.doodle.utils.Encoder
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.ObservableSet
import com.nectar.doodle.utils.isEven
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 3/20/18.
 */

private class ListRow<T>(textMetrics: TextMetrics, list: List<*, *>, row: T, var index: Int): Label(textMetrics, StyledText(row.toString())) {

    private var background = lightgray

    init {
        fitText             = false
        horizontalAlignment = Left

        styleChanged += { rerender() }

        mouseChanged += object: MouseListener {
            private var pressed   = false
            private var mouseOver = false

            override fun mouseEntered(event: MouseEvent) {
                mouseOver       = true
                backgroundColor = backgroundColor?.lighter(0.25f)
            }

            override fun mouseExited(event: MouseEvent) {
                mouseOver       = false
                backgroundColor = background
            }

            override fun mousePressed(event: MouseEvent) {
                pressed = true
            }

            override fun mouseReleased(event: MouseEvent) {
                if (mouseOver && pressed) {
                    setOf(index).also {
                        list.apply {
                            when {
                                Ctrl in event.modifiers || Meta in event.modifiers -> if (selected(index)) removeSelection(it) else addSelection(it)
                                else                                               -> setSelection   (it)
                            }
                        }
                    }
                }
                pressed = false
            }
        }

        update(list, row, index)
    }

    fun update(list: List<*, *>, row: Any?, index: Int) {
        text       = row.toString()
        this.index = index
        background = if (list.selected(index)) green else lightgray

        background = when {
            index.isEven -> background.lighter()
            else         -> background
        }

        backgroundColor = background
    }
}

private open class LabelItemGenerator<T>(private val textMetrics: TextMetrics): ItemGenerator<T> {
    override fun invoke(list: List<T, *>, row: T, index: Int, current: View?): View = when (current) {
        is ListRow<*> -> current.apply { update(list, row, index) }
        else          -> ListRow(textMetrics, list, row, index)
    }
}

private class BasicListPositioner<T>(private val height: Double): ItemPositioner<T> {
    override fun rowFor(list: List<T, *>, y: Double): Int {
        return max(0, ((y - list.insets.top) / height).toInt())
    }

    override fun invoke(list: List<T, *>, row: T, index: Int): Rectangle {
        return Rectangle(list.insets.left, list.insets.top + index * height, list.width - list.insets.run { left + right }, height)
    }
}

private class MutableLabelItemGenerator<T>(private val focusManager: FocusManager?, textMetrics: TextMetrics): LabelItemGenerator<T>(textMetrics) {
    override fun invoke(list: List<T, *>, row: T, index: Int, current: View?) = super.invoke(list, row, index, current).also {
        if (current !is ListRow<*>) {
            val result = it as ListRow<*>

            it.mouseChanged += object: MouseListener {
                override fun mouseReleased(event: MouseEvent) {
                    if (event.clickCount == 2) {
                        (list as MutableList).startEditing(result.index)
                    } else {
                        focusManager?.requestFocus(list)
                    }
                }
            }
        }
    }
}

class BasicListBehavior<T>(textMetrics: TextMetrics): ListBehavior<T> {
    override val positioner : ItemPositioner<T>  = BasicListPositioner (20.0       )
    override val generator: ItemGenerator<T> = LabelItemGenerator(textMetrics)

    override fun render(view: List<T, *>, canvas: Canvas) {}
}

class BasicMutableListBehavior<T>(focusManager: FocusManager?, textMetrics: TextMetrics): ListBehavior<T>, KeyListener {
    override val positioner : ItemPositioner<T>  = BasicListPositioner(20.0)
    override val generator: ItemGenerator<T> = MutableLabelItemGenerator(focusManager, textMetrics)

    override fun render(view: List<T, *>, canvas: Canvas) {}

    override fun install(view: List<T, *>) {
        view.keyChanged += this
    }

    override fun uninstall(view: List<T, *>) {
        view.keyChanged -= this
    }

    override fun keyPressed(event: KeyEvent) {
        when (event.code) {
            VK_DELETE, VK_BACKSPACE -> (event.source as MutableList<*,*>).let { list ->
                list.selection.sortedByDescending { it }.forEach { list.removeAt(it) }
            }
        }
    }
}

@Suppress("PrivatePropertyName", "unused")
open class TextEditOperation<T>(
        private val focusManager: FocusManager?,
        private val encoder     : Encoder<T, String>,
        private val list        : MutableList<T, *>,
                    row         : T,
        private var index       : Int): TextField(), EditOperation<T> {

    private val listSelectionChanged = { _:ObservableSet<out List<*, Model<*>>, *>,_: Set<Int>,_:  Set<Int> ->
        list.cancelEditing()
    }

    init {
        text                = encoder.encode(row) ?: ""
        fitText             = setOf(TextFit.Width)
        borderVisible       = false
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

    override fun invoke() = this
    override fun complete() = encoder.decode(text)

    override fun cancel() {
        list.selectionChanged -= listSelectionChanged
    }

    @Suppress("UNUSED_PARAMETER")
    private fun listSelectionChanged(set: ObservableSet<out List<*, Model<*>>, *>, removed: Set<Int>, added: Set<Int>) {
        list.cancelEditing()
    }
}

class ListTextEditor<T>(private val focusManager: FocusManager?, private val encoder: Encoder<T, String>): ListEditor<T> {
    override fun edit(list: MutableList<T, *>, row: T, index: Int, current: View?): EditOperation<T> = TextEditOperation(focusManager, encoder, list, row, index)
}