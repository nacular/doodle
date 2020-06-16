package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.TextItemVisualizer
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.spinner.Model
import io.nacular.doodle.controls.spinner.Spinner
import io.nacular.doodle.controls.spinner.SpinnerBehavior
import io.nacular.doodle.controls.text.LabelFactory
import io.nacular.doodle.controls.toString
import io.nacular.doodle.core.Icon
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.ColorBrush
import io.nacular.doodle.drawing.Pen
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.ConstraintLayout
import io.nacular.doodle.layout.constant
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.utils.Anchor

class BasicSpinnerBehavior(private val textMetrics: TextMetrics): SpinnerBehavior<Any, Model<Any>>() {

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

    private val itemVisualizer by lazy { toString<Any>(TextItemVisualizer(textMetrics, fitText = emptySet())) }

    override fun changed(spinner: Spinner<Any, Model<Any>>) {}

    override fun render(view: Spinner<Any, Model<Any>>, canvas: Canvas) {
        canvas.rect(view.bounds.atOrigin, 4.0, ColorBrush(Color.Lightgray))
    }

    override fun install(view: Spinner<Any, Model<Any>>) {
        super.install(view)

        val itemVisualizer = view.itemVisualizer ?: itemVisualizer

        val center = itemVisualizer(view.value)
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
            val newCenter = itemVisualizer(view.value, center)

            if (newCenter != center) {
                view.children -= center
                (view.layout as? ConstraintLayout)?.unconstrain(center)

                view.children += newCenter

                (view.layout as? ConstraintLayout)?.constrain(newCenter, next) { center, next ->
                    center.top = parent.top
                    center.left = parent.left
                    center.right = next.left
                    center.bottom = parent.bottom
                }
            }

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