package io.nacular.doodle.controls.menu

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Icon

/**
 * @see [io.nacular.doodle.controls.popupmenu.Menu]
 */
@Deprecated(message = "Use popupmenu.Menu along with PopupManager instead")
public interface PopupFactory {
    public operator fun invoke(): PopupMenu
}

@Deprecated(message = "Use popupmenu.Menu along with PopupManager instead")
public class PopupFactoryImpl(private val display: Display): PopupFactory {
    override fun invoke(): PopupMenu = PopupMenu(display)
}

/**
 * @see [io.nacular.doodle.controls.popupmenu.MenuFactory]
 */
@Deprecated(message = "Use popupmenu.MenuFactory instead")
public interface MenuFactory {
    public operator fun invoke(text: String = "", icon: Icon<Button>? = null): Menu
}

@Deprecated(message = "Use popupmenu.MenuFactory instead")
public class MenuFactoryImpl(private val menuSelectionManager: MenuSelectionManager, private val popupFactory: PopupFactory): MenuFactory {
    override fun invoke(text: String, icon: Icon<Button>?): Menu = Menu(menuSelectionManager, popupFactory, text, icon)
}