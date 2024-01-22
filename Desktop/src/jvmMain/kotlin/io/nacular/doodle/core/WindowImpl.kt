package io.nacular.doodle.core

import io.nacular.doodle.application.CustomSkikoView
import io.nacular.doodle.controls.popupmenu.MenuBehavior.ItemInfo
import io.nacular.doodle.controls.popupmenu.MenuCreationContext
import io.nacular.doodle.controls.popupmenu.MenuItem
import io.nacular.doodle.core.MenuBar.CreationContext
import io.nacular.doodle.core.MenuBar.Menu
import io.nacular.doodle.core.impl.DisplayImpl
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.RenderManager
import io.nacular.doodle.drawing.impl.RealGraphicsDevice
import io.nacular.doodle.drawing.impl.RealGraphicsSurface
import io.nacular.doodle.drawing.impl.SwingCanvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.theme.native.toDoodle
import io.nacular.doodle.utils.ChangeObserver
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.ObservableSet
import io.nacular.doodle.utils.SetObserver
import io.nacular.doodle.utils.SetObservers
import io.nacular.doodle.utils.map
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.skia.Font
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skiko.SkiaLayer
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import kotlin.system.exitProcess
import javax.swing.Icon as SwingIcon

internal class WindowImpl(
                appScope       : CoroutineScope,
    private val defaultFont    : Font,
                uiDispatcher   : CoroutineDispatcher,
                fontCollection : FontCollection,
                graphicsDevices: (SkiaLayer  ) -> RealGraphicsDevice<RealGraphicsSurface>,
    private val renderManagers : (DisplayImpl) -> RenderManager,
                size           : Size,
                undecorated    : Boolean = false
): Window {
    private inner class MenuBarImpl: MenuBar {
        override fun invoke(block: CreationContext.() -> Unit) {
            block(object: CreationContext {
                override fun menu(title: String, context: MenuCreationContext.() -> Unit): Menu {
                    val menu = SwingMenu(JMenu(title))

                    context(JMenuCreationContext(menu.menu))

                    if (skiaWindow.jMenuBar == null) { skiaWindow.jMenuBar = JMenuBar() }

                    skiaWindow.jMenuBar.add(menu.menu)

                    return menu
                }
            })
        }
    }

    private class SwingMenu(val menu: JMenu): Menu {
        override var enabled: Boolean get() = menu.isEnabled; set(new) { menu.isEnabled = new }
    }

    private class SwingMenuItem(val menu: JMenuItem): MenuItem, ItemInfo {
        override val text     get() = menu.text
        override val font     get() = menu.font.toDoodle()
        override var enabled  get() = menu.isEnabled; set(new) { menu.isEnabled = new }
        override val selected get() = menu.isSelected
        override val mirrored get() = false // FIXME
    }

    private inner class JMenuCreationContext(private val jMenu: JMenu): MenuCreationContext {
        override fun menu(title: String, context: MenuCreationContext.() -> Unit): MenuItem {
            val menu = SwingMenuItem(JMenu(title))

            jMenu.add(menu.menu)

            context(JMenuCreationContext(menu.menu as JMenu))

            return menu
        }

        override fun action(title: String, icon: Icon<ItemInfo>?, fired: (MenuItem) -> Unit) = prompt(title, icon, fired)

        override fun prompt(title: String, icon: Icon<ItemInfo>?, fired: (MenuItem) -> Unit) = menuItem(title, icon, fired).apply {
            jMenu.add(menu)
        }

        override fun separator() {
            jMenu.addSeparator()
        }
    }

    private fun menuItem(title: String, icon: Icon<ItemInfo>?, fired: (MenuItem) -> Unit): SwingMenuItem {
        val item = SwingMenuItem(JMenuItem(title))

        icon?.swing(item)?.let { item.menu.icon = it }

        item.menu.addActionListener { fired(item) }

        return item
    }

    private inner class JPopupMenuCreationContext(private val jPopupMenu: JPopupMenu): MenuCreationContext {
        override fun menu(title: String, context: MenuCreationContext.() -> Unit): MenuItem {
            val menu = SwingMenuItem(JMenu(title))

            jPopupMenu.add(menu.menu)

            context(JMenuCreationContext(menu.menu as JMenu))

            return menu
        }

        override fun action(title: String, icon: Icon<ItemInfo>?, fired: (MenuItem) -> Unit) = prompt(title, icon, fired)

        override fun prompt(title: String, icon: Icon<ItemInfo>?, fired: (MenuItem) -> Unit) = menuItem(title, icon, fired).apply {
            jPopupMenu.add(menu)
        }

        override fun separator() {
            jPopupMenu.addSeparator()
        }
    }

    private fun <T: Any> Icon<T>.swing(item: T) = object: SwingIcon {
        override fun paintIcon(c: Component?, g: Graphics?, x: Int, y: Int) {
            (g as? Graphics2D)?.let {
                render(item, SwingCanvas(it, defaultFont, size(item)), at = Point(x, y))
            }
        }

        override fun getIconWidth () = size(item).width.toInt ()
        override fun getIconHeight() = size(item).height.toInt()
    }

    private val skiaWindow = JFrame().apply {
        isUndecorated = undecorated

        addWindowListener(object: WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                this@WindowImpl.close()
            }
        })
    }

    internal lateinit var renderManager: RenderManager

    val skiaLayer = SkiaLayer().apply {
        addView(CustomSkikoView())
    }

    val graphicsDevice: GraphicsDevice<RealGraphicsSurface> = graphicsDevices(skiaLayer)

    override val closed: ChangeObservers<WindowImpl> = ChangeObserversImpl(this)

    override var bounds get() = skiaWindow.bounds.run { Rectangle(x, y, width, height) }; set(new) {
        skiaWindow.isVisible     = false
        skiaWindow.bounds        = java.awt.Rectangle(new.x.toInt(), new.y.toInt(), new.width.toInt(), new.height.toInt())
        skiaWindow.preferredSize = Dimension(new.width.toInt(), new.height.toInt())
        skiaWindow.pack()
        skiaWindow.isVisible     = true
    }

    override var title                get() = skiaWindow.title;                set(new) { skiaWindow.title         = new }
    override var enabled              get() = skiaWindow.isEnabled;            set(new) { skiaWindow.isEnabled     = new }
    override var resizable            get() = skiaWindow.isResizable;          set(new) { skiaWindow.isResizable   = new }
    override val decorated            get() = skiaWindow.isUndecorated
    override var focusable            get() = skiaWindow.focusableWindowState; set(new) { skiaWindow.focusableWindowState = new }
    override var triesToAlwaysBeOnTop get() = skiaWindow.isAlwaysOnTop;        set(new) { skiaWindow.isAlwaysOnTop        = new }

    override val display: Display
    override val menuBar: MenuBar by lazy { MenuBarImpl() }

    init {
        skiaLayer.attachTo(skiaWindow.contentPane)

        skiaWindow.preferredSize = size.run { Dimension(width.toInt(), height.toInt()) }
        skiaWindow.pack()
        skiaWindow.isVisible = true

        display = DisplayImpl(
            appScope,
            uiDispatcher,
            skiaLayer,
            defaultFont,
            fontCollection,
            graphicsDevice
        )
    }

    fun start() {
        renderManager = renderManagers(display as DisplayImpl)
    }

    private class SwingPopupMenu(val menu: JPopupMenu): PopupMenu {
        override var enabled: Boolean get() = menu.isEnabled; set(new) { menu.isEnabled = new }

        override fun dismiss() {
            menu.isVisible = false
        }
    }

    override fun popupMenu(at: Point, context: MenuCreationContext.() -> Unit): PopupMenu {
        val popup = SwingPopupMenu(JPopupMenu())

        context(JPopupMenuCreationContext(popup.menu))

        popup.menu.show(skiaWindow, at.x.toInt(), at.y.toInt())

        return popup
    }

    override fun close() {
        (display as DisplayImpl).shutdown()

        skiaWindow.isVisible = false
        skiaWindow.dispose()

        (closed as ChangeObserversImpl).forEach { it(this) }
    }
}

internal class WindowGroupImpl(private val windowFactory: (Boolean) -> WindowImpl): WindowGroup {
    override         val windows     = mutableListOf<WindowImpl>()

    private          var started     = false
    private          val allDisplays = ObservableSet<DisplayImpl>()
    private lateinit var closeHandler: ChangeObserver<WindowImpl>

    val displays: Set<DisplayImpl> get() = allDisplays

    val displaysChanged: SetObservers<WindowGroupImpl, DisplayImpl> = allDisplays.changed.map {
        object: SetObserver<ObservableSet<DisplayImpl>, DisplayImpl> {
            override fun invoke(
                source : ObservableSet<DisplayImpl>,
                removed: Set<DisplayImpl>,
                added  : Set<DisplayImpl>
            ) {
                it(this@WindowGroupImpl, removed, added)
            }
        }
    }

    override val windowsChanged: ChangeObservers<WindowGroup> = ChangeObserversImpl(this)

    override var mainWindowCloseBehavior: (() -> Unit)? = null

    override lateinit var main: Window

    init {
        closeHandler = {
            it.closed   -= closeHandler
            windows     -= it
            allDisplays -= it.display as DisplayImpl

            (windowsChanged as ChangeObserversImpl).forEach { it(this) }

            if (it == main) {
                when (val behavior = mainWindowCloseBehavior) {
                    null -> exitProcess(0)
                    else -> behavior()
                }
            }
        }

        main = this()

        allDisplays += windows.map { it.display as DisplayImpl }
    }

    override fun invoke(frameLess: Boolean) = windowFactory(frameLess).also {
        it.closed   += closeHandler
        windows     += it
        allDisplays += it.display as DisplayImpl

        (windowsChanged as ChangeObserversImpl).forEach { it(this) }

        if (started) {
            it.start()
        }
    }

    fun start() {
        if (started) return

        started = true
        windows.forEach { it.start() }
    }

    fun owner(of: View): WindowImpl? = windows.find { it.display.ancestorOf(of) }

    fun shutdown() {
        while (windows.isNotEmpty()) {
            windows.firstOrNull()?.close()
        }
    }
}