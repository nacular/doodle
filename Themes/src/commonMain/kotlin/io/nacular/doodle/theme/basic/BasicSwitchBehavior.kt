package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.Switch
import io.nacular.doodle.controls.theme.CommonButtonBehavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.interpolate
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.NoOpCompletable
import io.nacular.doodle.utils.cancelable


public open class BasicSwitchBehavior(
        private val onBackground : Color         = Blue,
        private val onForeground : Color         = White,
        private val offBackground: Color         = Lightgray,
        private val offForeground: Color         = onForeground,
                    focusManager : FocusManager? = null): CommonButtonBehavior<Switch>(focusManager) {

    public var hoverColorMapper   : ColorMapper = { it.darker(0.1f) }
    public var disabledColorMapper: ColorMapper = { it.lighter()    }

    private var progress = 0f
    private var activeTransition: Completable? by cancelable(null)

    public open fun transitionSlider(block: (Float) -> Unit): Completable = NoOpCompletable.also { block(1f) }

    override val selectionChanged: (Button, Boolean, Boolean) -> Unit = { button, _, new ->
        val start = progress
        val end   = when {
            new  -> 1f
            else -> 0f
        }

        activeTransition = transitionSlider {
            progress = start * (1 - it) + end * it
            button.rerenderNow()
        }.apply {
            completed += { activeTransition = null }
            canceled  += { activeTransition = null }
        }
    }

    override fun install(view: Switch) {
        super.install(view)

        progress = when {
            view.selected -> 1f
            else          -> 0f
        }
    }

    override fun render(view: Switch, canvas: Canvas) {
        val radius          = view.height / 2
        val border          = maxOf(2.0, view.height / 20)
        val circleColor     = color(view, offForeground, onForeground)
        val backgroundColor = color(view, offBackground, onBackground)

        canvas.rect(view.bounds.atOrigin, radius, ColorPaint(backgroundColor))

        val center = Point(radius + (view.width - 2 * radius) * progress, radius)

        canvas.circle(Circle(center, radius - border), ColorPaint(circleColor))
    }

    private fun color(button: Button, start: Color, end: Color) = interpolate(start, end, progress).let {
        when {
            !button.enabled          -> disabledColorMapper(it)
            button.model.pointerOver -> hoverColorMapper   (it)
            else                     -> it
        }
    }
}