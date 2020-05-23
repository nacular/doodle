package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.Slider
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyEvent.Companion.VK_DOWN
import com.nectar.doodle.event.KeyEvent.Companion.VK_LEFT
import com.nectar.doodle.event.KeyEvent.Companion.VK_RIGHT
import com.nectar.doodle.event.KeyEvent.Companion.VK_UP
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.PointerEvent
import com.nectar.doodle.event.PointerListener
import com.nectar.doodle.event.PointerMotionListener
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.utils.Orientation.Horizontal
import com.nectar.doodle.utils.Orientation.Vertical
import com.nectar.doodle.utils.size
import kotlin.math.round

/**
 * Created by Nicholas Eddy on 2/13/18.
 */

abstract class SliderBehavior: Behavior<Slider>, PointerListener, PointerMotionListener, KeyListener {

    private   var lastStart           = -1.0
    protected var lastPointerPosition = -1.0
        private set


    private val changed: (Slider, Double, Double) -> Unit = { it,_,_ -> it.rerender() }

    override fun install(view: Slider) {
        view.changed            += changed
        view.keyChanged         += this
        view.pointerChanged       += this
        view.pointerMotionChanged += this
    }

    override fun uninstall(view: Slider) {
        view.changed            -= changed
        view.keyChanged         -= this
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
            slider.value += round(scaleFactor * (offset - (barPosition + barSize / 2)))
        }

        lastPointerPosition = offset
        lastStart         = slider.value

        event.consume()
    }

    override fun released(event: PointerEvent) {
        lastStart         = -1.0
        lastPointerPosition = -1.0
    }

    override fun keyPressed(event: KeyEvent) {
        val slider    = event.source as Slider
        val increment = slider.range.size / 100

        when (event.code) {
            VK_LEFT,  VK_DOWN -> slider.value -= increment
            VK_RIGHT, VK_UP   -> slider.value += increment
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

    protected fun barPosition(slider: Slider) = round((slider.value - slider.range.start) * scaleFactor(slider))

    protected fun barSize(slider: Slider) = if (slider.orientation === Horizontal) slider.height else slider.width
}
