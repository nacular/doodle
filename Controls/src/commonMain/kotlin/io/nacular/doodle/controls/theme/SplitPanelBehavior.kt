package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.panels.SplitPanel
import io.nacular.doodle.core.View
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.system.Cursor.Companion.ColResize
import io.nacular.doodle.system.Cursor.Companion.RowResize
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.Orientation.Vertical
import kotlin.math.max
import kotlin.math.min

interface SplitPanelBehavior: Behavior<SplitPanel> {
    fun divider(panel: SplitPanel): View?

    val dividerVisible: Boolean
}

abstract class CommonSplitPanelBehavior(private val divider: View, override val dividerVisible: Boolean = false): SplitPanelBehavior, PointerListener, PointerMotionListener {

    private var splitPanel      = null as SplitPanel?
    private var orientation     = Vertical
    private var pressedLocation = 0.0

    init {
        divider.pointerChanged       += this
        divider.pointerMotionChanged += this
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

    override fun pressed(event: PointerEvent) {
        pressedLocation = when (orientation) {
            Vertical   -> event.location.x
            Horizontal -> event.location.y
        }
    }

    override fun dragged(event: PointerEvent) {
        splitPanel?.let { splitPanel ->

            var minPosition = 0.0
            var maxPosition = 0.0
            var position    = 0.0

            when (orientation) {
                Vertical   -> {
                    minPosition = splitPanel.insets.left
                    position    = divider.x + event.location.x - pressedLocation
                    maxPosition = splitPanel.run { width - divider.width - insets.run { right } }
                }

                Horizontal -> {

                    minPosition = splitPanel.insets.top
                    position    = divider.y + event.location.y - pressedLocation
                    maxPosition = splitPanel.run { height - divider.width - insets.run { bottom } }
                }
            }

            val newPosition = min(maxPosition, max(minPosition, position))

            splitPanel.ratio = ((newPosition - minPosition) / (maxPosition - minPosition)).toFloat()

            event.consume()
        }
    }
}