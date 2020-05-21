package com.nectar.doodle.system.impl

import com.nectar.doodle.Document
import com.nectar.doodle.HTMLElement
import com.nectar.doodle.addEventListener
import com.nectar.doodle.dom.Event
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.MouseEvent
import com.nectar.doodle.dom.WheelEvent
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.removeEventListener
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.SystemInputEvent.Modifier
import com.nectar.doodle.system.SystemInputEvent.Modifier.Alt
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Meta
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift
import com.nectar.doodle.system.SystemMouseEvent
import com.nectar.doodle.system.SystemMouseEvent.Button.Button1
import com.nectar.doodle.system.SystemMouseEvent.Button.Button2
import com.nectar.doodle.system.SystemMouseEvent.Button.Button3
import com.nectar.doodle.system.SystemMouseEvent.Type
import com.nectar.doodle.system.SystemMouseEvent.Type.Down
import com.nectar.doodle.system.SystemMouseEvent.Type.Drag
import com.nectar.doodle.system.SystemMouseEvent.Type.Enter
import com.nectar.doodle.system.SystemMouseEvent.Type.Exit
import com.nectar.doodle.system.SystemMouseEvent.Type.Move
import com.nectar.doodle.system.SystemMouseEvent.Type.Up
import com.nectar.doodle.system.SystemMouseScrollEvent
import com.nectar.doodle.system.impl.MouseInputServiceStrategy.EventHandler
import com.nectar.doodle.utils.ifFalse
import com.nectar.doodle.utils.ifTrue


internal open class MouseInputServiceStrategyWebkit(private val document: Document, private val htmlFactory: HtmlFactory): MouseInputServiceStrategy {

    override var toolTipText: String = ""
        set(new) {
            field              = new
            inputDevice?.title = new
        }

    override var cursor: Cursor = Cursor.Default
        set(new) {
            if (new != field) {
                inputDevice?.style?.cursor = new.toString()

                field = new
            }
    }

    override var mouseLocation = Origin
        protected set

    private var inputDevice  = null as HTMLElement?
    private var eventHandler = null as EventHandler?

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

    private fun mouseEnter(event: MouseEvent) {
        eventHandler?.handle(createMouseEvent(event, Enter, 0))
    }

    private fun mouseExit(event: MouseEvent) {
        eventHandler?.handle(createMouseEvent(event, Exit, 0))
    }

    private fun mouseUp(event: MouseEvent): Boolean {
        eventHandler?.handle(createMouseEvent(event, Up, 1))

        return isNativeElement(event.target).ifFalse {
            event.preventDefault ()
            event.stopPropagation()
        }
    }

    private fun mouseDown(event: MouseEvent): Boolean {
        // Need to update location here in case running on a touch-based device; in which case mouseMove isn't called
        // unless touch is dragged
        mouseLocation = Point(event.pageX, event.pageY)

        eventHandler?.handle(createMouseEvent(event, Down, 1))

        return true
    }

    // TODO: Remove this and just rely on vanilla down/up events since you usually get a single up right before a double click up
    private fun doubleClick(event: MouseEvent): Boolean {
        eventHandler?.handle(createMouseEvent(event, Up, 2))

        return isNativeElement(event.target).ifFalse {
            event.preventDefault ()
            event.stopPropagation()
        }
    }

    private fun mouseMove(event: MouseEvent): Boolean {
        mouseLocation = Point(event.pageX, event.pageY)

        eventHandler?.handle(createMouseEvent(event, Move, 0))

        return true
    }

    private fun mouseScroll(event: WheelEvent): Boolean {
        val deltaX = 0 - event.deltaX / 28
        val deltaY = 0 - event.deltaY / 28

        val scrollEvent = SystemMouseScrollEvent(
                mouseLocation,
                deltaX,
                deltaY,
                createModifiers(event),
                nativeScrollPanel(event.target))

        eventHandler?.handle(scrollEvent)

        return !scrollEvent.consumed
    }

    private fun createMouseEvent(event: MouseEvent, type: Type, clickCount: Int): SystemMouseEvent {
        val buttons    = mutableSetOf<SystemMouseEvent.Button>()
        val buttonsInt = event.buttons.toInt()

        // Work-around for fact that touch sets buttons to 0
        if ((type == Down || type == Drag) && buttonsInt == 0 && event.button == 0.toShort()) buttons += Button1

        if (buttonsInt and 1 == 1) buttons += Button1
        if (buttonsInt and 2 == 2) buttons += Button2
        if (buttonsInt and 4 == 4) buttons += Button3

        return SystemMouseEvent(
                type,
                mouseLocation,
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
//        onwheel     = { mouseScroll(it) }

        onpointerdown = { mouseDown  (it); followMouse(this) }
        onpointerover = { mouseEnter (it) }

        onmouseout  = { mouseExit  (it) }
        ondblclick  = { doubleClick(it) }

        // TODO: Figure out fallback in case PointerEvent not present
//        onmousedown = { mouseDown  (it); followMouse(this) }
//        onmouseover = { mouseEnter (it) }

        registerMouseCallbacks(this)
    }

    private val trackingMouseMove: (Event) -> Unit = { mouseMove(it as MouseEvent) }
    private val trackingMouseUp  : (Event) -> Unit = { mouseUp  (it as MouseEvent); registerMouseCallbacks(htmlFactory.root) }

    private fun followMouse(element: HTMLElement): Unit = element.run {
        onpointerup   = null
        onpointermove = null

        document.addEventListener(pointerup,   trackingMouseUp  )
        document.addEventListener(pointermove, trackingMouseMove)

//        document.addEventListener(mouseup,   trackingMouseUp  )
//        document.addEventListener(mousemove, trackingMouseMove)
//        onmouseup   = null
//        onmousemove = null
    }

    private fun registerMouseCallbacks(element: HTMLElement) = element.run {
        onpointerup   = { mouseUp  (it) }
        onpointermove = { mouseMove(it) }

        document.removeEventListener(pointerup,   trackingMouseUp  )
        document.removeEventListener(pointermove, trackingMouseMove)

//        onmouseup   = { mouseUp  (it) }
//        onmousemove = { mouseMove(it) }
//        document.removeEventListener(mouseup,   trackingMouseUp  )
//        document.removeEventListener(mousemove, trackingMouseMove)
    }

    private fun unregisterCallbacks(element: HTMLElement) = element.run {
        onmouseout    = null
        ondblclick    = null
        onpointerdown = null
        onpointerover = null

        document.removeEventListener(pointerup,   trackingMouseUp  )
        document.removeEventListener(pointermove, trackingMouseMove)

//        onwheel       = null
//        onmouseup     = null
//        onmousedown   = null
//        onmousemove   = null
//        onmouseover   = null
//        document.removeEventListener(mouseup,   trackingMouseUp  )
//        document.removeEventListener(mousemove, trackingMouseMove)
    }

    companion object {
        const val pointerup   = "pointerup"
        const val pointermove = "pointermove"

//        const val mouseup   = "mouseup"
//        const val mousemove = "mousemove"
    }
}
