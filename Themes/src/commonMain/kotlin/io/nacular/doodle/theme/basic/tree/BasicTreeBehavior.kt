package io.nacular.doodle.theme.basic.tree

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.StringVisualizer
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.theme.TreeBehavior
import io.nacular.doodle.controls.theme.TreeBehavior.RowGenerator
import io.nacular.doodle.controls.theme.TreeBehavior.RowPositioner
import io.nacular.doodle.controls.toString
import io.nacular.doodle.controls.tree.MutableTree
import io.nacular.doodle.controls.tree.Tree
import io.nacular.doodle.controls.tree.TreeEditor
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Green
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.horizontalStripedPaint
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.KeyText.Companion.Backspace
import io.nacular.doodle.event.KeyText.Companion.Delete
import io.nacular.doodle.event.KeyText.Companion.Enter
import io.nacular.doodle.event.KeyText.Companion.Escape
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.theme.basic.SelectableTreeKeyHandler
import io.nacular.doodle.theme.basic.SimpleTreeRowIcon
import io.nacular.doodle.theme.basic.TreeRow
import io.nacular.doodle.theme.basic.TreeRowIcon
import io.nacular.doodle.utils.Dimension.Width
import io.nacular.doodle.utils.Encoder
import io.nacular.doodle.utils.HorizontalAlignment.Left
import io.nacular.doodle.utils.PassThroughEncoder
import io.nacular.doodle.utils.Path
import io.nacular.doodle.utils.RelativePositionMonitor
import io.nacular.doodle.utils.SetObserver
import kotlin.math.max

private class BasicTreeRowPositioner<T>(private val height: Double): RowPositioner<T>() {
    override fun rowBounds(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int) = Rectangle(
        tree.insets.left,
        tree.insets.top + index * height,
        max(0.0, tree.width - tree.insets.run { left + right }),
        height
    )

    override fun contentBounds(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int): Rectangle {
        // FIXME: Centralize
        val depth    = (path.depth - if (!tree.rootVisible) 1 else 0)
        val indent   = height * (1 + depth)
        val maxWidth = max(0.0, tree.width - tree.insets.run { left + right } - indent)

        return Rectangle(tree.insets.left + indent, tree.insets.top + index * height, maxWidth, height)
    }

    override fun row(of: Tree<T, *>, at: Point): Int {
        return max(0, ((at.y - of.insets.top) / height).toInt())
    }

    override fun minimumSize(of: Tree<T, *>, below: Path<Int>) = Size(0.0, of.rowsBelow(below) * height + of.insets.run { top + bottom })
}

public open class BasicTreeRowGenerator<T>(
        private val focusManager         : FocusManager?,
        private val selectionColor       : Color? = Green.lighter(),
        private val selectionBlurredColor: Color? = Lightgray,
        private val iconFactory          : () -> TreeRowIcon = { SimpleTreeRowIcon(Black) }
): RowGenerator<T> {
    @Suppress("UNCHECKED_CAST")
    override fun invoke(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View?): View = when (current) {
        is TreeRow<*> -> (current as TreeRow<T>).apply { update(tree, node, path, index) }
        else          -> TreeRow(tree, node, path, index, tree.itemVisualizer ?: toString(StringVisualizer()), selectionColor = selectionColor, selectionBlurredColor = selectionBlurredColor, iconFactory = iconFactory).apply {
            pointerChanged += object: PointerListener {
                override fun released(event: PointerEvent) {
                    focusManager?.requestFocus(tree)
                }
            }
        }
    }.apply {
        tree.cellAlignment?.let { positioner = it }
    }
}

public class BasicMutableTreeRowGenerator<T>(
        focusManager         : FocusManager?,
        selectionColor       : Color? = Green.lighter(),
        selectionBlurredColor: Color? = Lightgray,
        iconFactory          : () -> TreeRowIcon = { SimpleTreeRowIcon() }
): BasicTreeRowGenerator<T>(focusManager, selectionColor, selectionBlurredColor, iconFactory) {
    override fun invoke(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View?): View = super.invoke(tree, node, path, index, current).also {
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

public open class BasicTreeBehavior<T>(override val generator   : RowGenerator<T>,
                                                    evenRowColor: Color? = White,
                                                    oddRowColor : Color? = Lightgray.lighter().lighter(),
                                                    rowHeight   : Double = 20.0): TreeBehavior<T>, KeyListener, SelectableTreeKeyHandler {
    public constructor(focusManager  : FocusManager?,
        rowHeight            : Double = 20.0,
        evenRowColor         : Color? = White,
        oddRowColor          : Color? = Lightgray.lighter().lighter(),
        selectionColor       : Color? = Green.lighter(),
        selectionBlurredColor: Color? = Lightgray,
        iconFactory          : () -> TreeRowIcon = { SimpleTreeRowIcon() }
    ): this(BasicTreeRowGenerator(focusManager, selectionColor, selectionBlurredColor, iconFactory), evenRowColor, oddRowColor, rowHeight)

    private val patternFill = if (evenRowColor != null || oddRowColor != null) horizontalStripedPaint(rowHeight, evenRowColor, oddRowColor) else null

    private val selectionListener: SetObserver<Tree<T, *>, Path<Int>> = { tree, _, _ ->
        tree.scrollToSelection()
    }

    override val positioner: RowPositioner<T> = BasicTreeRowPositioner(rowHeight)

    override fun render(view: Tree<T, *>, canvas: Canvas) {
        patternFill?.let { canvas.rect(view.bounds.atOrigin, it) }
    }

    override fun install(view: Tree<T, *>) {
        view.keyChanged       += this
        view.selectionChanged += selectionListener
    }

    override fun uninstall(view: Tree<T, *>) {
        view.keyChanged       -= this
        view.selectionChanged -= selectionListener
    }

    override fun pressed(event: KeyEvent) {
        super<SelectableTreeKeyHandler>.pressed(event)
    }
}

public class BasicMutableTreeBehavior<T>(generator   : RowGenerator<T>,
                                         evenRowColor: Color? = White,
                                         oddRowColor : Color? = Lightgray.lighter().lighter(),
                                         rowHeight   : Double = 20.0): BasicTreeBehavior<T>(generator, evenRowColor, oddRowColor, rowHeight) {
    public constructor(focusManager         : FocusManager?,
                rowHeight            : Double = 20.0,
                evenRowColor         : Color? = White,
                oddRowColor          : Color? = Lightgray.lighter().lighter(),
                selectionColor       : Color? = Green.lighter(),
                selectionBlurredColor: Color? = Lightgray,
                iconFactory          : () -> TreeRowIcon = { SimpleTreeRowIcon(Black) }): this(
            BasicMutableTreeRowGenerator(
                    focusManager,
                    selectionColor,
                    selectionBlurredColor,
                    iconFactory
            ),
            evenRowColor,
            oddRowColor,
            rowHeight
    )

    override fun pressed(event: KeyEvent) {
        (event.source as MutableTree<*, *>).let { tree ->
            when (event.key) {
                Delete, Backspace -> tree.selection.forEach { tree.removeAt(it) }
                else              -> super.pressed(event)
            }
        }
    }
}

@Suppress("PrivatePropertyName", "unused", "LeakingThis")
public open class TextEditOperation<T>(
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
        suggestBounds(Rectangle(bounds.position + new.position - old.position, bounds.size))
    }

    init {
        text                = encoder.encode(node).getOrDefault("")
        fitText             = setOf(Width) // TODO: Relax this if text exceeding tree row width
        borderVisible       = false
        foregroundColor     = current.foregroundColor
        backgroundColor     = Transparent
        horizontalAlignment = Left

        suggestBounds(contentBounds.at(contentBounds.position + tree.toAbsolute(Point.Origin)))

        styleChanged += { rerender() }

        focusChanged += { _,_,_ ->
            if (!hasFocus) {
//                tree.cancelEditing()
            }
        }

        keyChanged += object: KeyListener {
            override fun released(event: KeyEvent) {
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
            is TreeRow<*> -> current.update(this, tree)
            else          -> {
                positionMonitor[current] += positionChanged

                _display.children += this
            }
        }
    }

    override fun complete(): Result<T> = encoder.decode(text).also {
        cancel()
    }

    override fun cancel() {
        _display.children        -= this
        positionMonitor[current] -= positionChanged
    }
}

public class TreeTextEditor<T>(
        private val focusManager   : FocusManager?,
        private val encoder        : Encoder<T, String>,
        private val display        : Display,
        private val positionMonitor: RelativePositionMonitor): TreeEditor<T> {
    override fun edit(tree: MutableTree<T, *>, node: T, path: Path<Int>, contentBounds: Rectangle, current: View): EditOperation<T> = TextEditOperation(focusManager, encoder, display, positionMonitor, tree, node, path, contentBounds, current)

    public companion object {
        public operator fun invoke(focusManager: FocusManager?, display: Display, positionMonitor: RelativePositionMonitor): TreeTextEditor<String> = TreeTextEditor(focusManager, PassThroughEncoder(), display, positionMonitor)
    }
}