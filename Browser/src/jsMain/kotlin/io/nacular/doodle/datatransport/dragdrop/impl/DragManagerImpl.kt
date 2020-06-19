package io.nacular.doodle.datatransport.dragdrop.impl

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.HTMLInputElement
import io.nacular.doodle.core.View
import io.nacular.doodle.datatransport.DataBundle
import io.nacular.doodle.datatransport.MimeType
import io.nacular.doodle.datatransport.PlainText
import io.nacular.doodle.datatransport.UriList
import io.nacular.doodle.datatransport.dragdrop.DragManager
import io.nacular.doodle.datatransport.dragdrop.DragOperation
import io.nacular.doodle.datatransport.dragdrop.DragOperation.Action
import io.nacular.doodle.datatransport.dragdrop.DragOperation.Action.Copy
import io.nacular.doodle.datatransport.dragdrop.DragOperation.Action.Link
import io.nacular.doodle.datatransport.dragdrop.DragOperation.Action.Move
import io.nacular.doodle.datatransport.dragdrop.DropEvent
import io.nacular.doodle.datatransport.dragdrop.DropReceiver
import io.nacular.doodle.deviceinput.ViewFinder
import io.nacular.doodle.dom.DataTransfer
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.setTop
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.PatternFill
import io.nacular.doodle.drawing.Renderable
import io.nacular.doodle.drawing.impl.RealGraphicsSurface
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.scheduler.Scheduler
import io.nacular.doodle.system.PointerInputService
import io.nacular.doodle.system.PointerInputService.Preprocessor
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.SystemPointerEvent.Type.Down
import io.nacular.doodle.system.SystemPointerEvent.Type.Up
import kotlin.math.abs
import io.nacular.doodle.dom.MouseEvent as DomMouseEvent


@Suppress("NestedLambdaShadowedImplicitParameter")
internal class DragManagerImpl(
                      private val viewFinder       : ViewFinder,
                      private val scheduler        : Scheduler,
                      private val pointerInputService: PointerInputService,
                      private val graphicsDevice   : GraphicsDevice<RealGraphicsSurface>,
                                  htmlFactory      : HtmlFactory): DragManager {
    private val isIE                   = htmlFactory.create<HTMLElement>().asDynamic()["dragDrop"] != undefined
    private var pointerDown            = null as PointerEvent?
    private var rootElement            = htmlFactory.root
    private var dropAllowed            = false
    private var currentAction          = null as Action?
    private var allowedActions         = "all"
    private var currentDropHandler     = null as Pair<View, DropReceiver>?
    private var currentPointerLocation = Origin
    private var dataBundle             = null as DataBundle?

    private lateinit var visualCanvas: RealGraphicsSurface

    private fun createBundle(dataTransfer: DataTransfer?) = dataTransfer?.let {
        object: DataBundle {
            override fun <T> invoke(type: MimeType<T>) = when (type) {
                in this -> it.getData(type.toString()) as? T
                else    -> null
            }

            override fun <T> contains(type: MimeType<T>) = type.toString() in it.types
        }
    }

    init {
        pointerInputService += object: Preprocessor {
            override fun preprocess(event: SystemPointerEvent) {
                when (event.type) {
                    Up   -> pointerUp  (     )
                    Down -> pointerDown(event)
                    else -> pointerDrag(event)
                }
            }
        }

        rootElement.ondragover = { event ->
            if (event.target !is HTMLInputElement) {
                (dataBundle ?: createBundle(event.dataTransfer))?.let {
                    if (!isIE) {
                        visualCanvas.release()
                    }

                    if (currentDropHandler == null) {
                        dropAllowed = false
                    }

                    dropAllowed = dragUpdate(it, action(event.dataTransfer), pointerLocation(event))

                    if (dropAllowed) {
                        event.preventDefault ()
                        event.stopPropagation()
                    } else {
                        event.dataTransfer?.dropEffect = "none"
                    }
                }
            }
        }

        rootElement.ondrop = { event ->
            if (event.target !is HTMLInputElement) {
                // This is the case of a drop that originated from outside the browser
                if (dataBundle == null) {
                    createBundle(event.dataTransfer)?.let {
                        currentDropHandler?.let { (view, handler) ->
                            handler.drop(DropEvent(view, view.fromAbsolute(pointerLocation(event)), it, action(event.dataTransfer?.dropEffect)))
                        }
                    }
                }

                event.preventDefault ()
                event.stopPropagation()
            }
        }
    }

    override fun shutdown() {
        rootElement.ondragover = null
        rootElement.ondrop     = null
    }

    private fun pointerEvent(event: SystemPointerEvent, view: View) = PointerEvent(
            view,
            view,
            event.type,
            view.fromAbsolute(event.location),
            event.buttons,
            event.clickCount,
            event.modifiers)

    private fun pointerDrag(event: SystemPointerEvent) {
        pointerDown?.let {
            viewFinder.find(event.location)?.let { view ->

                if ((event.location - it.location).run { abs(x) >= DRAG_THRESHOLD || abs(y) >= DRAG_THRESHOLD }) {
                    view.dragRecognizer?.dragRecognized(pointerEvent(event, view))?.let { dragOperation ->
                        dataBundle = dragOperation.bundle

                        createVisual(dragOperation.visual)

                        visualCanvas.position = dragOperation.visualOffset // FIXME: Need to figure out how to position visual

                        scheduler.now { visualCanvas.release() } // FIXME: This doesn't happen fast enough

                        visualCanvas.rootElement.apply {
                            ondragstart = {
                                it.dataTransfer?.apply {
                                    effectAllowed = allowedActions(dragOperation.allowedActions)

                                    setOf(PlainText, UriList).forEach { mimeType ->
                                        dragOperation.bundle(mimeType)?.let { text ->
                                            it.dataTransfer?.setData("$it", text)
                                        }
                                    }
                                }
                            }
                        }

                        registerListeners(dragOperation, rootElement)

                        // FIXME: Need to find way to avoid dragging selected inputs even when they aren't being dragged
//                        if(document.asDynamic()["selection"] != undefined) {
//                            document.asDynamic().selection.empty()
//                        }

                        visualCanvas.rootElement.asDynamic().dragDrop()

                        pointerDown = null
                    }
                }
            }
        }
    }

    private fun pointerDown(event: SystemPointerEvent) {
        viewFinder.find(event.location).let {
            var view = it

            while (view != null) {
                if (tryDrag(view, event)) {
                    break
                }

                view = view.parent
            }
        }
    }

    private fun tryDrag(view: View, event: SystemPointerEvent): Boolean {
        if (!view.enabled || !view.visible) {
            return false
        }

        val pointerEvent = pointerEvent(event, view)

        if (isIE) {
            pointerDown = pointerEvent
            return true // IE doesn't start a drag after this.  We just have to hand-off at this point.
        }

        view.dragRecognizer?.dragRecognized(pointerEvent)?.let { dragOperation ->

            dataBundle            = dragOperation.bundle
            rootElement.draggable = true

            rootElement.ondragstart = {
                createVisual(dragOperation.visual)

                it.dataTransfer?.effectAllowed = allowedActions(dragOperation.allowedActions)

                it.dataTransfer?.setDragImage(visualCanvas.rootElement, dragOperation.visualOffset.x.toInt(), dragOperation.visualOffset.y.toInt())

                setOf(PlainText, UriList).forEach { mimeType ->
                    dragOperation.bundle(mimeType)?.let { text ->
                        it.dataTransfer?.setData("$mimeType", text)
                    }
                }

                dragOperation.started()
            }

            registerListeners(dragOperation, rootElement)

            return true
        }

        return false
    }

    private fun pointerUp() {
        pointerDown               = null
        dataBundle              = null
        rootElement.draggable   = false
        rootElement.ondragstart = null
    }

    private fun allowedActions(actions: Set<Action>): String {
        val builder = StringBuilder()

        if (Copy in actions) builder.append("copy")
        if (Link in actions) builder.append("Link")
        if (Move in actions) builder.append("Move")

        return when (val string = builder.toString()) {
            "copyLinkMove" -> "all"
            ""             -> "none"
            else           -> string.decapitalize()
        }.also {
            allowedActions = it
        }
    }

    private fun pointerLocation(event: DomMouseEvent) = Point(event.pageX, event.pageY)

    private fun action(name: String?) = when {
        name == null            -> null
        name.startsWith("copy") -> Copy
        name.startsWith("move") -> Move
        name.startsWith("link") -> Link
        name.startsWith("all" ) -> Copy
        else                    -> null
    }

    private fun action(dataTransfer: DataTransfer?) = action(dataTransfer?.run {
        when {
            effectAllowed != allowedActions && effectAllowed != "uninitialized" -> effectAllowed
            dropEffect    == "none"                                             -> "move"
            else                                                                -> dropEffect
        }
    })

    private fun registerListeners(dragOperation: DragOperation, element: HTMLElement) {
        element.ondragenter = {
            if (it.target !is HTMLInputElement) {
                it.preventDefault ()
                it.stopPropagation()
            }
        }
        element.ondragend = {
            element.draggable   = false
            element.ondragenter = null
            element.ondragstart = null

            val action = action(it.dataTransfer?.dropEffect)

            if (action == null) {
                dragOperation.canceled ()
            } else {
                currentDropHandler?.let { (view, handler) ->
                    if (handler.drop(DropEvent(view, view.fromAbsolute(pointerLocation(it)), dragOperation.bundle, action)) && it.target !is HTMLInputElement) {
                        dragOperation.completed(action)
                    } else {
                        dragOperation.canceled()
                    }
                } ?: dragOperation.completed(action)
            }

            currentDropHandler = null

            null
        }
    }

    private fun dragUpdate(bundle: DataBundle, desired: Action?, location: Point): Boolean {
        var dropAllowed = this.dropAllowed
        val dropHandler = getDropEventHandler(viewFinder.find(location))

        if (dropHandler != currentDropHandler) {
            currentDropHandler?.let { (view, handler) ->
                handler.dropExit(DropEvent(view, view.fromAbsolute(location), bundle, desired))
            }

            if (dropHandler != null) {
                dropAllowed = dropHandler.second.dropEnter(DropEvent(dropHandler.first, dropHandler.first.fromAbsolute(location), bundle, desired))

                currentDropHandler = when (desired) {
                    null -> null
                    else -> dropHandler
                }
            } else {
                currentDropHandler = null
            }
        } else {
            currentDropHandler?.let { (view, handler) ->
                val dropEvent = DropEvent(view, view.fromAbsolute(location), bundle, desired)

                when {
                    desired == null                    -> {
                        handler.dropExit(dropEvent)

                        currentDropHandler = null
                    }
                    currentPointerLocation != location -> {
                        currentPointerLocation = location

                        dropAllowed = handler.dropOver(dropEvent)
                    }
                    currentAction != desired           -> {
                        currentAction = desired

                        dropAllowed = handler.dropActionChanged(DropEvent(view, view.fromAbsolute(location), bundle, desired))
                    }
                }
            }
        }

        return dropAllowed
    }

    private fun createVisual(visual: Renderable?) {
        // TODO: Make this a general purpose View -> Image generator
        visualCanvas = graphicsDevice.create()

        if (visual != null) {
            visualCanvas.rootElement.style.setTop(-visual.size.height)

            visualCanvas.zOrder = -Int.MAX_VALUE
            visualCanvas.size   = visual.size

            visualCanvas.canvas.rect(Rectangle(size = visual.size), PatternFill(visual.size) {
                visual.render(this)
            })
        }
    }

    private fun getDropEventHandler(view: View?): Pair<View, DropReceiver>? {
        var current = view
        var handler = null as DropReceiver?

        while (current != null && current.enabled && current.visible) {
            handler = current.dropReceiver

            if (handler == null || !handler.active) {
                current = current.parent
            } else {
                break
            }
        }

        return if (handler != null && current != null) current to handler else null
    }

    private companion object {
        private const val DRAG_THRESHOLD = 5.0
    }
}