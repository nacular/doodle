package com.nectar.doodle.controls.buttons

import com.nectar.doodle.core.Icon
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.utils.Anchor
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
            model: ButtonModel  = ButtonModelImpl()): View() {


    private val modelFired: (ButtonModel) -> Unit = { fired_.forEach { it(this) } }

    init {
        model.fired += modelFired
    }

    override fun removedFromDisplay() {
        super.removedFromDisplay()

        model.fired -= modelFired
    }

    val textChanged: PropertyObservers<View, String> by lazy { PropertyObserversImpl<View, String>(this) }

    var text by ObservableProperty(text, { this }, textChanged as PropertyObserversImpl<View, String>)

    var behavior: Behavior<Button>? = null
        set(new) {
            if (field == new) { return }

            field?.uninstall(this)
            field = new?.apply { install(this@Button) }
        }

    var iconTextSpacing = 4.0
        set(new) { field = new; styleChanged() }

    var verticalAlignment = Middle
        set(new) { field = new; styleChanged() }

    var horizontalAlignment = Center
        set(new) { field = new; styleChanged() }

    var iconAnchor = Anchor.Left
        set(new) { field = new; styleChanged() }

    var pressedIcon: Icon<Button>? = null
        get() = field ?: icon
    var disabledIcon: Icon<Button>? = null
        get() = field ?: icon
    var selectedIcon: Icon<Button>? = null
        get() = field ?: icon
    var mouseOverIcon: Icon<Button>? = null
        get() = field ?: icon
    var disabledSelectedIcon: Icon<Button>? = null
        get() = field ?: disabledIcon
    var mouseOverSelectedIcon: Icon<Button>? = null
        get() = field ?: selectedIcon

    var selected: Boolean
        get(   ) = model.selected
        set(new) {
            model.selected = new
        }

    open var model: ButtonModel = model
        set(new) {
            field.apply {
                fired -= modelFired
            }

            field = new

            field.apply {
                fired += modelFired
            }
        }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point) = behavior?.contains(this, point) ?: super.contains(point)

    private val fired_ by lazy { ChangeObserversImpl(this) }

    val fired: ChangeObservers<Button> = fired_

    abstract fun click()
}
