package com.nectar.doodle.system.impl

import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.SystemInputEvent.Modifier
import com.nectar.doodle.system.SystemInputEvent.Modifier.Alt
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift
import com.nectar.doodle.system.SystemMouseEvent
import com.nectar.doodle.system.SystemMouseEvent.Button.Button1
import com.nectar.doodle.system.SystemMouseEvent.Button.Button2
import com.nectar.doodle.system.SystemMouseEvent.Button.Button3
import com.nectar.doodle.system.SystemMouseEvent.Type
import com.nectar.doodle.system.SystemMouseEvent.Type.Down
import com.nectar.doodle.system.SystemMouseEvent.Type.Enter
import com.nectar.doodle.system.SystemMouseEvent.Type.Exit
import com.nectar.doodle.system.SystemMouseEvent.Type.Move
import com.nectar.doodle.system.SystemMouseEvent.Type.Up
import com.nectar.doodle.system.SystemMouseWheelEvent
import com.nectar.doodle.system.impl.MouseInputServiceStrategy.EventHandler
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent

private val unitPoint = Point(1.0, 1.0)

internal class MouseInputServiceStrategyWebkit(private val htmlFactory: HtmlFactory): MouseInputServiceStrategy {

    override var toolTipText: String = ""
        set(new) {
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
        private set

//    private var overlay      = null as HTMLElement?
    private var inputDevice  = null as HTMLElement?
    private var eventHandler = null as EventHandler?

    override fun startUp(handler: EventHandler) {
        eventHandler = handler

        if (inputDevice == null) {
            inputDevice = htmlFactory.body.also {
                registerCallbacks(this, it)
            }
        }

//        if (overlay == null) {
//            overlay = htmlFactory.create<HTMLElement>().apply {
//                style.setOpacity(0f )
//                style.setWidth  (2.0)
//                style.setHeight (2.0)
//                style.zIndex  = "3"
//
////                htmlFactory.body.add(this)
//            }
//        }
    }

    override fun shutdown() {
        inputDevice?.let {
            unregisterCallbacks(it)

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

        if (isNativeElement(event.target as HTMLElement)) {
            return true
        }

        event.preventDefault ()
        event.stopPropagation()

        return false
    }

    private fun mouseDown(event: MouseEvent): Boolean {
        eventHandler?.handle(createMouseEvent(event, Down, 1))

        if (isNativeElement(event.target as HTMLElement)) {
            return true
        }

        event.preventDefault ()
        event.stopPropagation()

        return false
    }

    private fun doubleClick(event: MouseEvent): Boolean {
        eventHandler?.handle(createMouseEvent(event, Up, 2))

        if (isNativeElement(event.target as HTMLElement)) {
            return true
        }

        event.preventDefault ()
        event.stopPropagation()

        return false
    }

    private fun mouseMove(event: MouseEvent): Boolean {
        mouseLocation = Point(x = event.clientX + htmlFactory.body.scrollLeft,
                y = event.clientY + htmlFactory.body.scrollLeft)

        val isNativeElement = isNativeElement(event.target as HTMLElement)

//        if (!isNativeElement) {
//            overlay?.style?.translate(mouseLocation - unitPoint)
//        }

        eventHandler?.handle(createMouseEvent(event, Move, 0))

        if (isNativeElement) {
            return true
        }

        event.preventDefault ()
        event.stopPropagation()

        return false
    }

    private fun mouseScroll(event: WheelEvent): Boolean {
        val deltaX = 0 - event.deltaX / 28
        val deltaY = 0 - event.deltaY / 28

        val wheelEvent = SystemMouseWheelEvent(
                mouseLocation,
                deltaX.toInt(),
                deltaY.toInt(),
                createModifiers(event))

        eventHandler?.handle(wheelEvent)

        return !wheelEvent.consumed.also {
//            event.preventDefault ()
//            event.stopPropagation()
        }
    }

    private fun createMouseEvent(event: MouseEvent, aType: Type, clickCount: Int): SystemMouseEvent {
        val buttons    = mutableSetOf<SystemMouseEvent.Button>()
        val buttonsInt = event.buttons.toInt()

        if (buttonsInt and 1 == 1) buttons.add(Button1)
        if (buttonsInt and 2 == 2) buttons.add(Button2)
        if (buttonsInt and 4 == 4) buttons.add(Button3)

        return SystemMouseEvent(
                aType,
                Point(mouseLocation.x, mouseLocation.y),
                buttons,
                clickCount,
                createModifiers(event))
    }

    private fun createModifiers(event: MouseEvent) = mutableSetOf<Modifier>().apply {
        if (event.altKey) {
            add(Alt)
        }
        if (event.ctrlKey) {
            add(Ctrl)
        }
        if (event.shiftKey) {
            add(Shift)
        }
    }

    private fun registerCallbacks(handler: MouseInputServiceStrategyWebkit, element: HTMLElement) = element.apply {
        onmouseup   = { handler.mouseUp    (it as MouseEvent) }
        onmousedown = { handler.mouseDown  (it as MouseEvent) }
        onmousemove = { handler.mouseMove  (it as MouseEvent) }
        ondblclick  = { handler.doubleClick(it as MouseEvent) }
        onwheel     = { handler.mouseScroll(it as WheelEvent) }
        onmouseout  = { handler.mouseExit  (it as MouseEvent) }
        onmouseover = { handler.mouseEnter (it as MouseEvent) }
    }

    private fun unregisterCallbacks(element: HTMLElement) = element.apply {
        onmouseup   = null
        onmousedown = null
        onmousemove = null
        ondblclick  = null
        onwheel     = null
        onmouseout  = null
        onmouseover = null
    }
}
