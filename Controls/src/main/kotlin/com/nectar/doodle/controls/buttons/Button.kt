package com.nectar.doodle.controls.buttons

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Icon
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.system.SystemMouseEvent.Button.Button1
import com.nectar.doodle.system.SystemMouseEvent.Type.Down
import com.nectar.doodle.system.SystemMouseEvent.Type.Enter
import com.nectar.doodle.system.SystemMouseEvent.Type.Exit
import com.nectar.doodle.system.SystemMouseEvent.Type.Up
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.EventObservers
import com.nectar.doodle.utils.EventObserversImpl
import com.nectar.doodle.utils.HorizontalAlignment
import com.nectar.doodle.utils.ObservableProperty
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import com.nectar.doodle.utils.VerticalAlignment

/**
 * Created by Nicholas Eddy on 11/10/17.
 */
@Suppress("PrivatePropertyName")
abstract class Button protected constructor(
            text: String        = "",
        var icon: Icon<Button>? = null,
            model: ButtonModel  = ButtonModelImpl()): Gizmo() {


    private val onActionFun: (ButtonModel) -> Unit = { onAction_.forEach { it(this) } }


    init {
        model.apply {
            onAction += onActionFun
        }
    }

    val textChanged: PropertyObservers<Gizmo, String> by lazy { PropertyObserversImpl<Gizmo, String>(mutableSetOf()) }

    var text: String by ObservableProperty(text, { this }, textChanged as PropertyObserversImpl<Gizmo, String>)

    var renderer: Renderer<Button>? = null

    var iconTextSpacing     = 4.0
    var verticalAlignment   = VerticalAlignment.Center
    var horizontalAlignment = HorizontalAlignment.Center

//    var iconAnchor: Anchor?
//        get() = mIconAnchor
//        set(aIconAnchor) {
//            setProperty(object : AbstractNamedProperty<Anchor>(ICON_ANCHOR) {
//                var value: Anchor?
//                    get() = this@Button.mIconAnchor
//                    set(aValue) {
//                        this@Button.mIconAnchor = aValue
//                    }
//            },
//                    aIconAnchor)
//        }

    var pressedIcon          : Icon<Button>? = null
    var disabledIcon         : Icon<Button>? = null
    var selectedIcon         : Icon<Button>? = null
    var mouseOverIcon        : Icon<Button>? = null
    var disabledSelectedIcon : Icon<Button>? = null
    var mouseOverSelectedIcon: Icon<Button>? = null

    var selected: Boolean
        get(   ) = model.selected
        set(new) {
            model.selected = new
        }

    open var model: ButtonModel = model
        set(new) {
            field.apply {
                onAction -= onActionFun
            }

            field = new

            field.apply {
                onAction += onActionFun
            }
        }

    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    override fun contains(point: Point) = renderer?.contains(this, point) ?: super.contains(point)

    private val onAction_ by lazy { EventObserversImpl(this, mutableSetOf()) }

    val onAction: EventObservers<Button> = onAction_

    abstract fun click()

    override fun handleMouseEvent(event: MouseEvent) {
        super.handleMouseEvent(event)

        when (event.type) {
            Up    -> mouseReleased(event)
            Down  -> mousePressed (event)
            Exit  -> mouseExited  (event)
            Enter -> mouseEntered (event)
            else  -> return
        }
    }

    private fun mouseEntered(event: MouseEvent) {
        model.mouseOver = true

        if (enabled) {
            if (event.buttons == setOf(Button1) && model.pressed) {
                model.armed = true
            }
        }
    }

    private fun mouseExited(@Suppress("UNUSED_PARAMETER") event: MouseEvent) {
        model.mouseOver = false

        if (enabled) {
            model.armed = false
        }
    }

    private fun mousePressed(event: MouseEvent) {
        if (enabled && event.buttons == setOf(Button1)) {
            model.armed   = true
            model.pressed = true
        }
    }

    private fun mouseReleased(@Suppress("UNUSED_PARAMETER") event: MouseEvent) {
        if (enabled) {
            model.pressed = false
            model.armed   = false
        }
    }
}
