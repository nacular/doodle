package io.nacular.doodle.theme.basic.tabbedpanel

import io.nacular.doodle.accessibility.TabListRole
import io.nacular.doodle.accessibility.TabRole
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.fixedSpeedLinear
import io.nacular.doodle.animation.fixedTimeLinear
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.invoke
import io.nacular.doodle.controls.panels.TabbedPanel
import io.nacular.doodle.controls.panels.TabbedPanelBehavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Gray
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.path
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.layout.constraints.withSizeInsets
import io.nacular.doodle.system.Cursor.Companion.Grabbing
import io.nacular.doodle.theme.basic.ColorMapper
import io.nacular.doodle.utils.Cancelable
import io.nacular.doodle.utils.addOrAppend
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.Time.Companion.seconds
import io.nacular.measured.units.div
import io.nacular.measured.units.times
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Created by Nicholas Eddy on 3/14/19.
 */

public abstract class Tab<T>(protected val role: TabRole = TabRole()): View(accessibilityRole = role) {
    public abstract var index: Int
}

public open class BasicTab<T>(
        private  val panel              : TabbedPanel<T>,
        override var index              : Int,
                     visualizer         : ItemVisualizer<T, Any>,
        private  val radius             : Double,
        private  val tabColor           : Color,
        private  val move               : (panel: TabbedPanel<T>, tab: Int, by: Double) -> Unit,
        private  val cancelMove         : (panel: TabbedPanel<T>, tab: Int) -> Unit,
        private  var selectedColorMapper: ColorMapper,
        private  var hoverColorMapper   : ColorMapper
): Tab<T>() {

    private var pointerOver = false
        set(new) {
            field = new
            backgroundColor = when {
                selected  -> selectedColorMapper(tabColor)
                new       -> hoverColorMapper   (tabColor)
                else      -> tabColor
            }
        }

    private var pointerDown     = false
    private var initialPosition = null as Point?

    private val selected get() = panel.selection == index

    private var path: Path

    private val selectionChanged = { _: TabbedPanel<T>, old: Int?, new: Int? ->
        backgroundColor = when {
            old == index -> if (pointerOver) hoverColorMapper(tabColor) else null
            new == index -> selectedColorMapper(tabColor)
            else         -> null
        }

        role.selected = new == index
    }

    public var cellAlignment: (ConstraintDslContext.(Bounds) -> Unit) = { it.centerY eq parent.centerY }

    private fun constrainLayout(view: View) = constrain(view) { content ->
        withSizeInsets(width = 4 * radius) {
            cellAlignment(content.withOffset(left = 2 * radius))
        }

//        val width = content.width
//
//        content.width eq min(width, parent.width - 4 * radius)
    }

    init {
        children += visualizer(panel[index]!!).also {
            it.x = 2 * radius
        }

        layout = constrainLayout(children[0])

        pointerChanged += object: PointerListener {
            override fun pressed (event: PointerEvent) {
                pointerDown     = true
                panel.selection = index
                initialPosition = toLocal(event.location, event.target)
                event.consume()
            }

            override fun entered (event: PointerEvent) { if (!pointerOver) pointerOver = true  }
            override fun exited  (event: PointerEvent) {
                pointerOver = when (val p = parent) {
                    null -> event.target.toAbsolute(event.location) in this@BasicTab
                    else -> p.toLocal(event.location, event.target) in this@BasicTab
                }
            }
            override fun released(event: PointerEvent) {
                if (pointerDown) {
                    pointerDown = false
                    cursor    = null

                    cancelMove(panel, index)
                }

                initialPosition = null
            }
        }

        pointerMotionChanged += object: PointerMotionListener {
            override fun dragged(event: PointerEvent) {
                initialPosition?.let {
                    val delta = (toLocal(event.location, event.target) - it).x

                    move(panel, index, delta)

                    cursor = Grabbing

                    event.consume()
                }
            }
        }

        boundsChanged += { _,_,_ -> path = updatePath() }

        styleChanged += { rerender() }

        if (selected) {
            backgroundColor = selectedColorMapper(tabColor)
            role.selected   = true
        }

        path = updatePath()
    }

    override fun addedToDisplay() {
        super.addedToDisplay()

        panel.selectionChanged += selectionChanged
    }

    override fun removedFromDisplay() {
        super.removedFromDisplay()

        panel.selectionChanged -= selectionChanged
    }

    override fun render(canvas: Canvas) {
        val selection = panel.selection

        backgroundColor?.let {
            canvas.path(path, ColorPaint(it))
        } ?: when {
            selection != null && (index > selection || index < selection - 1) -> {
                canvas.line(Point(width - radius, radius), Point(width - radius, height - radius), Stroke(Gray))
            }
            else                                                              -> {}
        }
    }

    override fun contains(point: Point): Boolean = super.contains(point) && when (val localPoint = toLocal(point, parent)) {
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
            close      ().also {
        childrenClipPath = PolyClipPath(Rectangle(Point(2 * radius, 0.0), Size(max(0.0, width - 4 * radius), height)))
    }
}

public interface TabProducer<T> {
    public val spacing  : Double
    public val tabHeight: Double

    public operator fun invoke(panel: TabbedPanel<T>, item: T, index: Int): Tab<T>
}

public open class BasicTabProducer<T>(override  val tabHeight          : Double = 40.0,
                                      protected val tabRadius          : Double = 10.0,
                                      protected val tabColor           : Color  = Color(0xdee1e6u),
                                      protected val selectedColorMapper: ColorMapper = { White           },
                                      protected val hoverColorMapper   : ColorMapper = { it.darker(0.1f) }
): TabProducer<T> {
    override val spacing: Double = -2 * tabRadius

    override fun invoke(panel: TabbedPanel<T>, item: T, index: Int): BasicTab<T> = BasicTab(
            panel,
            index,
            panel.tabVisualizer,
            tabRadius,
            tabColor,
            move,
            cancelMove,
            selectedColorMapper,
            hoverColorMapper
    ).apply { size = Size(100.0, tabHeight) } // FIXME: use dynamic width

    protected open val move: (TabbedPanel<T>, Int, Double) -> Unit = { _,_,_ -> }

    protected open val cancelMove: (TabbedPanel<T>, Int) -> Unit = { _,_ -> }
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

public abstract class TabContainer<T>(protected val role: TabListRole = TabListRole()): View(accessibilityRole = role) {
    /**
     * Called whenever the TabbedPanel's selection changes. This is an explicit API to ensure that
     * behaviors receive the notification before listeners to [TabbedPanel.selectionChanged].
     *
     * @param panel with change
     * @param newIndex of the selected item
     * @param oldIndex of previously selected item
     */
    public abstract fun selectionChanged(panel: TabbedPanel<T>, newIndex: Int?, oldIndex: Int?)

    /**
     * Called whenever the items within the TabbedPanel change.
     *
     * @param panel with change
     * @param removed items
     * @param added items
     * @param moved items (changed index)
     */
    public abstract fun itemsChanged(panel: TabbedPanel<T>, removed: Map<Int, T>, added: Map<Int, T>, moved: Map<Int, Pair<Int, T>>)

    /**
     * Allows the container to learn which View will be associated with each tab.
     */
    public abstract fun registerTab(panel: TabbedPanel<T>, index: Int, tabPanel: View)
}

public open class SimpleTabContainer<T>(panel: TabbedPanel<T>, private val tabProducer: TabProducer<T>): TabContainer<T>() {
    init {
        children.addAll(panel.mapIndexed { index, item ->
            tabProducer(panel, item, index)
        })

        insets = Insets(top = 10.0) // TODO: Make this configurable
        layout = TabLayout(spacing = tabProducer.spacing)
    }

    override fun selectionChanged(panel: TabbedPanel<T>, newIndex: Int?, oldIndex: Int?) {
        oldIndex?.let { children.getOrNull(it) }?.let { it.zOrder = 0; it.rerender() }
        newIndex?.let { children.getOrNull(it) }?.let { it.zOrder = 1; it.rerender() }
    }

    override fun itemsChanged(panel: TabbedPanel<T>, removed: Map<Int, T>, added: Map<Int, T>, moved: Map<Int, Pair<Int, T>>) {
        children.batch {
            removed.keys.forEach { removeAt(it) }

            added.forEach { (index, item) ->
                addOrAppend(index, tabProducer(panel, item, index))
            }

            moved.forEach { (new, old) ->
                addOrAppend(new, removeAt(old.first))
            }

            filterIsInstance<Tab<T>>().forEachIndexed { index, item ->
                item.index = index
            }
        }
    }

    override fun registerTab(panel: TabbedPanel<T>, index: Int, tabPanel: View) {
        children.getOrNull(index)?.let { tab ->
            role[tab] = tabPanel
        }
    }
}

public class AnimatingTabContainer<T>(
    private val animate         : Animator,
    private val panel           : TabbedPanel<T>,
    private val tabProducer     : TabProducer<T>): SimpleTabContainer<T>(panel, tabProducer) {
    private val moveAnimations  = mutableMapOf<View, Cancelable>()
    private val colorAnimations = mutableMapOf<View, Cancelable>()
    private val hoverColors     = mutableMapOf<View, Color?>()

    init {
        childrenChanged += { _,_,added,_ ->
            added.values.filterIsInstance<Tab<T>>().forEach { tagTab(panel, it) }
        }

        children.filterIsInstance<Tab<T>>().forEach { tagTab(panel, it) }
    }

    private fun tagTab(panel: TabbedPanel<T>, tab: Tab<T>) = tab.apply {
        var pointerDown     = false
        var initialPosition = null as Point?
        var pointerOver     = false

        pointerChanged += object: PointerListener {
            override fun pressed(event: PointerEvent) {
                pointerDown     = true
                initialPosition = toLocal(event.location, event.target)
                cleanupAnimation(tab)
            }

            override fun entered(event: PointerEvent) {
                if (panel.selection != tab.index && !pointerOver) {
                    pointerOver      = true
                    hoverColors[tab] = tab.backgroundColor
                    doAnimation(panel, tab, 0f, 1f)
                }
            }

            override fun exited(event: PointerEvent) {
                if (panel.selection != tab.index &&
                    when (val p = parent) {
                            null -> event.target.toAbsolute(event.location) !in tab
                            else -> p.toLocal(event.location, event.target) !in tab
                    }) {
                    pointerOver         = false
                    tab.backgroundColor = hoverColors[tab]
                    doAnimation(panel, tab, 1f, 0f)
                }
            }

            override fun released(event: PointerEvent) {
                if (pointerDown) {
                    pointerDown = false
                    cancelMove(panel, index)
                }

                initialPosition = null
            }
        }

        pointerMotionChanged += object: PointerMotionListener {
            override fun dragged(event: PointerEvent) {
                initialPosition?.let {
                    val delta = (toLocal(event.location, event.target) - it).x

                    move(panel, index, delta)

                    cursor = Grabbing

                    event.consume()
                }
            }
        }
    }

    private fun cleanupAnimation(tab: View) {
        colorAnimations[tab]?.let {
            it.cancel()
            colorAnimations.remove(tab)
        }
    }

    // FIXME: Handle right-left when tab is left-right
    private fun move(panel: TabbedPanel<T>, movingIndex: Int, delta: Double) {
        children.getOrNull(movingIndex)?.apply {
            zOrder         = 1
            val translateX = transform.translateX
            val delta      = min(max(delta, 0 - (x + translateX)), panel.width - width - (x + translateX))

            transform *= Identity.translate(delta)

            val adjustWidth = width + tabProducer.spacing

            children.forEachIndexed { index, tab ->
                if (tab != this) {
                    val targetBounds = tab.bounds

                    val value = when (targetBounds.x + tab.transform.translateX + targetBounds.width / 2) {
                        in x + translateX + delta            .. x + translateX                    ->  adjustWidth
                        in x + translateX                    .. x + translateX + delta            -> -adjustWidth
                        in bounds.right + translateX         .. bounds.right + translateX + delta -> -adjustWidth
                        in bounds.right + translateX + delta .. bounds.right + translateX         ->  adjustWidth
                        else                                                                      ->  null
                    }

                    value?.let {
                        val oldTransform = tab.transform
                        val minViewX     = if (index > movingIndex) tab.x - adjustWidth else tab.x
                        val maxViewX     = minViewX + adjustWidth
                        val offset       = tab.x + tab.transform.translateX
                        val translate    = min(max(value, minViewX - offset), maxViewX - offset)

//                        tab.animation = behavior?.moveColumn(this@Table) {
                        moveAnimations[tab]?.cancel()

                        moveAnimations[tab] = animate { (0f to 1f using fixedSpeedLinear(10 / seconds)) {
                            tab.transform = oldTransform.translate(translate * it) //1.0)
                        } }
                    }
                }
            }
        }
    }

    private fun cancelMove(panel: TabbedPanel<T>, movingIndex: Int) {
        children.filterIsInstance<Tab<*>>().getOrNull(movingIndex)?.apply {
            zOrder           = 0
            val myOffset     = x + transform.translateX
            var myNewIndex   = if (myOffset >= children.last().x) children.size - 1 else movingIndex
            var targetBounds = bounds

            run loop@ {
                when {
                    transform.translateX > 0 -> {
                        for (index in children.lastIndex downTo 0) {
                            val tab = children[index]
                            val targetMiddle = tab.x + tab.transform.translateX + tab.width / 2

                            if (myOffset + width > targetMiddle) {
                                myNewIndex   = index
                                targetBounds = children[myNewIndex].bounds
                                return@loop
                            }
                        }
                    }
                    else                     -> {
                        children.forEachIndexed { index, tab ->
                            val targetMiddle = tab.x + tab.transform.translateX + tab.width / 2

                            if (myOffset < targetMiddle) {
                                myNewIndex   = index
                                targetBounds = children[myNewIndex].bounds
                                return@loop
                            }
                        }
                    }
                }
            }

            val oldTransform = transform

//            animation = behavior?.moveColumn(table) {
                transform = when {
                    index < myNewIndex -> oldTransform.translate((targetBounds.right - width - myOffset) * 1f)
                    else               -> oldTransform.translate((targetBounds.x             - myOffset) * 1f)
                }
//            }?.apply {
//                completed += {
                    if (index != myNewIndex) {
                        panel[movingIndex]?.let { panel.move(it, to = myNewIndex) }
                    }

                    children.forEach {
                        moveAnimations.remove(it)?.cancel()
                        it.transform = Identity
                    }
//                }
//            }
        }
    }

    private fun <T> doAnimation(panel: TabbedPanel<T>, tab: Tab<T>, start: Float, end: Float) {
        cleanupAnimation(tab)

        val tabColor = tab.backgroundColor

        colorAnimations[tab] = (animate (start to end) using fixedTimeLinear(250 * milliseconds)) {
            tab.backgroundColor = tabColor?.opacity(it)

            tab.rerenderNow()
        }.apply {
            completed += {
                colorAnimations.remove(tab)
                if (tab.backgroundColor?.opacity == 0f) {
                    tab.backgroundColor = null
                }
            }
        }
    }
}

public typealias TabContainerFactory<T> = (TabbedPanel<T>, TabProducer<T>) -> TabContainer<T>

public open class BasicTabbedPanelBehavior<T>(
        private val tabProducer    : TabProducer<T>,
        private val backgroundColor: Color = Color(0xdee1e6u),
        private val tabContainer   : TabContainerFactory<T> = { panel, tabProducer -> SimpleTabContainer(panel, tabProducer) }): TabbedPanelBehavior<T>() {

    override fun install(view: TabbedPanel<T>) {
        view.apply {
            val tabContainer = tabContainer(view, tabProducer)
            children += tabContainer

            view.forEachIndexed { index, item ->
                children.add(view.visualizer(item).apply {
                    visible = item == view.selectedItem
                    tabContainer.registerTab(view, index, this)
                })
            }

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

    override fun uninstall(view: TabbedPanel<T>) {
        view.apply {
            children.clear()
            layout = null
        }
    }

    override fun selectionChanged(panel: TabbedPanel<T>, new: T?, newIndex: Int?, old: T?, oldIndex: Int?) {
        val dirty = mutableSetOf<Int>()

        oldIndex?.let {
            panel.children.getOrNull(it + 1)?.visible = false

            dirty += it
        }

        newIndex?.let {
            panel.children.getOrNull(it + 1)?.visible = true

            dirty += it
        }

        (panel.children.getOrNull(0) as? TabContainer<T>)?.selectionChanged(panel, newIndex, oldIndex)
    }

    override fun itemsChanged(panel: TabbedPanel<T>, removed: Map<Int, T>, added: Map<Int, T>, moved: Map<Int, Pair<Int, T>>) {
        (panel.children.getOrNull(0) as? TabContainer<T>)?.apply {
            itemsChanged(panel, removed, added, moved)
        }

        removed.keys.forEach { panel.children.removeAt(it + 1) }

        added.forEach { (index, item) ->
            panel.children.addOrAppend(index + 1, panel.visualizer(item))
        }

        moved.forEach { (new, old) ->
            panel.children.addOrAppend(new + 1, panel.children.removeAt(old.first + 1))
        }
    }

    override fun render(view: TabbedPanel<T>, canvas: Canvas) {
        canvas.rect(view.bounds.atOrigin, backgroundColor.paint)
    }
}