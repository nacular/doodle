package com.nectar.doodle.controls.buttons

import com.nectar.doodle.accessibility.button
import com.nectar.doodle.core.Icon
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.utils.Anchor.Left
import com.nectar.doodle.utils.ChangeObservers
import com.nectar.doodle.utils.ChangeObserversImpl
import com.nectar.doodle.utils.HorizontalAlignment.Center
import com.nectar.doodle.utils.ObservableProperty
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import com.nectar.doodle.utils.VerticalAlignment.Middle

/**
 * Created by Nicholas Eddy on 11/10/17.
 */

@Suppress("PrivatePropertyName")
abstract class Button protected constructor(
            text: String        = "",
        var icon: Icon<Button>? = null,
            model: ButtonModel  = ButtonModelImpl()): View(accessibilityRole = button()) {

    private val armedChanged_       = { _: ButtonModel, old: Boolean, new: Boolean -> (armedChanged       as PropertyObserversImpl)(old, new) }
    private val pressedChanged_     = { _: ButtonModel, old: Boolean, new: Boolean -> (pressedChanged     as PropertyObserversImpl)(old, new) }
    private val pointerOverChanged_ = { _: ButtonModel, old: Boolean, new: Boolean -> (pointerOverChanged as PropertyObserversImpl)(old, new) }
    private val modelFired          = { _: ButtonModel -> (fired as ChangeObserversImpl).forEach { it(this) } }

    override fun addedToDisplay() {
        super.addedToDisplay()

        model.fired += modelFired
    }

    override fun removedFromDisplay() {
        super.removedFromDisplay()

        model.fired -= modelFired
    }

    val textChanged: PropertyObservers<Button, String> by lazy { PropertyObserversImpl<Button, String>(this) }

    var text by ObservableProperty(text, { this }, textChanged as PropertyObserversImpl<Button, String>)

    val fired: ChangeObservers<Button> by lazy { ChangeObserversImpl(this) }

    val armedChanged    : PropertyObservers<Button, Boolean> by lazy { PropertyObserversImpl<Button, Boolean>(this) }
    val pressedChanged  : PropertyObservers<Button, Boolean> by lazy { PropertyObserversImpl<Button, Boolean>(this) }
    val pointerOverChanged: PropertyObservers<Button, Boolean> by lazy { PropertyObserversImpl<Button, Boolean>(this) }

    var behavior: Behavior<Button>? = null
        set(new) {
            if (field == new) { return }

            clipCanvasToBounds = true
            field?.uninstall(this)

            field = new?.also {
                it.install(this)
                clipCanvasToBounds = it.clipCanvasToBounds(this)
            }
        }

    var iconTextSpacing = 4.0; set(new) { field = new; styleChanged() }

    var verticalAlignment = Middle; set(new) { field = new; styleChanged() }

    var horizontalAlignment = Center; set(new) { field = new; styleChanged() }

    var iconAnchor = Left; set(new) { field = new; styleChanged() }

    var pressedIcon            : Icon<Button>? = null; get() = field ?: icon
    var disabledIcon           : Icon<Button>? = null; get() = field ?: icon
    var selectedIcon           : Icon<Button>? = null; get() = field ?: icon
    var pointerOverIcon        : Icon<Button>? = null; get() = field ?: icon
    var disabledSelectedIcon   : Icon<Button>? = null; get() = field ?: disabledIcon
    var pointerOverSelectedIcon: Icon<Button>? = null; get() = field ?: selectedIcon

    var selected: Boolean
        get(   ) = model.selected
        set(new) {
            if (enabled) {
                model.selected = new
            }
        }

    open var model: ButtonModel = model
        set(new) {
            field.fired -= modelFired

            field = new

            if (displayed) {
                field.fired += modelFired
            }
        }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point) = super.contains(point) && behavior?.contains(this, point) ?: true

    abstract fun click()
}
