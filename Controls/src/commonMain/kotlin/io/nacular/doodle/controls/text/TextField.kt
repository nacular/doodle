package io.nacular.doodle.controls.text

import io.nacular.doodle.core.Behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl



interface TextFieldBehavior: Behavior<TextField> {
    fun fitTextSize(textField: TextField): Size
}

open class TextField(text: String = ""): TextInput(text) {
    var placeHolder = ""
        set(new) {
            field = new
            styleChanged { true }
        }

    var fitText = emptySet<TextFit>()
        set(new) {
            field = new

            fitText()
        }

    var borderVisible = true
        set(new) {
            field = new
            styleChanged { true }
        }

    val masked get() = mask != null

    val maskChanged: PropertyObservers<TextField, Char?> by lazy { PropertyObserversImpl<TextField, Char?>(this) }

    var mask: Char? = null
        set(new) {
            if (field == new) return

            val old = field
            field   = new

            mask?.let {
                displayText = "".padEnd(text.length, it)
            }

            (maskChanged as PropertyObserversImpl<TextField, Char?>)(old, new)
        }

    var behavior: TextFieldBehavior? = null
        set(new) {
            if (field == new) { return }

            clipCanvasToBounds = true
            field?.uninstall(this)

            field = new?.also {
                it.install(this)
                clipCanvasToBounds = it.clipCanvasToBounds(this)
            }

            fitText()
        }

    init {
        boundsChanged += { _,_,_ ->
            fitText()
        }
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override var text
        get(   ) = super.text
        set(new) {
            super.text = new

            displayText = when (val m = mask) {
                null -> new
                else -> {
                    if (displayText.length > text.length) {
                        displayText.substring(0, text.length)
                    } else {
                        displayText.padEnd(text.length - displayText.length, m)
                    }
                }
            }
        }

    override fun cut() = copy().also { deleteSelected() }

    override fun copy() = displayText.substring(selection.start, selection.end)

    var displayText = text
        private set(new) {
            field = new
            fitText()
        }

    private fun fitText() {
        behavior?.let {
            val size = it.fitTextSize(this)

            if (TextFit.Width  in fitText) width  = size.width
            if (TextFit.Height in fitText) height = size.height
        }
    }
}
