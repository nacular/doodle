package io.nacular.doodle.controls.popupmenu

import io.nacular.doodle.controls.PopupManager
import io.nacular.doodle.controls.popupmenu.MenuBehavior.ActionItemInfo
import io.nacular.doodle.controls.popupmenu.MenuBehavior.ItemConfig
import io.nacular.doodle.controls.popupmenu.MenuBehavior.ItemInfo
import io.nacular.doodle.controls.popupmenu.MenuBehavior.SeparatorConfig
import io.nacular.doodle.controls.popupmenu.MenuBehavior.SubMenuConfig
import io.nacular.doodle.controls.popupmenu.MenuBehavior.SubMenuInfo
import io.nacular.doodle.core.ContentDirection.LeftRight
import io.nacular.doodle.core.Icon
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.core.renderProperty
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.event.KeyCode.Companion.ArrowDown
import io.nacular.doodle.event.KeyCode.Companion.ArrowLeft
import io.nacular.doodle.event.KeyCode.Companion.ArrowRight
import io.nacular.doodle.event.KeyCode.Companion.ArrowUp
import io.nacular.doodle.event.KeyCode.Companion.Enter
import io.nacular.doodle.event.KeyCode.Companion.Escape
import io.nacular.doodle.event.KeyCode.Companion.Space
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.PointerListener.Companion.clicked
import io.nacular.doodle.event.PointerListener.Companion.entered
import io.nacular.doodle.event.PointerListener.Companion.on
import io.nacular.doodle.event.PointerMotionListener.Companion.moved
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.scheduler.Scheduler
import io.nacular.doodle.scheduler.Task
import io.nacular.doodle.utils.autoCanceling
import io.nacular.doodle.utils.observable
import io.nacular.doodle.utils.zeroMillis

/**
 * A component that contains a set of sub-menus that perform some action when triggered.
 */
public class Menu private constructor(
    private val focusManager: FocusManager?,
    private val parentMenu  : Menu? = null,
    private val close       : (Menu) -> Unit,
): View() {
    private sealed class Item: View() {
        abstract fun setRenderer(behavior: MenuBehavior?)
    }

    private val selection: InteractiveMenu? get() = selectionIndex.takeIf { it >= 0 }?.let { selectables[it] }
    private var selectedDrawer: SubMenu? = null

    private var selectionIndex by observable(-1) { old,new ->
        if (old >= 0) {
            selectables[old].selected = false
            selectedDrawer = null
        }

        if (new >= 0) {
            val newSelection = selection?.also { it.selected = true }

            if (newSelection is SubMenu) {
                selectedDrawer = newSelection
            }

            focusManager?.requestFocus(this)
        }
    }

    private var selectables = emptyList<InteractiveMenu>()

    private var menus: List<Item> by observable(emptyList()) { _,new ->
        children    += new
        selectables  = new.filterIsInstance<InteractiveMenu>()

        new.forEach { menu ->
            menu.setRenderer(behavior)

            if (menu is Action && menu.icon != null) {
                anyItemWithIcon_ = true
            }
        }
    }

    internal var insetsInternal: Insets get() = insets; set(new) { insets = new }

    internal var anyItemWithIcon_: Boolean = false; private set

    /**
     * Controls the look/feel of the Menu and all its sub-menus
     */
    public var behavior: MenuBehavior? by behavior { _,new ->
        menus.forEach {
            it.setRenderer(new)
        }
    }

    init {
        isFocusCycleRoot   = true
        clipCanvasToBounds = false
        layout             = simpleLayout { container ->
            var y        = insets.top
            val maxWidth = container.children.maxOf { it.idealSize?.width ?: 0.0 }

            container.children.forEach {
                it.bounds  = Rectangle(insets.left, y, maxWidth, it.height)
                y         += it.height
            }

            size = Size(maxWidth + insets.right, height = y + insets.bottom)
        }

        enabledChanged += { _,_,enabled ->
            if (!enabled) hideDrawer()
        }

        pointerChanged += on(
            entered = {
                clearSelection()
            },
            exited  = {
                if (hasFocus) {
                    clearSelection()
                }
            }
        )

        keyChanged += KeyListener.pressed {
            when (it.code) {
                ArrowUp      -> moveSelection { it - 1 }
                ArrowDown    -> moveSelection { it + 1 }
                ArrowLeft    -> when (contentDirection) {
                    LeftRight -> hideDrawerOrClose()
                    else      -> selectedDrawer?.trigger()
                }
                ArrowRight   -> when (contentDirection) {
                    LeftRight -> selectedDrawer?.trigger()
                    else      -> hideDrawerOrClose()
                }
                Escape       -> close()
                Space, Enter -> selection?.trigger()
            }
        }
    }

    /**
     * Causes the menu to reveal the specified item so it is visible on the screen.
     *
     * @param item to reveal
     */
    public fun reveal(item: MenuItem) {
        (item as? InteractiveMenu)?.let {
            it.parentDrawer?.let {
                it.parentMenu.reveal(it)
            }

            if (item is SubMenu) {
                it.parentMenu.requestSelection(it)
                item.trigger()
            }
        }
    }

    /**
     * Causes the menu to select and [reveal] the specified item.
     *
     * @param item to reveal
     */
    public fun select(item: MenuItem) {
        (item as? InteractiveMenu)?.let {
            it.parentDrawer?.let {
                it.parentMenu.select(it)
            }

            it.parentMenu.requestSelection(it)

            if (item is SubMenu) {
                item.trigger()
            }
        }
    }

    override fun addedToDisplay() {
        super.addedToDisplay()

        focusManager?.requestFocus(this)
    }

    override fun removedFromDisplay() {
        clearSelection()
        hideDrawer()
        super.removedFromDisplay()
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    private fun hideDrawerOrClose() {
        when (selectedDrawer?.menuVisible) {
            true -> hideDrawer()
            else -> parentMenu?.hideDrawer()
        }
    }

    private fun moveSelection(offset: (Int) -> Int) {
        val initialNext = (offset(selectionIndex) % selectables.size).let {
            if (it < 0) selectables.size - 1 else it
        }

        var possibleSelection = initialNext

        if (!selectables[possibleSelection].enabled) {
            do {
                possibleSelection = (offset(possibleSelection) % selectables.size).let {
                    if (it < 0) selectables.size - 1 else it
                }
            } while (possibleSelection != initialNext && !selectables[possibleSelection].enabled)
        }

        requestSelection(selectables[possibleSelection])
    }

    private fun clearSelection() {
        selectionIndex = -1
    }

    private fun requestSelection(item: InteractiveMenu): Boolean {
        if (!item.enabled) return false

        selectionIndex = selectables.indexOf(item)

        return item.selected
    }

    private fun hideDrawer() {
        selectedDrawer?.hideSubMenu()
        if (displayed) {
            focusManager?.requestFocus(this)
        }
    }

    private fun close() {
        close(this)
    }

    private abstract class InteractiveMenu(val parentMenu: Menu, override val text: String): Item(), MenuItem, ItemInfo {

        var parentDrawer: SubMenu? = null

        init {
            acceptsThemes = false

            styleChanged += {
                updateBounds()
            }

            pointerChanged += entered {
                parentMenu.requestSelection(this)
                it.consume()
            }

            pointerMotionChanged += moved {
                if (!selected) {
                    parentMenu.requestSelection(this)
                    it.consume()
                    trigger()
                }
            }
        }

        override var selected: Boolean by renderProperty(false)

        override fun toString() = text

        abstract fun trigger()

        protected abstract fun updateBounds()
    }

    private class SubMenu(
                    parentMenu  : Menu,
                    text        : String,
        private val popups      : PopupManager,
        private val scheduler   : Scheduler,
        private val menu        : Menu? = null
    ): InteractiveMenu(parentMenu, text) {

        private var renderer: SubMenuConfig? by renderProperty(null) { _,_ -> updateBounds() }

        private var popupTask: Task? by autoCanceling()

        val menuVisible get() = menu != null && popups.active(menu)

        init {
            styleChanged += {
                menu?.font = font
            }

            pointerChanged += entered {
                trigger()
            }
        }

        fun hideSubMenu() {
            menu?.let {
                popupTask?.cancel()
                popups.hide(it)
            }
        }

        override fun trigger() {
            if (!menuVisible) {
                menu?.let { menu ->
                    popupTask = scheduler.after(renderer?.showDelay ?: zeroMillis) {
                        popups.show(menu, relativeTo = this) { menu, self ->
                            (menu.top eq self.y - 5)..Strong

                            when {
                                parent.width.readOnly - self.right > menu.width.readOnly - 2 -> (menu.left eq self.right - 2)..Strong
                                else                                                         -> (menu.right eq self.x + 2)..Strong
                            }

                            (menu.top greaterEq 5)..Strong
                            (menu.left greaterEq 5)..Strong

                            menu.width.preserve
                            menu.height.preserve

                            menu.right lessEq parent.right - 5
                            menu.bottom lessEq parent.bottom - 5
                        }
                    }
                }
            }
        }

        override fun removedFromDisplay() {
            hideSubMenu()

            super.removedFromDisplay()
        }

        private val info = object: SubMenuInfo {
            override val text        get() = this@SubMenu.text
            override val font        get() = this@SubMenu.font
            override val enabled     get() = this@SubMenu.enabled
            override val selected    get() = this@SubMenu.selected
            override val mirrored    get() = this@SubMenu.mirrored
            override val hasChildren get() = menu != null
        }

        override fun render(canvas: Canvas) {
            renderer?.render(info, canvas)
        }

        override var selected: Boolean by renderProperty(false) { _,selected ->
            if (!selected) {
                hideSubMenu()
            }
        }

        override fun setRenderer(behavior: MenuBehavior?) {
            renderer = behavior?.subMenuConfig(parentMenu)
        }

        override fun updateBounds() {
            renderer?.preferredSize(info)?.let {
                height    = it.height
                idealSize = it
            }
        }
    }

    private open class Action(
                      parentMenu: Menu,
                      text      : String,
         override val icon      : Icon<ItemInfo>? = null,
        private   val fired     : (MenuItem) -> Unit
    ): InteractiveMenu(parentMenu, text), ActionItemInfo {

        protected var renderer: ItemConfig<ActionItemInfo>? by renderProperty(null) { _,_ -> updateBounds() }

        init {
            pointerChanged += clicked { trigger() }
        }

        override fun trigger() {
            fired(this)
            parentMenu.close()
        }

        override fun render(canvas: Canvas) {
            renderer?.apply {
                render(this@Action, canvas)
            }
        }

        override fun setRenderer(behavior: MenuBehavior?) {
            renderer = behavior?.actionConfig(parentMenu)
        }

        override fun updateBounds() {
            renderer?.preferredSize(this)?.let {
                height    = it.height
                idealSize = it
            }
        }
    }

    private class Prompt(
        parentMenu  : Menu,
        text        : String,
        icon        : Icon<ItemInfo>? = null,
        fired       : (MenuItem) -> Unit): Action(parentMenu, text, icon, fired) {
        override fun setRenderer(behavior: MenuBehavior?) {
            renderer = behavior?.promptConfig(parentMenu)
        }
    }

    private class Separator(private val parentMenu: Menu): Item() {
        private var renderer: SeparatorConfig? by renderProperty(null) { _,new ->
            height = new?.preferredSize()?.height ?: height
        }

        init {
            focusable     = false
            acceptsThemes = false
        }

        override fun setRenderer(behavior: MenuBehavior?) {
            renderer = behavior?.separatorConfig(parentMenu)
        }

        override fun render(canvas: Canvas) { renderer?.render(canvas) }
    }

    private class MenuCreationContextImpl(
        private val focusManager: FocusManager?,
        private val popups      : PopupManager,
        private val scheduler   : Scheduler,
        private val parent      : Menu
    ): MenuCreationContext {

        val items = mutableListOf<Item>()

        override fun menu(title: String, context: MenuCreationContext.() -> Unit): MenuItem {
            val subMenu = Menu(focusManager, parent) {
                parent.close()
            }
            val subContext = MenuCreationContextImpl(focusManager, popups, scheduler, subMenu)

            context(subContext)

            subMenu.menus  = subContext.items

            return SubMenu(
                popups      = popups,
                scheduler   = scheduler,
                parentMenu  = parent,
                text        = title,
                menu        = if (subMenu.menus.isNotEmpty()) subMenu else null
            ).also {
                items += it

                subMenu.menus.filterIsInstance<InteractiveMenu>().forEach { item ->
                    item.parentDrawer = it
                }
            }
        }

        override fun action(title: String, icon: Icon<ItemInfo>?, fired: (MenuItem) -> Unit): MenuItem = Action(
            parent,
            title,
            icon,
            fired
        ).also { items += it }

        override fun prompt(title: String, icon: Icon<ItemInfo>?, fired: (MenuItem) -> Unit): MenuItem = Prompt(
            parent,
            title,
            icon,
            fired
        ).also { items += it }

        override fun separator() {
            items += Separator(parent)
        }
    }

    public companion object {
        internal operator fun invoke(
            focusManager: FocusManager?,
            popups      : PopupManager,
            scheduler   : Scheduler,
            close       : (Menu) -> Unit,
            block       : MenuCreationContext.() -> Unit): Menu {

            val menu    = Menu(focusManager, parentMenu = null, close)
            val context = MenuCreationContextImpl(focusManager, popups, scheduler, menu)

            block(context)

            return menu.apply {
                menus = context.items
            }
        }
    }
}