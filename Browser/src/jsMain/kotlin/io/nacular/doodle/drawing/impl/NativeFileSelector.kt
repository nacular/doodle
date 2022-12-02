package io.nacular.doodle.drawing.impl

import io.nacular.doodle.controls.files.FileSelector
import io.nacular.doodle.core.View
import io.nacular.doodle.datatransport.LocalFile
import io.nacular.doodle.datatransport.SimpleFile
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.None
import io.nacular.doodle.dom.Static
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.setDisplay
import io.nacular.doodle.dom.setFont
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setPosition
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.CanvasFactory
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Size
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList


internal interface NativeFileSelectorFactory {
    operator fun invoke(fileSelector: FileSelector, customRenderer: ((FileSelector, Canvas) -> Unit)? = null, onChange: (List<LocalFile>) -> Unit): NativeFileSelector
}

internal class NativeFileSelectorFactoryImpl internal constructor(
        private val htmlFactory              : HtmlFactory,
        private val canvasFactory            : CanvasFactory,
        private val nativeEventHandlerFactory: NativeEventHandlerFactory,
        private val elementRuler             : ElementRuler,
        private val focusManager             : FocusManager?,
): NativeFileSelectorFactory {

    override fun invoke(fileSelector: FileSelector, customRenderer: ((FileSelector, Canvas) -> Unit)?, onChange: (List<LocalFile>) -> Unit) = NativeFileSelector(
            htmlFactory,
            nativeEventHandlerFactory,
            elementRuler,
            focusManager,
            canvasFactory,
            fileSelector,
            customRenderer,
            onChange)
}

internal class NativeFileSelector internal constructor(
                    htmlFactory   : HtmlFactory,
                    handlerFactory: NativeEventHandlerFactory,
        private val elementRuler  : ElementRuler,
        private val focusManager  : FocusManager?,
        private val canvasFactory : CanvasFactory,
        private val fileSelector    : FileSelector,
        private val customRenderer: ((FileSelector, Canvas) -> Unit)?,
        private val filesChanged  : (List<LocalFile>) -> Unit
): NativeEventListener {

    private val rootEventHandler : NativeEventHandler
    private val inputEventHandler: NativeEventHandler

    private var customCanvas: Canvas? = null

    private var inputSize = Size.Empty

    val idealSize: Size get() = inputSize

    private val inputElement = htmlFactory.createInput().apply {
        style.setFont         (null )
        style.setWidthPercent (100.0)
        style.setHeightPercent(100.0)
        style.cursor = "inherit"

        if (customRenderer != null) {
            style.setDisplay(None())
        }

        type     = "file"
        multiple = fileSelector.allowMultiple
        disabled = !fileSelector.enabled

        when {
            fileSelector.acceptedTypes === FileSelector.AnyFile -> {}
            else                                                -> accept = fileSelector.acceptedTypes.joinToString(",")
        }

        inputEventHandler = handlerFactory(this, this@NativeFileSelector).apply {
            registerFocusListener ()
            registerChangeListener()
        }

        style.setPosition(Static()) // remove absolute positioning due to global style

        val block = htmlFactory.create<HTMLElement>().also {
            it.add(this)
        }

        inputSize = elementRuler.size(block)

        style.setPosition() // restore absolute
    }

    private val rootElement = htmlFactory.create<HTMLElement>("LABEL").apply {
        style.cursor = "inherit"
        style.setWidthPercent (100.0)
        style.setHeightPercent(100.0)

        rootEventHandler = handlerFactory(this, this@NativeFileSelector).apply {
            registerFocusListener()
//            registerClickListener()
        }

        add(inputElement)

        if (customRenderer != null) {
            val canvasElement = htmlFactory.create<HTMLElement>().apply {
                style.setWidthPercent (100.0)
                style.setHeightPercent(100.0)

                customCanvas = canvasFactory(this)
            }

            add(canvasElement)
        }
    }

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        inputElement.disabled = !new
    }

    private val focusabilityChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        inputElement.tabIndex = if (new) 0 else -1
    }

    private val focusChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        when {
            new  -> inputElement.focus()
            else -> inputElement.blur ()
        }
    }

    init {
        fileSelector.apply {
            focusChanged          += this@NativeFileSelector.focusChanged
            enabledChanged        += this@NativeFileSelector.enabledChanged
            focusabilityChanged   += this@NativeFileSelector.focusabilityChanged
        }
    }

    fun render(canvas: Canvas) {
        if (canvas is NativeCanvas) {
            canvas.addData(listOf(rootElement), Origin)

            customCanvas?.apply {
                clear()
                customRenderer?.invoke(fileSelector, this)
                flush()
            }

            if (fileSelector.hasFocus) {
                inputElement.focus()
            }
        }
    }

    fun discard() {
        fileSelector.apply {
            focusChanged          -= this@NativeFileSelector.focusChanged
            enabledChanged        -= this@NativeFileSelector.enabledChanged
            focusabilityChanged   -= this@NativeFileSelector.focusabilityChanged
        }

        rootEventHandler.unregisterFocusListener  ()
//        rootEventHandler.unregisterClickListener  ()

        inputEventHandler.unregisterFocusListener ()
//        inputEventHandler.unregisterClickListener ()
        inputEventHandler.unregisterChangeListener()
    }

    override fun onFocusGained(event: Event): Boolean {
        fileSelector.let {
            if (!it.focusable) {
                return false
            }

            focusManager?.requestFocus(it)
        }

        return true
    }

    override fun onFocusLost(event: Event) = true.also {
        if (fileSelector === focusManager?.focusOwner) {
            focusManager.clearFocus()
        }
    }

    override fun onChange(event: Event): Boolean {
        inputElement.files?.asList()?.map { SimpleFile(it) }?.let {
            filesChanged(it)
        }

        return true
    }
}