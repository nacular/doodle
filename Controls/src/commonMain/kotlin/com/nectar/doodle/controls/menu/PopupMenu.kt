package com.nectar.doodle.controls.menu

import com.nectar.doodle.controls.MutableListModel
import com.nectar.doodle.controls.SingleItemSelectionModel
import com.nectar.doodle.controls.list.MutableList
import com.nectar.doodle.controls.mutableListModelOf
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl


/**
 * Created by Nicholas Eddy on 4/30/18.
 */
//class PopupMenu(private val display: Display): View(), MenuItem {
//
//    override val subMenus     = mutableListOf<MenuItem>()
//    override var parentMenu   = null as MenuItem?
//    override var menuSelected = false
//        set(new) {
//            if (field != new) {
//                (selectedChanged as PropertyObserversImpl)(old = field, new = new)
//
//                field = new
//            }
//        }
//
//    override val selectedChanged: PropertyObservers<MenuItem, Boolean> by lazy { PropertyObserversImpl<MenuItem, Boolean>(this) }
//
//    init {
//        visible = false
//        layout  = ListLayout()
//    }
//
//    fun add(menu: Menu) {
//        if (menu.parentMenu !== this) {
//            subMenus += menu
//
//            menu.parentMenu = this
//
//            super.children += menu
//        }
//    }
//
//    fun show(owner: View, at: Point) {
//        visible = true
//
//        // FIXME: IMPLEMENT
//        position = owner.toAbsolute(at)
//
//        display.children += this
//    }
//}


class PopupMenu(display: Display): MutableList<MenuItem, MutableListModel<MenuItem>>(mutableListModelOf(), selectionModel = SingleItemSelectionModel()), MenuItem {

    // View has an internal display property so have to create new one
    private  val _display       = display
    override val subMenus get() = model.iterator()
    override var parentMenu     = null as MenuItem?
    override var menuSelected   = false
        set(new) {
            if (field != new) {
                (selectedChanged as PropertyObserversImpl)(old = field, new = new)

                field = new
            }
        }

    override val selectedChanged: PropertyObservers<MenuItem, Boolean> by lazy { PropertyObserversImpl<MenuItem, Boolean>(this) }

    init {
        visible = false
    }

    fun add(menu: Menu) {
        if (menu.parentMenu !== this) {
            size = Size(100.0, 4.0) // FIXME: remove

            model.add(menu)

            menu.parentMenu = this
        }
    }

    fun show(owner: View, at: Point) {
        visible = true

        // FIXME: IMPLEMENT
        position = owner.toAbsolute(at)

        _display.children += this
    }
}
