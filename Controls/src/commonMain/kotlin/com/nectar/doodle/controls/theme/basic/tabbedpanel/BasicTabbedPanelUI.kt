package com.nectar.doodle.controls.theme.basic.tabbedpanel

import com.nectar.doodle.controls.panels.TabbedPanel
import com.nectar.doodle.controls.theme.TabbedPanelUI
import com.nectar.doodle.core.Box
import com.nectar.doodle.core.Container
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.black
import com.nectar.doodle.drawing.Color.Companion.gray
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.geometry.Path
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.path
import com.nectar.doodle.layout.HorizontalFlowLayout
import com.nectar.doodle.layout.Insets
import kotlin.math.sqrt

/**
 * Created by Nicholas Eddy on 3/14/19.
 */

class BasicTabbedPanelUI<T>(private val textMetrics    : TextMetrics,
                            private val tabHeight      : Double = 40.0,
                            private val tabRadius      : Double = 10.0,
                            private val backgroundColor: Color  = Color(0xdee1e6u),
                            private val namer          : (T) -> String,
                            private val displayer      : (T) -> View): TabbedPanelUI<T> {

    override fun install(panel: TabbedPanel<T>, container: Container) {
        container.apply {
            children.add(TabContainer(textMetrics, panel, namer, tabHeight, tabRadius))

            panel.forEach { children.add(displayer(it).apply {
                visible = it == panel.selectedItem
            }) }

            layout = object: Layout() {
                override fun layout(positionable: Positionable) {
                    positionable.children.forEachIndexed { index, view ->
                        view.bounds = when (index) {
                            0    -> Rectangle(positionable.width, tabHeight + 10)
                            else -> Rectangle(size = positionable.size).inset(Insets(top = tabHeight + 10))
                        }
                    }
                }
            }
        }
    }

    override fun uninstall(panel: TabbedPanel<T>, container: Container) {
        container.apply {
            children.clear()
            layout = null
        }
    }

    override fun selectionChanged(panel: TabbedPanel<T>, container: Container, new: T, newIndex: Int, old: T?, oldIndex: Int?) {
        val dirty = mutableSetOf<Int>()

        oldIndex?.let {
            container.children[it + 1].visible = false

            dirty += listOf(it, it - 1)
        }

        newIndex.let {
            container.children[it + 1].visible = true

            dirty += listOf(it, it - 1)
        }

        (container.children[0] as TabContainer<*>).apply {
//            oldIndex?.let{ children[it] as BasicTabbedPanelUI<*>.Tab }?.let { setZIndex(it, it.index) }

//            setZIndex(newIndex.let { children[it] as BasicTabbedPanelUI<*>.Tab }, 0)

            dirty.forEach {
                children.getOrNull(it)?.rerender()
            }
        }
    }

    override fun render(panel: TabbedPanel<T>, canvas: Canvas) {
        canvas.rect(panel.bounds.atOrigin, ColorBrush(backgroundColor))
    }

    private class Tab<T>(private val textMetrics: TextMetrics,
                         private val panel      : TabbedPanel<T>,
                                 val index      : Int,
                         private val name       : String,
                         private val radius     : Double): View() {
        private var mouseOver = false
            set(new) {
                field = new
                rerender()
            }

        private var path      : Path
        private var mouseDown = false

        init {
            mouseChanged += object: MouseListener {
                override fun mousePressed (event: MouseEvent) { mouseDown = true  }
                override fun mouseEntered (event: MouseEvent) { mouseOver = true  }
                override fun mouseExited  (event: MouseEvent) { mouseOver = false }
                override fun mouseReleased(event: MouseEvent) { if (mouseDown) { panel.selection = index }; mouseDown = false }
            }

            boundsChanged += { _,_,_ -> path = updatePath() }

            styleChanged += { rerender() }

            path = updatePath()
        }

        override fun render(canvas: Canvas) {
            val backgroundColor = when {
                panel.selection == index -> white
                mouseOver                -> Color(0xdee1e6u).lighter()
                else                     -> null
            }

            if (backgroundColor != null) {
                canvas.path(path, ColorBrush(backgroundColor))
            } else if (index > panel.selection || index < panel.selection - 1) {
                canvas.line(Point(width - radius, radius), Point(width - radius, height - radius), Pen(gray))
            }

            canvas.clip(Rectangle(Point(2 * radius, 0.0), Size(width - 4 * radius, height))) {
                val name       = name
                val nameHeight = textMetrics.height(name)

                text(name, at = Point(2 * radius, (height - nameHeight) / 2), brush = ColorBrush(black))
            }
        }

        override fun contains(point: Point): Boolean {
            return super.contains(point) && when (val localPoint = point - position) {
                in Rectangle(radius, 0.0, width - 2 * radius, height) -> when (localPoint) {
                    in Rectangle(            radius, 0.0, radius, radius)     -> sqrt((Point(        2 * radius,          radius) - localPoint).run { x * x + y * y }) <= radius
                    in Rectangle(width - 2 * radius, 0.0, radius, radius)     -> sqrt((Point(width - 2 * radius,          radius) - localPoint).run { x * x + y * y }) <= radius
                    else                                                      -> true
                }
                in Rectangle(           0.0, height - radius, radius, radius) -> sqrt((Point(           0.0,     height - radius) - localPoint).run { x * x + y * y }) >  radius
                in Rectangle(width - radius, height - radius, radius, radius) -> sqrt((Point(width - radius,     height - radius) - localPoint).run { x * x + y * y }) >  radius
                else                                                          -> false
            }
        }

        private fun updatePath() = path(Point(0.0, height)).
                quadraticTo(Point(radius,             height - radius), Point(radius,         height)).
                lineTo     (Point(radius,             radius                                        )).
                quadraticTo(Point(2 * radius,         0.0            ), Point(radius,         0.0   )).
                lineTo     (Point(width - 2 * radius, 0.0                                           )).
                quadraticTo(Point(width - radius,     radius         ), Point(width - radius, 0.0   )).
                lineTo     (Point(width - radius,     height - radius                               )).
                quadraticTo(Point(width,              height         ), Point(width - radius, height)).
                close      ()

    }

    private class TabContainer<T>(textMetrics: TextMetrics, panel: TabbedPanel<T>, namer: (T) -> String, tabHeight: Double, tabRadius: Double): Box() {
        init {
            children.addAll(panel.mapIndexed { index, item ->
                Tab(textMetrics, panel, index, namer(item), tabRadius).apply { size = Size(100.0, tabHeight) } // FIXME: use dynamic width
            })

            insets = Insets(top = 10.0)
            layout = HorizontalFlowLayout(horizontalSpacing = -2 * tabRadius) // FIXME: Use custom layout
        }
    }
}