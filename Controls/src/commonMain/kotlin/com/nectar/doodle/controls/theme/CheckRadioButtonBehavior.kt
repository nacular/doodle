package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.buttons.ToggleButton
import com.nectar.doodle.core.Icon
import com.nectar.doodle.core.View
import com.nectar.doodle.core.height
import com.nectar.doodle.core.width
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.Black
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.utils.Anchor
import com.nectar.doodle.utils.HorizontalAlignment.Left
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 4/25/19.
 */
open class CheckRadioButtonBehavior protected constructor(
        private val textMetrics: TextMetrics,
        private val icon       : Icon<Button>,
        private val spacing    : Double = 2.0): CommonTextButtonBehavior<ToggleButton>(textMetrics) {

    protected val insets = Insets()

    private val styleChanged: (View) -> Unit = {
        it.rerender()
    }

    private val selectionChanged: (ToggleButton, Boolean, Boolean) -> Unit = { button,_,_ ->
        button.rerender()
    }

    override fun render(view: ToggleButton, canvas: Canvas) {
        val icon      = icon(view)
        val textColor = if (view.enabled) Black else Color(0xccccccu)

        canvas.text(view.text, font(view), textPosition(view, icon = icon), ColorBrush(textColor))

        icon?.render(view, canvas, iconPosition(view, icon = this.icon))
    }

    override fun install(view: ToggleButton) {
        super.install(view)

        val textSize   = textMetrics.size(view.text, font(view))
        val idealWidth = icon.width + if (textSize.width > 0) spacing + textSize.width else 0.0

        view.icon                = icon
        view.iconAnchor          = Anchor.Left
        view.minimumSize         = icon.size
        view.iconTextSpacing     = spacing
        view.horizontalAlignment = Left

        view.styleChanged    += styleChanged
        view.selectedChanged += selectionChanged

        Size(idealWidth, max(icon.height, if (!textSize.empty) textSize.height else 0.0)).let {
            view.idealSize = it
            view.size      = it
        }
    }

    override fun uninstall(view: ToggleButton) {
        super.uninstall(view)

        view.styleChanged    -= styleChanged
        view.selectedChanged -= selectionChanged
    }
}