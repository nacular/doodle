package com.nectar.doodle.controls.panels

import com.nectar.doodle.controls.theme.TabbedPanelUI
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.utils.BoxOrientation
import com.nectar.doodle.utils.BoxOrientation.Top
import com.nectar.doodle.utils.ObservableList
import com.nectar.doodle.utils.ObservableProperty
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl

/**
 * Created by Nicholas Eddy on 4/2/18.
 */
private class Tab(val gizmo: Gizmo, var display: Gizmo?)

class TabbedPanel(orientation: BoxOrientation = Top): Gizmo() {

    val onSelectionChanged  : PropertyObservers<TabbedPanel, Int>            by lazy { PropertyObserversImpl<TabbedPanel, Int>           (this) }
    val onOrientationChanged: PropertyObservers<TabbedPanel, BoxOrientation> by lazy { PropertyObserversImpl<TabbedPanel, BoxOrientation>(this) }

    val numTabs    : Int            get() = tabs.size
    var selection  : Int            by ObservableProperty(-1,          { this }, onSelectionChanged   as PropertyObserversImpl)
    var orientation: BoxOrientation by ObservableProperty(orientation, { this }, onOrientationChanged as PropertyObserversImpl)

    var renderer: TabbedPanelUI? = null
        set(new) {
            if (new == field) return

            field = new?.also {
                itemUIGenerator = it.uiGenerator

                children.clear()

//                layout = InternalLayout(it.positioner)
            }
        }

    private val tabs = ObservableList<TabbedPanel, Tab>(this)
    private var itemUIGenerator = null as TabbedPanelUI.ItemUIGenerator?

    init {
        tabs.onChange += { _,removed,added,moved ->
            removed.forEach { (index, _) ->
                when {
                    selection  > index -> --selection
                    selection == index -> { selection = -1; return@forEach }
                }
            }

            added.forEach { (index, _) ->
                if (selection >= index) ++selection
            }

            moved.forEach { (from, pair) ->
                val to = pair.first

                when (selection) {
                    in (from + 1) .. (to   - 1) -> --selection
                    in (to   + 1) .. (from - 1) -> ++selection
                }
            }
        }
    }

    val selectedTab: Gizmo? get() = tabs.getOrNull(selection)?.gizmo


    fun add(tab: Gizmo, display: Gizmo? = null) = insert(tab, display)

    fun insert(tab: Gizmo, display: Gizmo? = null, index: Int = numTabs) {
        tabs.add(index, Tab(tab, display))
    }

    fun remove(tab: Gizmo) = remove(tabs.indexOfFirst { it.gizmo == tab })

    fun remove(from: Int) {
        tabs.removeAt(from)
    }

    fun clear() {
        tabs.clear()
    }

    fun tab(at: Int): Gizmo? = tabs.getOrNull(at)?.gizmo

    fun set(display: Gizmo, at: Int) {
        tabs.getOrNull(at)?.let { it.display = display }

        // TODO: Inform renderer?
    }

    fun display(at: Int): Gizmo? = tabs.getOrNull(at)?.display

    fun indexOf(tab: Gizmo): Int = tabs.indexOfFirst { it.gizmo == tab }

}
