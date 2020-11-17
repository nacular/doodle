package io.nacular.doodle.controls.text

import io.nacular.doodle.controls.text.TextFit.Height
import io.nacular.doodle.controls.text.TextFit.Width
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.text.invoke
import io.nacular.doodle.utils.HorizontalAlignment
import io.nacular.doodle.utils.HorizontalAlignment.Center
import io.nacular.doodle.utils.VerticalAlignment
import io.nacular.doodle.utils.VerticalAlignment.Middle
import kotlin.properties.Delegates.observable


interface LabelBehavior: Behavior<Label> {
    val Label.textSize get() = _textSize

    fun measureText(label: Label): Size
}

open class Label(styledText         : StyledText          = StyledText(""),
                 verticalAlignment  : VerticalAlignment   = Middle,
                 horizontalAlignment: HorizontalAlignment = Center): View() {

    constructor(
            text               : String,
            verticalAlignment  : VerticalAlignment   = Middle,
            horizontalAlignment: HorizontalAlignment = Center): this(StyledText(text), verticalAlignment, horizontalAlignment)

    var fitText = setOf(Width, Height)
        set(new) {
            field = new
            measureText()
        }

    var text: String get() = styledText.text
        set(new) {
            styledText = StyledText(new)
        }

    // this a clone of actualStyledText with the Label's styling applied
    private var visibleStyledText = styledText
        set(value) {
            field = value
            foregroundColor?.invoke { field }
            font?.invoke { field }
        }

    // this is the styled-text that is set by a caller
    private var actualStyledText = styledText
        set(new) {
            field = new
            visibleStyledText = field.copy()

            measureText()

            rerender()
        }

    var styledText get() = visibleStyledText
        set(new) {
            actualStyledText = new
        }

    var wrapsWords = false
        set(new) {
            if (field != new) {
                field = new
                measureText()
            }
        }

    var verticalAlignment   by observable(verticalAlignment  ) { _,_,_ -> measureText(); rerender() }
    var horizontalAlignment by observable(horizontalAlignment) { _,_,_ -> measureText(); rerender() }

    internal val _textSize get() = textSize

    private var textSize = Empty
        set(new) {
            field = new
            idealSize = new
            if (Width  in fitText) width  = new.width
            if (Height in fitText) height = new.height
        }

    var behavior: LabelBehavior? by behavior { _,new ->
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

    override var focusable = false

    override fun addedToDisplay() {
        if (textSize.empty) {
            measureText()
        }

        // force update
        actualStyledText = actualStyledText
    }

    override fun render(canvas: Canvas) { behavior?.render(this, canvas) }
}
