package io.nacular.doodle.system.impl

import io.nacular.doodle.core.View
import io.nacular.doodle.deviceinput.ViewFinder
import io.nacular.doodle.geometry.Point
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
import io.nacular.doodle.system.Cursor.Companion.SResize
import io.nacular.doodle.system.Cursor.Companion.SeResize
import io.nacular.doodle.system.Cursor.Companion.SwResize
import io.nacular.doodle.system.Cursor.Companion.Text
import io.nacular.doodle.system.Cursor.Companion.WResize
import io.nacular.doodle.system.Cursor.Companion.Wait
import io.nacular.doodle.system.Cursor.Companion.custom
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
import io.nacular.doodle.system.SystemPointerEvent.Type.Drag
import io.nacular.doodle.system.SystemPointerEvent.Type.Enter
import io.nacular.doodle.system.SystemPointerEvent.Type.Exit
import io.nacular.doodle.system.SystemPointerEvent.Type.Up
import org.jetbrains.skiko.SkiaWindow
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
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import java.awt.Cursor as AwtCursor

internal interface NativeScrollHandler {
    operator fun invoke(event: MouseWheelEvent, pointInTarget: Point)
}

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
        private val window                   : SkiaWindow,
        private val viewFinder               : ViewFinder,
        private val nativeScrollHandlerFinder: NativeScrollHandlerFinder?
): PointerInputService, MouseAdapter(), MouseWheelListener, MouseMotionListener {
    private var started       = false
    private val listeners     = mutableSetOf<Listener>()
    private val preprocessors = mutableSetOf<Preprocessor>()

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
//        Progress  ->
        NeResize  -> AwtCursor(NE_RESIZE_CURSOR)
        NwResize  -> AwtCursor(NW_RESIZE_CURSOR)
        SeResize  -> AwtCursor(SE_RESIZE_CURSOR)
        SwResize  -> AwtCursor(SW_RESIZE_CURSOR)
        Crosshair -> AwtCursor(CROSSHAIR_CURSOR)
//        ColResize ->
//        RowResize ->
        else      -> null
    }

    override var cursor: Cursor?
        get() = when (window.layeredPane.cursor.type) {
            TEXT_CURSOR      -> Text
            WAIT_CURSOR      -> Wait
            MOVE_CURSOR      -> Move
            HAND_CURSOR      -> Grab
            DEFAULT_CURSOR   -> Default
            N_RESIZE_CURSOR  -> NResize
            S_RESIZE_CURSOR  -> SResize
            E_RESIZE_CURSOR  -> EResize
            W_RESIZE_CURSOR  -> WResize
            NE_RESIZE_CURSOR -> NeResize
            NW_RESIZE_CURSOR -> NwResize
            SE_RESIZE_CURSOR -> SeResize
            SW_RESIZE_CURSOR -> SwResize
            CROSSHAIR_CURSOR -> Crosshair
            else             -> custom(window.layeredPane.cursor.name, or = Default)
        }
        set(new) {
            window.layer.cursor = new.swing()
        }

    override var toolTipText: String
        get(   ) = window.layeredPane.toolTipText
        set(new) {
            // FIXME: This doesn't work
            window.layeredPane.toolTipText = new
        }

    override operator fun plusAssign (listener: Listener) { listeners.plusAssign (listener); if (listeners.size == 1) startUp() }
    override operator fun minusAssign(listener: Listener) { listeners.minusAssign(listener); shutdown()                         }

    override operator fun plusAssign (preprocessor: Preprocessor) { preprocessors.plusAssign (preprocessor); if (preprocessors.size == 1) startUp() }
    override operator fun minusAssign(preprocessor: Preprocessor) { preprocessors.minusAssign(preprocessor); shutdown()                             }

    override fun mousePressed(e: MouseEvent) {
        notifyPointerEvent(e, Down)
    }

    override fun mouseReleased(e: MouseEvent) {
        notifyPointerEvent(e, Up)
    }

    override fun mouseMoved(e: MouseEvent) {
        notifyPointerEvent(e, Type.Move)
    }

    override fun mouseDragged(e: MouseEvent) {
        notifyPointerEvent(e, Type.Move)
    }

    override fun mouseWheelMoved(e: MouseWheelEvent) {
        // TODO: Expose wheel events to View generally?
        if (nativeScrollHandlerFinder == null) {
            return
        }

        val absoluteLocation = e.location(window)

        viewFinder.find(absoluteLocation)?.let {
            var target = it as View?

            while (target != null) {
                val handler = nativeScrollHandlerFinder[target]

                if (handler != null) {
                    handler(e, target.fromAbsolute(absoluteLocation))

                    break
                }

                target = target.parent
            }
        }
    }

    private fun startUp() {
        if (!started) {
            window.layer.addMouseListener       (this)
            window.layer.addMouseWheelListener  (this)
            window.layer.addMouseMotionListener (this)

            // FIXME: This is currently needed b/c the canvas steals focus from native controls. Need to fix.
            window.layer.canvas.isFocusable = false

            started = true
        }
    }

    private fun shutdown() {
        if (started && listeners.isEmpty() && preprocessors.isEmpty()) {
            window.layer.removeMouseListener      (this)
            window.layer.removeMouseWheelListener (this)
            window.layer.removeMouseMotionListener(this)

            started = false
        }
    }

    private fun notifyPointerEvent(mouseEvent: MouseEvent, type: Type): Boolean {
        val event = mouseEvent.toDoodle(window, type)

        preprocessors.takeWhile { !event.consumed }.forEach { it.preprocess(event) }
        listeners.takeWhile     { !event.consumed }.forEach { it.changed   (event) }

        return event.consumed
    }
}

private val MouseEvent.type: Type get() = when (id) {
    MouseEvent.MOUSE_ENTERED  -> Enter
    MouseEvent.MOUSE_EXITED   -> Exit
    MouseEvent.MOUSE_PRESSED  -> Down
    MouseEvent.MOUSE_RELEASED -> Up
    MouseEvent.MOUSE_CLICKED  -> Click
    MouseEvent.MOUSE_DRAGGED  -> Drag
    else                      -> Type.Move
}

internal fun MouseEvent.toDoodle(window: SkiaWindow, type: Type = this.type): SystemPointerEvent {
    var buttons = when (button) {
        1    -> setOf(Button1)
        2    -> setOf(Button2)
        3    -> setOf(Button3)
        else -> emptySet()
    }

    // FIXME: Change browser behavior to track released button instead of doing this
    if (type == Up) {
        buttons = emptySet()
    }

    val modifiers = mutableSetOf<Modifier>()

    if (isShiftDown  ) modifiers += Shift
    if (isAltDown    ) modifiers += Alt
    if (isMetaDown   ) modifiers += Meta
    if (isControlDown) modifiers += Ctrl

    return SystemPointerEvent(
            id                = 0,
            type              = type,
            location          = location(window),
            buttons           = buttons,
            clickCount        = clickCount,
            modifiers         = modifiers,
            nativeScrollPanel = false)
}