package io.nacular.doodle.system.impl

import io.nacular.doodle.Document
import io.nacular.doodle.HTMLElement
import io.nacular.doodle.addActiveEventListener
import io.nacular.doodle.addEventListener
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.MouseEvent
import io.nacular.doodle.dom.PointerEvent
import io.nacular.doodle.dom.TouchEvent
import io.nacular.doodle.dom.setCursor
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.removeActiveEventListener
import io.nacular.doodle.removeEventListener
import io.nacular.doodle.system.Cursor
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
import io.nacular.doodle.system.SystemPointerEvent.Type.Down
import io.nacular.doodle.system.SystemPointerEvent.Type.Drag
import io.nacular.doodle.system.SystemPointerEvent.Type.Enter
import io.nacular.doodle.system.SystemPointerEvent.Type.Exit
import io.nacular.doodle.system.SystemPointerEvent.Type.Move
import io.nacular.doodle.system.SystemPointerEvent.Type.Up
import io.nacular.doodle.system.impl.PointerInputServiceStrategy.EventHandler
import io.nacular.doodle.utils.ifFalse
import io.nacular.doodle.utils.ifTrue

internal class PointerLocationResolverImpl(private val document: Document, private val htmlFactory: HtmlFactory): PointerLocationResolver {
    var nested = false

    override fun invoke(event: MouseEvent): Point = when {
        // FIXME: This does not solve for cases where apps have transforms
        /*!nested &&*/ htmlFactory.root != document.body -> {
            val rect = htmlFactory.root.getBoundingClientRect()

            Point(event.clientX - rect.x, event.clientY - rect.y)
        }
        else -> Point(event.clientX, event.clientY)
    }
}

internal open class PointerInputServiceStrategyWebkit(
        private val document               : Document,
        private val htmlFactory            : HtmlFactory,
        private val pointerLocationResolver: PointerLocationResolver
): PointerInputServiceStrategy {

    override var toolTipText: String = ""; set(new) {
        field              = new
        inputDevice?.title = new
    }

    override var cursor: Cursor? = null; set(new) {
        if (new != field) {
            inputDevice?.style?.setCursor(new)

            field = new
        }
    }

    // tracks previous up event pointerId so it can continue on to double-click
    // which has no ID since it is a MouseEvent
    private var lastUpId           = -1
    private var inputDevice        = null as HTMLElement?
    private var eventHandler       = null as EventHandler?
    private val preventScroll      = mutableSetOf<Int>()
    private var lastUpIsPointer    = false
    private var preventContextMenu = null as Int?

    override fun startUp(handler: EventHandler) {
        eventHandler = handler

        if (inputDevice == null) {
            // TODO: Should we listen to the document here to enable better integration with nested applications?
            inputDevice = htmlFactory.root.also {
                registerCallbacks(it)
            }
        }
    }

    override fun shutdown() {
        inputDevice?.let {
            unregisterCallbacks(it)

            it.style.cursor = ""

            inputDevice = null
        }
    }

    private fun pointerEnter(event: PointerEvent) {
        eventHandler?.invoke(createPointerEvent(event, Enter, 0))
    }

    private fun pointerCancel(event: PointerEvent) {
        preventScroll -= event.pointerId

        // Clean-up context menu prevention initiated by the pointer-down for this event
        if (event.pointerId == preventContextMenu) {
            preventContextMenu = null
        }

        eventHandler?.invoke(createPointerEvent(event, Exit, 0))
    }

    private fun pointerUp(event: PointerEvent): Boolean {
        lastUpId         = event.pointerId
        preventScroll   -= event.pointerId
        lastUpIsPointer  = (event as? PointerEvent)?.pointerType == "touch"

        eventHandler?.invoke(createPointerEvent(event, Up, 1))

        // Fake exit when dealing w/ touch event
        if (lastUpIsPointer) {
            eventHandler?.invoke(createPointerEvent(event, Exit, 1))
        }

        return isNativeElement(event.target).ifFalse {
            event.preventBrowserDefault()
        }
    }

    private fun pointerDown(event: PointerEvent) = true.also {
        val systemPointerEvent = createPointerEvent(event, Down, 1)

        if (eventHandler?.invoke(systemPointerEvent) == true) {
            preventScroll += event.pointerId

            if (Button2 in systemPointerEvent.buttons) {
                preventContextMenu = event.pointerId
            }
        }
    }

    // TODO: Remove this and just rely on vanilla down/up events since you usually get a single up right before a double click up
    private fun doubleClick(event: MouseEvent): Boolean {
        eventHandler?.invoke(createPointerEvent(event, lastUpId, Up, 2))

        // Fake exit when dealing w/ touch event
        if (lastUpIsPointer) {
            eventHandler?.invoke(createPointerEvent(event, lastUpId, Exit, 2))
        }

        return isNativeElement(event.target).ifFalse {
            event.preventBrowserDefault()
        }
    }

    private fun pointerMove(event: PointerEvent) = true.also {
        eventHandler?.invoke(createPointerEvent(event, Move, 0))
    }

    private fun contextMenu(event: MouseEvent) = (preventContextMenu == null).ifFalse {
        event.preventBrowserDefault()
        preventContextMenu = null
    }

    private fun createPointerEvent(event: PointerEvent, type: Type, clickCount: Int) = createPointerEvent(event, event.pointerId, type, clickCount)

    private fun createPointerEvent(event: MouseEvent, id: Int, type: Type, clickCount: Int): SystemPointerEvent {
        val buttons    = mutableSetOf<SystemPointerEvent.Button>()
        val buttonsInt = event.buttons.toInt()

        // Work-around for fact that touch sets buttons to 0
        if ((type == Down || type == Drag) && buttonsInt == 0 && event.button == 0.toShort()) buttons += Button1

        if (buttonsInt and 1 == 1) buttons += Button1
        if (buttonsInt and 2 == 2) buttons += Button2
        if (buttonsInt and 4 == 4) buttons += Button3

        return SystemPointerEvent(
                id,
                type,
                pointerLocationResolver(event),
                buttons,
                clickCount,
                createModifiers(event),
                nativeScrollPanel(event.target))
    }

    private fun createModifiers(event: MouseEvent) = mutableSetOf<Modifier>().also {
        event.altKey.ifTrue   { it += Alt   }
        event.ctrlKey.ifTrue  { it += Ctrl  }
        event.metaKey.ifTrue  { it += Meta  }
        event.shiftKey.ifTrue { it += Shift }
    }

    private fun registerCallbacks(element: HTMLElement) = element.run {
        // TODO: Figure out fallback in case PointerEvent not present
        ondblclick      = { doubleClick  (it)                      }
        onpointerdown   = { pointerDown  (it); followPointer(this) }
        onpointerover   = { pointerEnter (it)                      }
        onpointercancel = { pointerCancel(it)                      }

        addActiveEventListener(TOUCH_MOVE,    preventTouchDefault     )
        addActiveEventListener(TOUCH_START,   preventTouchStartDefault)
        addActiveEventListener(GESTURE_START, preventTouchDefault     )

        registerPointerCallbacks(this)
    }

    private val preventTouchDefault: (Event) -> Unit = {
        if (preventScroll.isNotEmpty()) {
            it.preventBrowserDefault()
        }
    }

    private val preventTouchStartDefault: (Event) -> Unit = {
        if ((it as TouchEvent).touches.length > 1) preventTouchDefault(it)
    }

    private fun Event.preventBrowserDefault() {
        preventDefault ()
        stopPropagation()
    }

    private val trackingPointerUp  : (Event) -> Unit = { pointerUp  (it as PointerEvent); registerPointerCallbacks(htmlFactory.root) }
    private val trackingPointerMove: (Event) -> Unit = { pointerMove(it as PointerEvent) }
    private val trackingContextMenu: (Event) -> Unit = { contextMenu(it as MouseEvent  ) }

    private fun followPointer(element: HTMLElement): Unit = element.run {
        onpointerup   = null
        onpointermove = null
        oncontextmenu = null

        document.addEventListener(POINTER_UP,   trackingPointerUp  )
        document.addEventListener(POINTER_MOVE, trackingPointerMove)
        document.addEventListener(CONTEXT_MENU, trackingContextMenu)
    }

    private fun registerPointerCallbacks(element: HTMLElement) = element.run {
        onpointerup   = { pointerUp  (it) }
        onpointermove = { pointerMove(it) }
        oncontextmenu = { contextMenu(it) }

        document.removeEventListener(POINTER_UP,   trackingPointerUp  )
        document.removeEventListener(POINTER_MOVE, trackingPointerMove)
        document.removeEventListener(CONTEXT_MENU, trackingContextMenu)
    }

    private fun unregisterCallbacks(element: HTMLElement) = element.run {
        ondblclick      = null
        onpointerup     = null
        onpointermove   = null
        oncontextmenu   = null
        onpointerdown   = null
        onpointerover   = null
        onpointercancel = null

        removeActiveEventListener(TOUCH_MOVE,    preventTouchDefault     )
        removeActiveEventListener(TOUCH_START,   preventTouchStartDefault)
        removeActiveEventListener(GESTURE_START, preventTouchDefault     )

        document.removeEventListener(POINTER_UP,   trackingPointerUp  )
        document.removeEventListener(POINTER_MOVE, trackingPointerMove)
        document.removeEventListener(CONTEXT_MENU, trackingContextMenu)
    }

    private companion object {
        private const val TOUCH_MOVE    = "touchmove"
        private const val POINTER_UP    = "pointerup"
        private const val TOUCH_START   = "touchstart"
        private const val POINTER_MOVE  = "pointermove"
        private const val CONTEXT_MENU  = "contextmenu"
        private const val GESTURE_START = "gesturestart"
    }
}
