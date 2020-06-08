package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.ToggleButton
import io.nacular.doodle.core.Icon
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.ColorBrush
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.Anchor
import io.nacular.doodle.utils.HorizontalAlignment.Left
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 4/25/19.
 */
open class CheckRadioButtonBehavior<T: ToggleButton> protected constructor(
        private val textMetrics: TextMetrics,
        private val icon       : Icon<T>,
        private val spacing    : Double = 2.0): CommonTextButtonBehavior<T>(textMetrics) {

    protected val insets = Insets()

    override fun render(view: T, canvas: Canvas) {
        val icon      = icon(view)
        val textColor = if (view.enabled) Black else Color(0xccccccu)

        canvas.text(view.text, font(view), textPosition(view, icon = icon), ColorBrush(textColor))

        icon?.render(view, canvas, iconPosition(view, icon = this.icon as Icon<Button>))
    }

    override fun install(view: T) {
        super.install(view)

        val iconSize   = icon.size(view)
        val textSize   = textMetrics.size(view.text, font(view))
        val idealWidth = iconSize.width + if (textSize.width > 0) spacing + textSize.width else 0.0

        view.icon                = icon as Icon<Button>
        view.iconAnchor          = Anchor.Left
        view.minimumSize         = iconSize
        view.iconTextSpacing     = spacing
        view.horizontalAlignment = Left

        Size(idealWidth, max(iconSize.height, if (!textSize.empty) textSize.height else 0.0)).let {
            view.idealSize = it
            view.size      = it
        }
    }
}