package io.nacular.doodle.controls.list

import io.nacular.doodle.controls.IndexedItemVisualizer
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.ListSelectionManager
import io.nacular.doodle.controls.Selectable
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.SimpleListModel
import io.nacular.doodle.controls.list.ListBehavior.RowGenerator
import io.nacular.doodle.controls.list.ListBehavior.RowPositioner
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.mostRecentAncestor
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.SetObserver
import io.nacular.doodle.utils.SetPool
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
        fun rowBounds(of: List<T, *>, row: T, index: Int, view: View? = null): Rectangle

        fun row(of: List<T, *>, atY: Double): Int

        fun totalRowHeight(of: List<T, *>): Double
    }

    val generator : RowGenerator<T>
    val positioner: RowPositioner<T>
}

interface ListLike: Selectable<Int> {
    val hasFocus    : Boolean
    val focusChanged: PropertyObservers<View, Boolean>
}

open class List<T, out M: ListModel<T>>(
        protected open val model         : M,
                       val itemVisualizer: IndexedItemVisualizer<T>? = null,
        protected      val selectionModel: SelectionModel<Int>?      = null,
        private        val fitContent    : Boolean                   = true,
        private        val scrollCache   : Int                       = 10): View(), ListLike, Selectable<Int> by ListSelectionManager(selectionModel, { model.size }) {

    val numRows get() = model.size
    val isEmpty get() = model.isEmpty

    fun contains(value: T) = value in model

    val selectionChanged: Pool<SetObserver<Int>> = SetPool()

    @Suppress("PropertyName")
    private val selectionChanged_: SetObserver<Int> = { set,removed,added ->
        scrollToSelection() // FIXME: Avoid scrolling on selectAll, move to Behavior

        (selectionChanged as SetPool).forEach {
            it(set, removed, added)
        }

        children.batch {
            (added + removed).forEach {
                update(this, it)
            }
        }
    }

    private   var rowGenerator : RowGenerator <T>? = null
    private   var rowPositioner: RowPositioner<T>? = null
    private   var minVisibleY  = 0.0
    private   var maxVisibleY  = 0.0
    protected var minHeight    = 0.0
        set(new) {
            field  = new
            height = field
        }

    protected var firstVisibleRow =  0
    protected var lastVisibleRow  = -1

    var behavior: ListBehavior<T>? = null
        set(new) {
            if (new == behavior) { return }

            field?.uninstall(this)

            field = new?.also {
                rowGenerator  = it.generator
                rowPositioner = it.positioner

                children.clear()

                it.install(this)

                updateVisibleHeight()
            }
        }

    protected fun updateVisibleHeight() {
        val oldHeight = minHeight

        minHeight = rowPositioner?.totalRowHeight(this) ?: 0.0

        if (oldHeight == minHeight) {
            // FIXME: This reset logic could be handled better
            minVisibleY     =  0.0
            maxVisibleY     =  0.0
            firstVisibleRow =  0
            lastVisibleRow  = -1
        }

        handleDisplayRectEvent(Empty, displayRect)
    }

    public override var insets
        get(   ) = super.insets
        set(new) { super.insets = new }

    init {
        sizePreferencesChanged += { _,_,_ ->
            idealSize = minimumSize
        }

        monitorsDisplayRect = true

        selectionModel?.let { it.changed += selectionChanged_ }

        layout = object: Layout {
            override fun layout(container: PositionableContainer) {
                (firstVisibleRow .. lastVisibleRow).forEach {
                    model[it]?.let { row ->
                        children.getOrNull(it % children.size)?.let { child ->
                            layout(child, row, it)
                        }
                    }
                }
            }
        }
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
            val oldLast  = lastVisibleRow

            var y = new.y

            firstVisibleRow = when {
                y == old.y && !old.empty -> firstVisibleRow
                else                     -> max(0, findRowAt(y, firstVisibleRow) - scrollCache)
            }

            y = new.bottom

            lastVisibleRow = when {
                y == old.bottom && !old.empty -> lastVisibleRow
                else                          -> min(model.size - 1, findRowAt(y, lastVisibleRow) + scrollCache)
            }

            val halfCacheLength = min(children.size, scrollCache) / 2

            model[firstVisibleRow + halfCacheLength]?.let { minVisibleY = positioner.rowBounds(this, it, firstVisibleRow + halfCacheLength).y      }
            model[lastVisibleRow  - halfCacheLength]?.let { maxVisibleY = positioner.rowBounds(this, it, lastVisibleRow  - halfCacheLength).bottom }

            if (oldFirst > firstVisibleRow) {
                val end = min(oldFirst, lastVisibleRow)

                (firstVisibleRow until end).forEach { insert(children, it) }
            }

            if (oldLast < lastVisibleRow) {
                val start = when {
                    oldLast > firstVisibleRow -> oldLast + 1
                    else                      -> firstVisibleRow
                }

                (start..lastVisibleRow).forEach { insert(children, it) }
            }
        }
    }

    protected fun layout(view: View, row: T, index: Int) {
        rowPositioner?.let {
            view.bounds = it.rowBounds(this, row, index, view)

            minimumSize = Size(max(width, view.width), minHeight)
            idealSize   = Size(max(idealSize?.width ?: 0.0, view.idealSize?.width ?: minimumSize.width), minHeight)

            if (fitContent) {
                size = minimumSize
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

    private fun findRowAt(y: Double, nearbyRow: Int) = min(model.size - 1, rowPositioner?.row(this, y) ?: nearbyRow)

    fun scrollToSelection() {
        mostRecentAncestor { it is ScrollPanel }?.let { it as ScrollPanel }?.let { parent ->
            lastSelection?.let { lastSelection ->
                this[lastSelection]?.let {
                    rowPositioner?.rowBounds(this, it, lastSelection)?.let {
                        parent.scrollVerticallyToVisible(it.y .. it.bottom)
                    }
                }
            }
        }
    }

    companion object {
        operator fun invoke(
                progression    : IntProgression,
                itemVisualizer : IndexedItemVisualizer<Int>,
                selectionModel : SelectionModel<Int>? = null,
                fitContent     : Boolean              = true,
                scrollCache    : Int                  = 10) =
                List<Int, ListModel<Int>>(IntProgressionModel(progression), itemVisualizer, selectionModel, fitContent, scrollCache)

        operator fun <T> invoke(
                values        : kotlin.collections.List<T>,
                itemVisualizer: IndexedItemVisualizer<T>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10): List<T, ListModel<T>> =
                List<T, ListModel<T>>(SimpleListModel(values), itemVisualizer, selectionModel, fitContent, scrollCache)
    }
}

private class IntProgressionModel(private val progression: IntProgression): ListModel<Int> {
    override val size = progression.run { (last - first) / step }

    override fun get(index: Int) = progression.elementAt(index)

    override fun section(range: ClosedRange<Int>) = progression.asSequence().drop(range.start).take(range.endInclusive - range.start).toList()

    override fun contains(value: Int) = progression.contains(value)

    override fun iterator() = progression.iterator()
}