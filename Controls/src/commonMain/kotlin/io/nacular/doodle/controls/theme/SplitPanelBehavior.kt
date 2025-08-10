package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.panels.SplitPanel
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.system.Cursor.Companion.ColResize
import io.nacular.doodle.system.Cursor.Companion.RowResize
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.Orientation.Vertical
import kotlin.math.max
import kotlin.math.min

public interface SplitPanelBehavior: Behavior<SplitPanel> {
    public fun divider(panel: SplitPanel): View?

    public val dividerVisible: Boolean
}

public abstract class CommonSplitPanelBehavior(private val divider: View = object: View() {}, private val size: Double, override val dividerVisible: Boolean = false): SplitPanelBehavior, PointerListener, PointerMotionListener {

    private var splitPanel      = null as SplitPanel?
    private var pressedLocation = 0.0

    private val orientationChanged: (source: SplitPanel) -> Unit = {
        when (it.orientation) {
            Vertical   -> {
                divider.cursor = ColResize
                divider.suggestWidth(size)
            }
            Horizontal -> {
                divider.cursor = RowResize
                divider.suggestHeight(size)
            }
        }
    }

    init {
        divider.pointerChanged       += this
        divider.pointerMotionChanged += this
    }

    override fun divider(panel: SplitPanel): View = divider

    override fun install(view: SplitPanel) {
        splitPanel  = view

        orientationChanged(view)

        view.orientationChanged += orientationChanged
    }

    override fun uninstall(view: SplitPanel) {
        splitPanel?.orientationChanged?.minusAssign(orientationChanged)

        splitPanel = null
    }

    override fun pressed(event: PointerEvent) {
        splitPanel?.orientation?.let {
            pressedLocation = when (it) {
                Vertical   -> event.location.x
                Horizontal -> event.location.y
            }
        }

        event.consume()
    }

    override fun dragged(event: PointerEvent) {
        splitPanel?.let { splitPanel ->
            var minPosition: Double
            var position   : Double
            var maxPosition: Double

            when (splitPanel.orientation) {
                Vertical   -> {
                    minPosition = splitPanel.insets.left
                    position    = divider.x + event.location.x - pressedLocation
                    maxPosition = splitPanel.run { width - divider.width - insets.run { right } }
                }

                Horizontal -> {
                    minPosition = splitPanel.insets.top
                    position    = divider.y + event.location.y - pressedLocation
                    maxPosition = splitPanel.run { height - divider.height - insets.run { bottom } }
                }
            }

            val newPosition = min(maxPosition, max(minPosition, position))

            splitPanel.ratio = ((newPosition - minPosition) / (maxPosition - minPosition)).toFloat()

            event.consume()
        }
    }
}