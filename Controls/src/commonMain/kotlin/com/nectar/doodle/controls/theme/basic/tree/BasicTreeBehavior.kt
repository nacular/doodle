package com.nectar.doodle.controls.theme.basic.tree

import com.nectar.doodle.controls.EditOperation
import com.nectar.doodle.controls.text.Label
import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.controls.text.TextFit.Height
import com.nectar.doodle.controls.text.TextFit.Width
import com.nectar.doodle.controls.theme.TreeBehavior
import com.nectar.doodle.controls.theme.TreeBehavior.RowGenerator
import com.nectar.doodle.controls.theme.TreeBehavior.RowPositioner
import com.nectar.doodle.controls.theme.basic.ContentGenerator
import com.nectar.doodle.controls.theme.basic.SelectableTreeKeyHandler
import com.nectar.doodle.controls.theme.basic.SimpleTreeRowIcon
import com.nectar.doodle.controls.theme.basic.TreeRow
import com.nectar.doodle.controls.tree.MutableTree
import com.nectar.doodle.controls.tree.Tree
import com.nectar.doodle.controls.tree.TreeEditor
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.black
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.drawing.horizontalStripedBrush
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyEvent.Companion.VK_BACKSPACE
import com.nectar.doodle.event.KeyEvent.Companion.VK_DELETE
import com.nectar.doodle.event.KeyEvent.Companion.VK_ESCAPE
import com.nectar.doodle.event.KeyEvent.Companion.VK_RETURN
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
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
            val indent   = height * (1 + depth)
            val maxWidth = tree.width - tree.insets.run { left + right } - indent

            Rectangle(tree.insets.left + indent, tree.insets.top + index * height, maxWidth, height)
        }
    }

    override fun row(of: Tree<T, *>, atY: Double): Int {
        return max(0, ((atY - of.insets.top) / height).toInt())
    }
}

private class LabelContentGenerator<T>(private val labelFactory: LabelFactory): ContentGenerator<T> {
    override fun invoke(item: T, index: Int, previous: View?) = when (previous) {
        is Label -> { previous.text = item.toString(); previous }
        else     -> labelFactory(item.toString()).apply {
            fitText             = setOf(Width, Height)
            horizontalAlignment = Left
        }
    }
}

open class BasicTreeRowGenerator<T>(private val focusManager         : FocusManager?,
                                    private val contentGenerator     : ContentGenerator<T>,
                                    private val selectionColor       : Color? = green.lighter(),
                                    private val selectionBlurredColor: Color? = lightgray,
                                    private val iconColor            : Color  = black): RowGenerator<T> {
    override fun invoke(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View?): View = when (current) {
        is TreeRow<*> -> (current as TreeRow<T>).apply { update(tree, node, path, index) }
        else          -> TreeRow(tree, node, path, index, contentGenerator, selectionColor = selectionColor, selectionBluredColor = selectionBlurredColor, iconFactory = { SimpleTreeRowIcon(iconColor) }).apply {
            mouseChanged += object: MouseListener {
                override fun mouseReleased(event: MouseEvent) {
                    focusManager?.requestFocus(tree)
                }
            }
        }
    }
}

class BasicMutableTreeRowGenerator<T>(focusManager         : FocusManager?,
                                      contentGenerator     : ContentGenerator<T>,
                                      selectionColor       : Color? = green.lighter(),
                                      selectionBlurredColor: Color? = lightgray,
                                      iconColor            : Color = black): BasicTreeRowGenerator<T>(focusManager, contentGenerator, selectionColor, selectionBlurredColor, iconColor) {
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

open class BasicTreeBehavior<T>(override val generator   : RowGenerator<T>,
                                             evenRowColor: Color? = white,
                                             oddRowColor : Color? = lightgray.lighter().lighter(),
                                             rowHeight   : Double = 20.0): TreeBehavior<T>, KeyListener, SelectableTreeKeyHandler {
    constructor(labelFactory         : LabelFactory,
                evenRowColor         : Color? = white,
                oddRowColor          : Color? = lightgray.lighter().lighter(),
                selectionColor       : Color? = green.lighter(),
                selectionBlurredColor: Color? = lightgray,
                iconColor            : Color  = black,
                focusManager         : FocusManager?): this(BasicTreeRowGenerator(focusManager, LabelContentGenerator(labelFactory), selectionColor, selectionBlurredColor, iconColor), evenRowColor, oddRowColor)

    private val patternBrush = horizontalStripedBrush(rowHeight, evenRowColor, oddRowColor)

    override val positioner: RowPositioner<T> = BasicTreeRowPositioner(rowHeight)

    override fun render(view: Tree<T, *>, canvas: Canvas) {
        canvas.rect(view.bounds.atOrigin, patternBrush)
    }

    override fun install(view: Tree<T, *>) {
        view.keyChanged += this

        view.rerender()
    }

    override fun uninstall(view: Tree<T, *>) {
        view.keyChanged -= this
    }

    override fun keyPressed(event: KeyEvent) {
        super<SelectableTreeKeyHandler>.keyPressed(event)
    }
}

class BasicMutableTreeBehavior<T>(generator   : RowGenerator<T>,
                                  evenRowColor: Color? = white,
                                  oddRowColor : Color? = lightgray.lighter().lighter(),
                                  rowHeight   : Double = 20.0): BasicTreeBehavior<T>(generator, evenRowColor, oddRowColor, rowHeight) {
    constructor(labelFactory         : LabelFactory,
                evenRowColor         : Color? = white,
                oddRowColor          : Color? = lightgray.lighter().lighter(),
                selectionColor       : Color? = green.lighter(),
                selectionBlurredColor: Color? = lightgray,
                iconColor            : Color  = black,
                focusManager         : FocusManager?): this(BasicMutableTreeRowGenerator(focusManager, LabelContentGenerator(labelFactory), selectionColor, selectionBlurredColor, iconColor), evenRowColor, oddRowColor)

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
        fitText             = setOf(Width)
        bounds              = contentBounds.at(contentBounds.position + tree.toAbsolute(Point.Origin))
        borderVisible       = false
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

        backgroundColor = current.backgroundColor
        selectAll()
    }

    override fun invoke(): View? = null.also { display.children += this }

    override fun complete() = encoder.decode(text).also {
        cancel()
    }

    override fun cancel() {
        display.children         -= this
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