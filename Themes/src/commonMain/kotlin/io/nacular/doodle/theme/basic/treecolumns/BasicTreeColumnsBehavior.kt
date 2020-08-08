package io.nacular.doodle.theme.basic.treecolumns

import io.nacular.doodle.controls.TextItemVisualizer
import io.nacular.doodle.controls.ignoreIndex
import io.nacular.doodle.controls.treecolumns.TreeColumns
import io.nacular.doodle.controls.treecolumns.TreeColumnsBehavior
import io.nacular.doodle.controls.treecolumns.TreeColumnsBehavior.CellGenerator
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Green
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.theme.basic.ListPositioner
import io.nacular.doodle.theme.basic.SelectableTreeKeyHandler
import io.nacular.doodle.utils.Path

/**
 * Created by Nicholas Eddy on 7/25/20.
 */


open class BasicTreeColumnRowGenerator<T>(
        private val focusManager         : FocusManager?,
        private val textMetrics          : TextMetrics,
        private val selectionColor       : Color? = Green.lighter(),
        private val selectionBlurredColor: Color? = Lightgray,
        private val iconFactory          : () -> TreeColumnRowIcon = { SimpleTreeColumnRowIcon() }): CellGenerator<T> {
    override fun invoke(treeColumns: TreeColumns<T, *>, node: T, path: Path<Int>, row: Int, current: View?): View = when (current) {
        is TreeColumnRow<*> -> (current as TreeColumnRow<T>).apply { update(treeColumns, node, path, row) }
        else                -> TreeColumnRow(treeColumns, node, path, row, treeColumns.itemVisualizer ?: ignoreIndex(io.nacular.doodle.controls.toString(TextItemVisualizer(textMetrics))), selectionColor = selectionColor, selectionBlurredColor = selectionBlurredColor, iconFactory = iconFactory).apply {
            pointerChanged += object: PointerListener {
                override fun released(event: PointerEvent) {
                    focusManager?.requestFocus(treeColumns)
                }
            }
        }
    }
}

class BasicTreeColumnsBehavior<T>(
        override val generator   : CellGenerator<T>,
        evenRowColor: Color? = Color.White,
        oddRowColor : Color? = Lightgray.lighter().lighter(),
        rowHeight   : Double = 20.0): TreeColumnsBehavior<T>(), /*KeyListener,*/ SelectableTreeKeyHandler {

    constructor(
            focusManager         : FocusManager?,
            textMetrics          : TextMetrics,
            rowHeight            : Double = 20.0,
            evenRowColor         : Color? = Color.White,
            oddRowColor          : Color? = Lightgray.lighter().lighter(),
            selectionColor       : Color? = Green.lighter(),
            selectionBlurredColor: Color? = Lightgray,
            iconFactory          : () -> TreeColumnRowIcon = { SimpleTreeColumnRowIcon() }
    ): this(BasicTreeColumnRowGenerator(focusManager, textMetrics, selectionColor, selectionBlurredColor, iconFactory), evenRowColor, oddRowColor, rowHeight)

    private class BasicTreeColumnPositioner<T>(height: Double, spacing: Double = 0.0): ListPositioner(height, spacing), RowPositioner<T> {
        override fun rowBounds(treeColumns: TreeColumns<T, *>, columnWidth: Double, path: Path<Int>, row: T, index: Int, current: View?) = super.rowBounds(columnWidth, Insets(right = VERTICAL_LINE_THICKNESS), index, current)

        override fun row(of: TreeColumns<T, *>, path: Path<Int>, y: Double) = super.rowFor(Insets.None, y)

        override fun totalRowHeight(of: TreeColumns<T, *>, path: Path<Int>) = super.totalHeight(of.numChildren(path), Insets.None)
    }

//    private val patternFill = if (evenRowColor != null || oddRowColor != null) horizontalStripedFill(rowHeight, evenRowColor, oddRowColor) else null

    override val positioner: RowPositioner<T> = BasicTreeColumnPositioner(rowHeight)

    override fun install(view: TreeColumns<T, *>) {
//        view.keyChanged += this

//        view.rerender()
    }

//    override fun uninstall(view: TreeColumns<T, *>) {
//        view.keyChanged -= this
//    }
//
//    override fun keyPressed(event: KeyEvent) {
//        super<SelectableTreeKeyHandler>.keyPressed(event)
//    }

    override fun renderColumnBody(treeColumns: TreeColumns<T, *>, path: Path<Int>, canvas: Canvas) {
        val x = canvas.size.width - VERTICAL_LINE_THICKNESS / 2

        canvas.line(Point(x, 0.0), Point(x, canvas.size.height), Stroke(Black, VERTICAL_LINE_THICKNESS))

//        patternFill?.let { canvas.rect(Rectangle(size = canvas.size), it) }
    }

    companion object {
        private const val VERTICAL_LINE_THICKNESS = 1.0
    }
}