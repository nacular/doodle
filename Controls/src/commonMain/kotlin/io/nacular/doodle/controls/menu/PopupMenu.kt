package io.nacular.doodle.controls.menu

import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.SingleItemSelectionModel
import io.nacular.doodle.controls.list.MutableList
import io.nacular.doodle.controls.mutableListModelOf
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl


/**
 * Created by Nicholas Eddy on 4/30/18.
 */
@Deprecated(message = "Use popupmenu.Menu instead")
public class PopupMenu(display: Display): MutableList<MenuItem, MutableListModel<MenuItem>>(mutableListModelOf(), selectionModel = SingleItemSelectionModel()), MenuItem {

    // View has an internal display property so have to create new one
    private  val _display                               = display
    override val subMenus    : Iterator<MenuItem> get() = model.iterator()
    override var parentMenu  : MenuItem?                = null
    override var menuSelected: Boolean                  = false
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

    public fun add(menu: Menu) {
        if (menu.parentMenu !== this) {
            size = Size(100.0, 4.0) // FIXME: remove

            model.add(menu)

            menu.parentMenu = this
        }
    }

    public fun show(owner: View, at: Point) {
        visible = true

        // FIXME: IMPLEMENT
        position = owner.toAbsolute(at)

        _display.children += this
    }
}
