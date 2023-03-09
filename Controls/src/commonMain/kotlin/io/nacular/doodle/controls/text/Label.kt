package io.nacular.doodle.controls.text

import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.text.invoke
import io.nacular.doodle.utils.Dimension
import io.nacular.doodle.utils.Dimension.Height
import io.nacular.doodle.utils.Dimension.Width
import io.nacular.doodle.utils.HorizontalAlignment
import io.nacular.doodle.utils.TextAlignment
import io.nacular.doodle.utils.TextAlignment.Center
import io.nacular.doodle.utils.TextAlignment.End
import io.nacular.doodle.utils.TextAlignment.Start
import io.nacular.doodle.utils.VerticalAlignment
import io.nacular.doodle.utils.VerticalAlignment.Middle
import io.nacular.doodle.utils.dimensionSetProperty
import io.nacular.doodle.utils.observable


public interface LabelBehavior: Behavior<Label> {
    public val Label.textSize: Size get() = _textSize

    public fun measureText(label: Label): Size
}

public open class Label(
    styledText         : StyledText        = StyledText(""),
    verticalAlignment  : VerticalAlignment = Middle,
    horizontalAlignment: TextAlignment     = Center
): View() {

    public var fitText: Set<Dimension> by dimensionSetProperty(setOf(Width, Height)) { _,_ -> measureText() }

    public var text: String get() = styledText.text
        set(new) {
            styledText = StyledText(new)
        }

    // this a clone of actualStyledText with the Label's styling applied
    private var visibleStyledText = styledText
        set(value) {
            field = value
            foregroundColor?.invoke { field }
            font?.invoke            { field }
        }

    // this is the styled-text that is set by a caller
    private var actualStyledText = styledText
        set(new) {
            field             = new
            visibleStyledText = field.copy()

            measureText()

            rerender()
        }

    public var styledText: StyledText
        get(   ) = visibleStyledText
        set(new) {
            actualStyledText = new
        }

    public var wrapsWords: Boolean = false
        set(new) {
            if (field != new) {
                field = new
                measureText()
            }
        }

    public var verticalAlignment  : VerticalAlignment   by observable(verticalAlignment  ) { _,_ -> measureText(); rerender() }

    @Deprecated("Use TextAlignment instead", ReplaceWith("textAlignment", imports = arrayOf("io.nacular.doodle.utils.TextAlignment")))
    public var horizontalAlignment: HorizontalAlignment get() = textAlignment.horizontalAlignment; set(value) {
        textAlignment = value.textAlignment
    }

    public var textAlignment: TextAlignment by observable(horizontalAlignment) { _,_ -> measureText(); rerender() }

    public var lineSpacing  : Float  by observable(1f) { _,_ -> if (wrapsWords) { measureText(); rerender() } }
    public var letterSpacing: Double by observable(0.0) { _,_ -> measureText(); rerender() }

    internal val _textSize get() = textSize

    private var textSize = Empty
        set(new) {
            field     = new
            idealSize = new
            if (Width  in fitText) width  = new.width
            if (Height in fitText) height = new.height
        }

    public var behavior: LabelBehavior? by behavior { _,new ->
        mirrorWhenRightLeft = false

        new?.measureText(this)?.also { textSize = it }
    }

    private fun measureText(): Size {
        return behavior?.measureText(this)?.also { textSize = it } ?: Empty
    }

    init {
        boundsChanged += { _,old,new ->
            if (old.size != new.size) {
                measureText()
            }
        }

        styleChanged += {
            // force update
            actualStyledText = actualStyledText

            rerender()
        }

        mirrorWhenRightLeft = false

        // force update
        actualStyledText = actualStyledText

        size = textSize
    }

    override var focusable: Boolean = false

    override fun addedToDisplay() {
        if (textSize.empty) {
            measureText()
        }

        // force update
        actualStyledText = actualStyledText
    }

    override fun render(canvas: Canvas) { behavior?.render(this, canvas) }

    public companion object {
        private val HorizontalAlignment.textAlignment get() = when (this) {
            HorizontalAlignment.Left  -> Start
            HorizontalAlignment.Center -> Center
            HorizontalAlignment.Right  -> End
        }

        private val TextAlignment.horizontalAlignment get() = when (this) {
            Center -> HorizontalAlignment.Center
            else   -> HorizontalAlignment.Center
        }

        public operator fun invoke(
            text               : String,
            verticalAlignment  : VerticalAlignment = Middle,
            horizontalAlignment: TextAlignment     = Center
        ): Label = Label(StyledText(text), verticalAlignment, horizontalAlignment)

        public operator fun invoke(
            text               : String,
            verticalAlignment  : VerticalAlignment   = Middle,
            horizontalAlignment: HorizontalAlignment
        ): Label = Label(StyledText(text), verticalAlignment, horizontalAlignment)

        public operator fun invoke(
            styledText         : StyledText          = StyledText(""),
            verticalAlignment  : VerticalAlignment   = Middle,
            horizontalAlignment: HorizontalAlignment
        ): Label = Label(styledText, verticalAlignment, horizontalAlignment.textAlignment)
    }
}
