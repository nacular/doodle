package com.nectar.doodle.controls.menu

import com.nectar.doodle.core.Display

/**
 * Created by Nicholas Eddy on 4/30/18.
 */
interface PopupFactory {
    operator fun invoke(): PopupMenu
}

class PopupFactoryImpl(private val display: Display): PopupFactory {
    override fun invoke() = PopupMenu(display)
}