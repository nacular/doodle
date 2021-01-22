package io.nacular.doodle.controls.panels

import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ViewVisualizer
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.BoxOrientation
import io.nacular.doodle.utils.BoxOrientation.Top
import io.nacular.doodle.utils.ListObserver
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.observable

/**
 * Provides presentation and behavior customization for [TabbedPanel].
 */
public abstract class TabbedPanelBehavior<T>: Behavior<TabbedPanel<T>> {
    public val TabbedPanel<T>.children        : ObservableList<View> get() = _children
    public var TabbedPanel<T>.insets          : Insets               get() = _insets;           set(new) { _insets           = new }
    public var TabbedPanel<T>.layout          : Layout?              get() = _layout;           set(new) { _layout           = new }
    public var TabbedPanel<T>.isFocusCycleRoot: Boolean              get() = _isFocusCycleRoot; set(new) { _isFocusCycleRoot = new }

    public inline operator fun TabbedPanel<T>.plusAssign (view: View           ): Unit = children.plusAssign (view )
    public inline operator fun TabbedPanel<T>.minusAssign(view: View           ): Unit = children.minusAssign(view )
    public inline operator fun TabbedPanel<T>.plusAssign (views: Iterable<View>): Unit = children.plusAssign (views)
    public inline operator fun TabbedPanel<T>.minusAssign(views: Iterable<View>): Unit = children.minusAssign(views)

    /**
     * Called whenever the TabbedPanel's selection changes. This is an explicit API to ensure that
     * behaviors receive the notification before listeners to [TabbedPanel.selectionChanged].
     *
     * @param panel with change
     * @param new item selected
     * @param newIndex of the selected item
     * @param old item that was selected
     * @param oldIndex of previously selected item
     */
    public abstract fun selectionChanged(panel: TabbedPanel<T>, new: T?, newIndex: Int?, old: T?, oldIndex: Int?)

    /**
     * Called whenever the items within the TabbedPanel change.
     *
     * @param panel with change
     * @param removed items
     * @param added items
     * @param moved items (changed index)
     */
    public abstract fun itemsChanged(panel: TabbedPanel<T>, removed: Map<Int, T>, added: Map<Int, T>, moved: Map<Int, Pair<Int, T>>)
}

/**
 * A container that manages a set of tabs, each representing one of the items it holds. This container is
 * intended to display a single item at a time based on which is selected. This class delegates
 * all rendering an configuration to its [TabbedPanelBehavior], which allows a high degree of flexibility
 * regarding display and behavior.
 *
 * @constructor
 * @param orientation of the tab container
 * @param visualizer to display each item
 * @param tabVisualizer to display the tab for each item
 * @param item the first item in the list
 * @param remaining items in the lest
 */
public class TabbedPanel<T>(
                   orientation  : BoxOrientation = Top,
        public val visualizer   : ItemVisualizer<T, Any>,
        public val tabVisualizer: ItemVisualizer<T, Any>,
                   item         : T,
        vararg     remaining    : T
): View(), Iterable<T> {
    /**
     * Creates a TabbedPanel with [orientation] == [Top].
     *
     * @param visualizer to display each item
     * @param tabVisualizer to display the tab for each item
     * @param item the first item in the list
     * @param remaining items in the lest
     */
    public constructor(
                   visualizer   : ItemVisualizer<T, Any>,
                   tabVisualizer: ItemVisualizer<T, Any>,
                   item         : T,
            vararg remaining    : T
    ): this(Top, visualizer, tabVisualizer, item, *remaining)

    private val items = ObservableList<T>()

    /** Notifies of changes to items. */
    public val itemsChanged: Pool<ListObserver<T>> = items.changed

    /** Notifies of changes to [selection]. */
    public val selectionChanged: PropertyObservers<TabbedPanel<T>, Int?> by lazy { PropertyObserversImpl(this) }

    /** Notifies of changes to [orientation]. */
    public val orientationChanged: PropertyObservers<TabbedPanel<T>, BoxOrientation> by lazy { PropertyObserversImpl(this) }

    /** The number of items (tabs) in the panel. */
    public val numItems: Int get() = items.size

    /** The currently selected item/tab. Defaults to `0`. */
    public var selection: Int? by observable(0, selectionChanged as PropertyObserversImpl)

    /** The location of the tabs. */
    public var orientation: BoxOrientation by observable(orientation, orientationChanged as PropertyObserversImpl)

    /** Component responsible for controlling the presentation and behavior of the panel. */
    public var behavior: TabbedPanelBehavior<T>? by behavior { _,_ ->
        children.batch {
            clear()
        }
    }

    // Expose container APIs for behavior
    internal val _children         get() = children
    internal var _insets           get() = insets; set(new) { insets = new }
    internal var _layout           get() = layout; set(new) { layout = new }
    internal var _isFocusCycleRoot get() = isFocusCycleRoot; set(new) { isFocusCycleRoot = new }

    init {
        items += item
        items += remaining

        selectionChanged += { _,old,new ->
            behavior?.selectionChanged(this, selectedItem, new, old?.let { get(it) }, old)
        }

        items.changed += { _,removed,added,moved ->

            behavior?.itemsChanged(this, removed, added, moved)

            removed.forEach { (index, _) ->
                selection = when {
                    selection ?: 0 > index -> selection?.let { it - 1 }
                    selection == index     -> {
                        selection = -1; return@forEach
                    }
                    else -> selection
                }
            }

            added.forEach { (index, _) ->
                selection = selection?.let { if (it >= index) it + 1 else it }
            }

            moved.forEach { (new, old) ->
                val to   = new
                val from = old.first

                selection = when (val s = selection) {
                    null                     -> s
                    in (from + 1) until to   -> s - 1
                    in (to   + 1) until from -> s + 1
                    from                     -> to
                    else                     -> s
                }
            }
        }
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override operator fun iterator(): Iterator<T> = items.iterator()

    /** The currently selected item. */
    public val selectedItem: T? get() = selection?.let { get(it) }

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
     * @return `true` if item in panel
     */
    public operator fun contains(item: T): Boolean = item in items

    /**
     * @param index of item
     * @return the item at [index] or `null`
     */
    public operator fun get(index: Int): T? = items.getOrNull(index)

    /**
     * Adds an item to the panel/
     * @param item to add
     */
    public fun add(item: T): Boolean = items.add(item)

    /**
     * Adds an item to the panel at a particular index.
     * @param item to add
     */
    public fun add(at: Int, item: T): Unit = items.add(at, item)

    /** Removes all items from the panel */
    public fun clear() { items.clear() }

    /**
     * Removes an item from the panel.
     * @param item to remove
     */
    public fun remove(item: T): Boolean = items.remove(item)

    /**
     * Removes the item at an index from the panel.
     * @param at the index
     */
    public fun remove(at: Int): T = items.removeAt(at)

    /**
     * Moves an item from to the given index.
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
        public operator fun invoke(
                       orientation  : BoxOrientation = Top,
                       tabVisualizer: ItemVisualizer<View, Any>,
                       item         : View,
                vararg remaining    : View): TabbedPanel<View> = TabbedPanel(
                    orientation   = orientation,
                    visualizer    = ViewVisualizer,
                    tabVisualizer = tabVisualizer,
                    item          = item,
                    remaining     = remaining
            )
    }
}
