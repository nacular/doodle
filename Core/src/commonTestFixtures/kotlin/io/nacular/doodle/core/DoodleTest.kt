package io.nacular.doodle.core

import io.nacular.doodle.accessibility.AccessibilityManager
import io.nacular.doodle.drawing.RenderManager
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 3/15/20.
 */
fun View.handleDisplayRectEvent(old: Rectangle, new: Rectangle) = this.handleDisplayRectEvent_(old, new)

fun View.handleKeyEvent(event: KeyEvent) = this.handleKeyEvent_(event)

fun View.filterPointerEvent(event: PointerEvent) = this.filterPointerEvent_(event)

fun View.handlePointerEvent(event: PointerEvent) = this.handlePointerEvent_(event)

fun View.filterPointerMotionEvent(event: PointerEvent) = this.filterPointerMotionEvent_(event)

fun View.handlePointerMotionEvent(event: PointerEvent) = this.handlePointerMotionEvent_(event)

fun View.focusGained(previous: View?) = this.focusGained_(previous)

fun View.focusLost(new: View?) = this.focusLost_(new)

fun View.addedToDisplay(display: InternalDisplay, renderManager: RenderManager, accessibilityManager: AccessibilityManager) = this.addedToDisplay_(display, renderManager, accessibilityManager)

fun View.removedFromDisplay() = this.removedFromDisplay_()

fun View.forceX(x: Double) {
    suggestX(x)
    syncBounds()
}

fun View.forceY(x: Double) {
    suggestX(x)
    syncBounds()
}

fun View.forcePosition(x: Double, y: Double) {
    suggestPosition(x, y)
    syncBounds()
}

fun View.forceWidth(width: Double) {
    suggestWidth(width)
    syncBounds()
}

fun View.forceHeight(height: Double) {
    suggestHeight(height)
    syncBounds()
}

fun View.forceSize(size: Size) {
    suggestSize(size)
    syncBounds()
}

fun View.forceBounds(bounds: Rectangle) {
    suggestBounds(bounds)
    syncBounds()
}