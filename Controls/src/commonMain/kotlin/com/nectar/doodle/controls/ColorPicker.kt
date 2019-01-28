package com.nectar.doodle.controls

import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.CanvasBrush
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.black
import com.nectar.doodle.drawing.Color.Companion.blackOrWhiteContrast
import com.nectar.doodle.drawing.Color.Companion.lightgray
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
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.layout.max
import com.nectar.doodle.layout.min
import com.nectar.doodle.system.Cursor.Companion.Crosshair
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
class ColorPicker(color: Color): View() {
    var color
        get(   ) = colorRect.color.toRgb()
        set(new) {
            colorRect.color = HsvColor(new)
        }

    private class ColorRect(color: HsvColor): View() {
        var color = color
            set(new) {
                if (field == new) { return }

                val old = field
                field   = new

                baseColor = HsvColor(color.hue, 1f, 1f).toRgb()

                rerender()

                (changed as PropertyObserversImpl).forEach { it(this, old, new) }
            }

        private val angle = 90 * degrees

        private var selection = 1f to 0f
            set(new) {
                field = min(1f, max(0f, new.first)) to min(1f, max(0f, new.second))

                val value      = 1f - field.second
                val saturation =      field.first

                color = HsvColor(color.hue, saturation, value, color.opacity)
            }

        private var mousePressed = false

        init {
            cursor = Crosshair

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

            canvas.circle(Circle(Point(selection.first * width, selection.second * height), 7.0), Pen(blackOrWhiteContrast(color.toRgb())))
        }
    }

    private class HueStrip(hue: Measure<Angle>): View() {
        private val brush = LinearGradientBrush(listOf(0, 60, 120, 180, 240, 300, 0).map { it * degrees }.mapIndexed { index, measure -> LinearGradientBrush.Stop(HsvColor(measure, 1f, 1f).toRgb(), index / 6f) })

        private val handle: Handle = Handle().apply { width = 12.0 }

        var hue = hue
            set(new) {
                if (field == new) { return }

                val old = field
                field = new

                handle.x = (width - handle.width) * (field / (360 * degrees))

                (changed as PropertyObserversImpl).forEach { it(this@HueStrip, old, field) }
            }

        init {
            children += handle

            layout = constrain(handle) {
                it.left    = min(it.parent.right - handle.width, max(0.0, it.parent.left + it.parent.width * { (this.hue / (360 * degrees)) } - handle.width / 2))
                it.centerY = it.parent.centerY
                it.height  = it.parent.height
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
        private val checkerBrush = CanvasBrush(Size(30.0, 15.0)) {
            val w = 16.0
            val h = w / 2

            rect(Rectangle(0.0, 0.0, w, h), ColorBrush(white    ))
            rect(Rectangle(0.0,   h, w, h), ColorBrush(lightgray))
            rect(Rectangle(w,   0.0, w, h), ColorBrush(lightgray))
            rect(Rectangle(w,     h, w, h), ColorBrush(white    ))
        }

        private var brush = LinearGradientBrush(transparent, color.with(1f))

        private val handle: Handle = Handle().apply { width = 12.0 }

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
            children += handle

            layout = constrain(handle) {
                it.left    = min(it.parent.right - handle.width, max(0.0, it.parent.left + it.parent.width * { this.opacity } - handle.width / 2))
                it.centerY = it.parent.centerY
                it.height  = it.parent.height
            }

            mouseChanged += object: MouseListener {
                override fun mousePressed(event: MouseEvent) {
                    mousePressed = true
                    opacity      = (event.location.x / width).toFloat()
                }

                override fun mouseReleased(event: MouseEvent) {
                    mousePressed = false
                }
            }

            mouseMotionChanged += object: MouseMotionListener {
                override fun mouseDragged(mouseEvent: MouseEvent) {
                    if (mousePressed) {
                        opacity = min(1f, max(0f, (mouseEvent.location.x / width).toFloat()))
                    }
                }
            }
        }

        val changed: PropertyObservers<OpacityStrip, Float> by lazy { PropertyObserversImpl<OpacityStrip, Float>(this) }

        private var mousePressed = false

        override fun render(canvas: Canvas) {
//            canvas.innerShadow {
            canvas.rect(bounds.atOrigin, min(width, height) / 5, checkerBrush)
//            }

            canvas.rect(bounds.atOrigin, min(width, height) / 5, brush)
        }
    }

    private class Handle: View() {
        override fun render(canvas: Canvas) {
            val inset = 2.0

            canvas.outerShadow(blurRadius = inset) {
                canvas.rect(bounds.atOrigin.inset(inset), (width - inset) / 4, ColorBrush(white))
            }
        }
    }

    private class ColorSquare: View() {
        init {
            styleChanged += { rerender() }
        }

        override fun render(canvas: Canvas) {
            canvas.innerShadow(color = Color(0x808080u), blurRadius = 1.0) {
                if (backgroundColor?.opacity ?: 0f < 1f) {
                    val brushSize = Size(width * 2 / 3, height * 2 / 3)
                    val checkerBrush = CanvasBrush(brushSize) {
                        val w = brushSize.width / 2
                        val h = brushSize.height / 2

                        rect(Rectangle(0.0, 0.0, w, h), ColorBrush(lightgray))
                        rect(Rectangle(0.0, h,   w, h), ColorBrush(white    ))
                        rect(Rectangle(w,   0.0, w, h), ColorBrush(white    ))
                        rect(Rectangle(w,   h,   w, h), ColorBrush(lightgray))
                    }

                    canvas.rect(bounds.atOrigin, 3.0, checkerBrush)
                }

                backgroundColor?.let {
                    canvas.rect(bounds.atOrigin, 3.0, ColorBrush(it))
                }
            }
        }
    }

    private val inset = 4.0

    private val colorRect: ColorRect = ColorRect(HsvColor(color)).apply {
        changed += { _,_,color ->
            color.toRgb().let {
                opacityStrip.color          = it
                colorSquare.backgroundColor = it
            }
        }
    }

    private val hueStrip = HueStrip(HsvColor(color).hue).apply {
        height = 15.0

        changed += { _,_,hue ->
            colorRect.color = colorRect.color.run { HsvColor(hue, saturation, value, opacity) }
        }
    }

    private val opacityStrip = OpacityStrip(color).apply {
        height = 15.0

        changed += { _,_,opacity ->
            colorRect.color = colorRect.color.with(opacity)
        }
    }

    private val colorSquare = ColorSquare().apply { backgroundColor = colorRect.color.toRgb(); size = Size(hueStrip.height + opacityStrip.height + inset) }

    init {
        children += colorRect
        children += hueStrip
        children += opacityStrip
        children += colorSquare

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
        }
    }
}