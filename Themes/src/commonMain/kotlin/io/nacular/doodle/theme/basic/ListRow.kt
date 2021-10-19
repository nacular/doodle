package io.nacular.doodle.theme.basic

import io.nacular.doodle.accessibility.ListItemRole
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.SimpleIndexedItem
import io.nacular.doodle.controls.list.ListLike
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.VerticalConstraint
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.system.SystemInputEvent.Modifier.Ctrl
import io.nacular.doodle.system.SystemInputEvent.Modifier.Meta
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift
import io.nacular.doodle.utils.observable
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 4/8/19.
 */
public open class ListRow<T>(
                    list                           : ListLike,
        public  var row                            : T,
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

    public var insetTop: Double = if (backgroundSelectionColor != null || backgroundSelectionBlurredColor != null) 1.0 else 0.0

    public var positioner: Constraints.() -> Unit = { centerY = parent.centerY }
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

        children += itemVisualizer.invoke(row, context = SimpleIndexedItem(index, listSelected))

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

        update(list, row, index)
    }

    public fun update(list: ListLike, row: T, index: Int) {
        this.row   = row
        this.index = index

        role.index    = index
        role.listSize = list.numRows

        children[0] = itemVisualizer(row, children.firstOrNull(), SimpleIndexedItem(index, list.selected(index)))

        idealSize = children[0].idealSize?.let { Size(it.width, it.height + insetTop) }
        layout    = constrainLayout(children[0])

        this.list  = list
    }

    override fun render(canvas: Canvas) {
        backgroundColor?.let { canvas.rect(bounds.atOrigin.inset(Insets(top = insetTop)), it.paint) }
    }

    private fun constrainLayout(view: View) = constrain(view) { content ->
        positioner(
            // Override the parent for content to confine it within a smaller region
            ConstraintWrapper(content) { parent ->
                object: ParentConstraintWrapper(parent) {
                    override val top = VerticalConstraint(this@ListRow) { insetTop }
                }
            }
        )
    }
}

public open class ListPositioner(protected open val height: Double, protected open val spacing: Double = 0.0) {
    public fun rowFor(insets: Insets, y: Double): Int = max(0, ((y - insets.top) / (height + spacing)).toInt())

    public fun totalHeight(numItems: Int, insets: Insets): Double = numItems * height + insets.run { top + bottom }

    public fun rowBounds(width: Double, insets: Insets, index: Int, current: View? = null): Rectangle = Rectangle(
            insets.left,
            insets.top + index * height + (index + 1) * spacing,
            max(0.0, width - insets.run { left + right }),
            height
    )
}
