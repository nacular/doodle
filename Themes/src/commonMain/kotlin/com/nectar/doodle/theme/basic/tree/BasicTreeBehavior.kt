package com.nectar.doodle.theme.basic.tree

import com.nectar.doodle.controls.EditOperation
import com.nectar.doodle.controls.text.Label
import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.controls.text.TextFit.Height
import com.nectar.doodle.controls.text.TextFit.Width
import com.nectar.doodle.controls.theme.TreeBehavior
import com.nectar.doodle.controls.theme.TreeBehavior.RowGenerator
import com.nectar.doodle.controls.theme.TreeBehavior.RowPositioner
import com.nectar.doodle.controls.tree.MutableTree
import com.nectar.doodle.controls.tree.Tree
import com.nectar.doodle.controls.tree.TreeEditor
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.Black
import com.nectar.doodle.drawing.Color.Companion.Green
import com.nectar.doodle.drawing.Color.Companion.Lightgray
import com.nectar.doodle.drawing.Color.Companion.White
import com.nectar.doodle.drawing.horizontalStripedBrush
import com.nectar.doodle.drawing.lighter
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.KeyText.Companion.Backspace
import com.nectar.doodle.event.KeyText.Companion.Delete
import com.nectar.doodle.event.KeyText.Companion.Enter
import com.nectar.doodle.event.KeyText.Companion.Escape
import com.nectar.doodle.event.PointerEvent
import com.nectar.doodle.event.PointerListener
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.theme.basic.ContentGenerator
import com.nectar.doodle.theme.basic.SelectableTreeKeyHandler
import com.nectar.doodle.theme.basic.SimpleTreeRowIcon
import com.nectar.doodle.theme.basic.TreeRow
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
    override fun invoke(item: T, index: Int, previous: View?, isSelected: () -> Boolean) = when (previous) {
        is Label -> { previous.text = item.toString(); previous }
        else     -> labelFactory(item.toString()).apply {
            fitText             = setOf(Width, Height)
            horizontalAlignment = Left
        }
    }
}

open class BasicTreeRowGenerator<T>(private val focusManager         : FocusManager?,
                                    private val contentGenerator     : ContentGenerator<T>,
                                    private val selectionColor       : Color? = Green.lighter(),
                                    private val selectionBlurredColor: Color? = Lightgray,
                                    private val iconColor            : Color  = Black): RowGenerator<T> {
    override fun invoke(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View?): View = when (current) {
        is TreeRow<*> -> (current as TreeRow<T>).apply { update(tree, node, path, index) }
        else          -> TreeRow(tree, node, path, index, contentGenerator, selectionColor = selectionColor, selectionBluredColor = selectionBlurredColor, iconFactory = { SimpleTreeRowIcon(iconColor) }).apply {
            pointerChanged += object: PointerListener {
                override fun released(event: PointerEvent) {
                    focusManager?.requestFocus(tree)
                }
            }
        }
    }
}

class BasicMutableTreeRowGenerator<T>(focusManager         : FocusManager?,
                                      contentGenerator     : ContentGenerator<T>,
                                      selectionColor       : Color? = Green.lighter(),
                                      selectionBlurredColor: Color? = Lightgray,
                                      iconColor            : Color = Black): BasicTreeRowGenerator<T>(focusManager, contentGenerator, selectionColor, selectionBlurredColor, iconColor) {
    override fun invoke(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View?) = super.invoke(tree, node, path, index, current).also {
        if (current !is TreeRow<*>) {
            val result = it as TreeRow<*>

            it.pointerChanged += object: PointerListener {
                override fun released(event: PointerEvent) {
                    if (event.clickCount == 2) {
                        (tree as MutableTree).startEditing(result.path)
                    }
                }
            }
        }
    }
}

open class BasicTreeBehavior<T>(override val generator   : RowGenerator<T>,
                                             evenRowColor: Color? = White,
                                             oddRowColor : Color? = Lightgray.lighter().lighter(),
                                             rowHeight   : Double = 20.0): TreeBehavior<T>, KeyListener, SelectableTreeKeyHandler {
    constructor(labelFactory         : LabelFactory,
                evenRowColor         : Color? = White,
                oddRowColor          : Color? = Lightgray.lighter().lighter(),
                selectionColor       : Color? = Green.lighter(),
                selectionBlurredColor: Color? = Lightgray,
                iconColor            : Color  = Black,
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
                                  evenRowColor: Color? = White,
                                  oddRowColor : Color? = Lightgray.lighter().lighter(),
                                  rowHeight   : Double = 20.0): BasicTreeBehavior<T>(generator, evenRowColor, oddRowColor, rowHeight) {
    constructor(labelFactory         : LabelFactory,
                evenRowColor         : Color? = White,
                oddRowColor          : Color? = Lightgray.lighter().lighter(),
                selectionColor       : Color? = Green.lighter(),
                selectionBlurredColor: Color? = Lightgray,
                iconColor            : Color  = Black,
                focusManager         : FocusManager?): this(BasicMutableTreeRowGenerator(focusManager, LabelContentGenerator(labelFactory), selectionColor, selectionBlurredColor, iconColor), evenRowColor, oddRowColor)

    override fun keyPressed(event: KeyEvent) {
        (event.source as MutableTree<*, *>).let { tree ->
            when (event.key) {
                Delete, Backspace -> tree.selection.forEach { tree.removeAt(it) }
                else              -> super.keyPressed(event)
            }
        }
    }
}

@Suppress("PrivatePropertyName", "unused")
open class TextEditOperation<T>(
        private val focusManager   : FocusManager?,
        private val encoder        : Encoder<T, String>,
                    display        : Display,
        private val positionMonitor: RelativePositionMonitor,
        private val tree           : MutableTree<T, *>,
                    node           : T,
        private val path           : Path<Int>,
                    contentBounds  : Rectangle,
        private val current        : View): TextField(), EditOperation<T> {

    // View has an internal display property so have to create new one
    private val _display = display

    private val positionChanged = { _:View, old: Rectangle, new: Rectangle ->
        bounds = Rectangle(bounds.position + new.position - old.position, bounds.size)
    }

    init {
        text                = encoder.encode(node) ?: ""
        fitText             = setOf(Width) // TODO: Relax this if text exceeding tree row width
        bounds              = contentBounds.at(contentBounds.position + tree.toAbsolute(Point.Origin))
        borderVisible       = false
        horizontalAlignment = Left

        styleChanged += { rerender() }

        focusChanged += { _,_,_ ->
            if (!hasFocus) {
                tree.cancelEditing()
            }
        }

        keyChanged += object: KeyListener {
            override fun keyReleased(event: KeyEvent) {
                when (event.key) {
                    Enter  -> { tree.completeEditing(); focusManager?.requestFocus(tree) }
                    Escape -> { tree.cancelEditing  (); focusManager?.requestFocus(tree) }
                }
            }
        }
    }

    override fun addedToDisplay() {
        focusManager?.requestFocus(this)

        backgroundColor = current.backgroundColor
        selectAll()
    }

    override fun invoke(): View? = null.also {
        when (current) {
            is TreeRow<*> -> {
                bounds = current.content.bounds
                current.content = this
            }
            else          -> {
                positionMonitor[current] += positionChanged

                _display.children += this
            }
        }
    }

    override fun complete() = encoder.decode(text).also {
        cancel()
    }

    override fun cancel() {
        _display.children        -= this
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