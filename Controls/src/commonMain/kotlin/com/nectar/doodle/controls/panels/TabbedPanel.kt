package com.nectar.doodle.controls.panels

import com.nectar.doodle.controls.ItemVisualizer
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.utils.BoxOrientation
import com.nectar.doodle.utils.BoxOrientation.Top
import com.nectar.doodle.utils.ObservableList
import com.nectar.doodle.utils.ObservableProperty
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import com.nectar.doodle.utils.addOrAppend

/**
 * Provides presentation and behavior customization for [TabbedPanel].
 */
abstract class TabbedPanelBehavior<T>: Behavior<TabbedPanel<T>> {
    val TabbedPanel<T>.children         get() = this._children
    var TabbedPanel<T>.insets           get() = this._insets;           set(new) { _insets           = new }
    var TabbedPanel<T>.layout           get() = this._layout;           set(new) { _layout           = new }
    var TabbedPanel<T>.isFocusCycleRoot get() = this._isFocusCycleRoot; set(new) { _isFocusCycleRoot = new }

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
    abstract fun selectionChanged(panel: TabbedPanel<T>, new: T?, newIndex: Int?, old: T?, oldIndex: Int?)

    /**
     * Called whenever the items within the TabbedPanel change.
     *
     * @param panel with change
     * @param removed items
     * @param added items
     * @param moved items (changed index)
     */
    abstract fun itemsChanged(panel: TabbedPanel<T>, removed: Map<Int, T>, added: Map<Int, T>, moved: Map<Int, Pair<Int, T>>)
}

/**
 * A container that manages a set of tabs, each representing one of the items it holds. This container is
 * intended to display a single item at a time based on which item is selected. This class delegates
 * all rendering an configuration to its [TabbedPanelBehavior], which allows a high degree of flexibility
 * regarding display and behavior.
 *
 * @constructor
 * @param orientation of the tab container
 * @param visualizer to display each item
 * @param item the first item in the list
 * @param remaining items in the lest
 */
class TabbedPanel<T>(orientation: BoxOrientation = Top, val visualizer: ItemVisualizer<T>, item: T, vararg remaining: T): View(), Iterable<T> {
    /**
     * Creates a TabbedPanel with [orientation] == [Top].
     *
     * @param visualizer to display each item
     * @param item the first item in the list
     * @param remaining items in the lest
     */
    constructor(visualizer: ItemVisualizer<T>, item: T, vararg remaining: T): this(Top, visualizer, item, *remaining)

    /** Notifies of changes to [selection]. */
    val selectionChanged: PropertyObservers<TabbedPanel<T>, Int?> by lazy { PropertyObserversImpl<TabbedPanel<T>, Int?>(this) }

    /** Notifies of changes to [orientation]. */
    val orientationChanged: PropertyObservers<TabbedPanel<T>, BoxOrientation> by lazy { PropertyObserversImpl<TabbedPanel<T>, BoxOrientation>(this) }

    /** The number of items (tabs) in the panel. */
    val numItems: Int get() = items.size

    // FIXME: Selection can be null if empty
    /** The currently selected item/tab. Defaults to `0`. */
    var selection: Int? by ObservableProperty(0, { this }, selectionChanged as PropertyObserversImpl)

    /** The location of the tabs. */
    var orientation: BoxOrientation by ObservableProperty(orientation, { this }, orientationChanged as PropertyObserversImpl)

    /** Component responsible for controlling the presentation and behavior of the panel. */
    var behavior: TabbedPanelBehavior<T>? = null
        set(new) {
            if (new == field) return

            children.batch {
                clear()

                selection = 0

                field?.uninstall(this@TabbedPanel)

                field = new?.apply { install(this@TabbedPanel) }
            }
        }

    // Expose container APIs for behavior
    internal val _children         get() = children
    internal var _insets           get() = insets; set(new) { insets = new }
    internal var _layout           get() = layout; set(new) { layout = new }
    internal var _isFocusCycleRoot get() = isFocusCycleRoot; set(new) { isFocusCycleRoot = new }

    private val items = ObservableList<T>()

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

            moved.forEach { (from, pair) ->
                val to = pair.first

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

    override operator fun iterator() = items.iterator()

    /** The currently selected item. */
    val selectedItem: T? get() = selection?.let { get(it) }

    /**
     * @param item to search for
     * @return index of the given item.
     */
    fun indexOf(item: T) = items.indexOf(item)

    /**
     * @param item to search for
     * @return last index of the given item.
     */
    fun lastIndexOf(item: T) = items.lastIndexOf(item)

    /** `true` if the item list is empty */
    fun isEmpty() = items.isEmpty()

    /**
     * @param item to check
     * @return `true` if item in panel
     */
    operator fun contains(item: T) = item in items

    /**
     * @param index of item
     * @return the item at [index] or `null`
     */
    operator fun get(index: Int): T? = items.getOrNull(index)

    /**
     * Adds an item to the panel/
     * @param item to add
     */
    fun add(item: T) = items.add(item)

    /**
     * Adds an item to the panel at a particular index.
     * @param item to add
     */
    fun add(at: Int, item: T) = items.add(at, item)

    /** Removes all items from the panel */
    fun clear() { items.clear() }

    /**
     * Removes an item from the panel.
     * @param item to remove
     */
    fun remove(item: T) = items.remove(item)

    /**
     * Removes the item at an index from the panel.
     * @param at the index
     */
    fun remove(at: Int) = items.removeAt(at)

    /**
     * Moves an item from to the given index.
     * @param item to move
     * @param to this index
     */
    fun move(item: T, to: Int) = items.batch {
        val index = indexOf(item)

        if (index >= 0 && index != to) {
            addOrAppend(to, removeAt(index))
        }
    }

    /**
     * Changes the item at a given index.
     * @param at this index
     * @param item to replace it with
     */
    operator fun set(at: Int, item: T) = items.set(at, item)
}
