package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Icon
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.Insets.Companion.None
import io.nacular.doodle.text.TextSpacing
import io.nacular.doodle.text.TextSpacing.Companion.default
import io.nacular.doodle.utils.Anchor
import io.nacular.doodle.utils.HorizontalAlignment.Center
import io.nacular.doodle.utils.HorizontalAlignment.Left
import io.nacular.doodle.utils.HorizontalAlignment.Right
import io.nacular.doodle.utils.VerticalAlignment.Bottom
import io.nacular.doodle.utils.VerticalAlignment.Middle
import io.nacular.doodle.utils.VerticalAlignment.Top
import kotlin.math.max
import kotlin.math.min

/**
 * A [Behavior]<[Button]> that handles most of the commonly expected behavior of buttons with text. This makes it a good base for
 * more complex behaviors.
 *
 * @param textMetrics used for measuring the button's text
 * @param defaultFont used for buttons without a Font
 * @param insets that determine padding around text.
 * @param focusManager used to request focus
 */
public abstract class CommonTextButtonBehavior<in T: Button>(
        private val textMetrics : TextMetrics,
        private val defaultFont : Font?         = null,
        private val insets      : Insets        = None,
                    focusManager: FocusManager? = null): CommonButtonBehavior<T>(focusManager) {

    protected open val textChanged: (Button, String, String) -> Unit = { button,_,_ ->
        button.rerender()
    }

    override fun install(view: T) {
        super.install(view)

        view.textChanged += textChanged
    }

    override fun uninstall(view: T) {
        super.uninstall(view)

        view.textChanged -= textChanged
    }

    public fun textPosition(button: Button, text: String = button.text, icon: Icon<Button>? = button.icon, bounds: Rectangle = button.bounds.atOrigin, textSpacing: TextSpacing = default): Point {
        var minX       = insets.left
        val stringSize = textMetrics.size(text, font(button), textSpacing)
        var iconWidth  = 0.0
        var maxX       = bounds.width - stringSize.width - insets.right

        icon?.let {
            iconWidth = it.size(button).width + button.iconTextSpacing

            when (button.iconAnchor) {
                Anchor.Left,  Anchor.Leading  -> minX += iconWidth
                Anchor.Right, Anchor.Trailing -> maxX -= iconWidth
            }
        }

        val x = when (button.horizontalAlignment) {
            Right  -> max(maxX, minX)
            Center -> {
                val iconOffset = when (button.iconAnchor) {
                    Anchor.Leading -> iconWidth
                    else           -> 0.0
                }

                max(minX, min(maxX, (bounds.width - (stringSize.width + iconWidth)) / 2 + iconOffset))
            }
            Left   -> minX
        }

        val y = when (button.verticalAlignment) {
            Bottom -> bounds.height - insets.bottom
            Middle -> max(insets.top, min(bounds.height - insets.bottom, (bounds.height - stringSize.height) / 2))
            Top    -> insets.top
        }

        return Point(x, y) + bounds.position
    }

    public fun iconPosition(
        button: Button,
        text: String = button.text,
        icon: Icon<Button>,
        stringPosition: Point = textPosition(button, text, icon),
        bounds: Rectangle = button.bounds.atOrigin
    ): Point {
        val size = icon.size(button)

        val y = when (button.verticalAlignment) {
            Bottom -> bounds.height - insets.bottom
            Middle -> max(insets.top, min(bounds.height - insets.bottom, (bounds.height - size.height) / 2))
            Top    -> insets.top
        }

        val minX        = insets.left
        val maxX        = bounds.width - size.width - insets.right
        val stringWidth = textMetrics.width(text, font(button))

        val x = when (button.iconAnchor) {
            Anchor.Leading -> when {
                stringWidth > 0 -> max(minX, stringPosition.x - size.width - button.iconTextSpacing)
                else            -> max(minX, min(maxX, (bounds.width - size.width) / 2))
            }

            Anchor.Right -> when {
                stringWidth > 0 -> max(maxX, stringPosition.x + stringWidth + button.iconTextSpacing)
                else            -> max(maxX, minX)
            }

            Anchor.Trailing -> when {
                stringWidth > 0 -> stringPosition.x + stringWidth + button.iconTextSpacing
                else            -> max(minX, min(maxX, (bounds.width - size.width) / 2))
            }
            else -> minX
        }

        return Point(x, y) + bounds.position
    }

    public fun iconPosition(
        button: Button,
        text: String = button.text,
        icon: Icon<Button>,
        textSpacing: TextSpacing = default,
        stringPosition: Point = textPosition(button, text, icon, textSpacing = textSpacing),
        bounds: Rectangle = button.bounds.atOrigin
    ): Point = iconPosition(button = button, text = text, icon = icon, stringPosition = stringPosition, bounds = bounds)

    public fun font(button: Button): Font? = button.font ?: defaultFont
}

/**
 * Creates a [CommonTextButtonBehavior]<[Button]> that uses [render] to draw a button's contents.
 *
 * @param textMetrics used by the backing behavior
 * @param render called when updating the button
 * @return a new [CommonTextButtonBehavior]
 */
public inline fun <T: Button> simpleTextButtonRenderer(
    textMetrics : TextMetrics,
    crossinline render: CommonTextButtonBehavior<T>.(button: T, canvas: Canvas) -> Unit): Behavior<T> = object: CommonTextButtonBehavior<T>(textMetrics, focusManager = null) {
    override fun render(view: T, canvas: Canvas) = render(this, view, canvas)
}

/**
 * Creates a [CommonTextButtonBehavior]<[Button]> that uses [render] to draw a button's contents.
 *
 * @param textMetrics used by the backing behavior
 * @param focusManager used by the backing behavior
 * @param render called when updating the button
 * @return a new [CommonTextButtonBehavior]
 */
public inline fun <T: Button> simpleTextButtonRenderer(
    textMetrics : TextMetrics,
    focusManager: FocusManager?,
    crossinline render: CommonTextButtonBehavior<T>.(button: T, canvas: Canvas) -> Unit): Behavior<T> = object: CommonTextButtonBehavior<T>(textMetrics, focusManager = focusManager) {
    override fun render(view: T, canvas: Canvas) = render(this, view, canvas)
}

/**
 * Creates a [CommonTextButtonBehavior]<[Button]> that uses [render] to draw a button's contents.
 *
 * @param textMetrics used by the backing behavior
 * @param focusManager used by the backing behavior
 * @param insets used by the backing behavior
 * @param render called when updating the button
 * @return a new [CommonTextButtonBehavior]
 */
public inline fun <T: Button> simpleTextButtonRenderer(
    textMetrics : TextMetrics,
    focusManager: FocusManager? = null,
    insets      : Insets        = None,
    crossinline render: CommonTextButtonBehavior<T>.(button: T, canvas: Canvas) -> Unit): Behavior<T> = object: CommonTextButtonBehavior<T>(textMetrics, focusManager = focusManager, insets = insets) {
    override fun render(view: T, canvas: Canvas) = render(this, view, canvas)
}

/**
 * Creates a [CommonTextButtonBehavior]<[Button]> that uses [render] to draw a button's contents.
 *
 * @param textMetrics used by the backing behavior
 * @param focusManager used by the backing behavior
 * @param insets used by the backing behavior
 * @param contains determines whether a [Point] is within the button
 * @param render called when updating the button
 * @return a new [CommonTextButtonBehavior]
 */
public inline fun <T: Button> simpleTextButtonRenderer(
    textMetrics         : TextMetrics,
    focusManager        : FocusManager? = null,
    insets              : Insets        = None,
    crossinline contains: (T, Point) -> Boolean,
    crossinline render  : CommonTextButtonBehavior<T>.(button: T, canvas: Canvas) -> Unit): Behavior<T> = object: CommonTextButtonBehavior<T>(textMetrics, focusManager = focusManager, insets = insets) {
    override fun render  (view: T, canvas: Canvas) = render  (this, view, canvas)
    override fun contains(view: T, point : Point ) = contains(view, point       )
}