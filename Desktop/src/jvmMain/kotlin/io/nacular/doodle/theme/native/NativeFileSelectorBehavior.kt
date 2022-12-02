package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.files.FileSelector
import io.nacular.doodle.controls.files.FileSelector.Companion.AnyFile
import io.nacular.doodle.controls.files.FileSelectorBehavior
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.datatransport.LocalFile
import io.nacular.doodle.datatransport.MimeType
import io.nacular.doodle.datatransport.SimpleFile
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.impl.CanvasImpl
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener.Companion.clicked
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.Cursor.Companion.Default
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.Dimension
import java.awt.datatransfer.DataFlavor
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseEvent.MOUSE_DRAGGED
import java.awt.event.MouseEvent.MOUSE_MOVED
import java.io.File
import java.nio.file.Files
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFileChooser.APPROVE_OPTION
import javax.swing.JPanel
import javax.swing.filechooser.FileFilter
import kotlin.coroutines.CoroutineContext

internal class NativeFileSelectorStylerImpl(window: JPanel): NativeFileSelectorStyler {
    private val fileHandler = FileSelectionHandler(window)

    override fun invoke(fileSelector: FileSelector, behavior: Behavior<FileSelector>): Behavior<FileSelector> = NativeFileSelectorBehaviorWrapper(
        fileHandler,
        fileSelector,
        behavior
    )
}

private class FileSelectionHandler(private val window: JPanel) {
    private val fileChooser: JFileChooser by lazy {
        JFileChooser().apply { isAcceptAllFileFilterUsed = false }
    }

    fun run(fileSelector: FileSelector): List<LocalFile> {
        try {
            with(fileChooser) {
                currentDirectory        = File(System.getProperty("user.home"))
                isMultiSelectionEnabled = fileSelector.allowMultiple

                when {
                    fileSelector.acceptedTypes === AnyFile -> {}
                    else                                   -> {
                        fileFilter = object : FileFilter() {
                            override fun accept(pathname: File?): Boolean {
                                return pathname?.let { Files.probeContentType(it.toPath()) }?.let {
                                    val type = DataFlavor(it).run {
                                        MimeType<Any>(primaryType, subType, emptyMap())
                                    }

                                    type in fileSelector.acceptedTypes

                                } ?: false
                            }

                            override fun getDescription() = fileSelector.acceptedTypes.joinToString(", ")
                        }
                    }
                }

                showOpenDialog(window).let { result ->
                    if (result == APPROVE_OPTION) {
                        return fileChooser.selectedFiles.map { SimpleFile(it) }
                    }
                }
            }
        } catch (ignored: Exception) {
        }

        return emptyList()
    }
}

private class NativeFileSelectorBehaviorWrapper(private val fileHandler: FileSelectionHandler, fileSelector: FileSelector, private val delegate: Behavior<FileSelector>): FileSelectorBehavior, Behavior<FileSelector> by delegate {
    init {
        fileSelector.pointerChanged += clicked {
            fileSelector.files = fileHandler.run(fileSelector)
        }
    }
}

internal class NativeFileSelectorBehavior(
        private val appScope                 : CoroutineScope,
        private val uiDispatcher             : CoroutineContext,
        private val window                   : JPanel,
        private val swingGraphicsFactory     : SwingGraphicsFactory,
        private val focusManager             : FocusManager?,
        private val nativePointerPreprocessor: NativePointerPreprocessor?,
        private val prompt               : String,
): FileSelectorBehavior {
    private val fileHandler = FileSelectionHandler(window)

    private inner class FileSelectorPeer(fileSelector: FileSelector): JButton(prompt) {
        // This needs to remain since JButton will render on construct before the local value is initialized
        @Suppress("RedundantNullableReturnType")
        private val fileSelector: FileSelector? = fileSelector

        init {
            focusTraversalKeysEnabled = false

            addFocusListener(object : FocusListener {
                override fun focusGained(e: FocusEvent?) {
                    focusManager?.requestFocus(fileSelector)
                }

                override fun focusLost(e: FocusEvent?) {
                    focusManager?.clearFocus()
                }
            })

            nativePointerPreprocessor?.set(fileSelector, object: NativePointerHandler {
                override fun invoke(event: PointerEvent) {
                    if (event.source == fileSelector) {
                        this@FileSelectorPeer.processMouseEvent(event.toAwt(nativePeer))
                    }
                }
            })

            addMouseListener(object: MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    fileSelector.files = fileHandler.run(fileSelector)
                }
            })
        }

        override fun repaint(tm: Long, x: Int, y: Int, width: Int, height: Int) {
            fileSelector?.rerender()
        }

        public override fun processMouseEvent(e: MouseEvent?)  = when (e?.id) {
            MOUSE_MOVED, MOUSE_DRAGGED -> super.processMouseMotionEvent(e)
            else                       -> super.processMouseEvent      (e)
        }
    }

    private lateinit var nativePeer   : FileSelectorPeer
    private          var oldCursor    : Cursor? = null
    private          var oldIdealSize : Size?   = null

    private val focusChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        when (new) {
            true -> nativePeer.requestFocusInWindow()
            else -> nativePeer.transferFocus       ()
        }
    }

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        nativePeer.isEnabled = new
    }

    private val focusableChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        nativePeer.isFocusable = new
    }

    private val boundsChanged: (View, Rectangle, Rectangle) -> Unit = { _,_,new ->
        nativePeer.size = new.size.run { Dimension(width.toInt(), height.toInt()) }
    }

    override fun render(view: FileSelector, canvas: Canvas) {
        nativePeer.paint(swingGraphicsFactory((canvas as CanvasImpl).skiaCanvas))
    }

    override fun install(view: FileSelector) {
        super.install(view)

        nativePeer = FileSelectorPeer(view)

        view.apply {
            focusChanged        += this@NativeFileSelectorBehavior.focusChanged
            boundsChanged       += this@NativeFileSelectorBehavior.boundsChanged
            enabledChanged      += this@NativeFileSelectorBehavior.enabledChanged
            focusabilityChanged += this@NativeFileSelectorBehavior.focusableChanged
        }

        appScope.launch(uiDispatcher) {
            nativePeer.size = view.size.run { Dimension(view.width.toInt(), view.height.toInt()) }

            view.apply {
                cursor    = Default
                idealSize = nativePeer.preferredSize.run { Size(width, height) }
            }

            window.add(nativePeer)

            if (view.hasFocus) {
                nativePeer.requestFocusInWindow()
            }
        }
    }

    override fun uninstall(view: FileSelector) {
        super.uninstall(view)

        view.apply {
            cursor    = oldCursor
            idealSize = oldIdealSize

            focusChanged        -= this@NativeFileSelectorBehavior.focusChanged
            boundsChanged       -= this@NativeFileSelectorBehavior.boundsChanged
            enabledChanged      -= this@NativeFileSelectorBehavior.enabledChanged
            focusabilityChanged -= this@NativeFileSelectorBehavior.focusableChanged
        }

        appScope.launch(uiDispatcher) {
            window.remove(nativePeer)
        }
    }
}