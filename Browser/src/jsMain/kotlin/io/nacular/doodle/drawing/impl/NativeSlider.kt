package io.nacular.doodle.drawing.impl

import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Overflow.Visible
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.setBounds
import io.nacular.doodle.dom.setOverflow
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.focus.NativeFocusManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.size
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.EventTarget
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 11/20/18.
 */
internal interface NativeSliderFactory {
    operator fun invoke(slider: Slider): NativeSlider
}

internal class NativeSliderFactoryImpl internal constructor(
        private val htmlFactory              : HtmlFactory,
        private val elementRuler             : ElementRuler,
        private val nativeEventHandlerFactory: NativeEventHandlerFactory,
        private val focusManager             : FocusManager?,
        private val nativeFocusManager       : NativeFocusManager?
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

    override fun invoke(slider: Slider) = NativeSlider(
            htmlFactory,
            nativeEventHandlerFactory,
            focusManager,
            nativeFocusManager,
            defaultSize,
            sizeDifference,
            slider
    )
}

internal class NativeSlider internal constructor(
                    htmlFactory       : HtmlFactory,
                    handlerFactory    : NativeEventHandlerFactory,
        private val focusManager      : FocusManager?,
        private val nativeFocusManager: NativeFocusManager?,
        private val defaultSize       : Size,
        private val marginSize        : Size,
        private val slider            : Slider
): NativeEventListener {

    private val oldSliderHeight = slider.height

    private val nativeEventHandler: NativeEventHandler

    private val sliderElement = htmlFactory.createInput().apply {
        type  = "range"
        step  = "any"
        value = slider.value.toString()

        style.setOverflow(Visible())
    }

    private val changed: (Slider, Double, Double) -> Unit = { it,_,_ ->
        sliderElement.value = "${it.value / slider.model.limits.size * 100}"
    }

    private val focusChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        when (new) {
            true -> sliderElement.focus()
            else -> sliderElement.blur ()
        }

        nativeFocusManager?.hasFocusOwner = new
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
            registerClickListener()
            registerInputListener()
        }

        slider.apply {
            changed             += this@NativeSlider.changed
            focusChanged        += this@NativeSlider.focusChanged
            boundsChanged       += this@NativeSlider.boundsChanged
            enabledChanged      += this@NativeSlider.enabledChanged
            focusabilityChanged += this@NativeSlider.focusableChanged

            changed      (this, 0.0,             value )
            boundsChanged(this, Rectangle.Empty, bounds)

            height = this@NativeSlider.defaultSize.height + this@NativeSlider.marginSize.height
        }
    }

    fun discard() {
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

    override fun onFocusGained(target: EventTarget?): Boolean {
        if (!slider.focusable) {
            return false
        }

        focusManager?.requestFocus(slider)

        return true
    }

    override fun onFocusLost(target: EventTarget?): Boolean {
        if (slider === focusManager?.focusOwner) {
            focusManager.clearFocus()
        }

        return true
    }

    override fun onInput(target: EventTarget?): Boolean {
        sliderElement.value.toDoubleOrNull()?.let {
            slider.value = slider.model.limits.start + (it / 100 * slider.model.limits.size)
        }

        return true
    }
}