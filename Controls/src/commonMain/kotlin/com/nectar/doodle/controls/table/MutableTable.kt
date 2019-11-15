package com.nectar.doodle.controls.table

import com.nectar.doodle.controls.EditOperation
import com.nectar.doodle.controls.IndexedItemVisualizer
import com.nectar.doodle.controls.MutableListModel
import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.controls.list.ListEditor
import com.nectar.doodle.controls.list.MutableList
import com.nectar.doodle.controls.sortWith
import com.nectar.doodle.core.View
import com.nectar.doodle.layout.Constraints

/**
 * Created by Nicholas Eddy on 5/19/19.
 */

interface TableEditor<T> {
    operator fun <R> invoke(table: MutableTable<T, *>, row: T, column: MutableColumn<T, R>, index: Int, current: View): EditOperation<T>
}

class MutableTable<T, M: MutableListModel<T>>(
        model         : M,
        selectionModel: SelectionModel<Int>? = null,
        scrollCache   : Int                  = 10,
        block         : MutableColumnFactory<T>.() -> Unit): DynamicTable<T, M>(model, selectionModel, scrollCache, {}) {

    private inner class MutableInternalListColumn<R, S: Comparable<S>>(
                        header         : View?,
                        headerAlignment: (Constraints.() -> Unit)? = null,
                        cellGenerator  : CellVisualizer<R>,
                        cellAlignment  : (Constraints.() -> Unit)? = null,
                        preferredWidth : Double? = null,
                        minWidth       : Double  = 0.0,
                        maxWidth       : Double? = null,
            private val extractor      : Extractor<T, R>,
                        firstColumn    : Boolean,
            val         sorter         : Sorter<T, S>?,
                        editor         : TableEditor<T>?): InternalListColumn<R>(header, headerAlignment, cellGenerator, cellAlignment, preferredWidth, minWidth, maxWidth, extractor, firstColumn), MutableColumn<T, R> {

        private inner class FieldModel<A>(model: M, extractor: Extractor<T, A>): InternalListColumn<R>.FieldModel<A>(model, extractor), MutableListModel<A> {
            override fun set        (index  : Int, value : A            ): A? = this[index] // This is essential to allow MutableList to handle edits that result in the content being unchanged
            override fun add        (              value : A            ) { /*NO-OP*/ }
            override fun add        (index  : Int, value : A            ) { /*NO-OP*/ }
            override fun remove     (              value : A            ) { /*NO-OP*/ }
            override fun removeAt   (index  : Int                       ): A? = null // NO-OP
            override fun addAll     (              values: Collection<A>) { /*NO-OP*/ }
            override fun addAll     (index  : Int, values: Collection<A>) { /*NO-OP*/ }
            override fun removeAll  (              values: Collection<A>) { /*NO-OP*/ }
            override fun retainAll  (              values: Collection<A>) { /*NO-OP*/ }
            override fun removeAllAt(indexes: Collection<Int>           ) { /*NO-OP*/ }
            override fun clear      (                                   ) { /*NO-OP*/ }
        }

        private inner class ListEditorAdapter(private val editor: TableEditor<T>): ListEditor<R> {
            override fun edit(list: MutableList<R, *>, row: R, index: Int, current: View) = editor(this@MutableTable, model[index]!!, this@MutableInternalListColumn, index, current).let {
                object: EditOperation<R> {
                    override fun invoke  () = it.invoke()
                    override fun complete() = it.complete()?.also { model[index] = it }?.let(extractor)
                    override fun cancel  () = it.cancel()
                }
            }
        }

        override var editor = editor
            set(new) {
                field = new

                view.editor = new?.let { ListEditorAdapter(it) }
            }

        override val view: MutableList<R, *> = MutableList(FieldModel(model, extractor), object: IndexedItemVisualizer<R> {
            override fun invoke(item: R, index: Int, previous: View?) = object: View() {}
        }, selectionModelWrapper).apply {
            acceptsThemes = false

            this@MutableInternalListColumn.editor?.let {
                this.editor = ListEditorAdapter(it)
            }
        }

        override fun sort(list: MutableListModel<T>) {
            sorter?.let {
                list.sortWith(Comparator { a, b -> it(a).compareTo(it(b)) })
            }
        }
    }

    private inner class MutableColumnFactoryImpl: MutableColumnFactory<T> {
        override fun <R, S: Comparable<S>> column(header: View?, extractor: Extractor<T, R>, cellVisualizer: CellVisualizer<R>, editor: TableEditor<T>?, sorter: Sorter<T, S>?, builder: ColumnBuilder.() -> Unit) = ColumnBuilderImpl().run {
            builder(this)

            MutableInternalListColumn(header, headerAlignment, cellVisualizer, cellAlignment, width, minWidth, maxWidth, extractor, internalColumns.isEmpty(), sorter, editor).also {
                internalColumns += it
            }
        }
    }

    init {
        MutableColumnFactoryImpl().apply(block)
    }

    val editing get() = editingColumn != null

    private var editingColumn = null as MutableInternalListColumn<T, *>?

    operator fun set(index: Int, value: T) { model[index] = value }

    fun add      (value : T                         ) = model.add      (value        )
    fun add      (index : Int, values: T            ) = model.add      (index, values)
    fun remove   (value : T                         ) = model.remove   (value        )
    fun removeAt (index : Int                       ) = model.removeAt (index        )
    fun addAll   (values: Collection<T>             ) = model.addAll   (values       )
    fun addAll   (index : Int, values: Collection<T>) = model.addAll   (index, values)
    fun removeAll(values: Collection<T>             ) = model.removeAll(values       )
    fun retainAll(values: Collection<T>             ) = model.retainAll(values       )

    fun clear() = model.clear()

    fun sort(by: MutableColumn<T, *>) {
        by.sort(model)
    }

    fun sortDescending(by: MutableColumn<T, *>) {

    }

    fun startEditing(index: Int, column: MutableColumn<T, *>) {
        (column as? MutableInternalListColumn<T, *>)?.let {
            editingColumn = it
            it.view.startEditing(index)
        }
    }

    fun completeEditing() {
        editingColumn?.view?.completeEditing()
    }

    fun cancelEditing() {
        editingColumn?.view?.cancelEditing()
    }

    companion object {
//        operator fun invoke(
//                strand        : Strand,
//                progression   : IntProgression,
//                itemGenerator : ItemGenerator<Int>,
//                selectionModel: SelectionModel<Int>? = null,
//                fitContent    : Boolean              = true,
//                scrollCache   : Int                  = 10) =
//                MutableTable(strand, progression.toMutableList(), itemGenerator, selectionModel, fitContent, scrollCache)
//
//        operator fun <T> invoke(
//                strand        : Strand,
//                values        : kotlin.collections.List<T>,
//                itemGenerator : ItemGenerator<T>,
//                selectionModel: SelectionModel<Int>? = null,
//                fitContent    : Boolean              = true,
//                scrollCache   : Int                  = 10): MutableList<T, SimpleMutableListModel<T>> =
//                MutableTable(strand, SimpleMutableListModel(values.toMutableList()), itemGenerator, selectionModel, fitContent, scrollCache)
    }
}