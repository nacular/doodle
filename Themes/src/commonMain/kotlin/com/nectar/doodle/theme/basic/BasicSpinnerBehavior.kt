package com.nectar.doodle.theme.basic

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.buttons.PushButton
import com.nectar.doodle.controls.spinner.Model
import com.nectar.doodle.controls.spinner.Spinner
import com.nectar.doodle.controls.spinner.SpinnerBehavior
import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.core.Icon
import com.nectar.doodle.drawing.AffineTransform.Companion.Identity
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.Black
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.drawing.lighter
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.constant
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.utils.Anchor

class BasicSpinnerBehavior(private val textMetrics: TextMetrics, private val labelFactory: LabelFactory): SpinnerBehavior<Any, Model<Any>>() {

    private class ButtonIcon(private val color: Color, private val disabledColor: Color, private val isUp: Boolean): Icon<Button> {
        override fun size(view: Button) = Size(view.width * 0.3, view.height * 0.3)

        override fun render(view: Button, canvas: Canvas, at: Point) {
            val size = size(view)
            val transform = when {
                isUp -> Identity
                else -> Identity.flipVertically(at.y + size.height / 2)
            }

            val pen = Pen(when {
                view.enabled -> color
                else         -> disabledColor
            }, 2.0)

            canvas.transform(transform) {
                path(listOf(
                        Point(at.x,                  at.y + size.height),
                        Point(at.x + size.width / 2, at.y              ),
                        Point(at.x + size.width,     at.y + size.height)), pen)
            }
        }
    }

    private inner class SpinnerButtonBehavior(
            private val isTop: Boolean,
            private val cornerRadius: Double = 4.0): BasicButtonBehavior(textMetrics, Color.Lightgray) {
        override fun install(view: Button) {
            view.icon = ButtonIcon(Black, Color.Lightgray.lighter(), isTop)

            super.install(view)
        }

        override fun render(view: Button, canvas: Canvas) {
            val colors = colors(view)

            val fill = if (view.enabled) colors.fillColor else Color.Lightgray

            canvas.rect(Rectangle(-cornerRadius, 0.0 - if (!isTop) cornerRadius else 0.0, view.width + cornerRadius, view.height + cornerRadius), cornerRadius, ColorBrush(fill))

            icon(view)?.let {
                val adjust = it.size(view).height / 3 * if (isTop) 1 else -1
                it.render(view, canvas, iconPosition(view, icon = it) + Point(0.0, adjust))
            }
        }
    }


    override fun changed(spinner: Spinner<Any, Model<Any>>) {}

    override fun render(view: Spinner<Any, Model<Any>>, canvas: Canvas) {
        canvas.rect(view.bounds.atOrigin, 4.0, ColorBrush(Color.Lightgray))
    }

    override fun install(view: Spinner<Any, Model<Any>>) {
        super.install(view)

        val center = labelFactory(view.value.toString()).apply { fitText = emptySet() }
        val next = PushButton().apply {
            iconAnchor    = Anchor.Leading
            enabled       = view.hasNext
            acceptsThemes = false
            behavior      = SpinnerButtonBehavior(true)
        }

        val previous = PushButton().apply {
            iconAnchor    = Anchor.Leading
            enabled       = view.hasPrevious
            acceptsThemes = false
            behavior      = SpinnerButtonBehavior(false)
        }

        view.changed += {
            center.text      = it.value.toString() // TODO: Define string converter?
            next.enabled     = it.hasNext
            previous.enabled = it.hasPrevious
        }

        next.fired += {
            view.next()
        }

        previous.fired += {
            view.previous()
        }

        view.children += listOf(center, next, previous)

        view.layout = constrain(center, next, previous) { center, next, previous ->
            center.top      = parent.top
            center.left     = parent.left
            center.right    = next.left
            center.bottom   = parent.bottom

            next.top        = parent.top
            next.right      = parent.right
            next.bottom     = parent.centerY
            next.width      = constant(40.0)

            previous.top    = next.bottom
            previous.left   = next.left
            previous.right  = next.right
            previous.bottom = parent.bottom //center.bottom
        }
    }
}