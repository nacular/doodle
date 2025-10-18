package io.nacular.doodle.controls

import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.ListObservers
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.ObservableSet
import io.nacular.doodle.utils.SetObserver
import io.nacular.doodle.utils.SetObservers
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.diff.Differences
import io.nacular.doodle.utils.diff.Insert

/**
 * Provides presentation and behavior customization for [Accordion].
 */
public abstract class AccordionBehavior<T>: Behavior<Accordion<T>> {
    public val Accordion<T>.children        : ObservableList<View> get() = _children
    public var Accordion<T>.insets          : Insets               get() = _insets;           set(new) { _insets           = new }
    public var Accordion<T>.layout          : Layout?              get() = _layout;           set(new) { _layout           = new }
    public var Accordion<T>.isFocusCycleRoot: Boolean              get() = _isFocusCycleRoot; set(new) { _isFocusCycleRoot = new }

    public operator fun Accordion<T>.plusAssign (view: View           ): Unit = children.plusAssign (view )
    public operator fun Accordion<T>.minusAssign(view: View           ): Unit = children.minusAssign(view )
    public operator fun Accordion<T>.plusAssign (views: Iterable<View>): Unit = children.plusAssign (views)
    public operator fun Accordion<T>.minusAssign(views: Iterable<View>): Unit = children.minusAssign(views)

    /**
     * Called whenever an item is expanded/collapsed within an Accordion.
     *
     * @param accordion with change
     * @param index     that changed
     * @param expanded  indicates whether index is expanded
     */
    public abstract fun expansionChanged(accordion: Accordion<T>, index: Int, expanded: Boolean)

    /**
     * Called whenever the items within the Accordion change.
     *
     * @param accordion with change
     * @param differences that occurred
     */
    public abstract fun itemsChanged(accordion: Accordion<T>, differences: Differences<T>)
}

public interface ExpansionModel: Iterable<Int> {
    public val first  : Int?
    public val last   : Int?
    public val size   : Int
    public val isEmpty: Boolean

    public fun add        (index  : Int            ): Boolean
    public fun clear      (                        )
    public fun addAll     (indexes: Collection<Int>): Boolean
    public fun remove     (index  : Int            ): Boolean
    public fun contains   (index  : Int            ): Boolean
    public fun removeAll  (indexes: Collection<Int>): Boolean
    public fun retainAll  (indexes: Collection<Int>): Boolean
    public fun replaceAll (indexes: Collection<Int>): Boolean
    public fun containsAll(indexes: Collection<Int>): Boolean
    public fun toggle     (indexes: Collection<Int>): Boolean

    public val changed: SetObservers<ExpansionModel, Int>
}

public open class MultiExpansionModel: ExpansionModel {

    private val set = LinkedHashSet<Int>()
    protected val observableSet: ObservableSet<Int> = ObservableSet(set)

    init {
        observableSet.changed += { set, removed, added ->
            (changed as SetPool).forEach {
                it(this, removed, added)
            }
        }
    }

    override val size  : Int  get() = observableSet.size
    override val first : Int? get() = observableSet.firstOrNull()
    override val last  : Int? get() = observableSet.lastOrNull ()

    override val isEmpty: Boolean get() = observableSet.isEmpty()

    override fun add        (index  : Int            ): Boolean              = observableSet.add        (index )
    override fun clear      (                        ): Unit                 = observableSet.clear      (      )
    override fun addAll     (indexes: Collection<Int>): Boolean              = observableSet.batch { removeAll(indexes); addAll(indexes) }
    override fun remove     (index  : Int            ): Boolean              = observableSet.remove     (index  )
    override fun iterator   (                        ): MutableIterator<Int> = observableSet.iterator   (       )
    override fun contains   (index  : Int            ): Boolean              = observableSet.contains   (index  )
    override fun removeAll  (indexes: Collection<Int>): Boolean              = observableSet.removeAll  (indexes)
    override fun retainAll  (indexes: Collection<Int>): Boolean              = observableSet.retainAll  (indexes)
    override fun containsAll(indexes: Collection<Int>): Boolean              = observableSet.containsAll(indexes)
    override fun replaceAll (indexes: Collection<Int>): Boolean              = observableSet.replaceAll (indexes)
    override fun toggle     (indexes: Collection<Int>): Boolean              {
        var result = false

        observableSet.batch {
            indexes.forEach {
                result = remove(it)
                if (!result) {
                    result = add(it)
                }
            }
        }

        return result
    }

    override val changed: SetObservers<ExpansionModel, Int> = SetPool()
}

public class SingleItemExpansionModel: MultiExpansionModel() {
    override fun add(index: Int): Boolean {
        var result = false

        observableSet.batch {
            clear()
            result = add(index)
        }

        return result
    }

    override fun addAll(indexes: Collection<Int>): Boolean {
        if (observableSet.firstOrNull() in indexes) {
            return false
        }

        return indexes.lastOrNull()?.let { add(it) } ?: false
    }

    override fun replaceAll(indexes: Collection<Int>): Boolean = indexes.lastOrNull()?.let { super.replaceAll(listOf(it)) } ?: false

    override fun toggle(indexes: Collection<Int>): Boolean {
        var result = false

        observableSet.batch {
            indexes.forEach {
                result = remove(it)
                if (!result) {
                    clear()
                    result = add(it)
                }
            }
        }

        return result
    }
}

/**
 * Represents the state of an item within an [Accordion] that is passed to its [ItemVisualizer].
 *
 * @property index of the item
 * @property expanded is `true` for expanded items
 */
public class AccordionItem(public val index: Int, public val expanded: Boolean)

/**
 * A container that manages a list of items, each within a section that may be expanded or collapsed. This class
 * delegates all rendering and configuration to its [AccordionBehavior], which allows a high degree of flexibility
 * regarding display and behavior.
 *
 * @constructor
 * @param visualizer to display each item
 * @param sectionVisualizer to display the heading for each item
 * @param item the first item in the list
 * @param remaining items in the lest
 */
public class Accordion<T>(
    public  val visualizer       : ItemVisualizer<T, AccordionItem>,
    public  val sectionVisualizer: ItemVisualizer<T, AccordionItem>,
    private val expansionModel   : ExpansionModel = SingleItemExpansionModel(),
                item             : T,
    vararg      remaining        : T
): View(), Iterable<T> {

    private val expansionChanged_: SetObserver<ExpansionModel, Int> = { _,removed,added ->
        behavior?.let {
            removed.forEach { index -> it.expansionChanged(this, index, false) }
            added.forEach   { index -> it.expansionChanged(this, index, true ) }
        }

        (expansionChanged as SetPool).forEach {
            it(this, removed, added)
        }
    }

    private val items = ObservableList<T>()

    /** Notifies of changes to items. */
    public val itemsChanged: ListObservers<Accordion<T>, T> = SetPool()

    /** The number of items in the accordion. */
    public val numItems: Int get() = items.size

    /** Component responsible for controlling the presentation and behavior of the accordion. */
    public var behavior: AccordionBehavior<T>? by behavior { _, _ ->
        children.clear()
    }

    /**
     * Notifies of expansion changes to the Accordion.
     */
    public val expansionChanged: SetObservers<Accordion<T>, Int> = SetPool()

    // Expose container APIs for behavior
    internal val _children         get() = children
    internal var _insets           get() = insets; set(new) { insets = new }
    internal var _layout           get() = layout; set(new) { layout = new }
    internal var _isFocusCycleRoot get() = isFocusCycleRoot; set(new) { isFocusCycleRoot = new }

    init {
        items += item
        items += remaining

        expansionModel.changed += expansionChanged_

        items.changed += { _,diffs ->
            behavior?.itemsChanged(this, diffs)

            diffs.computeMoves().forEach { diff ->
                if (diff is Insert) {
                    diff.items.forEachIndexed { index, item ->
                        diff.origin(item)?.let {
                            expansionModel.remove(it)
                            expansionModel.add(index)
                        }
                    }
                }
            }

            (itemsChanged as SetPool).forEach {
                it(this, diffs)
            }
        }
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override operator fun iterator(): Iterator<T> = items.iterator()

    /** The currently expanded item. */
    public val expandedItems: List<Int> get() = expansionModel.toList()

    public fun expand(index: Int) { expansionModel.add(index) }

    public fun toggle(index: Int) { expansionModel.toggle(listOf(index)) }

    public fun collapse(index: Int) { expansionModel.remove(index) }

    /**
     * @param item to search for
     * @return index of the given item.
     */
    public fun indexOf(item: T): Int = items.indexOf(item)

    /**
     * @param item to search for
     * @return last index of the given item.
     */
    public fun lastIndexOf(item: T): Int = items.lastIndexOf(item)

    /** `true` if the item list is empty */
    public fun isEmpty(): Boolean = items.isEmpty()

    /**
     * @param item to check
     * @return `true` if item in accordion
     */
    public operator fun contains(item: T): Boolean = item in items

    /**
     * @param index of item
     * @return the item at [index] or `null`
     */
    public operator fun get(index: Int): T? = items.getOrNull(index)

    /**
     * Adds an item to the accordion
     * @param item to add
     */
    public fun add(item: T): Boolean = items.add(item)

    /**
     * Adds an item to the accordion at a particular index.
     * @param item to add
     */
    public fun add(at: Int, item: T): Unit = items.add(at, item)

    /** Removes all items from the accordion */
    public fun clear() { items.clear() }

    /**
     * Removes an item from the accordion.
     * @param item to remove
     */
    public fun remove(item: T): Boolean = items.remove(item)

    /**
     * Removes the item at an index from the accordion.
     * @param at the index
     */
    public fun remove(at: Int): T = items.removeAt(at)

    /**
     * Moves an item to the given index.
     * @param item to move
     * @param to this index
     */
    public fun move(item: T, to: Int): Boolean = items.move(item, to)

    /**
     * Changes the item at a given index.
     * @param at this index
     * @param item to replace it with
     */
    public operator fun set(at: Int, item: T): T = items.set(at, item)

    public companion object {
        public operator fun <T> invoke(
                   visualizer       : ItemVisualizer<T, Any>,
                   sectionVisualizer: ItemVisualizer<T, Any>,
                   item             : T,
            vararg remaining        : T
        ): Accordion<T> = Accordion(visualizer, sectionVisualizer, item = item, remaining = remaining)
    }
}
