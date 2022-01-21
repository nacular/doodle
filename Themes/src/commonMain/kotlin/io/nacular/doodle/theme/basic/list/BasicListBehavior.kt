package io.nacular.doodle.theme.basic.list

import io.nacular.doodle.controls.TextVisualizer
import io.nacular.doodle.controls.list.List
import io.nacular.doodle.controls.list.ListBehavior
import io.nacular.doodle.controls.list.ListBehavior.ItemGenerator
import io.nacular.doodle.controls.list.ListBehavior.ItemPositioner
import io.nacular.doodle.controls.toString
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.horizontalStripedPaint
import io.nacular.doodle.drawing.verticalStripedPaint
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.theme.basic.HorizontalListPositioner
import io.nacular.doodle.theme.basic.VerticalListPositioner
import io.nacular.doodle.theme.basic.ListItem
import io.nacular.doodle.theme.basic.SelectableListKeyHandler

/**
 * Created by Nicholas Eddy on 3/20/18.
 */

public open class BasicItemGenerator<T>(private val selectionColor: Color? = null, private val selectionBlurredColor: Color? = null): ItemGenerator<T> {
    override fun invoke(list: List<T, *>, item: T, index: Int, current: View?): View = when (current) {
        is ListItem<*> -> (current as ListItem<T>).apply { update(list, item, index) }
        else           -> ListItem(
                list                            = list,
                item                            = item,
                index                           = index,
                itemVisualizer                  = list.itemVisualizer ?: toString(TextVisualizer()),
                backgroundSelectionColor        = selectionColor,
                backgroundSelectionBlurredColor = selectionBlurredColor
        )
    }.apply {
        list.cellAlignment?.let { positioner = it }
    }
}

public fun <T> basicItemGenerator(
        selectionColor       : Color? = null,
        selectionBlurredColor: Color? = null,
        configure            : ListItem<T>.() -> Unit
): ItemGenerator<T> = object: BasicItemGenerator<T>(selectionColor, selectionBlurredColor) {
    override fun invoke(list: List<T, *>, item: T, index: Int, current: View?) = super.invoke(list, item, index, current).apply {
        if (current != this) configure(this as ListItem<T>)
    }
}

public open class BasicListBehavior<T>(
    private  val focusManager: FocusManager? = null,
    override val generator   : ItemGenerator<T>,
    override val positioner  : ItemPositioner<T>,
    private  val fill        : Paint? = null
): ListBehavior<T>, KeyListener, PointerListener, SelectableListKeyHandler {
    override fun install(view: List<T, *>) {
        view.keyChanged    += this
        view.pointerFilter += this

        view.rerender()
    }

    override fun uninstall(view: List<T, *>) {
        view.keyChanged    -= this
        view.pointerFilter -= this
    }

    override fun render(view: List<T, *>, canvas: Canvas) {
        fill?.let { canvas.rect(view.bounds.atOrigin, it) }
    }

    override fun pressed(event: KeyEvent) {
        super<SelectableListKeyHandler>.pressed(event)
    }

    override fun pressed(event: PointerEvent) {
        focusManager?.requestFocus(event.source)
    }
}

public inline fun <T> verticalBasicListBehavior(
    focusManager : FocusManager?,
    generator    : ItemGenerator<T>,
    evenItemColor: Color?,
    oddItemColor : Color?,
    numColumns   : Int,
    itemHeight   : Double
): BasicListBehavior<T> = BasicListBehavior(
    focusManager = focusManager,
    generator    = generator,
    positioner   = BasicVerticalListPositioner(itemHeight, numColumns),
    fill         = when {
        evenItemColor != null || oddItemColor != null -> horizontalStripedPaint(itemHeight, evenItemColor, oddItemColor)
        else                                          -> null
    }
)

public inline fun <T> verticalBasicListBehavior(
    focusManager         : FocusManager? = null,
    evenItemColor        : Color?        = null,
    oddItemColor         : Color?        = null,
    selectionColor       : Color?        = null,
    selectionBlurredColor: Color?        = null,
    numColumns           : Int           = 1,
    itemHeight           : Double
): BasicListBehavior<T> = verticalBasicListBehavior(
    focusManager  = focusManager,
    generator     = BasicItemGenerator(selectionColor = selectionColor, selectionBlurredColor = selectionBlurredColor),
    evenItemColor = evenItemColor,
    oddItemColor  = oddItemColor,
    numColumns    = numColumns,
    itemHeight    = itemHeight
)

public inline fun <T> horizontalBasicListBehavior(
    focusManager : FocusManager?,
    generator    : ItemGenerator<T>,
    evenItemColor: Color?,
    oddItemColor : Color?,
    numRows      : Int,
    itemWidth    : Double
): BasicListBehavior<T> = BasicListBehavior(
    focusManager = focusManager,
    generator    = generator,
    positioner   = BasicHorizontalListPositioner(itemWidth, numRows),
    fill         = when {
        evenItemColor != null || oddItemColor != null -> verticalStripedPaint(itemWidth, evenItemColor, oddItemColor)
        else                                          -> null
    }
)

public inline fun <T> horizontalBasicListBehavior(
    focusManager         : FocusManager? = null,
    evenItemColor        : Color?        = null,
    oddItemColor         : Color?        = null,
    selectionColor       : Color?        = null,
    selectionBlurredColor: Color?        = null,
    numRows              : Int           = 1,
    itemWidth            : Double
): BasicListBehavior<T> = horizontalBasicListBehavior(
    focusManager  = focusManager,
    generator     = BasicItemGenerator(selectionColor = selectionColor, selectionBlurredColor = selectionBlurredColor),
    evenItemColor = evenItemColor,
    oddItemColor  = oddItemColor,
    numRows       = numRows,
    itemWidth     = itemWidth
)

public open class BasicVerticalListPositioner<T>(height: Double, numColumns: Int = 1, spacing: Double = 0.0): VerticalListPositioner(height, numColumns, spacing), ItemPositioner<T> {
    override fun item       (of: List<T, *>, at: Point                       ): Int       = itemFor    (of.insets, at)
    override fun minimumSize(of: List<T, *>                                  ): Size      = minimumSize(of.numItems, of.insets)
    override fun itemBounds (of: List<T, *>, item: T, index: Int, view: View?): Rectangle = itemBounds (of.size, of.insets, index, view)
}

public open class BasicHorizontalListPositioner<T>(width: Double, numRows: Int = 1, spacing: Double = 0.0): HorizontalListPositioner(width, numRows, spacing), ItemPositioner<T> {
    override fun item       (of: List<T, *>, at: Point                       ): Int       = itemFor    (of.insets, at)
    override fun minimumSize(of: List<T, *>                                  ): Size      = minimumSize(of.numItems, of.insets)
    override fun itemBounds (of: List<T, *>, item: T, index: Int, view: View?): Rectangle = itemBounds (of.size, of.insets, index, view)
}