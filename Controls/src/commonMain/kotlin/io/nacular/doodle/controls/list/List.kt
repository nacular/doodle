package io.nacular.doodle.controls.list

import io.nacular.doodle.accessibility.ListRole
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.IntProgressionModel
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.ListSelectionManager
import io.nacular.doodle.controls.Selectable
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.SimpleListModel
import io.nacular.doodle.controls.ViewVisualizer
import io.nacular.doodle.controls.list.ListBehavior.ItemGenerator
import io.nacular.doodle.controls.list.ListBehavior.ItemPositioner
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.core.fixed
import io.nacular.doodle.core.scrollTo
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.utils.Dimension
import io.nacular.doodle.utils.Dimension.Height
import io.nacular.doodle.utils.Dimension.Width
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.SetObserver
import io.nacular.doodle.utils.SetObservers
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.addOrAppend
import io.nacular.doodle.utils.dimensionSetProperty
import io.nacular.doodle.utils.observable
import kotlin.math.max
import kotlin.math.min


public interface ListLike: Selectable<Int> {
    public val hasFocus    : Boolean
    public val focusChanged: PropertyObservers<View, Boolean>
    public val numItems    : Int
}

/**
 * Controls how a [List] behaves and is rendered, by providing key strategies.
 */
public interface ListBehavior<T>: Behavior<List<T, *>> {
    /**
     * [List] uses this to create each item it displays. The generator provides a mapping between some item [T]
     * and the [View] that will represent it in the List.
     */
    public interface ItemGenerator<T> {
        /**
         * Provides a mapping between an item [T] in the list at [index] and the [View] that should
         * be used to render its item.
         *
         * @param list being configured
         * @param item being rendered
         * @param index of the item in [list]
         * @param current View that could be recycled
         */
        public operator fun invoke(list: List<T, *>, item: T, index: Int, current: View? = null): View
    }

    /**
     * [List] uses this to determine item geometry.
     */
    public interface ItemPositioner<T> {
        /**
         * Provides the bounds for a given item in the list.
         *
         * @param of a specific List
         * @param item being rendered
         * @param index of the item
         * @param view being used to render that item
         * @return bounds for the given item
         */
        public fun itemBounds(of: List<T, *>, item: T, index: Int, view: View? = null): Rectangle

        /**
         * Returns the item at the given offset in a List.
         *
         * @param of a specific list
         * @param at the offset in the list
         * @return the item's index
         */
        public fun item(of: List<T, *>, at: Point): Int

        /**
         * Returns the minimum size of the given List including all items.
         *
         * @param of a specific List
         * @return the minimum size
         */
        public fun minimumSize(of: List<T, *>): Size
    }

    public val generator : ItemGenerator<T>
    public val positioner: ItemPositioner<T>
}

/**
 * Creates an [ItemGenerator] from the given lambda.
 *
 * @param block that will serve as the visualizer's [ItemGenerator.invoke].
 */
public inline fun <T> itemGenerator(crossinline block: (list: List<T, *>, item: T, index: Int, current: View?) -> View): ItemGenerator<T> = object: ItemGenerator<T> {
    override fun invoke(list: List<T, *>, item: T, index: Int, current: View?) = block(list, item, index, current)
}

/**
 * A visual component that renders an immutable list of items of type [T] using a [ListBehavior]. Items are obtained via
 * the [model] and selection is managed via the optional [selectionModel]. Large ("infinite") lists are supported
 * efficiently, since List recycles the Views generated to render its items.
 *
 * Note that this class assumes the given [ListModel] is immutable and will not automatically respond
 * to changes in the model. See [DynamicList] or [MutableList] if this behavior is desirable.
 *
 * List does not provide scrolling internally, so it should be embedded in a [ScrollPanel] or similar component if needed.
 *
 * @param model that holds the data for this List
 * @param itemVisualizer that maps [T] to [View] for each item in the List
 * @param selectionModel that manages the List's selection state
 * @param fitContent determines whether the List scales to fit its items' width or total height
 * @param scrollCache determining how many "hidden" items are rendered above and below the List's view-port. A value of 0 means
 * only visible items are rendered, but quick scrolling is more likely to show blank areas.
 *
 * @property model that holds the data for this List
 * @property itemVisualizer that maps [T] to [View] for each item in the List
 * @property selectionModel that manages the List's selection state
 * only visible items are rendered, but quick scrolling is more likely to show blank areas.
 */
@Suppress("LeakingThis")
public open class List<T, out M: ListModel<T>>(
        protected open val model         : M,
        public         val itemVisualizer: ItemVisualizer<T, IndexedItem>? = null,
        protected      val selectionModel: SelectionModel<Int>?            = null,
                           fitContent    : Set<Dimension>                  = setOf(Width, Height),
        private        val scrollCache   : Int                             = 0): View(ListRole()), ListLike, Selectable<Int> by ListSelectionManager(selectionModel, { model.size }) {

    private val selectionChanged_: SetObserver<SelectionModel<Int>, Int> = { _,removed,added ->
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

    private val fitContent: Set<Dimension> by dimensionSetProperty(fitContent)

    private   var itemGenerator : ItemGenerator <T>? = null
    private   var itemPositioner: ItemPositioner<T>? = null

    private   var maxRight           = 0.0
    private   var maxBottom          = 0.0
    private   var minVisiblePoint    = Origin
    private   var maxVisiblePoint    = Origin
    private   var handlingRectChange = false

    protected var firstVisibleItem: Int =  0
    protected var lastVisibleItem : Int = -1

    override val numItems: Int     get() = model.size
    public val isEmpty: Boolean get() = model.isEmpty

    /**
     * Notifies of changes to the List's selection.
     */
    public val selectionChanged: SetObservers<List<T, M>, Int> = SetPool()

    /**
     * Defines how the contents of an item should be aligned within it.
     */
    public var cellAlignment: (ConstraintDslContext.(Bounds) -> Unit)? by observable(null) { _,_ ->
        children.batch {
            (firstVisibleItem .. lastVisibleItem).forEach {
                update(this, it)
            }
        }
    }

    public fun contains(value: T): Boolean = value in model

    /**
     * Controls how the List behaves and how its items are rendered. A List will not render without
     * a behavior specified.
     */
    public var behavior: ListBehavior<T>? by behavior { _,new ->
        new?.also {
            itemGenerator  = it.generator
            itemPositioner = it.positioner

            children.clear()

            minVisiblePoint =  Origin
            maxVisiblePoint =  Origin
            firstVisibleItem =  0
            lastVisibleItem  = -1

            updateVisibleHeight()
        }
    }

    private var minimumSize = Size.Empty

    protected fun updateVisibleHeight() {
        val oldSize = minimumSize

        minimumSize = itemPositioner?.minimumSize(this) ?: minimumSize

        if (oldSize == minimumSize) {
            // FIXME: This reset logic could be handled better
            minVisiblePoint  =  Origin
            maxVisiblePoint  =  Origin
            firstVisibleItem =  0
            lastVisibleItem  = -1
        }

        handleDisplayRectEvent(Empty, displayRect)
    }

    public override var insets: Insets
        get(   ) = super.insets
        set(new) { super.insets = new }

    init {
        monitorsDisplayRect = true

        layout = simpleLayout { _,_,current,_,_ ->
            (firstVisibleItem .. lastVisibleItem).forEach {
                if (it < model.size) {
                    model[it].onSuccess { item ->
                        children.getOrNull(it % children.size)?.let { child ->
                            layout(child, item, it)
                        }
                    }
                }
            }

            preferredSize = fixed(Size(max(minimumSize.width, current.width), max(minimumSize.height, current.height)))
            idealSize
        }
    }

    /**
     * Returns the item at [index] if one exists, `null` otherwise.
     */
    public operator fun get(index: Int): Result<T> = model[index]

    public override var isFocusCycleRoot: Boolean = true

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

//    override fun preferredSize(min: Size, max: Size): Size = Size(super.preferredSize(min, max).width, minimumSize.height)

    override fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {
        itemPositioner?.let { positioner ->
            if (maxVisiblePoint.x > new.right && maxVisiblePoint.y > new.bottom && minVisiblePoint.x < new.x && minVisiblePoint.y < new.y) {
                return
            }

            val oldFirst = firstVisibleItem
            val oldLast  = lastVisibleItem

            var position = new.position

            firstVisibleItem = when {
                position == old.position && !old.empty -> firstVisibleItem
                else                                   -> max(0, findItem(position, firstVisibleItem) - scrollCache)
            }

            position = Point(new.right - 1, new.bottom - 1)

            lastVisibleItem = when {
                position == Point(old.right, old.bottom) && !old.empty -> lastVisibleItem
                else                                                   -> min(model.size - 1, findItem(position, lastVisibleItem) + scrollCache)
            }

            if (firstVisibleItem in 0 until model.size) {
                model[firstVisibleItem].onSuccess { minVisiblePoint = positioner.itemBounds(this, it, firstVisibleItem).position }
            }
            if (lastVisibleItem in 0 until model.size) {
                model[lastVisibleItem].onSuccess { maxVisiblePoint = positioner.itemBounds(this, it, lastVisibleItem).run { Point(right, bottom) } }
            }

            // reset size info
            maxRight  = 0.0
            maxBottom = 0.0

            handlingRectChange = true

            if (oldFirst > firstVisibleItem) {
                val end = min(oldFirst, lastVisibleItem)

                (firstVisibleItem .. end).forEach { insert(children, it) }
            }

            if (oldLast < lastVisibleItem) {
                val start = when {
                    oldLast > firstVisibleItem -> oldLast + 1
                    else                       -> firstVisibleItem
                }

                (start..lastVisibleItem).forEach { insert(children, it) }
            }

            // this updates "hashing" of items into the children list (using % of list size) since the children size has changed
            if (oldLast - oldFirst != lastVisibleItem - firstVisibleItem) {
                (firstVisibleItem .. lastVisibleItem).forEach {
                    update(children, it)
                }
            }

            handlingRectChange = false
        }
    }

    private fun maxWithNull(first: Double?, second: Double?): Double? = when {
        first != null && second != null -> max(first, second)
        first == null -> second
        else          -> first
    }

    protected fun layout(view: View, item: T, index: Int) {
        itemPositioner?.let {
            view.suggestBounds(it.itemBounds(this@List, item, index, view))

            maxRight    = max(maxRight,  view.bounds.right )
            maxBottom   = max(maxBottom, view.bounds.bottom)

            val mSize   = Size(max(minimumSize.width, maxRight), max(minimumSize.height, maxBottom))
            minimumSize = mSize
        }
    }

    protected fun update(children: kotlin.collections.MutableList<View>, index: Int) {
        if (index in firstVisibleItem .. lastVisibleItem) {
            itemGenerator?.let { uiGenerator ->
                model[index].onSuccess { item ->
                    val i = index % children.size

                    uiGenerator(this, item, index, children.getOrNull(i)).also { ui ->
                        children[i] = ui
                        layout(ui, item, index)

                        if (index + 1 < numItems - 1) {
                            ui.nextInAccessibleReadOrder = children[(index + 1) % children.size]
                        }
                    }
                }
            }
        }
    }

    private fun insert(children: kotlin.collections.MutableList<View>, index: Int) {
        itemGenerator?.let { uiGenerator ->
            model[index].onSuccess { item ->
                if (children.size <= lastVisibleItem - firstVisibleItem) {
                    uiGenerator(this, item, index).also { ui ->
                        children.addOrAppend(index, ui)
                        layout(ui, item, index)
                    }
                } else {
                    update(children, index)
                }
            }
        }
    }

    private fun findItem(point: Point, nearbyItem: Int) = min(model.size - 1, itemPositioner?.item(this, point) ?: nearbyItem)

    /**
     * Scrolls [item] into view if the List is within a [ScrollPanel].
     */
    public fun scrollTo(item: Int) {
        this[item].onSuccess {
            itemPositioner?.itemBounds(this, it, item)?.let { bounds ->
                scrollTo(bounds)
            }
        }
    }

    /**
     * Scrolls the last selected item into view if the List is within a [ScrollPanel].
     */
    public fun scrollToSelection() {
        lastSelection?.let { scrollTo(it) }
    }

    public companion object {
        public operator fun invoke(
            progression   : IntProgression,
            itemVisualizer: ItemVisualizer<Int, IndexedItem>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            scrollCache   : Int                  = 0): List<Int, ListModel<Int>> =
            List<Int, ListModel<Int>>(IntProgressionModel(progression), itemVisualizer, selectionModel, fitContent, scrollCache)

        public operator fun <T> invoke(
            values        : kotlin.collections.List<T>,
            itemVisualizer: ItemVisualizer<T, IndexedItem>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            scrollCache   : Int                  = 0): List<T, ListModel<T>> =
            List<T, ListModel<T>>(SimpleListModel(values), itemVisualizer, selectionModel, fitContent, scrollCache)

        public operator fun invoke(
            values        : kotlin.collections.List<View>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            scrollCache   : Int                  = 0): List<View, ListModel<View>> =
            List<View, ListModel<View>>(SimpleListModel(values), ViewVisualizer, selectionModel, fitContent, scrollCache)

        public operator fun <T, M: ListModel<T>>invoke(
            model         : M,
            itemGenerator : ItemVisualizer<T, IndexedItem>? = null,
            selectionModel: SelectionModel<Int>?            = null,
            fitContent    : Set<Dimension>                  = setOf(Width, Height),
            scrollCache   : Int                             = 0): List<T, M> =
            List(model, itemGenerator, selectionModel, fitContent, scrollCache)
    }
}

