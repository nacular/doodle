package com.nectar.doodle.controls

import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.black
import com.nectar.doodle.drawing.Color.Companion.blackOrWhiteContrast
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.Color.Companion.transparent
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.HsvColor
import com.nectar.doodle.drawing.LinearGradientBrush
import com.nectar.doodle.drawing.LinearGradientBrush.Stop
import com.nectar.doodle.drawing.PatternBrush
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
import com.nectar.doodle.system.Cursor.Companion.Grab
import com.nectar.doodle.system.Cursor.Companion.Grabbing
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

    private val changed_ by lazy { PropertyObserversImpl<ColorPicker, Color>(this) }

    val changed: PropertyObservers<ColorPicker, Color> = changed_

    private class ColorRect(color: HsvColor): View() {
        var color = color
            set(new) {
                if (field == new) { return }

                val old = field
                field   = new

                baseColor = HsvColor(color.hue, 1f, 1f).toRgb()
                selection = color.saturation to 1f - color.value

                rerender()

                (changed as PropertyObserversImpl).forEach { it(this, old, new) }
            }

        private val angle = 90 * degrees

        private var selection = 1f to 0f
            set(new) {
                if (field == new) return

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
                    selection    = event.location.run { (x / width).toFloat() to (y / height).toFloat() }
                    mousePressed = true

                    event.consume()
                }

                override fun mouseReleased(event: MouseEvent) {
                    mousePressed = false
                }
            }

            mouseMotionChanged += object: MouseMotionListener {
                override fun mouseDragged(event: MouseEvent) {
                    if (mousePressed) {
                        selection = event.location.run { (x / width).toFloat() to (y / height).toFloat() }

                        event.consume()
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
                canvas.rect(rect, 3.0, LinearGradientBrush(white, baseColor,   Point.Origin,            Point(rect.width, 0.0)))
                canvas.rect(rect, 3.0, LinearGradientBrush(black, transparent, Point(0.0, rect.height), Point.Origin          ))
            }

            canvas.circle(Circle(Point(selection.first * width, selection.second * height), 7.0), Pen(blackOrWhiteContrast(color.toRgb())))
        }
    }

    private open class Strip(ratio: Float): View() {
        private class Handle: View() {
            override fun render(canvas: Canvas) {
                val inset = 2.0

                canvas.outerShadow(blurRadius = inset) {
                    canvas.rect(bounds.atOrigin.inset(inset), (width - inset) / 4, ColorBrush(white))
                }
            }
        }

        private val handle: Handle = Handle().apply { width = 12.0 }

        var ratio = ratio
            set(new) {
                if (field == new) { return }

                val old = field
                field = new

                handle.x = (width - handle.width) * field

                changed_.forEach { it(this@Strip, old, field) }
            }

        init {
            children += handle

            layout = constrain(handle) {
                it.left    = min(it.parent.right - handle.width, max(0.0, it.parent.left + it.parent.width * { this.ratio } - handle.width / 2))
                it.centerY = it.parent.centerY
                it.height  = it.parent.height
            }

            mouseChanged += object: MouseListener {
                override fun mousePressed(event: MouseEvent) {
                    mousePressed     = true
                    this@Strip.ratio = (toLocal(event.location, from = event.target).x / width).toFloat()
                    cursor           = Grabbing
                    event.consume()
                }

                override fun mouseEntered(event: MouseEvent) {
                    if (mousePressed) {
                        return
                    }

                    cursor = when (event.target) {
                        handle -> Grab
                        else   -> null
                    }
                }

                override fun mouseReleased(event: MouseEvent) {
                    mousePressed = false

                    cursor = when (event.target) {
                        handle -> Grab
                        else   -> null
                    }
                }
            }

            mouseMotionChanged += object: MouseMotionListener {
                override fun mouseDragged(event: MouseEvent) {
                    if (mousePressed) {
                        this@Strip.ratio = min(1f, max(0f, (toLocal(event.location, from = event.target).x / width).toFloat()))
                        event.consume()
                    }
                }
            }
        }

        protected val changed_: PropertyObserversImpl<Strip, Float> by lazy { PropertyObserversImpl<Strip, Float>(this) }

        private var mousePressed = false
    }

    private class HueStrip(hue: Measure<Angle>): Strip((hue / (360 * degrees)).toFloat()) {
        private lateinit var brush: LinearGradientBrush

        var hue = hue
            set(new) {
                if (field == new) { return }

                val old = field
                field = new

                ratio = (new / (360 * degrees)).toFloat()

                (changed as PropertyObserversImpl).forEach { it(this@HueStrip, old, field) }
            }

        init {
            changed_ += { _,_,new ->
                this@HueStrip.hue = new * 360 * degrees
            }

            boundsChanged += { _,_,_ ->
                updateBrush()
            }

            updateBrush()
        }

        val changed: PropertyObservers<HueStrip, Measure<Angle>> by lazy { PropertyObserversImpl<HueStrip, Measure<Angle>>(this) }

        override fun render(canvas: Canvas) {
            canvas.rect(bounds.atOrigin, min(width, height) / 5, brush)
        }

        private fun updateBrush() {
            brush = LinearGradientBrush(
                    listOf(0, 60, 120, 180, 240, 300, 0).map { it * degrees }.mapIndexed { index, measure -> Stop(HsvColor(measure, 1f, 1f).toRgb(), index / 6f) },
                    Point.Origin, Point(width, 0.0)
            )
        }
    }

    private class OpacityStrip(color: Color): Strip(color.opacity) {
        private val checkerBrush = PatternBrush(Size(32.0, 15.0)) {
            val w         = 32.0 / 2
            val h         = 15.0 / 2
            val white     = ColorBrush(white    )
            val lightGray = ColorBrush(lightgray)

            rect(Rectangle(0.0, 0.0, w, h), white    )
            rect(Rectangle(0.0,   h, w, h), lightGray)
            rect(Rectangle(w,   0.0, w, h), lightGray)
            rect(Rectangle(w,     h, w, h), white    )
        }

        private lateinit var brush: LinearGradientBrush

        var color = color
            set(new) {
                if (field == new) { return }
                field   = new
                updateBrush()
                opacity = color.opacity

                rerender()
            }

        var opacity = color.opacity
            set(new) {
                if (field == new) { return }

                val old = field
                field = new

                ratio = new

                (changed as PropertyObserversImpl).forEach { it(this@OpacityStrip, old, field) }
            }

        init {
            changed_ += { _,_,new ->
                opacity = new
            }

            boundsChanged += { _,_,_ ->
                updateBrush()
            }
        }

        val changed: PropertyObservers<OpacityStrip, Float> by lazy { PropertyObserversImpl<OpacityStrip, Float>(this) }

        override fun render(canvas: Canvas) {
//            canvas.innerShadow {
            canvas.rect(bounds.atOrigin, min(width, height) / 5, checkerBrush)
//            }

            canvas.rect(bounds.atOrigin, min(width, height) / 5, brush)
        }

        private fun updateBrush() {
            brush = LinearGradientBrush(transparent, color.with(1f), Point.Origin, Point(width, 0.0))
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
                    val checkerBrush = PatternBrush(brushSize) {
                        val w = brushSize.width  / 2
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
        changed += { _,old,new ->
            new.toRgb().let {
                opacityStrip.color          = it
                hueStrip.hue                = new.hue
                colorSquare.backgroundColor = it

                changed_(old.toRgb(), it)
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