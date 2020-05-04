package com.nectar.doodle.themes.basic.tabbedpanel

import com.nectar.doodle.controls.panels.TabbedPanel
import com.nectar.doodle.controls.theme.TabbedPanelBehavior
import com.nectar.doodle.core.Box
import com.nectar.doodle.core.Container
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.PositionableContainer
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
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.geometry.Path
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.path
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.utils.addOrAppend
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Created by Nicholas Eddy on 3/14/19.
 */

open class BasicTab<T>(private val textMetrics  : TextMetrics,
                       private val panel        : TabbedPanel<T>,
                               var index        : Int,
                       private val name         : String,
                       private val radius       : Double,
                       private val panelColor   : Color,
                       private val selectedColor: Color,
                       private val move         : (panel: TabbedPanel<T>, tab: Int, by: Double) -> Unit,
                       private val cancelMove   : (panel: TabbedPanel<T>, tab: Int) -> Unit): View() {
    private var mouseOver = false
        set(new) {
            field = new
            if (!selected) backgroundColor = panelColor.lighter()
        }

    private var mouseDown       = false
    private var initialPosition = null as Point?

    private val selected get() = panel.selection == index

    private var path: Path

    private val selectionChanged = { _: TabbedPanel<T>, old: Int?, new: Int? ->
        backgroundColor = when {
            old == index -> if (mouseOver) panelColor.lighter() else null
            new == index -> selectedColor
            else         -> null
        }
    }

    init {
        mouseChanged += object: MouseListener {
            override fun mousePressed (event: MouseEvent) { mouseDown = true; panel.selection = index; initialPosition = toLocal(event.location, event.target) }
            override fun mouseEntered (event: MouseEvent) { mouseOver = true  }
            override fun mouseExited  (event: MouseEvent) { mouseOver = false }
            override fun mouseReleased(event: MouseEvent) {
                if (mouseDown) {
                    mouseDown = false
                    cursor    = null

                    cancelMove(panel, index)
                }

                initialPosition = null
            }
        }

        mouseMotionChanged += object: MouseMotionListener {
            override fun mouseDragged(event: MouseEvent) {
                initialPosition?.let {
                    val delta = (toLocal(event.location, event.target) - it).x

                    move(panel, index, delta)

                    cursor = Cursor.Grabbing

                    event.consume()
                }
            }
        }

        boundsChanged += { _,_,_ -> path = updatePath() }

        styleChanged += { rerender() }

        panel.selectionChanged += selectionChanged

        if (selected) {
            backgroundColor = selectedColor
        }

        path = updatePath()
    }

    override fun removedFromDisplay() {
        super.removedFromDisplay()

        panel.selectionChanged -= selectionChanged
    }

    override fun render(canvas: Canvas) {
        backgroundColor?.let {
            canvas.path(path, ColorBrush(it))
        } ?: if (index > panel.selection || index < panel.selection - 1) {
            canvas.line(Point(width - radius, radius), Point(width - radius, height - radius), Pen(gray))
        }

        canvas.clip(Rectangle(Point(2 * radius, 0.0), Size(width - 4 * radius, height))) {
            val name       = name
            val nameHeight = textMetrics.height(name)

            text(name, at = Point(2 * radius, (height - nameHeight) / 2), brush = ColorBrush(black))
        }
    }

    override fun contains(point: Point) = super.contains(point) && when (val localPoint = toLocal(point, parent)) {
        in Rectangle(radius, 0.0, width - 2 * radius, height) -> when (localPoint) {
            in Rectangle(            radius, 0.0, radius, radius)     -> sqrt((Point(        2 * radius, radius) - localPoint).run { x * x + y * y }) <= radius
            in Rectangle(width - 2 * radius, 0.0, radius, radius)     -> sqrt((Point(width - 2 * radius, radius) - localPoint).run { x * x + y * y }) <= radius
            else                                                      -> true
        }
        in Rectangle(           0.0, height - radius, radius, radius) -> sqrt((Point(  0.0, height - radius) - localPoint).run { x * x + y * y }) >  radius
        in Rectangle(width - radius, height - radius, radius, radius) -> sqrt((Point(width, height - radius) - localPoint).run { x * x + y * y }) >  radius
        else                                                          -> false
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

interface TabProducer<T> {
    val spacing  : Double
    val tabHeight: Double

    operator fun invoke(panel: TabbedPanel<T>, item: T, index: Int): View
}

open class BasicTabProducer<T>(protected val textMetrics  : TextMetrics,
                               protected val namer        : (T) -> String,
                               override  val tabHeight    : Double = 40.0,
                               protected val tabRadius    : Double = 10.0,
                               protected val selectedColor: Color  = white,
                               protected val tabColor     : Color  = Color(0xdee1e6u)): TabProducer<T> {
    override val spacing = -2 * tabRadius

    override fun invoke(panel: TabbedPanel<T>, item: T, index: Int) = BasicTab(textMetrics, panel, index, namer(item), tabRadius, tabColor, selectedColor, move, cancelMove).apply { size = Size(100.0, tabHeight) } // FIXME: use dynamic width

    protected open val move = { _: TabbedPanel<T>, _: Int,_: Double -> }

    protected open val cancelMove = { _: TabbedPanel<T>, _: Int -> }
}

private class TabLayout(private val minWidth: Double = 40.0, private val defaultWidth: Double = 200.0, private val spacing: Double = 0.0): Layout {
    override fun layout(container: PositionableContainer) {
        val maxLineWidth = max(0.0, container.width - container.insets.left - container.insets.right - (container.children.size - 1) * spacing)

        var x     = container.insets.left
        val width = max(minWidth, min(defaultWidth, maxLineWidth / container.children.size))

        container.children.filter { it.visible }.forEach { child ->
            child.width    = width
            child.position = Point(x, container.insets.top)

            x += width + spacing
        }
    }
}

open class BasicTabbedPanelBehavior<T>(private val tabProducer    : TabProducer<T>,
                                       private val backgroundColor: Color = Color(0xdee1e6u),
                                       private val displayer      : (T) -> View): TabbedPanelBehavior<T> {

    override fun install(panel: TabbedPanel<T>, container: Container) {
        container.apply {
            children.add(TabContainer(panel, tabProducer))

            panel.forEach { children.add(displayer(it).apply {
                visible = it == panel.selectedItem
            }) }

            layout = object: Layout {
                override fun layout(container: PositionableContainer) {
                    container.children.forEachIndexed { index, view ->
                        view.bounds = when (index) {
                            0    -> Rectangle(container.width, tabProducer.tabHeight + 10)
                            else -> Rectangle(size = container.size).inset(Insets(top = tabProducer.tabHeight + 10))
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
            container.children.getOrNull(it + 1)?.visible = false

            dirty += it
        }

        newIndex.let {
            container.children.getOrNull(it + 1)?.visible = true

            dirty += it
        }

        (container.children[0] as TabContainer<*>).apply {
            oldIndex?.let{ children.getOrNull(it) }?.let { it.zOrder = 0 }

            newIndex.let { children.getOrNull(it) }?.zOrder = 1

            dirty.forEach {
                children.getOrNull(it)?.rerender()
            }
        }
    }

    override fun tabsChanged(panel: TabbedPanel<T>, container: Container, removed: Map<Int, T>, added: Map<Int, T>, moved: Map<Int, Pair<Int, T>>) {
        (container.first() as? TabContainer<T>)?.apply {
            children.batch {
                removed.keys.forEach { removeAt(it) }

                added.forEach { (index, item) ->
                    addOrAppend(index, tabProducer(panel, item, index))
                }

                moved.forEach { (oldIndex, new) ->
                    addOrAppend(new.first, removeAt(oldIndex))
                }

                this.forEachIndexed { index, item ->
                    (item as? BasicTab<*>)?.index = index // FIXME
                }
            }
        }

        removed.keys.forEach { container.children.removeAt(it + 1) }

        added.forEach { (index, item) ->
            container.children.addOrAppend(index + 1, displayer(item))
        }

        moved.forEach { (oldIndex, new) ->
            container.children.addOrAppend(new.first + 1, container.children.removeAt(oldIndex + 1))
        }
    }

    override fun render(panel: TabbedPanel<T>, canvas: Canvas) {
        canvas.rect(panel.bounds.atOrigin, ColorBrush(backgroundColor))
    }

    private class TabContainer<T>(panel: TabbedPanel<T>, tabProducer: TabProducer<T>): Box() {
        init {
            children.addAll(panel.mapIndexed { index, item ->
                tabProducer(panel, item, index)
            })

            insets = Insets(top = 10.0) // TODO: Make this configurable
            layout = TabLayout(spacing = tabProducer.spacing)
        }
    }
}