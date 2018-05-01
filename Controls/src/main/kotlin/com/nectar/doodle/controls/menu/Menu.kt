package com.nectar.doodle.controls.menu

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.core.Icon
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl

/**
 * Created by Nicholas Eddy on 4/30/18.
 */
class Menu(
        private val menuSelectionManager: MenuSelectionManager,
                    popupFactory        : PopupFactory,
                    text                : String = "",
                    icon                : Icon<Button>? = null): Button(text, icon), MenuItem {

    private val popup = popupFactory()

    override var parentMenu: MenuItem? = null

    override var menuSelected
        get(   ) = selected
        set(new) { selected = new }

    init {
        model.selectedChanged += { _,old,new ->
            (selectedChanged as PropertyObserversImpl<MenuItem, Boolean>)(old, new)
        }

        popup.parentMenu = this

        popup.selectedChanged += { _,_,new ->
            setPopupVisible(new)
        }
    }

    override val subMenus = popup.subMenus

    override val selectedChanged: PropertyObservers<MenuItem, Boolean> by lazy { PropertyObserversImpl<MenuItem, Boolean>(this) }

    private fun setPopupVisible(visible: Boolean) {
        if (visible != popup.visible) {
            if (visible) {
                // FIXME: IMPLEMENT BASED ON MENU LOCATION ETC

                popup.show(this, Point(width + 1, 0.0))
            } else {
                popup.visible = false
            }
        }
    }

    fun add(aMenu: Menu) {
        popup.add(aMenu)
    }

    override fun click() {
//        menuSelectionManager.select(Utilities.getPath(popup))
    }
}