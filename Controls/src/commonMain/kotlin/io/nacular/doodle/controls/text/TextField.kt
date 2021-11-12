package io.nacular.doodle.controls.text

import io.nacular.doodle.controls.text.TextFit.*
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.observable


public interface TextFieldBehavior: Behavior<TextField> {
    public fun fitTextSize(textField: TextField): Size
}

public open class TextField(text: String = ""): TextInput(text) {

    public var placeHolder: String by observableStyleProperty("") { _,new ->
        role.placeHolder = new
    }

    public var placeHolderFont: Font? by styleProperty(font)

    public var placeHolderColor: Color? by styleProperty(foregroundColor)

    public var selectionForegroundColor: Color? by styleProperty(null)

    public var selectionBackgroundColor: Color? by styleProperty(null)

    public var fitText: Set<TextFit> by observable(emptySet()) { _,_ ->
        fitText()
    }

    public var borderVisible: Boolean by styleProperty(true)

    public val masked: Boolean get() = mask != null

    public val maskChanged: PropertyObservers<TextField, Char?> by lazy { PropertyObserversImpl(this) }

    public var mask: Char? = null
        set(new) {
            if (field == new) return

            val old = field
            field   = new

            mask?.let {
                displayText = "".padEnd(text.length, it)
            }

            (maskChanged as PropertyObserversImpl<TextField, Char?>)(old, new)
        }

    public var behavior: TextFieldBehavior? by behavior(
            beforeChange = { _,_ -> mirrorWhenRightLeft = false },
            afterChange  = { _,_ -> fitText() }
    )

    init {
        boundsChanged += { _,old,new ->
            if (new.size != old.size) {
                fitText()
            }
        }
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override var text: String
        get(   ) = super.text
        set(new) {
            super.text  = new
            displayText = when (val m = mask) {
                null -> new
                else -> {
                    when {
                        displayText.length > text.length -> displayText.substring(0, text.length)
                        else                             -> displayText.padEnd   (text.length - displayText.length, m)
                    }
                }
            }
        }

    override fun cut(): String = copy().also { deleteSelected() }

    override fun copy(): String = displayText.substring(selection.start, selection.end)

    public var displayText: String = text
        private set(new) {
            field = new
            fitText()
        }

    private fun fitText() {
        behavior?.let {
            val size = it.fitTextSize(this)

            if (Width  in fitText) width  = size.width
            if (Height in fitText) height = size.height
        }
    }
}
