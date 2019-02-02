package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.Slider
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.Orientation.Horizontal
import com.nectar.doodle.utils.Orientation.Vertical
import com.nectar.doodle.utils.size
import kotlin.math.round

/**
 * Created by Nicholas Eddy on 2/13/18.
 */

abstract class SliderUI: Renderer<Slider>, MouseListener, MouseMotionListener {

    private   var lastStart         = -1.0
    protected var lastMousePosition = -1.0
        private set


    private val changed: (Slider, Double, Double) -> Unit = { it,_,_ -> it.rerender() }

    override fun install(view: Slider) {
        view.changed            += changed
        view.mouseChanged       += this
        view.mouseMotionChanged += this
    }

    override fun uninstall(view: Slider) {
        view.changed            -= changed
        view.mouseChanged       -= this
        view.mouseMotionChanged -= this
    }

    override fun mousePressed(event: MouseEvent) {
        val slider      = event.source as Slider
        val scaleFactor = scaleFactor(slider).let { if ( it!= 0f) 1 / it else 0f }

        val offset = when (slider.orientation) {
            Horizontal -> event.location.x
            Vertical   -> event.location.y
        }

        val barSize     = barSize(slider)
        val barPosition = barPosition(slider)

        if (offset < barPosition || offset > barPosition + barSize) {
            slider.value += round(scaleFactor * (offset - (barPosition + barSize / 2)))
        }

        lastMousePosition = offset
        lastStart         = slider.value
    }

    override fun mouseReleased(event: MouseEvent) {
        lastStart         = -1.0
        lastMousePosition = -1.0
    }

    override fun mouseDragged(mouseEvent: MouseEvent) {
        val slider = mouseEvent.source as Slider

        val delta = when (slider.orientation) {
            Horizontal -> mouseEvent.location.x - lastMousePosition
            Vertical   -> mouseEvent.location.y - lastMousePosition
        }

        val deltaValue = delta / scaleFactor(slider)

        slider.value = lastStart + deltaValue
    }

    private fun scaleFactor(slider: Slider): Float {
        val size = (if (slider.orientation === Horizontal) slider.width else slider.height) - barSize(slider)

        return if (!slider.range.isEmpty()) (size / slider.range.size).toFloat() else 0f
    }

    protected fun barPosition(slider: Slider) = round((slider.value - slider.range.start) * scaleFactor(slider))

    protected fun barSize(slider: Slider) = if (slider.orientation === Horizontal) slider.height else slider.width
}
