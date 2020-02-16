package com.nectar.doodle.controls.text

import com.nectar.doodle.controls.text.TextFit.Height
import com.nectar.doodle.controls.text.TextFit.Width
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.nectar.doodle.text.StyledText
import com.nectar.doodle.text.invoke
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.utils.HorizontalAlignment
import com.nectar.doodle.utils.HorizontalAlignment.Center
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.VerticalAlignment
import com.nectar.doodle.utils.VerticalAlignment.Middle
import com.nectar.doodle.utils.VerticalAlignment.Top
import com.nectar.doodle.utils.observable


interface LabelFactory {
    operator fun invoke(
            styledText         : StyledText          = StyledText(""),
            verticalAlignment  : VerticalAlignment   = Middle,
            horizontalAlignment: HorizontalAlignment = Center): Label

    operator fun invoke(
            text               : String,
            verticalAlignment  : VerticalAlignment   = Middle,
            horizontalAlignment: HorizontalAlignment = Center) = this(StyledText(text), verticalAlignment, horizontalAlignment)
}

class LabelFactoryImpl(private val textMetrics: TextMetrics): LabelFactory {
    override operator fun invoke(
            styledText         : StyledText,
            verticalAlignment  : VerticalAlignment,
            horizontalAlignment: HorizontalAlignment) = Label(textMetrics, styledText, verticalAlignment, horizontalAlignment)
}

open class Label internal constructor(
        private val textMetrics        : TextMetrics,
                    styledText         : StyledText          = StyledText(""),
                    verticalAlignment  : VerticalAlignment   = Middle,
                    horizontalAlignment: HorizontalAlignment = Center): View() {

    var fitText = setOf(Width, Height)

    var text: String get() = styledText.text
        set(new) {
            styledText = StyledText(new, font, foregroundColor?.let { ColorBrush(it) })
        }

    var styledText = font?.invoke { foregroundColor?.invoke { styledText } ?: styledText } ?: styledText
        set(new) {
            if (new == field) { return }

            field = font?.invoke { foregroundColor?.invoke { new } ?: new } ?: new
            measureText()

            rerender()
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

    var textSize = Empty
        private set(new) {
            field = new
            if (Width  in fitText) width  = new.width
            if (Height in fitText) height = new.height
        }

    var behavior: Behavior<Label>? = null
        set(new) {
            if (field == new) { return }

            field?.uninstall(this)
            field = new?.apply { install(this@Label) }
        }

    private fun measureText(): Size {
        val height = when {
            Height in fitText || verticalAlignment != Top -> if (wrapsWords) textMetrics.height(styledText, width) else textMetrics.height(styledText)
            else                                          -> 0.0
        }

        val width = when {
            Width in fitText || horizontalAlignment != Left -> if (wrapsWords) textMetrics.width(styledText, width) else textMetrics.width(styledText)
            else                                            -> 0.0
        }

        return Size(width, height).also { textSize = it }
    }

    init {
        boundsChanged += { _,old,new ->
            if (old.size != new.size) {
                measureText()
            }
        }

        styleChanged += {
            text = text

            rerender()
        }

        size = textSize
    }

    override var focusable = false

    override fun addedToDisplay() {
        if (textSize.empty) {
            measureText()
        }
    }

    override fun render(canvas: Canvas) { behavior?.render(this, canvas) }
}
