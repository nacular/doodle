package com.nectar.doodle.controls

import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.black
import com.nectar.doodle.drawing.Color.Companion.red
import com.nectar.doodle.drawing.Color.Companion.transparent
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.HsvColor
import com.nectar.doodle.drawing.LinearGradientBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Polygon
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.utils.HorizontalAlignment.Center
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import com.nectar.measured.units.Angle
import com.nectar.measured.units.Measure
import com.nectar.measured.units.degrees
import com.nectar.measured.units.div
import com.nectar.measured.units.times
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 1/9/19.
 */

private class ColorRect(color: Color): View() {
    private var hsvColor = HsvColor(color)
        set(new) {
            if (field == new) { return }

            field = new
            color = field.toRgb()
        }

    private val angle = 90 * degrees

    private var selection = 1f to 0f
        set(new) {
            field = min(1f, max(0f, new.first)) to min(1f, max(0f, new.second))

            val value      = 1f - field.second
            val saturation =      field.first

            hsvColor = HsvColor(hsvColor.hue, saturation, value)
        }

    var color = color
        set(new) {
            if (field == new) { return }

            val old   = field
            field     = new
            hsvColor  = HsvColor(field)
            baseColor = HsvColor(hsvColor.hue, 1f, 1f).toRgb()

            rerenderNow()

            (changed as PropertyObserversImpl).forEach { it(this, old, field) }
        }

    private var mousePressed = false

    init {
        mouseChanged += object: MouseListener {
            override fun mousePressed(event: MouseEvent) {
                selection = event.location.run { (x / width).toFloat() to (y / height).toFloat() }
                mousePressed = true
            }

            override fun mouseReleased(event: MouseEvent) {
                mousePressed = false
            }
        }

        mouseMotionChanged += object: MouseMotionListener {
            override fun mouseDragged(mouseEvent: MouseEvent) {
                if (mousePressed) {
                    selection = mouseEvent.location.run { (x / width).toFloat() to (y / height).toFloat() }
                }
            }
        }
    }

    private var baseColor = HsvColor(hsvColor.hue, 1f, 1f).toRgb()
        set(new) {
            if (field == new) { return }
            field = new
        }


    val changed: PropertyObservers<ColorRect, Color> by lazy { PropertyObserversImpl<ColorRect, Color>(this) }

    override fun render(canvas: Canvas) {
        bounds.atOrigin.let { rect ->
            canvas.rect(rect, ColorBrush(baseColor))
            canvas.rect(rect, LinearGradientBrush(white, transparent       ))
            canvas.rect(rect, LinearGradientBrush(black, transparent, angle))
        }

        canvas.circle(Circle(Point(selection.first * width, selection.second * height), 7.0), Pen(getContrastColor(color)))
    }
}

private class HueStrip(hue: Measure<Angle>): View() {
    private val angle = 90 * degrees
    private val brush = LinearGradientBrush(listOf(0, 60, 120, 180, 240, 300, 0).map { it * degrees }.mapIndexed { index, measure -> LinearGradientBrush.Stop(HsvColor(measure, 1f, 1f).toRgb(), index / 6f) }, angle)


    var hue = hue
        set(new) {
            if (field == new) { return }

            val old = hue
            field = new

            (changed as PropertyObserversImpl).forEach { it(this@HueStrip, old, hue) }
        }

    init {
        mouseChanged += object: MouseListener {
            override fun mousePressed(event: MouseEvent) {
                mousePressed      = true
                this@HueStrip.hue = (360 * (1 - event.location.y / height)) * degrees
            }

            override fun mouseReleased(event: MouseEvent) {
                mousePressed = false
            }
        }

        mouseMotionChanged += object: MouseMotionListener {
            override fun mouseDragged(mouseEvent: MouseEvent) {
                if (mousePressed) {
                    this@HueStrip.hue = (360 * (1 - min(1.0, max(0.0, mouseEvent.location.y / height)))) * degrees
                }
            }
        }
    }

    val changed: PropertyObservers<HueStrip, Measure<Angle>> by lazy { PropertyObserversImpl<HueStrip, Measure<Angle>>(this) }

    private var mousePressed = false

    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, brush)
    }
}

class Handle: View() {
    override fun render(canvas: Canvas) {
        val width  = this.width  - 4
        val height = this.height - 4

        canvas.shadow(blurRadius = 2.0) {
            poly(Polygon(
                    Point(2.0, height / 2),
                    Point(width / 2, 2.0),
                    Point(width, 2.0),
                    Point(width, height),
                    Point(width / 2, height)
            ), Pen(black), ColorBrush(white))
        }
    }
}

// TODO: Should this be centralized?
private fun getContrastColor(color: Color): Color {
    val y = (299u * color.red + 587u * color.green + 114u * color.blue) / 1000u
    return if (y >= 128u) black else white
}


class ColorPicker: View() {
    var color = red
        set(new) {
            colorRect.color = new
        }

    private val colorRect: ColorRect = ColorRect(color).apply {
        cursor = Cursor.Crosshair

        changed += { _,_,color ->
            hex.text            = "#${color.hexString}"
            hex.backgroundColor = color
            hex.foregroundColor = getContrastColor(color)
        }
    }

    private val hueStrip  = HueStrip(HsvColor(color).hue).apply {
        cursor = Cursor.Crosshair

        changed += { _,_,hue ->
            val color       = HsvColor(colorRect.color)
            colorRect.color = HsvColor(hue, color.saturation, color.value, color.opacity).toRgb()
            handle.y        = y + height * (1 - (hue / (360 * degrees))) - handle.height / 2
        }
    }
    private val handle: Handle = Handle     ().apply { y = hueStrip.y + hueStrip.height * (1 - (hueStrip.hue / (360 * degrees))) - height / 2 }
    private val hex            = TextField  ().apply {
        text  = "#${colorRect.color.hexString}"
        width = 100.0
        horizontalAlignment = Center
        backgroundColor = colorRect.color
        foregroundColor = getContrastColor(colorRect.color)
    }
//    private val colorSquare    = ColorSquare().apply { backgroundColor = colorRect.color; size = Size(40.0, 40.0) }

    init {
        children += colorRect
        children += hueStrip
        children += handle
        children += hex
//        children += colorSquare

        layout = constrain(colorRect, hueStrip, handle, hex) { colorRect, hueStrip, handle, hex ->
            val inset = 2

            colorRect.top    = hueStrip.top
            colorRect.left   = colorRect.parent.left   + inset
            colorRect.bottom = hueStrip.bottom
            colorRect.right  = hueStrip.left           - (colorRect.parent.height / 100.0 * inset)

            hueStrip.top     = hueStrip.parent.top     + handle.height / 2
            hueStrip.width   = hueStrip.parent.height / 4
            hueStrip.bottom  = hueStrip.parent.bottom - 35
            hueStrip.right   = hueStrip.parent.right - handle.width / 2

            handle.width     = hueStrip.width / 2.5
            handle.right     = handle.parent.right - inset
            handle.height    = handle.width * 12 / 15

            hex.top          = hueStrip.bottom +  (hex.parent.height / 100.0 * inset)
            hex.centerX      = colorRect.centerX
            hex.bottom       = hex.parent.bottom - inset
        }/*.constrain(hueStrip, handle) { hueStrip, handle ->
            handle.top = hueStrip.top + hueStrip.height * (1 - (this.hueStrip.hue / (360 * degrees))) - handle.height / 2
        }*/
    }

//    override fun render(canvas: Canvas) {
//        canvas.rect(bounds.atOrigin, ColorBrush(Color(0xecececu)))
//    }
}