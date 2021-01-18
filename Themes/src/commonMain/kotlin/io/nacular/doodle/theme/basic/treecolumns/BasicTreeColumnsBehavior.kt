package io.nacular.doodle.theme.basic.treecolumns

import io.nacular.doodle.controls.TextVisualizer
import io.nacular.doodle.controls.toString
import io.nacular.doodle.controls.treecolumns.TreeColumns
import io.nacular.doodle.controls.treecolumns.TreeColumnsBehavior
import io.nacular.doodle.controls.treecolumns.TreeColumnsBehavior.CellGenerator
import io.nacular.doodle.core.ContentDirection.LeftRight
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Green
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.ColorFill
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.KeyText
import io.nacular.doodle.event.KeyText.Companion.ArrowDown
import io.nacular.doodle.event.KeyText.Companion.ArrowLeft
import io.nacular.doodle.event.KeyText.Companion.ArrowRight
import io.nacular.doodle.event.KeyText.Companion.ArrowUp
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.system.SystemInputEvent.Modifier.Ctrl
import io.nacular.doodle.system.SystemInputEvent.Modifier.Meta
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift
import io.nacular.doodle.theme.basic.ListPositioner
import io.nacular.doodle.utils.Path

/**
 * Created by Nicholas Eddy on 7/25/20.
 */


open class BasicTreeColumnRowGenerator<T>(
        private val focusManager         : FocusManager?,
        private val selectionColor       : Color? = Green.lighter(),
        private val selectionBlurredColor: Color? = Lightgray,
        private val iconFactory          : () -> TreeColumnRowIcon = { SimpleTreeColumnRowIcon() }): CellGenerator<T> {
    override fun invoke(treeColumns: TreeColumns<T, *>, node: T, path: Path<Int>, row: Int, current: View?): View = when (current) {
        is TreeColumnRow<*> -> (current as TreeColumnRow<T>).apply { update(treeColumns, node, path, row) }
        else                -> TreeColumnRow(treeColumns, node, path, row, treeColumns.itemVisualizer ?: toString(TextVisualizer()), selectionColor = selectionColor, selectionBlurredColor = selectionBlurredColor, iconFactory = iconFactory).apply {
            pointerChanged += object: PointerListener {
                override fun released(event: PointerEvent) {
                    focusManager?.requestFocus(treeColumns)
                }
            }
        }
    }
}

class BasicTreeColumnsBehavior<T>(
        override val generator           : CellGenerator<T>,
        private  val columnSeparatorColor: Color? = Lightgray.lighter().lighter(),
        private  val backgroundColor     : Color? = Lightgray,
        rowHeight                        : Double = 20.0): TreeColumnsBehavior<T>(), KeyListener {

    constructor(
            focusManager         : FocusManager?,
            rowHeight            : Double = 20.0,
            columnSeparatorColor : Color? = Lightgray.lighter().lighter(),
            selectionColor       : Color? = Green.lighter(),
            selectionBlurredColor: Color? = Lightgray,
            backgroundColor      : Color? = Lightgray,
            iconFactory          : () -> TreeColumnRowIcon = { SimpleTreeColumnRowIcon() }
    ): this(BasicTreeColumnRowGenerator(focusManager, selectionColor, selectionBlurredColor, iconFactory), columnSeparatorColor, backgroundColor, rowHeight)

    private class BasicTreeColumnPositioner<T>(height: Double, spacing: Double = 0.0): ListPositioner(height, spacing), RowPositioner<T> {
        override fun rowBounds(treeColumns: TreeColumns<T, *>, columnWidth: Double, path: Path<Int>, row: T, index: Int, current: View?) = super.rowBounds(columnWidth, Insets(right = VERTICAL_LINE_THICKNESS), index, current)

        override fun row(of: TreeColumns<T, *>, path: Path<Int>, y: Double) = super.rowFor(Insets.None, y)

        override fun totalRowHeight(of: TreeColumns<T, *>, path: Path<Int>) = super.totalHeight(of.numChildren(path), Insets.None)
    }

    override val positioner: RowPositioner<T> = BasicTreeColumnPositioner(rowHeight)

    override fun install(view: TreeColumns<T, *>) {
        view.keyChanged += this
    }

    override fun uninstall(view: TreeColumns<T, *>) {
        view.keyChanged -= this
    }

    override fun pressed(event: KeyEvent) {
        (event.source as? TreeColumns<*,*>)?.let { tree ->
            val (expandKey, collapseKey) = when (tree.contentDirection) {
                LeftRight -> ArrowRight to ArrowLeft
                else      -> ArrowLeft to ArrowRight
            }

            when (event.key) {
                ArrowUp, ArrowDown -> {
                    when (Shift) {
                        in event -> {
                            tree.selectionAnchor?.let { anchor ->
                                tree.lastSelection?.let { if (event.key == ArrowUp) tree.previous(it) else tree.next(it) }?.let { current ->
//                                    val currentRow = tree.rowFromPath(current)
//                                    val anchorRow  = tree.rowFromPath(anchor )
//
//                                    if (currentRow != null && anchorRow != null) {
//                                        when {
//                                            currentRow < anchorRow  -> tree.setSelection((currentRow..anchorRow).reversed().toSet())
//                                            anchorRow  < currentRow -> tree.setSelection((anchorRow..currentRow).toSet())
//                                            else                    -> tree.setSelection(setOf(currentRow))
//                                        }
//                                    }
                                }
                            }
                        }
                        else -> tree.lastSelection?.let { if (event.key == ArrowUp) tree.previous(it) else tree.next(it) }?.let { tree.setSelection(setOf(it)) }
                    }
                }
                collapseKey        -> { tree.selection.firstOrNull()?.parent?.takeUnless { it.depth == 0 }?.let { tree.setSelection(setOf(it)) } }
                expandKey          -> { tree.selection.firstOrNull()?.takeUnless { tree.isLeaf(it) }?.let { tree.setSelection(setOf(it + 0)) } }
                KeyText("a"), KeyText("A")         -> {
                    if (Ctrl in event || Meta in event) {
                        tree.selectAll()
                    }
                    Unit
                }
                else -> {}
            }
        }
    }

    override fun renderColumnBody(treeColumns: TreeColumns<T, *>, path: Path<Int>, canvas: Canvas) {
        backgroundColor?.let { canvas.rect(Rectangle(size = canvas.size), ColorFill(it)) }

        val x = canvas.size.width - VERTICAL_LINE_THICKNESS / 2

        columnSeparatorColor?.let {
            canvas.line(Point(x, 0.0), Point(x, canvas.size.height), Stroke(it, VERTICAL_LINE_THICKNESS))
        }

//        patternFill?.let { canvas.rect(Rectangle(size = canvas.size), it) }
    }

    companion object {
        private const val VERTICAL_LINE_THICKNESS = 1.0
    }
}