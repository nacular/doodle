package io.nacular.doodle.datatransport.dragdrop.impl

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.datatransport.DataBundle
import io.nacular.doodle.datatransport.Files
import io.nacular.doodle.datatransport.LocalFile
import io.nacular.doodle.datatransport.MimeType
import io.nacular.doodle.datatransport.PlainText
import io.nacular.doodle.datatransport.SimpleFile
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
import io.nacular.doodle.dom.DragEvent
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HTMLInputElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.contains
import io.nacular.doodle.dom.get
import io.nacular.doodle.dom.setPosition
import io.nacular.doodle.dom.toJsString
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.Renderable
import io.nacular.doodle.drawing.impl.NativeCanvas
import io.nacular.doodle.drawing.impl.RealGraphicsSurface
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.system.PointerInputService
import io.nacular.doodle.system.PointerInputService.Preprocessor
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.SystemPointerEvent.Type.Down
import io.nacular.doodle.system.SystemPointerEvent.Type.Up
import io.nacular.doodle.system.impl.PointerLocationResolver


@Suppress("NestedLambdaShadowedImplicitParameter")
internal class DragManagerImpl(
      private val display                : Display,
      private val viewFinder             : ViewFinder,
      private val pointerInputService    : PointerInputService,
      private val pointerLocationResolver: PointerLocationResolver,
      private val graphicsDevice         : GraphicsDevice<RealGraphicsSurface>,
                  htmlFactory            : HtmlFactory
): DragManager {
    private var pointerDown            = null as PointerEvent?
    private var rootElement            = htmlFactory.root
    private var dropAllowed            = false
    private var currentAction          = null as Action?
    private var allowedActions         = "all"
    private var currentDropHandler     = null as Pair<View, DropReceiver>?
    private var currentPointerLocation = Origin
    private var dataBundle             = null as DataBundle?

    private lateinit var visualCanvas: RealGraphicsSurface

    private fun getFiles(dataTransfer: DataTransfer, mimeType: Files): List<LocalFile> {
        val mimeTypes = mimeType.types.map { "$it" }

        return (0 .. dataTransfer.items.length).mapNotNull { dataTransfer.items[it] }.filter { it.type in mimeTypes }.mapNotNull {
            it.getAsFile()?.let { SimpleFile(it) }
        }
    }

    private fun contains(dataTransfer: DataTransfer, mimeType: Files): Boolean {
        if ("Files".toJsString() !in dataTransfer.types) {
            return false
        }

        // Special case to deal with browsers that do not provide file info until drop event.
        // We simply say that the file types are present to avoid exposing complexity
        // (like having a custom MimeType that checks for this condition and letting the caller
        // decide)
        if (dataTransfer.items.length == 0 && dataTransfer.files.length == 0) {
            return true
        }

        val mimeTypes = mimeType.types.map { "$it" }

        return mimeTypes.isEmpty() || (0 .. dataTransfer.items.length).mapNotNull { dataTransfer.items[it] }.find { it.type in mimeTypes } != null
    }

    private val mimeRegex by lazy { Regex("(application|audio|font|example|image|message|model|multipart|text|video|\\*|x-[\\dA-Za-z!#$%&'*+.^_`|~-]+)/([\\dA-Za-z!#\$%&'*+.^_`|~-]+)((?:[ \\t]*;[ \\t]*[\\dA-Za-z!#\$%&'*+.^_`|~-]+=(?:[\\dA-Za-z!#\$%&'*+.^_`|~-]+|\"(?:[^\"\\\\]|\\\\.)*\"))*)") }

    private fun createBundle(dataTransfer: DataTransfer?) = dataTransfer?.let { transfer ->
        object: DataBundle {
            @Suppress("UNCHECKED_CAST")
            override fun <T> get(type: MimeType<T>) = when(type) {
                is Files -> getFiles(transfer, type) as? T
                in this  -> transfer.getData(type.toString()) as? T
                else     -> null
            }

            override fun <T> contains(type: MimeType<T>) = when(type) {
                is Files -> contains(transfer, type)
                else     -> transfer.types.contains("$type".toJsString())
            }

            override val includedTypes: List<MimeType<*>> by lazy {
                (0 .. transfer.items.length).mapNotNull {
                    transfer.items[it]?.type?.let {
                        mimeRegex.matchEntire(it)?.let innerLet@ { match ->
                            val primary   = match.groups[1]?.value ?: return@innerLet null
                            val secondary = match.groups[2]?.value ?: return@innerLet null
                            val params    = match.groups[3]?.value ?: return@innerLet null
                            val splits    = params.split(";").map { it.trim() }.filter { it.isNotBlank() }.map {
                                it.split("=").let { it[0].trim() to it[1].trim() }
                            }

                            MimeType<Any>(primary, secondary, splits.associate { it.first to it.second })
                        }
                    }
                }
            }
        }
    }

    init {
        pointerInputService.addPreprocessor(display, object: Preprocessor {
            override fun invoke(event: SystemPointerEvent) {
                when (event.type) {
                    Up   -> pointerUp  (     )
                    Down -> pointerDown(event)
                    else -> {}
                }
            }
        })

        rootElement.ondragover = { event ->
            if (event.target !is HTMLInputElement) {
                (dataBundle ?: createBundle(event.dataTransfer))?.let {
                    if (::visualCanvas.isInitialized) {
                        visualCanvas.release()
                    }

                    if (currentDropHandler == null) {
                        dropAllowed = false
                    }

                    dropAllowed = dragUpdate(it, action(event.dataTransfer), pointerLocationResolver(event))

                    if (!dropAllowed) {
                        event.dataTransfer?.dropEffect = "none"
                    }

                    event.preventDefault ()
                    event.stopPropagation()
                }
            }
        }

        rootElement.ondrop = { event ->
            if (event.target !is HTMLInputElement) {
                // This is the case of a drop that originated from outside the browser
                if (dataBundle == null) {
                    createBundle(event.dataTransfer)?.let {
                        currentDropHandler?.let { (view, handler) ->
                            handler.drop(DropEvent(view, view.fromAbsolute(pointerLocationResolver(event)), it, action(event.dataTransfer?.dropEffect)))

                            currentDropHandler = null
                        }
                    }
                }

                event.preventDefault ()
                event.stopPropagation()

                pointerUp()
            }
        }
    }

    override fun shutdown() {
        rootElement.ondragover = null
        rootElement.ondrop     = null
    }

    private fun pointerEvent(event: SystemPointerEvent, view: View) = PointerEvent(view, event)

    private fun pointerDown(event: SystemPointerEvent) {
        viewFinder.find(display, event.location).let {
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

        view.dragRecognizer?.dragRecognized(pointerEvent)?.let { dragOperation ->

            dataBundle            = dragOperation.bundle
            rootElement.draggable = true

            rootElement.ondragstart = { dragEvent: DragEvent ->

                dragEvent.dataTransfer?.effectAllowed = allowedActions(dragOperation.allowedActions)

                val visual = dragOperation.visual ?: object: Renderable {
                    override val size get() = view.size

                    override fun render(canvas: Canvas) {
                        (canvas as? NativeCanvas)?.apply {
                            addData(listOf((graphicsDevice[view].rootElement.cloneNode(deep = true) as HTMLElement)))
                        }
                    }
                }

                createVisual(visual, event.location + dragOperation.visualOffset)
                dragEvent.dataTransfer?.setDragImage(visualCanvas.rootElement, -dragOperation.visualOffset.x.toInt(), -dragOperation.visualOffset.y.toInt())

                var dataEmpty = true

                setOf(PlainText, UriList).forEach { mimeType ->
                    dragOperation.bundle[mimeType]?.let { text ->
                        dragEvent.dataTransfer?.setData("$mimeType", text)
                        dataEmpty = false
                    }
                }

                if (dataEmpty) {
                    // Hack for Safari, which kills the drag immediately if it has no data
                    dragEvent.dataTransfer?.setData("", "")
                }

                dragOperation.started()
            }

            registerListeners(dragOperation, rootElement)

            return true
        }

        return false
    }

    private fun pointerUp() {
        pointerDown             = null
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
            else           -> string.replaceFirstChar { it.lowercase() }
        }.also {
            allowedActions = it
        }
    }

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

            when (val action = action(it.dataTransfer?.dropEffect)) {
                null -> dragOperation.canceled()
                else -> currentDropHandler?.let { (view, handler) ->
                    when {
                        handler.drop(DropEvent(view, view.fromAbsolute(pointerLocationResolver(it)), dragOperation.bundle, action)) && it.target !is HTMLInputElement -> dragOperation.completed(action)
                        else -> dragOperation.canceled()
                    }
                } ?: dragOperation.completed(action)
            }

            currentDropHandler = null

            pointerUp()
        }
    }

    private fun dragUpdate(bundle: DataBundle, desired: Action?, location: Point): Boolean {
        var dropAllowed = this.dropAllowed
        val dropHandler = getDropEventHandler(viewFinder.find(display, location))

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

    private fun createVisual(visual: Renderable, position: Point) {
        class CanvasWrapper(private val delegate: NativeCanvas): NativeCanvas by delegate {
            override fun addData(elements: List<HTMLElement>, at: Point) {
                delegate.addData(elements.map { it.cloneNode(deep = true) as HTMLElement }, at)
            }
        }

        // TODO: Make this a general purpose View -> Image generator
        visualCanvas = graphicsDevice.create()

        visualCanvas.apply {
            rootElement.style.setPosition(position)
            rootElement.style.zIndex = "${-Int.MAX_VALUE}"

            size = visual.size
        }

        (visualCanvas.canvas as? NativeCanvas)?.let {
            visual.render(CanvasWrapper(it))
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