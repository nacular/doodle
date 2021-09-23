package io.nacular.doodle.theme.basic.dropdown

import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.SimpleIndexedItem
import io.nacular.doodle.controls.SingleItemSelectionModel
import io.nacular.doodle.controls.TextVisualizer
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.dropdown.Dropdown
import io.nacular.doodle.controls.dropdown.DropdownBehavior
import io.nacular.doodle.controls.list.List
import io.nacular.doodle.controls.list.ListBehavior
import io.nacular.doodle.controls.spinner.Spinner
import io.nacular.doodle.controls.toString
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Icon
import io.nacular.doodle.core.View
import io.nacular.doodle.core.center
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.event.KeyCode.Companion.Enter
import io.nacular.doodle.event.KeyCode.Companion.Escape
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.KeyText
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerListener.Companion.clicked
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.ConstraintBlockContext
import io.nacular.doodle.layout.ConstraintLayout
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constant
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.layout.fill
import io.nacular.doodle.theme.basic.BasicButtonBehavior
import io.nacular.doodle.theme.basic.ColorMapper
import io.nacular.doodle.theme.basic.ListRow
import io.nacular.doodle.theme.basic.list.BasicListBehavior
import io.nacular.doodle.theme.basic.list.BasicListPositioner
import io.nacular.doodle.utils.Anchor
import io.nacular.doodle.utils.ChangeObserver
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 9/9/21.
 */
public class BasicDropdownBehavior<T, M: ListModel<T>>(
        private val display            : Display,
        private val textMetrics        : TextMetrics,
        private val backgroundColor    : Color,
        private val darkBackgroundColor: Color,
        private val foregroundColor    : Color,
        private val cornerRadius       : Double,
        private val buttonWidth        : Double = 20.0,
        private val focusManager       : FocusManager? = null,
): DropdownBehavior<T, M>, PointerListener, KeyListener {

    public var hoverColorMapper   : ColorMapper = { it.darker(0.1f) }
    public var disabledColorMapper: ColorMapper = { it.lighter()    }

    internal var buttonAlignment: (Constraints.() -> Unit) = fill

    private inner class ButtonIcon(private val colors: (Button) -> Color): Icon<Button> {
        override fun size(view: Button) = Size(buttonWidth, max(0.0, view.height - 2 * INSET))

        override fun render(view: Button, canvas: Canvas, at: Point) {
            val iconSize      = size(view)
            val arrowSize     = iconSize.run { Size(width * 0.5, height * 0.6) }
            val arrowPosition = at + Point(x = (iconSize.width - arrowSize.width) / 2, y = (iconSize.height - arrowSize.height) / 2)

            val stroke = Stroke(when {
                view.enabled -> foregroundColor
                else         -> disabledColorMapper(foregroundColor)
            }, 1.5)

            val points = listOf(
                    Point(arrowPosition.x,                       arrowPosition.y + arrowSize.height * 0.3),
                    Point(arrowPosition.x + arrowSize.width / 2, arrowPosition.y                         ),
                    Point(arrowPosition.x + arrowSize.width,     arrowPosition.y + arrowSize.height * 0.3)
            )

            canvas.rect(view.bounds.atOrigin.inset(Insets(
                    left   = view.width - buttonWidth - INSET,
                    top    = INSET,
                    right  = INSET,
                    bottom = INSET)), cornerRadius, colors(view).paint)

            canvas.path(points, stroke)
            canvas.transform(Identity.flipVertically(arrowPosition.y + arrowSize.height / 2)) {
                path(points, stroke)
            }
        }
    }

    private inner class ButtonBehavior: BasicButtonBehavior(
            textMetrics         = textMetrics,
            cornerRadius        = cornerRadius,
            backgroundColor     = backgroundColor,
            foregroundColor     = foregroundColor,
            darkBackgroundColor = darkBackgroundColor
    ) {
        init {
            hoverColorMapper    = this@BasicDropdownBehavior.hoverColorMapper
            disabledColorMapper = { it }
        }

        override fun install(view: Button) {
            view.icon       = ButtonIcon { colors(it).fillColor }
            view.iconAnchor = Anchor.Right

            super.install(view)
        }

        override fun render(view: Button, canvas: Canvas) {
            icon(view)?.let {
                it.render(view, canvas, iconPosition(view, icon = it) - Point(INSET, 0.0))
            }
        }
    }

    private val itemVisualizer by lazy { toString<T, IndexedItem>(TextVisualizer(fitText = emptySet())) }

    private val changeObserver: ChangeObserver<Dropdown<T, M>> = {
        it.list?.setSelection(setOf(it.selection))
    }

    private inner class CustomListRow(
                        dropdown      : Dropdown<T, M>,
                        list          : List<T, *>,
                        row           : T,
                        index         : Int,
                        itemVisualizer: ItemVisualizer<T, IndexedItem>,
            private val cornerRadius  : Double,
    ): ListRow<T>(list,
            row,
            index,
            itemVisualizer,
            backgroundSelectionColor        = hoverColorMapper(this@BasicDropdownBehavior.backgroundColor),
            backgroundSelectionBlurredColor = null) {
        init {
            pointerFilter += clicked {
                dropdown.selection = index
                hideList(dropdown)
            }
        }

        override fun pointerOver(value: Boolean) {
            when {
                value -> list.setSelection   (setOf(index))
                else  -> list.removeSelection(setOf(index))
            }
        }

        override fun render(canvas: Canvas) {
            backgroundColor?.let { canvas.rect(bounds.atOrigin, cornerRadius, it.paint) }
        }
    }

    private inner class ItemGenerator(private val dropdown: Dropdown<T, M>): ListBehavior.RowGenerator<T> {
        override fun invoke(list: List<T, *>, row: T, index: Int, current: View?): View = when (current) {
            is ListRow<*> -> (current as BasicDropdownBehavior<T, M>.CustomListRow).apply { update(list, row, index) }
            else          -> CustomListRow(
                    dropdown       = dropdown,
                    list           = list,
                    row            = row,
                    index          = index,
                    cornerRadius   = cornerRadius,
                    itemVisualizer = list.itemVisualizer ?: toString(TextVisualizer())
            )
        }.apply {
            list.cellAlignment?.let { positioner = it }
        }
    }

    override fun render(view: Dropdown<T, M>, canvas: Canvas) {
        canvas.rect(view.bounds.atOrigin, cornerRadius, backgroundColor.paint)
    }

    private fun showList(view: Dropdown<T, M>) {
        view.list?.let {
            // FIXME Do better placement?
            val rowHeight = view.height - 2 * INSET

            it.x     = view.x
            it.y     = view.center.y - view.height / 2 - (view.selection * rowHeight)
            it.width = view.width
            display.children += it

            it.setSelection(setOf(view.selection))

            focusManager?.requestFocus(it)
        }
    }

    private fun hideList(view: Dropdown<T, M>) {
        view.list?.let { display.children -= it }
    }

    override fun install(view: Dropdown<T, M>) {
        super.install(view)

        view.list = List(view.model, selectionModel = SingleItemSelectionModel()).apply {
            insets = Insets(INSET)

            cellAlignment = {
                centerX = parent.centerX - buttonWidth / 2
                centerY = parent.centerY
            }

            behavior = object: BasicListBehavior<T>(focusManager, ItemGenerator(view), null, 0.0) {
                override val positioner: ListBehavior.RowPositioner<T> = object: BasicListPositioner<T>(0.0) {
                    override val height get() = view.height - 2 * INSET
                }

                override fun render(view: List<T, *>, canvas: Canvas) {
                    canvas.rect(view.bounds.atOrigin, cornerRadius, this@BasicDropdownBehavior.backgroundColor.paint)
                }
            }

            acceptsThemes = false

            focusChanged += { _,_,focused ->
                if (!focused) {
                    hideList(view)
                }
            }

            keyChanged += KeyListener.released { event ->
                if (displayed) {
                    when (event.code) {
                        Enter  -> firstSelection?.let { index ->
                            view.selection = index
                            hideList(view)
                        }
                        Escape -> hideList(view)
                    }
                }
            }
        }

        val center = (view.boxItemVisualizer ?: itemVisualizer)(view.value, null, SimpleIndexedItem(view.selection, true))
        val button = PushButton().apply {
            focusable     = false
            iconAnchor    = Anchor.Leading
            acceptsThemes = false
            behavior      = ButtonBehavior()
            enabled       = !view.isEmpty

            fired += {
                showList(view)
            }
        }

        view.children.clear()

        view.children += center

        constrainCenter(view, center)

        view.children += button

        view.layout = when (val l = view.layout) {
            is ConstraintLayout -> l.constrain(button) { buttonAlignment(it) }
            else                -> constrain  (button) { buttonAlignment(it) }
        }

        view.changed        += changeObserver
        view.keyChanged     += this
        view.pointerChanged += this
    }

    override fun uninstall(view: Dropdown<T, M>) {
        super.uninstall(view)

        hideList(view)

        view.list?.behavior  = null
        view.list            = null
        view.changed        -= changeObserver
        view.keyChanged     -= this
        view.pointerChanged -= this
    }

    override fun changed(dropdown: Dropdown<T, M>) {
        updateCenter(dropdown)
    }

    override fun pressed(event: KeyEvent) {
        (event.source as? Dropdown<T, M>)?.apply {
            when (event.key) {
                KeyText.ArrowUp   -> selection -= 1
                KeyText.ArrowDown -> selection += 1
            }
        }
    }

    override fun pressed(event: PointerEvent) {
        focusManager?.requestFocus(event.source)
    }

    internal val centerChanged: Pool<(Dropdown<T, M>, View?, View?) -> Unit> = SetPool()

    internal fun updateCenter(dropdown: Dropdown<T, M>, oldCenter: View? = visualizedValue(dropdown), newCenter: View? = (dropdown.boxItemVisualizer ?: itemVisualizer)(dropdown.value, oldCenter, SimpleIndexedItem(dropdown.selection, true)))  {
        if (oldCenter != null && newCenter != oldCenter) {
            dropdown.children -= oldCenter
            (dropdown.layout as? ConstraintLayout)?.unconstrain(oldCenter)

            newCenter?.let {
                dropdown.children += it

                constrainCenter(dropdown, newCenter)
            }

            (centerChanged as SetPool).forEach { it(dropdown, oldCenter, newCenter) }
        }
    }

    private fun constrainCenter(dropdown: Dropdown<T, M>, center: View) {
        val constrains: ConstraintBlockContext.(Constraints) -> Unit = {
            it.top    = parent.top    + INSET
            it.left   = parent.left   + INSET
            it.right  = parent.right  - constant(buttonWidth + INSET)
            it.bottom = parent.bottom - INSET
        }

        when (val l = dropdown.layout) {
            is ConstraintLayout -> l.constrain(center, constrains)
            else                -> dropdown.layout = constrain(center, constrains)
        }
    }

    internal fun visualizedValue(dropdown: Dropdown<T, M>): View? = dropdown.children.firstOrNull { it !is PushButton }

    public companion object {
        internal const val INSET = 4.0

        public operator fun <T, M: ListModel<T>> invoke(
                display              : Display,
                textMetrics          : TextMetrics,
                backgroundColor      : Color,
                darkBackgroundColor  : Color,
                foregroundColor      : Color,
                cornerRadius         : Double,
                buttonWidth          : Double        = 20.0,
                focusManager         : FocusManager? = null): BasicDropdownBehavior<T, M> = BasicDropdownBehavior(
                    display             = display,
                    textMetrics         = textMetrics,
                    backgroundColor     = backgroundColor,
                    darkBackgroundColor = darkBackgroundColor,
                    focusManager        = focusManager,
                    foregroundColor     = foregroundColor,
                    cornerRadius        = cornerRadius,
                    buttonWidth         = buttonWidth,
        )
    }
}