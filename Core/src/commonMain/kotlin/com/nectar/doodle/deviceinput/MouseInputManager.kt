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


interface MouseInputManager {
    fun shutdown()
}

@Suppress("NestedLambdaShadowedImplicitParameter")
class MouseInputManagerImpl(
        private val display     : Display,
        private val viewFinder  : ViewFinder,
        private val inputService: MouseInputService): MouseInputManager, MouseInputService.Listener {

    private var mouseDown             = false
    private var clickedView           = null as View?
    private var clickedEventAwareView = null as View?
    private var coveredEventAwareView = null as View?
    private var coveredView           = null as View?
        set(new) {
            if (new == field) {
                return
            }

            field?.let { unregisterCursorListeners(it) }
            field = new
            field?.let { registerCursorListeners  (it) }
        }

    private var cursor = null as Cursor?
        set(new) {
            field = new
            inputService.cursor = cursor ?: display.cursor ?: Default
        }

    private val displayCursorChanged = { _: Display, _: Cursor?, new: Cursor? -> cursor = new }

    private val viewCursorChanged = { view: View, _: Cursor?, _: Cursor? ->
        cursor = cursor(of = view)
    }


    init {
        inputService += this

        display.cursorChanged += displayCursorChanged

        cursor = display.cursor
    }

    override fun shutdown() {
        inputService -= this

        display.cursorChanged -= displayCursorChanged
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

    private fun mouseUp(event: SystemMouseEvent) {
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
                    // Avoid case where mouse-move hasn't been seen (possible if drag-drop happened)
                    if (coveredEventAwareView == it) {
                        coveredEventAwareView = null

                        it.handleMouseEvent_(createMouseEvent(event, it, Exit))
                    }
                }

                if (view != null) {
                    coveredEventAwareView = view
                    view.handleMouseEvent_(createMouseEvent(event, view, Enter))
                    view.handleMouseEvent_(createMouseEvent(event, view, Up   ))

                    cursor = cursor(of = view)

                    event.consume()
                } else {
                    cursor = display.cursor
                }
            } else {
                cursor = cursor(of = view)
            }

            clickedEventAwareView = null
        } else if (view != null) {
            view.handleMouseEvent_(createMouseEvent(event, view, Enter))
            view.handleMouseEvent_(createMouseEvent(event, view       ))

            cursor = cursor(of = view)

            event.consume()
        } else {
            cursor = display.cursor
        }

        mouseDown = false

        // Work-around to re-synch state in case events were not delivered (due to consumption at the MouseInputService layer).
//        mouseMove(event)
    }

    private fun mouseDown(event: SystemMouseEvent) {
        inputService.toolTipText = ""

        view(from = event)?.let {
            val mouseEvent = createMouseEvent(event, it).apply { clickedView = it }

            getMouseEventHandler(it)?.let {
                it.handleMouseEvent_(mouseEvent)

                clickedEventAwareView = it
                coveredEventAwareView = it

                event.consume()
            }
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

        clickedEventAwareView?.let { view ->
            if (view.monitorsMouseMotion) {
                view.handleMouseMotionEvent_(createMouseEvent(event, view, Drag))

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
        }

        if (!mouseDown) {
            val handler = getMouseMotionEventHandler(coveredView)?.let {
                it.handleMouseMotionEvent_(createMouseEvent(event, it, Move))

                event.consume()
            }

            if (handler == null) {
                inputService.toolTipText = ""
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
            value.cursorChanged += viewCursorChanged

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
            value.cursorChanged -= viewCursorChanged

            value = value.parent
        }
    }

    private fun cursor(of: View?) = when (display.cursor) {
        null -> of?.cursor
        else -> display.cursor
    }

    private fun view(from: SystemMouseEvent): View? {
        var view = viewFinder.find(from.location)

        return view?.let {
            if (from.nativeScrollPanel) {
                while(view != null && view !is ScrollPanel) {
                    view = view?.parent
                }
            }

            view
        }
    }

    private fun createMouseEvent(event: SystemMouseEvent, view: View, type: Type = event.type) = MouseEvent(
            view,
            type,
            event.location - view.toAbsolute(Origin),
            event.buttons,
            event.clickCount,
            event.modifiers)
}