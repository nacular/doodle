package io.nacular.doodle.controls.menu

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.core.Icon
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.Path
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl

/**
 * Created by Nicholas Eddy on 4/30/18.
 */
class Menu(
        private val menuSelectionManager: MenuSelectionManager,
                    popupFactory        : PopupFactory,
                    text                : String = "",
                    icon                : Icon<Button>? = null
): PushButton(text, icon), MenuItem {

    private val popup = popupFactory()

    override var parentMenu: MenuItem? = null

    override var menuSelected
        get(   ) = selected
        set(new) { selected = new }

    init {
        model.fired += {
            menuSelectionManager.selectedPath = getPath(popup)
        }

        model.selectedChanged += { _,old,new ->
            if (new) {
                menuSelectionManager.selectedPath = getPath(popup)
            }

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

    fun add(menu: Menu) = popup.add(menu)
}

private fun getPath(menuItem: MenuItem): Path<MenuItem> {
    var item = menuItem as MenuItem?
    val list = ArrayList<MenuItem>()

    while (item != null) {
        list.add(0, item)

        item = item.parentMenu
    }

    return Path(list)
}
