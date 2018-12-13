package com.nectar.doodle.controls.panels

import com.nectar.doodle.controls.theme.TabbedPanelUI
import com.nectar.doodle.core.View
import com.nectar.doodle.utils.BoxOrientation
import com.nectar.doodle.utils.BoxOrientation.Top
import com.nectar.doodle.utils.ObservableList
import com.nectar.doodle.utils.ObservableProperty
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl

/**
 * Created by Nicholas Eddy on 4/2/18.
 */
private class Tab(val view: View, var display: View?)

class TabbedPanel(orientation: BoxOrientation = Top): View() {

    val selectionChanged  : PropertyObservers<TabbedPanel, Int?>           by lazy { PropertyObserversImpl<TabbedPanel, Int>           (this) }
    val orientationChanged: PropertyObservers<TabbedPanel, BoxOrientation> by lazy { PropertyObserversImpl<TabbedPanel, BoxOrientation>(this) }

    val numTabs    : Int            get() = tabs.size
    var selection  : Int?           by ObservableProperty(null,        { this }, selectionChanged   as PropertyObserversImpl)
    var orientation: BoxOrientation by ObservableProperty(orientation, { this }, orientationChanged as PropertyObserversImpl)

    var renderer: TabbedPanelUI? = null
        set(new) {
            if (new == field) return

            field = new?.also {
                itemUIGenerator = it.tabRenderer

                children.clear()

//                layout = InternalLayout(it.positioner)
            }
        }

    private val tabs            = ObservableList<TabbedPanel, Tab>(this)
    private var itemUIGenerator = null as TabbedPanelUI.TabRenderer?

    init {
        tabs.changed += { _,removed,added,moved ->
            removed.forEach { (index, _) ->
                selection = selection?.let {
                    when {
                        it > index  -> it - 1
                        it == index -> {
                            selection = -1; return@forEach
                        }
                        else -> selection
                    }
                }
            }

            added.forEach { (index, _) ->
                selection = selection?.let { if (it >= index) it + 1 else selection }
            }

            moved.forEach { (from, pair) ->
                val to = pair.first

                selection = selection?.let {
                    when (it) {
                        in (from + 1)..(to - 1) -> it - 1
                        in (to + 1)..(from - 1) -> it + 1
                        else                    -> it
                    }
                }
            }
        }
    }

    val selectedTab: View? get() = selection?.let { tabs.getOrNull(it)?.view }

    fun add(tab: View, display: View? = null) = insert(tab, display)

    fun insert(tab: View, display: View? = null, index: Int = numTabs) {
        tabs.add(index, Tab(tab, display))
    }

    fun remove(tab: View) = indexOf(tab)?.let { remove(it) }

    fun remove(from: Int) {
        tabs.removeAt(from)
    }

    fun clear() {
        tabs.clear()
    }

    fun tab(at: Int): View? = tabs.getOrNull(at)?.view

    fun display(at: Int): View? = tabs.getOrNull(at)?.display

    fun set(display: View, at: Int) {
        tabs.getOrNull(at)?.let { it.display = display }

        // TODO: Inform renderer?
    }

    fun indexOf(tab: View): Int? = when (val index = tabs.indexOfFirst { it.view == tab }) {
        -1   -> null
        else -> index
    }
}
