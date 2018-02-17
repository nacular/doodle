package com.nectar.doodle.controls.panels

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.theme.Renderer
import kotlin.math.max
import kotlin.math.min


interface ScrollPanelRenderer: Renderer<ScrollPanel> {
    var onScroll: ((Point) -> Unit)?

    fun scrollTo(point: Point)
}

class ScrollPanel: Gizmo() {

    var content = null as Gizmo?
        set(new) {
            if (new == this) {
                // FIXME: throw error
                return
            }

            field?.let {
                children -= it
            }

            if (field != new) {
                field = new

                field?.let {
                    children += it
                }
            }
        }

    var scroll = Origin
        private set

    var renderer: ScrollPanelRenderer? = null
        set(new) {
            field?.onScroll = null

            field = new?.also { it.onScroll = {
                scrollTo(it, force = true)
            } }
        }

    init {
        layout = ViewLayout(this)
    }

    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    override fun contains(point: Point) = renderer?.contains(this, point) ?: super.contains(point)

    fun scrollTo(point: Point) {
        renderer?.scrollTo(point)
    }

    private fun scrollTo(point: Point, force: Boolean = false) {
        content?.let {
            if (scroll != point) {
                val newScroll = when (force) {
                    true  -> point
                    false -> Point(max(0.0, min(point.x, it.width - width)), max(0.0, min(point.y, it.height - height)))
                }

                scroll      =  newScroll
                it.position = -newScroll
            }
        }
    }

    fun scrollBy(point: Point) {
        scrollTo(scroll + point)
    }

    fun scrollToVisible(point: Point) {
        moveToVisible(point)
    }

    fun scrollToVisible(rect: Rectangle) {
        moveToVisible(rect)
    }

    protected fun moveToVisible(point: Point) {
        var target = scroll.x + width

        val x = when {
            point.x > target -> point.x - target
            else             -> point.x
        }

        target = scroll.y + height

        val y = when {
            point.y > target -> point.y - target
            else             -> point.y
        }

        scrollBy(Point(x, y))
    }

    protected fun moveToVisible(rect: Rectangle) {
        moveToVisible(rect.position)
        moveToVisible(Point(rect.x + rect.width, rect.y + rect.height))
    }

    private inner class ViewLayout(private val panel: ScrollPanel): Layout() {
        override fun layout(positionable: Positionable) {
            positionable.children.forEach  {
                var width  = it.width
                var height = it.height

                it.idealSize?.let {
                    width  = it.width
                    height = it.height
                }

                it.bounds = Rectangle(-scroll.x, -scroll.y, width, height)
            }
        }

        override val usesChildIdealSize = true
    }
}