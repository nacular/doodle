package io.nacular.doodle.controls.theme.range

import io.nacular.doodle.controls.range.RangeValueSlider
import io.nacular.doodle.controls.range.ValueSlider
import io.nacular.doodle.controls.range.size
import io.nacular.doodle.core.ContentDirection
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyText.Companion.ArrowDown
import io.nacular.doodle.event.KeyText.Companion.ArrowLeft
import io.nacular.doodle.event.KeyText.Companion.ArrowRight
import io.nacular.doodle.event.KeyText.Companion.ArrowUp
import kotlin.math.pow

private val MIN_OFFSET = 10.0.pow(-10)

internal fun handleKeyPress(slider: ValueSlider<*>, event: KeyEvent) {
    val positiveIncrement =   slider.snapSize?.takeIf { it > 0 }?.let { snapSize ->
        (snapSize - slider.value.toDouble() % snapSize).takeIf { it > MIN_OFFSET } ?: snapSize
    } ?: (slider.range.size.toDouble() / 100)
    val negativeIncrement = -(slider.snapSize?.takeIf { it > 0 }?.let { snapSize ->
        (slider.value.toDouble() % snapSize).takeIf { it > MIN_OFFSET } ?: snapSize
    } ?: (slider.range.size.toDouble() / 100))

    val (incrementKey, decrementKey) = when (slider.contentDirection) {
        ContentDirection.LeftRight -> ArrowRight to ArrowLeft
        else                       -> ArrowLeft to ArrowRight
    }

    when (event.key) {
        ArrowUp,   incrementKey -> slider.adjust(by = positiveIncrement)
        ArrowDown, decrementKey -> slider.adjust(by = negativeIncrement)
    }
}

internal fun handleKeyPress(slider: RangeValueSlider<*>, event: KeyEvent) {
    val positiveIncrement =   slider.snapSize?.takeIf { it > 0 }?.let { snapSize ->
        (snapSize - slider.value.start.toDouble() % snapSize).takeIf { it > MIN_OFFSET } ?: snapSize
    } ?: (slider.range.size.toDouble() / 100)
    val negativeIncrement = -(slider.snapSize?.takeIf { it > 0 }?.let { snapSize ->
        (slider.value.start.toDouble() % snapSize).takeIf { it > MIN_OFFSET } ?: snapSize
    } ?: (slider.range.size.toDouble() / 100))

    val (incrementKey, decrementKey) = when (slider.contentDirection) {
        ContentDirection.LeftRight -> ArrowRight to ArrowLeft
        else                       -> ArrowLeft  to ArrowRight
    }

    when (event.key) {
        ArrowUp,   incrementKey -> slider.adjust(startBy = positiveIncrement, endBy = positiveIncrement)
        ArrowDown, decrementKey -> slider.adjust(startBy = negativeIncrement, endBy = negativeIncrement)
    }
}