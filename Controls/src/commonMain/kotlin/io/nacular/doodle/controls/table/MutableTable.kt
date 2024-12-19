package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.list.ListEditor
import io.nacular.doodle.controls.list.MutableList
import io.nacular.doodle.core.View
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.utils.Editable
import io.nacular.doodle.utils.Extractor
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.SortOrder
import io.nacular.doodle.utils.SortOrder.Ascending
import io.nacular.doodle.utils.SortOrder.Descending
import io.nacular.doodle.utils.observable
import kotlin.Result.Companion.failure

/**
 * Defines how cells in a [MutableTable] are edited.
 */
public interface TableCellEditor<T, R> {
    /**
     * Starts an edit operation for a cell within a [MutableTable].
     *
     * @param table being edited
     * @param row data of cell within [table]
     * @param column of cell within [table]
     * @param index of the row
     * @param current view used to render the row
     */
    public operator fun invoke(table: MutableTable<T, *>, row: T, column: MutableColumn<T, R>, index: Int, current: View): EditOperation<T>
}

/**
 * DSL for creating [TableCellEditor] from a lambda.
 */
public fun <T, R> tableCellEditor(block: (
    table  : MutableTable<T, *>,
    row    : T,
    column : MutableColumn<T, R>,
    index  : Int,
    current: View
) -> EditOperation<T>): TableCellEditor<T, R> = object: TableCellEditor<T, R> {
    override fun invoke(
        table  : MutableTable<T, *>,
        row    : T,
        column : MutableColumn<T, R>,
        index  : Int,
        current: View
    ) = block(table, row, column, index, current)
}

/**
 * Creates a [TableCellEditor] that modifies a single cell [R] within a row [T].
 *
 * @param cell extracts the cell within the row to be edited
 * @param result constructs the new row based on the updated cell
 * @param cellEditor an [EditOperation] for the cell
 * @return a TableEditor
 */
public fun <T, R> simpleTableCellEditor(
    cell      : T.(              ) -> R,
    result    : T.(updatedCell: R) -> T,
    cellEditor: (table: MutableTable<T, *>, column: MutableColumn<T, R>, cell: R, index: Int, current: View, result: (R) -> T) -> EditOperation<R>
): TableCellEditor<T, R> = tableCellEditor { table, row, column, index, current -> object: EditOperation<T> {
        val delegate = cellEditor(table, column, cell(row), index, current) { result(row, it) }

        override fun invoke  () = delegate()
        override fun complete() = delegate.complete().map { result(row, it) }
        override fun cancel  () = delegate.cancel()
    }
}

/**
 * A visual component that renders a mutable list of items of type [T] using a [TableBehavior].
 *
 * @see Table
 * @param model that holds the data for the Table
 * @param selectionModel that manages the Table's selection state
 * @param scrollCache determining how many "hidden" items are rendered above and below the Table's view-port. A value of 0 means
 * only visible items are rendered, but quick scrolling is more likely to show blank areas.
 * @param block factory to define the set of columns for the Table
 *
 * @property model that holds the data for the MutableTable
 * @property selectionModel that manages the Table's selection state
 */
public class MutableTable<T, M: MutableListModel<T>>(
                      model         : M,
                      selectionModel: SelectionModel<Int>? = null,
        private   val scrollCache   : Int                  = 0,
                      block         : MutableColumnFactory<T>.() -> Unit
): DynamicTable<T, M>(model, selectionModel, scrollCache, {}), Editable {

    private inner class MutableInternalListColumn<R, S: Comparable<S>>(
                        header         : View?,
                        headerAlignment: (ConstraintDslContext.(Bounds) -> Unit)? = null,
                        footer         : View?,
                        footerAlignment: (ConstraintDslContext.(Bounds) -> Unit)? = null,
                        cellGenerator  : CellVisualizer<T, R>,
                        cellAlignment  : (ConstraintDslContext.(Bounds) -> Unit)? = null,
                        preferredWidth : Double? = null,
                        minWidth       : Double  = 0.0,
                        maxWidth       : Double? = null,
            private val extractor      : Extractor<T, R>,
                        firstColumn    : Boolean,
                        sorter         : Sorter<T, S>?,
                        editor         : TableCellEditor<T, R>?
    ): InternalListColumn<R>(header, headerAlignment, footer, footerAlignment, cellGenerator, cellAlignment, preferredWidth, minWidth, maxWidth, extractor, firstColumn), MutableColumn<T, R> {

        private inner class FieldModel<A>(model: M, extractor: Extractor<T, A>): InternalListColumn<R>.FieldModel<A>(model, extractor), MutableListModel<A> {
            override fun set        (index  : Int, value : A            ): Result<A> = this[index] // This is essential to allow MutableList to handle edits that result in the content being unchanged
            override fun add        (              value : A            ) { /*NO-OP*/ }
            override fun add        (index  : Int, value : A            ) { /*NO-OP*/ }
            override fun remove     (              value : A            ) { /*NO-OP*/ }
            override fun removeAt   (index  : Int                       ): Result<A> = failure(UnsupportedOperationException()) // NO-OP
            override fun addAll     (              values: Collection<A>) { /*NO-OP*/ }
            override fun addAll     (index  : Int, values: Collection<A>) { /*NO-OP*/ }
            override fun removeAll  (              values: Collection<A>) { /*NO-OP*/ }
            override fun retainAll  (              values: Collection<A>) { /*NO-OP*/ }
            override fun removeAllAt(indexes: Collection<Int>           ) { /*NO-OP*/ }
            override fun replaceAll (              values: Collection<A>) { /*NO-OP*/ }
            override fun clear      (                                   ) { /*NO-OP*/ }

            override fun notifyChanged(index: Int) { /*NO-OP*/ }

            override fun <R: Comparable<R>> sortBy            (selector  : (A) -> R?       ) { /*NO-OP*/ }
            override fun <R: Comparable<R>> sortByDescending  (selector  : (A) -> R?       ) { /*NO-OP*/ }
            override fun                    sortWith          (comparator: Comparator<in A>) { /*NO-OP*/ }
            override fun                    sortWithDescending(comparator: Comparator<in A>) { /*NO-OP*/ }
        }

        private inner class ListEditorAdapter(private val editor: TableCellEditor<T, R>): ListEditor<R> {
            override fun edit(list: MutableList<R, *>, item: R, index: Int, current: View): EditOperation<R> = editor(this@MutableTable, model[index].getOrNull()!!, this@MutableInternalListColumn, index, current).let {
                object: EditOperation<R> {
                    override fun invoke  () = it.invoke  ()
                    override fun complete() = it.complete().map { r -> model[index] = r; editingColumn = null; extractor(r) }
                    override fun cancel  () = it.cancel  ()
                }
            }
        }

        override var editor = editor
            set(new) {
                field = new

                view.editor = new?.let { ListEditorAdapter(it) }
            }

        override var comparator = sorter?.let {
            Comparator<T> { a, b -> it(a).compareTo(it(b)) }
        }

        override val view: MutableList<R, *> by lazy {
            MutableList(
                FieldModel(model, extractor),
                object: ItemVisualizer<R, Any> {
                    override fun invoke(item: R, previous: View?, context: Any) = object: View() {}
                },
                selectionModelWrapper,
                scrollCache = scrollCache,
            ).apply {
                acceptsThemes = false

                this@MutableInternalListColumn.editor?.let {
                    this.editor = ListEditorAdapter(it)
                }
            }
        }
    }

    private inner class MutableColumnFactoryImpl: MutableColumnFactory<T> {
        override fun <R, S: Comparable<S>> column(header: View?, extractor: Extractor<T, R>, cellVisualizer: CellVisualizer<T, R>, editor: TableCellEditor<T, R>?, sorter: Sorter<T, S>?, footer: View?, builder: MutableColumnBuilder<T, R>.() -> Unit) = MutableColumnBuilderImpl<T, R>().run {
            builder(this)

            MutableInternalListColumn(header, headerAlignment, footer, footerAlignment, cellVisualizer, cellAlignment, width, minWidth, maxWidth, extractor, internalColumns.isEmpty(), sorter, editor).also {
                internalColumns += it
            }
        }
    }

    init {
        MutableColumnFactoryImpl().apply(block)

        model.changed += { _,_ ->
            updateSort()
        }
    }

    public class Sorting<T>(public val column: MutableColumn<T, *>, public val order: SortOrder)

    /** Notifies changes to [sorting] */
    public val sortingChanged: PropertyObservers<MutableTable<T, M>, List<Sorting<T>>> by lazy { PropertyObserversImpl(this) }

    /** current sorting for the table default is ```emptyList()```.  */
    public var sorting: List<Sorting<T>> by observable(emptyList(), sortingChanged as PropertyObserversImpl<MutableTable<T, M>, List<Sorting<T>>>) { _,_ ->
        updateSort()
    }

    public val editing: Boolean get() = editingColumn != null

    private var editingColumn = null as MutableInternalListColumn<T, *>?

    public operator fun set(index: Int, value: T) { model[index] = value }

    public fun add      (value : T                         ): Unit      = model.add      (value        )
    public fun add      (index : Int, values: T            ): Unit      = model.add      (index, values)
    public fun remove   (value : T                         ): Unit      = model.remove   (value        )
    public fun removeAt (index : Int                       ): Result<T> = model.removeAt (index        )
    public fun addAll   (values: Collection<T>             ): Unit      = model.addAll   (values       )
    public fun addAll   (index : Int, values: Collection<T>): Unit      = model.addAll   (index, values)
    public fun removeAll(values: Collection<T>             ): Unit      = model.removeAll(values       )
    public fun retainAll(values: Collection<T>             ): Unit      = model.retainAll(values       )

    public fun clear(): Unit = model.clear()

    public fun toggleSort(by: MutableColumn<T, *>) {
        sorting.find { it.column == by }?.let {
            when (it.order) {
                Ascending -> {
                    sortDescending(by)
                    return
                }
                else -> {}
            }
        }

        sort(by)
    }

    public fun sort(by: MutableColumn<T, *>) {
        sorting = listOf(Sorting(by, order = Ascending))
    }

    public fun sortDescending(by: MutableColumn<T, *>) {
        sorting = listOf(Sorting(by, order = Descending))
    }

    private fun updateSort() {
        val comparator = sorting.fold(null as Comparator<T>?) { a, b ->
            var next = b.column.comparator

            if (b.order == Descending) {
                next = next?.reversed()
            }

            when {
                a == null -> next
                next != null -> a.then(next)
                else -> null
            }
        }

        comparator?.let { model.sortWith(it) }
    }

    public fun startEditing(index: Int, column: MutableColumn<T, *>) {
        @Suppress("UNCHECKED_CAST")
        (column as? MutableInternalListColumn<T, *>)?.let {
            editingColumn = it
            it.view.startEditing(index)
        }
    }

    public override fun completeEditing() {
        editingColumn?.view?.completeEditing()
    }

    public override fun cancelEditing() {
        editingColumn?.view?.cancelEditing()
    }
}