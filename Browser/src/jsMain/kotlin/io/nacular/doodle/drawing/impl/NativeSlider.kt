package io.nacular.doodle.drawing.impl

import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Overflow.Visible
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.setBounds
import io.nacular.doodle.dom.setOverflow
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import org.w3c.dom.HTMLElement
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 11/20/18.
 */
internal interface NativeSliderFactory {
    operator fun <T> invoke(slider: Slider<T>, valueSetter: (Slider<T>, Double) -> Unit): NativeSlider<T> where T: Number, T: Comparable<T>
}

internal class NativeSliderFactoryImpl internal constructor(
        private val htmlFactory              : HtmlFactory,
        private val elementRuler             : ElementRuler,
        private val nativeEventHandlerFactory: NativeEventHandlerFactory,
        private val focusManager             : FocusManager?
): NativeSliderFactory {

    private val sizeDifference: Size by lazy {
        val slider = htmlFactory.createInput().apply {
            style.position = "initial"
            type           = "range"
        }

        val holder = htmlFactory.create<HTMLElement>().apply {
            add(slider)
        }

        elementRuler.size(holder).let {
            Size(it.width - defaultSize.width, it.height - defaultSize.height)
        }
    }

    private val defaultSize: Size by lazy {
        elementRuler.size(htmlFactory.createInput().apply {
            type = "range"
        })
    }

    override fun <T> invoke(slider: Slider<T>, valueSetter: (Slider<T>, Double) -> Unit) where T: Number, T: Comparable<T> = NativeSlider(
            htmlFactory,
            nativeEventHandlerFactory,
            focusManager,
            defaultSize,
            sizeDifference,
            slider,
            valueSetter
    )
}

internal class NativeSlider<T> internal constructor(
                    htmlFactory   : HtmlFactory,
                    handlerFactory: NativeEventHandlerFactory,
        private val focusManager  : FocusManager?,
        private val defaultSize   : Size,
        private val marginSize    : Size,
        private val slider        : Slider<T>,
        private val valueSetter   : (Slider<T>, Double) -> Unit
): NativeEventListener where T: Number, T: Comparable<T> {

    private val oldSliderHeight = slider.height

    private val nativeEventHandler: NativeEventHandler

    private val sliderElement = htmlFactory.createInput().apply {
        type  = "range"
        step  = "any"
        value = slider.value.toString()

        style.setOverflow(Visible())
    }

    private val changed: (Slider<T>, T, T) -> Unit = { it,_,_ ->
        sliderElement.value = "${it.value.toDouble() / slider.model.limits.size * 100}"
    }

    private val focusChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        when (new) {
            true -> sliderElement.focus()
            else -> sliderElement.blur ()
        }
    }

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        sliderElement.disabled = !new
    }

    private val focusableChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        sliderElement.tabIndex = if (new) -1 else 0
    }

    private val boundsChanged: (View, Rectangle, Rectangle) -> Unit = { _,_,new ->
        val width  = max(0.0, slider.width  - marginSize.width )
        val height = max(0.0, slider.height - marginSize.height)

        sliderElement.style.setBounds(Rectangle((new.width - width) / 2, (new.height - height) / 2, width, height))
    }

    init {
        nativeEventHandler = handlerFactory(sliderElement, this).apply {
            registerFocusListener()
            registerInputListener()
        }

        slider.apply {
            changed             += this@NativeSlider.changed
            focusChanged        += this@NativeSlider.focusChanged
            boundsChanged       += this@NativeSlider.boundsChanged
            enabledChanged      += this@NativeSlider.enabledChanged
            focusabilityChanged += this@NativeSlider.focusableChanged

            changed      (this, 0.0 as T,        value )
            boundsChanged(this, Rectangle.Empty, bounds)

            height = this@NativeSlider.defaultSize.height + this@NativeSlider.marginSize.height
        }
    }

    fun discard() {
        nativeEventHandler.apply {
            unregisterFocusListener()
            unregisterInputListener()
        }

        slider.apply {
            changed             -= this@NativeSlider.changed
            focusChanged        -= this@NativeSlider.focusChanged
            boundsChanged       -= this@NativeSlider.boundsChanged
            enabledChanged      -= this@NativeSlider.enabledChanged
            focusabilityChanged -= this@NativeSlider.focusableChanged

            height = oldSliderHeight
        }
    }

    fun render(canvas: Canvas) {
        if (canvas is NativeCanvas) {

            canvas.addData(listOf(sliderElement))

            if (slider.hasFocus) {
                sliderElement.focus()
            }
        }
    }

    override fun onFocusGained(event: Event): Boolean {
        if (!slider.focusable) {
            return false
        }

        focusManager?.requestFocus(slider)

        return true
    }

    override fun onFocusLost(event: Event): Boolean {
        if (slider === focusManager?.focusOwner) {
            focusManager.clearFocus()
        }

        return true
    }

    override fun onInput(event: Event): Boolean {
        sliderElement.value.toDoubleOrNull()?.let {
            valueSetter(slider, slider.model.limits.start.toDouble() + (it / 100 * slider.model.limits.size))
        }

        return true
    }

    internal val <T> ClosedRange<T>.size: Double where T: Number, T: Comparable<T> get() = (endInclusive.toDouble() - start.toDouble() + 1)
}