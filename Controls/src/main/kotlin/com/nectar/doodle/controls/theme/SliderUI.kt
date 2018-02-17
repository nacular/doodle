package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.Slider
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.ranges.size
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.Orientation.Horizontal
import com.nectar.doodle.utils.Orientation.Vertical
import kotlin.math.round

/**
 * Created by Nicholas Eddy on 2/13/18.
 */

abstract class SliderUI protected constructor(private val slider: Slider): Renderer<Slider>, MouseListener, MouseMotionListener {

    private   var lastStart         = -1.0
    protected var lastMousePosition = -1.0
        private set

    init {
        slider.onChanged          += this::onChanged
        slider.mouseChanged       += this
        slider.mouseMotionChanged += this
    }

    override fun uninstall(gizmo: Slider) {
        gizmo.onChanged          -= this::onChanged
        gizmo.mouseChanged       -= this
        gizmo.mouseMotionChanged -= this
    }

    override fun mousePressed(event: MouseEvent) {
        val scaleFactor_ = if (scaleFactor != 0f) 1 / scaleFactor else 0f

        val offset = when (slider.orientation) {
            Horizontal -> event.location.x
            Vertical   -> event.location.y
        }

        if (offset < barPosition || offset > barPosition + barSize) {
            slider.value += round(scaleFactor_ * (offset - (barPosition + barSize / 2)))
        }

        lastMousePosition = offset
        lastStart         = slider.value
    }

    override fun mouseReleased(event: MouseEvent) {
        lastStart         = -1.0
        lastMousePosition = -1.0
    }

    override fun mouseDragged(mouseEvent: MouseEvent) {
        val delta = when (slider.orientation) {
            Horizontal -> mouseEvent.location.x - lastMousePosition
            Vertical   -> mouseEvent.location.y - lastMousePosition
        }

        val deltaValue = delta / scaleFactor

        slider.value = lastStart + deltaValue
    }

    private fun onChanged(slider: Slider) = slider.rerender()

    private val scaleFactor: Float get() {
        val size = (if (slider.orientation === Horizontal) slider.width else slider.height) - barSize

        return if (!slider.range.isEmpty()) (size / slider.range.size).toFloat() else 0f
    }

    protected val barPosition get() = round((slider.value - slider.range.start) * scaleFactor)

    protected val barSize get() = if (slider.orientation === Horizontal) slider.height else slider.width
}
