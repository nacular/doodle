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
import io.nacular.doodle.system.SystemPointerEvent.Type.Down
import io.nacular.doodle.system.SystemPointerEvent.Type.Drag
import io.nacular.doodle.utils.Direction.East
import io.nacular.doodle.utils.Direction.North
import io.nacular.doodle.utils.Direction.South
import io.nacular.doodle.utils.Direction.West
import kotlin.math.max

public class Resizer(private val view: View): PointerListener, PointerMotionListener {

    init {
        view.pointerChanged       += this
        view.pointerMotionChanged += this
    }

    public var movable    : Boolean        = true
    public var directions : Set<Direction> = setOf(North, East, South, West)
    public var hotspotSize: Double         = 5.0

    private var dragMode             = mutableSetOf<Direction>()
    private var oldCursor            = view.cursor
    private var initialSize          = Empty
    private var initialPosition      = Origin
    private var ignorePropertyChange = false
    private var activePointer        = null as Pointer?

    override fun released(event: PointerEvent) {
        if (activePointerChanged(event)) {
            captureInitialState(event)
        }
    }

    override fun pressed(event: PointerEvent) {
        if (activePointer == null || activePointer !in event.targetInteractions) {
            captureInitialState(event)
            event.preventOsHandling()
        }
    }

    override fun entered(event: PointerEvent) {
        (activeInteraction(event) ?: event.changedInteractions.firstOrNull())?.let {
            updateCursor(it)
        }
    }

    override fun exited(event: PointerEvent) {
        if (dragMode.isEmpty() && activePointerChanged(event)) {
            view.cursor = oldCursor
        }
    }

    override fun moved(event: PointerEvent) {
        (activeInteraction(event) ?: event.changedInteractions.firstOrNull())?.let {
            updateCursor(it)
        }
    }

    override fun dragged(event: PointerEvent) {
        event.changedInteractions.find { it.pointer == activePointer }?.let { activeInteration ->
            val delta = view.toLocal(activeInteration.location, event.target) - initialPosition

            if (dragMode.isEmpty() && movable) {
                view.position += delta

                event.consume()
            } else if (dragMode.isNotEmpty()) {
                val bounds = view.bounds

                var x = bounds.x
                var y = bounds.y
                var width = bounds.width
                var height = bounds.height

                val minWidth = view.minimumSize.width
                val minHeight = view.minimumSize.height

                var consume = false

                when {
                    West in dragMode && West in directions -> {
                        width = max(minWidth, view.width - delta.x)
                        x += bounds.width - width
                        consume = true
                    }
                    East in dragMode && East in directions -> {
                        width = max(minWidth, initialSize.width + delta.x)
                        consume = true
                    }
                }

                when {
                    North in dragMode && North in directions -> {
                        height = max(minHeight, view.height - delta.y)
                        y += bounds.height - height
                        consume = true
                    }
                    South in dragMode && South in directions -> {
                        height = max(minHeight, initialSize.height + delta.y)
                        consume = true
                    }
                }

                view.bounds = Rectangle(x, y, width, height)

                if (consume) {
                    event.consume()
                }
            }
        }
    }

    private fun activePointerChanged(event: PointerEvent): Boolean = activeInteraction(event) != null

    private fun activeInteraction(event: PointerEvent): Interaction? = event.changedInteractions.find { it.pointer == activePointer }

    private fun captureInitialState(event: PointerEvent) {
        activePointer = null

        dragMode.clear()

        val interaction = event.targetInteractions.firstOrNull { it.state == Down || it.state == Drag }

        if (interaction != null) {
            activePointer   = interaction.pointer
            initialPosition = interaction.location
            initialSize     = view.size

            when {
                initialPosition.y <= hotspotSize               -> dragMode.plusAssign(North)
                initialPosition.y >= view.height - hotspotSize -> dragMode.plusAssign(South)
            }

            when {
                initialPosition.x >= view.width - hotspotSize -> dragMode.plusAssign(East)
                initialPosition.x <= hotspotSize              -> dragMode.plusAssign(West)
            }

            updateCursor(interaction)
        } else {
            view.cursor = oldCursor
        }
    }

    private fun updateCursor(interaction: Interaction) {
        if (dragMode.isNotEmpty()) {
            return
        }

        val location = interaction.location
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