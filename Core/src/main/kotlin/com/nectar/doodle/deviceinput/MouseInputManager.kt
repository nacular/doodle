package com.nectar.doodle.deviceinput

import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.MouseInputService
import com.nectar.doodle.system.SystemMouseEvent
import com.nectar.doodle.system.SystemMouseEvent.Type.Click
import com.nectar.doodle.system.SystemMouseEvent.Type.Down
import com.nectar.doodle.system.SystemMouseEvent.Type.Drag
import com.nectar.doodle.system.SystemMouseEvent.Type.Enter
import com.nectar.doodle.system.SystemMouseEvent.Type.Exit
import com.nectar.doodle.system.SystemMouseEvent.Type.Move
import com.nectar.doodle.system.SystemMouseEvent.Type.Up
import com.nectar.doodle.system.SystemMouseWheelEvent


class MouseInputManager(private val display: Display, private val inputService: MouseInputService): MouseInputService.Listener {

    private var mouseDown              = false
    private var clickedEventAwareGizmo = null as Gizmo?
    private var coveredEventAwareGizmo = null as Gizmo?

    init {
        inputService += this

//        mDisplay.addPropertyListener(object : PropertyListener() {
//            fun propertyChanged(aPropertyEvent: PropertyEvent) {
//                if (aPropertyEvent.getProperty() === Gizmo.CURSOR) {
//                    this@MouseInputManager.setCursor(aPropertyEvent.getNewValue() as Cursor)
//                }
//            }
//        })
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

    override fun changed(event: SystemMouseWheelEvent) {
        mouseScroll(event)
    }

//    fun propertyChanged(aEvent: PropertyEvent) {
//        val aSource = aEvent.getSource() as Gizmo
//
//        if (aEvent.getProperty() === Gizmo.CURSOR) {
//            if (aEvent.getOldValue() == null) {
//                unregisterPropertyListeners(aSource.getParent())
//            } else if (aEvent.getNewValue() == null) {
//                registerPropertyListeners(aSource.getParent())
//            }
//
//            setCursor(getGizmoCursor(aSource))
//        }
//    }

    private fun mouseUp(event: SystemMouseEvent) {
        var cursor: Cursor? = null
        val gizmo = getMouseEventHandler(gizmo(event.location))

        if (clickedEventAwareGizmo != null || mouseDown) {

            clickedEventAwareGizmo?.let {
                getMouseEventHandler(it)?.let {
                    it.handleMouseEvent_(createMouseEvent(event, it))

                    if (gizmo === it) {
                        it.handleMouseEvent_(createMouseEvent(event, it, Click))
                    }

                    event.consume()
                }
            }

            if (gizmo !== clickedEventAwareGizmo) {
                clickedEventAwareGizmo?.let {
                    it.handleMouseEvent_(createMouseEvent(event, it, Exit))
                }

                gizmo?.let {
                    it.handleMouseEvent_(createMouseEvent(event, it, Enter))

                    cursor = getGizmoCursor(it)

                    event.consume()
                }
            }

            clickedEventAwareGizmo = null
        } else if (gizmo != null) {
            gizmo.handleMouseEvent_(createMouseEvent(event, gizmo))

            cursor = getGizmoCursor(gizmo)

            event.consume()
        }

        this.cursor = cursor

        mouseDown = false

        // Work-around to re-synch state in case events were not delivered (due to consumption at the MouseInputService layer).

        mouseMove(event)
    }

    private fun mouseDown(event: SystemMouseEvent) {
        inputService.toolTipText = ""

        getMouseEventHandler(gizmo(event.location))?.let {
            it.handleMouseEvent_(createMouseEvent(event, it))

            clickedEventAwareGizmo = it

            event.consume()
        }

        mouseDown = true
    }

    private fun doubleClick(event: SystemMouseEvent) {
        inputService.toolTipText = ""

        getMouseEventHandler(gizmo(event.location))?.let {
            it.handleMouseEvent_(createMouseEvent(event, it, Up   ))
            it.handleMouseEvent_(createMouseEvent(event, it, Click))

            event.consume()
        }
    }

    private fun mouseMove(event: SystemMouseEvent) {
        val coveredGizmo = gizmo(event.location)

        clickedEventAwareGizmo?.let {
            if (it.monitorsMouseMotion) {
                it.handleMouseMotionEvent_(createMouseEvent(event, it, Drag))

                event.consume()
            }
        }

        val gizmo = getMouseEventHandler(coveredGizmo)

        if (gizmo !== coveredEventAwareGizmo) {
            coveredEventAwareGizmo?.let {
                if (!mouseDown || it === clickedEventAwareGizmo) {
                    getMouseEventHandler(it)?.let {
                        it.handleMouseEvent_(createMouseEvent(event, it, Exit))

                        event.consume()
                    }
                }

//                unregisterPropertyListeners(coveredEventAwareGizmo)
            }

            if (gizmo != null) {
                if (!mouseDown || gizmo === clickedEventAwareGizmo) {
                    val enterEvent = createMouseEvent(event, gizmo, Enter)

                    gizmo.handleMouseEvent_(enterEvent)

                    inputService.toolTipText = gizmo.toolTipText(enterEvent)

                    cursor = getGizmoCursor(coveredGizmo)

                    event.consume()
                }
            } else if (clickedEventAwareGizmo == null) {
                inputService.toolTipText = ""

                cursor = null
            }

            coveredEventAwareGizmo = gizmo

            if (coveredEventAwareGizmo != null) {
//                registerPropertyListeners(coveredEventAwareGizmo)
            }
        } else if (!mouseDown) {
            if (coveredEventAwareGizmo != null) {
                val handler = getMouseMotionEventHandler(coveredEventAwareGizmo)

                if (handler != null) {
                    handler.handleMouseMotionEvent_(createMouseEvent(event, handler, Move))

                    event.consume()
                }
            } else {
                inputService.toolTipText = ""
            }

            cursor = getGizmoCursor(coveredGizmo)
        }

//        sScrollInputImpl.setCoveredGizmo(coveredGizmo)
    }

    private fun mouseScroll(event: SystemMouseWheelEvent) {
//        var gizmo = sScrollInputImpl.getCoveredGizmo()
//
//        if (gizmo != null) {
//            gizmo = getMouseWheelEventHandler(gizmo)
//        } else {
//            gizmo = getMouseWheelEventHandler(gizmo(event.location))
//        }
//
//        if (gizmo != null) {
//            val aAbsolutePosition = Coordinates.convertPointToScreen(Point.Origin, gizmo)
//
//            val aNewMouseWheelEvent = MouseWheelEvent(gizmo,
//                    event.location - aAbsolutePosition,
//                    event.xRotation,
//                    event.yRotation,
//                    event.modifiers)
//
//            gizmo!!.handleMouseWheelEvent(aNewMouseWheelEvent)
//
//            event.consume()
//        }
    }

    private fun getMouseEventHandler      (gizmo: Gizmo?) = findInHierarchy(gizmo) { it.monitorsMouse       }
    private fun getMouseWheelEventHandler (gizmo: Gizmo?) = findInHierarchy(gizmo) { it.monitorsMouseWheel  }
    private fun getMouseMotionEventHandler(gizmo: Gizmo?) = findInHierarchy(gizmo) { it.monitorsMouseMotion }

    private fun findInHierarchy(gizmo: Gizmo?, block: (Gizmo) -> Boolean): Gizmo? {
        var result: Gizmo? = gizmo

        loop@ while (result != null) {
            result = when {
                !block(result) -> result.parent
                else           -> break@loop
            }
        }

        return result
    }

//    private fun registerPropertyListeners(gizmo: Gizmo?) {
//        var gizmo = gizmo
//        while (gizmo != null && gizmo!!.getParent() != null) {
//            gizmo!!.addPropertyListener(this)
//
//            if (gizmo!!.getCursor() != null) {
//                break
//            } else {
//                gizmo = gizmo!!.getParent()
//            }
//        }
//    }
//
//    private fun unregisterPropertyListeners(gizmo: Gizmo?) {
//        var gizmo = gizmo
//        while (gizmo != null && gizmo!!.getParent() != null) {
//            gizmo!!.removePropertyListener(this)
//
//            if (gizmo!!.getCursor() != null) {
//                break
//            } else {
//                gizmo = gizmo!!.getParent()
//            }
//        }
//    }

    private fun getGizmoCursor(gizmo: Gizmo?) = when (display.cursor) {
        null -> gizmo?.cursor
        else -> display.cursor
    }

    private var cursor: Cursor? = null
        set(new) {
            field = new
            inputService.cursor = cursor ?: display.cursor ?: Cursor.Default
        }

    private fun gizmo(at: Point): Gizmo? {
        var newPoint = at
        var gizmo    = display.child(at = at)

        while(gizmo != null) {
            newPoint -= gizmo.position

            val child = gizmo.child_(at = newPoint) ?: break

            gizmo = child
        }

        return gizmo
    }

    private fun createMouseEvent(mouseEvent: SystemMouseEvent, gizmo: Gizmo, type: SystemMouseEvent.Type = mouseEvent.type) = MouseEvent(
            gizmo,
            type,
            mouseEvent.location - gizmo.toAbsolute(Point.Origin),
            mouseEvent.buttons,
            mouseEvent.clickCount,
            mouseEvent.modifiers)
}
