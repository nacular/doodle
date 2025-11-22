package io.nacular.doodle.controls.text

import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.text.invoke
import io.nacular.doodle.utils.TextAlignment
import io.nacular.doodle.utils.TextAlignment.Center
import io.nacular.doodle.utils.VerticalAlignment
import io.nacular.doodle.utils.VerticalAlignment.Middle
import io.nacular.doodle.utils.observable


public interface LabelBehavior: Behavior<Label> {
    public val Label.textSize: Size get() = textSize_

    public fun measureText(label: Label): Size

    public fun measureText(label: Label, assumeWidth: Double = label.width): Size = measureText(label)
}

public open class Label(
    styledText         : StyledText        = StyledText(""),
    verticalAlignment  : VerticalAlignment = Middle,
    horizontalAlignment: TextAlignment     = Center
): View() {
    /**
     * Text displayed by the Label.
     */
    public var text: String get() = styledText.text; set(new) {
        styledText = StyledText(new)
    }

    // this a clone of actualStyledText with the Label's styling applied
    private var visibleStyledText = styledText; set(value) {
        field = value
        foregroundColor?.invoke { field }
        font?.invoke            { field }
    }

    // this is the styled-text that is set by a caller
    private var actualStyledText = styledText; set(new) {
        val old           = visibleStyledText
        field             = new
        visibleStyledText = field.copy()

        if (visibleStyledText != old) {
            measureText()
            rerender()
        }
    }

    /**
     * Text displayed by the label with styles.
     */
    public var styledText: StyledText get() = visibleStyledText; set(new) {
        actualStyledText = new
    }

    /**
     * Determines if the Label will wrap text when its width is too short to
     * show it all.
     */
    public var wrapsWords: Boolean = false; set(new) {
        if (field != new) {
            field = new
            measureText()
            rerender()
        }
    }

    /**
     * Alignment of text along the vertical axis.
     */
    public var verticalAlignment: VerticalAlignment by observable(verticalAlignment  ) { _,_ -> measureText(); rerender() }

    /**
     * Alignment of text along the horizontal axis.
     */
    public var textAlignment: TextAlignment by observable(horizontalAlignment) { _,_ -> measureText(); rerender() }

    /**
     * Space between lines in [text] (when [wrapsWords] == `true`) in terms of the [font] height.
     */
    public var lineSpacing: Float by observable(-1f) { _,_ -> if (wrapsWords) { measureText(); rerender() } }

    /**
     * Space between the words in [text].
     */
    public var wordSpacing: Double by observable(0.0) { _,_ -> measureText(); rerender() }

    /**
     * Space between the letters in [text].
     */
    public var letterSpacing: Double by observable(0.0) { _,_ -> measureText(); rerender() }

    internal var textSize_ = Empty; private set(new) {
        field = new

        var cachedSize  = Empty
        var cachedWidth = 0.0

        preferredSize = { min, max ->
            when {
                wrapsWords && width !in min.width .. max.width -> {
                    when (max.width) {
                        cachedWidth -> cachedSize
                        else        -> {
                            cachedWidth = max.width
                            (behavior?.measureText(this@Label, assumeWidth = max.width) ?: new).also { cachedSize = it }
                        }
                    }
                }
                else                             -> new
            }
        }

        suggestSize(new)
    }

    public var behavior: LabelBehavior? by behavior { _,new ->
        mirrorWhenRightLeft = false
        measureText(new)
    }

    private fun measureText(behavior: LabelBehavior? = this.behavior): Size {
        return behavior?.measureText(this)?.also { textSize_ = it } ?: Empty
    }

    init {
        styleChanged += {
            // force update
            actualStyledText = actualStyledText

            rerender()
        }

        boundsChanged += { _,old,new ->
            if (wrapsWords && old.width != new.width) {
                measureText()
            }
        }

        mirrorWhenRightLeft = false

        // force update
        actualStyledText = actualStyledText

        suggestSize(textSize_)
    }

    override var focusable: Boolean = false

    override fun addedToDisplay() {
        if (textSize_.empty) {
            measureText()
        }

        // force update
        actualStyledText = actualStyledText
    }

    override fun render(canvas: Canvas) { behavior?.render(this, canvas) }

    override fun toString(): String = text

    public companion object {
        public operator fun invoke(
            text               : String,
            verticalAlignment  : VerticalAlignment = Middle,
            horizontalAlignment: TextAlignment     = Center,
        ): Label = Label(StyledText(text), verticalAlignment, horizontalAlignment)

        public operator fun invoke(
            styledText         : StyledText        = StyledText(""),
            verticalAlignment  : VerticalAlignment = Middle,
            horizontalAlignment: TextAlignment     = Center,
        ): Label = Label(styledText, verticalAlignment, horizontalAlignment)
    }
}
