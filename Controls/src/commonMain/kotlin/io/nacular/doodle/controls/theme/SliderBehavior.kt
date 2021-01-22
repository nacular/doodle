package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.ContentDirection.LeftRight
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.KeyText.Companion.ArrowDown
import io.nacular.doodle.event.KeyText.Companion.ArrowLeft
import io.nacular.doodle.event.KeyText.Companion.ArrowRight
import io.nacular.doodle.event.KeyText.Companion.ArrowUp
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.Orientation.Vertical
import io.nacular.doodle.utils.size
import kotlin.math.round

/**
 * Created by Nicholas Eddy on 2/13/18.
 */

public abstract class SliderBehavior(private val focusManager: FocusManager?): Behavior<Slider>, PointerListener, PointerMotionListener, KeyListener {

    private   var lastStart          : Double = -1.0
    protected var lastPointerPosition: Double = -1.0
        private set

    private val changed: (Slider, Double, Double) -> Unit = { it,_,_ -> it.rerender() }

    override fun install(view: Slider) {
        view.changed              += changed
        view.keyChanged           += this
        view.pointerChanged       += this
        view.pointerMotionChanged += this
    }

    override fun uninstall(view: Slider) {
        view.changed              -= changed
        view.keyChanged           -= this
        view.pointerChanged       -= this
        view.pointerMotionChanged -= this
    }

    override fun pressed(event: PointerEvent) {
        val slider      = event.source as Slider
        val scaleFactor = scaleFactor(slider).let { if ( it != 0f) 1 / it else 0f }

        val offset = when (slider.orientation) {
            Horizontal -> event.location.x
            Vertical   -> event.location.y
        }

        val barSize     = barSize(slider)
        val barPosition = barPosition(slider)

        if (offset < barPosition || offset > barPosition + barSize) {
            slider.value += scaleFactor * (offset - (barPosition + barSize / 2))
        }

        lastPointerPosition = offset
        lastStart           = slider.value

        focusManager?.requestFocus(slider)

        event.consume()
    }

    override fun released(event: PointerEvent) {
        lastStart           = -1.0
        lastPointerPosition = -1.0
    }

    override fun pressed(event: KeyEvent) {
        val slider    = event.source as Slider
        val increment = slider.range.size / 100

        val (incrementKey, decrementKey) = when (slider.contentDirection) {
            LeftRight -> ArrowRight to ArrowLeft
            else      -> ArrowLeft  to ArrowRight
        }

        when (event.key) {
            ArrowUp,   incrementKey -> slider.value += increment
            ArrowDown, decrementKey -> slider.value -= increment
        }
    }

    override fun dragged(event: PointerEvent) {
        val slider = event.source as Slider

        val delta = when (slider.orientation) {
            Horizontal -> event.location.x - lastPointerPosition
            Vertical   -> event.location.y - lastPointerPosition
        }

        val deltaValue = delta / scaleFactor(slider)

        slider.value = lastStart + deltaValue

        event.consume()
    }

    private fun scaleFactor(slider: Slider): Float {
        val size = (if (slider.orientation === Horizontal) slider.width else slider.height) - barSize(slider)

        return if (!slider.range.isEmpty()) (size / slider.range.size).toFloat() else 0f
    }

    protected fun barPosition(slider: Slider): Double = round((slider.value - slider.range.start) * scaleFactor(slider))

    protected fun barSize(slider: Slider): Double = if (slider.orientation === Horizontal) slider.height else slider.width
}
