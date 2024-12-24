package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.text.LabelBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.text.Style
import io.nacular.doodle.text.TextDecoration
import io.nacular.doodle.text.TextSpacing
import io.nacular.doodle.utils.ChangeObserver
import io.nacular.doodle.utils.Dimension.Height
import io.nacular.doodle.utils.Dimension.Width
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.TextAlignment.Center
import io.nacular.doodle.utils.TextAlignment.End
import io.nacular.doodle.utils.TextAlignment.Justify
import io.nacular.doodle.utils.TextAlignment.Start
import io.nacular.doodle.utils.VerticalAlignment.Bottom
import io.nacular.doodle.utils.VerticalAlignment.Middle
import io.nacular.doodle.utils.VerticalAlignment.Top

/**
 * Behavior providing basic rendering of [Label]s.
 */
public open class CommonLabelBehavior(
        private        val textMetrics    : TextMetrics,
        protected open val foregroundColor: Color? = null,
        protected open val backgroundColor: Color? = null
): LabelBehavior {

    /** Controls how [Color]s are changed when the rendered [Label] is disabled. */
    public var disabledColorMapper: (Color) -> Color = { it.lighter() }

    private var labelDesiredForeground: Color? = null
    private var labelDesiredBackground: Color? = null

    private val enabledChanged: PropertyObserver<View, Boolean> = { view,_,_ ->
        view.rerender()
    }

    private val styleChanged: ChangeObserver<View> = {
        labelDesiredForeground = it.foregroundColor
        labelDesiredBackground = it.backgroundColor
    }

    override fun install(view: Label) {
        styleChanged(view)

        view.enabledChanged += enabledChanged

        foregroundColor?.let { view.foregroundColor = it }
        backgroundColor?.let { view.backgroundColor = it }

        view.styleChanged += styleChanged
    }

    override fun uninstall(view: Label) {
        view.enabledChanged -= enabledChanged
        view.styleChanged   -= styleChanged

        view.foregroundColor = labelDesiredForeground
        view.backgroundColor = labelDesiredBackground
    }

    override fun render(view: Label, canvas: Canvas) {
        view.apply {
            val y = when (verticalAlignment) {
                Top    -> 0.0
                Middle -> (height - textSize_.height) / 2
                Bottom ->  height - textSize_.height
            }

            val x = if (!wrapsWords) {
                when (textAlignment) {
                    Start   -> 0.0
                    Center  -> (width - textSize_.width) / 2
                    End     ->  width - textSize_.width
                    Justify -> 0.0
                }
            } else 0.0

            val bgColor = when {
                view.enabled -> backgroundColor
                else         -> backgroundColor?.let(disabledColorMapper)
            }

            bgColor?.let {
                canvas.rect(bounds.atOrigin, color = it)
            }

            val renderedText = when {
                view.enabled -> styledText
                else         -> styledText.mapStyle {
                    object: Style {
                        override val font       get() = it.font
                        override val foreground get() = when (val paint = it.foreground) {
                            is ColorPaint -> disabledColorMapper(paint.color).paint
                            else          -> paint
                        }
                        override val background get() = when (val paint = it.background) {
                            is ColorPaint -> disabledColorMapper(paint.color).paint
                            else          -> paint
                        }
                        override val decoration: TextDecoration? get() = when (val d = it.decoration) {
                            null -> null
                            else -> TextDecoration(
                                    d.lines,
                                    d.color?.let { disabledColorMapper(it) },
                                    d.style,
                                    d.thickness
                            )
                        }

                        override val stroke: Stroke? get() = it.stroke
                    }
                }
            }

            val textSpacing = TextSpacing(letterSpacing = letterSpacing, wordSpacing = wordSpacing)

            when {
                wrapsWords -> canvas.wrapped(renderedText, Point(x, y), width = width, alignment = textAlignment, lineSpacing = lineSpacing, textSpacing = textSpacing)
                else       -> canvas.text   (renderedText, Point(x, y), textSpacing)
            }
        }
    }

    override fun measureText(label: Label): Size {
        val textSpacing = TextSpacing(letterSpacing = label.letterSpacing, wordSpacing = label.wordSpacing)

        val height = when {
            Height in label.fitText || label.verticalAlignment != Top -> if (label.wrapsWords) textMetrics.height(label.styledText, label.width, lineSpacing = label.lineSpacing, textSpacing = textSpacing) else textMetrics.height(label.styledText)
            else                                                      -> 0.0
        }

        val width = when {
            Width in label.fitText || label.textAlignment != Start    -> if (label.wrapsWords) textMetrics.width(label.styledText, label.width, textSpacing = textSpacing) else textMetrics.width(label.styledText, textSpacing = textSpacing)
            else                                                      -> 0.0
        }

        return Size(width, height)
    }
}