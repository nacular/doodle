package io.nacular.doodle.drawing.impl

import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.core.View
import io.nacular.doodle.core.View.Companion.fixed
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Visible
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.setBounds
import io.nacular.doodle.dom.setMargin
import io.nacular.doodle.dom.setOrientation
import io.nacular.doodle.dom.setOverflow
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.Orientation.Vertical

/**
 * Created by Nicholas Eddy on 11/20/18.
 */
internal interface SliderValueAdapter<T> where T: Comparable<T> {
    operator fun get(slider: Slider<T>              ): Float
    operator fun set(slider: Slider<T>, value: Float)
}

internal interface NativeSliderFactory {
    operator fun <T> invoke(slider: Slider<T>, adapter: SliderValueAdapter<T>): NativeSlider<T> where T: Comparable<T>
}

internal class NativeSliderFactoryImpl internal constructor(
    private val htmlFactory              : HtmlFactory,
    private val elementRuler             : ElementRuler,
    private val nativeEventHandlerFactory: NativeEventHandlerFactory,
    private val focusManager             : FocusManager?
): NativeSliderFactory {
    override fun <T> invoke(slider: Slider<T>, adapter: SliderValueAdapter<T>) where T: Comparable<T> = NativeSlider(
            htmlFactory,
            nativeEventHandlerFactory,
            focusManager,
            defaultSize,
            sizeDifference,
            slider,
            adapter
    )

    private val defaultSize = elementRuler.size(htmlFactory.createInput().apply {
        type = "range"
    })

    private val sizeDifference: Size = elementRuler.size(htmlFactory.create<HTMLElement>().apply {
        add(htmlFactory.createInput().apply {
            style.position = "initial"
            type           = "range"
        })
    }).let {
        Size(it.width - defaultSize.width, it.height - defaultSize.height)
    }
}

internal class NativeSlider<T> internal constructor(
                htmlFactory   : HtmlFactory,
                handlerFactory: NativeEventHandlerFactory,
    private val focusManager  : FocusManager?,
    private val defaultSize   : Size,
    private val marginSize    : Size,
    private val slider        : Slider<T>,
    private val adapter       : SliderValueAdapter<T>
): NativeEventListener where T: Comparable<T> {

    private val oldSliderHeight = slider.height

    private val nativeEventHandler: NativeEventHandler

    private val changed: (Slider<T>, T, T) -> Unit = { it,_,_ ->
        sliderElement.value = "${adapter[it] * 100}"
    }

    private val styleChanged: (View) -> Unit = {
        sliderElement.step = when {
            slider.snapToTicks && slider.ticks > 1 -> "${100.0 / (slider.ticks - 1)}".take(17) // Hack to avoid rounding issue that prevents final step (i.e. 100.0 / 3)
            else                                   -> "any"
        }
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
        sliderElement.style.setBounds(Rectangle(size = new.size))
    }

    private val sliderElement = htmlFactory.createInput().apply {
        type = "range"
        setOrientation   (slider.orientation)
        style.setMargin  (0.0               )
        style.setOverflow(Visible()         )
    }

    init {
        nativeEventHandler = handlerFactory(sliderElement, this).apply {
            registerFocusListener()
            registerInputListener()
        }

        slider.apply {
            changed             += this@NativeSlider.changed
            styleChanged        += this@NativeSlider.styleChanged
            focusChanged        += this@NativeSlider.focusChanged
            boundsChanged       += this@NativeSlider.boundsChanged
            enabledChanged      += this@NativeSlider.enabledChanged
            focusabilityChanged += this@NativeSlider.focusableChanged

            changed      (this, value, value )
            styleChanged (this               )
            boundsChanged(this, Empty, bounds)

            val size = when (orientation) {
                Horizontal -> Size(defaultSize.width  + marginSize.width,  defaultSize.height + marginSize.height)
                Vertical   -> Size(defaultSize.height + marginSize.height, defaultSize.width  + marginSize.width )
            }

            preferredSize = fixed(size)
        }
    }

    fun discard() {
        nativeEventHandler.apply {
            unregisterFocusListener()
            unregisterInputListener()
        }

        slider.apply {
            changed             -= this@NativeSlider.changed
            styleChanged        -= this@NativeSlider.styleChanged
            focusChanged        -= this@NativeSlider.focusChanged
            boundsChanged       -= this@NativeSlider.boundsChanged
            enabledChanged      -= this@NativeSlider.enabledChanged
            focusabilityChanged -= this@NativeSlider.focusableChanged

            suggestHeight(oldSliderHeight)
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
            adapter[slider] = (it / 100).toFloat()
        }

        return true
    }

    internal val <T> ClosedRange<T>.size: Double where T: Number, T: Comparable<T> get() = (endInclusive.toDouble() - start.toDouble())
}