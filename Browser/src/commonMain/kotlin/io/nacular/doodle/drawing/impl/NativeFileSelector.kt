package io.nacular.doodle.drawing.impl

import io.nacular.doodle.controls.files.FileSelector
import io.nacular.doodle.core.View
import io.nacular.doodle.datatransport.LocalFile
import io.nacular.doodle.datatransport.SimpleFile
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.None
import io.nacular.doodle.dom.Static
import io.nacular.doodle.dom.Visible
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.asList
import io.nacular.doodle.dom.setDisplay
import io.nacular.doodle.dom.setDomPosition
import io.nacular.doodle.dom.setFont
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setOverflow
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.CanvasFactory
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size


internal interface NativeFileSelectorFactory {
    operator fun invoke(
        fileSelector      : FileSelector,
        onChange          : (List<LocalFile>) -> Unit
    ): NativeFileSelector = invoke(fileSelector, null, false, onChange)

    operator fun invoke(
        fileSelector      : FileSelector,
        customRenderer    : ((FileSelector, Canvas) -> Unit)?,
        clipCanvasToBounds: Boolean,
        onChange          : (List<LocalFile>) -> Unit
    ): NativeFileSelector
}

internal class NativeFileSelectorFactoryImpl internal constructor(
    private val htmlFactory              : HtmlFactory,
    private val canvasFactory            : CanvasFactory,
    private val nativeEventHandlerFactory: NativeEventHandlerFactory,
    private val elementRuler             : ElementRuler,
    private val focusManager             : FocusManager?,
): NativeFileSelectorFactory {

    override fun invoke(
        fileSelector      : FileSelector,
        customRenderer    : ((FileSelector, Canvas) -> Unit)?,
        clipCanvasToBounds: Boolean,
        onChange          : (List<LocalFile>) -> Unit
    ) = NativeFileSelector(
            htmlFactory,
            nativeEventHandlerFactory,
            elementRuler,
            focusManager,
            canvasFactory,
            fileSelector,
            customRenderer,
            clipCanvasToBounds,
            onChange)
}

internal class NativeFileSelector internal constructor(
    htmlFactory       : HtmlFactory,
    handlerFactory    : NativeEventHandlerFactory,
    private val elementRuler      : ElementRuler,
    private val focusManager      : FocusManager?,
    private val canvasFactory     : CanvasFactory,
    private val fileSelector      : FileSelector,
    private val customRenderer    : ((FileSelector, Canvas) -> Unit)?,
    private val clipCanvasToBounds: Boolean,
    private val filesChanged      : (List<LocalFile>) -> Unit
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

        style.setDomPosition(Static()) // remove absolute positioning due to global style

        val block = htmlFactory.create<HTMLElement>().also {
            it.add(this)
        }

        inputSize = elementRuler.size(block)

        style.setDomPosition() // restore absolute
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

                customCanvas = canvasFactory(this).apply { size = fileSelector.size}

                if (!clipCanvasToBounds) {
                    style.setOverflow(Visible())
                }
            }

            add(canvasElement)
        }

        if (!clipCanvasToBounds) {
            style.setOverflow(Visible())
        }
    }

    private val boundsChanged: (View, Rectangle, Rectangle) -> Unit = { view,_,_ ->
        customCanvas?.let {
            it.size = view.size
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
            boundsChanged         += this@NativeFileSelector.boundsChanged
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
            boundsChanged         -= this@NativeFileSelector.boundsChanged
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

    private var ignoreChange = false

    override fun onChange(event: Event): Boolean {
        if (!ignoreChange) {
            inputElement.files?.asList()?.map { SimpleFile(it) }?.let {
                filesChanged(it)
                ignoreChange = true
                inputElement.value = ""
                ignoreChange = false
            }
        }

        return true
    }
}