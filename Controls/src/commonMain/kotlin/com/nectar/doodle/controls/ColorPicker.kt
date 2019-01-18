package com.nectar.doodle.controls

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
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Polygon
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import com.nectar.measured.units.Angle
import com.nectar.measured.units.Measure
import com.nectar.measured.units.degrees
import com.nectar.measured.units.times

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
            field = new

            val value      = 1f - field.second
            val saturation =      field.first

            hsvColor = HsvColor(hsvColor.hue, saturation, value)

            println(color)
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

    init {
        mouseChanged += object: MouseListener {
            override fun mousePressed(event: MouseEvent) {
                selection = event.location.run { (x / width).toFloat() to (y / height).toFloat() }
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

        canvas.circle(Circle(Point(selection.first * width, selection.second * height), 5.0), Pen(getContrastColor(color)))
    }

    // TODO: Should this be centralized?
    private fun getContrastColor(color: Color): Color {
        val y = (299u * color.red + 587u * color.green + 114u * color.blue) / 1000u
        return if (y >= 128u) black else white
    }
}

private class HueStrip(var hue: Measure<Angle>): View() {
    private val angle = 90 * degrees
    private val brush = LinearGradientBrush(listOf(0, 60, 120, 180, 240, 300, 0).map { it * degrees }.mapIndexed { index, measure -> LinearGradientBrush.Stop(HsvColor(measure, 1f, 1f).toRgb(), index / 6f) }, angle)

    init {
        mouseChanged += object: MouseListener {
            override fun mousePressed(event: MouseEvent) {
                val old = hue
                hue = (360 * (1 - event.location.y / height)) * degrees

                (changed as PropertyObserversImpl).forEach { it(this@HueStrip, old, hue) }
            }
        }
    }

    val changed: PropertyObservers<HueStrip, Measure<Angle>> by lazy { PropertyObserversImpl<HueStrip, Measure<Angle>>(this) }

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
            ), ColorBrush(white))
        }
    }
}

class ColorPicker: View() {
    var color = red
        set(new) {
            colorRect.color = new
        }

    private val colorRect = ColorRect(color)
    private val hueStrip = HueStrip(HsvColor(color).hue).apply {
        changed += { _,_,hue ->
            val color       = HsvColor(colorRect.color)
            colorRect.color = HsvColor(hue, color.saturation, color.value, color.opacity).toRgb()
        }
    }
    private val handle = Handle()

    init {
        children += colorRect
        children += hueStrip
        children += handle

        layout = constrain(colorRect, hueStrip, handle) { colorRect, hueStrip, handle ->
            colorRect.top    = colorRect.parent.top    + 2
            colorRect.left   = colorRect.parent.left   + 2
            colorRect.bottom = colorRect.parent.bottom - 2
            colorRect.right  = hueStrip.left           - 4

            hueStrip.top     = hueStrip.parent.top    +  2
            hueStrip.left    = hueStrip.parent.right  - (colorRect.bottom - colorRect.top) / 3 - 30
            hueStrip.bottom  = hueStrip.parent.bottom -  2
            hueStrip.right   = hueStrip.parent.right  -  30

            handle.width     = hueStrip.width / 2
            handle.height    = handle.width * 12 / 15
            handle.left      = hueStrip.right - handle.width * 0.75
        }
    }
}