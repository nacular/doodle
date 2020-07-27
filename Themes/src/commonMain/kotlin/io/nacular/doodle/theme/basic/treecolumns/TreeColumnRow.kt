package io.nacular.doodle.theme.basic.treecolumns

import io.nacular.doodle.controls.IndexedItemVisualizer
import io.nacular.doodle.controls.treecolumns.TreeColumns
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.ColorFill
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.ConstraintLayout
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.HorizontalConstraint
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.MagnitudeConstraint
import io.nacular.doodle.layout.VerticalConstraint
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.system.SystemInputEvent.Modifier.Ctrl
import io.nacular.doodle.system.SystemInputEvent.Modifier.Meta
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift
import io.nacular.doodle.theme.basic.ConstraintWrapper
import io.nacular.doodle.theme.basic.ParentConstraintWrapper
import io.nacular.doodle.utils.Path
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 7/25/20.
 */
abstract class TreeColumnRowIcon: View() {
    abstract var selected: Boolean
}

class SimpleTreeColumnRowIcon(private val color: Color = Black, private val selectedColor: Color = White): TreeColumnRowIcon() {
    override var selected = false

    override fun render(canvas: Canvas) {
        val centeredRect = bounds.atOrigin.inset(6.0)

        canvas.transform(transform) {
            path(listOf(
                    centeredRect.position,
                    Point(centeredRect.right, centeredRect.y + centeredRect.height / 2),
                    Point(centeredRect.x, centeredRect.bottom)),
                    ColorFill(if (selected) selectedColor else color))
        }
    }
}

class TreeColumnRow<T>(
                    treeColumns          : TreeColumns<T, *>,
                    node                 : T,
                var path                 : Path<Int>,
        private var index                : Int,
        private val itemVisualizer       : IndexedItemVisualizer<T>,
        private val selectionColor       : Color? = Color.Green,
        private val selectionBlurredColor: Color? = selectionColor,
        private val iconFactory          : () -> TreeColumnRowIcon): View() {

    var insetTop = 1.0

    var positioner: Constraints.() -> Unit = { left = parent.left; centerY = parent.centerY }
        set(new) {
            if (field == new) {
                return
            }

            field = new

            layout = constrainLayout(children[0])
        }

    private var icon    = null as TreeColumnRowIcon?
    private var content = itemVisualizer(node, index, null) { treeColumns.enclosedBySelection(path) }
        private set(new) {
            if (field != new) {
                children.batch {
                    remove(field)
                    field = new
                    add(field)
                }
            }
        }

    private  val iconWidth   = 20.0
    private  var pointerOver = false

    private lateinit var constraintLayout: ConstraintLayout

    init {
        children       += content
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
                    setOf(path).also {
                        treeColumns.apply {
                            when {
                                Ctrl in event.modifiers || Meta in event.modifiers -> toggleSelection(it)
                                Shift in event.modifiers && lastSelection != null  -> {
//                                    selectionAnchor?.let { rowFromPath(it) }?.let { anchor ->
//                                        rowFromPath(path)?.let { current ->
//                                            when {
//                                                current < anchor  -> setSelection((current .. anchor ).reversed().toSet())
//                                                anchor  < current -> setSelection((anchor  .. current).           toSet())
//                                            }
//                                        }
//                                    }
                                }
                                else                                               -> setSelection(it)
                            }
                        }
                    }
                }

                pressed = false
            }
        }

        update(treeColumns, node, path, index)
    }

    private fun constrainLayout(view: View) = constrain(view) { content ->
        positioner(
                // Override the parent for content to confine it within a smaller region
                ConstraintWrapper(content) { parent ->
                    object: ParentConstraintWrapper(parent) {
                        override val top    = VerticalConstraint  (this@TreeColumnRow) { insetTop              }
                        override val right  = HorizontalConstraint(this@TreeColumnRow) { it.width  - iconWidth }
                        override val width  = MagnitudeConstraint (this@TreeColumnRow) { it.width  - iconWidth }
                        override val height = MagnitudeConstraint (this@TreeColumnRow) { it.height - insetTop  }
                    }
                }
        )
    }

    fun update(tree: TreeColumns<T, *>, node: T, path: Path<Int>, index: Int) {
        this.path  = path
        this.index = index

        update(itemVisualizer(node, index, content) { tree.enclosedBySelection(path) }, tree)
    }

    fun update(content: View, treeColumns: TreeColumns<T, *>) {
        this.content = content

        constraintLayout = constrainLayout(content)

        constrainIcon(icon)

        layout = constraintLayout

        if (treeColumns.isLeaf(path)) {
            icon?.let {
                this.children -= it
                constraintLayout.unconstrain(it)
            }
            icon = null
        } else  {
            icon = icon ?: iconFactory().apply {
                width  = iconWidth
                height = width

                this@TreeColumnRow.children += this

                constrainIcon(this)
            }

            icon?.apply {
                selected = treeColumns.enclosedBySelection(path)
            }
        }

        idealSize       = Size(children.map { it.width }.reduce { a, b -> a + b  }, children.map { it.height }.reduce { a, b -> max(a, b) })
        backgroundColor = when {
            treeColumns.selected           (path) -> if (treeColumns.hasFocus) selectionColor else selectionBlurredColor
            treeColumns.enclosedBySelection(path) -> selectionBlurredColor
            else                                  -> null
        }
    }

    override fun render(canvas: Canvas) {
        backgroundColor?.let { canvas.rect(bounds.atOrigin.inset(Insets(top = insetTop)), ColorFill(it)) }
    }

    private fun constrainIcon(icon: View?) {
        icon?.let {
            constraintLayout.constrain(it, content) { icon, content ->
                icon.right   = parent.right
                icon.centerY = content.centerY
            }
        }
    }
}