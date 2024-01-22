package io.nacular.doodle.core

import io.nacular.doodle.controls.popupmenu.MenuCreationContext
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.with
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

/**
 * Represents a single window an application.
 */
public sealed interface Window {
    /** Location of the window's left edge. */
    public var x: Double get() = bounds.x;  set(new) { bounds = bounds.at(x = new) }

    /** Location of the window's top edge. */
    public var y: Double get() = bounds.y; set(new) { bounds = bounds.at(y = new) }

    /** Location of the window's top-left point. */
    public var position: Point get() = bounds.position; set(new) { bounds = bounds.at(position = new) }

    /** Horizontal size of the window. */
    public var width: Double get() = bounds.width;  set(new) { bounds = bounds.with(width  = new) }

    /** Vertical size of the window. */
    public var height: Double get() = bounds.height; set(new) { bounds = bounds.with(height = new) }

    /** Width and height of the window. */
    public var size: Size get() = bounds.size; set(new) { bounds = bounds.with(size = new) }

    /** The window's location and size. */
    public var bounds: Rectangle

    /** Text shown in window's title section. */
    public var title: String

    /** Whether the window can be interacted with. */
    public var enabled: Boolean

    /** Whether the window's [size] can be changed. */
    public var resizable: Boolean

    /** Whether the window has chrome. */
    public val decorated: Boolean

    /** Whether the window can be a focus owner. */
    public var focusable: Boolean

    /** Whether the window tries to remain above all other windows. */
    public var triesToAlwaysBeOnTop: Boolean

    /** Triggered whenever the window is closed. */
    public val closed: ChangeObservers<Window>

    /** Used to manipulate menu items for the window. */
    public val menuBar: MenuBar

    /** The underlying display for holding Views in the window. */
    public val display: Display

    /**
     * Shows a pop-up menu for the window.
     *
     * @param at the given point relative to the window
     * @param context for specifying items in the pop-up
     */
    public fun popupMenu(at: Point, context: MenuCreationContext.() -> Unit): PopupMenu

    /**
     * Closes the window.
     */
    public fun close()
}

/**
 * Manages all windows for an application.
 */
public interface WindowGroup {
    /**
     * The application's main window. All apps have a main window that is automatically created. Closing this main
     * window will exit the app by default, unless [mainWindowCloseBehavior] has been set. If so, then it is invoked
     * whenever the main window closes and can decide whether the app gets closed or not.
     */
    public val main: Window

    /**
     * All windows currently open for the application.
     */
    public val windows: List<Window>

    /**
     * Called whenever the [main] window is closed. Setting this to `null` will revert to the default close behavior:
     * the app exists when the [main] window is closed.
     */
    public var mainWindowCloseBehavior: (() -> Unit)?

    /** Notified whenever [windows] changes. */
    public val windowsChanged: ChangeObservers<WindowGroup>

    /**
     * Creates and shows a new window.
     *
     * @param frameLess indicates whether the window has chrome (see [Window.decorated]).
     * @return the new window
     */
    public operator fun invoke(frameLess: Boolean = false): Window
}
