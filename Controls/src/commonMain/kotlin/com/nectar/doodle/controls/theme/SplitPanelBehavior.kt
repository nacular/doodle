package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.panels.SplitPanel
import com.nectar.doodle.core.View
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.system.Cursor.Companion.ColResize
import com.nectar.doodle.system.Cursor.Companion.RowResize
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.utils.Orientation.Horizontal
import com.nectar.doodle.utils.Orientation.Vertical
import kotlin.math.max
import kotlin.math.min

interface SplitPanelBehavior: Behavior<SplitPanel> {
    fun divider(panel: SplitPanel): View?

    val dividerVisible: Boolean
}

abstract class AbstractSplitPanelBehavior(private val divider: View, override val dividerVisible: Boolean = false): SplitPanelBehavior, MouseListener, MouseMotionListener {

    private var splitPanel      = null as SplitPanel?
    private var orientation     = Vertical
    private var pressedLocation = 0.0

    init {
        divider.mouseChanged       += this
        divider.mouseMotionChanged += this
    }

    override fun divider(panel: SplitPanel): View? = divider

    override fun install(view: SplitPanel) {
        splitPanel  = view
        orientation = view.orientation

        when (view.orientation) {
            Vertical   -> divider.cursor = ColResize
            Horizontal -> divider.cursor = RowResize
        }
    }

    override fun uninstall(view: SplitPanel) {
        splitPanel = null
    }

    override fun mousePressed(event: MouseEvent) {
        pressedLocation = when (orientation) {
            Vertical   -> event.location.x
            Horizontal -> event.location.y
        }
    }

    override fun mouseDragged(mouseEvent: MouseEvent) {
        splitPanel?.let { splitPanel ->

            var minPosition = 0.0
            var maxPosition = 0.0
            var position    = 0.0

            when (orientation) {
                Vertical   -> {
                    minPosition = splitPanel.insets.left
                    position    = divider.x + mouseEvent.location.x - pressedLocation
                    maxPosition = splitPanel.run { width - divider.width - insets.run { right } }
                }

                Horizontal -> {

                    minPosition = splitPanel.insets.top
                    position    = divider.y + mouseEvent.location.y - pressedLocation
                    maxPosition = splitPanel.run { height - divider.width - insets.run { bottom } }
                }
            }

            val newPosition = min(maxPosition, max(minPosition, position))

            splitPanel.ratio = ((newPosition - minPosition) / (maxPosition - minPosition)).toFloat()
        }
    }
}