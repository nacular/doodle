package io.nacular.doodle.theme.basic.table

import io.nacular.doodle.controls.ColorPicker
import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.list.ListLike
import io.nacular.doodle.controls.table.AbstractTableBehavior.HeaderCellGenerator
import io.nacular.doodle.controls.table.AbstractTableBehavior.HeaderPositioner
import io.nacular.doodle.controls.table.AbstractTableBehavior.OverflowColumnConfig
import io.nacular.doodle.controls.table.Column
import io.nacular.doodle.controls.table.HeaderGeometry
import io.nacular.doodle.controls.table.MutableColumn
import io.nacular.doodle.controls.table.MutableTable
import io.nacular.doodle.controls.table.Table
import io.nacular.doodle.controls.table.TableBehavior
import io.nacular.doodle.controls.table.TableBehavior.CellGenerator
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.text.TextFit
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.horizontalStripedPaint
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.KeyText.Companion.Enter
import io.nacular.doodle.event.KeyText.Companion.Escape
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.system.SystemInputEvent.Modifier.Ctrl
import io.nacular.doodle.system.SystemInputEvent.Modifier.Meta
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift
import io.nacular.doodle.theme.basic.ListPositioner
import io.nacular.doodle.theme.basic.ListRow
import io.nacular.doodle.theme.basic.SelectableListKeyHandler
import io.nacular.doodle.utils.Encoder
import io.nacular.doodle.utils.HorizontalAlignment
import io.nacular.doodle.utils.PassThroughEncoder
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.SetObserver

/**
 * Created by Nicholas Eddy on 4/8/19.
 */

private class TableListRow<T>(
        private val column               : Column<T>,
                    list                 : ListLike,
                    row                  : T,
                    index                : Int,
                    itemVisualizer       : ItemVisualizer<T, IndexedItem>,
                    selectionColor       : Color? = Blue,
                    selectionBlurredColor: Color? = selectionColor): ListRow<T>(list, row, index, itemVisualizer, backgroundSelectionColor = selectionColor, backgroundSelectionBlurredColor = selectionBlurredColor) {

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

public open class BasicCellGenerator<T>: CellGenerator<T> {
    override fun <A> invoke(table: Table<T, *>, column: Column<A>, cell: A, row: Int, itemGenerator: ItemVisualizer<A, IndexedItem>, current: View?): View = when (current) {
        is TableListRow<*> -> (current as TableListRow<A>).apply { update(table, cell, row) }
        else               -> TableListRow(column, table, cell, row, itemGenerator, selectionColor = null, selectionBlurredColor = null)
    }
}

public open class BasicTableBehavior<T>(
        private   val focusManager         : FocusManager?,
        protected val rowHeight            : Double = 20.0,
        protected val headerColor          : Color? = Lightgray,
                      evenRowColor         : Color? = White,
                      oddRowColor          : Color? = Lightgray.lighter().lighter(),
        protected val selectionColor       : Color? = Blue,
        protected val selectionBlurredColor: Color? = Lightgray): TableBehavior<T>(), PointerListener, KeyListener, SelectableListKeyHandler {

    private val selectionChanged: SetObserver<Table<T, *>, Int> = { table,_,_ ->
        table.bodyDirty()
    }

    private val focusChanged: PropertyObserver<View, Boolean> = { table,_,_ ->
        (table as? Table<T, *>)?.bodyDirty()
    }

    private  val patternFill   = horizontalStripedPaint(rowHeight, evenRowColor, oddRowColor)
    private  val movingColumns = mutableSetOf<Column<*>>()
    override val cellGenerator: BasicCellGenerator<T> = BasicCellGenerator()

    override val headerPositioner: HeaderPositioner<Table<T, *>> = object: HeaderPositioner<Table<T, *>> {
        override fun invoke(table: Table<T, *>) = HeaderGeometry(0.0, 1.1 * rowHeight)
    }

    override val rowPositioner: RowPositioner<T> = object: RowPositioner<T> {
        private val delegate = ListPositioner(rowHeight)

        override fun rowBounds(of: Table<T, *>, row: T, index: Int) = delegate.rowBounds(of.width, of.insets, index)
        override fun row(of: Table<T, *>, y: Double)                = delegate.rowFor(of.insets,  y                )
        override fun totalRowHeight(of: Table<T, *>)                = delegate.totalHeight(of.numRows, of.insets   )
    }

    override val headerCellGenerator: HeaderCellGenerator<Table<T, *>> = object: HeaderCellGenerator<Table<T, *>> {
        override fun <A> invoke(table: Table<T, *>, column: Column<A>) = TableHeaderCell(column, headerColor)
    }

    override val overflowColumnConfig: OverflowColumnConfig<Table<T, *>> = object: OverflowColumnConfig<Table<T, *>> {
        override fun body(table: Table<T, *>): View? = object: View() {
            init {
                pointerChanged += object: PointerListener {
                    private var pointerOver    = false
                    private var pointerPressed = false

                    override fun entered(event: PointerEvent) {
                        pointerOver = true
                    }

                    override fun exited(event: PointerEvent) {
                        pointerOver = false
                    }

                    override fun pressed(event: PointerEvent) {
                        pointerPressed = true
                    }

                    override fun released(event: PointerEvent) {
                        if (pointerOver && pointerPressed) {
                            val index = rowPositioner.row(table, event.location.y)

                            if (index >= table.numRows) {
                                return
                            }

                            when {
                                Ctrl  in event.modifiers || Meta in event.modifiers     -> table.toggleSelection(setOf(index))
                                Shift in event.modifiers && table.lastSelection != null -> {
                                    table.selectionAnchor?.let { anchor ->
                                        when {
                                            index  < anchor -> table.setSelection((index.. anchor ).reversed().toSet())
                                            anchor < index  -> table.setSelection((anchor  ..index).           toSet())
                                        }
                                    }
                                }
                                else                                                    -> table.setSelection(setOf(index))
                            }
                        }

                        pointerPressed = false
                    }
                }
            }
        }
    }

    override fun renderHeader(table: Table<T, *>, canvas: Canvas) {
        headerColor?.let { canvas.rect(Rectangle(size = canvas.size), ColorPaint(it)) }
    }

    override fun renderBody(table: Table<T, *>, canvas: Canvas) {
        canvas.rect(Rectangle(size = canvas.size), patternFill)

        val color = if (table.hasFocus) selectionColor else selectionBlurredColor

        if (color != null) {
            // FIXME: Performance can be bad for large lists
            table.selection.map { it to table[it] }.forEach { (index, row) ->
                row?.let {
                    canvas.rect(rowPositioner.rowBounds(table, row, index).inset(Insets(top = 1.0)), ColorPaint(color))
                }
            }
        }
    }

    override fun <A> renderColumnBody(table: Table<T, *>, column: Column<A>, canvas: Canvas) {
        if (column in movingColumns && headerColor != null) {
            canvas.rect(Rectangle(size = canvas.size), ColorPaint(headerColor.opacity(0.2f)))
        }
    }

    // FIXME: Centralize
    override fun install(view: Table<T, *>) {
        view.keyChanged       += this
        view.focusChanged     += focusChanged
        view.pointerFilter    += this
        view.selectionChanged += selectionChanged

        view.bodyDirty  ()
        view.headerDirty()
    }

    override fun uninstall(view: Table<T, *>) {
        view.keyChanged       -= this
        view.focusChanged     -= focusChanged
        view.pointerFilter    -= this
        view.selectionChanged -= selectionChanged
    }

    override fun pressed(event: PointerEvent) {
        focusManager?.requestFocus(event.source)
    }

    override fun pressed(event: KeyEvent) {
        super<SelectableListKeyHandler>.pressed(event)
    }

    override fun <A> columnMoveStart(table: Table<T, *>, column: Column<A>) {
        if (headerColor == null) {
            return
        }

        movingColumns += column

        table.columnDirty(column)
    }

    override fun <A> columnMoveEnd(table: Table<T, *>, column: Column<A>) {
        if (headerColor == null) {
            return
        }

        movingColumns -= column

        table.columnDirty(column)
    }
}

public open class BasicMutableTableBehavior<T>(
        focusManager         : FocusManager?,
        rowHeight            : Double = 20.0,
        headerColor          : Color? = Lightgray,
        evenRowColor         : Color? = White,
        oddRowColor          : Color? = Lightgray.lighter().lighter(),
        selectionColor       : Color? = Blue,
        selectionBlurredColor: Color? = Lightgray): BasicTableBehavior<T>(focusManager, rowHeight, headerColor, evenRowColor, oddRowColor, selectionColor, selectionBlurredColor) {

    override val headerCellGenerator: HeaderCellGenerator<Table<T, *>> = object: HeaderCellGenerator<Table<T, *>> {
        override fun <A> invoke(table: Table<T, *>, column: Column<A>) = TableHeaderCell(column, headerColor).apply {
            toggled += {
                if (table is MutableTable && column is MutableColumn<*,*>) {
                    table.toggleSort(by = column as MutableColumn<T, *>)
                }
            }
        }
    }

    override val cellGenerator: BasicCellGenerator<T> = object: BasicCellGenerator<T>() {
        override fun <A> invoke(table: Table<T, *>, column: Column<A>, cell: A, row: Int, itemGenerator: ItemVisualizer<A, IndexedItem>, current: View?) = super.invoke(table, column, cell, row, itemGenerator, current).also {
            if (current !is TableListRow<*>) {
                val result = it as TableListRow<*>

                it.pointerChanged += object: PointerListener {
                    override fun released(event: PointerEvent) {
                        if (event.clickCount == 2 && table is MutableTable && column is MutableColumn<*,*>) {
                            table.startEditing(result.index, column as MutableColumn<T, *>)
                        }
                    }
                }
            }
        }
    }
}

public open class TextEditOperation<T>(
        private val focusManager: FocusManager?,
        private val encoder     : Encoder<T, String>,
        private val table       : MutableTable<*, *>,
                    row         : T,
        private var index       : Int,
                    current     : View): TextField(), EditOperation<T> {

    private val tableSelectionChanged = { _: Table<*, *>, _: Set<Int>, _:  Set<Int> ->
        table.cancelEditing()
    }

    init {
        text                = encoder.to(row).getOrDefault("")
        fitText             = setOf(TextFit.Width, TextFit.Height)
        borderVisible       = false
        foregroundColor     = current.foregroundColor
        backgroundColor     = Transparent
        horizontalAlignment = HorizontalAlignment.Left

        styleChanged += { rerender() }

        focusChanged += { _,_,_ ->
            if (!hasFocus) {
                table.cancelEditing()
            }
        }

        keyChanged += object: KeyListener {
            override fun released(event: KeyEvent) {
                when (event.key) {
                    Enter  -> { table.completeEditing(); focusManager?.requestFocus(table) }
                    Escape -> { table.cancelEditing  (); focusManager?.requestFocus(table) }
                }
            }
        }

        table.selectionChanged += tableSelectionChanged
    }

    override fun addedToDisplay() {
        focusManager?.requestFocus(this)
        selectAll()
    }

    override fun invoke(): View = object: View() {
        init {
            children += this@TextEditOperation

            layout = constrain(this@TextEditOperation) {
                it.centerY = it.parent.centerY
            }
        }

        override fun render(canvas: Canvas) {
            this@TextEditOperation.backgroundColor?.let { canvas.rect(bounds.atOrigin, ColorPaint(it)) }
        }
    }

    override fun complete(): T? = encoder.from(text).getOrNull().also { cancel() }

    override fun cancel() {
        table.selectionChanged -= tableSelectionChanged
    }

    public companion object {
        public operator fun invoke(focusManager: FocusManager?,
                            table       : MutableTable<*, *>,
                            row         : String,
                            index       : Int,
                            current     : View): TextEditOperation<String> = TextEditOperation(focusManager, PassThroughEncoder(), table, row, index, current)
    }
}

public open class ColorEditOperation<T>(
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

    override fun complete(): Color = colorPicker.color.also {
        cancel()
    }

    override fun cancel() {
        colorPicker.keyChanged   -= this
        colorPicker.changed      -= listener
        display.children         -= colorPicker
        colorPicker.focusChanged -= focusChanged
    }

    override fun pressed(event: KeyEvent) {
        when (event.key) {
            Enter  -> table.completeEditing()
            Escape -> table.cancelEditing  ()
        }
    }
}

public open class BooleanEditOperation<T>(
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

    private val tableSelectionChanged = { _: Table<T, *>, _: Set<Int>, _:  Set<Int> ->
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

    override fun complete(): Boolean = checkBox.selected.also { cancel() }

    override fun cancel() {
        table.selectionChanged -= tableSelectionChanged
    }
}