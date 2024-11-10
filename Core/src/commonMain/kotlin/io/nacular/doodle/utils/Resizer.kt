package io.nacular.doodle.utils

import io.nacular.doodle.core.View
import io.nacular.doodle.event.Interaction
import io.nacular.doodle.event.Pointer
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.system.Cursor.Companion.EResize
import io.nacular.doodle.system.Cursor.Companion.Grab
import io.nacular.doodle.system.Cursor.Companion.Grabbing
import io.nacular.doodle.system.Cursor.Companion.NResize
import io.nacular.doodle.system.Cursor.Companion.NeResize
import io.nacular.doodle.system.Cursor.Companion.NwResize
import io.nacular.doodle.system.Cursor.Companion.SResize
import io.nacular.doodle.system.Cursor.Companion.SeResize
import io.nacular.doodle.system.Cursor.Companion.SwResize
import io.nacular.doodle.system.Cursor.Companion.WResize
import io.nacular.doodle.system.SystemPointerEvent.Button.Button2
import io.nacular.doodle.system.SystemPointerEvent.Type.Down
import io.nacular.doodle.system.SystemPointerEvent.Type.Drag
import io.nacular.doodle.system.SystemPointerEvent.Type.Up
import io.nacular.doodle.utils.Direction.East
import io.nacular.doodle.utils.Direction.North
import io.nacular.doodle.utils.Direction.South
import io.nacular.doodle.utils.Direction.West
import io.nacular.doodle.utils.Resizer.Phase
import io.nacular.doodle.utils.Resizer.Phase.EventBubbling
import io.nacular.doodle.utils.Resizer.Phase.EventSinking

/**
 * Utility for resizing/moving a View. It supports compass direction resizing and dragging.
 *
 * @param view         to be resized/moved
 * @param manageCursor indicates whether the cursor should be updated by the Resizer
 * @param movable      indicates whether the Resizer will handle drag events to move [view]
 * @param during       [Phase] during which the Resizer is triggered. [EventSinking] allows the Resizer to get events **before** the View and its children
 */
public class Resizer(
    private val view        : View,
    private val manageCursor: Boolean = true,
    public  var movable     : Boolean = true,
                during      : Phase   = EventBubbling,
): PointerListener, PointerMotionListener {

    /**
     * Event phase when a Resizer will trigger
     */
    public enum class Phase { EventBubbling, EventSinking }

    init {
        when (during) {
            EventBubbling -> {
                view.pointerChanged       += this
                view.pointerMotionChanged += this
            }
            EventSinking -> {
                view.pointerFilter        += this
                view.pointerMotionFilter  += this
            }
        }
    }

    /**
     * Set of directions the Resizer will resize its View in.
     */
    public var directions: Set<Direction> = setOf(North, East, South, West)

    /**
     * Size of the resize zone when adjusting the View's width/height.
     */
    public var hotspotSize: Double = 5.0

    /**
     * Determines whether pointer release events are consumed when the Resizer has done a drag.
     */
    public var suppressReleaseEvent: Boolean = true

    private var dragMode             = mutableSetOf<Direction>()
    private var oldCursor            = view.cursor
    private var initialSize          = Empty
    private var consumedDrag         = false
    private var activePointer        = null as Pointer?
    private var initialPosition      = Origin
    private var ignorePropertyChange = false

    override fun released(event: PointerEvent) {
        val interaction = activeInteraction(event)

        if (activePointerChanged(event) && interaction?.state == Up) {
            captureInitialState(event)
            if (consumedDrag) {
                event.consume()
                consumedDrag = false
            }
        }
    }

    override fun pressed(event: PointerEvent) {
        if (activePointer == null || event.targetInteractions.find { it.pointer == activePointer } == null) {
            captureInitialState(event)
        }
    }

    override fun entered(event: PointerEvent) {
        (activeInteraction(event) ?: event.changedInteractions.firstOrNull())?.let {
            updateCursor(it, event)
        }
    }

    override fun exited(event: PointerEvent) {
        if (dragMode.isEmpty() && activePointerChanged(event)) {
            view.cursor = oldCursor
        }
    }

    override fun moved(event: PointerEvent) {
        (activeInteraction(event) ?: event.changedInteractions.firstOrNull())?.let {
            updateCursor(it, event)
        }
    }

    override fun dragged(event: PointerEvent) {
        event.changedInteractions.find { it.pointer == activePointer }?.let { activeInteraction ->
            val deltaInParent = view.toParent(view.toLocal(activeInteraction.location, event.target)) - view.toParent(initialPosition)

            if (dragMode.isEmpty() && movable) {
                view.suggestPosition(view.position + deltaInParent)

                event.consume()
                consumedDrag = true
            } else if (dragMode.isNotEmpty()) {
                val bounds    = view.bounds
                var x         = bounds.x
                var y         = bounds.y
                var width     = bounds.width
                var height    = bounds.height
                var consume   = false
                val delta     = view.toLocal(activeInteraction.location, event.target) - initialPosition

                when {
                    West in dragMode && West in directions -> {
                        width    = view.width - delta.x
                        x       += bounds.width - width
                        consume  = true
                    }
                    East in dragMode && East in directions -> {
                        width   = initialSize.width + delta.x
                        consume = true
                    }
                }

                when {
                    North in dragMode && North in directions -> {
                        height   = view.height - delta.y
                        y       += bounds.height - height
                        consume  = true
                    }
                    South in dragMode && South in directions -> {
                        height  = initialSize.height + delta.y
                        consume = true
                    }
                }

                view.suggestBounds(Rectangle(x, y, width, height))

                if (consume) {
                    event.consume()
                    consumedDrag = true
                }
            }
        }
    }

    private fun activePointerChanged(event: PointerEvent): Boolean = activeInteraction(event) != null

    private fun activeInteraction(event: PointerEvent): Interaction? = event.changedInteractions.find { it.pointer == activePointer }

    private fun captureInitialState(event: PointerEvent) {
        activePointer = null

        dragMode.clear()

        val interaction = if (Button2 in event.buttons) null else  event.targetInteractions.firstOrNull { it.state == Down || it.state == Drag }

        if (interaction != null) {
            activePointer   = interaction.pointer
            initialPosition = view.toLocal(interaction.location, event.target)
            initialSize     = view.size

            when {
                initialPosition.y <= hotspotSize               -> dragMode += North
                initialPosition.y >= view.height - hotspotSize -> dragMode += South
            }

            when {
                initialPosition.x >= view.width - hotspotSize -> dragMode += East
                initialPosition.x <= hotspotSize              -> dragMode += West
            }

            if (dragMode.isNotEmpty() || movable) {
                event.preventOsHandling()
            }

            updateCursor(interaction, event)
        } else {
            when (val upInteraction = event.targetInteractions.firstOrNull { it.state == Up }) {
                null -> view.cursor = oldCursor
                else -> updateCursor(upInteraction, event)
            }
        }
    }

    private fun updateCursor(interaction: Interaction, event: PointerEvent) {
        if (!manageCursor || dragMode.isNotEmpty()) {
            return
        }

        val location = view.toLocal(interaction.location, event.target)
        val x        = location.x
        val y        = location.y
        val mask     = mutableSetOf<Direction>()
        var innerX   = false
        var innerY   = false

        if (x <= hotspotSize) {
            if (West in directions) {
                mask += West
            }
        } else if (x >= view.width - hotspotSize) {
            if (East in directions) {
                mask += East
            }
        } else {
            innerX = true
        }
        if (y <= hotspotSize) {
            if (North in directions) {
                mask += North
            }
        } else if (y >= view.height - hotspotSize) {
            if (South in directions) {
                mask += South
            }
        } else {
            innerY = true
        }

        ignorePropertyChange = true

        view.cursor = when {
            North in mask    -> when {
                East in mask -> NeResize
                West in mask -> NwResize
                else         -> NResize
            }
            South in mask    -> when {
                East in mask -> SeResize
                West in mask -> SwResize
                else         -> SResize
            }
            East in mask                -> EResize
            West in mask                -> WResize
            movable && innerX && innerY -> when (interaction.state) {
                Down -> Grabbing
                else -> Grab
            }
            else                        -> oldCursor
        }

        ignorePropertyChange = false
    }
}