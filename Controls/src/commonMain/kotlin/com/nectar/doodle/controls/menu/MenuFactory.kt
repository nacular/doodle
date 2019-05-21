package com.nectar.doodle.controls.menu

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Icon

/**
 * Created by Nicholas Eddy on 4/30/18.
 */
interface PopupFactory {
    operator fun invoke(): PopupMenu
}

class PopupFactoryImpl(private val display: Display): PopupFactory {
    override fun invoke() = PopupMenu(display)
}

interface MenuFactory {
    operator fun invoke(text: String = "", icon: Icon<Button>? = null): Menu
}

class MenuFactoryImpl(private val menuSelectionManager: MenuSelectionManager, private val popupFactory: PopupFactory): MenuFactory {
    override fun invoke(text: String, icon: Icon<Button>?) = Menu(menuSelectionManager, popupFactory, text, icon)
}