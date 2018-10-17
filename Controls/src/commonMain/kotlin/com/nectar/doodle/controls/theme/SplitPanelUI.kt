package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.panels.SplitPanel
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.Orientation
import kotlin.math.max
import kotlin.math.min

interface SplitPanelUI: Renderer<SplitPanel> {
    fun divider(panel: SplitPanel): Gizmo?
    fun resizer(panel: SplitPanel): Gizmo?
}

abstract class AbstractSplitPanelUI(
        private val spacing: Double = 7.0,
        private val divider: Gizmo,
        private val resizer: Gizmo? = null): SplitPanelUI, MouseListener, MouseMotionListener {

    private var splitPanel      = null as SplitPanel?
    private var orientation     = Orientation.Vertical
    private var pressedLocation = 0.0

    init {
        divider.mouseChanged       += this
        divider.mouseMotionChanged += this

        resizer?.visible            = false
    }

    override fun divider(panel: SplitPanel): Gizmo? = divider
    override fun resizer(panel: SplitPanel): Gizmo? = resizer

    override fun install(gizmo: SplitPanel) {
        splitPanel  = gizmo.also { it.panelSpacing = spacing }
        orientation = gizmo.orientation

        when (gizmo.orientation) {
            Orientation.Vertical   -> divider.cursor = Cursor.ColResize
            Orientation.Horizontal -> divider.cursor = Cursor.RowResize
        }
    }

    override fun uninstall(gizmo: SplitPanel) {
        splitPanel = null
    }

    override fun mousePressed(event: MouseEvent) {
        resizer?.bounds  = divider.bounds
        resizer?.visible = true

        pressedLocation = when (orientation) {
            Orientation.Vertical   -> event.location.x
            Orientation.Horizontal -> event.location.y
        }
    }

    override fun mouseReleased(event: MouseEvent) {
        splitPanel?.let { splitPanel ->
            resizer?.let { resizer ->
                var minPosition = 0.0
                var position = 0.0
                var maxPosition = 0.0

                when (orientation) {
                    Orientation.Vertical   -> {
                        minPosition = splitPanel.insets.left
                        position = resizer.x
                        maxPosition = splitPanel.width - splitPanel.panelSpacing - splitPanel.insets.run { right }
                    }

                    Orientation.Horizontal -> {
                        minPosition = splitPanel.insets.bottom
                        position = resizer.y
                        maxPosition = splitPanel.height - splitPanel.panelSpacing - splitPanel.insets.run { bottom }
                    }
                }

                splitPanel.ratio = ((position - minPosition) / (maxPosition - minPosition)).toFloat()

                resizer.visible = false
            }
        }
    }

    override fun mouseDragged(mouseEvent: MouseEvent) {
        splitPanel?.let { splitPanel ->

            var minPosition = 0.0
            var maxPosition = 0.0
            var position    = 0.0

            when (orientation) {
                Orientation.Vertical   -> {
                    minPosition = splitPanel.insets.left
                    position    = divider.x + mouseEvent.location.x - pressedLocation
                    maxPosition = splitPanel.run { width - panelSpacing - insets.run { right } }
                }

                Orientation.Horizontal -> {

                    minPosition = splitPanel.insets.top
                    position    = divider.y + mouseEvent.location.y - pressedLocation
                    maxPosition = splitPanel.run { height - panelSpacing - insets.run { bottom } }
                }
            }

            val newPosition = min(maxPosition, max(minPosition, position))

            if (resizer != null) {
                when (orientation) {
                    Orientation.Vertical   -> resizer.x = newPosition
                    Orientation.Horizontal -> resizer.y = newPosition
                }
            } else {
                splitPanel.ratio = ((newPosition - minPosition) / (maxPosition - minPosition)).toFloat()
            }
        }
    }
}