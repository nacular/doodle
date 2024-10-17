package io.nacular.doodle.theme.basic

import io.nacular.doodle.accessibility.ListItemRole
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.SimpleIndexedItem
import io.nacular.doodle.controls.list.ListLike
import io.nacular.doodle.core.View
import io.nacular.doodle.core.fixed
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.system.SystemInputEvent.Modifier.Ctrl
import io.nacular.doodle.system.SystemInputEvent.Modifier.Meta
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift
import io.nacular.doodle.utils.observable
import kotlin.math.ceil
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 4/8/19.
 */
@Suppress("LeakingThis")
public open class ListItem<T>(
                    list                           : ListLike,
        public  var item                           : T,
        public  var index                          : Int,
        private val itemVisualizer                 : ItemVisualizer<T, IndexedItem>,
        private val backgroundSelectionColor       : Color? = Blue,
        private val backgroundSelectionBlurredColor: Color? = backgroundSelectionColor,
        private val role                           : ListItemRole = ListItemRole()): View(role) {

    public var list: ListLike = list
        private set(new) {
            field.focusChanged -= listFocusChanged

            field = new

            when {
                field.selected(index) -> {
                    field.focusChanged += listFocusChanged

                    backgroundColor = if (field.hasFocus) backgroundSelectionColor else backgroundSelectionBlurredColor
                }
                else                  -> {
                    field.focusChanged -= listFocusChanged

                    backgroundColor = null
                }
            }
        }

    public var insetTop : Double = if (backgroundSelectionColor != null || backgroundSelectionBlurredColor != null) 1.0 else 0.0
    public var insetLeft: Double = 0.0

    public var positioner: ConstraintDslContext.(Bounds) -> Unit = defaultPositioner
        set(new) {
            if (field == new) {
                return
            }

            field = new

            layout = constrainLayout(children[0])
        }

    private var pointerOver by observable(false) { _,new ->
        pointerOver(new)
    }

    protected open fun pointerOver(value: Boolean) {}

    private val listFocusChanged = { _:View, _:Boolean, new:Boolean ->
        if (list.selected(index)) {
            backgroundColor = if (new) backgroundSelectionColor else backgroundSelectionBlurredColor
        }
    }

    init {
        val listSelected = list.selected(index)

        children += itemVisualizer(item, context = SimpleIndexedItem(index, listSelected))

        focusable       = false
        styleChanged   += { rerender() }
        pointerChanged += object: PointerListener {
            private var pressed = false

            override fun entered(event: PointerEvent) {
                pointerOver = true
            }

            override fun exited(event: PointerEvent) {
                pointerOver = false
            }

            override fun pressed(event: PointerEvent) {
                pressed = true
            }

            override fun released(event: PointerEvent) {
                if (pointerOver && pressed) {
                    setOf(index).also {
                        list.apply {
                            when {
                                Ctrl  in event.modifiers || Meta in event.modifiers -> toggleSelection(it)
                                Shift in event.modifiers && lastSelection != null   -> {
                                    selectionAnchor?.let { anchor ->
                                        val current = index
                                        when {
                                            current < anchor  -> setSelection((current .. anchor ).reversed().toSet())
                                            anchor  < current -> setSelection((anchor  .. current).           toSet())
                                        }
                                    }
                                }
                                else                                                -> setSelection(it)
                            }
                        }
                    }
                }
                pressed = false
            }
        }

        update(list, item, index)
        layout = constrainLayout(children[0])
    }

    public fun update(list: ListLike, item: T, index: Int) {
        this.item     = item
        this.index    = index

        role.index    = index
        role.listSize = list.numItems
        role.selected = list.selected(index)

        val offset    = Point(insetLeft, insetTop)
        val oldChild  = children.firstOrNull()

        children[0] = itemVisualizer(item, children.firstOrNull(), SimpleIndexedItem(index, role.selected)).also { visualizer ->
            if (visualizer != oldChild) {
                oldChild?.let { it.position -= offset }
                visualizer.position += offset
                layout = constrainLayout(children[0])
            }
        }

        preferredSize = fixed(children[0].idealSize.let { Size(it.width + insetLeft, it.height + insetTop) })
        this.list = list
    }

    override fun render(canvas: Canvas) {
        backgroundColor?.let { canvas.rect(bounds.atOrigin.inset(Insets(top = insetTop, left = insetLeft)), it.paint) }
    }

    private fun constrainLayout(view: View) = constrain(view) { content ->
        positioner(content.withOffset(left = insetLeft, top = insetTop))
    }

    protected companion object {
        public val defaultPositioner: ConstraintDslContext.(Bounds) -> Unit = { it.centerY eq parent.centerY; it.size eq it.preferredSize }
    }
}

public open class VerticalListPositioner(protected open val height: Double, numColumns: Int = 1, protected open val spacing: Double = 0.0) {
    protected val numColumns: Int = max(1, numColumns)

    public fun itemFor(size: Size, insets: Insets, at: Point): Int = max(0,
        ((at.y - insets.top) / (height + spacing)).toInt() * numColumns + columnFactor(at.x, size, insets)
    )

    public fun minimumSize(numItems: Int, insets: Insets): Size {
        val rows = ceil(numItems.toDouble() / numColumns)

        return Size(0.0, rows * height + (rows - 1) * spacing + insets.run { top + bottom })
    }

    @Suppress("UNUSED_PARAMETER")
    public fun itemBounds(size: Size, insets: Insets, index: Int, current: View? = null): Rectangle = Rectangle(
        x      = insets.left + (index % numColumns) * size.width / numColumns,
        y      = insets.top  + (index / numColumns) * height + (index / numColumns + 1) * spacing,
        width  = columnWidth(size, insets),
        height = height
    )

    private fun columnFactor(x: Double, size: Size, insets: Insets) = when (val width = columnWidth(size, insets)) {
        0.0  -> 0
        else -> (x / width).toInt()
    }

    private fun columnWidth(size: Size, insets: Insets) = max(0.0, size.width / numColumns - insets.run { left + right })
}

public open class HorizontalListPositioner(protected open val width: Double, numRows: Int = 1, protected open val spacing: Double = 0.0) {
    protected val numRows: Int = max(1, numRows)

    public fun itemFor(size: Size, insets: Insets, at: Point): Int = max(0,
        ((at.x - insets.left) / (width + spacing)).toInt() * numRows + rowFactor(at.y, size, insets)
    )

    public fun minimumSize(numItems: Int, insets: Insets): Size {
        val cols = ceil(numItems.toDouble() / numRows)

        return Size(cols * width + (cols - 1) * spacing + insets.run { left + right }, 0.0)
    }

    @Suppress("UNUSED_PARAMETER")
    public fun itemBounds(size: Size, insets: Insets, index: Int, current: View? = null): Rectangle = Rectangle(
        x      = insets.left + (index / numRows) * width + (index / numRows + 1) * spacing,
        y      = insets.top  + (index % numRows) * size.height / numRows,
        width  = width,
        height = rowHeight(size, insets)
    )

    private fun rowFactor(y: Double, size: Size, insets: Insets) = when (val height = rowHeight(size, insets)) {
        0.0  -> 0
        else -> (y / height).toInt()
    }

    private fun rowHeight(size: Size, insets: Insets) = max(size.height / numRows - insets.run { top + bottom }, 0.0)
}
