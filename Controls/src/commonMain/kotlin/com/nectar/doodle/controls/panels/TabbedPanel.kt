package com.nectar.doodle.controls.panels

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

abstract class TabbedPanelBehavior<T>: Behavior<TabbedPanel<T>> {
    val TabbedPanel<T>.children         get() = this._children
    var TabbedPanel<T>.insets           get() = this._insets;           set(new) { _insets           = new }
    var TabbedPanel<T>.layout           get() = this._layout;           set(new) { _layout           = new }
    var TabbedPanel<T>.isFocusCycleRoot get() = this._isFocusCycleRoot; set(new) { _isFocusCycleRoot = new }

    abstract fun selectionChanged(panel: TabbedPanel<T>, new: T, newIndex: Int, old: T?, oldIndex: Int?)

    abstract fun tabsChanged(panel: TabbedPanel<T>, removed: Map<Int, T>, added: Map<Int, T>, moved: Map<Int, Pair<Int, T>>)
}

class TabbedPanel<T>(orientation: BoxOrientation = Top, item: T, vararg remaining: T): View(), Iterable<T> {
    constructor(item: T, vararg remaining: T): this(Top, item, *remaining)

    val selectionChanged  : PropertyObservers<TabbedPanel<T>, Int?>           by lazy { PropertyObserversImpl<TabbedPanel<T>, Int?>          (this) }
    val orientationChanged: PropertyObservers<TabbedPanel<T>, BoxOrientation> by lazy { PropertyObserversImpl<TabbedPanel<T>, BoxOrientation>(this) }

    val numItems   : Int            get() = items.size
    var selection  : Int            by ObservableProperty(0   ,        { this }, selectionChanged   as PropertyObserversImpl)
    var orientation: BoxOrientation by ObservableProperty(orientation, { this }, orientationChanged as PropertyObserversImpl)

    var behavior: TabbedPanelBehavior<T>? = null
        set(new) {
            if (new == field) return

            children.batch {
                clear()

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
            behavior?.selectionChanged(this, selectedItem, new!!, old?.let { get(it) }, old)
        }

        items.changed += { _,removed,added,moved ->

            behavior?.tabsChanged(this, removed, added, moved)

            removed.forEach { (index, _) ->
                selection = when {
                    selection >  index -> selection - 1
                    selection == index -> {
                        selection = -1; return@forEach
                    }
                    else -> selection
                }
            }

            added.forEach { (index, _) ->
                selection = if (selection >= index) selection + 1 else selection
            }

            moved.forEach { (from, pair) ->
                val to = pair.first

                selection = when (selection) {
                    in (from + 1) until to   -> selection - 1
                    in (to   + 1) until from -> selection + 1
                    from                     -> to
                    else                     -> selection
                }
            }
        }
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override operator fun iterator() = items.iterator()

    val selectedItem: T get() = get(selection)!!

    fun indexOf(item: T) = items.indexOf(item)

    fun lastIndexOf(item: T) = items.lastIndexOf(item)

    fun isEmpty() = items.isEmpty()

    operator fun contains(item: T) = item in items

    operator fun get(index: Int) = items.getOrNull(index)

    fun add(item: T) = items.add(item)

    fun add(at: Int, item: T) = items.add(at, item)

    fun clear() { items.clear() }

    fun remove(item: T) = items.remove(item)

    fun remove(at: Int) = items.removeAt(at)

    fun move(item: T, to: Int) = items.batch {
        val index = indexOf(item)

        if (index >= 0 && index != to) {
            addOrAppend(to, removeAt(index))
        }
    }

    operator fun set(at: Int, item: T) = items.set(at, item)
}
