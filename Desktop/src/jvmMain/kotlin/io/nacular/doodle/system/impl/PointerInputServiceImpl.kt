package io.nacular.doodle.system.impl

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.core.WindowGroupImpl
import io.nacular.doodle.core.impl.DisplayImpl
import io.nacular.doodle.deviceinput.ViewFinder
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.swing.doodle
import io.nacular.doodle.swing.location
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.Cursor.Companion.Crosshair
import io.nacular.doodle.system.Cursor.Companion.Default
import io.nacular.doodle.system.Cursor.Companion.EResize
import io.nacular.doodle.system.Cursor.Companion.Grab
import io.nacular.doodle.system.Cursor.Companion.Move
import io.nacular.doodle.system.Cursor.Companion.NResize
import io.nacular.doodle.system.Cursor.Companion.NeResize
import io.nacular.doodle.system.Cursor.Companion.NwResize
import io.nacular.doodle.system.Cursor.Companion.Progress
import io.nacular.doodle.system.Cursor.Companion.SResize
import io.nacular.doodle.system.Cursor.Companion.SeResize
import io.nacular.doodle.system.Cursor.Companion.SwResize
import io.nacular.doodle.system.Cursor.Companion.Text
import io.nacular.doodle.system.Cursor.Companion.WResize
import io.nacular.doodle.system.Cursor.Companion.Wait
import io.nacular.doodle.system.PointerInputService
import io.nacular.doodle.system.PointerInputService.Listener
import io.nacular.doodle.system.PointerInputService.Preprocessor
import io.nacular.doodle.system.SystemInputEvent.Modifier
import io.nacular.doodle.system.SystemInputEvent.Modifier.Alt
import io.nacular.doodle.system.SystemInputEvent.Modifier.Ctrl
import io.nacular.doodle.system.SystemInputEvent.Modifier.Meta
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.SystemPointerEvent.Button.Button1
import io.nacular.doodle.system.SystemPointerEvent.Button.Button2
import io.nacular.doodle.system.SystemPointerEvent.Button.Button3
import io.nacular.doodle.system.SystemPointerEvent.Type
import io.nacular.doodle.system.SystemPointerEvent.Type.Click
import io.nacular.doodle.system.SystemPointerEvent.Type.Down
import io.nacular.doodle.system.SystemPointerEvent.Type.Enter
import io.nacular.doodle.system.SystemPointerEvent.Type.Exit
import io.nacular.doodle.system.SystemPointerEvent.Type.Up
import java.awt.Cursor.CROSSHAIR_CURSOR
import java.awt.Cursor.DEFAULT_CURSOR
import java.awt.Cursor.E_RESIZE_CURSOR
import java.awt.Cursor.HAND_CURSOR
import java.awt.Cursor.MOVE_CURSOR
import java.awt.Cursor.NE_RESIZE_CURSOR
import java.awt.Cursor.NW_RESIZE_CURSOR
import java.awt.Cursor.N_RESIZE_CURSOR
import java.awt.Cursor.SE_RESIZE_CURSOR
import java.awt.Cursor.SW_RESIZE_CURSOR
import java.awt.Cursor.S_RESIZE_CURSOR
import java.awt.Cursor.TEXT_CURSOR
import java.awt.Cursor.WAIT_CURSOR
import java.awt.Cursor.W_RESIZE_CURSOR
import java.awt.event.MouseEvent
import java.awt.event.MouseEvent.BUTTON1
import java.awt.event.MouseEvent.BUTTON2
import java.awt.event.MouseEvent.BUTTON3
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import java.awt.Cursor as AwtCursor

internal typealias NativeScrollHandler = (event: MouseWheelEvent, pointInTarget: Point) -> Unit

internal class NativeScrollHandlerFinder {
    private val handlers = mutableMapOf<View, NativeScrollHandler>()

    operator fun get(view: View) = handlers[view]

    operator fun set(view: View, handler: NativeScrollHandler) {
        handlers[view] = handler
    }

    fun remove(view: View) = handlers.remove(view)
}

/**
 * Created by Nicholas Eddy on 5/24/21.
 */
internal class PointerInputServiceImpl(
    private val windowGroup              : WindowGroupImpl,
    private val viewFinder               : ViewFinder,
    private val nativeScrollHandlerFinder: NativeScrollHandlerFinder?
): PointerInputService {
    private var started       = false
    private val listeners     = mutableMapOf<Display, MutableSet<Listener>>()
    private val preprocessors = mutableMapOf<Display, MutableSet<Preprocessor>>()

    private fun Cursor?.swing() = when (this) {
//        None      -> null
        Text      -> AwtCursor(TEXT_CURSOR)
        Wait      -> AwtCursor(WAIT_CURSOR)
//        Help      -> AwtCursor(CUSTOM_CURSOR)
        Move      -> AwtCursor(MOVE_CURSOR)
        Grab      -> AwtCursor(HAND_CURSOR)
//        Copy      ->
//        Alias     ->
//        ZoomIn    ->
//        NoDrop    ->
//        ZoomOut   ->
        Default   -> AwtCursor(DEFAULT_CURSOR)
//        Pointer   ->
        NResize   -> AwtCursor(N_RESIZE_CURSOR)
        SResize   -> AwtCursor(S_RESIZE_CURSOR)
        EResize   -> AwtCursor(E_RESIZE_CURSOR)
        WResize   -> AwtCursor(W_RESIZE_CURSOR)
//        EWResize  ->
//        Grabbing  ->
        Progress  -> AwtCursor(WAIT_CURSOR     )
        NeResize  -> AwtCursor(NE_RESIZE_CURSOR)
        NwResize  -> AwtCursor(NW_RESIZE_CURSOR)
        SeResize  -> AwtCursor(SE_RESIZE_CURSOR)
        SwResize  -> AwtCursor(SW_RESIZE_CURSOR)
        Crosshair -> AwtCursor(CROSSHAIR_CURSOR)
//        ColResize ->
//        RowResize ->
        else      -> AwtCursor(DEFAULT_CURSOR)
    }

    override fun getCursor(display: Display) = (display as? DisplayImpl)?.panel?.cursor?.doodle()

    override fun setCursor(display: Display, cursor: Cursor?) {
        (display as? DisplayImpl)?.panel?.cursor = cursor.swing()
    }

    // FIXME: This doesn't seem to work
    override fun getToolTipText(display: Display): String = "" //(display as? DisplayImpl)?.panel?.toolTipText ?: ""

    // FIXME: This doesn't seem to work
    override fun setToolTipText(display: Display, text: String) {
//        (display as? DisplayImpl)?.panel?.toolTipText = text
    }

    override fun addListener   (display: Display, listener: Listener) { listeners.getOrPut(display) { mutableSetOf() }.plusAssign (listener); if (listeners.size == 1) startUp() }
    override fun removeListener(display: Display, listener: Listener) { listeners[display]?.minusAssign(listener); shutdown() }

    override fun addPreprocessor   (display: Display, preprocessor: Preprocessor) { preprocessors.getOrPut(display) { mutableSetOf() }.plusAssign (preprocessor); if (preprocessors.size == 1) startUp() }
    override fun removePreprocessor(display: Display, preprocessor: Preprocessor) { preprocessors[display]?.minusAssign(preprocessor); shutdown()                             }

    private fun startUp() {
        if (!started) {
            windowGroup.displays.forEach(::setupDisplay)

            windowGroup.displaysChanged += { _, removed, added ->
                removed.forEach(::teardownDisplay)
                added.forEach  (::setupDisplay   )
            }

            started = true
        }
    }

    private val displayListeners = mutableMapOf<DisplayImpl, SkikoPointerListener>()

    private inner class SkikoPointerListener(private val display: DisplayImpl): MouseListener, MouseMotionListener, MouseWheelListener {
        override fun mouseClicked   (e: MouseEvent     ) { notifyPointerEvent(display, e, Click    ) }
        override fun mousePressed   (e: MouseEvent     ) { notifyPointerEvent(display, e, Down     ) }
        override fun mouseReleased  (e: MouseEvent     ) { notifyPointerEvent(display, e, Up       ) }
        override fun mouseEntered   (e: MouseEvent     ) { notifyPointerEvent(display, e, Enter    ) }
        override fun mouseExited    (e: MouseEvent     ) { notifyPointerEvent(display, e, Exit     ) }
        override fun mouseMoved     (e: MouseEvent     ) { notifyPointerEvent(display, e, Type.Move) }
        override fun mouseDragged   (e: MouseEvent     ) { notifyPointerEvent(display, e, Type.Move) }
        override fun mouseWheelMoved(e: MouseWheelEvent) { handleMouseWheel  (display, e           ) }
    }

    private fun setupDisplay(display: DisplayImpl) {
        SkikoPointerListener(display).also {
            displayListeners[display] = it

            display.panel.addMouseListener      (it)
            display.panel.addMouseWheelListener (it)
            display.panel.addMouseMotionListener(it)
        }
    }

    private fun teardownDisplay(display: DisplayImpl) {
        displayListeners.remove(display)?.let {
            display.panel.removeMouseListener      (it)
            display.panel.removeMouseWheelListener (it)
            display.panel.removeMouseMotionListener(it)
        }
    }

    private fun shutdown() {
        if (started && listeners.isEmpty() && preprocessors.isEmpty()) {
            started = false
        }
    }

    private fun notifyPointerEvent(display: Display, pointerEvent: MouseEvent, type: Type): Boolean {
        val event = pointerEvent.toDoodle(type)

        preprocessors[display]?.takeWhile { !event.consumed }?.forEach { it(event) }
        listeners    [display]?.takeWhile { !event.consumed }?.forEach { it(event) }

        return event.consumed
    }

    private fun handleMouseWheel(display: DisplayImpl, wheelEvent: MouseWheelEvent) {
        // TODO: Expose wheel events to View generally?
        if (nativeScrollHandlerFinder == null) {
            return
        }

        val absoluteLocation = wheelEvent.location(display.panel)

        viewFinder.find(display, absoluteLocation)?.let {
            var target = it as View?

            while (target != null) {
                val handler = nativeScrollHandlerFinder[target]

                if (handler != null) {
                    handler(wheelEvent, target.fromAbsolute(absoluteLocation))

                    break
                }

                target = target.parent
            }
        }
    }
}

internal fun MouseEvent.toDoodle(type: Type): SystemPointerEvent {
    var buttons = when (this.button) {
        BUTTON1 -> setOf(Button1)
        BUTTON2 -> setOf(Button2)
        BUTTON3 -> setOf(Button3)
        else    -> emptySet()
    }

    // FIXME: Change browser behavior to track released button instead of doing this
    if (type == Up) {
        buttons = emptySet()
    }

    val modifiers = mutableSetOf<Modifier>()

    if (isAltDown    ) modifiers += Alt
    if (isMetaDown   ) modifiers += Meta
    if (isShiftDown  ) modifiers += Shift
    if (isControlDown) modifiers += Ctrl

    return SystemPointerEvent(
            id                = 0,
            type              = type,
            location          = Point(x, y),
            buttons           = buttons,
            clickCount        = this.clickCount,
            modifiers         = modifiers,
            nativeScrollPanel = false
    )
}