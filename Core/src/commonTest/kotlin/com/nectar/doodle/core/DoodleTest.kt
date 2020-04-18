package com.nectar.doodle.core

import com.nectar.doodle.accessibility.AccessibilityManager
import com.nectar.doodle.drawing.RenderManager
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseScrollEvent
import com.nectar.doodle.geometry.Rectangle

/**
 * Created by Nicholas Eddy on 3/15/20.
 */
class DoodleTest {
    fun View.handleDisplayRectEvent(old: Rectangle, new: Rectangle) = this.handleDisplayRectEvent_(old, new)

    fun View.handleKeyEvent(event: KeyEvent) = this.handleKeyEvent_(event)

    fun View.filterMouseEvent(event: MouseEvent) = this.filterMouseEvent_(event)

    fun View.handleMouseEvent(event: MouseEvent) = this.handleMouseEvent_(event)

    fun View.filterMouseMotionEvent(event: MouseEvent) = this.filterMouseMotionEvent_(event)

    fun View.handleMouseMotionEvent(event: MouseEvent) = this.handleMouseMotionEvent_(event)

    fun View.handleMouseScrollEvent(event: MouseScrollEvent) = this.handleMouseScrollEvent_(event)

    fun View.focusGained(@Suppress("UNUSED_PARAMETER") previous: View?) = this.focusGained(previous)

    fun View.focusLost(@Suppress("UNUSED_PARAMETER") new: View?) = this.focusLost(new)

    fun View.addedToDisplay(renderManager: RenderManager, accessibilityManager: AccessibilityManager) = this.addedToDisplay(renderManager, accessibilityManager)

    fun View.removedFromDisplay() = this.removedFromDisplay_()
}