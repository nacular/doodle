package com.nectar.doodle.core

import com.nectar.doodle.accessibility.AccessibilityManager
import com.nectar.doodle.drawing.RenderManager
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.PointerEvent
import com.nectar.doodle.geometry.Rectangle

/**
 * Created by Nicholas Eddy on 3/15/20.
 */
class DoodleTest {
    fun View.handleDisplayRectEvent(old: Rectangle, new: Rectangle) = this.handleDisplayRectEvent_(old, new)

    fun View.handleKeyEvent(event: KeyEvent) = this.handleKeyEvent_(event)

    fun View.filterPointerEvent(event: PointerEvent) = this.filterPointerEvent_(event)

    fun View.handlePointerEvent(event: PointerEvent) = this.handlePointerEvent_(event)

    fun View.filterPointerMotionEvent(event: PointerEvent) = this.filterPointerMotionEvent_(event)

    fun View.handlePointerMotionEvent(event: PointerEvent) = this.handlePointerMotionEvent_(event)

    fun View.focusGained(@Suppress("UNUSED_PARAMETER") previous: View?) = this.focusGained(previous)

    fun View.focusLost(@Suppress("UNUSED_PARAMETER") new: View?) = this.focusLost(new)

    fun View.addedToDisplay(display: Display, renderManager: RenderManager, accessibilityManager: AccessibilityManager) = this.addedToDisplay(display, renderManager, accessibilityManager)

    fun View.removedFromDisplay() = this.removedFromDisplay_()
}