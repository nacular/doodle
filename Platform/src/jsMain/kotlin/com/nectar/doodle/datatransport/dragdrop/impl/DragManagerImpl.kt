package com.nectar.doodle.datatransport.dragdrop.impl

import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.datatransport.DataBundle
import com.nectar.doodle.datatransport.dragdrop.DragManager
import com.nectar.doodle.datatransport.dragdrop.DragOperation
import com.nectar.doodle.datatransport.dragdrop.DragOperation.Action
import com.nectar.doodle.datatransport.dragdrop.DropCompleteEvent
import com.nectar.doodle.datatransport.dragdrop.DropEvent
import com.nectar.doodle.datatransport.dragdrop.DropHandler
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.GraphicsSurface
import com.nectar.doodle.drawing.Renderable
import com.nectar.doodle.event.KeyEvent.Companion.VK_ESCAPE
import com.nectar.doodle.event.KeyState
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.KeyInputService
import com.nectar.doodle.system.MouseInputService
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift
import com.nectar.doodle.system.SystemMouseEvent
import com.nectar.doodle.system.SystemMouseEvent.Type.Enter
import com.nectar.doodle.system.SystemMouseEvent.Type.Exit
import com.nectar.doodle.system.SystemMouseEvent.Type.Move
import com.nectar.doodle.system.SystemMouseEvent.Type.Up
import com.nectar.doodle.system.SystemMouseWheelEvent


class DragManagerImpl(
        private val display          : Display,
        private val graphicsDevice   : GraphicsDevice<*>,
        private val keyInputService  : KeyInputService,
        private val mouseInputService: MouseInputService): DragManager, KeyInputService.Preprocessor, MouseInputService.Preprocessor {

    private var visual             = null as Renderable?
    private var bundle             = null as DataBundle?
    private var action             = null as DragOperation.Action?
    private var dragging           = false
    private var observer           = null as ((DropCompleteEvent) -> Unit)?
    private var visualOffset       = Origin
    private var currentDropHandler = null as Pair<View, DropHandler>?

    private lateinit var visualCanvas: GraphicsSurface

    override fun startDrag(view        : View,
                           event       : MouseEvent,
                           bundle      : DataBundle,
                           visual      : Renderable?,
                           visualOffset: Point,
                           observer    : (DropCompleteEvent) -> Unit) {
        dragging          = true
        this.bundle       = bundle
        this.action       = getUserAction(event)
        this.visual       = visual
        this.observer     = observer
        this.visualOffset = visualOffset
        visualCanvas      = graphicsDevice.create()

        renderDragVisual(visual, visualOffset)

        keyInputService   += this
        mouseInputService += this

        preprocess(SystemMouseEvent(Move, mouseInputService.mouseLocation, event.buttons, event.clickCount, event.modifiers))
    }

    override fun invoke(keyState: KeyState): Boolean {
        if (keyState.code == VK_ESCAPE) {
            stopDrag(false)
        } else {
            var dropAllowed = false
            val mouseLocation = mouseInputService.mouseLocation
            val coveredView = display.child(mouseLocation)
            val dropHandler = getDropEventHandler(coveredView)

            action = getUserAction(keyState)

            if (dropHandler === currentDropHandler) {
                currentDropHandler?.let { (view, handler) ->
                    val dropEvent = DropEvent(view, mouseLocation, bundle!!, action!!)

                    dropAllowed = handler.dropActionChanged(dropEvent)

                    if (action === Action.None) {
                        handler.dropExit(dropEvent)

                        currentDropHandler = null
                    }
                }
            } else if (dropHandler != null) {
                val dropEvent = DropEvent(dropHandler.first, mouseLocation, bundle!!, action!!)

                dropAllowed = dropHandler.second.dropEnter(dropEvent)

                if (action !== Action.None) {
                    currentDropHandler = dropHandler
                } else {
                    currentDropHandler = null
                }
            }

            mouseInputService.cursor = cursor(dropAllowed)
        }

        return true
    }

    override fun preprocess(event: SystemMouseEvent) {
        if (dragging) {
            when (event.type) {
                Up    -> { mouseUp   (event); return }
                Move  ->   mouseMove (event)
                Exit  ->   mouseExit (     )
                Enter ->   mouseEnter(     )
                else  -> {                           }
            }

            event.consume()
        }
    }

    override fun preprocess(event: SystemMouseWheelEvent) {
        if (dragging) {
            event.consume()
        }
    }

    private fun mouseEnter() {
//        visualCanvas = graphicsDevice.create().apply {
//            render {
//                visual?.render(it)
//            }
//        }
    }

    private fun mouseExit() {
//        visualCanvas.release()
    }

    private fun mouseUp(event: SystemMouseEvent) {
        val succeeded = currentDropHandler?.let { (view, handler) ->
            handler.drop(DropEvent(view, event.location, bundle!!, action!!))
        } ?: false

        stopDrag(succeeded)
    }

    private fun stopDrag(succeeded: Boolean) {
        observer?.invoke(DropCompleteEvent(succeeded, action ?: Action.None))

        currentDropHandler = null

        cleanupVisuals()

        keyInputService   -= this
        mouseInputService -= this
    }

    private fun mouseMove(event: SystemMouseEvent) {
        var dropEvent: DropEvent
        var dropAllowed = false
        val coveredView = display.child(event.location)
        val dropHandler = getDropEventHandler(coveredView)

        if (dropHandler !== currentDropHandler) {
            currentDropHandler?.let { (view, handler) ->
                dropEvent = DropEvent(view, event.location, bundle!!, action!!)

                handler.dropExit(dropEvent)
            }

            if (dropHandler != null) {
                dropEvent = DropEvent(dropHandler.first, event.location, bundle!!, action!!)

                dropAllowed = dropHandler.second.dropEnter(dropEvent)

                if (action !== Action.None) {
                    currentDropHandler = dropHandler
                } else {
                    currentDropHandler = null
                }
            } else {
                currentDropHandler = null
            }
        } else {
            currentDropHandler?.let { (view, handler) ->
                dropEvent   = DropEvent(view, event.location, bundle!!, action!!)
                dropAllowed = handler.dropOver(dropEvent)

                if (action === Action.None) {
                    handler.dropExit(dropEvent)

                    currentDropHandler = null
                }
            }
        }

        mouseInputService.cursor = cursor(dropAllowed)

        if (visual != null) {
            visualCanvas.position = event.location + visualOffset
        }
    }

    private fun cursor(dropAllowed: Boolean) = when {
        dropAllowed -> {
            when (action) {
                Action.Link -> Cursor.Alias
                Action.Copy -> Cursor.Copy
                Action.Move -> Cursor.Grabbing
                else        -> Cursor.NoDrop
            }
        }
        else -> Cursor.NoDrop
    }

    private fun renderDragVisual(visual: Renderable?, offset: Point) {
        if (visual != null) {
            visualCanvas.bounds = Rectangle(mouseInputService.mouseLocation + offset, visual.size)

            visualCanvas.render {
                visual.render(it)
            }
        }
    }

    private fun getUserAction(keyState: KeyState) = when {
        Shift in keyState.modifiers && Ctrl in keyState.modifiers -> Action.Link
        Ctrl  in keyState.modifiers                               -> Action.Copy
        else                                                      -> Action.Move
    }

    private fun getUserAction(event: MouseEvent) = when {
        Shift in event.modifiers && Ctrl in event.modifiers -> Action.Link
        Ctrl  in event.modifiers                            -> Action.Copy
        else                                                -> Action.Move
    }

    private fun cleanupVisuals() {
        visualCanvas.release()
    }

    private fun getDropEventHandler(view: View?): Pair<View, DropHandler>? {
        var current = view
        var handler = null as DropHandler?

        while (current != null) {
            handler = current.dropHandler

            if (handler == null || !handler.active) {
                current = current.parent
            } else {
                break
            }
        }

        return if (handler != null && current != null) current to handler else null
    }
}