package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.text.LabelBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.text.Style
import io.nacular.doodle.text.TextDecoration
import io.nacular.doodle.utils.Dimension.Height
import io.nacular.doodle.utils.Dimension.Width
import io.nacular.doodle.utils.HorizontalAlignment.Center
import io.nacular.doodle.utils.HorizontalAlignment.Left
import io.nacular.doodle.utils.HorizontalAlignment.Right
import io.nacular.doodle.utils.PropertyObserver
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

    private val enabledChanged: PropertyObserver<View, Boolean> = { view,_,_ ->
        view.rerender()
    }

    override fun install(view: Label) {
        view.enabledChanged += enabledChanged
        foregroundColor?.let { view.foregroundColor = it }
        backgroundColor?.let { view.backgroundColor = it }
    }

    override fun uninstall(view: Label) {
        view.enabledChanged -= enabledChanged
        view.foregroundColor = null // FIXME: This might override a user-pref
    }

    override fun render(view: Label, canvas: Canvas) {
        view.apply {
            val y = when (verticalAlignment) {
                Top    -> 0.0
                Middle -> (height - textSize.height) / 2
                Bottom ->  height - textSize.height
            }

            val x = if (!wrapsWords) {
                when (horizontalAlignment) {
                    Left   -> 0.0
                    Center -> (width - textSize.width) / 2
                    Right  ->  width - textSize.width
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
                    }
                }
            }

            if (wrapsWords) {
                canvas.wrapped(renderedText, Point(x, y), 0.0, width, alignment = view.horizontalAlignment, lineSpacing = view.lineSpacing, letterSpacing = view.letterSpacing)
            } else {
                canvas.text(renderedText, Point(x, y), letterSpacing = letterSpacing)
            }
        }
    }

    override fun measureText(label: Label): Size {
        val height = when {
            Height in label.fitText || label.verticalAlignment != Top -> if (label.wrapsWords) textMetrics.height(label.styledText, label.width, lineSpacing = label.lineSpacing) else textMetrics.height(label.styledText)
            else                                                      -> 0.0
        }

        val width = when {
            Width in label.fitText || label.horizontalAlignment != Left -> if (label.wrapsWords) textMetrics.width(label.styledText, label.width, letterSpacing = label.letterSpacing) else textMetrics.width(label.styledText, letterSpacing = label.letterSpacing)
            else                                                        -> 0.0
        }

        return Size(width, height)
    }
}