package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.ItemVisualizer
import com.nectar.doodle.controls.tree.TreeLike
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.ConstraintLayout
import com.nectar.doodle.layout.Constraints
import com.nectar.doodle.layout.HorizontalConstraint
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.layout.MagnitudeConstraint
import com.nectar.doodle.layout.ParentConstraints
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Meta
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift
import com.nectar.doodle.utils.Path
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 5/7/19.
 */

private class ConstraintWrapper(delegate: Constraints, parent: (ParentConstraints) -> ParentConstraints): Constraints by delegate {
    override val parent = parent(delegate.parent)
}

private open class ParentConstraintWrapper(delegate: ParentConstraints): ParentConstraints by delegate

interface ContentGenerator<T>: ItemVisualizer<T> {
    fun position(tree: TreeLike, node: T, path: Path<Int>, index: Int): Constraints.() -> Unit = {
        left    = parent.left
        centerY = parent.centerY
    }
}

abstract class TreeRowIcon: View() {
    abstract var expanded: Boolean
}

class SimpleTreeRowIcon(private val color: Color): TreeRowIcon() {
    override var expanded = false
        set (new) {
            field = new
            rerender()
        }

    override fun render(canvas: Canvas) {
        val pen      = Pen(color)
        val length   = 3.5
        val width_2  = width  / 2
        val height_2 = height / 2

        if (!expanded) {
            canvas.line(Point(width_2, height_2 - length), Point(width_2, height_2 + length), pen)
        }

        canvas.line(Point(width_2 - length, height_2), Point(width_2 + length, height_2), pen)
    }
}

class TreeRow<T>(tree                : TreeLike, node: T,
             var path                : Path<Int>,
     private var index               : Int,
     private val contentGenerator    : ContentGenerator<T>,
     private val selectionColor      : Color? = green,
     private val selectionBluredColor: Color? = selectionColor,
     private val iconFactory         : () -> TreeRowIcon): View() {

    private  var icon      = null as TreeRowIcon?
    private  var depth     = -1
    internal var content   = contentGenerator(node)
    private  val iconWidth = 20.0
    private  var mouseOver = false

    private val treeFocusChanged = { _:View, _:Boolean, new:Boolean ->
        if (tree.selected(index)) {
            backgroundColor = if (new) selectionColor else selectionBluredColor
        }
    }

    private lateinit var constraintLayout: ConstraintLayout

    init {
        children     += content
        styleChanged += { rerender() }
        mouseChanged += object: MouseListener {
            private var pressed   = false

            override fun mouseEntered(event: MouseEvent) {
                mouseOver = true
            }

            override fun mouseExited(event: MouseEvent) {
                mouseOver = false
            }

            override fun mousePressed(event: MouseEvent) {
                pressed = true
            }

            override fun mouseReleased(event: MouseEvent) {
                if (mouseOver && pressed) {
                    setOf(path).also {
                        tree.apply {
                            when {
                                Ctrl  in event.modifiers || Meta in event.modifiers -> toggleSelection(it)
                                Shift in event.modifiers && lastSelection != null -> {
                                    selectionAnchor?.let { rowFromPath(it) }?.let { anchor ->
                                        rowFromPath(path)?.let { current ->
                                            when {
                                                current < anchor  -> setSelection((current .. anchor ).reversed().toSet())
                                                anchor  < current -> setSelection((anchor  .. current).           toSet())
                                            }
                                        }
                                    }
                                }
                                else -> setSelection(it)
                            }
                        }
                    }
                }

                pressed = false
            }
        }

        update(tree, node, path, index)
    }

    fun update(tree: TreeLike, node: T, path: Path<Int>, index: Int) {
        this.path  = path
        this.index = index

        content = contentGenerator(node, content).also {
            if (it != content) {
                children.batch {
                    remove(content)
                    add   (it     )
                }
                depth = -1 // force layout
            }
        }

        val newDepth = (path.depth - if (!tree.rootVisible) 1 else 0)

        if (newDepth != depth) {
            constraintLayout = constrain(content) {
                contentGenerator.position(tree, node, path, index)(ConstraintWrapper(it) { parent ->
                    object: ParentConstraintWrapper(parent) {
                        override val left  = HorizontalConstraint(this@TreeRow) { iconWidth * (1 + newDepth) }
                        override val width = MagnitudeConstraint (this@TreeRow) { it.width - iconWidth * (1 + newDepth) }
                    }
                })
            }

            constrainIcon(icon)

            layout = constraintLayout
            depth  = newDepth
        }

        if (tree.isLeaf(this.path)) {
            icon?.let {
                this.children -= it
                constraintLayout.unconstrain(it)
            }
            icon = null
        } else  {
            icon = icon?.apply { expanded = tree.expanded(path) } ?: iconFactory().apply {
                width    = iconWidth
                height   = width
                expanded = tree.expanded(path)

                this@TreeRow.children += this

                mouseChanged += object: MouseListener {
                    private var pressed   = false
                    private var mouseOver = false

                    override fun mouseEntered(event: MouseEvent) {
                        mouseOver = true
                    }

                    override fun mouseExited(event: MouseEvent) {
                        mouseOver = false
                    }

                    override fun mousePressed(event: MouseEvent) {
                        pressed   = true
                        mouseOver = true
                    }

                    override fun mouseReleased(event: MouseEvent) {
                        if (mouseOver && pressed) {
                            when (tree.expanded(this@TreeRow.path)) {
                                true -> tree.collapse(this@TreeRow.path)
                                else -> tree.expand  (this@TreeRow.path)
                            }

                            event.consume()
                        }
                        pressed = false
                    }
                }

                constrainIcon(this)
            }
        }

        idealSize       = Size(children.map { it.width }.reduce { a, b -> a + b  }, children.map { it.height }.reduce { a, b -> max(a, b) })
        backgroundColor = when {
            tree.selected(index) -> {
                tree.focusChanged += treeFocusChanged

                if (tree.hasFocus) selectionColor else selectionBluredColor
            }
            else                 -> {
                tree.focusChanged -= treeFocusChanged
                null
            }
        }
    }

    override fun render(canvas: Canvas) {
        backgroundColor?.let { canvas.rect(bounds.atOrigin.inset(Insets(top = 1.0)), ColorBrush(it)) }
    }

    private fun constrainIcon(icon: TreeRowIcon?) {
        icon?.let {
            constraintLayout.constrain(it, content) { icon, label ->
                icon.right   = label.left
                icon.centerY = label.centerY
            }
        }
    }
}