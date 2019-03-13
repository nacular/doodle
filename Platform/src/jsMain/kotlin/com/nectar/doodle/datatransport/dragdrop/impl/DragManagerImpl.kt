package com.nectar.doodle.datatransport.dragdrop.impl

import com.nectar.doodle.core.View
import com.nectar.doodle.datatransport.DataBundle
import com.nectar.doodle.datatransport.PlainText
import com.nectar.doodle.datatransport.UriList
import com.nectar.doodle.datatransport.dragdrop.DragManager
import com.nectar.doodle.datatransport.dragdrop.DragOperation
import com.nectar.doodle.datatransport.dragdrop.DragOperation.Action
import com.nectar.doodle.datatransport.dragdrop.DropEvent
import com.nectar.doodle.datatransport.dragdrop.DropReceiver
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.setTop
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.Renderable
import com.nectar.doodle.drawing.impl.RealGraphicsSurface
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.system.SystemMouseEvent
import org.w3c.dom.DataTransfer
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import kotlin.math.abs


@Suppress("NestedLambdaShadowedImplicitParameter")
class DragManagerImpl(private val scheduler: Scheduler, private val graphicsDevice: GraphicsDevice<RealGraphicsSurface>, htmlFactory: HtmlFactory): DragManager {
    private val isIE                 = htmlFactory.create<HTMLElement>().asDynamic()["dragDrop"] != undefined
    private var mouseDown            = null as MouseEvent?
    private var rootElement          = htmlFactory.root
    private var dropAllowed          = false
    private var currentAction        = null as Action?
    private var allowedActions       = "all"
    private var currentDropHandler   = null as Pair<View, DropReceiver>?
    private var currentMouseLocation = Origin

    private lateinit var visualCanvas: RealGraphicsSurface
    private lateinit var targetFinder: (Point) -> View?

    override fun mouseDrag(view: View, event: MouseEvent, targetFinder: (Point) -> View?) {
        this.targetFinder = targetFinder

        mouseDown?.let {
            if ((event.location - it.location).run { abs(x) >= DRAG_THRESHOLD || abs(y) >= DRAG_THRESHOLD }) {
                view.dragRecognizer?.dragRecognized(event)?.let { dragOperation ->
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

                    mouseDown = null
                }
            }
        }
    }

    override fun mouseDown(view: View, event: MouseEvent, targetFinder: (Point) -> View?) {
        if (isIE) {
            mouseDown = event
            return
        }

        this.targetFinder = targetFinder

        view.dragRecognizer?.dragRecognized(event)?.let { dragOperation ->

            rootElement.draggable = true

            rootElement.ondragstart = {
                createVisual(dragOperation.visual)

                dragOperation.visual?.let { visualCanvas.rootElement.style.setTop(-it.size.height) }

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
        }
    }

    override fun mouseUp(event: SystemMouseEvent) {
        mouseDown               = null
        rootElement.draggable   = false
        rootElement.ondragstart = null
    }

    private fun allowedActions(actions: Set<Action>): String {
        val builder = StringBuilder()

        if (Action.Copy in actions) builder.append("copy")
        if (Action.Link in actions) builder.append("Link")
        if (Action.Move in actions) builder.append("Move")

        return when (val string = builder.toString()) {
            "copyLinkMove" -> "all"
            ""             -> "none"
            else           -> string.decapitalize()
        }.also {
            allowedActions = it
        }
    }

    private fun mouseLocation(event: org.w3c.dom.events.MouseEvent) = Point(
            x = event.clientX - rootElement.offsetLeft + rootElement.scrollLeft,
            y = event.clientY - rootElement.offsetTop  + rootElement.scrollLeft)

    private fun action(name: String?) = when (name) {
        "copy" -> Action.Copy
        "move" -> Action.Move
        "link" -> Action.Link
        else   -> null
    }

    private fun action(dataTransfer: DataTransfer?) = action(dataTransfer?.run {
        when {
            effectAllowed != allowedActions -> effectAllowed
            dropEffect    == "none"         -> "move"
            else                            -> dropEffect
        }
    })

    private fun registerListeners(dragOperation: DragOperation, element: HTMLElement) {
        element.ondragenter = {
            if (it.target !is HTMLInputElement) {
                it.preventDefault ()
                it.stopPropagation()
            }
        }
        element.ondragover = { event ->
            if (event.target !is HTMLInputElement) {

                if (!isIE) {
                    try {
                        visualCanvas.release()
                    } catch (ignored: Throwable) {}
                }

                if (currentDropHandler == null) {
                    dropAllowed = false
                }

                dropAllowed = dragUpdate(dragOperation.bundle, action(event.dataTransfer), mouseLocation(event))

                if (dropAllowed) {
                    event.preventDefault ()
                    event.stopPropagation()
                } else {
                    event.dataTransfer?.dropEffect = "none"
                }
            }
        }
        element.ondrop = {
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

            val action = action(it.dataTransfer?.dropEffect)

            if (action == null) {
                dragOperation.canceled ()
            } else {
                currentDropHandler?.let { (view, handler) ->
                    if (handler.drop(DropEvent(view, mouseLocation(it), dragOperation.bundle, action)) && it.target !is HTMLInputElement) {
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
        val dropHandler = getDropEventHandler(targetFinder(location))

        if (dropHandler != currentDropHandler) {
            currentDropHandler?.let { (view, handler) ->
                handler.dropExit(DropEvent(view, location, bundle, desired))
            }

            if (dropHandler != null) {
                dropAllowed = dropHandler.second.dropEnter(DropEvent(dropHandler.first, location, bundle, desired))

                when (desired) {
                    null -> currentDropHandler = null
                    else -> currentDropHandler = dropHandler
                }
            } else {
                currentDropHandler = null
            }
        } else {
            currentDropHandler?.let { (view, handler) ->
                val dropEvent = DropEvent(view, location, bundle, desired)

                if (desired == null) {
                    handler.dropExit(dropEvent)

                    currentDropHandler = null
                } else {
                    if (currentMouseLocation != location) {
                        currentMouseLocation = location

                        dropAllowed = handler.dropOver(dropEvent)
                    }

                    if (currentAction != desired) {
                        currentAction = desired

                        dropAllowed = handler.dropActionChanged(DropEvent(view, location, bundle, desired))
                    }
                }
            }
        }

        return dropAllowed
    }

    private fun createVisual(visual: Renderable?) {
        visualCanvas = graphicsDevice.create()

        if (visual != null) {
            visualCanvas.size = visual.size

            visualCanvas.render {
                visual.render(it)
            }
        }
    }

    private fun getDropEventHandler(view: View?): Pair<View, DropReceiver>? {
        var current = view
        var handler = null as DropReceiver?

        while (current != null) {
            handler = current.dropReceiver

            if (handler == null || !handler.active) {
                current = current.parent
            } else {
                break
            }
        }

        return if (handler != null && current != null) current to handler else null
    }

    companion object {
        private const val DRAG_THRESHOLD = 5.0
    }
}