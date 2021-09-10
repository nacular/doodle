package io.nacular.doodle.theme.basic.dropdown

import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.TextVisualizer
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.dropdown.Dropdown
import io.nacular.doodle.controls.dropdown.DropdownBehavior
import io.nacular.doodle.controls.list.ListBehavior
import io.nacular.doodle.controls.spinner.Spinner
import io.nacular.doodle.controls.toString
import io.nacular.doodle.core.Icon
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.PatternPaint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.ConstraintLayout
import io.nacular.doodle.layout.constant
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.theme.basic.BasicButtonBehavior
import io.nacular.doodle.theme.basic.ColorMapper
import io.nacular.doodle.theme.basic.list.BasicItemGenerator
import io.nacular.doodle.theme.basic.list.BasicListBehavior
import io.nacular.doodle.utils.Anchor
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool

/**
 * Created by Nicholas Eddy on 9/9/21.
 */
public class BasicDropdownBehavior<T, M: ListModel<T>>(
        private val textMetrics        : TextMetrics,
        private val backgroundColor    : Color,
        private val darkBackgroundColor: Color,
        private val foregroundColor    : Color,
        private val cornerRadius       : Double,
        private val generator          : ListBehavior.RowGenerator<T>,
        private val rowHeight          : Double,
        private val patternFill        : PatternPaint? = null,
        private val buttonWidth        : Double = 20.0,
        private val focusManager       : FocusManager? = null,
): DropdownBehavior<T, M>, KeyListener, PointerListener {

    public var hoverColorMapper   : ColorMapper = { it.darker(0.1f) }
    public var disabledColorMapper: ColorMapper = { it.lighter()    }

    private inner class ButtonIcon: Icon<Button> {
        override fun size(view: Button) = Size(view.width * 0.5, view.height * 0.3)

        override fun render(view: Button, canvas: Canvas, at: Point) {
            val size = size(view)

            val stroke = Stroke(when {
                view.enabled -> foregroundColor
                else         -> disabledColorMapper(foregroundColor)
            }, 1.5)

            val points = listOf(
                Point(at.x,                  at.y + size.height),
                Point(at.x + size.width / 2, at.y              ),
                Point(at.x + size.width,     at.y + size.height)
            )

            canvas.path(points, stroke)
            canvas.transform(Identity.flipVertically(view.height / 2)) {
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
            view.icon = ButtonIcon()

            super.install(view)
        }

        override fun render(view: Button, canvas: Canvas) {
            canvas.rect(Rectangle(view.width + cornerRadius, view.height + cornerRadius), cornerRadius, colors(view).fillColor.paint)

            icon(view)?.let {
                it.render(view, canvas, iconPosition(view, icon = it))
            }
        }
    }

    private val itemVisualizer by lazy { toString<T?, Unit>(TextVisualizer(fitText = emptySet())) }

    override fun install(view: Dropdown<T, M>) {
        super.install(view)

        view.list.behavior = BasicListBehavior(focusManager, generator, patternFill, rowHeight)

        val center = (view.comboItemVisualizer ?: itemVisualizer)(view.value, null, Unit)
        val button = PushButton().apply {
            iconAnchor    = Anchor.Leading
            acceptsThemes = false
            focusable     = false
            behavior      = ButtonBehavior()
        }

        // FIXME: need to cleanup in uninstall
        view.changed += {
            updateCenter(view)
        }

        button.fired += {

        }

        view.children.clear()
        view.children += listOf(center, button)

        view.layout = constrain(center, button) { center, button ->
            center.top      = parent.top
            center.left     = parent.left
            center.right    = button.left
            center.bottom   = parent.bottom

            button.top        = parent.top
            button.right      = parent.right
            button.bottom     = parent.bottom
            button.width      = constant(buttonWidth)
        }

        view.keyChanged     += this
        view.pointerChanged += this
    }

    override fun uninstall(view: Dropdown<T, M>) {
        super.uninstall(view)

        view.list.behavior = null
    }

    internal val centerChanged: Pool<(Dropdown<*, *>, View?, View) -> Unit> = SetPool()

    internal fun updateCenter(dropdown: Dropdown<T, M>, oldCenter: View? = visualizedValue(dropdown), newCenter: View = (dropdown.comboItemVisualizer ?: itemVisualizer)(dropdown.value, oldCenter, Unit)) {
        if (oldCenter != null && newCenter != oldCenter) {
            dropdown.children -= oldCenter
            (dropdown.layout as? ConstraintLayout)?.unconstrain(oldCenter)

            dropdown.children += newCenter

            (dropdown.layout as? ConstraintLayout)?.constrain(newCenter, dropdown.children[0]) { center, button ->
                center.top    = parent.top
                center.left   = parent.left
                center.right  = button.left
                center.bottom = parent.bottom
            }

            (centerChanged as SetPool).forEach { it(dropdown, oldCenter, newCenter) }
        }
    }

    internal fun visualizedValue(dropdown: Dropdown<T, M>): View? = dropdown.children.firstOrNull { it !is PushButton }

    public companion object {
        public operator fun <T, M: ListModel<T>> invoke(
                textMetrics          : TextMetrics,
                backgroundColor      : Color,
                darkBackgroundColor  : Color,
                foregroundColor      : Color,
                cornerRadius         : Double,
                patternFill          : PatternPaint? = null,
                buttonWidth          : Double = 20.0,
                focusManager         : FocusManager? = null,
                selectionColor       : Color?        = null,
                selectionBlurredColor: Color?        = null,
                rowHeight            : Double): BasicDropdownBehavior<T, M> = BasicDropdownBehavior(
                    textMetrics         = textMetrics,
                    backgroundColor     = backgroundColor,
                    darkBackgroundColor = darkBackgroundColor,
                    focusManager        = focusManager,
                    rowHeight           = rowHeight,
                    generator           = BasicItemGenerator(selectionColor, selectionBlurredColor),
                    foregroundColor     = foregroundColor,
                    cornerRadius        = cornerRadius,
                    patternFill         = patternFill,
                    buttonWidth         = buttonWidth,
        )
    }
}