package io.nacular.doodle.utils

import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.utils.RelativePositionMonitor.AbsoluteMonitor
import io.nacular.doodle.utils.RelativePositionMonitor.PairMonitor

/**
 * Created by Nicholas Eddy on 12/26/18.
 */
public typealias AbsoluteBoundsListener = (view: View,                   old: Rectangle, new: Rectangle) -> Unit
public typealias RelativeBoundsListener = (view: View, relativeTo: View, old: Rectangle, new: Rectangle) -> Unit

public interface RelativePositionMonitor {
    public interface AbsoluteMonitor {
        public operator fun plusAssign (listener: AbsoluteBoundsListener)
        public operator fun minusAssign(listener: AbsoluteBoundsListener)
    }

    public interface PairMonitor {
        public operator fun plusAssign (listener: RelativeBoundsListener)
        public operator fun minusAssign(listener: RelativeBoundsListener)
    }

    public operator fun get(view: View                  ): AbsoluteMonitor
    public operator fun get(view: View, relativeTo: View): PairMonitor
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

public class RelativePositionMonitorImpl: RelativePositionMonitor {
    private val absoluteMapping   = mutableMapOf<View, MutableList<AbsoluteBoundsListener>>()
    private val relativeMapping   = mutableMapOf<View, MutableMap<View, MutableSet<RelativeBoundsListener>>>()
    private val absolutePositions = mutableMapOf<View, Point>()
    private val listenerCounts    = mutableMapOf<View, Int>  ()
    private val boundsChanged_    = ::boundsChanged

    public fun add(view: View, listener: AbsoluteBoundsListener) {
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

    public fun remove(view: View, listener: AbsoluteBoundsListener) {
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

    public fun add(view: View, relativeTo: View, listener: RelativeBoundsListener) {
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

    public fun remove(view: View, relativeTo: View, listener: RelativeBoundsListener) {
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