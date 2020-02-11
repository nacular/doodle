package com.nectar.doodle.controls.theme.basic.table

import com.nectar.doodle.controls.ColorPicker
import com.nectar.doodle.controls.EditOperation
import com.nectar.doodle.controls.IndexedItemVisualizer
import com.nectar.doodle.controls.buttons.CheckBox
import com.nectar.doodle.controls.list.ListLike
import com.nectar.doodle.controls.table.Column
import com.nectar.doodle.controls.table.HeaderGeometry
import com.nectar.doodle.controls.table.MutableColumn
import com.nectar.doodle.controls.table.MutableTable
import com.nectar.doodle.controls.table.Table
import com.nectar.doodle.controls.table.TableBehavior
import com.nectar.doodle.controls.table.TableBehavior.CellGenerator
import com.nectar.doodle.controls.table.TableBehavior.HeaderCellGenerator
import com.nectar.doodle.controls.table.TableBehavior.HeaderPositioner
import com.nectar.doodle.controls.table.TableBehavior.RowPositioner
import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.controls.theme.basic.ListPositioner
import com.nectar.doodle.controls.theme.basic.ListRow
import com.nectar.doodle.controls.theme.basic.SelectableListKeyHandler
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.horizontalStripedBrush
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.utils.Encoder
import com.nectar.doodle.utils.HorizontalAlignment
import com.nectar.doodle.utils.ObservableSet
import com.nectar.doodle.utils.PassThroughEncoder
import com.nectar.doodle.utils.PropertyObserver
import com.nectar.doodle.utils.SetObserver

/**
 * Created by Nicholas Eddy on 4/8/19.
 */

private class TableListRow<T>(
        private val column              : Column<T>,
                    list                : ListLike,
                    row                 : T,
                    index               : Int,
                    itemVisualizer      : IndexedItemVisualizer<T>,
                    selectionColor      : Color? = green,
                    selectionBlurredColor: Color? = selectionColor): ListRow<T>(list, row, index, itemVisualizer, selectionColor, selectionBlurredColor) {

    private val alignmentChanged: (Column<*>) -> Unit = {
        it.cellAlignment?.let { positioner = it }
    }

    init {
        column.cellAlignment?.let { positioner = it }
    }

    override fun addedToDisplay() {
        super.addedToDisplay()

        column.alignmentChanged += alignmentChanged
    }

    override fun removedFromDisplay() {
        super.removedFromDisplay()

        column.alignmentChanged -= alignmentChanged
    }
}

open class BasicCellGenerator<T>: CellGenerator<T> {
    override fun <A> invoke(table: Table<T, *>, column: Column<A>, cell: A, row: Int, itemGenerator: IndexedItemVisualizer<A>, current: View?): View = when (current) {
        is ListRow<*> -> (current as TableListRow<A>).apply { update(table, cell, row) }
        else          -> TableListRow(column, table, cell, row, itemGenerator, selectionColor = null, selectionBlurredColor = null)
    }
}

open class BasicTableBehavior<T>(
        private val focusManager         : FocusManager?,
        private val rowHeight            : Double = 20.0,
        private val headerColor          : Color? = lightgray,
                    evenRowColor         : Color? = white,
                    oddRowColor          : Color? = lightgray.lighter().lighter(),
        private val selectionColor       : Color? = green.lighter(),
        private val selectionBlurredColor: Color? = lightgray): TableBehavior<T>, MouseListener, KeyListener, SelectableListKeyHandler {

    override var bodyDirty  : ((         ) -> Unit)? = null
    override var headerDirty: ((         ) -> Unit)? = null
    override var columnDirty: ((Column<*>) -> Unit)? = null

    private val selectionChanged: SetObserver<Int> = { _,_,_ ->
        bodyDirty?.invoke()
    }

    private val focusChanged: PropertyObserver<View, Boolean> = { _,_,_ ->
        bodyDirty?.invoke()
    }

    private  val patternBrush  = horizontalStripedBrush(rowHeight, evenRowColor, oddRowColor)
    private  val movingColumns = mutableSetOf<Column<*>>()
    override val cellGenerator = BasicCellGenerator<T>()

    override val headerPositioner = object: HeaderPositioner<T> {
        override fun invoke(table: Table<T, *>) = HeaderGeometry(0.0, 1.1 * rowHeight)
    }

    override val rowPositioner = object: RowPositioner<T> {
        private val delegate = ListPositioner(rowHeight)

        override fun invoke(table: Table<T, *>, row: T, index: Int) = delegate(table, table.insets, index)
        override fun rowFor(table: Table<T, *>, y: Double)          = delegate.rowFor(table.insets, y    )
    }

    override val headerCellGenerator = object: HeaderCellGenerator<T> {
        override fun <A> invoke(table: Table<T, *>, column: Column<A>) = TableHeaderCell(column, headerColor)
    }

    override fun renderHeader(table: Table<T, *>, canvas: Canvas) {
        headerColor?.let { canvas.rect(Rectangle(size = canvas.size), ColorBrush(it)) }
    }

    override fun renderBody(table: Table<T, *>, canvas: Canvas) {
        canvas.rect(Rectangle(size = canvas.size), patternBrush)

        val color = if (table.hasFocus) selectionColor else selectionBlurredColor

        if (color != null) {
            // FIXME: Performance can be bad for large lists
            table.selection.map { it to table[it] }.forEach { (index, row) ->
                row?.let {
                    canvas.rect(rowPositioner(table, row, index).inset(Insets(top = 1.0)), ColorBrush(color))
                }
            }
        }
    }

    override fun <A> renderColumnBody(table: Table<T, *>, column: Column<A>, canvas: Canvas) {
        if (column in movingColumns && headerColor != null) {
            canvas.rect(Rectangle(size = canvas.size), ColorBrush(headerColor.opacity(0.2f)))
        }
    }

    // FIXME: Centralize
    override fun install(view: Table<T, *>) {
        view.keyChanged       += this
        view.mouseChanged     += this
        view.focusChanged     += focusChanged
        view.selectionChanged += selectionChanged

        bodyDirty?.invoke  ()
        headerDirty?.invoke()
    }

    override fun uninstall(view: Table<T, *>) {
        view.keyChanged       -= this
        view.mouseChanged     -= this
        view.focusChanged     -= focusChanged
        view.selectionChanged -= selectionChanged
    }

    override fun mousePressed(event: MouseEvent) {
        focusManager?.requestFocus(event.source)
    }

    override fun keyPressed(event: KeyEvent) {
        super<SelectableListKeyHandler>.keyPressed(event)
    }

    override fun <A> columnMoveStart(table: Table<T, *>, column: Column<A>) {
        if (headerColor == null) {
            return
        }

        movingColumns += column

        columnDirty?.invoke(column)
    }

    override fun <A> columnMoveEnd(table: Table<T, *>, column: Column<A>) {
        if (headerColor == null) {
            return
        }

        movingColumns -= column

        columnDirty?.invoke(column)
    }
}

class BasicMutableTableBehavior<T>(
        focusManager         : FocusManager?,
        rowHeight            : Double = 20.0,
        headerColor          : Color? = lightgray,
        evenRowColor         : Color? = white,
        oddRowColor          : Color? = lightgray.lighter().lighter(),
        selectionColor       : Color? = green.lighter(),
        selectionBlurredColor: Color? = lightgray): BasicTableBehavior<T>(focusManager, rowHeight, headerColor, evenRowColor, oddRowColor, selectionColor, selectionBlurredColor) {

    override val headerCellGenerator = object: HeaderCellGenerator<T> {
        override fun <A> invoke(table: Table<T, *>, column: Column<A>) = TableHeaderCell(column, headerColor).apply {
            toggled += {
                if (table is MutableTable && column is MutableColumn<*,*>) {
                    table.toggleSort(by = column as MutableColumn<T, *>)
                }
            }
        }
    }

    override val cellGenerator = object: BasicCellGenerator<T>() {
        override fun <A> invoke(table: Table<T, *>, column: Column<A>, cell: A, row: Int, itemGenerator: IndexedItemVisualizer<A>, current: View?) = super.invoke(table, column, cell, row, itemGenerator, current).also {
            if (current !is ListRow<*>) {
                val result = it as ListRow<*>

                it.mouseChanged += object: MouseListener {
                    override fun mouseReleased(event: MouseEvent) {
                        if (event.clickCount == 2 && table is MutableTable && column is MutableColumn<*,*>) {
                            table.startEditing(result.index, column as MutableColumn<T, *>)
                        }
                    }
                }
            }
        }
    }
}

open class TextEditOperation<T>(
        private val focusManager: FocusManager?,
        private val encoder     : Encoder<T, String>,
        private val table       : MutableTable<*, *>,
                    row         : T,
        private var index       : Int,
                    current     : View): TextField(), EditOperation<T> {

    private val tableSelectionChanged = { _: ObservableSet<Int>,_: Set<Int>,_:  Set<Int> ->
        table.cancelEditing()
    }

    init {
        text                = encoder.encode(row) ?: ""
        fitText             = setOf(TextFit.Width, TextFit.Height)
        borderVisible       = false
        backgroundColor     = current.backgroundColor
        horizontalAlignment = HorizontalAlignment.Left

        styleChanged += { rerender() }

        focusChanged += { _,_,_ ->
            if (!hasFocus) {
                table.cancelEditing()
            }
        }

        keyChanged += object: KeyListener {
            override fun keyReleased(event: KeyEvent) {
                when (event.code) {
                    KeyEvent.VK_RETURN -> { table.completeEditing(); focusManager?.requestFocus(table) }
                    KeyEvent.VK_ESCAPE -> { table.cancelEditing  (); focusManager?.requestFocus(table) }
                }
            }
        }

        table.selectionChanged += tableSelectionChanged
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

    override fun complete() = encoder.decode(text).also { cancel() }

    override fun cancel() {
        table.selectionChanged -= tableSelectionChanged
    }

    companion object {
        operator fun invoke(focusManager: FocusManager?,
                            table       : MutableTable<*, *>,
                            row         : String,
                            index       : Int,
                            current     : View) = TextEditOperation(focusManager, PassThroughEncoder(), table, row, index, current)
    }
}

open class ColorEditOperation<T>(
        private val display     : Display,
        private val focusManager: FocusManager,
        private val table       : MutableTable<T, *>,
        private val index       : Int,
                    value       : Color,
        private val generator   : (Color) -> T,
        private val colorPicker : ColorPicker): EditOperation<Color>, KeyListener {

    private val listener = { _: ColorPicker, _: Color, new: Color ->
        table[index] = generator(new)
    }

    private val focusChanged = { _: View, _: Boolean, _: Boolean ->
        table.cancelEditing()
    }

    init {
        colorPicker.color      = value
        colorPicker.changed    += listener
        colorPicker.keyChanged += this
    }

    override fun invoke(): View? = null.also {
        display.children += colorPicker

        colorPicker.position = Point((display.size.width - colorPicker.width)/ 2.0, (display.size.height - colorPicker.height)/ 2.0)

        focusManager.requestFocus(colorPicker)

        colorPicker.focusChanged += focusChanged
    }

    override fun complete() = colorPicker.color.also {
        cancel()
    }

    override fun cancel() {
        colorPicker.keyChanged   -= this
        colorPicker.changed      -= listener
        display.children         -= colorPicker
        colorPicker.focusChanged -= focusChanged
    }

    override fun keyPressed(event: KeyEvent) {
        when (event.code) {
            KeyEvent.VK_RETURN -> table.completeEditing()
            KeyEvent.VK_ESCAPE -> table.cancelEditing  ()
        }
    }
}

open class BooleanEditOperation<T>(
        private val focusManager: FocusManager,
        private val table       : MutableTable<T, *>,
                    column      : Column<*>,
        private val index       : Int,
                    value       : Boolean,
        private val generator   : (Boolean) -> T): View(), EditOperation<Boolean> {

    private val checkBox = CheckBox().apply {
        selected = value

        selectedChanged += { _,_,new ->
            table[index] = generator(new)
        }
    }

    private val tableSelectionChanged = { _: ObservableSet<Int>,_: Set<Int>,_:  Set<Int> ->
        table.completeEditing()
    }

    init {
        children += checkBox

        layout = constrain(checkBox) {
            column.cellAlignment?.let { alignment -> alignment(it) }
        }

        table.selectionChanged += tableSelectionChanged
    }

    override fun addedToDisplay() {
        super.addedToDisplay()

        focusManager.requestFocus(checkBox)
    }

    override fun invoke(): View? = this

    override fun complete() = checkBox.selected.also { cancel() }

    override fun cancel() {
        table.selectionChanged -= tableSelectionChanged
    }
}