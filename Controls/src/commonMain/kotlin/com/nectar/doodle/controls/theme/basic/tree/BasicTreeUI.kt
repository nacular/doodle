package com.nectar.doodle.controls.theme.basic.tree

import com.nectar.doodle.controls.text.Label
import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.controls.theme.TreeRenderer
import com.nectar.doodle.controls.theme.TreeRenderer.RowGenerator
import com.nectar.doodle.controls.theme.TreeRenderer.RowPositioner
import com.nectar.doodle.controls.tree.EditOperation
import com.nectar.doodle.controls.tree.Model
import com.nectar.doodle.controls.tree.MutableTree
import com.nectar.doodle.controls.tree.Tree
import com.nectar.doodle.controls.tree.TreeEditor
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.layout.ConstraintLayout
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Meta
import com.nectar.doodle.utils.Encoder
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.ObservableSet
import com.nectar.doodle.utils.Path
import com.nectar.doodle.utils.isEven
import kotlin.math.max

private class BasicTreeRowPositioner<T>(private val height: Double): RowPositioner<T> {
    override fun invoke(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int): Rectangle {
        return Rectangle(tree.insets.left, tree.insets.top + index * height, tree.width - tree.insets.run { left + right }, height)
    }

    override fun row(of: Tree<T, *>, atY: Double): Int {
        return max(0, ((atY - of.insets.top) / height).toInt())
    }
}

private class TreeRow(private val labelFactory: LabelFactory, tree: Tree<*, *>, node: Any?, var path: Path<Int>, index: Int): View() {

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

    fun update(tree: Tree<*, *>, node: Any?, path: Path<Int>, index: Int) {
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

private open class TreeLabelItemUIGenerator<T>(private val labelFactory: LabelFactory): RowGenerator<T> {
    override fun invoke(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View?): View = when (current) {
        is TreeRow -> current.apply { update(tree, node, path, index) }
        else       -> TreeRow(labelFactory, tree, node, path, index)
    }
}

class BasicTreeUI<T>(labelFactory: LabelFactory): TreeRenderer<T> {
    override val positioner : RowPositioner<T>  = BasicTreeRowPositioner(20.0)
    override val generator: RowGenerator<T> = TreeLabelItemUIGenerator(labelFactory)

    override fun render(view: Tree<T, *>, canvas: Canvas) {
//        canvas.rect(view.bounds.atOrigin, Pen(red), ColorBrush(white))
    }

//    override fun install(view: Tree<T>) {
//        view.insets = Insets(2.0)
//    }
}

private class MutableLabelItemUIGenerator<T>(private val focusManager: FocusManager?, labelFactory: LabelFactory): TreeLabelItemUIGenerator<T>(labelFactory) {
    override fun invoke(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View?) = super.invoke(tree, node, path, index, current).also {
        if (current !is TreeRow) {
            val result = it as TreeRow

            it.mouseChanged += object: MouseListener {
                override fun mouseReleased(event: MouseEvent) {
                    if (event.clickCount == 2) {
                        (tree as MutableTree).startEditing(result.path)
                    } else {
                        focusManager?.requestFocus(tree)
                    }
                }
            }
        }
    }
}


class BasicMutableTreeUI<T>(focusManager: FocusManager?, labelFactory: LabelFactory): TreeRenderer<T>, KeyListener {
    override val positioner : TreeRenderer.RowPositioner<T>  = BasicTreeRowPositioner(20.0)
    override val generator: TreeRenderer.RowGenerator<T> = MutableLabelItemUIGenerator(focusManager, labelFactory)

    override fun render(view: Tree<T, *>, canvas: Canvas) {}

    override fun install(view: Tree<T, *>) {
        view.keyChanged += this
    }

    override fun uninstall(view: Tree<T, *>) {
        view.keyChanged -= this
    }

    override fun keyPressed(event: KeyEvent) {
        when (event.code) {
            KeyEvent.VK_DELETE, KeyEvent.VK_BACKSPACE -> (event.source as MutableTree<*, *>).let { tree ->
                tree.selection./*sortedByDescending { it }.*/forEach { tree.removeAt(it) }
            }
        }
    }
}

@Suppress("PrivatePropertyName", "unused")
open class TextEditOperation<T>(
        private val focusManager: FocusManager?,
        private val encoder     : Encoder<T, String>,
        private val tree        : MutableTree<T, *>,
                    node        : T,
        private var path        : Path<Int>): TextField(), EditOperation<T> {

    private val treeSelectionChanged_ = ::treeSelectionChanged

    init {
        tree.selectionChanged += treeSelectionChanged_

        text = encoder.encode(node) ?: ""

        horizontalAlignment = Left

        styleChanged += { rerender() }

        focusChanged += { _,_,_ ->
            if (!hasFocus) {
                tree.cancelEditing()
            }
        }

        this.keyChanged += object: KeyListener {
            override fun keyReleased(event: KeyEvent) {
                if (event.code == KeyEvent.VK_RETURN) {
                    tree.completeEditing()
                }
            }
        }
    }

    override fun addedToDisplay() {
        focusManager?.requestFocus(this)
        selectAll()
    }

    override fun invoke  () = this
    override fun complete() = encoder.decode(text)

    override fun cancel() {
        tree.selectionChanged -= treeSelectionChanged_
    }

    @Suppress("UNUSED_PARAMETER")
    private fun treeSelectionChanged(set: ObservableSet<out Tree<*, Model<*>>, *>, removed: Set<Path<Int>>, added: Set<Path<Int>>) {
        tree.cancelEditing()
    }
}

class TreeTextEditor<T>(private val focusManager: FocusManager?, private val encoder: Encoder<T, String>): TreeEditor<T> {
    override fun edit(tree: MutableTree<T, *>, node: T, path: Path<Int>, current: View?): EditOperation<T> = TextEditOperation(focusManager, encoder, tree, node, path)
}
