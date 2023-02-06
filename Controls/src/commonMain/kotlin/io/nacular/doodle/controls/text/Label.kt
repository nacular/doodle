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
import io.nacular.doodle.utils.HorizontalAlignment.Center
import io.nacular.doodle.utils.VerticalAlignment
import io.nacular.doodle.utils.VerticalAlignment.Middle
import io.nacular.doodle.utils.dimensionSetProperty
import io.nacular.doodle.utils.observable


public interface LabelBehavior: Behavior<Label> {
    public val Label.textSize: Size get() = _textSize

    public fun measureText(label: Label): Size
}

public open class Label(
    styledText         : StyledText          = StyledText(""),
    verticalAlignment  : VerticalAlignment   = Middle,
    horizontalAlignment: HorizontalAlignment = Center): View() {

    public constructor(
            text               : String,
            verticalAlignment  : VerticalAlignment   = Middle,
            horizontalAlignment: HorizontalAlignment = Center): this(StyledText(text), verticalAlignment, horizontalAlignment)

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
            field = new
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
    public var horizontalAlignment: HorizontalAlignment by observable(horizontalAlignment) { _,_ -> measureText(); rerender() }

    public var lineSpacing  : Float  = 1f
    public var letterSpacing: Double = 0.0

    internal val _textSize get() = textSize

    private var textSize = Empty
        set(new) {
            field = new
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
}
