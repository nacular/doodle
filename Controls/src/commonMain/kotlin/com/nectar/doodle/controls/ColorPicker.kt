package com.nectar.doodle.controls

import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.black
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.Color.Companion.transparent
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.HsvColor
import com.nectar.doodle.drawing.ImageBrush
import com.nectar.doodle.drawing.LinearGradientBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
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


private class ColorRect(color: HsvColor): View() {
    private val angle = 90 * degrees

    private var selection = 1f to 0f
        set(new) {
            field = min(1f, max(0f, new.first)) to min(1f, max(0f, new.second))

            val value      = 1f - field.second
            val saturation =      field.first

            color = HsvColor(color.hue, saturation, value, color.opacity)
        }

    var color = color
        set(new) {
            if (field == new) { return }

            val old = field
            field   = new

            baseColor = HsvColor(color.hue, 1f, 1f).toRgb()

            rerender()

            (changed as PropertyObserversImpl).forEach { it(this, old, new) }
        }

    private var mousePressed = false

    init {
        cursor = Cursor.Crosshair

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

    private var baseColor = HsvColor(color.hue, 1f, 1f).toRgb()
        set(new) {
            if (field == new) { return }
            field = new
        }

    val changed: PropertyObservers<ColorRect, HsvColor> by lazy { PropertyObserversImpl<ColorRect, HsvColor>(this) }

    override fun render(canvas: Canvas) {
        bounds.atOrigin.let { rect ->
            canvas.rect(rect, 3.0, ColorBrush(baseColor))
            canvas.rect(rect, 3.0, LinearGradientBrush(white, transparent       ))
            canvas.rect(rect, 3.0, LinearGradientBrush(black, transparent, angle))
        }

        canvas.circle(Circle(Point(selection.first * width, selection.second * height), 7.0), Pen(getContrastColor(color.toRgb())))
    }
}

private class HueStrip(hue: Measure<Angle>): View() {
    private val brush = LinearGradientBrush(listOf(0, 60, 120, 180, 240, 300, 0).map { it * degrees }.mapIndexed { index, measure -> LinearGradientBrush.Stop(HsvColor(measure, 1f, 1f).toRgb(), index / 6f) })

    private val handle: Handle = Handle().apply { x = (this@HueStrip.width - width) * (hue / (360 * degrees)); width = 8.0 + 4.0 }

    var hue = hue
        set(new) {
            if (field == new) { return }

            val old = field
            field = new

            handle.x = (width - handle.width) * (field / (360 * degrees))

            (changed as PropertyObserversImpl).forEach { it(this@HueStrip, old, field) }
        }

    init {
        cursor = Cursor.Crosshair

        children += handle

        layout = constrain(handle) {
            it.left    = it.parent.left + (it.parent.width - handle.width) * { (this.hue / (360 * degrees)) }
            it.centerY = it.parent.centerY
            it.height  = it.parent.height //- 2 * 2.0
        }

        mouseChanged += object: MouseListener {
            override fun mousePressed(event: MouseEvent) {
                mousePressed      = true
                this@HueStrip.hue = (360 * event.location.x / width) * degrees
            }

            override fun mouseReleased(event: MouseEvent) {
                mousePressed = false
            }
        }

        mouseMotionChanged += object: MouseMotionListener {
            override fun mouseDragged(mouseEvent: MouseEvent) {
                if (mousePressed) {
                    this@HueStrip.hue = (360 * min(1.0, max(0.0, mouseEvent.location.x / width))) * degrees
                }
            }
        }
    }

    val changed: PropertyObservers<HueStrip, Measure<Angle>> by lazy { PropertyObserversImpl<HueStrip, Measure<Angle>>(this) }

    private var mousePressed = false

    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, min(width, height) / 5, brush)
    }
}

private class OpacityStrip(color: Color): View() {
    private val checkerBrush = ImageBrush(Size(30.0, 15.0)) {
        val w = 15.0
        val h = w / 2

        rect(Rectangle(0.0, 0.0, w, h), Pen(white    ), ColorBrush(white    ))
        rect(Rectangle(0.0,   h, w, h), Pen(lightgray), ColorBrush(lightgray))
        rect(Rectangle(w,   0.0, w, h), Pen(lightgray), ColorBrush(lightgray))
        rect(Rectangle(w,     h, w, h), Pen(white    ), ColorBrush(white    ))
    }

    private var brush = LinearGradientBrush(transparent, color.with(1f))

    private val handle: Handle = Handle().apply { x = (this@OpacityStrip.width - width) * opacity; width = 8.0 + 4.0 }

    var color = color
        set(new) {
            if (field == new) { return }
            field = new
            brush = LinearGradientBrush(transparent, new.with(1f))

            rerender()
        }

    var opacity = color.opacity
        set(new) {
            if (field == new) { return }

            val old = field
            field = new

            handle.x = (width - handle.width) * field

            (changed as PropertyObserversImpl).forEach { it(this@OpacityStrip, old, field) }
        }

    init {
        cursor = Cursor.Crosshair

        children += handle

        layout = constrain(handle) {
            it.left    = it.parent.left + (it.parent.width - handle.width) * { this.opacity }
            it.centerY = it.parent.centerY
            it.height  = it.parent.height //- 2 * 2.0
        }

        mouseChanged += object: MouseListener {
            override fun mousePressed(event: MouseEvent) {
                mousePressed              = true
                this@OpacityStrip.opacity = (event.location.x / width).toFloat()
            }

            override fun mouseReleased(event: MouseEvent) {
                mousePressed = false
            }
        }

        mouseMotionChanged += object: MouseMotionListener {
            override fun mouseDragged(mouseEvent: MouseEvent) {
                if (mousePressed) {
                    this@OpacityStrip.opacity = min(1f, max(0f, (mouseEvent.location.x / width).toFloat()))
                }
            }
        }
    }

    val changed: PropertyObservers<OpacityStrip, Float> by lazy { PropertyObserversImpl<OpacityStrip, Float>(this) }

    private var mousePressed = false

    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, min(width, height) / 5, checkerBrush)

        canvas.rect(bounds.atOrigin, min(width, height) / 5, brush)
    }
}

class Handle: View() {
    override fun render(canvas: Canvas) {
        val inset = 2.0

        canvas.shadow(blurRadius = inset) {
            canvas.rect(bounds.atOrigin.inset(inset), (width - inset) / 4, ColorBrush(white))
        }
    }
}

// TODO: Should this be centralized?
private fun getContrastColor(color: Color): Color {
    val y = (299u * color.red + 587u * color.green + 114u * color.blue) / 1000u
    return if (y >= 128u) black else white
}

private class ColorSquare: View() {
    init {
        styleChanged += {
            rerender()
        }
    }

    override fun render(canvas: Canvas) {
        val brushSize    = Size(width * 2/3, height * 2/3)
        val checkerBrush = ImageBrush(brushSize) {
            val w = brushSize.width  / 2
            val h = brushSize.height / 2

            rect(Rectangle(0.0, 0.0, w, h), Pen(white    ), ColorBrush(white    ))
            rect(Rectangle(0.0,   h, w, h), Pen(lightgray), ColorBrush(lightgray))
            rect(Rectangle(w,   0.0, w, h), Pen(lightgray), ColorBrush(lightgray))
            rect(Rectangle(w,     h, w, h), Pen(white    ), ColorBrush(white    ))
        }

        canvas.rect(bounds.atOrigin, 3.0, checkerBrush)

        backgroundColor?.let {
            canvas.rect(bounds.atOrigin, 3.0, /*Pen(lightgray),*/ ColorBrush(it))
        }
    }
}

class ColorPicker(color: Color): View() {
    var color = color
        set(new) {
            colorRect.color = HsvColor(new)
        }

    private val inset = 4.0

    private val colorRect: ColorRect = ColorRect(HsvColor(color)).apply {
        changed += { _,_,color ->
            val rgb = color.toRgb()
            hex.text                    = "#${rgb.hexString}"
            opacityStrip.color          = rgb
            colorSquare.backgroundColor = rgb
//            hex.backgroundColor = color
//            hex.foregroundColor = getContrastColor(color)
        }
    }

    private val hueStrip = HueStrip(HsvColor(color).hue).apply {
        height = 15.0

        changed += { _,_,hue ->
            val color       = colorRect.color
            colorRect.color = HsvColor(hue, color.saturation, color.value, color.opacity)
        }
    }

    private val opacityStrip = OpacityStrip(color).apply {
        height = 15.0

        changed += { _,_,opacity ->
            colorRect.color = colorRect.color.with(opacity)
        }
    }

    private val hex = TextField().apply {
        val rgbColor = colorRect.color.toRgb()

        text                = "#${rgbColor.hexString}"
        width               = 100.0
        horizontalAlignment = Center
        backgroundColor     = rgbColor
        foregroundColor     = getContrastColor(rgbColor)
    }

    private val colorSquare = ColorSquare().apply { backgroundColor = colorRect.color.toRgb(); size = Size(hueStrip.height + opacityStrip.height + inset) }

    init {
        children += colorRect
        children += hueStrip
        children += opacityStrip
        children += colorSquare
//        children += hex

        layout = constrain(colorRect, hueStrip, opacityStrip, colorSquare) { colorRect, hueStrip, opacityStrip, colorSquare ->
            colorRect.top    = colorRect.parent.top    + inset
            colorRect.left   = colorRect.parent.left   + inset
            colorRect.right  = colorRect.parent.right  - inset
            colorRect.bottom = hueStrip.top            - inset

            hueStrip.left    = hueStrip.parent.left   + inset
            hueStrip.right   = colorSquare.left       - inset
            hueStrip.bottom  = opacityStrip.top       - inset

            opacityStrip.left    = hueStrip.parent.left   + inset
            opacityStrip.right   = colorSquare.left       - inset
            opacityStrip.bottom  = hueStrip.parent.bottom - inset

            colorSquare.right    = colorSquare.parent.right  - inset
            colorSquare.bottom   = colorSquare.parent.bottom - inset

//            hex.top          = hueStrip.bottom +  (hex.parent.height / 100.0 * inset)
//            hex.centerX      = colorRect.centerX
//            hex.bottom       = hex.parent.bottom - inset
        }
    }

//    override fun render(canvas: Canvas) {
//        canvas.rect(bounds.atOrigin, ColorBrush(Color(0xecececu)))
//    }
}