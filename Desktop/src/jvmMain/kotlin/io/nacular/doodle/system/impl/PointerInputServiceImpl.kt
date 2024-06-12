package io.nacular.doodle.system.impl

import io.nacular.doodle.application.CustomSkikoView
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
import org.jetbrains.skiko.SkikoGestureEvent
import org.jetbrains.skiko.SkikoInputModifiers.Companion.ALT
import org.jetbrains.skiko.SkikoInputModifiers.Companion.CONTROL
import org.jetbrains.skiko.SkikoInputModifiers.Companion.META
import org.jetbrains.skiko.SkikoInputModifiers.Companion.SHIFT
import org.jetbrains.skiko.SkikoMouseButtons
import org.jetbrains.skiko.SkikoPointerEvent
import org.jetbrains.skiko.SkikoPointerEventKind
import org.jetbrains.skiko.SkikoPointerEventKind.DOWN
import org.jetbrains.skiko.SkikoPointerEventKind.DRAG
import org.jetbrains.skiko.SkikoPointerEventKind.ENTER
import org.jetbrains.skiko.SkikoPointerEventKind.EXIT
import org.jetbrains.skiko.SkikoPointerEventKind.MOVE
import org.jetbrains.skiko.SkikoPointerEventKind.UP
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
import java.awt.event.MouseWheelEvent
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

    override fun getCursor(display: Display) = (display as? DisplayImpl)?.skiaLayer?.cursor?.doodle()

    override fun setCursor(display: Display, cursor: Cursor?) {
        (display as? DisplayImpl)?.skiaLayer?.cursor = cursor.swing()
    }

    // FIXME: This doesn't seem to work
    override fun getToolTipText(display: Display): String = (display as? DisplayImpl)?.skiaLayer?.toolTipText ?: ""

    // FIXME: This doesn't seem to work
    override fun setToolTipText(display: Display, text: String) {
        (display as? DisplayImpl)?.skiaLayer?.toolTipText = text
    }

    override fun addListener   (display: Display, listener: Listener) { listeners.getOrPut(display) { mutableSetOf() }.plusAssign (listener); if (listeners.size == 1) startUp() }
    override fun removeListener(display: Display, listener: Listener) { listeners[display]?.minusAssign(listener); shutdown() }

    override fun addPreprocessor   (display: Display, preprocessor: Preprocessor) { preprocessors.getOrPut(display) { mutableSetOf() }.plusAssign (preprocessor); if (preprocessors.size == 1) startUp() }
    override fun removePreprocessor(display: Display, preprocessor: Preprocessor) { preprocessors[display]?.minusAssign(preprocessor); shutdown()                             }

    private fun skikoPointerEvent(display: DisplayImpl, e: SkikoPointerEvent) {
        when (e.kind) {
            SkikoPointerEventKind.SCROLL -> handleMouseWheel  (display, e        )
            else                         -> notifyPointerEvent(display, e, e.type)
        }
    }

    private fun skikoGestureEvent(display: Display, e: SkikoGestureEvent) {
        // TODO: Implement
    }

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

    private fun setupDisplay(display: DisplayImpl) {
        (display.skiaLayer.skikoView as CustomSkikoView).apply {
            onPointerEvent = { skikoPointerEvent(display, it) }
            onGestureEvent = { skikoGestureEvent(display, it) }
        }

        // FIXME: This is currently needed b/c the canvas steals focus from native controls. Need to fix.
        display.skiaLayer.canvas.isFocusable = false
    }

    private fun teardownDisplay(display: DisplayImpl) {
        (display.skiaLayer.skikoView as CustomSkikoView).apply {
            onPointerEvent = {}
            onGestureEvent = {}
        }
    }

    private fun shutdown() {
        if (started && listeners.isEmpty() && preprocessors.isEmpty()) {
            started = false
        }
    }

    private fun notifyPointerEvent(display: Display, pointerEvent: SkikoPointerEvent, type: Type): Boolean {
        val event = pointerEvent.toDoodle(type)

        preprocessors[display]?.takeWhile { !event.consumed }?.forEach { it(event) }
        listeners    [display]?.takeWhile { !event.consumed }?.forEach { it(event) }

        return event.consumed
    }

    private fun handleMouseWheel(display: DisplayImpl, e: SkikoPointerEvent) {
        // TODO: Expose wheel events to View generally?
        if (nativeScrollHandlerFinder == null) {
            return
        }

        val wheelEvent = e.platform as MouseWheelEvent

        val absoluteLocation = wheelEvent.location(display.skiaLayer)

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

private val SkikoPointerEvent.type: Type get() = when (kind) {
    ENTER -> Enter
    EXIT  -> Exit
    DOWN  -> Down
    UP    -> Up
    DRAG  -> Type.Move
    MOVE  -> Type.Move
    else  -> Click
}

internal fun SkikoPointerEvent.toDoodle(type: Type = this.type): SystemPointerEvent {
    var buttons = when (this.button) {
        SkikoMouseButtons.BUTTON_1 -> setOf(Button1)
        SkikoMouseButtons.BUTTON_2 -> setOf(Button2)
        SkikoMouseButtons.BUTTON_3 -> setOf(Button3)
        else                       -> emptySet()
    }

    // FIXME: Change browser behavior to track released button instead of doing this
    if (type == Up) {
        buttons = emptySet()
    }

    val modifiers = mutableSetOf<Modifier>()

    if (this.modifiers.has(SHIFT  )) modifiers += Shift
    if (this.modifiers.has(ALT    )) modifiers += Alt
    if (this.modifiers.has(META   )) modifiers += Meta
    if (this.modifiers.has(CONTROL)) modifiers += Ctrl

    return SystemPointerEvent(
            id                = 0,
            type              = type,
            location          = Point(x, y),
            buttons           = buttons,
            clickCount        = this.platform?.clickCount ?: 0,
            modifiers         = modifiers,
            nativeScrollPanel = false)
}