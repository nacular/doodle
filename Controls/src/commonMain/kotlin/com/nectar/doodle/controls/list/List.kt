package com.nectar.doodle.controls.list

import com.nectar.doodle.controls.ItemGenerator
import com.nectar.doodle.controls.SimpleListModel
import com.nectar.doodle.controls.ListSelectionManager
import com.nectar.doodle.controls.ListModel
import com.nectar.doodle.controls.Selectable
import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.controls.list.ListBehavior.RowGenerator
import com.nectar.doodle.controls.list.ListBehavior.RowPositioner
import com.nectar.doodle.controls.panels.ScrollPanel
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.scheduler.Strand
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.utils.AdaptingObservableSet
import com.nectar.doodle.utils.ObservableSet
import com.nectar.doodle.utils.Pool
import com.nectar.doodle.utils.SetObserver
import com.nectar.doodle.utils.SetPool
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 3/19/18.
 */
interface ListBehavior<T>: Behavior<List<T, *>> {
    interface RowGenerator<T> {
        operator fun invoke(list: List<T, *>, row: T, index: Int, current: View? = null): View
    }

    interface RowPositioner<T> {
        operator fun invoke(list: List<T, *>, row: T, index: Int): Rectangle

        fun rowFor(list: List<T, *>, y: Double): Int
    }

    val generator : RowGenerator<T>
    val positioner: RowPositioner<T>
}

open class List<T, out M: ListModel<T>>(
        private        val strand        : Strand,
        protected open val model         : M,
                       val itemGenerator : ItemGenerator<T>?    = null,
        protected      val selectionModel: SelectionModel<Int>? = null,
        private        val fitContent    : Boolean              = true,
        private        val cacheLength   : Int                  = 10): View(), Selectable<Int> by ListSelectionManager(selectionModel, { model.size }) {

    val numRows: Int get() = model.size
    val selectionChanged: Pool<SetObserver<List<T, *>, Int>> = SetPool()

    @Suppress("PrivatePropertyName")
    protected open val selectionChanged_: SetObserver<SelectionModel<Int>, Int> = { set,removed,added ->
        mostRecentAncestor { it is ScrollPanel }?.let { it as ScrollPanel }?.let { parent ->
            lastSelection?.let { added ->
                rowPositioner?.invoke(this, this[added]!!, added)?.let {
                    parent.scrollToVisible(it)
                }
            }
        }

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

    private var rowGenerator    : RowGenerator <T>? = null
    private var rowPositioner   : RowPositioner<T>? = null
    private val halfCacheLength = cacheLength / 2
    private var minVisibleY     = 0.0
    private var maxVisibleY     = 0.0

    protected var firstVisibleRow =  0
    protected var lastVisibleRow  = -1

    var behavior: ListBehavior<T>? = null
        set(new) {
            if (new == behavior) { return }

            field?.uninstall(this)

            field = new?.also {
                this.rowGenerator  = it.generator
                this.rowPositioner = it.positioner

                children.batch {
                    clear()

                    updateVisibleHeight()
                }

                it.install(this)
            }
        }

    protected fun updateVisibleHeight() {
        height = model.size * (model[0]?.let { rowPositioner?.invoke(this@List, it, 0)?.height } ?: 0.0) + insets.run { top + bottom }
    }

    public override var insets
        get(   ) = super.insets
        set(new) { super.insets = new }

    init {
        monitorsDisplayRect = true

        selectionModel?.let { it.changed += selectionChanged_ }
    }

    operator fun get(index: Int) = model[index]

    override var isFocusCycleRoot = true

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun removedFromDisplay() {
        selectionModel?.let { it.changed -= selectionChanged_ }

        super.removedFromDisplay()
    }

    override fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {
        rowPositioner?.let { positioner ->
            if (maxVisibleY > new.bottom && minVisibleY < new.y) {
                return
            }

            val oldFirst = firstVisibleRow
            val oldLast = lastVisibleRow

            firstVisibleRow = when (val y = new.y) {
                old.y -> firstVisibleRow
                else  -> max(0, findRowAt(y, firstVisibleRow) - cacheLength)
            }

            lastVisibleRow = when (val y = new.bottom) {
                old.bottom -> lastVisibleRow
                else       -> min(model.size - 1, findRowAt(y, lastVisibleRow) + cacheLength)
            }

            model[firstVisibleRow + halfCacheLength]?.let { minVisibleY = positioner(this, it, firstVisibleRow + halfCacheLength).y      }
            model[lastVisibleRow  - halfCacheLength]?.let { maxVisibleY = positioner(this, it, lastVisibleRow  - halfCacheLength).bottom }

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

                jobs += (start..lastVisibleRow).asSequence().map { { insert(children, it) } }
            }

            // TODO: Is there a better way to avoid laying out when only a scroll happens?
            strand(jobs.ifEmpty {
                (firstVisibleRow..lastVisibleRow).asSequence().mapNotNull { index -> model[index]?.let { item -> { layout(children[index % children.size], item, index) } } }
            })
        }
    }

    protected fun layout(view: View, row: T, index: Int) {
        rowPositioner?.let {
            view.bounds = it(this, row, index)
        }
    }

    private fun insert(children: kotlin.collections.MutableList<View>, index: Int) {
        rowGenerator?.let { uiGenerator ->
            model[index]?.let { row ->
                if (children.size <= lastVisibleRow - firstVisibleRow) {
                    uiGenerator(this, row, index).also { ui ->
                        when {
                            index > children.lastIndex -> children.add(ui)
                            else                       -> children.add(index, ui)
                        }

                        layout(ui, row, index)
                    }
                } else {
                    update(children, index)
                }
            }
        }
    }

    protected fun update(children: kotlin.collections.MutableList<View>, index: Int) {
        if (index in firstVisibleRow .. lastVisibleRow) {
            rowGenerator?.let { uiGenerator ->
                model[index]?.let { row ->
                    val i = index % children.size

                    uiGenerator(this, row, index, children.getOrNull(i)).also { ui ->
                        children[i] = ui

                        layout(ui, row, index)
                    }
                }
            }
        }
    }

    private fun findRowAt(y: Double, nearbyRow: Int): Int {
        return min(model.size - 1, rowPositioner?.rowFor(this, y) ?: nearbyRow)
    }

    companion object {
        operator fun invoke(
                strand        : Strand,
                progression   : IntProgression,
                itemGenerator : ItemGenerator<Int>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                cacheLength   : Int                  = 10) =
                List<Int, ListModel<Int>>(strand, IntProgressionModel(progression), itemGenerator, selectionModel, fitContent, cacheLength)

        operator fun <T> invoke(
                strand        : Strand,
                values        : kotlin.collections.List<T>,
                itemGenerator : ItemGenerator<T>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                cacheLength   : Int                  = 10): List<T, ListModel<T>> =
                List<T, ListModel<T>>(strand, SimpleListModel(values), itemGenerator, selectionModel, fitContent, cacheLength)
    }
}

private class IntProgressionModel(private val progression: IntProgression): ListModel<Int> {
    override val size = progression.run { (last - first) / step }

    override fun get(index: Int) = progression.elementAt(index)

    override fun section(range: ClosedRange<Int>) = progression.asSequence().drop(range.start).take(range.endInclusive - range.start).toList()

    override fun contains(value: Int) = progression.contains(value)

    override fun iterator() = progression.iterator()
}