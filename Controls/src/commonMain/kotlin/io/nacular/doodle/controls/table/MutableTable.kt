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
 * Created by Nicholas Eddy on 5/19/19.
 */

public interface TableEditor<T> {
    public operator fun <R> invoke(table: MutableTable<T, *>, row: T, column: MutableColumn<T, R>, index: Int, current: View): EditOperation<T>
}

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
                        editor         : TableEditor<T>?
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

        private inner class ListEditorAdapter(private val editor: TableEditor<T>): ListEditor<R> {
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

        override val view: MutableList<R, *> = MutableList(FieldModel(model, extractor), object: ItemVisualizer<R, Any> {
            override fun invoke(item: R, previous: View?, context: Any) = object: View() {}
        }, selectionModelWrapper, scrollCache = scrollCache, fitContent = emptySet()).apply {
            acceptsThemes = false

            this@MutableInternalListColumn.editor?.let {
                this.editor = ListEditorAdapter(it)
            }
        }
    }

    private inner class MutableColumnFactoryImpl: MutableColumnFactory<T> {
        override fun <R, S: Comparable<S>> column(header: View?, extractor: Extractor<T, R>, cellVisualizer: CellVisualizer<T, R>, editor: TableEditor<T>?, sorter: Sorter<T, S>?, footer: View?, builder: MutableColumnBuilder<T>.() -> Unit) = MutableColumnBuilderImpl<T>().run {
            builder(this)

            MutableInternalListColumn(header, headerAlignment, footer, footerAlignment, cellVisualizer, cellAlignment, width, minWidth, maxWidth, extractor, internalColumns.isEmpty(), sorter, editor).also {
                internalColumns += it
            }
        }
    }

    init {
        MutableColumnFactoryImpl().apply(block)
    }

    public class Sorting<T>(public val column: MutableColumn<T, *>, public val order: SortOrder)

    /** Notifies changes to [sorting] */
    public val sortingChanged: PropertyObservers<MutableTable<T, M>, List<Sorting<T>>> by lazy { PropertyObserversImpl<MutableTable<T, M>, List<Sorting<T>>>(this) }

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