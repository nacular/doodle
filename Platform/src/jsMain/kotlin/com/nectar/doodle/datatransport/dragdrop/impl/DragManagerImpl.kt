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
import com.nectar.doodle.drawing.Renderable
import com.nectar.doodle.drawing.impl.RealGraphicsSurface
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.scheduler.Scheduler
import org.w3c.dom.DataTransfer
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import kotlin.math.abs


@Suppress("NestedLambdaShadowedImplicitParameter")
class DragManagerImpl(private val scheduler: Scheduler, private val graphicsDevice: GraphicsDevice<RealGraphicsSurface>, htmlFactory: HtmlFactory): DragManager {
    private var visual             = null as Renderable?
    private var mouseDown          = null as MouseEvent?
    private var rootElement        = htmlFactory.root
    private var visualOffset       = Origin
    private var currentDropHandler = null as Pair<View, DropHandler>?

    private val isIE = htmlFactory.create<HTMLElement>().asDynamic()["dragDrop"] != undefined

    private lateinit var visualCanvas: RealGraphicsSurface
    private lateinit var targetFinder: (Point) -> View?

    private fun createVisual(visual: Renderable?) {
        visualCanvas = graphicsDevice.create()

        renderDragVisual(visual)
    }

    override fun mouseDrag(view: View, event: MouseEvent, targetFinder: (Point) -> View?) {
        this.targetFinder = targetFinder

        mouseDown?.let {
            if ((event.location - it.location).run { abs(x) >= DRAG_THRESHOLD || abs(y) >= DRAG_THRESHOLD }) {
                view.dragHandler?.dragRecognized(event)?.let { dragOperation ->
                    createVisual(dragOperation.visual)

                    visualCanvas.position = dragOperation.visualOffset // FIXME: Need to figure out how to position visual

                    scheduler.now { visualCanvas.release() } // FIXME: This doesn't happen fast enough

                    visualCanvas.rootElement.apply {
                        ondragstart = {
                            it.dataTransfer?.apply {
                                effectAllowed = "all"

                                dragOperation.bundle.invoke(String::class)?.let {
                                    setData("text", it)
                                }
                            }
                        }
                    }

                    registerListeners(dragOperation, rootElement)

//                        if(document.asDynamic()["selection"] != undefined) {
//                            document.asDynamic().selection.empty()
//                        }

                    visualCanvas.rootElement.asDynamic().dragDrop()

                    mouseDown = null
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

            rootElement.ondragstart = {
                createVisual(dragOperation.visual)

                visualCanvas.rootElement.style.top = "-100px"

                it.dataTransfer?.effectAllowed = "all" // FIXME: limit based on operation

                it.dataTransfer?.setDragImage(visualCanvas.rootElement, visualOffset.x.toInt(), visualOffset.y.toInt())

                dragOperation.bundle.invoke(String::class)?.let { text ->
                    it.dataTransfer?.setData("text/plain", text)
                }
            }

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

    private fun action(dataTransfer: DataTransfer?) = action(dataTransfer?.run {
        when {
            effectAllowed != "all" -> effectAllowed
            dropEffect == "none"   -> "move"
            else                   -> dropEffect
        }
    })

    private fun registerListeners(dragOperation: DragOperation, element: HTMLElement) {
        element.ondragenter = {
            if (it.target !is HTMLInputElement) {
                it.preventDefault ()
                it.stopPropagation()
            }
        }
        element.ondragover = {
            if (it.target !is HTMLInputElement) {

                if (!isIE) {
                    try {
                        visualCanvas.release()
                    } catch (ignored: Throwable) {}
                }

                if (!dragMove(dragOperation.bundle, action(it.dataTransfer), mouseLocation(it))) {
                    it.dataTransfer?.dropEffect = "none"
                } else {
                    it.preventDefault ()
                    it.stopPropagation()
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

            currentDropHandler?.let { (view, handler) ->
                if (handler.drop(DropEvent(view, mouseLocation(it), dragOperation.bundle, action)) && it.target !is HTMLInputElement) {
                    dragOperation.completed(action)
                } else {
                    dragOperation.canceled()
                }
            } ?: when (action) {
                // Covers the case that no drop handler found or drop happens outside the window
                Action.None -> dragOperation.canceled (      )
                else        -> dragOperation.completed(action)
            }

            currentDropHandler = null

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

    companion object {
        private const val DRAG_THRESHOLD = 5.0
    }
}