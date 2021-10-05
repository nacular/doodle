package io.nacular.doodle.datatransport.dragdrop.impl

import io.nacular.doodle.core.View
import io.nacular.doodle.datatransport.DataBundle
import io.nacular.doodle.datatransport.Files
import io.nacular.doodle.datatransport.LocalFile
import io.nacular.doodle.datatransport.MimeType
import io.nacular.doodle.datatransport.PlainText
import io.nacular.doodle.datatransport.dragdrop.DragManager
import io.nacular.doodle.datatransport.dragdrop.DragOperation
import io.nacular.doodle.datatransport.dragdrop.DragOperation.Action
import io.nacular.doodle.datatransport.dragdrop.DragOperation.Action.Copy
import io.nacular.doodle.datatransport.dragdrop.DragOperation.Action.Link
import io.nacular.doodle.datatransport.dragdrop.DragOperation.Action.Move
import io.nacular.doodle.datatransport.dragdrop.DropEvent
import io.nacular.doodle.datatransport.dragdrop.DropReceiver
import io.nacular.doodle.deviceinput.ViewFinder
import io.nacular.doodle.drawing.impl.CanvasImpl
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.swing.location
import io.nacular.doodle.system.PointerInputService
import io.nacular.doodle.system.impl.toDoodle
import io.nacular.measured.units.BinarySize.Companion.bytes
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.times
import kotlinx.coroutines.CancellationException
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Font
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skiko.SkiaWindow
import org.jetbrains.skiko.toBufferedImage
import java.awt.Cursor
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE
import java.awt.dnd.DnDConstants.ACTION_LINK
import java.awt.dnd.DragGestureEvent
import java.awt.dnd.DragGestureListener
import java.awt.dnd.DragSource
import java.awt.dnd.DragSourceAdapter
import java.awt.dnd.DragSourceDropEvent
import java.awt.event.MouseEvent
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files.probeContentType
import java.util.Base64
import javax.swing.JComponent
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
import java.awt.Point as AwtPoint


@Suppress("NestedLambdaShadowedImplicitParameter")
internal class DragManagerImpl(
        private val window             : SkiaWindow,
        private val viewFinder         : ViewFinder,
        private val defaultFont        : Font,
        private val fontCollection     : FontCollection): DragManager, DragGestureListener {

    private var dropAllowed            = false
    private var currentAction          = null as Action?
    private var currentDropHandler     = null as Pair<View, DropReceiver>?
    private var currentPointerLocation = Origin
    private var dragOperation          = null as DragOperation?
    private val dragSource             = DragSource()

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
        val fileTypes = mimeType.types.map { it.toString() }

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
            is Files     -> support.getFiles(type) as? T
            is PlainText -> DataFlavor(type.toString(), null).getReaderForText(support.transferable).readText() as T
            in this      -> support.transferable.getTransferData(DataFlavor(type.toString(), null)) as? T
            else         -> null
        }

        override val includedTypes: List<MimeType<*>>
            get() = support.transferable.transferDataFlavors.map { MimeType<Any>(it.primaryType, it.subType, emptyMap()) }

        override fun <T> contains(type: MimeType<T>) = type in support
    }

    init {
        dragSource.createDefaultDragGestureRecognizer(window.layer, ACTION_COPY_OR_MOVE or ACTION_LINK, this)

        window.layer.transferHandler = object: TransferHandler() {
            override fun canImport(support: TransferSupport): Boolean {
                (dragOperation?.bundle ?: createBundle(support)).let {
                    if (currentDropHandler == null) {
                        dropAllowed = false
                    }

                    dropAllowed = dragUpdate(it, action(support), support.dropLocation.dropPoint.run { Point(x, y) })
                }

                return dropAllowed
            }

            override fun getSourceActions(c: JComponent?): Int {
                var result = NONE

                dragOperation?.allowedActions?.forEach {
                    result = result.or(when (it) {
                        Copy -> COPY
                        Move -> MOVE
                        Link -> LINK
                    })
                }

                return result
            }

            override fun importData(support: TransferSupport): Boolean {
                var succeeded = false
                val bundle    = dragOperation?.bundle ?: createBundle(support)

                currentDropHandler?.let { (view, handler) ->
                    succeeded = handler.drop(DropEvent(view, view.fromAbsolute(support.dropLocation.dropPoint.run { Point(x, y) }), bundle, action(support)))

                    currentDropHandler = null
                }

                return succeeded
            }
        }
    }

    override fun shutdown() {
    }

    override fun dragGestureRecognized(event: DragGestureEvent?) {
        if (event == null) {
            return
        }

        val mouseEvent = event.triggerEvent as? MouseEvent
        val location   = mouseEvent?.location(window) ?: return

        viewFinder.find(location).let {
            var view = it

            while (view != null) {
                if (tryDrag(view, PointerEvent(view, mouseEvent.toDoodle(window)), event)) {
                    break
                }

                view = view.parent
            }
        }
    }

    private fun tryDrag(view: View, event: PointerEvent, dragEvent: DragGestureEvent): Boolean {
        view.dragRecognizer?.dragRecognized(event)?.let { dragOperation ->
            this.dragOperation = dragOperation

            val transferable = dragOperation.bundle.let { bundle ->
                object: Transferable {
                    override fun getTransferDataFlavors(): Array<DataFlavor> = bundle.includedTypes.map {
                        when (it) {
                            PlainText -> DataFlavor.plainTextFlavor
                            else      -> DataFlavor(it.toString())
                        }
                    }.toTypedArray()

                    // TODO: Cannot get flavor.mimeType b/c it is shadowed by flavor.getMimeType, which returns a string
                    override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean = flavor != null && MimeType<Any>(flavor.primaryType, flavor.subType) in bundle

                    // TODO: Cannot get flavor.mimeType b/c it is shadowed by flavor.getMimeType, which returns a string
                    override fun getTransferData(flavor: DataFlavor?): Any? = flavor?.run { bundle[MimeType<Any>(primaryType, subType)] }
                }
            }

            val dragListener = object: DragSourceAdapter() {
                override fun dragDropEnd(dropEvent: DragSourceDropEvent?) {
                    dropEvent?.let { event ->
                        val dropAction = action(event.dropAction)

                        when {
                            event.dropSuccess && dropAction != null -> dragOperation.completed(dropAction)
                            else                                    -> dragOperation.canceled (          )
                        }
                    }

                    this@DragManagerImpl.dragOperation = null
                }
            }

            when {
                DragSource.isDragImageSupported() -> {
                    val image = dragOperation.visual?.let { visual ->
                        val bitmap = Bitmap().apply {
                            allocN32Pixels(visual.size.width.toInt(), visual.size.height.toInt())
                        }

                        val bitmapCanvas = Canvas(bitmap)

                        visual.render(CanvasImpl(bitmapCanvas, defaultFont, fontCollection).apply { size = visual.size })

                        bitmap.toBufferedImage()
                    }

                    val offset = dragOperation.visualOffset.run { AwtPoint(-x.toInt(), -y.toInt()) }

                    dragEvent.startDrag(Cursor.getDefaultCursor(), image, offset, transferable, dragListener)
                }
                else                              -> dragEvent.startDrag(Cursor.getDefaultCursor(), transferable, dragListener)
            }

            dragOperation.started()

            return true
        }

        return false
    }

    private fun action(awtAction: Int) = when (awtAction) {
        NONE         -> null
        COPY         -> Copy
        MOVE         -> Move
        COPY_OR_MOVE -> Copy
        LINK         -> Link
        else         -> null
    }

    private fun action(support: TransferSupport) = action(support.dropAction)

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
}