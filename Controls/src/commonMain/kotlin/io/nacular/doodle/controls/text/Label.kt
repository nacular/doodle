package io.nacular.doodle.controls.text

import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.text.AnnotatedString
import io.nacular.doodle.text.invoke
import io.nacular.doodle.utils.TextAlignment
import io.nacular.doodle.utils.TextAlignment.Center
import io.nacular.doodle.utils.VerticalAlignment
import io.nacular.doodle.utils.VerticalAlignment.Middle


public interface LabelBehavior: Behavior<Label> {
    public val Label.textSize: Size get() = textSize_

    public fun measureText(label: Label): Size

    public fun measureText(label: Label, assumeWidth: Double = label.width): Size = measureText(label)
}

public open class Label(
    annotatedText         : AnnotatedString        = AnnotatedString(""),
    verticalAlignment  : VerticalAlignment = Middle,
    horizontalAlignment: TextAlignment     = Center
): View() {
    /**
     * Text displayed by the Label.
     */
    public var text: String get() = annotatedText.text; set(new) {
        annotatedText = AnnotatedString(new)
    }

    // this a clone of actualStyledText with the Label's styling applied
    private var visibleStyledText = annotatedText; set(value) {
        field = value
        foregroundColor?.invoke { field }
        font?.invoke            { field }
    }

    // this is the styled-text that is set by a caller
    private var actualStyledText = annotatedText; set(new) {
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
    public var annotatedText: AnnotatedString get() = visibleStyledText; set(new) {
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
    public var verticalAlignment: VerticalAlignment = verticalAlignment; set(new) { field = new; measureText(); rerender() }

    /**
     * Alignment of text along the horizontal axis.
     */
    public var textAlignment: TextAlignment = horizontalAlignment; set(new) { if (field == new) return; field = new; measureText(); rerender() }

    /**
     * Space between lines in [text] (when [wrapsWords] == `true`) in terms of the [font] height.
     */
    public var lineSpacing: Float = -1f; set(new) { if (field == new) return; field = new; if (wrapsWords) { measureText(); rerender() } }

    /**
     * Space between the words in [text].
     */
    public var wordSpacing: Double = 0.0; set(new) { if (field == new) return; field = new; measureText(); rerender() }

    /**
     * Space between the letters in [text].
     */
    public var letterSpacing: Double = 0.0; set(new) { if (field == new) return; field = new; measureText(); rerender() }

    /**
     * Determines whether the Label's [width] is suggested to match its text's width.
     */
    public var autoSuggestWidth: Boolean = true

    /**
     * Determines whether the Label's [height] is suggested to match its text's height.
     */
    public var autoSuggestHeight: Boolean = true

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
                else -> new
            }
        }

        suggestSizeInternal(new)
    }

    private fun suggestSizeInternal(size: Size) {
        when {
            autoSuggestWidth && autoSuggestHeight -> suggestSize  (size            )
            autoSuggestWidth                      -> suggestWidth (textSize_.width )
            autoSuggestHeight                     -> suggestHeight(textSize_.height)
        }
    }

    public var behavior: LabelBehavior? by behavior { _,new ->
        mirrorWhenRightLeft = false
        measureText(new)
    }

    private fun measureText(behavior: LabelBehavior? = this.behavior) {
        behavior?.measureText(this)?.also { textSize_ = it }
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

        suggestSizeInternal(textSize_)
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
        ): Label = Label(AnnotatedString(text), verticalAlignment, horizontalAlignment)

        public operator fun invoke(
            annotatedText         : AnnotatedString        = AnnotatedString(""),
            verticalAlignment  : VerticalAlignment = Middle,
            horizontalAlignment: TextAlignment     = Center,
        ): Label = Label(annotatedText, verticalAlignment, horizontalAlignment)
    }
}
