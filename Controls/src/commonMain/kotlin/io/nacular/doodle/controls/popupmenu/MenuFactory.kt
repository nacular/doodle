package io.nacular.doodle.controls.popupmenu

import io.nacular.doodle.controls.PopupManager
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.scheduler.Scheduler

/**
 * Item added to a [Menu] via [MenuCreationContext].
 */
public sealed interface MenuItem {
    /**
     * Determines whether the item is enabled.
     */
    public var enabled: Boolean
}

/**
 * DSL for configuring [Menu]s.
 *
 * @see MenuFactory.invoke
 */
public interface MenuCreationContext {
    /**
     * Adds an item to the menu that will show a child menu.
     *
     * @param title of the sub menu
     * @param context for populating the drawer's sub-menus
     */
    public fun menu(title: String, context: MenuCreationContext.() -> Unit): MenuItem

    /**
     * Adds an action item to the menu that will invoke [fired] when it is triggered.
     *
     * @param title of the item
     * @param fired invoked when this item is triggered
     */
    public fun action(title: String, fired: (MenuItem) -> Unit): MenuItem

    /**
     * Adds an item to the menu that will invoke [fired] when it is triggered. These items
     * should be used when the user will be prompted before taking any action.
     *
     * @param title of the item
     * @param fired invoked when this item is triggered
     */
    public fun prompt(title: String, fired: (MenuItem) -> Unit): MenuItem

    /**
     * Adds a separator to the menu.
     */
    public fun separator()
}

/**
 * Used to create [Menu]s.
 */
public interface MenuFactory {
    /**
     * Creates a new [Menu].
     *
     * @param close invoked when the Menu needs to be dismissed.
     */
    public operator fun invoke(close: (Menu) -> Unit, block: MenuCreationContext.() -> Unit): Menu
}

public class MenuFactoryImpl(
    private val popups      : PopupManager,
    private val scheduler   : Scheduler,
    private val focusManager: FocusManager?,
): MenuFactory {
    override fun invoke(close: (Menu) -> Unit, block: MenuCreationContext.() -> Unit): Menu = Menu(
        focusManager = focusManager,
        popups       = popups,
        scheduler    = scheduler,
        close        = close,
        block        = block
    )
}
