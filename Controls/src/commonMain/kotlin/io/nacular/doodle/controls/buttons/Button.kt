package io.nacular.doodle.controls.buttons

import io.nacular.doodle.accessibility.ButtonRole
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
 * Base for Buttons, which a "clickable" controls that notify listeners when their state changes.
 *
 * @author Nicholas Edd
 */
@Suppress("PrivatePropertyName")
public abstract class Button protected constructor(
            text : String        = "",
            icon : Icon<Button>? = null,
            model: ButtonModel   = ButtonModelImpl(),
            role : ButtonRole    = ButtonRole()): View(accessibilityRole = role) {

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

    /** Notifies when the button's text is modified */
    public val textChanged: PropertyObservers<Button, String> by lazy { PropertyObserversImpl(this) }

    /** Text displayed on the button */
    public var text: String by observable(text, textChanged as PropertyObserversImpl<Button, String>) { _,new ->
        if (accessibilityLabel == null) {
            accessibilityLabel = new
        }
    }

    /** Notifies whenever the button is clicked (pressed/released) */
    public val fired: ChangeObservers<Button> by lazy { ChangeObserversImpl(this) }

    public val armedChanged      : PropertyObservers<Button, Boolean> by lazy { PropertyObserversImpl(this) }
    public val pressedChanged    : PropertyObservers<Button, Boolean> by lazy { PropertyObserversImpl(this) }
    public val pointerOverChanged: PropertyObservers<Button, Boolean> by lazy { PropertyObserversImpl(this) }

    /** All rendering is delegated to the behavior */
    public var behavior: Behavior<Button>? by behavior()

    /** Space between icons and [text] */
    public var iconTextSpacing: Double by styleProperty(4.0) { true }

    /** How icons and [text] are positioned vertically in the button */
    public var verticalAlignment: VerticalAlignment by styleProperty(Middle) { true }

    /** How icons and [text] are positioned horizontally in the button */
    public var horizontalAlignment: HorizontalAlignment by styleProperty(Center) { true }

    /** How icons are positioned horizontally relative to [text] */
    public var iconAnchor: Anchor by styleProperty(Left) { true }

    /** Default icon shown if a state-specific one is not installed */
    public var icon: Icon<Button>? by styleProperty(icon) { true }

    /** Icon when button pressed */
    public var pressedIcon: Icon<Button>? = null; get() = field ?: icon;         set(new) { field = new; styleChanged { true } }

    /** Icon when [enabled] == `false` */
    public var disabledIcon: Icon<Button>? = null; get() = field ?: icon;         set(new) { field = new; styleChanged { true } }

    /** Icon when [selected] == `true` */
    public var selectedIcon: Icon<Button>? = null; get() = field ?: icon;         set(new) { field = new; styleChanged { true } }

    /** Icon when a Pointer is over the button */
    public var pointerOverIcon        : Icon<Button>? = null; get() = field ?: icon;         set(new) { field = new; styleChanged { true } }

    /** Icon when [enabled] == `false` and [selected] == `true` */
    public var disabledSelectedIcon   : Icon<Button>? = null; get() = field ?: disabledIcon; set(new) { field = new; styleChanged { true } }

    /** Icon when a Pointer is over the button and [selected] == `true` */
    public var pointerOverSelectedIcon: Icon<Button>? = null; get() = field ?: selectedIcon; set(new) { field = new; styleChanged { true } }

    /** `true` if the button is in the "on" state. */
    public var selected: Boolean
        get(   ) = model.selected
        set(new) {
            if (enabled) {
                model.selected = new
            }
        }

    /** Stores the button's state and notifies listeners when it changes. */
    public open var model: ButtonModel by observable(model) { old, new ->
        unregisterModel(old)
        registerModel  (new)
    }

    init {
        enabledChanged += { _,_,_ ->
            if (!enabled) {
                // Disarm model w/o causing it to fire
                model.armed       = false
                model.pressed     = false
                model.pointerOver = false // FIXME: Should PointerManager deliver exit events when View disabled?
            }
        }

        registerModel(model)

        accessibilityLabel = text
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point): Boolean = super.contains(point) && behavior?.contains(this, point) ?: true

    /**
     * Causes the button to "press/release". This triggers the [model] to notify listeners of [ButtonModel.fired].
     */
    public abstract fun click()
}
