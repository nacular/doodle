package com.nectar.doodle.system.impl

import com.nectar.doodle.Document
import com.nectar.doodle.HTMLElement
import com.nectar.doodle.addEventListener
import com.nectar.doodle.dom.Event
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.MouseEvent
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.removeEventListener
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.SystemInputEvent.Modifier
import com.nectar.doodle.system.SystemInputEvent.Modifier.Alt
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Meta
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift
import com.nectar.doodle.system.SystemPointerEvent
import com.nectar.doodle.system.SystemPointerEvent.Button.Button1
import com.nectar.doodle.system.SystemPointerEvent.Button.Button2
import com.nectar.doodle.system.SystemPointerEvent.Button.Button3
import com.nectar.doodle.system.SystemPointerEvent.Type
import com.nectar.doodle.system.SystemPointerEvent.Type.Down
import com.nectar.doodle.system.SystemPointerEvent.Type.Drag
import com.nectar.doodle.system.SystemPointerEvent.Type.Enter
import com.nectar.doodle.system.SystemPointerEvent.Type.Exit
import com.nectar.doodle.system.SystemPointerEvent.Type.Move
import com.nectar.doodle.system.SystemPointerEvent.Type.Up
import com.nectar.doodle.system.impl.PointerInputServiceStrategy.EventHandler
import com.nectar.doodle.utils.ifFalse
import com.nectar.doodle.utils.ifTrue


internal open class PointerInputServiceStrategyWebkit(private val document: Document, private val htmlFactory: HtmlFactory): PointerInputServiceStrategy {

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

    override var pointerLocation = Origin
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
        eventHandler?.handle(createPointerEvent(event, Enter, 0))
    }

    private fun mouseExit(event: MouseEvent) {
        eventHandler?.handle(createPointerEvent(event, Exit, 0))
    }

    private fun mouseUp(event: MouseEvent): Boolean {
        eventHandler?.handle(createPointerEvent(event, Up, 1))

        return isNativeElement(event.target).ifFalse {
            event.preventDefault ()
            event.stopPropagation()
        }
    }

    private fun mouseDown(event: MouseEvent): Boolean {
        // Need to update location here in case running on a touch-based device; in which case mouseMove isn't called
        // unless touch is dragged
        pointerLocation = Point(event.pageX, event.pageY)

        eventHandler?.handle(createPointerEvent(event, Down, 1))

        return true
    }

    // TODO: Remove this and just rely on vanilla down/up events since you usually get a single up right before a double click up
    private fun doubleClick(event: MouseEvent): Boolean {
        eventHandler?.handle(createPointerEvent(event, Up, 2))

        return isNativeElement(event.target).ifFalse {
            event.preventDefault ()
            event.stopPropagation()
        }
    }

    private fun mouseMove(event: MouseEvent): Boolean {
        pointerLocation = Point(event.pageX, event.pageY)

        eventHandler?.handle(createPointerEvent(event, Move, 0))

        return true
    }

    private fun createPointerEvent(event: MouseEvent, type: Type, clickCount: Int): SystemPointerEvent {
        val buttons    = mutableSetOf<SystemPointerEvent.Button>()
        val buttonsInt = event.buttons.toInt()

        // Work-around for fact that touch sets buttons to 0
        if ((type == Down || type == Drag) && buttonsInt == 0 && event.button == 0.toShort()) buttons += Button1

        if (buttonsInt and 1 == 1) buttons += Button1
        if (buttonsInt and 2 == 2) buttons += Button2
        if (buttonsInt and 4 == 4) buttons += Button3

        return SystemPointerEvent(
                type,
                pointerLocation,
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

        onmouseout    = { mouseExit  (it)                      }
        ondblclick    = { doubleClick(it)                      }
        onpointerdown = { mouseDown  (it); followPointer(this) }
        onpointerover = { mouseEnter (it)                      }

        registerMouseCallbacks(this)
    }

    private val trackingMouseMove: (Event) -> Unit = { mouseMove(it as MouseEvent) }
    private val trackingMouseUp  : (Event) -> Unit = { mouseUp  (it as MouseEvent); registerMouseCallbacks(htmlFactory.root) }

    private fun followPointer(element: HTMLElement): Unit = element.run {
        onpointerup   = null
        onpointermove = null

        document.addEventListener(POINTER_UP,   trackingMouseUp  )
        document.addEventListener(POINTER_MOVE, trackingMouseMove)
    }

    private fun registerMouseCallbacks(element: HTMLElement) = element.run {
        onpointerup   = { mouseUp  (it) }
        onpointermove = { mouseMove(it) }

        document.removeEventListener(POINTER_UP,   trackingMouseUp  )
        document.removeEventListener(POINTER_MOVE, trackingMouseMove)
    }

    private fun unregisterCallbacks(element: HTMLElement) = element.run {
        onmouseout    = null
        ondblclick    = null
        onpointerdown = null
        onpointerover = null

        document.removeEventListener(POINTER_UP,   trackingMouseUp  )
        document.removeEventListener(POINTER_MOVE, trackingMouseMove)
    }

    private companion object {
        private const val POINTER_UP   = "pointerup"
        private const val POINTER_MOVE = "pointermove"
    }
}
