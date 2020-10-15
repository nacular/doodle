package io.nacular.doodle.theme.basic.table

import io.nacular.doodle.controls.ColorPicker
import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.IndexedIem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.list.ListLike
import io.nacular.doodle.controls.table.Column
import io.nacular.doodle.controls.table.HeaderGeometry
import io.nacular.doodle.controls.table.MutableColumn
import io.nacular.doodle.controls.table.MutableTable
import io.nacular.doodle.controls.table.Table
import io.nacular.doodle.controls.table.TableBehavior
import io.nacular.doodle.controls.table.TableBehavior.CellGenerator
import io.nacular.doodle.controls.table.TableBehavior.HeaderCellGenerator
import io.nacular.doodle.controls.table.TableBehavior.HeaderPositioner
import io.nacular.doodle.controls.table.TableBehavior.OverflowColumnConfig
import io.nacular.doodle.controls.table.TableBehavior.RowPositioner
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
import io.nacular.doodle.drawing.ColorFill
import io.nacular.doodle.drawing.horizontalStripedFill
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
import io.nacular.doodle.utils.ObservableSet
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
                    itemVisualizer       : ItemVisualizer<T, IndexedIem>,
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

open class BasicCellGenerator<T>: CellGenerator<T> {
    override fun <A> invoke(table: Table<T, *>, column: Column<A>, cell: A, row: Int, itemGenerator: ItemVisualizer<A, IndexedIem>, current: View?): View = when (current) {
        is ListRow<*> -> (current as TableListRow<A>).apply { update(table, cell, row) }
        else          -> TableListRow(column, table, cell, row, itemGenerator, selectionColor = null, selectionBlurredColor = null)
    }
}

open class BasicTableBehavior<T>(
        private   val focusManager         : FocusManager?,
        protected val rowHeight            : Double = 20.0,
        protected val headerColor          : Color? = Lightgray,
                      evenRowColor         : Color? = White,
                      oddRowColor          : Color? = Lightgray.lighter().lighter(),
        protected val selectionColor       : Color? = Blue,
        protected val selectionBlurredColor: Color? = Lightgray): TableBehavior<T>, PointerListener, KeyListener, SelectableListKeyHandler {

    override var bodyDirty  : ((         ) -> Unit)? = null
    override var headerDirty: ((         ) -> Unit)? = null
    override var columnDirty: ((Column<*>) -> Unit)? = null

    private val selectionChanged: SetObserver<Int> = { _,_,_ ->
        bodyDirty?.invoke()
    }

    private val focusChanged: PropertyObserver<View, Boolean> = { _,_,_ ->
        bodyDirty?.invoke()
    }

    private  val patternFill   = horizontalStripedFill(rowHeight, evenRowColor, oddRowColor)
    private  val movingColumns = mutableSetOf<Column<*>>()
    override val cellGenerator = BasicCellGenerator<T>()

    override val headerPositioner = object: HeaderPositioner<T> {
        override fun invoke(table: Table<T, *>) = HeaderGeometry(0.0, 1.1 * rowHeight)
    }

    override val rowPositioner = object: RowPositioner<T> {
        private val delegate = ListPositioner(rowHeight)

        override fun rowBounds(of: Table<T, *>, row: T, index: Int) = delegate.rowBounds(of.width, of.insets, index)
        override fun row(of: Table<T, *>, y: Double)                = delegate.rowFor(of.insets,  y                )
        override fun totalRowHeight(of: Table<T, *>)                = delegate.totalHeight(of.numRows, of.insets   )
    }

    override val headerCellGenerator = object: HeaderCellGenerator<T> {
        override fun <A> invoke(table: Table<T, *>, column: Column<A>) = TableHeaderCell(column, headerColor)
    }

    override val overflowColumnConfig = object: OverflowColumnConfig<T> {
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
        headerColor?.let { canvas.rect(Rectangle(size = canvas.size), ColorFill(it)) }
    }

    override fun renderBody(table: Table<T, *>, canvas: Canvas) {
        canvas.rect(Rectangle(size = canvas.size), patternFill)

        val color = if (table.hasFocus) selectionColor else selectionBlurredColor

        if (color != null) {
            // FIXME: Performance can be bad for large lists
            table.selection.map { it to table[it] }.forEach { (index, row) ->
                row?.let {
                    canvas.rect(rowPositioner.rowBounds(table, row, index).inset(Insets(top = 1.0)), ColorFill(color))
                }
            }
        }
    }

    override fun <A> renderColumnBody(table: Table<T, *>, column: Column<A>, canvas: Canvas) {
        if (column in movingColumns && headerColor != null) {
            canvas.rect(Rectangle(size = canvas.size), ColorFill(headerColor.opacity(0.2f)))
        }
    }

    // FIXME: Centralize
    override fun install(view: Table<T, *>) {
        view.keyChanged       += this
        view.focusChanged     += focusChanged
        view.pointerFilter    += this
        view.selectionChanged += selectionChanged

        bodyDirty?.invoke  ()
        headerDirty?.invoke()
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

open class BasicMutableTableBehavior<T>(
        focusManager         : FocusManager?,
        rowHeight            : Double = 20.0,
        headerColor          : Color? = Lightgray,
        evenRowColor         : Color? = White,
        oddRowColor          : Color? = Lightgray.lighter().lighter(),
        selectionColor       : Color? = Blue,
        selectionBlurredColor: Color? = Lightgray): BasicTableBehavior<T>(focusManager, rowHeight, headerColor, evenRowColor, oddRowColor, selectionColor, selectionBlurredColor) {

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
        override fun <A> invoke(table: Table<T, *>, column: Column<A>, cell: A, row: Int, itemGenerator: ItemVisualizer<A, IndexedIem>, current: View?) = super.invoke(table, column, cell, row, itemGenerator, current).also {
            if (current !is ListRow<*>) {
                val result = it as ListRow<*>

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
            override fun keyReleased(event: KeyEvent) {
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

    override fun invoke() = object: View() {
        init {
            children += this@TextEditOperation

            layout = constrain(this@TextEditOperation) {
                it.centerY = it.parent.centerY
            }
        }

        override fun render(canvas: Canvas) {
            this@TextEditOperation.backgroundColor?.let { canvas.rect(bounds.atOrigin, ColorFill(it)) }
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
        when (event.key) {
            Enter  -> table.completeEditing()
            Escape -> table.cancelEditing  ()
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