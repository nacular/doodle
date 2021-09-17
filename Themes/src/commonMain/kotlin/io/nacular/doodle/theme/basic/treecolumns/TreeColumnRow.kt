package io.nacular.doodle.theme.basic.treecolumns

import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.SimpleIndexedItem
import io.nacular.doodle.controls.treecolumns.TreeColumns
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.geometry.ConvexPolygon
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.rounded
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
public abstract class TreeColumnRowIcon: View() {
    public abstract var selected: Boolean
}

public class SimpleTreeColumnRowIcon(private val color: Color = Black, private val selectedColor: Color = White): TreeColumnRowIcon() {
    override var selected: Boolean = false

    override fun render(canvas: Canvas) {
        val centeredRect = bounds.atOrigin

        val path = ConvexPolygon(centeredRect.position,
                                 Point(centeredRect.right, centeredRect.y + centeredRect.height / 2),
                                 Point(centeredRect.x, centeredRect.bottom)).rounded(1.0)

        canvas.transform(transform) {
            path(path, ColorPaint(if (selected) selectedColor else color))
        }
    }
}

public class TreeColumnRow<T>(
                    treeColumns          : TreeColumns<T, *>,
                    node                 : T,
        public  var path                 : Path<Int>,
        private var index                : Int,
        private val itemVisualizer       : ItemVisualizer<T, IndexedItem>,
        private val selectionColor       : Color? = Color.Green,
        private val selectionBlurredColor: Color? = selectionColor,
        private val iconFactory          : () -> TreeColumnRowIcon): View() {

    public var insetTop: Double = 1.0

    public var positioner: Constraints.() -> Unit = { left = parent.left; centerY = parent.centerY }
        set(new) {
            if (field == new) {
                return
            }

            field = new

            layout = constrainLayout(children[0])
        }

    private var icon    = null as TreeColumnRowIcon?
    private var content = itemVisualizer.invoke(node, context = SimpleIndexedItem(index, selected = treeColumns.enclosedBySelection(path)))
        private set(new) {
            if (field != new) {
                children.batch {
                    remove(field)
                    field = new
                    add(field)
                }
            }
        }

    // FIXME: Inject
    private val iconWidth   = 9.0
    private val iconSpacing = 4.0
    private var pointerOver = false

    private lateinit var constraintLayout: ConstraintLayout

    private val columnsFocusChanged = { _:View, _:Boolean, _:Boolean ->
        backgroundColor = backgroundColor(treeColumns)
    }

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
                        override val top    = VerticalConstraint  (this@TreeColumnRow) { insetTop                                  }
                        override val right  = HorizontalConstraint(this@TreeColumnRow) { it.width  - (iconWidth + 2 * iconSpacing) }
                        override val width  = MagnitudeConstraint (this@TreeColumnRow) { it.width  - (iconWidth + 2 * iconSpacing) }
                        override val height = MagnitudeConstraint (this@TreeColumnRow) { it.height - insetTop                      }
                    }
                }
        )
    }

    public fun update(tree: TreeColumns<T, *>, node: T, path: Path<Int>, index: Int) {
        this.path  = path
        this.index = index

        update(itemVisualizer.invoke(node, content, SimpleIndexedItem(index, selected = tree.enclosedBySelection(path))), tree)
    }

    public fun update(content: View, treeColumns: TreeColumns<T, *>) {
        this.content = content

        idealSize = Size(
                (content.idealSize?.width ?: content.width) + iconWidth + 2 * iconSpacing,
                max(content.idealSize?.height ?: content.height, iconWidth)
        )

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
                size = Size(iconWidth, iconWidth)

                this@TreeColumnRow.children += this

                constrainIcon(this)
            }

            icon?.apply {
                selected = treeColumns.enclosedBySelection(path)
            }
        }

        backgroundColor = backgroundColor(treeColumns)
    }

    private fun backgroundColor(treeColumns: TreeColumns<T, *>): Color? {
        val selected = treeColumns.selected(path)

        when {
            selected -> treeColumns.focusChanged += columnsFocusChanged
            else     -> treeColumns.focusChanged -= columnsFocusChanged
        }

        return when {
            selected                              -> if (treeColumns.hasFocus) selectionColor else selectionBlurredColor
            treeColumns.enclosedBySelection(path) -> selectionBlurredColor
            else                                  -> null
        }
    }

    override fun render(canvas: Canvas) {
        backgroundColor?.let { canvas.rect(bounds.atOrigin.inset(Insets(top = insetTop)), ColorPaint(it)) }
    }

    private fun constrainIcon(icon: View?) {
        icon?.let {
            constraintLayout.constrain(it, content) { icon, content ->
                icon.right   = parent.right - iconSpacing
                icon.centerY = content.centerY
            }
        }
    }
}