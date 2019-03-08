package com.nectar.doodle.datatransport.dragdrop.impl

import com.nectar.doodle.core.View
import com.nectar.doodle.datatransport.DataBundle
import com.nectar.doodle.datatransport.dragdrop.DragManager
import com.nectar.doodle.datatransport.dragdrop.DragOperation
import com.nectar.doodle.datatransport.dragdrop.DragOperation.Action
import com.nectar.doodle.datatransport.dragdrop.DropEvent
import com.nectar.doodle.datatransport.dragdrop.DropHandler
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.GraphicsSurface
import com.nectar.doodle.drawing.Renderable
import com.nectar.doodle.drawing.impl.RealGraphicsSurface
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.scheduler.Scheduler
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import kotlin.browser.document
import kotlin.math.abs


class DragManagerImpl(private val scheduler: Scheduler, private val graphicsDevice: GraphicsDevice<*>, htmlFactory: HtmlFactory): DragManager {


    private var visual             = null as Renderable?
    private var mouseDown          = null as MouseEvent?
    private var rootElement        = htmlFactory.root
    private var visualOffset       = Origin
    private var currentDropHandler = null as Pair<View, DropHandler>?

    private val isIE = graphicsDevice.create().let {
        val dynamic = (it as RealGraphicsSurface).rootElement.asDynamic()

        it.release()

        dynamic["dragDrop"] != undefined
    }

    private lateinit var visualCanvas: GraphicsSurface
    private lateinit var targetFinder: (Point) -> View?

    private fun createVisual(visual: Renderable?) {
        visualCanvas = graphicsDevice.create()

        renderDragVisual(visual)
    }

    override fun mouseDrag(view: View, event: MouseEvent, targetFinder: (Point) -> View?) {
        this.targetFinder = targetFinder

        if (isIE) {
            mouseDown?.let {
                if ((event.location - it.location).run { abs(x) >= 5.0 || abs(y) >= 5.0 }) {
                    view.dragHandler?.dragRecognized(event)?.let { dragOperation ->
                        createVisual(dragOperation.visual)

                        visualCanvas.position = dragOperation.visualOffset

                        scheduler.now { visualCanvas.release() }

                        (visualCanvas as RealGraphicsSurface).rootElement.apply {
                            ondragstart = {
                                it.dataTransfer?.apply {
                                    effectAllowed = "copy"

                                    dragOperation.bundle.invoke(String::class)?.let {
                                        setData("text", it)
                                    }
                                }
                            }
                        }

                        if(document.asDynamic()["selection"] != undefined) {
                            document.asDynamic().selection.empty()
                        }

                        (visualCanvas as RealGraphicsSurface).rootElement.asDynamic().dragDrop()

                        mouseDown = null
                    }
                }
            }
        }
    }

    override fun mouseDown(view: View, event: MouseEvent, targetFinder: (Point) -> View?): Boolean {
        if (isIE) {
            mouseDown = event
            return false
        }

        this.targetFinder = targetFinder

        view.dragHandler?.dragRecognized(event)?.let { dragOperation ->

            rootElement.draggable = true

            registerListeners(dragOperation, rootElement)

            return true
        }

        return false
    }

    private fun mouseLocation(event: org.w3c.dom.events.MouseEvent) = Point(
            x = event.clientX - rootElement.offsetLeft + rootElement.scrollLeft,
            y = event.clientY - rootElement.offsetTop  + rootElement.scrollLeft)

    private fun action(name: String?) = when (name) {
        "copy" -> Action.Copy
        "move" -> Action.Move
        "link" -> Action.Link
        else   -> Action.None
    }

    private fun registerListeners(dragOperation: DragOperation, element: HTMLElement) {
        element.ondragstart = {
            createVisual(dragOperation.visual)

            (visualCanvas as RealGraphicsSurface).rootElement.style.top = "-100px"

            it.dataTransfer?.effectAllowed = "all" // FIXME

            it.dataTransfer?.setDragImage((visualCanvas as RealGraphicsSurface).rootElement, 0, 0)

            dragOperation.bundle.invoke(String::class)?.let { text ->
                it.dataTransfer?.setData("text/plain", text)
            }
        }
        element.ondragenter = {
            if (it.target !is HTMLInputElement) {
                it.preventDefault ()
                it.stopPropagation()
            }
        }
        element.ondragover = {
            if (it.target !is HTMLInputElement) {

                try {
                    visualCanvas.release()
                } catch (ignored: Throwable) {
                }

                val action = action(it.dataTransfer?.run {
                    when {
                        effectAllowed != "all" -> effectAllowed
                        dropEffect == "none"   -> "move"
                        else                   -> dropEffect
                    }
                })

                if (!dragMove(dragOperation.bundle, action, mouseLocation(it))) {
                    it.dataTransfer?.dropEffect = "none"
                } else {
                    it.preventDefault ()
                    it.stopPropagation()
                }
            }
        }
        element.ondrop = {
            currentDropHandler?.let { (view, handler) ->
                if (handler.drop(DropEvent(view, mouseLocation(it), dragOperation.bundle, action(it.dataTransfer?.dropEffect))) && it.target !is HTMLInputElement) {
                    it.preventDefault ()
                    it.stopPropagation()
                }
            }

            currentDropHandler = null

            if (it.target !is HTMLInputElement) {
                it.preventDefault ()
                it.stopPropagation()
            }
        }
        element.ondragend = {
            element.draggable   = false
            element.ondragover  = null
            element.ondragenter = null
            element.ondragstart = null

            null
        }
    }

    private fun dragMove(bundle: DataBundle, desired: Action, location: Point): Boolean {
        var dropEvent: DropEvent
        var dropAllowed = false
        val coveredView = targetFinder(location)
        val dropHandler = getDropEventHandler(coveredView)

        if (dropHandler !== currentDropHandler) {
            currentDropHandler?.let { (view, handler) ->
                dropEvent = DropEvent(view, location, bundle, desired)

                handler.dropExit(dropEvent)
            }

            if (dropHandler != null) {
                dropEvent = DropEvent(dropHandler.first, location, bundle, desired)

                dropAllowed = dropHandler.second.dropEnter(dropEvent)

                if (desired !== Action.None) {
                    currentDropHandler = dropHandler
                } else {
                    currentDropHandler = null
                }
            } else {
                currentDropHandler = null
            }
        } else {
            currentDropHandler?.let { (view, handler) ->
                dropEvent   = DropEvent(view, location, bundle, desired)
                dropAllowed = handler.dropOver(dropEvent)

                if (desired === Action.None) {
                    handler.dropExit(dropEvent)

                    currentDropHandler = null
                }
            }
        }

        if (visual != null) {
            visualCanvas.position = location + visualOffset
        }

        return dropAllowed
    }

    private fun renderDragVisual(visual: Renderable?) {
        if (visual != null) {
            visualCanvas.size = visual.size

            visualCanvas.render {
                visual.render(it)
            }
        }
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

//class DragManagerImpl(
//        private val display          : Display,
//        private val graphicsDevice   : GraphicsDevice<*>,
//        private val keyInputService  : KeyInputService,
//        private val mouseInputService: MouseInputService,
//        private val scheduler        : Scheduler): DragManager, KeyInputService.Preprocessor, MouseInputService.Preprocessor {
//
//    private val isIE = graphicsDevice.create().let {
//        val dynamic = (it as RealGraphicsSurface).rootElement.asDynamic()
//
//        it.release()
//
//        dynamic["dragDrop"] != undefined
//    }
//
//    private fun createVisual() {
//        visualCanvas = graphicsDevice.create()
//
//        renderDragVisual(object : Renderable {
//            override val size = Size(100.0)
//
//            override fun render(canvas: Canvas) {
//                canvas.rect(Rectangle(size = size), ColorBrush(Color.red))
//            }
//        }, Origin)
//
//        visualCanvas.size = Size(100.0)
//
////            visualCanvas.position = Origin
//    }
//
//    override fun test() {
//        if (isIE) {
//            createVisual()
//
//            scheduler.now { visualCanvas.release() }
//
//            (visualCanvas as RealGraphicsSurface).rootElement.asDynamic().dragDrop()
//        } else {
//            document.body!!.draggable = true
//            document.body!!.ondragstart = {
//                createVisual()
//
//                (visualCanvas as RealGraphicsSurface).rootElement.style.top = "-100px"
//
//                it.dataTransfer?.setDragImage((visualCanvas as RealGraphicsSurface).rootElement, 0, 0)
//                it.dataTransfer?.setData("text/plain", "foo")
//            }
//            document.body!!.ondragover = {
//                try {
//                    visualCanvas.release()
//                } catch (ignored: Throwable) {}
//            }
//        }
//    }
//
//    private var visual             = null as Renderable?
//    private var bundle             = null as DataBundle?
//    private var action             = null as DragOperation.Action?
//    private var dragging           = false
//    private var observer           = null as ((DropCompleteEvent) -> Unit)?
//    private var visualOffset       = Origin
//    private var currentDropHandler = null as Pair<View, DropHandler>?
//
//    private lateinit var visualCanvas: GraphicsSurface
//
//    override fun startDrag(view        : View,
//                           event       : MouseEvent,
//                           bundle      : DataBundle,
//                           visual      : Renderable?,
//                           visualOffset: Point,
//                           observer    : (DropCompleteEvent) -> Unit) {
//        return
//
//        dragging          = true
//        this.bundle       = bundle
//        this.action       = getUserAction(event)
//        this.visual       = visual
//        this.observer     = observer
//        this.visualOffset = visualOffset
//        visualCanvas      = graphicsDevice.create()
//
//        renderDragVisual(visual, visualOffset)
//
//        keyInputService   += this
//        mouseInputService += this
//
//        preprocess(SystemMouseEvent(Move, mouseInputService.mouseLocation, event.buttons, event.clickCount, event.modifiers))
//    }
//
//    override fun invoke(keyState: KeyState): Boolean {
//        if (keyState.code == VK_ESCAPE) {
//            stopDrag(false)
//        } else {
//            var dropAllowed = false
//            val mouseLocation = mouseInputService.mouseLocation
//            val coveredView = display.child(mouseLocation)
//            val dropHandler = getDropEventHandler(coveredView)
//
//            action = getUserAction(keyState)
//
//            if (dropHandler === currentDropHandler) {
//                currentDropHandler?.let { (view, handler) ->
//                    val dropEvent = DropEvent(view, mouseLocation, bundle!!, action!!)
//
//                    dropAllowed = handler.dropActionChanged(dropEvent)
//
//                    if (action === Action.None) {
//                        handler.dropExit(dropEvent)
//
//                        currentDropHandler = null
//                    }
//                }
//            } else if (dropHandler != null) {
//                val dropEvent = DropEvent(dropHandler.first, mouseLocation, bundle!!, action!!)
//
//                dropAllowed = dropHandler.second.dropEnter(dropEvent)
//
//                if (action !== Action.None) {
//                    currentDropHandler = dropHandler
//                } else {
//                    currentDropHandler = null
//                }
//            }
//
//            mouseInputService.cursor = cursor(dropAllowed)
//        }
//
//        return true
//    }
//
//    override fun preprocess(event: SystemMouseEvent) {
//        if (dragging) {
//            when (event.type) {
//                Up    -> { mouseUp   (event); return }
//                Move  ->   mouseMove (event)
//                Exit  ->   mouseExit (     )
//                Enter ->   mouseEnter(     )
//                else  -> {                           }
//            }
//
//            event.consume()
//        }
//    }
//
//    override fun preprocess(event: SystemMouseWheelEvent) {
//        if (dragging) {
//            event.consume()
//        }
//    }
//
//    private fun mouseEnter() {
////        visualCanvas = graphicsDevice.create().apply {
////            render {
////                visual?.render(it)
////            }
////        }
//    }
//
//    private fun mouseExit() {
////        visualCanvas.release()
//    }
//
//    private fun mouseUp(event: SystemMouseEvent) {
//        val succeeded = currentDropHandler?.let { (view, handler) ->
//            handler.drop(DropEvent(view, event.location, bundle!!, action!!))
//        } ?: false
//
//        stopDrag(succeeded)
//    }
//
//    private fun stopDrag(succeeded: Boolean) {
//        observer?.invoke(DropCompleteEvent(succeeded, action ?: Action.None))
//
//        currentDropHandler = null
//
//        cleanupVisuals()
//
//        keyInputService   -= this
//        mouseInputService -= this
//    }
//
//    private fun mouseMove(event: SystemMouseEvent) {
//        var dropEvent: DropEvent
//        var dropAllowed = false
//        val coveredView = display.child(event.location)
//        val dropHandler = getDropEventHandler(coveredView)
//
//        if (dropHandler !== currentDropHandler) {
//            currentDropHandler?.let { (view, handler) ->
//                dropEvent = DropEvent(view, event.location, bundle!!, action!!)
//
//                handler.dropExit(dropEvent)
//            }
//
//            if (dropHandler != null) {
//                dropEvent = DropEvent(dropHandler.first, event.location, bundle!!, action!!)
//
//                dropAllowed = dropHandler.second.dropEnter(dropEvent)
//
//                if (action !== Action.None) {
//                    currentDropHandler = dropHandler
//                } else {
//                    currentDropHandler = null
//                }
//            } else {
//                currentDropHandler = null
//            }
//        } else {
//            currentDropHandler?.let { (view, handler) ->
//                dropEvent   = DropEvent(view, event.location, bundle!!, action!!)
//                dropAllowed = handler.dropOver(dropEvent)
//
//                if (action === Action.None) {
//                    handler.dropExit(dropEvent)
//
//                    currentDropHandler = null
//                }
//            }
//        }
//
//        mouseInputService.cursor = cursor(dropAllowed)
//
//        if (visual != null) {
//            visualCanvas.position = event.location + visualOffset
//        }
//    }
//
//    private fun cursor(dropAllowed: Boolean) = when {
//        dropAllowed -> {
//            when (action) {
//                Action.Link -> Cursor.Alias
//                Action.Copy -> Cursor.Copy
//                Action.Move -> Cursor.Grabbing
//                else        -> Cursor.NoDrop
//            }
//        }
//        else -> Cursor.NoDrop
//    }
//
//    private fun renderDragVisual(visual: Renderable?, offset: Point) {
//        if (visual != null) {
//            visualCanvas.bounds = Rectangle(mouseInputService.mouseLocation + offset, visual.size)
//
//            visualCanvas.render {
//                visual.render(it)
//            }
//        }
//    }
//
//    private fun getUserAction(keyState: KeyState) = when {
//        Shift in keyState.modifiers && Ctrl in keyState.modifiers -> Action.Link
//        Ctrl  in keyState.modifiers                               -> Action.Copy
//        else                                                      -> Action.Move
//    }
//
//    private fun getUserAction(event: MouseEvent) = when {
//        Shift in event.modifiers && Ctrl in event.modifiers -> Action.Link
//        Ctrl  in event.modifiers                            -> Action.Copy
//        else                                                -> Action.Move
//    }
//
//    private fun cleanupVisuals() {
//        visualCanvas.release()
//    }
//
//    private fun getDropEventHandler(view: View?): Pair<View, DropHandler>? {
//        var current = view
//        var handler = null as DropHandler?
//
//        while (current != null) {
//            handler = current.dropHandler
//
//            if (handler == null || !handler.active) {
//                current = current.parent
//            } else {
//                break
//            }
//        }
//
//        return if (handler != null && current != null) current to handler else null
//    }
//}