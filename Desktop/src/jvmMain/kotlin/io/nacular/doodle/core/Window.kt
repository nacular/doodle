package io.nacular.doodle.core

import io.nacular.doodle.controls.popupmenu.MenuCreationContext
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.ChangeObservers

public interface MenuBar {
    public interface Menu {
        /**
         * Determines whether the item is enabled.
         */
        public var enabled: Boolean
    }

    public interface CreationContext {
        /**
         * Adds an item to the menu that will show a child menu.
         *
         * @param title of the sub menu
         * @param context for populating the drawer's sub-menus
         */
        public fun menu(title: String, context: MenuCreationContext.() -> Unit): Menu
    }

    public operator fun invoke(block: CreationContext.() -> Unit)
}

public interface PopupMenu {
    public var enabled: Boolean

    public fun dismiss()
}

public sealed interface Window {
    public var size                : Size
    public var title               : String
    public var enabled             : Boolean
    public var resizable           : Boolean
    public val decorated           : Boolean
    public var focusable           : Boolean
    public var triesToAlwaysBeOnTop: Boolean

    public val menuBar: MenuBar

    public val closed: ChangeObservers<Window>

    public val display: Display

    public fun popupMenu(at: Point, context: MenuCreationContext.() -> Unit): PopupMenu

    public fun close()
}

public interface WindowGroup {
    public val main: Window

    public operator fun invoke(frameLess: Boolean = false): Window

    public fun shutdown()
}
