package io.nacular.doodle.examples

import io.nacular.doodle.core.View
import io.nacular.doodle.event.Event
import io.nacular.doodle.event.Interaction
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.examples.GestureRecognizer.Type.*
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool

class GestureEvent(
        target : View,
        val initial: List<Interaction>,
        val current: List<Interaction>,
        val center : Point,
        val scale  : Double
): Event<View>(source = target)

interface GestureListener<T> {
    fun started(event: T) {}
    fun changed(event: T) {}
    fun ended  (event: T) {}
}

/**
 * Simple gesture recognizer that tracks 2 pointers and reports on their relative movement.
 */
class GestureRecognizer(private val view: View): PointerListener, PointerMotionListener {
    private var first            : Interaction? = null
    private var second           : Interaction? = null
    private var originalDistance = 0.0
    private var originalCenter   = Point.Origin

    private enum class Type { Start, Change, End }

    val changed: Pool<GestureListener<GestureEvent>> = SetPool()

    init {
        view.pointerFilter       += this
        view.pointerMotionFilter += this
    }

    fun discard() {
        view.pointerFilter       -= this
        view.pointerMotionFilter -= this
    }

    override fun pressed(event: PointerEvent) {
        event.preventOsHandling()
    }

    override fun released(event: PointerEvent) {
        val oldFirst                = first
        val oldSecond               = second
        var newFirst : Interaction? = null
        var newSecond: Interaction? = null
        val interactions            = event.targetInteractions

        interactions.firstOrNull { it.pointer == first?.pointer  }?.let { first  = null; newFirst  = it }
        interactions.firstOrNull { it.pointer == second?.pointer }?.let { second = null; newSecond = it }

        if (newFirst != null && newSecond != null) {
            val scale = (newFirst!!.inParent(view) distanceFrom newSecond!!.inParent(view)) / originalDistance

            notifyChanged(event, GestureEvent(view, listOf(oldFirst!!, oldSecond!!), listOf(newFirst!!, newSecond!!), originalCenter, scale), End)
        }
    }

    override fun dragged(event: PointerEvent) {
        when {
            second == null         -> captureInitialPoints(event)
            originalDistance > 0.0 -> {
                val interactions = event.targetInteractions

                interactions.firstOrNull { it.pointer == first?.pointer }?.let { newFirst ->
                    interactions.firstOrNull { it.pointer == second?.pointer }?.let { newSecond ->
                        val scale = (newFirst.inParent(view) distanceFrom newSecond.inParent(view)) / originalDistance

                        notifyChanged(event, GestureEvent(view, listOf(first!!, second!!), listOf(newFirst, newSecond), originalCenter, scale), Change)

                        event.preventOsHandling()
                    }
                }
            }
        }
    }

    private fun captureInitialPoints(event: PointerEvent) {
        val interactions = event.targetInteractions

        if (first  == null && interactions.isNotEmpty()) { first = interactions.first() }
        if (second == null && interactions.size > 1) {
            second           = interactions.first { it.pointer != first?.pointer }
            originalDistance = first!!.inParent(view) distanceFrom second!!.inParent(view)
            originalCenter   = (first!!.location + second!!.location) / 2

            notifyChanged(event, GestureEvent(view, listOf(first!!, second!!), listOf(first!!, second!!), originalCenter, 0.0), Start)
        }
    }

    private fun notifyChanged(event: PointerEvent, gestureEvent: GestureEvent, type: Type) {
        (changed as SetPool).forEach {
            when (type) {
                Start  -> it.started(gestureEvent)
                Change -> it.changed(gestureEvent)
                End    -> it.ended  (gestureEvent)
            }

            if (gestureEvent.consumed) {
                event.consume()
                return@forEach
            }
        }
    }
}