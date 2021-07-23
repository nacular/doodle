package io.nacular.doodle.datatransport.dragdrop.impl

import io.nacular.doodle.core.View
import io.nacular.doodle.datatransport.DataBundle
import io.nacular.doodle.datatransport.Files
import io.nacular.doodle.datatransport.LocalFile
import io.nacular.doodle.datatransport.MimeType
import io.nacular.doodle.datatransport.dragdrop.DragManager
import io.nacular.doodle.datatransport.dragdrop.DragOperation.Action
import io.nacular.doodle.datatransport.dragdrop.DragOperation.Action.Copy
import io.nacular.doodle.datatransport.dragdrop.DragOperation.Action.Link
import io.nacular.doodle.datatransport.dragdrop.DragOperation.Action.Move
import io.nacular.doodle.datatransport.dragdrop.DropEvent
import io.nacular.doodle.datatransport.dragdrop.DropReceiver
import io.nacular.doodle.deviceinput.ViewFinder
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.Renderable
import io.nacular.doodle.drawing.impl.RealGraphicsSurface
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.scheduler.Scheduler
import io.nacular.doodle.system.PointerInputService
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.measured.units.BinarySize.Companion.bytes
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.times
import kotlinx.coroutines.CancellationException
import org.jetbrains.skiko.SkiaWindow
import java.awt.datatransfer.DataFlavor
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files.probeContentType
import java.util.Base64
import javax.swing.TransferHandler
import javax.swing.TransferHandler.COPY
import javax.swing.TransferHandler.COPY_OR_MOVE
import javax.swing.TransferHandler.LINK
import javax.swing.TransferHandler.MOVE
import javax.swing.TransferHandler.NONE
import javax.swing.TransferHandler.TransferSupport
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs


@Suppress("NestedLambdaShadowedImplicitParameter")
internal class DragManagerImpl(
        private val window                 : SkiaWindow,
        private val viewFinder             : ViewFinder,
        private val scheduler              : Scheduler,
        private val pointerInputService    : PointerInputService,
        private val graphicsDevice         : GraphicsDevice<RealGraphicsSurface>): DragManager {
    private var pointerDown            = null as PointerEvent?
    private var dropAllowed            = false
    private var currentAction          = null as Action?
    private var allowedActions         = "all"
    private var currentDropHandler     = null as Pair<View, DropReceiver>?
    private var currentPointerLocation = Origin
    private var dataBundle             = null as DataBundle?

    private lateinit var visualCanvas: RealGraphicsSurface

    private class SimpleFile(private val delegate: File): LocalFile {
        override val name: String get() = delegate.name
        override val size         get() = delegate.length() * bytes
        override val type         get() = delegate.extension
        override val isClosed     get() = false
        override val lastModified get() = delegate.lastModified() * milliseconds

        override suspend fun read(progress: (Float) -> Unit): ByteArray? = suspendCoroutine { coroutine ->
            try {
                val result    = ByteArrayOutputStream()
                val fileSize  = delegate.length()
                var totalRead = 0L

                delegate.forEachBlock { buffer, bytesRead ->
                    result.write(buffer)
                    totalRead += bytesRead
                    progress((fileSize / totalRead).toFloat())
                }

                coroutine.resume(result.toByteArray())
            } catch (e: CancellationException) {
                coroutine.resumeWithException(e)
            }
        }

        override suspend fun readText(encoding: String?, progress: (Float) -> Unit): String? = suspendCoroutine { coroutine ->
            try {
                val result = java.lang.StringBuilder()
                val fileSize = delegate.length()
                var totalRead = 0L

                delegate.forEachLine(Charset.forName(encoding)) {
                    result.append(it)
                    totalRead += it.length
                    progress((fileSize / totalRead).toFloat())
                }

                coroutine.resume(result.toString())
            } catch (e: CancellationException) {
                coroutine.resumeWithException(e)
            }
        }

        override suspend fun readBase64(progress: (Float) -> Unit): String? = Base64.getEncoder().encodeToString(read(progress))
    }

    private fun TransferSupport.getFiles(mimeType: Files): List<LocalFile> {
        val fileTypes = mimeType.types.map { it.toString() }.also { println("fileTypes: ${it.joinToString()}") }

        return (transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<File>)?.filter {
            probeContentType(it.toPath()) in fileTypes
        }?.map {
            SimpleFile(it)
        } ?: emptyList()
    }

    private operator fun <T> TransferSupport.contains(mimeType: MimeType<T>): Boolean {
        when (mimeType) {
            is Files -> {
                if (isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    return when (val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<File>) {
                        null -> true
                        else -> files.find {
                            val fileTypes = mimeType.types.map { it.toString() }

                            probeContentType(it.toPath()) in fileTypes
                        } != null
                    }
                }
            }
            else -> dataFlavors.forEach {
                if (it.isMimeTypeEqual(mimeType.toString())) return true
            }
        }

        return false
    }

    private fun createBundle(support: TransferSupport) = object: DataBundle {
        override fun <T> get(type: MimeType<T>) = when (type) {
            is Files -> support.getFiles(type) as? T
            in this  -> support.transferable.getTransferData(DataFlavor(type.toString(), null)) as? T
            else    -> null
        }

        override fun <T> contains(type: MimeType<T>) = type in support
    }

    init {
//        pointerInputService += object: Preprocessor {
//            override fun preprocess(event: SystemPointerEvent) {
//                when (event.type) {
//                    Up   -> pointerUp  (     )
//                    Down -> pointerDown(event)
//                    else -> pointerDrag(event)
//                }
//            }
//        }

        window.layer.transferHandler = object: TransferHandler() {
            override fun canImport(support: TransferSupport): Boolean {
                (dataBundle ?: createBundle(support)).let {
                    if (currentDropHandler == null) {
                        dropAllowed = false
                    }

                    dropAllowed = dragUpdate(it, action(support), support.dropLocation.dropPoint.run { Point(x, y) })
                }

                return dropAllowed
            }

//            override fun getDragImage(): Image {
//                return super.getDragImage()
//            }
//
//            override fun getDragImageOffset(): java.awt.Point {
//                return super.getDragImageOffset()
//            }

            override fun importData(support: TransferSupport): Boolean {
                if (dataBundle == null) {
                    createBundle(support).let {
                        currentDropHandler?.let { (view, handler) ->
                            handler.drop(DropEvent(view, view.fromAbsolute(support.dropLocation.dropPoint.run { Point(x, y) }), it, action(support)))

                            currentDropHandler = null
                        }
                    }

                    return true
                }

                return false
            }
        }
    }

    override fun shutdown() {
    }

    private fun pointerEvent(event: SystemPointerEvent, view: View) = PointerEvent(view, event)

    private fun pointerDrag(event: SystemPointerEvent) {
        pointerDown?.let {
            viewFinder.find(event.location)?.let { view ->

                if ((event.location - it.location).run { abs(x) >= DRAG_THRESHOLD || abs(y) >= DRAG_THRESHOLD }) {
                    view.dragRecognizer?.dragRecognized(pointerEvent(event, view))?.let { dragOperation ->
                        dataBundle = dragOperation.bundle

                        dragOperation.visual?.let { visual ->
                            createVisual(visual)

//                            visualCanvas.position = dragOperation.visualOffset // FIXME: Need to figure out how to position visual
//
//                            scheduler.now { visualCanvas.release() } // FIXME: This doesn't happen fast enough
//
//                            visualCanvas.rootElement.apply {
//                                ondragstart = {
//                                    it.dataTransfer?.apply {
//                                        effectAllowed = allowedActions(dragOperation.allowedActions)
//
//                                        setOf(PlainText, UriList).forEach { mimeType ->
//                                            dragOperation.bundle[mimeType]?.let { text ->
//                                                it.dataTransfer?.setData("$it", text)
//                                            }
//                                        }
//                                    }
//                                }
//                            }
                        }

//                        registerListeners(dragOperation, rootElement)

                        // FIXME: Need to find way to avoid dragging selected inputs even when they aren't being dragged
//                        if(document.asDynamic()["selection"] != undefined) {
//                            document.asDynamic().selection.empty()
//                        }

//                        visualCanvas.rootElement.asDynamic().dragDrop()

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

        view.dragRecognizer?.dragRecognized(pointerEvent)?.let { dragOperation ->

            dataBundle            = dragOperation.bundle

//            rootElement.ondragstart = {
//
//                it.dataTransfer?.effectAllowed = allowedActions(dragOperation.allowedActions)
//
//                dragOperation.visual?.let { visual ->
//                    createVisual(visual)
//
//                    it.dataTransfer?.setDragImage(visualCanvas.rootElement, dragOperation.visualOffset.x.toInt(), dragOperation.visualOffset.y.toInt())
//                }
//
//                setOf(PlainText, UriList).forEach { mimeType ->
//                    dragOperation.bundle[mimeType]?.let { text ->
//                        it.dataTransfer?.setData("$mimeType", text)
//                    }
//                }
//
//                dragOperation.started()
//            }
//
//            registerListeners(dragOperation, rootElement)

            return true
        }

        return false
    }

    private fun pointerUp() {
        pointerDown = null
        dataBundle  = null
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

//    private fun pointerLocation(event: DomMouseEvent) = Point(event.pageX, event.pageY)

    private fun action(support: TransferSupport) = when (support.dropAction) {
        NONE         -> null
        COPY         -> Copy
        MOVE         -> Move
        COPY_OR_MOVE -> Copy
        LINK         -> Link
        else         -> null
    }

//    private fun action(dataTransfer: DataTransfer?) = action(dataTransfer?.run {
//        when {
//            effectAllowed != allowedActions && effectAllowed != "uninitialized" -> effectAllowed
//            dropEffect    == "none"                                             -> "move"
//            else                                                                -> dropEffect
//        }
//    })

//    private fun registerListeners(dragOperation: DragOperation, element: HTMLElement) {
//        element.ondragenter = {
//            if (it.target !is HTMLInputElement) {
//                it.preventDefault ()
//                it.stopPropagation()
//            }
//        }
//        element.ondragend = {
//            element.draggable   = false
//            element.ondragenter = null
//            element.ondragstart = null
//
//            val action = action(it.dataTransfer?.dropEffect)
//
//            if (action == null) {
//                dragOperation.canceled()
//            } else {
//                currentDropHandler?.let { (view, handler) ->
//                    if (handler.drop(DropEvent(view, view.fromAbsolute(pointerLocationResolver(it)), dragOperation.bundle, action)) && it.target !is HTMLInputElement) {
//                        dragOperation.completed(action)
//                    } else {
//                        dragOperation.canceled()
//                    }
//                } ?: dragOperation.completed(action)
//            }
//
//            currentDropHandler = null
//
//            pointerUp()
//
//            null
//        }
//    }

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

    private fun createVisual(visual: Renderable) {
//        class CanvasWrapper(private val delegate: NativeCanvas): NativeCanvas by delegate {
//            override fun addData(elements: List<HTMLElement>, at: Point) {
//                delegate.addData(elements.map { it.cloneNode(deep = true) as HTMLElement }, at)
//            }
//        }
//
//        // TODO: Make this a general purpose View -> Image generator
//        visualCanvas = graphicsDevice.create()
//
//        visualCanvas.rootElement.style.setTop(-visual.size.height)
//
//        visualCanvas.zOrder = -Int.MAX_VALUE
//        visualCanvas.size   = visual.size
//
//        (visualCanvas.canvas as? NativeCanvas)?.let {
//            visual.render(CanvasWrapper(it))
//        }
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