package io.nacular.doodle.controls.menu

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Icon

/**
 * Created by Nicholas Eddy on 4/30/18.
 */
public interface PopupFactory {
    public operator fun invoke(): PopupMenu
}

public class PopupFactoryImpl(private val display: Display): PopupFactory {
    override fun invoke(): PopupMenu = PopupMenu(display)
}

public interface MenuFactory {
    public operator fun invoke(text: String = "", icon: Icon<Button>? = null): Menu
}

public class MenuFactoryImpl(private val menuSelectionManager: MenuSelectionManager, private val popupFactory: PopupFactory): MenuFactory {
    override fun invoke(text: String, icon: Icon<Button>?): Menu = Menu(menuSelectionManager, popupFactory, text, icon)
}