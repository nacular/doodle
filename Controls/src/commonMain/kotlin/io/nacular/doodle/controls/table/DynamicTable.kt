package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.DynamicListModel
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ModelObserver
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.list.DynamicList
import io.nacular.doodle.controls.mutableListModelOf
import io.nacular.doodle.core.View
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.utils.Extractor
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.diff.Differences

/**
 * Created by Nicholas Eddy on 9/29/19.
 */

internal class SelectionModelWrapper(private val delegate: SelectionModel<Int>): SelectionModel<Int> by delegate {
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

public open class DynamicTable<T, M: DynamicListModel<T>>(
        model         : M,
        selectionModel: SelectionModel<Int>? = null,
        scrollCache   : Int                  = 0,
        block         : ColumnFactory<T>.() -> Unit
): Table<T, M>(model, selectionModel, scrollCache, block) {

    private inner class ColumnFactoryImpl: ColumnFactory<T> {
        override fun <R> column(header: View?, extractor: Extractor<T, R>, cellVisualizer: CellVisualizer<T, R>, footer: View?, builder: ColumnBuilder.() -> Unit) = ColumnBuilderImpl().run {
            builder(this)

            InternalListColumn(header, headerAlignment, footer, footerAlignment, cellVisualizer, cellAlignment, width, minWidth, maxWidth, extractor, internalColumns.isEmpty()).also { internalColumns += it }
        }
    }

    internal open inner class InternalListColumn<R>(
            header         : View?,
            headerAlignment: (ConstraintDslContext.(Bounds) -> Unit)? = null,
            footer         : View?,
            footerAlignment: (ConstraintDslContext.(Bounds) -> Unit)? = null,
            cellGenerator  : CellVisualizer<T, R>,
            cellAlignment  : (ConstraintDslContext.(Bounds) -> Unit)? = null,
            preferredWidth : Double? = null,
            minWidth       : Double  = 0.0,
            maxWidth       : Double? = null,
            extractor      : Extractor<T, R>,
            private val firstColumn: Boolean
    ): Table<T, M>.InternalListColumn<R>(header, headerAlignment, footer, footerAlignment, cellGenerator, cellAlignment, preferredWidth, minWidth, maxWidth, extractor) {

        protected open inner class FieldModel<A>(private val model: M, private val extractor: Extractor<T, A>): DynamicListModel<A> {
            init {
                model.changed += { _: DynamicListModel<T>, differences: Differences<T> ->

                    if (!firstColumn) {
                        selectionModelWrapper?.allowMutations = false
                    }

                    changed.forEach {
                        it(this, differences.map { extractor(it) })
                    }

                    if (!firstColumn) {
                        selectionModelWrapper?.allowMutations = true
                    }
                }
            }

            override val changed = SetPool<ModelObserver<A>>()

            override val size get() = model.size

            override fun get(index: Int): Result<A> = model[index].map(extractor)

            override fun section(range: ClosedRange<Int>) = model.section(range).map(extractor)

            override fun contains(value: A) = value in model.map(extractor)

            override fun iterator() = model.map(extractor).iterator()
        }

        protected val selectionModelWrapper = selectionModel?.let { SelectionModelWrapper(it) }

        override val view: DynamicList<R, *> by lazy {
            DynamicList(
                FieldModel(model, extractor),
                object: ItemVisualizer<R, IndexedItem> {
                    override fun invoke(item: R, previous: View?, context: IndexedItem) = object : View() {}
                },
                selectionModelWrapper,
            ).apply {
                acceptsThemes = false
            }
        }
    }

    override val factory: ColumnFactory<T> = ColumnFactoryImpl()

    public companion object {
        public inline operator fun <reified T> invoke(
                values        : List<T>,
                selectionModel: SelectionModel<Int>? = null,
                scrollCache   : Int                  = 0,
                noinline block: ColumnFactory<T>.() -> Unit
        ): DynamicTable<T, MutableListModel<T>> = DynamicTable(mutableListModelOf(*values.toTypedArray()), selectionModel, scrollCache, block)
    }
}