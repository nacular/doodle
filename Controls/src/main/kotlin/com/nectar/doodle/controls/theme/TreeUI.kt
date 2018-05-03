package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.text.Label
import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.controls.theme.TreeUI.ItemPositioner
import com.nectar.doodle.controls.theme.TreeUI.ItemUIGenerator
import com.nectar.doodle.controls.tree.Tree
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.Color.Companion.red
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.layout.ConstraintLayout
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Meta
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.Path
import com.nectar.doodle.utils.isEven
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 3/23/18.
 */

interface TreeUI<T>: Renderer<Tree<T>> {
    interface ItemUIGenerator<T> {
        operator fun invoke(tree: Tree<T>, node: T, path: Path<Int>, index: Int, current: Gizmo? = null): Gizmo
    }

    interface ItemPositioner<T> {
        operator fun invoke(tree: Tree<T>, node: T, path: Path<Int>, index: Int): Rectangle

        fun rowFor(tree: Tree<T>, y: Double): Int
    }

    val positioner : ItemPositioner<T>
    val uiGenerator: ItemUIGenerator<T>
}

private class BasicPositioner<T>(private val height: Double): ItemPositioner<T> {
    override fun invoke(tree: Tree<T>, node: T, path: Path<Int>, index: Int): Rectangle {
        return Rectangle(tree.insets.left, tree.insets.top + index * height, tree.width - tree.insets.run { left + right }, height)
    }

    override fun rowFor(tree: Tree<T>, y: Double): Int {
        return max(0, ((y - tree.insets.top) / height).toInt())
    }
}

private class TreeRow(private val labelFactory: LabelFactory, tree: Tree<*>, node: Any?, private var path: Path<Int>, index: Int): Gizmo() {

    private var depth     = -1
    private val iconWidth = 20.0

    private var icon = null as Label?

    private val label = labelFactory(node.toString()).apply {
        fitText             = false
        horizontalAlignment = Left
    }

    private var background = lightgray

    private lateinit var constraintLayout: ConstraintLayout

    init {
        styleChanged += { rerender() }

        children += label

        mouseChanged += object: MouseListener {
            private var pressed   = false
            private var mouseOver = false

            override fun mouseEntered(event: MouseEvent) {
                mouseOver       = true
                backgroundColor = backgroundColor?.lighter(0.25f)
            }

            override fun mouseExited(event: MouseEvent) {
                mouseOver       = false
                backgroundColor = background
            }

            override fun mousePressed(event: MouseEvent) {
                pressed = true
            }

            override fun mouseReleased(event: MouseEvent) {
                if (mouseOver && pressed) {
                    setOf(path).also {
                        tree.apply {
                            when {
                                selected(path)          -> removeSelection(it)
                                Ctrl in event.modifiers ||
                                Meta in event.modifiers -> addSelection   (it)
                                else                    -> setSelection   (it)
                            }
                        }
                    }
                }
                pressed = false
            }
        }

        update(tree, node, path, index)
    }

    fun update(tree: Tree<*>, node: Any?, path: Path<Int>, index: Int) {
        this.path = path

        val newDepth = (path.depth - if (!tree.rootVisible) 1 else 0)

        if (newDepth != depth) {
            constraintLayout = constrain(label) { label ->
                label.top    = label.parent.top
                label.left   = label.parent.left + { iconWidth * (1 + newDepth) }
                label.right  = label.parent.right
                label.bottom = label.parent.bottom
            }

            constrainIcon(icon)

            layout = constraintLayout
            depth  = newDepth
        }

        when (!tree.isLeaf(this.path)) {
            true -> {
                val text = if (tree.expanded(path)) "-" else "+"

                icon = icon?.apply {
                    this.text = text
                } ?: labelFactory(text).apply {
                    fitText = false
                    width   = iconWidth

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
                            }
                            pressed = false
                        }
                    }

                    constrainIcon(this)
                }
            }

            false -> {
                icon?.let {
                    this.children -= it
                    constraintLayout.unconstrain(it)
                }
                icon = null
            }
        }

        label.text = node.toString()

        background = if (tree.selected(path)) green else lightgray

        background = when {
            index.isEven -> background.lighter()
            else         -> background
        }

        backgroundColor = background
    }

    override fun render(canvas: Canvas) {
        backgroundColor?.let { canvas.rect(bounds.atOrigin, ColorBrush(it)) }
    }

    private fun constrainIcon(icon: Label?) {
        icon?.let {
            constraintLayout.constrain(it, label) { icon, label ->
                icon.top    = label.top
                icon.right  = label.left
                icon.bottom = label.bottom
            }
        }
    }
}

class LabelItemUIGenerator<T>(private val labelFactory: LabelFactory): ItemUIGenerator<T> {
    override fun invoke(tree: Tree<T>, node: T, path: Path<Int>, index: Int, current: Gizmo?): Gizmo = when (current) {
        is TreeRow -> current.apply { update(tree, node, path, index) }
        else       -> TreeRow(labelFactory, tree, node, path, index)
    }
}

class AbstractTreeUI<T>(labelFactory: LabelFactory): TreeUI<T> {
    override val positioner: ItemPositioner<T> = BasicPositioner(20.0)

    override val uiGenerator: ItemUIGenerator<T> = LabelItemUIGenerator(labelFactory)

    override fun render(gizmo: Tree<T>, canvas: Canvas) {
        canvas.rect(gizmo.bounds.atOrigin, Pen(red), ColorBrush(white))
    }

    override fun install(gizmo: Tree<T>) {
        gizmo.insets = Insets(2.0)
    }
}
