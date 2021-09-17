package io.nacular.doodle.controls.list

import io.nacular.doodle.accessibility.ListRole
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.ListSelectionManager
import io.nacular.doodle.controls.Selectable
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.SimpleListModel
import io.nacular.doodle.controls.ViewVisualizer
import io.nacular.doodle.controls.list.ListBehavior.RowGenerator
import io.nacular.doodle.controls.list.ListBehavior.RowPositioner
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.core.mostRecentAncestor
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.SetObserver
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.addOrAppend
import kotlin.math.max
import kotlin.math.min


public interface ListLike: Selectable<Int> {
    public val hasFocus    : Boolean
    public val focusChanged: PropertyObservers<View, Boolean>
    public val numRows     : Int
}

/**
 * Controls how a [List] behaves and is rendered, by providing key strategies.
 */
public interface ListBehavior<T>: Behavior<List<T, *>> {
    /**
     * [List] uses this to create each row it displays. The generator provides a mapping between some item [T]
     * and the [View] that will represent its row in the List.
     */
    public interface RowGenerator<T> {
        /**
         * Provides a mapping between an item [T] in the list at [index] and the [View] that should
         * be used to render its row.
         *
         * @param list being configured
         * @param row being rendered
         * @param index of the row in [list]
         * @param current View that could be recycled
         */
        public operator fun invoke(list: List<T, *>, row: T, index: Int, current: View? = null): View
    }

    /**
     * [List] uses this to determine where row geometry.
     */
    public interface RowPositioner<T> {
        /**
         * Provides the bounds for a given row in the list.
         *
         * @param of a specific List
         * @param row being rendered
         * @param index of the row
         * @param view being used to render that row
         */
        public fun rowBounds(of: List<T, *>, row: T, index: Int, view: View? = null): Rectangle

        /**
         * Returns the row at the given y-offset in a List.
         *
         * @param of a specific list
         * @param atY the y-offset in the list
         */
        public fun row(of: List<T, *>, atY: Double): Int

        /**
         * Returns the total height of the given List including all rows.
         *
         * @param of a specific List
         */
        public fun totalRowHeight(of: List<T, *>): Double
    }

    public val generator : RowGenerator<T>
    public val positioner: RowPositioner<T>
}

/**
 * A visual component that renders an immutable list of items of type [T] using a [ListBehavior]. Items are obtained via
 * the [model] and selection is managed via the optional [selectionModel]. Large ("infinite") lists are supported
 * efficiently, since List recycles the Views generated to render its rows.
 *
 * Note that this class assumes the given [ListModel] is immutable and therefore it will not automatically respond
 * to changes to it. See [DynamicList] or [MutableList] if this behavior is desirable.
 *
 * List does not provide scrolling internally, so it should be embedded in a [ScrollPanel] or similar component if needed.
 *
 * @param model that holds the data for this List
 * @param itemVisualizer that maps [T] to [View] for each item in the List
 * @param selectionModel that manages the List's selection state
 * @param fitContent determines whether the List scales to fit it's rows width and total height
 * @param scrollCache determining how many "hidden" rows are rendered above and below the List's view-port. A value of 0 means
 * only visible rows are rendered, but quick scrolling is more likely to show blank areas.
 *
 * @property model that holds the data for this List
 * @property itemVisualizer that maps [T] to [View] for each item in the List
 * @property selectionModel that manages the List's selection state
 * @property fitContent determines whether the List scales to fit it's rows width and total height
 * @property scrollCache determining how many "hidden" rows are rendered above and below the List's view-port. A value of 0 means
 * only visible rows are rendered, but quick scrolling is more likely to show blank areas.

 */
public open class List<T, out M: ListModel<T>>(
        protected open val model         : M,
        public         val itemVisualizer: ItemVisualizer<T, IndexedItem>? = null,
        protected      val selectionModel: SelectionModel<Int>?            = null,
        private        val fitContent    : Boolean                         = true,
        private        val scrollCache   : Int                             = 10): View(ListRole()), ListLike, Selectable<Int> by ListSelectionManager(selectionModel, { model.size }) {

    @Suppress("PropertyName")
    private val selectionChanged_: SetObserver<SelectionModel<Int>, Int> = { set,removed,added ->
        scrollToSelection() // FIXME: Avoid scrolling on selectAll, move to Behavior

        (selectionChanged as SetPool).forEach {
            it(this, removed, added)
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
    protected var minHeight: Double = 0.0
        set(new) {
            field       = new
            height      = field
            minimumSize = Size(minimumSize.width, field)
        }

    protected var firstVisibleRow: Int =  0
    protected var lastVisibleRow : Int = -1

    override val numRows: Int     get() = model.size
    public val isEmpty: Boolean get() = model.isEmpty

    /**
     * Notifies of changes to the List's selection.
     */
    public val selectionChanged: Pool<SetObserver<List<T, M>, Int>> = SetPool()

    /**
     * Defines how the contents of a row should be aligned within that row.
     */
    public var cellAlignment: (Constraints.() -> Unit)? = null

    public fun contains(value: T): Boolean = value in model

    /**
     * Controls how the List behaves and how its items are rendered. A List will not render without
     * a behavior specified.
     */
    public var behavior: ListBehavior<T>? by behavior { _,new ->
        new?.also {
            rowGenerator  = it.generator
            rowPositioner = it.positioner

            children.clear()

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

    public override var insets: Insets
        get(   ) = super.insets
        set(new) { super.insets = new }

    init {
        sizePreferencesChanged += { _,_,_ ->
            idealSize = minimumSize
        }

        monitorsDisplayRect = true

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

    /**
     * Returns the item at [index] if one exists, `null` otherwise.
     */
    public operator fun get(index: Int): T? = model[index]

    override var isFocusCycleRoot: Boolean = true

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun addedToDisplay() {
        selectionModel?.let { it.changed += selectionChanged_ }

        super.addedToDisplay()
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

            model[firstVisibleRow]?.let { minVisibleY = positioner.rowBounds(this, it, firstVisibleRow).y      }
            model[lastVisibleRow ]?.let { maxVisibleY = positioner.rowBounds(this, it, lastVisibleRow ).bottom }

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

            // this updates "hashing" of rows into the children list (using % of list size) since the children size has changed
            if (oldLast - oldFirst != lastVisibleRow - firstVisibleRow) {
                (firstVisibleRow .. lastVisibleRow).forEach {
                    update(children, it)
                }
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

                        if (index + 1 < numRows - 1) {
                            ui.nextInAccessibleReadOrder = children[(index + 1) % children.size]
                        }
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
                        children.addOrAppend(index, ui)
                        layout(ui, row, index)
                    }
                } else {
                    update(children, index)
                }
            }
        }
    }

    private fun findRowAt(y: Double, nearbyRow: Int) = min(model.size - 1, rowPositioner?.row(this, y) ?: nearbyRow)

    /**
     * Scrolls [row] into view if the List is within a [ScrollPanel].
     */
    public fun scrollTo(row: Int) {
        mostRecentAncestor { it is ScrollPanel }?.let { it as ScrollPanel }?.let { parent ->
            this[row]?.let {
                rowPositioner?.rowBounds(this, it, row)?.let {
                    parent.scrollVerticallyToVisible(it.y .. it.bottom)
                }
            }
        }
    }

    /**
     * Scrolls the last selected row into view if the List is within a [ScrollPanel].
     */
    public fun scrollToSelection() {
        lastSelection?.let { scrollTo(it) }
    }

    public companion object {
        public operator fun invoke(
                progression    : IntProgression,
                itemVisualizer : ItemVisualizer<Int, IndexedItem>,
                selectionModel : SelectionModel<Int>? = null,
                fitContent     : Boolean              = true,
                scrollCache    : Int                  = 10): List<Int, ListModel<Int>> =
                List<Int, ListModel<Int>>(IntProgressionModel(progression), itemVisualizer, selectionModel, fitContent, scrollCache)

        public operator fun <T> invoke(
                values        : kotlin.collections.List<T>,
                itemVisualizer: ItemVisualizer<T, IndexedItem>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10): List<T, ListModel<T>> =
                List<T, ListModel<T>>(SimpleListModel(values), itemVisualizer, selectionModel, fitContent, scrollCache)

        public operator fun invoke(
                values        : kotlin.collections.List<View>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10): List<View, ListModel<View>> =
                List<View, ListModel<View>>(SimpleListModel(values), ViewVisualizer, selectionModel, fitContent, scrollCache)

        public operator fun <T, M: ListModel<T>>invoke(
                model         : M,
                itemGenerator : ItemVisualizer<T, IndexedItem>? = null,
                selectionModel: SelectionModel<Int>?           = null,
                fitContent    : Boolean                        = true,
                scrollCache   : Int                            = 10): List<T, M> =
                List(model, itemGenerator, selectionModel, fitContent, scrollCache)
    }
}

private class IntProgressionModel(private val progression: IntProgression): ListModel<Int> {
    override val size = progression.run { (last - first) / step }

    override fun get(index: Int) = progression.elementAt(index)

    override fun section(range: ClosedRange<Int>) = progression.asSequence().drop(range.start).take(range.endInclusive - range.start).toList()

    override fun contains(value: Int) = progression.contains(value)

    override fun iterator() = progression.iterator()
}