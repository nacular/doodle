package io.nacular.doodle.controls.text

import io.nacular.doodle.controls.text.TextField.Purpose.Text
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.Dimension
import io.nacular.doodle.utils.Dimension.Height
import io.nacular.doodle.utils.Dimension.Width
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.observable


public interface TextFieldBehavior: Behavior<TextField> {
    public fun fitTextSize(textField: TextField): Size
}

public open class TextField(text: String = ""): TextInput(text) {

    /**
     * Defines set of common uses for TextFields that can help with specializing
     * their rendering/behavior.
     */
    public enum class Purpose {
        Email,
        Search,
        Telephone,
        Number,
        Integer,
        Url,
        Text,
        Password,
    }

    /**
     * Default text shown when the TextField is empty.
     */
    public var placeHolder: String by observableStyleProperty("") { _,new ->
        role.placeHolder = new
    }

    /**
     * Font used to render [placeHolder].
     */
    public var placeHolderFont: Font? by styleProperty(font)

    /**
     * Color used to render [placeHolder].
     */
    public var placeHolderColor: Color? by styleProperty(foregroundColor)

    /**
     * Color of selected text.
     */
    public var selectionForegroundColor: Color? by styleProperty(null)

    /**
     * Color of selected text background (i.e. highlight color).
     */
    public var selectionBackgroundColor: Color? by styleProperty(null)

    /**
     * Determines whether the TextField resizes to fit its text's width, height, or both.
     */
    public var fitText: Set<Dimension> by observable(emptySet()) { _,_ ->
        fitText()
    }

    /**
     * Indicates to [behavior] that a border should or should not be rendered.
     */
    public var borderVisible: Boolean by styleProperty(true)

    /**
     * `true` IFF [mask] is NOT `nul`
     */
    public val masked: Boolean get() = mask != null

    /**
     * Notified whenever [mask] changes.
     */
    public val maskChanged: PropertyObservers<TextField, Char?> by lazy { PropertyObserversImpl(this) }

    /**
     * Optional character to mask text shown in the field. This only affects [displayText].
     */
    public var mask: Char? = null
        set(new) {
            if (field == new) return

            val old = field
            field   = new

            displayText = when (val m = mask) {
                null -> text
                else -> "".padEnd(text.length, m)
            }

            (maskChanged as PropertyObserversImpl<TextField, Char?>)(old, new)
        }

    /**
     * Indicates what a TextField will be used for. Behaviors can use this to change the way
     * it is displayed, or which keyboard is shown for it on mobile devices.
     */
    public var purpose: Purpose by observableStyleProperty(Text) { old, new ->
        (purposeChanged as PropertyObserversImpl)(old, new)
    }

    /**
     * Notified whenever [purpose] changes.
     */
    public val purposeChanged: PropertyObservers<TextField, Purpose> by lazy { PropertyObserversImpl(this) }

    public var behavior: TextFieldBehavior? by behavior(
        beforeChange = { _,_ -> mirrorWhenRightLeft = false },
        afterChange  = { _,_ -> fitText()                   }
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
            displayText = when (val m = mask) {
                null -> new
                else -> {
                    when {
                        displayText.length > text.length -> displayText.substring(0, text.length)
                        else                             -> displayText.padEnd   (text.length - displayText.length, m)
                    }
                }
            }
            super.text  = new
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

            if (Width  in fitText) suggestWidth (size.width )
            if (Height in fitText) suggestHeight(size.height)
        }
    }
}
