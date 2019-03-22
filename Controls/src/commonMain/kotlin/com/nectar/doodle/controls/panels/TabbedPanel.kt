package com.nectar.doodle.controls.panels

import com.nectar.doodle.controls.theme.TabbedPanelUI
import com.nectar.doodle.core.Container
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.utils.BoxOrientation
import com.nectar.doodle.utils.BoxOrientation.Top
import com.nectar.doodle.utils.ObservableList
import com.nectar.doodle.utils.ObservableProperty
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl

/**
 * Created by Nicholas Eddy on 4/2/18.
 */

class TabbedPanel<T>(orientation: BoxOrientation = Top, item: T, vararg remaining: T): View(), Iterable<T> {
    constructor(item: T, vararg remaining: T): this(Top, item, *remaining)

    private val wrapper = object: Container {
        override var insets
            get(   ) = this@TabbedPanel.insets
            set(new) { this@TabbedPanel.insets = new }
        override var layout: Layout?
            get(   ) = this@TabbedPanel.layout
            set(new) { this@TabbedPanel.layout = new }
        override var isFocusCycleRoot: Boolean
            get(   ) = this@TabbedPanel.isFocusCycleRoot
            set(new) { this@TabbedPanel.isFocusCycleRoot = new }
        override val children get() = this@TabbedPanel.children

        override fun setZIndex(of: View, to: Int) {
            this@TabbedPanel.setZIndex(of, to)
        }

        override fun zIndex(of: View) = this@TabbedPanel.zIndex(of)

        override fun ancestorOf(view: View) = this@TabbedPanel.ancestorOf(view)

        override fun child(at: Point) = this@TabbedPanel.child(at)
    }

    val selectionChanged  : PropertyObservers<TabbedPanel<T>, Int?>           by lazy { PropertyObserversImpl<TabbedPanel<T>, Int>           (this) }
    val orientationChanged: PropertyObservers<TabbedPanel<T>, BoxOrientation> by lazy { PropertyObserversImpl<TabbedPanel<T>, BoxOrientation>(this) }

    val numItems   : Int            get() = items.size
    var selection  : Int            by ObservableProperty(0   ,        { this }, selectionChanged   as PropertyObserversImpl)
    var orientation: BoxOrientation by ObservableProperty(orientation, { this }, orientationChanged as PropertyObserversImpl)

    var renderer: TabbedPanelUI<T>? = null
        set(new) {
            if (new == field) return

            children.batch {
                clear()

                field?.uninstall(this@TabbedPanel, wrapper)

                field = new?.apply { install(this@TabbedPanel, wrapper) }
            }
        }

    private val items = ObservableList<TabbedPanel<T>, T>(this)

    init {
        items += item
        items += remaining

        selectionChanged += { _,old,new ->
            renderer?.selectionChanged(this, wrapper, selectedItem, new!!, old?.let { get(it) }, old)
        }

        items.changed += { _,removed,added,moved ->
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
                    in (from + 1)..(to   - 1) -> selection - 1
                    in (to   + 1)..(from - 1) -> selection + 1
                    else                      -> selection
                }
            }
        }
    }

    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    val selectedItem: T get() = get(selection)!!

    fun indexOf(item: T) = items.indexOf(item)

    fun lastIndexOf(item: T) = items.lastIndexOf(item)

    fun isEmpty() = items.isEmpty()

    operator fun contains(item: T) = item in items

    operator fun get(index: Int) = items.getOrNull(index)

    override operator fun iterator() = items.iterator()

    fun add(item: T) = items.add(item)

    fun add(at: Int, item: T) = items.add(at, item)

    fun clear() { items.clear() }

    fun remove(item: T) = items.remove(item)

    fun remove(at: Int) = items.removeAt(at)

    operator fun set(at: Int, item: T) = items.set(at, item)
}
