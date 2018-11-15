package com.nectar.doodle.controls.list

import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.controls.list.ListRenderer.ItemPositioner
import com.nectar.doodle.controls.list.ListRenderer.ItemUIGenerator
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.scheduler.Strand
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.AdaptingObservableSet
import com.nectar.doodle.utils.ObservableSet
import com.nectar.doodle.utils.Pool
import com.nectar.doodle.utils.SetObserver
import com.nectar.doodle.utils.SetPool
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 3/19/18.
 */
interface ListRenderer<T>: Renderer<List<T, *>> {
    interface ItemUIGenerator<T> {
        operator fun invoke(list: List<T, *>, row: T, index: Int, current: View? = null): View
    }

    interface ItemPositioner<T> {
        operator fun invoke(list: List<T, *>, row: T, index: Int): Rectangle

        fun rowFor(list: List<T, *>, y: Double): Int
    }

    val positioner : ItemPositioner<T>
    val uiGenerator: ItemUIGenerator<T>
}

open class List<T, out M: Model<T>>(
        private        val strand        : Strand,
        protected open val model         : M,
        protected      val selectionModel: SelectionModel<Int>? = null,
        private        val fitContent    : Boolean              = true): View() {

    val selectionChanged: Pool<SetObserver<List<T, *>, Int>> = SetPool()

    @Suppress("PrivatePropertyName")
    protected open val selectionChanged_: SetObserver<SelectionModel<Int>, Int> = { set,removed,added ->
        val adaptingSet: ObservableSet<List<T, *>, Int> = AdaptingObservableSet(this, set)

        (selectionChanged as SetPool).forEach {
            it(adaptingSet, removed, added)
        }

        children.batch {
            (added + removed).forEach {
                update(this, it)
            }
        }
    }

    private var itemPositioner : ItemPositioner<T>?  = null
    private var itemUIGenerator: ItemUIGenerator<T>? = null

    protected var firstVisibleRow =  0
    protected var lastVisibleRow  = -1

    var renderer: ListRenderer<T>? = null
        set(new) {
            if (new == renderer) { return }

            field = new?.also {
                itemPositioner  = it.positioner
                itemUIGenerator = it.uiGenerator

                children.batch {
                    clear()

                    updateVisibleHeight()
                }
            }
        }

    protected fun updateVisibleHeight() {
        height = model.size * (model[0]?.let { itemPositioner?.invoke(this@List, it, 0)?.height } ?: 0.0) + insets.run { top + bottom }
    }

    public override var insets
        get(   ) = super.insets
        set(new) { super.insets = new }

    init {
        monitorsDisplayRect = true

        selectionModel?.let { it.changed += selectionChanged_ }
    }

    operator fun get(index: Int) = model[index]

    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    override fun removedFromDisplay() {
        selectionModel?.let { it.changed -= selectionChanged_ }

        super.removedFromDisplay()
    }

    override fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {
        val oldFirst = firstVisibleRow
        val oldLast  = lastVisibleRow

        firstVisibleRow = when (val y = new.y) {
            old.y -> firstVisibleRow
            else  -> findRowAt(y, firstVisibleRow)
        }

        lastVisibleRow = when (val y = new.y + new.height) {
            old.y + old.height -> lastVisibleRow
            else               -> findRowAt(y, lastVisibleRow)
        }

        var jobs = emptySequence<() -> Unit>()

        if (oldFirst > firstVisibleRow) {
            val end = min(oldFirst, lastVisibleRow)

            jobs += (firstVisibleRow until end).asSequence().map { { insert(children, it) } }
        }

        if (oldLast < lastVisibleRow) {
            val start = when {
                oldLast > firstVisibleRow -> oldLast + 1
                else                      -> firstVisibleRow
            }

            jobs += (start .. lastVisibleRow).asSequence().map { { insert(children, it) } }
        }

        strand(jobs)
    }

//    override fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {
//        val oldFirst = firstVisibleRow
//        val oldLast  = lastVisibleRow
//
//        firstVisibleRow = new.y.let { when {
//            it != old.y -> findRowAt(it, firstVisibleRow)
//            else        -> firstVisibleRow
//        }}
//
//        lastVisibleRow = (new.y + new.height).let { when {
//            it != old.y + old.height -> findRowAt(it, lastVisibleRow)
//            else                     -> lastVisibleRow
//        }}
//
//        if (oldFirst > firstVisibleRow) {
//            val end = min(oldFirst, lastVisibleRow)
//
//            (firstVisibleRow until end).asSequence().forEach {
//                insert(children, it)
//            }
//        }
//
//        if (oldLast < lastVisibleRow) {
//            val start = when {
//                oldLast > firstVisibleRow -> oldLast + 1
//                else                      -> firstVisibleRow
//            }
//
//            (start .. lastVisibleRow).asSequence().forEach {
//                insert(children, it)
//            }
//        }
//    }

    fun selected       (row : Int     ) = selectionModel?.contains  (row ) ?: false
    fun addSelection   (rows: Set<Int>) { selectionModel?.addAll    (rows) }
    fun setSelection   (rows: Set<Int>) { selectionModel?.replaceAll(rows) }
    fun removeSelection(rows: Set<Int>) { selectionModel?.removeAll (rows) }
    fun clearSelection (              ) = selectionModel?.clear     (    )

    val selection get() = selectionModel?.toSet() ?: emptySet()

    protected fun layout(view: View, row: T, index: Int) {
        itemPositioner?.let {
            view.bounds = it(this, row, index)
        }
    }

    private fun insert(children: kotlin.collections.MutableList<View>, index: Int) {
        itemUIGenerator?.let {
            model[index]?.let { row ->
                if (children.size <= lastVisibleRow - firstVisibleRow) {
                    it(this, row, index).also {
                        when {
                            index > children.lastIndex -> children.add(it)
                            else                       -> children.add(index, it)
                        }

                        layout(it, row, index)
                    }
                } else {
                    update(children, index)
                }
            }
        }
    }

    protected fun update(children: kotlin.collections.MutableList<View>, index: Int) {
        if (index in firstVisibleRow .. lastVisibleRow) {
            itemUIGenerator?.let {
                model[index]?.let { row ->
                    val i = index % children.size

                    it(this, row, index, children.getOrNull(i)).also {
                        children[i] = it

                        layout(it, row, index)
                    }
                }
            }
        }
    }

    private fun findRowAt(y: Double, nearbyRow: Int): Int {
        return min(model.size - 1, itemPositioner?.rowFor(this, y) ?: nearbyRow)
    }

    companion object {
        operator fun invoke(strand: Strand, progression: IntProgression, selectionModel: SelectionModel<Int>? = null, fitContent: Boolean = true) =
                List<Int, Model<Int>>(strand, IntProgressionModel(progression), selectionModel, fitContent)

        operator fun <T> invoke(strand: Strand, values: kotlin.collections.List<T>, selectionModel: SelectionModel<Int>? = null, fitContent: Boolean = true): List<T, Model<T>> =
                List<T, Model<T>>(strand, ListModel(values), selectionModel, fitContent)
    }
}

interface EditOperation<T> {
    operator fun invoke(): View
    fun finish(): T
    fun cancel()
}

interface ListEditor<T> {
    fun edit(list: MutableList<T, *>, row: T, index: Int, current: View? = null): EditOperation<T>
}

open class MutableList<T, M: MutableModel<T>>(strand: Strand, model: M, selectionModel: SelectionModel<Int>? = null, fitContent: Boolean = true): List<T, M>(strand, model, selectionModel, fitContent) {
    private val modelChanged: ModelObserver<T> = { _,removed,added,moved ->
        val trueRemoved = removed.filterKeys { it !in added   }
        val trueAdded   = added.filterKeys   { it !in removed }

        itemsRemoved(trueRemoved)
        itemsAdded  (trueAdded  )

        val oldSize = children.size

        children.batch {
            trueRemoved.forEach { removeAt(it.key) }

            (added + moved).map { it.key }.forEach {
                update(this, it)
            }
        }

        if (oldSize > children.size) {
            updateVisibleHeight()

            // FIXME: Make this more efficient
            (firstVisibleRow..lastVisibleRow).forEach { update(children, it) }
        }
    }

    override var model = model
        set(new) {
            field.changed -= modelChanged
            field          = new
            field.changed += modelChanged
        }

    init {
        model.changed += modelChanged
    }

    val editing get() = editingRow != null

    var listEditor = null as ListEditor<T>?

    private var editingRow    = null as Int?
    private var editOperation = null as EditOperation<T>?

    fun add      (value : T                         ) = model.add      (value        )
    fun add      (index : Int, values: T            ) = model.add      (index, values)
    fun remove   (value : T                         ) = model.remove   (value        )
    fun removeAt (index : Int                       ) = model.removeAt (index        )
    fun addAll   (values: Collection<T>             ) = model.addAll   (values       )
    fun addAll   (index : Int, values: Collection<T>) = model.addAll   (index, values)
    fun removeAll(values: Collection<T>             ) = model.removeAll(values       )
    fun retainAll(values: Collection<T>             ) = model.retainAll(values       )

    fun clear() = model.clear()


    override fun removedFromDisplay() {
        model.changed -= modelChanged

        super.removedFromDisplay()
    }

    fun startEditing(index: Int) {
        listEditor?.let {
            model[index]?.let { row ->
                val i = index % children.size

                editingRow    = index
                editOperation = it.edit(this, row, index, children.getOrNull(i)).also {
                    children[i] = it()

                    layout(children[i], row, index)
                }
            }
        }
    }

    fun completeEditing() {
        editOperation?.let { operation ->
            editingRow?.let { index ->
                val result = operation.finish()

                cleanupEditing()

                if (result == model.set(index, result)) {
                    // This is the case that the "new" value is the same as what was there
                    // so need to explicitly update since the model won't fire a change
                    update(children, index)
                }
            }
        }
    }

    fun cancelEditing() {
        cleanupEditing()?.let { update(children, it) }
    }

    private fun cleanupEditing(): Int? {
        editOperation?.cancel()
        val result    = editingRow
        editOperation = null
        editingRow    = null
        return result
    }

    private fun itemsAdded(values: Map<Int, T>) {
        if (selectionModel != null) {
            val updatedSelection = mutableSetOf<Int>()

            for (selectionItem in selectionModel) {
                var delta = 0

                for (index in values.keys) {
                    if (selectionItem >= index) {
                        ++delta
                    }
                }

                updatedSelection.add(selectionItem + delta)
            }

            setSelection(updatedSelection)
        }
    }

    private fun itemsRemoved(values: Map<Int, T>) {
        if (selectionModel != null) {

            val updatedSelection = mutableSetOf<Int>()

            for (selectionItem in selectionModel) {
                var delta = 0

                for (aIndex in values.keys) {
                    if (selectionItem > aIndex) {
                        delta--
                    }
                }

                if (delta > 0) {
                    updatedSelection.add(selectionItem + delta)
                } else {
                    updatedSelection.add(selectionItem)
                }
            }

            setSelection(updatedSelection)
        }
    }

    companion object {
        operator fun <T> invoke(strand: Strand, values: kotlin.collections.List<T>, selectionModel: SelectionModel<Int>? = null, fitContent: Boolean = true): MutableList<T, MutableListModel<T>> =
                MutableList(strand, MutableListModel(values.toMutableList()), selectionModel, fitContent)
    }
}

private class IntProgressionModel(private val progression: IntProgression): Model<Int> {
    override val size = progression.run { (last - first) / step }

    override fun get(index: Int) = progression.elementAt(index)

    override fun section(range: ClosedRange<Int>) = progression.asSequence().drop(range.start).take(range.endInclusive - range.start).toList()

    override fun contains(value: Int) = progression.contains(value)

    override fun iterator() = progression.iterator()
}