package io.nacular.doodle.controls

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.Color.Companion.blackOrWhiteContrast
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.GradientPaint.Stop
import io.nacular.doodle.drawing.HsvColor
import io.nacular.doodle.drawing.LinearGradientPaint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.checkerPaint
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.toRgb
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.div
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.system.Cursor.Companion.Crosshair
import io.nacular.doodle.system.Cursor.Companion.Grab
import io.nacular.doodle.system.Cursor.Companion.Grabbing
import io.nacular.doodle.system.Cursor.Companion.None
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.div
import io.nacular.measured.units.times
import kotlin.math.max
import kotlin.math.min

/**
 * Control for selecting a [Color] from within a palette.
 *
 * @constructor
 * @param color to select by default
 */
public class ColorPicker(color: Color): View() {

    /** The selected color */
    public var color: Color
        get(   ) = colorRect.color.toRgb()
        set(new) {
            colorRect.color = HsvColor(new)
        }

    private val changed_ by lazy { PropertyObserversImpl<ColorPicker, Color>(this) }

    /** Notifies of changes to [color]. */
    public val changed: PropertyObservers<ColorPicker, Color> = changed_

    private class ColorRect(color: HsvColor): View() {
        var color = color
            set(new) {
                if (field == new) { return }

                val old = field
                field   = new

                baseColor = HsvColor(color.hue, 1f, 1f).toRgb()
                selection = color.saturation to 1f - color.value

                rerender()

                (changed as PropertyObserversImpl).invoke(old, new)
            }

        private var selection = color.saturation to 1f - color.value
            set(new) {
                if (field == new) return

                field = min(1f, max(0f, new.first)) to min(1f, max(0f, new.second))

                val value      = 1f - field.second
                val saturation =      field.first

                color = HsvColor(color.hue, saturation, value, color.opacity)
            }

        private var pointerPressed = false
            set(new) {
                field = new

                cursor = when (new) {
                    true -> None
                    else -> Crosshair
                }
            }

        init {
            cursor = Crosshair

            pointerChanged += object: PointerListener {
                override fun pressed(event: PointerEvent) {
                    selection      = event.location.run { (x / width).toFloat() to (y / height).toFloat() }
                    pointerPressed = true

                    event.consume()
                }

                override fun released(event: PointerEvent) {
                    pointerPressed = false
                }
            }

            pointerMotionChanged += object: PointerMotionListener {
                override fun dragged(event: PointerEvent) {
                    if (pointerPressed) {
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

        val changed: PropertyObservers<ColorRect, HsvColor> by lazy { PropertyObserversImpl(this) }

        override fun render(canvas: Canvas) {
            bounds.atOrigin.let { rect ->
                canvas.rect(rect, 3.0, LinearGradientPaint(White, baseColor,   Origin,            Point(rect.width, 0.0)))
                canvas.rect(rect, 3.0, LinearGradientPaint(Black, Transparent, Point(0.0, rect.height), Origin          ))
            }

            canvas.circle(Circle(Point(selection.first * width, selection.second * height), 7.0), Stroke(blackOrWhiteContrast(color.toRgb())))
        }
    }

    private open class Strip(ratio: Float): View() {
        private class Handle: View() {
            override fun render(canvas: Canvas) {
                val inset = 2.0

                canvas.outerShadow(blurRadius = 1.0) {
                    canvas.rect(bounds.atOrigin.inset(inset), (width - inset) / 4, ColorPaint(White))
                }
            }
        }

        private val handle: Handle = Handle().apply {
            suggestWidth(12.0)
        }

        var ratio = ratio; set(new) {
            if (field == new) { return }

            val old = field
            field = new

            handle.suggestX((width - handle.width) * field)

            changed_.forEach { it(this@Strip, old, field) }
        }

        final override var layout  : Layout?              get() = super.layout; set(new) { super.layout = new }
        final override val children: ObservableList<View> get() = super.children

        init {
            children += handle

            layout = constrain(handle) {
                it.left    eq min(parent.right.readOnly - handle.width, max(0.0, parent.width.readOnly * this@Strip.ratio - handle.width / 2))
                it.centerY eq parent.centerY
                it.height  eq parent.height
            }

            pointerChanged += object: PointerListener {
                override fun pressed(event: PointerEvent) {
                    pointerPressed   = true
                    this@Strip.ratio = (toLocal(event.location, from = event.target).x / width).toFloat()
                    cursor           = Grabbing
                    event.consume()
                }

                override fun entered(event: PointerEvent) {
                    if (pointerPressed) {
                        return
                    }

                    cursor = when (event.target) {
                        handle -> Grab
                        else   -> null
                    }
                }

                override fun released(event: PointerEvent) {
                    pointerPressed = false

                    cursor = when (event.target) {
                        handle -> Grab
                        else   -> null
                    }
                }
            }

            pointerMotionChanged += object: PointerMotionListener {
                override fun dragged(event: PointerEvent) {
                    if (pointerPressed) {
                        this@Strip.ratio = min(1f, max(0f, (toLocal(event.location, from = event.target).x / width).toFloat()))
                        event.consume()
                    }
                }
            }
        }

        protected val changed_: PropertyObserversImpl<Strip, Float> by lazy { PropertyObserversImpl(this) }

        private var pointerPressed = false
    }

    private class HueStrip(hue: Measure<Angle>): Strip((hue / (360 * degrees)).toFloat()) {
        private lateinit var fill: LinearGradientPaint

        var hue = hue; set(new) {
            if (field == new) { return }

            val old = field
            field = new

            ratio = (new / (360 * degrees)).toFloat()

            (changed as PropertyObserversImpl)(old, field)
        }

        init {
            changed_ += { _,_,new ->
                this@HueStrip.hue = new * 360 * degrees
            }

            boundsChanged += { _,_,_ ->
                updateFill()
            }

            updateFill()
        }

        val changed: PropertyObservers<HueStrip, Measure<Angle>> by lazy { PropertyObserversImpl(this) }

        override fun render(canvas: Canvas) {
            canvas.rect(bounds.atOrigin, min(width, height) / 5, fill)
        }

        private fun updateFill() {
            fill = LinearGradientPaint(
                listOf(0, 60, 120, 180, 240, 300, 0)
                    .map { it * degrees }
                    .mapIndexed { index, measure -> Stop(HsvColor(measure, 1f, 1f).toRgb(), index / 6f) },
                Origin,
                Point(width, 0.0)
            )
        }
    }

    private class OpacityStrip(color: Color): Strip(color.opacity) {
        private val checkerFill = checkerPaint(Size(32.0 / 2, 15.0 / 2), White, Lightgray)

        private lateinit var fill: LinearGradientPaint

        var color = color; set(new) {
            if (field == new) { return }
            field   = new
            updateFill()
            value = color.opacity

            rerender()
        }

        var value = color.opacity; set(new) {
            if (field == new) { return }

            val old = field
            field = new

            ratio = new

            (changed as PropertyObserversImpl)(old, field)
        }

        init {
            changed_ += { _,_,new ->
                value = new
            }

            boundsChanged += { _,_,_ ->
                updateFill()
            }
        }

        val changed: PropertyObservers<OpacityStrip, Float> by lazy { PropertyObserversImpl(this) }

        override fun render(canvas: Canvas) {
//            canvas.innerShadow {
            canvas.rect(bounds.atOrigin, min(width, height) / 5, checkerFill)
//            }

            canvas.rect(bounds.atOrigin, min(width, height) / 5, fill)
        }

        private fun updateFill() {
            fill = LinearGradientPaint(Transparent, color.opacity(1f), Origin, Point(width, 0.0))
        }
    }

    private class ColorSquare: View() {
        init {
            styleChanged += { rerender() }
        }

        override fun render(canvas: Canvas) {
            canvas.innerShadow(color = Color(0x808080u), blurRadius = 1.0) {
                if ((backgroundColor?.opacity ?: 0f) < 1f) {
                    canvas.rect(bounds.atOrigin, 3.0, checkerPaint(Size(width * 2 / 3, height * 2 / 3) / 2, Lightgray, White))
                }

                backgroundColor?.let {
                    canvas.rect(bounds.atOrigin, 3.0, ColorPaint(it))
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
        suggestHeight(15.0)

        changed += { _,_,hue ->
            colorRect.color = colorRect.color.run { HsvColor(hue, saturation, value, opacity) }
        }
    }

    private val opacityStrip = OpacityStrip(color).apply {
        suggestHeight(15.0)

        changed += { _,_,opacity ->
            colorRect.color = colorRect.color.opacity(opacity)
        }
    }

    private val colorSquare = ColorSquare().apply {
        backgroundColor = colorRect.color.toRgb()

        suggestSize(Size(hueStrip.prospectiveBounds.height + opacityStrip.prospectiveBounds.height + inset))
    }

    init {
        children += colorRect
        children += hueStrip
        children += opacityStrip
        children += colorSquare

        layout = constrain(colorRect, hueStrip, opacityStrip, colorSquare) { colorRect, hueStrip, opacityStrip, colorSquare ->
            colorRect.top        eq inset
            colorRect.left       eq inset
            colorRect.right      eq parent.right.readOnly  - inset
            colorRect.bottom     eq hueStrip.top  - inset

            hueStrip.left        eq inset
            hueStrip.height.preserve
            hueStrip.right       eq colorSquare.left - inset
            hueStrip.bottom      eq opacityStrip.top - inset

            opacityStrip.left    eq inset
            opacityStrip.height.preserve
            opacityStrip.right   eq colorSquare.left - inset
            opacityStrip.bottom  eq parent.bottom.readOnly    - inset

            colorSquare.width.preserve
            colorSquare.height.preserve
            colorSquare.right    eq parent.right.readOnly  - inset
            colorSquare.bottom   eq parent.bottom.readOnly - inset
        }
    }
}