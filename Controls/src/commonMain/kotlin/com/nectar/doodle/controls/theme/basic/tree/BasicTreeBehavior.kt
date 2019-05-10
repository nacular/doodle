package com.nectar.doodle.controls.theme.basic.tree

import com.nectar.doodle.controls.text.Label
import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.controls.theme.TreeBehavior
import com.nectar.doodle.controls.theme.TreeBehavior.RowGenerator
import com.nectar.doodle.controls.theme.TreeBehavior.RowPositioner
import com.nectar.doodle.controls.theme.basic.ContentGenerator
import com.nectar.doodle.controls.theme.basic.TreeRow
import com.nectar.doodle.controls.tree.EditOperation
import com.nectar.doodle.controls.tree.MutableTree
import com.nectar.doodle.controls.tree.Tree
import com.nectar.doodle.controls.tree.TreeEditor
import com.nectar.doodle.controls.tree.TreeLike
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.CanvasBrush
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyEvent.Companion.VK_A
import com.nectar.doodle.event.KeyEvent.Companion.VK_BACKSPACE
import com.nectar.doodle.event.KeyEvent.Companion.VK_DELETE
import com.nectar.doodle.event.KeyEvent.Companion.VK_DOWN
import com.nectar.doodle.event.KeyEvent.Companion.VK_ESCAPE
import com.nectar.doodle.event.KeyEvent.Companion.VK_LEFT
import com.nectar.doodle.event.KeyEvent.Companion.VK_RETURN
import com.nectar.doodle.event.KeyEvent.Companion.VK_RIGHT
import com.nectar.doodle.event.KeyEvent.Companion.VK_UP
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Meta
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift
import com.nectar.doodle.utils.Encoder
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.Path
import com.nectar.doodle.utils.RelativePositionMonitor
import kotlin.math.max

private class BasicTreeRowPositioner<T>(private val height: Double): RowPositioner<T> {
    override fun rowBounds(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View?) = Rectangle(
            tree.insets.left,
            tree.insets.top + index * height,
            max(tree.width - tree.insets.run { left + right }, contentBounds(tree, node, path, index, current).right),
            height)

    override fun contentBounds(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View?) = when (current) {
        is TreeRow<*> -> current.content.bounds.let { it.at(y = it.y + index * height) }
        else               -> {
            // FIXME: Centralize
            val depth    = (path.depth - if (!tree.rootVisible) 1 else 0)
            val indent   = 20.0 * (1 + depth)
            val maxWidth = tree.width - tree.insets.run { left + right } - indent

            Rectangle(tree.insets.left + indent, tree.insets.top + index * height, maxWidth, height)
        }
    }

    override fun row(of: Tree<T, *>, atY: Double): Int {
        return max(0, ((atY - of.insets.top) / height).toInt())
    }
}

private class LabelContentGenerator<T>(private val labelFactory: LabelFactory): ContentGenerator<T> {
    override fun invoke(tree: TreeLike, node: T, path: Path<Int>, index: Int, previous: View?) = when (previous) {
        is Label -> { previous.text = node.toString(); previous }
        else     -> labelFactory(node.toString()).apply {
            fitText             = true
            horizontalAlignment = Left
        }
    }
}

open class BasicTreeRowGenerator<T>(private val focusManager: FocusManager?, private val contentGenerator: ContentGenerator<T>): RowGenerator<T> {
    override fun invoke(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View?): View = when (current) {
        is TreeRow<*> -> (current as TreeRow<T>).apply { update(tree, node, path, index) }
        else          -> TreeRow(tree, node, path, index, contentGenerator).apply {
            mouseChanged += object: MouseListener {
                override fun mouseReleased(event: MouseEvent) {
                    focusManager?.requestFocus(tree)
                }
            }
        }
    }
}

class BasicMutableTreeRowGenerator<T>(focusManager: FocusManager?, contentGenerator: ContentGenerator<T>): BasicTreeRowGenerator<T>(focusManager, contentGenerator) {
    override fun invoke(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View?) = super.invoke(tree, node, path, index, current).also {
        if (current !is TreeRow<*>) {
            val result = it as TreeRow<*>

            it.mouseChanged += object: MouseListener {
                override fun mouseReleased(event: MouseEvent) {
                    if (event.clickCount == 2) {
                        (tree as MutableTree).startEditing(result.path)
                    }
                }
            }
        }
    }
}

open class BasicTreeBehavior<T>(override val generator: RowGenerator<T>): TreeBehavior<T>, KeyListener {
    constructor(labelFactory: LabelFactory, focusManager: FocusManager?): this(BasicTreeRowGenerator(focusManager, LabelContentGenerator(labelFactory)))

    override val positioner: RowPositioner<T> = BasicTreeRowPositioner(20.0)

    override fun render(view: Tree<T, *>, canvas: Canvas) {
        canvas.rect(view.bounds.atOrigin, CanvasBrush(Size(20, 40)) {
            rect(Rectangle(       20, 20), ColorBrush(lightgray.lighter()))
            rect(Rectangle(0, 20, 20, 20), ColorBrush(lightgray          ))
        })
    }

    override fun install(view: Tree<T, *>) {
        view.keyChanged += this
    }

    override fun uninstall(view: Tree<T, *>) {
        view.keyChanged -= this
    }

    override fun keyPressed(event: KeyEvent) {
        (event.source as Tree<*, *>).let { tree ->
            when (event.code) {
                VK_UP, VK_DOWN -> {
                    when (Shift) {
                        in event -> {
                            tree.selectionAnchor?.let { tree.rowFromPath(it) }?.let { anchor ->
                                tree.lastSelection?.let { tree.rowFromPath(it) }?.let { if (event.code == VK_UP) it - 1 else it + 1 }?.takeUnless { it < 0 || it > tree.numRows - 1 }?.let { current ->
                                    when {
                                        current < anchor  -> tree.setSelection((current .. anchor ).reversed().toSet())
                                        anchor  < current -> tree.setSelection((anchor  .. current).           toSet())
                                        else              -> tree.setSelection(setOf(current))
                                    }
                                }
                            }
                        }
                        else -> tree.lastSelection?.let { tree.rowFromPath(it) }?.let { if (event.code == VK_UP) it - 1 else it + 1 }?.takeUnless { it < 0 || it > tree.numRows - 1 }?.let { tree.setSelection(setOf(it)) }
                    }?.let { Unit } ?: Unit
                }
                VK_LEFT        -> tree.selection.firstOrNull()?.also { if (tree.expanded(it)) { tree.collapse(it) } else it.parent?.let { tree.setSelection(setOf(it)) } }?.let { Unit } ?: Unit
                VK_RIGHT       -> tree.selection.firstOrNull()?.also { tree.expand(it) }?.let { Unit } ?: Unit
                VK_A           -> {
                    if (Ctrl in event || Meta in event) {
                        tree.selectAll()
                    }
                }
            }
        }
    }
}

class BasicMutableTreeBehavior<T>(generator: RowGenerator<T>): BasicTreeBehavior<T>(generator) {
    constructor(labelFactory: LabelFactory, focusManager: FocusManager?): this(BasicMutableTreeRowGenerator(focusManager, LabelContentGenerator(labelFactory)))

    override fun keyPressed(event: KeyEvent) {
        (event.source as MutableTree<*, *>).let { tree ->
            when (event.code) {
                VK_DELETE, VK_BACKSPACE -> tree.selection.forEach { tree.removeAt(it) }
                else                    -> super.keyPressed(event)
            }
        }
    }
}

@Suppress("PrivatePropertyName", "unused")
open class TextEditOperation<T>(
        private val focusManager   : FocusManager?,
        private val encoder        : Encoder<T, String>,
        private val display        : Display,
        private val positionMonitor: RelativePositionMonitor,
        private val tree           : MutableTree<T, *>,
                    node           : T,
        private val path           : Path<Int>,
                    contentBounds  : Rectangle,
        private val current        : View): TextField(), EditOperation<T> {

    private val positionChanged = { _:View, old: Rectangle, new: Rectangle ->
        bounds = Rectangle(bounds.position + new.position - old.position, bounds.size)
    }

    init {
        text                = encoder.encode(node) ?: ""
        fitText             = setOf(TextFit.Width)
        bounds              = contentBounds.at(contentBounds.position + tree.toAbsolute(Point.Origin))
        borderVisible       = false
        backgroundColor     = current.backgroundColor
        horizontalAlignment = Left

        styleChanged += { rerender() }

        focusChanged += { _,_,_ ->
            if (!hasFocus) {
                tree.cancelEditing()
            }
        }

        positionMonitor[current] += positionChanged

        keyChanged += object: KeyListener {
            override fun keyReleased(event: KeyEvent) {
                when (event.code) {
                    VK_RETURN -> { tree.completeEditing(); focusManager?.requestFocus(tree) }
                    VK_ESCAPE -> { tree.cancelEditing  (); focusManager?.requestFocus(tree) }
                }
            }
        }
    }

    override fun addedToDisplay() {
        focusManager?.requestFocus(this)
        selectAll()
    }

    override fun invoke(): View? = null.also { display.children += this }

    override fun complete() = encoder.decode(text).also {
        cancel()
    }

    override fun cancel() {
        display.children -= this
        positionMonitor[current] -= positionChanged
    }
}

class TreeTextEditor<T>(
        private val focusManager   : FocusManager?,
        private val encoder        : Encoder<T, String>,
        private val display        : Display,
        private val positionMonitor: RelativePositionMonitor): TreeEditor<T> {
    override fun edit(tree: MutableTree<T, *>, node: T, path: Path<Int>, contentBounds: Rectangle, current: View): EditOperation<T> = TextEditOperation(focusManager, encoder, display, positionMonitor, tree, node, path, contentBounds, current)

    companion object {
        operator fun invoke(focusManager: FocusManager?, display: Display, positionMonitor: RelativePositionMonitor): TreeTextEditor<String> {
            return TreeTextEditor(focusManager, object: Encoder<String, String> {
                override fun decode(b: String) = b
                override fun encode(a: String) = a
            }, display, positionMonitor)
        }
    }
}