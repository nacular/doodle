package com.nectar.doodle.controls.table

import com.nectar.doodle.controls.DynamicListModel
import com.nectar.doodle.controls.IndexedItemVisualizer
import com.nectar.doodle.controls.ModelObserver
import com.nectar.doodle.controls.MutableListModel
import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.controls.list.DynamicList
import com.nectar.doodle.controls.list.ListBehavior
import com.nectar.doodle.controls.mutableListModelOf
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.layout.Constraints
import com.nectar.doodle.utils.SetPool

/**
 * Created by Nicholas Eddy on 9/29/19.
 */

private class SelectionModelWrapper(private val delegate: SelectionModel<Int>): SelectionModel<Int> by delegate {
    var allowMutations = true

    override fun add(item: Int) = when {
        allowMutations -> delegate.add(item)
        else           -> false
    }

    override fun clear() {
        if (allowMutations) {
            delegate.clear()
        }
    }

    override fun addAll(items: Collection<Int>) = when {
        allowMutations -> delegate.addAll(items)
        else           -> false
    }

    override fun remove(item: Int) = when {
        allowMutations -> delegate.remove(item)
        else           -> false
    }

    override fun removeAll(items: Collection<Int>) = when {
        allowMutations -> delegate.removeAll(items)
        else           -> false
    }

    override fun retainAll(items: Collection<Int>) = when {
        allowMutations -> delegate.retainAll(items)
        else           -> false
    }

    override fun replaceAll(items: Collection<Int>) = when {
        allowMutations -> delegate.replaceAll(items)
        else           -> false
    }

    override fun toggle(items: Collection<Int>) = when {
        allowMutations -> delegate.toggle(items)
        else           -> false
    }
}

open class DynamicTable<T, M: DynamicListModel<T>>(
        model         : M,
        selectionModel: SelectionModel<Int>? = null,
        scrollCache   : Int                  = 10,
        block         : ColumnFactory<T>.() -> Unit): Table<T, M>(model, selectionModel, scrollCache, block) {

    private inner class ColumnFactoryImpl: ColumnFactory<T> {
        override fun <R> column(header: View?, extractor: Extractor<T, R>, cellVisualizer: CellVisualizer<R>, builder: ColumnBuilder.() -> Unit) = ColumnBuilderImpl().run {
            builder(this)

            InternalListColumn(header, headerAlignment, cellVisualizer, cellAlignment, width, minWidth, maxWidth, extractor, internalColumns.isEmpty()).also { internalColumns += it }
        }
    }

    internal open inner class InternalListColumn<R>(
            header         : View?,
            headerAlignment: (Constraints.() -> Unit)? = null,
            cellGenerator  : CellVisualizer<R>,
            cellAlignment  : (Constraints.() -> Unit)? = null,
            preferredWidth : Double? = null,
            minWidth       : Double  = 0.0,
            maxWidth       : Double? = null,
            extractor      : Extractor<T, R>,
            private val firstColumn: Boolean): InternalColumn<TableLikeWrapper, TableLikeBehaviorWrapper, R>(TableLikeWrapper(), TableLikeBehaviorWrapper(), header, headerAlignment, cellGenerator, cellAlignment, preferredWidth, minWidth, maxWidth) {

        private inner class FieldModel<A>(private val model: M, private val extractor: Extractor<T, A>): DynamicListModel<A> {
            init {
                model.changed += { _: DynamicListModel<T>, removed: Map<Int, T>, added: Map<Int, T>, moved: Map<Int, Pair<Int, T>> ->

                    if (!firstColumn) {
                        selectionModelWrapper?.allowMutations = false
                    }

                    changed.forEach {
                        it(this,removed.mapValues { extractor(it.value) }, added.mapValues { extractor(it.value) }, moved.mapValues { it.value.first to extractor(it.value.second) })
                    }

                    if (!firstColumn) {
                        selectionModelWrapper?.allowMutations = true
                    }
                }
            }

            override val changed = SetPool<ModelObserver<A>>()

            override val size get() = model.size

            override fun get(index: Int) = model[index]?.let(extractor)

            override fun section(range: ClosedRange<Int>) = model.section(range).map(extractor)

            override fun contains(value: A) = value in model.map(extractor)

            override fun iterator() = model.map(extractor).iterator()
        }

        private val selectionModelWrapper = selectionModel?.let { SelectionModelWrapper(it) }

        override val view: DynamicList<R, *> = DynamicList(FieldModel(model, extractor), object: IndexedItemVisualizer<R> {
            override fun invoke(item: R, index: Int, previous: View?) = object: View() {}
        }, selectionModelWrapper).apply {
            acceptsThemes = false
        }

        override fun behavior(behavior: TableLikeBehaviorWrapper?) {
            behavior?.delegate?.let {
                view.behavior = object: ListBehavior<R> {
                    override val generator get() = object: ListBehavior.RowGenerator<R> {
                        override fun invoke(list: com.nectar.doodle.controls.list.List<R, *>, row: R, index: Int, current: View?) = it.cellGenerator(this@DynamicTable, this@InternalListColumn, row, index, object: IndexedItemVisualizer<R> {
                            override fun invoke(item: R, index: Int, previous: View?) = this@InternalListColumn.cellGenerator(this@InternalListColumn, item, index, previous) { list.selected(index) }
                        }, current)
                    }

                    override val positioner get() = object: ListBehavior.RowPositioner<R> {
                        override fun invoke(list: com.nectar.doodle.controls.list.List<R, *>, row: R, index: Int) = it.rowPositioner.invoke(this@DynamicTable, model[index]!!, index).run { Rectangle(0.0, y, list.width, height) }

                        override fun rowFor(list: com.nectar.doodle.controls.list.List<R, *>, y: Double) = it.rowPositioner.rowFor(this@DynamicTable, y)
                    }

                    override fun render(view: com.nectar.doodle.controls.list.List<R, *>, canvas: Canvas) {
                        if (this@InternalListColumn != internalColumns.last()) {
                            it.renderColumnBody(this@DynamicTable, this@InternalListColumn, canvas)
                        }
                    }
                }
            }
        }
    }

    override val factory: ColumnFactory<T> = ColumnFactoryImpl()

    companion object {
        operator fun <T> invoke(
                values        : List<T>,
                selectionModel: SelectionModel<Int>? = null,
                scrollCache   : Int                  = 10,
                block         : ColumnFactory<T>.() -> Unit): DynamicTable<T, MutableListModel<T>> = DynamicTable(mutableListModelOf(*values.toTypedArray()), selectionModel, scrollCache, block)
    }
}