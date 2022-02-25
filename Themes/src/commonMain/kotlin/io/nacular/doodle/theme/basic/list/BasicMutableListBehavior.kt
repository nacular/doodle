package io.nacular.doodle.theme.basic.list

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.list.List
import io.nacular.doodle.controls.list.ListBehavior.ItemGenerator
import io.nacular.doodle.controls.list.ListBehavior.ItemPositioner
import io.nacular.doodle.controls.list.ListEditor
import io.nacular.doodle.controls.list.MutableList
import io.nacular.doodle.utils.Dimension.Width
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.PatternPaint
import io.nacular.doodle.drawing.horizontalStripedPaint
import io.nacular.doodle.drawing.verticalStripedPaint
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyText.Companion.Backspace
import io.nacular.doodle.event.KeyText.Companion.Delete
import io.nacular.doodle.event.PointerListener.Companion.released
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.theme.basic.GenericTextEditOperation
import io.nacular.doodle.theme.basic.ListItem
import io.nacular.doodle.utils.Encoder
import io.nacular.doodle.utils.HorizontalAlignment.Left


public open class BasicMutableItemGenerator<T>(selectionColor: Color? = null, selectionBlurredColor: Color? = null): BasicItemGenerator<T>(selectionColor, selectionBlurredColor) {
    override fun invoke(list: List<T, *>, item: T, index: Int, current: View?): View = super.invoke(list, item, index, current).also {
        if (current !is ListItem<*>) {
            val result = it as ListItem<*>

            it.pointerFilter += released { event ->
                if (list.selected(result.index)) {
                    (list as? MutableList)?.startEditing(result.index)
                    event.consume()
                }
            }
        }
    }
}

public open class BasicMutableListBehavior<T>(
    focusManager: FocusManager? = null,
    generator   : ItemGenerator<T>,
    positioner  : ItemPositioner<T>,
    patternFill : PatternPaint? = null
): BasicListBehavior<T>(focusManager, generator, positioner, patternFill) {
    override fun pressed(event: KeyEvent) {
        when (event.key) {
            Delete, Backspace -> (event.source as MutableList<*, *>).let { list ->
                list.selection.sortedByDescending { it }.forEach { list.removeAt(it) }
            }
            else              -> super.pressed(event)
        }
    }
}

public inline fun <T> basicVerticalMutableListBehavior(
    focusManager : FocusManager?,
    generator    : ItemGenerator<T>,
    evenItemColor: Color?,
    oddItemColor : Color?,
    numColumns   : Int,
    itemHeight   : Double): BasicMutableListBehavior<T> = BasicMutableListBehavior(
    focusManager = focusManager,
    generator    = generator,
    positioner   = BasicVerticalListPositioner(itemHeight, numColumns),
    patternFill  = when {
        evenItemColor != null || oddItemColor != null -> horizontalStripedPaint(itemHeight, evenItemColor, oddItemColor)
        else                                          -> null
    }
)

public inline fun <T> basicVerticalMutableListBehavior(
    focusManager         : FocusManager? = null,
    evenItemColor        : Color?        = null,
    oddItemColor         : Color?        = null,
    selectionColor       : Color?        = null,
    selectionBlurredColor: Color?        = null,
    numColumns           : Int = 1,
    itemHeight           : Double): BasicMutableListBehavior<T> = basicVerticalMutableListBehavior(
    focusManager  = focusManager,
    generator     = BasicMutableItemGenerator(selectionColor, selectionBlurredColor),
    evenItemColor = evenItemColor,
    oddItemColor  = oddItemColor,
    numColumns    = numColumns,
    itemHeight    = itemHeight
)

public inline fun <T> basicHorizontalMutableListBehavior(
    focusManager : FocusManager?,
    generator    : ItemGenerator<T>,
    evenItemColor: Color?,
    oddItemColor : Color?,
    numRows      : Int,
    itemWidth    : Double): BasicMutableListBehavior<T> = BasicMutableListBehavior(
    focusManager = focusManager,
    generator    = generator,
    positioner   = BasicHorizontalListPositioner(itemWidth, numRows),
    patternFill  = when {
        evenItemColor != null || oddItemColor != null -> verticalStripedPaint(itemWidth, evenItemColor, oddItemColor)
        else                                          -> null
    }
)

public inline fun <T> basicHorizontalMutableListBehavior(
    focusManager         : FocusManager? = null,
    evenItemColor        : Color?        = null,
    oddItemColor         : Color?        = null,
    selectionColor       : Color?        = null,
    selectionBlurredColor: Color?        = null,
    numRows              : Int = 1,
    itemWidth            : Double): BasicMutableListBehavior<T> = basicHorizontalMutableListBehavior(
    focusManager  = focusManager,
    generator     = BasicMutableItemGenerator(selectionColor, selectionBlurredColor),
    evenItemColor = evenItemColor,
    oddItemColor  = oddItemColor,
    numRows       = numRows,
    itemWidth     = itemWidth
)

public open class TextEditOperation<T>(
                    focusManager: FocusManager?,
                    mapper      : Encoder<T, String>,
        private val list        : MutableList<T, *>,
                    item        : T,
                    current     : View): GenericTextEditOperation<T, MutableList<T, *>>(focusManager, mapper, list, item, current) {

    private val listSelectionChanged = { _: List<T, *>, _: Set<Int>, _:  Set<Int> ->
        list.cancelEditing()
    }

    init {
        list.selectionChanged += listSelectionChanged

        textField.horizontalAlignment = Left
        textField.fitText             = setOf(Width)
        textField.backgroundColor     = current.backgroundColor
    }

    override fun invoke(): View = object: View() {
        init {
            children += textField

            layout = constrain(textField) {
                it.height = parent.height - 1
                it.bottom = parent.bottom
            }
        }

        override fun render(canvas: Canvas) {
            textField.backgroundColor?.let { canvas.rect(bounds.atOrigin.inset(Insets(top = 1.0)), ColorPaint(it)) }
        }
    }

    override fun cancel() {
        list.selectionChanged -= listSelectionChanged
    }
}

public open class ListTextEditor<T>(private val focusManager: FocusManager?, private val encoder: Encoder<T, String>): ListEditor<T> {
    override fun edit(list: MutableList<T, *>, item: T, index: Int, current: View): EditOperation<T> = TextEditOperation(focusManager, encoder, list, item, current)
}