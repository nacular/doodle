package com.nectar.doodle.utils

import com.nectar.doodle.core.View
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.utils.RelativePositionMonitor.AbsoluteMonitor
import com.nectar.doodle.utils.RelativePositionMonitor.PairMonitor

/**
 * Created by Nicholas Eddy on 12/26/18.
 */
typealias AbsoluteBoundsListener = (view: View,                   old: Rectangle, new: Rectangle) -> Unit
typealias RelativeBoundsListener = (view: View, relativeTo: View, old: Rectangle, new: Rectangle) -> Unit

interface RelativePositionMonitor {
    interface AbsoluteMonitor {
        operator fun plusAssign (listener: AbsoluteBoundsListener)
        operator fun minusAssign(listener: AbsoluteBoundsListener)
    }

    interface PairMonitor {
        operator fun plusAssign (listener: RelativeBoundsListener)
        operator fun minusAssign(listener: RelativeBoundsListener)
    }

    operator fun get(view: View                  ): AbsoluteMonitor
    operator fun get(view: View, relativeTo: View): PairMonitor
}

private class AbsoluteMonitorImpl(
        private val monitor   : RelativePositionMonitorImpl,
        private val view      : View): AbsoluteMonitor {
    override fun plusAssign(listener: AbsoluteBoundsListener) {
        monitor.add(view, listener)
    }

    override fun minusAssign(listener: AbsoluteBoundsListener) {
        monitor.remove(view, listener)
    }
}

private class PairMonitorImpl(
        private val monitor   : RelativePositionMonitorImpl,
        private val view      : View,
        private val relativeTo: View): PairMonitor {
    override fun plusAssign(listener: RelativeBoundsListener) {
        monitor.add(view, relativeTo, listener)
    }

    override fun minusAssign(listener: RelativeBoundsListener) {
        monitor.remove(view, relativeTo, listener)
    }
}

class RelativePositionMonitorImpl: RelativePositionMonitor {
    private val absoluteMapping   = mutableMapOf<View, MutableList<AbsoluteBoundsListener>>()
    private val relativeMapping   = mutableMapOf<View, MutableMap<View, MutableSet<RelativeBoundsListener>>>()
    private val absolutePositions = mutableMapOf<View, Point>()
    private val listenerCounts    = mutableMapOf<View, Int>  ()
    private val boundsChanged_    = ::boundsChanged

    fun add(view: View, listener: AbsoluteBoundsListener) {
        absolutePositions.getOrPut(view) {
            view.toAbsolute(Origin)
        }

        absoluteMapping.getOrPut(view) {
            mutableListOf()
        } += listener

        var p = view as View?

        while (p != null) {
            p.boundsChanged += boundsChanged_
            listenerCounts[p] = listenerCounts.getOrPut(p) { 0 } + 1

            p = p.parent
        }
    }

    fun remove(view: View, listener: AbsoluteBoundsListener) {
        absoluteMapping[view]?.let {
            it -= listener

            if (it.isEmpty()) {
                absolutePositions -= view

                var p = view as View?

                while (p != null) {
                    p.boundsChanged -= boundsChanged_
                    listenerCounts[p] = listenerCounts.getOrPut(p) { 0 } - 1

                    if (listenerCounts[p]!! <= 0) {
                        listenerCounts -= p
                    }

                    p = p.parent
                }
            }
        }
    }

    fun add(view: View, relativeTo: View, listener: RelativeBoundsListener) {
        listOf(view, relativeTo).forEach {
            absolutePositions.getOrPut(it) {
                it.toAbsolute(Origin)
            }
        }

        relativeMapping.getOrPut(view) {
            mutableMapOf(view to mutableSetOf())
        }.getOrPut(relativeTo) {
            mutableSetOf()
        } += listener

        relativeMapping.getOrPut(relativeTo) {
            mutableMapOf(relativeTo to mutableSetOf())
        }.getOrPut(view) {
            mutableSetOf()
        } += { a,b,old,new ->
            if (old.position != new.position) {
                listener(b, a, Rectangle(-old.position, b.size), Rectangle(-new.position, b.size))
            }
        }

        val firstAncestors  = mutableListOf(view      )
        val secondAncestors = mutableListOf(relativeTo)

        var p = view.parent

        while (p != null) {
            firstAncestors += p
            p = p.parent
        }

        p = relativeTo.parent

        while (p != null) {
            secondAncestors += p
            p = p.parent
        }

        firstAncestors.reverse ()
        secondAncestors.reverse()

        while(firstAncestors.isNotEmpty() && secondAncestors.isNotEmpty()) {
            if (firstAncestors[0] == secondAncestors[0]) {
                firstAncestors.drop (0)
                secondAncestors.drop(0)
            } else {
                break
            }
        }

        firstAncestors.forEach  { it.boundsChanged += boundsChanged_ }
        secondAncestors.forEach { it.boundsChanged += boundsChanged_ }
    }

    fun remove(view: View, relativeTo: View, listener: RelativeBoundsListener) {
        // FIXME: Implement
    }

    override operator fun get(view: View): AbsoluteMonitor = AbsoluteMonitorImpl(this, view)

    override operator fun get(view: View, relativeTo: View): PairMonitor = PairMonitorImpl(this, view, relativeTo)

    private fun boundsChanged(source: View, old: Rectangle, new: Rectangle) {
        updateListeners(source)
    }

    private fun updateListeners(view: View) {
        val oldPosition = absolutePositions[view] ?: Origin
        absolutePositions[view] = view.parent?.let { absolutePositions[it]?.plus(view.position) } ?: view.toAbsolute(Origin)

        absoluteMapping[view]?.let {
            it.forEach {
                it(view, Rectangle(oldPosition, view.size), Rectangle(absolutePositions[view]!!, view.size))
            }
        }

        relativeMapping[view]?.let {
            it.forEach { (relativeTo, listeners) ->
                listeners.forEach { it(view, relativeTo, Rectangle(oldPosition, view.size), Rectangle(absolutePositions[view]!! - absolutePositions[relativeTo]!!, view.size)) }
            }
        }

        view.children_.forEach { updateListeners(it) }
    }
}