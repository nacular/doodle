package com.nectar.doodle.controls.menu

import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.layout.ListLayout
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl


/**
 * Created by Nicholas Eddy on 4/30/18.
 */
class PopupMenu(private val display: Display): Gizmo(), MenuItem {

    override val subMenus     = mutableListOf<MenuItem>()
    override var parentMenu   = null as MenuItem?
    override var menuSelected = false
        set(new) {
            if (field != new) {
                (selectedChanged as PropertyObserversImpl)(old = field, new = new)

                field = new
            }
        }

    override val selectedChanged: PropertyObservers<MenuItem, Boolean> by lazy { PropertyObserversImpl<MenuItem, Boolean>(this) }

    init {
        visible = false
        layout  = ListLayout()
    }

    fun add(menu: Menu) {
        if (menu.parentMenu !== this) {
            subMenus += menu

            menu.parentMenu = this

            super.children += menu
        }
    }

    fun show(owner: Gizmo, at: Point) {
        visible = true

        // FIXME: IMPLEMENT
        position = owner.toAbsolute(at)

        display.children += this
    }
}
