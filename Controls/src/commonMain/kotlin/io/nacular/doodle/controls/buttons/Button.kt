package io.nacular.doodle.controls.buttons

import io.nacular.doodle.accessibility.button
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Icon
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.Anchor
import io.nacular.doodle.utils.Anchor.Left
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.HorizontalAlignment
import io.nacular.doodle.utils.HorizontalAlignment.Center
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.VerticalAlignment
import io.nacular.doodle.utils.VerticalAlignment.Middle
import io.nacular.doodle.utils.observable

/**
 * Created by Nicholas Eddy on 11/10/17.
 */

@Suppress("PrivatePropertyName")
public abstract class Button protected constructor(
            text : String        = "",
            icon : Icon<Button>? = null,
            model: ButtonModel   = ButtonModelImpl(),
            role : button        = button()): View(accessibilityRole = role) {

    private val armedChanged_       = { _: ButtonModel, old: Boolean, new: Boolean -> (armedChanged       as PropertyObserversImpl)(old, new) }
    private val pressedChanged_     = { _: ButtonModel, old: Boolean, new: Boolean -> (pressedChanged     as PropertyObserversImpl)(old, new) }
    private val pointerOverChanged_ = { _: ButtonModel, old: Boolean, new: Boolean -> (pointerOverChanged as PropertyObserversImpl)(old, new) }
    private val modelFired          = { _: ButtonModel -> (fired as ChangeObserversImpl).forEach { it(this) } }

    private fun registerModel(model: ButtonModel) {
        model.fired              += modelFired
        model.armedChanged       += armedChanged_
        model.pressedChanged     += pressedChanged_
        model.pointerOverChanged += pointerOverChanged_
    }

    private fun unregisterModel(model: ButtonModel) {
        model.fired              -= modelFired
        model.armedChanged       -= armedChanged_
        model.pressedChanged     -= pressedChanged_
        model.pointerOverChanged -= pointerOverChanged_
    }

    public val textChanged: PropertyObservers<Button, String> by lazy { PropertyObserversImpl(this) }

    public var text: String by observable(text, textChanged as PropertyObserversImpl<Button, String>)

    public val fired: ChangeObservers<Button> by lazy { ChangeObserversImpl(this) }

    public val armedChanged      : PropertyObservers<Button, Boolean> by lazy { PropertyObserversImpl(this) }
    public val pressedChanged    : PropertyObservers<Button, Boolean> by lazy { PropertyObserversImpl(this) }
    public val pointerOverChanged: PropertyObservers<Button, Boolean> by lazy { PropertyObserversImpl(this) }

    public var behavior: Behavior<Button>? by behavior()

    public var iconTextSpacing: Double by styleProperty(4.0) { true }

    public var verticalAlignment: VerticalAlignment by styleProperty(Middle) { true }

    public var horizontalAlignment: HorizontalAlignment by styleProperty(Center) { true }

    public var iconAnchor: Anchor by styleProperty(Left) { true }

    public var icon                   : Icon<Button>? = icon;                                set(new) { field = new; styleChanged { true } }
    public var pressedIcon            : Icon<Button>? = null; get() = field ?: icon;         set(new) { field = new; styleChanged { true } }
    public var disabledIcon           : Icon<Button>? = null; get() = field ?: icon;         set(new) { field = new; styleChanged { true } }
    public var selectedIcon           : Icon<Button>? = null; get() = field ?: icon;         set(new) { field = new; styleChanged { true } }
    public var pointerOverIcon        : Icon<Button>? = null; get() = field ?: icon;         set(new) { field = new; styleChanged { true } }
    public var disabledSelectedIcon   : Icon<Button>? = null; get() = field ?: disabledIcon; set(new) { field = new; styleChanged { true } }
    public var pointerOverSelectedIcon: Icon<Button>? = null; get() = field ?: selectedIcon; set(new) { field = new; styleChanged { true } }

    public var selected: Boolean
        get(   ) = model.selected
        set(new) {
            if (enabled) {
                model.selected = new
            }
        }

    public open var model: ButtonModel = model
        set(new) {
            unregisterModel(field)

            field = new

            registerModel(field)
        }

    init {
        registerModel(model)
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point): Boolean = super.contains(point) && behavior?.contains(this, point) ?: true

    public abstract fun click()
}
