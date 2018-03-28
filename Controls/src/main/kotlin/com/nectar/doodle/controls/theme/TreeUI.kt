package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.controls.tree.Path
import com.nectar.doodle.controls.tree.Tree
import com.nectar.doodle.core.Box
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.red
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.HorizontalAlignment.Center
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.isEven

/**
 * Created by Nicholas Eddy on 3/23/18.
 */

interface ItemUIGenerator<T> {
    operator fun invoke(tree: Tree<T>, node: T, path: Path<Int>, index: Int, selected: Boolean, hasFocus: Boolean, expanded: Boolean): Gizmo
}

interface ItemPositioner<T> {
    operator fun invoke(tree: Tree<T>, node: T, path: Path<Int>, index: Int, selected: Boolean, hasFocus: Boolean, expanded: Boolean): Rectangle
}

interface TreeUI<T>: Renderer<Tree<T>> {
    val positioner : ItemPositioner<T>
    val uiGenerator: ItemUIGenerator<T>
}

private class BasicPositioner<T>(private val height: Double): ItemPositioner<T> {
    override fun invoke(tree: Tree<T>, node: T, path: Path<Int>, index: Int, selected: Boolean, hasFocus: Boolean, expanded: Boolean): Rectangle {
        return Rectangle(0.0, index * height, tree.width, height)
    }
}

class LabelItemUIGenerator<T>(private val labelFactory: LabelFactory): ItemUIGenerator<T> {
    override fun invoke(tree: Tree<T>, node: T, path: Path<Int>, index: Int, selected: Boolean, hasFocus: Boolean, expanded: Boolean): Gizmo {
        val iconWidth = 20.0

        val icon = when (!tree.isLeaf(path)) {
            true -> labelFactory(if (expanded) "-" else "+").apply {
                fitText             = false
                horizontalAlignment = Center
            }
            false -> null
        }

        val label = labelFactory(node.toString()).apply {
            fitText             = false
            horizontalAlignment = Left
        }

        return object: Box() {
            override fun render(canvas: Canvas) {
                backgroundColor?.let { canvas.rect(bounds.atOrigin, ColorBrush(it)) }
            }
        }.apply<Box> {
            styleChanged += { rerender() }

            children += label
            icon?.let { children += it }

            val layout = constrain(label) { label ->
                label.top    = label.parent.top
                label.left   = label.parent.left + { iconWidth * (1 + (path.depth - if (!tree.rootVisible) 1 else 0)) }
                label.right  = label.parent.right
                label.bottom = label.parent.bottom
            }

            icon?.let {
                children += it

                it.width = iconWidth

                layout.constrain(it, label) { icon, label ->
                    icon.top    = label.top
                    icon.right  = label.left
                    icon.bottom = label.bottom
                }
            }

            this.layout = layout

            var background = if (selected) Color.green else Color.lightgray

            background = when {
                index.isEven -> background.lighter()
                else         -> background
            }

            backgroundColor = background

            mouseChanged += object: MouseListener {
                private var pressed   = false
                private var mouseOver = false

                override fun mouseEntered(event: MouseEvent) {
                    mouseOver = true
                    backgroundColor = backgroundColor?.lighter(0.25f)
                }

                override fun mouseExited(event: MouseEvent) {
                    mouseOver = false
                    backgroundColor = background
                }

                override fun mousePressed(event: MouseEvent) {
                    pressed = true
                    mouseOver = true
                }

                override fun mouseReleased(event: MouseEvent) {
                    if (mouseOver && pressed) {
                        if (!tree.isLeaf(path)) {
                            when (tree.expanded(path)) {
                                true -> tree.collapse(path)
                                else -> tree.expand  (path)
                            }
                        }
//                        setOf(index).also {
//                            tree.apply {
//                                when {
//                                    selected(path)          -> removeSelection(it)
//                                    Ctrl in event.modifiers ||
//                                    Meta in event.modifiers -> addSelection   (it)
//                                    else                    -> setSelection   (it)
//                                }
//                            }
//                        }
                    }
                    pressed = false
                }
            }
        }
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
