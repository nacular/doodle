package io.nacular.doodle.theme.basic.spinner

import io.nacular.doodle.controls.TextVisualizer
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.spinner.Spinner
import io.nacular.doodle.controls.spinner.SpinnerBehavior
import io.nacular.doodle.controls.spinner.SpinnerModel
import io.nacular.doodle.controls.toString
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.Icon
import io.nacular.doodle.core.View
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.KeyText.Companion.ArrowDown
import io.nacular.doodle.event.KeyText.Companion.ArrowUp
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.ConstraintLayout
import io.nacular.doodle.layout.constraints.center
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.theme.basic.BasicButtonBehavior
import io.nacular.doodle.theme.basic.ColorMapper
import io.nacular.doodle.utils.Anchor.Leading
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool

public class BasicSpinnerBehavior<T, M: SpinnerModel<T>>(
        private val textMetrics        : TextMetrics,
        private val backgroundColor    : Color,
        private val darkBackgroundColor: Color,
        private val foregroundColor    : Color,
        private val cornerRadius       : Double,
        private val buttonWidth        : Double = 20.0,
        private val focusManager       : FocusManager? = null,
): SpinnerBehavior<T, M>(), KeyListener, PointerListener {

    public var hoverColorMapper   : ColorMapper = { it.darker(0.1f) }
    public var disabledColorMapper: ColorMapper = { it.lighter()    }

    private inner class ButtonIcon(private val isUp: Boolean): Icon<Button> {
        override fun size(view: Button) = Size(view.width * 0.5, view.height * 0.3)

        override fun render(view: Button, canvas: Canvas, at: Point) {
            val size = size(view)
            val transform = when {
                isUp -> Identity
                else -> Identity.flipVertically(at.y + size.height / 2)
            }

            val stroke = Stroke(when {
                view.enabled -> foregroundColor
                else         -> disabledColorMapper(foregroundColor)
            }, 1.5)

            canvas.transform(transform) {
                path(listOf(
                        Point(at.x,                  at.y + size.height),
                        Point(at.x + size.width / 2, at.y              ),
                        Point(at.x + size.width,     at.y + size.height)), stroke)
            }
        }
    }

    private inner class SpinnerButtonBehavior(private val isTop: Boolean): BasicButtonBehavior(
            textMetrics         = textMetrics,
            cornerRadius        = cornerRadius,
            backgroundColor     = backgroundColor,
            foregroundColor     = foregroundColor,
            darkBackgroundColor = darkBackgroundColor
    ) {
        init {
            hoverColorMapper    = this@BasicSpinnerBehavior.hoverColorMapper
            disabledColorMapper = { it }
        }

        override fun install(view: Button) {
            view.icon = ButtonIcon(isTop)

            super.install(view)
        }

        override fun render(view: Button, canvas: Canvas) {
            canvas.rect(
                Rectangle(
                    0.0,
                    0.0 - if (!isTop) cornerRadius else 0.0,
                    view.width,
                    view.height + cornerRadius),
                cornerRadius, colors(view).fillColor.paint)

            icon(view)?.let {
                val adjust = it.size(view).height / 5 * if (isTop) 1 else -1
                it.render(view, canvas, iconPosition(view, icon = it) + Point(0.0, adjust))
            }
        }
    }

    private val itemVisualizer by lazy { toString<T, Any>(TextVisualizer()) }

    override fun changed(spinner: Spinner<T, M>) {}

    override fun render(view: Spinner<T, M>, canvas: Canvas) {
        canvas.rect(view.bounds.atOrigin, cornerRadius, ColorPaint(backgroundColor))
    }

    override fun install(view: Spinner<T, M>) {
        super.install(view)

        val center = Container().apply { focusable = false }
        val next = PushButton().apply {
            iconAnchor    = Leading
            enabled       = view.hasNext
            acceptsThemes = false
            focusable     = false
            behavior      = SpinnerButtonBehavior(true)
        }

        val previous = PushButton().apply {
            iconAnchor    = Leading
            enabled       = view.hasPrevious
            acceptsThemes = false
            focusable     = false
            behavior      = SpinnerButtonBehavior(false)
        }

        // FIXME: need to cleanup in uninstall
        view.changed += {
            updateCenter(view)

            next.enabled     = it.hasNext
            previous.enabled = it.hasPrevious
        }

        next.fired += {
            view.next()
        }

        previous.fired += {
            view.previous()
        }

        view.children.clear()
        view.children += listOf(center, next, previous)

        view.layout = constrain(center, next, previous) { center, next, previous ->
            center.top      eq INSET
            center.left     eq INSET
            center.right    eq next.left     - INSET
            center.bottom   eq parent.bottom - INSET

            next.top        eq INSET
            next.right      eq parent.right - INSET
            next.bottom     eq parent.centerY
            next.width      eq buttonWidth

            previous.top    eq next.bottom
            previous.left   eq next.left
            previous.right  eq next.right
            previous.bottom eq parent.bottom - INSET
        }

        updateCenter(view)

        view.keyChanged     += this
        view.pointerChanged += this
    }

    override fun uninstall(view: Spinner<T, M>) {
        super.uninstall(view)

        view.children.clear()
        view.keyChanged     -= this
        view.pointerChanged -= this
    }

    override fun pressed(event: KeyEvent) {
        (event.source as? Spinner<*,*>)?.apply {
            when (event.key) {
                ArrowUp   -> { next    (); event.consume() }
                ArrowDown -> { previous(); event.consume() }
            }
        }
    }

    override fun pressed(event: PointerEvent) {
        focusManager?.requestFocus(event.source)
    }

    internal val centerChanged: Pool<(Spinner<T, M>, View?, View) -> Unit> = SetPool()

    internal fun updateCenter(spinner: Spinner<T, M>, oldCenter: View? = visualizedValue(spinner), newCenter: View = centerView(spinner, oldCenter)) {
        if (newCenter != oldCenter) {
            viewContainer(spinner)?.let { centerView ->
                centerView.children.clear()

                centerView += newCenter

                updateAlignment(spinner, centerView)
            }

            (centerChanged as SetPool).forEach { it(spinner, oldCenter, newCenter) }
        }
    }

    private fun centerView(spinner: Spinner<T, M>, oldCenter: View?) = spinner.value.fold(
        onSuccess = { (spinner.itemVisualizer ?: itemVisualizer)(it, oldCenter, spinner) },
        onFailure = { view {  } }
    )

    private fun updateAlignment(spinner: Spinner<T, M>, centerView: Container) {
        val constrains: ConstraintDslContext.(Bounds) -> Unit = {
            (spinner.cellAlignment ?: center)(it)
        }

        centerView.firstOrNull()?.let { child ->
            when (val l = centerView.layout) {
                is ConstraintLayout -> { l.unconstrain(child, constrains); l.constrain(child, constrains) }
                else                -> centerView.layout = constrain(child, constrains)
            }
        }
    }

    private  fun viewContainer  (spinner: Spinner<T, M>): Container? = spinner.children.firstOrNull { it !is PushButton } as? Container
    internal fun visualizedValue(spinner: Spinner<T, M>): View?      = viewContainer(spinner)?.firstOrNull()

    public companion object {
        private const val INSET = 4.0
    }
}