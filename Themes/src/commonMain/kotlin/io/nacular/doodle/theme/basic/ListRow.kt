package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.IndexedItemVisualizer
import io.nacular.doodle.controls.list.ListLike
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.ColorFill
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.VerticalConstraint
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.system.SystemInputEvent.Modifier.Ctrl
import io.nacular.doodle.system.SystemInputEvent.Modifier.Meta
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 4/8/19.
 */
open class ListRow<T>(private var list                           : ListLike,
                      private var row                            : T,
                              var index                          : Int,
                      private val itemVisualizer                 : IndexedItemVisualizer<T>,
                      private val backgroundSelectionColor       : Color? = Blue,
                      private val backgroundSelectionBlurredColor: Color? = backgroundSelectionColor): View() {

    var insetTop = 1.0

    var positioner: Constraints.() -> Unit = { centerY = parent.centerY }
        set(new) {
            if (field == new) {
                return
            }

            field = new

            layout = constrainLayout(children[0])
        }

    private var pointerOver = false

    private val listFocusChanged = { _:View, _:Boolean, new:Boolean ->
        if (list.selected(index)) {
            backgroundColor = if (new) backgroundSelectionColor else backgroundSelectionBlurredColor
        }
    }

    init {
        childrenChanged += { _,_,_,_ ->
            layout = constrainLayout(children[0])
        }

        children += itemVisualizer(row, index)

        styleChanged += { rerender() }
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
                                Shift in event.modifiers && lastSelection != null  -> {
                                    selectionAnchor?.let { anchor ->
                                        val current = index
                                        when {
                                            current < anchor  -> setSelection((current .. anchor ).reversed().toSet())
                                            anchor  < current -> setSelection((anchor  .. current).           toSet())
                                        }
                                    }
                                }
                                else                                               -> setSelection(it)
                            }
                        }
                    }
                }
                pressed = false
            }
        }

        update(list, row, index)
    }

    fun update(list: ListLike, row: T, index: Int) {
        this.list  = list
        this.row   = row
        this.index = index

        val listSelected = list.selected(index)

        children[0] = itemVisualizer(row, index, children.firstOrNull()) { listSelected }

        when {
            listSelected -> {
                list.focusChanged += listFocusChanged

                backgroundColor = if (list.hasFocus) backgroundSelectionColor else backgroundSelectionBlurredColor
            }
            else         -> {
                list.focusChanged -= listFocusChanged

                backgroundColor = null
            }
        }
    }

    override fun render(canvas: Canvas) {
        backgroundColor?.let { canvas.rect(bounds.atOrigin.inset(Insets(top = insetTop)), ColorFill(it)) }
    }

    private fun constrainLayout(view: View) = constrain(view) { content ->
        positioner(
                // Override the parent for content to confine it within a smaller region
                ConstraintWrapper(content) { parent ->
                    object: ParentConstraintWrapper(parent) {
                        override val top = VerticalConstraint  (this@ListRow) { insetTop }
                    }
                }
        )
    }
}

open class ListPositioner(private val height: Double, private val spacing: Double = 0.0) {
    fun rowFor(insets: Insets, y: Double) = max(0, ((y - insets.top) / (height + spacing)).toInt())

    operator fun invoke(width: Double, insets: Insets, index: Int) = Rectangle(
            insets.left,
            insets.top + index * height + (index + 1) * spacing,
            width - insets.run { left + right },
            height
    )
}
