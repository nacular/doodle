package com.nectar.doodle.deviceinput

import com.nectar.doodle.controls.panels.ScrollPanel
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.Cursor.Companion.Default
import com.nectar.doodle.system.MouseInputService
import com.nectar.doodle.system.SystemMouseEvent
import com.nectar.doodle.system.SystemMouseEvent.Type
import com.nectar.doodle.system.SystemMouseEvent.Type.Click
import com.nectar.doodle.system.SystemMouseEvent.Type.Down
import com.nectar.doodle.system.SystemMouseEvent.Type.Drag
import com.nectar.doodle.system.SystemMouseEvent.Type.Enter
import com.nectar.doodle.system.SystemMouseEvent.Type.Exit
import com.nectar.doodle.system.SystemMouseEvent.Type.Move
import com.nectar.doodle.system.SystemMouseEvent.Type.Up
import com.nectar.doodle.system.SystemMouseWheelEvent


class MouseInputManager(private val display: Display, private val inputService: MouseInputService): MouseInputService.Listener {

    private var mouseDown             = false
    private var clickedEventAwareView = null as View?
    private var coveredEventAwareView = null as View?
    private var coveredView           = null as View?
        set(new) {
            if (new == field) {
                return
            }

            field?.let { unregisterCursorListeners(it) }
            field = new
            field?.let { registerCursorListeners(it) }
        }

    private var cursor = null as Cursor?
        set(new) {
            field = new
            inputService.cursor = cursor ?: display.cursor ?: Default
        }

    init {
        inputService += this

        display.cursorChanged += { _,_,new -> cursor = new }

        cursor = display.cursor
    }

    override fun changed(event: SystemMouseEvent) {
        when (event.type) {
            Up -> when(event.clickCount) {
                1    -> mouseUp(event)
                else -> doubleClick(event)
            }

            Move -> mouseMove(event)
            Down -> mouseDown(event)
            else -> {}
        }
    }

    override fun changed(event: SystemMouseWheelEvent) = mouseScroll(event)

    private val cursorChanged_ = ::cursorChanged
    @Suppress("UNUSED_PARAMETER")
    private fun cursorChanged(view: View, old: Cursor?, new: Cursor?) {
//        if (old == null) {
//            view.parent?.let { unregisterCursorListeners(it) }
//        } else if (new == null) {
//            view.parent?.let { registerCursorListeners(it) }
//        }

        cursor = cursor(of = view)
    }

    private fun mouseUp(event: SystemMouseEvent) {
        var cursor: Cursor? = null
        val view = getMouseEventHandler(view(from = event))

        if (clickedEventAwareView != null || mouseDown) {

            clickedEventAwareView?.let {
                getMouseEventHandler(it)?.let {
                    it.handleMouseEvent_(createMouseEvent(event, it))

                    if (view === it) {
                        it.handleMouseEvent_(createMouseEvent(event, it, Click))
                    }

                    event.consume()
                }
            }

            if (view !== clickedEventAwareView) {
                clickedEventAwareView?.let {
                    it.handleMouseEvent_(createMouseEvent(event, it, Exit))
                }

                view?.let {
                    it.handleMouseEvent_(createMouseEvent(event, it, Enter))
                    it.handleMouseEvent_(createMouseEvent(event, it, Up   ))

                    cursor = cursor(of = it)

                    event.consume()
                }
            }

            clickedEventAwareView = null
        } else if (view != null) {
            view.handleMouseEvent_(createMouseEvent(event, view, Enter))
            view.handleMouseEvent_(createMouseEvent(event, view       ))

            cursor = cursor(of = view)

            event.consume()
        }

        this.cursor = cursor

        mouseDown = false

        // Work-around to re-synch state in case events were not delivered (due to consumption at the MouseInputService layer).
//        mouseMove(event)
    }

    private fun mouseDown(event: SystemMouseEvent) {
        inputService.toolTipText = ""

        getMouseEventHandler(view(from = event))?.let {
            it.handleMouseEvent_(createMouseEvent(event, it))

            clickedEventAwareView = it

            event.consume()
        }

        mouseDown = true
    }

    private fun doubleClick(event: SystemMouseEvent) {
        inputService.toolTipText = ""

        getMouseEventHandler(view(from = event))?.let {
            it.handleMouseEvent_(createMouseEvent(event, it, Up   ))
            it.handleMouseEvent_(createMouseEvent(event, it, Click))

            event.consume()
        }
    }

    private fun mouseMove(event: SystemMouseEvent) {
        coveredView = view(from = event)

        clickedEventAwareView?.let {
            if (it.monitorsMouseMotion) {
                it.handleMouseMotionEvent_(createMouseEvent(event, it, Drag))

                event.consume()
            }
        }

        val view = getMouseEventHandler(coveredView)

        if (view !== coveredEventAwareView) {
            coveredEventAwareView?.let {
                if (!mouseDown || it === clickedEventAwareView) {
                    getMouseEventHandler(it)?.let {
                        it.handleMouseEvent_(createMouseEvent(event, it, Exit))

                        event.consume()
                    }
                }
            }

            if (view != null) {
                if (!mouseDown || view === clickedEventAwareView) {
                    createMouseEvent(event, view, Enter).also {
                        view.handleMouseEvent_(it)

                        inputService.toolTipText = view.toolTipText(it)
                    }

                    cursor = cursor(of = coveredView)

                    event.consume()
                }
            } else if (clickedEventAwareView == null) {
                inputService.toolTipText = ""

                cursor = null
            }

            coveredEventAwareView = view

        } else if (!mouseDown) {
            if (coveredEventAwareView != null) {
                getMouseMotionEventHandler(coveredEventAwareView)?.let {
                    it.handleMouseMotionEvent_(createMouseEvent(event, it, Move))

                    event.consume()
                }
            } else {
                val handler = getMouseMotionEventHandler(coveredView)?.let {
                    it.handleMouseMotionEvent_(createMouseEvent(event, it, Move))

                    event.consume()
                }

                if (handler == null) {
                    inputService.toolTipText = ""
                }
            }

            cursor = cursor(of = coveredView)
        }

//        sScrollInputImpl.setCoveredView(coveredView)
    }

    private fun mouseScroll(event: SystemMouseWheelEvent) {
//        var view = sScrollInputImpl.getCoveredView()
//
//        if (view != null) {
//            view = getMouseWheelEventHandler(view)
//        } else {
//            view = getMouseWheelEventHandler(view(event.location))
//        }
//
//        if (view != null) {
//            val aAbsolutePosition = Coordinates.convertPointToScreen(Point.Origin, view)
//
//            val aNewMouseWheelEvent = MouseWheelEvent(view,
//                    event.location - aAbsolutePosition,
//                    event.xRotation,
//                    event.yRotation,
//                    event.modifiers)
//
//            view!!.handleMouseWheelEvent(aNewMouseWheelEvent)
//
//            event.consume()
//        }
    }

    private fun getMouseEventHandler      (view: View?) = findInHierarchy(view) { it.monitorsMouse       }
    private fun getMouseWheelEventHandler (view: View?) = findInHierarchy(view) { it.monitorsMouseWheel  }
    private fun getMouseMotionEventHandler(view: View?) = findInHierarchy(view) { it.monitorsMouseMotion }

    private fun findInHierarchy(view: View?, block: (View) -> Boolean): View? {
        var result: View? = view

        loop@ while (result != null) {
            result = when {
                !block(result) -> result.parent
                else           -> break@loop
            }
        }

        return result
    }

    private fun registerCursorListeners(view: View) {
        var value: View? = view

        while (value != null) {
            value.cursorChanged += cursorChanged_

            if (value.cursor != null) {
                break
            } else {
                value = value.parent
            }
        }
    }

    private fun unregisterCursorListeners(view: View) {
        var value: View? = view

        while (value != null) {
            value.cursorChanged -= cursorChanged_

//            if (value.cursor != null) {
//                break
//            } else {
                value = value.parent
//            }
        }
    }

    private fun cursor(of: View?) = when (display.cursor) {
        null -> of?.cursor
        else -> display.cursor
    }

    private fun view(from: SystemMouseEvent): View? {
        var newPoint = from.location
        var view     = display.child(at = from.location)

        while(view != null) {
            newPoint -= view.position

            view = view.child_(at = newPoint) ?: break
        }

        return view?.let {
            if (from.nativeScrollPanel) {
                while(view != null && view !is ScrollPanel) {
                    view = view?.parent
                }
            }

            view
        }
    }

    private fun createMouseEvent(mouseEvent: SystemMouseEvent, view: View, type: Type = mouseEvent.type) = MouseEvent(
            view,
            type,
            mouseEvent.location - view.toAbsolute(Origin),
            mouseEvent.buttons,
            mouseEvent.clickCount,
            mouseEvent.modifiers)
}
