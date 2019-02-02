package com.nectar.doodle.drawing.impl

import com.nectar.doodle.controls.Slider
import com.nectar.doodle.core.View
import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Size
import org.w3c.dom.HTMLElement

/**
 * Created by Nicholas Eddy on 11/20/18.
 */
interface NativeSliderFactory {
    operator fun invoke(slider: Slider): NativeSlider
}

class NativeSliderFactoryImpl internal constructor(
        private val htmlFactory              : HtmlFactory,
        private val elementRuler             : ElementRuler,
        private val nativeEventHandlerFactory: NativeEventHandlerFactory,
        private val focusManager             : FocusManager?): NativeSliderFactory {

    private val sizeDifference: Size by lazy {
        val slider = htmlFactory.createInput().apply {
            style.position = "initial"
            type           = "range"
        }

        val holder = htmlFactory.create<HTMLElement>().apply {
            style.position  = "initial"
            style.overflowX = "initial"
            style.overflowY = "initial"
            style.display   = "inline-block"

            add(slider)
        }

        elementRuler.size(holder).let {
            Size(it.width - defaultSize.width, it.height - defaultSize.height)
        }
    }

    private val defaultSize: Size by lazy {
        val slider = htmlFactory.createInput().apply {
            type = "range"
        }

        elementRuler.size(slider)
    }

    override fun invoke(slider: Slider) = NativeSlider(htmlFactory, nativeEventHandlerFactory, focusManager, defaultSize, sizeDifference, slider)
}

class NativeSlider internal constructor(
                    htmlFactory   : HtmlFactory,
                    handlerFactory: NativeEventHandlerFactory,
        private val focusManager  : FocusManager?,
        private val defaultSize   : Size,
        private val marginSize    : Size,
        private val slider        : Slider): NativeEventListener {

//    private val glassPanelElement : HTMLElement
    private val nativeEventHandler: NativeEventHandler

    private val sliderElement = htmlFactory.createInput().apply {
        style.setWidthPercent (100.0)
        style.setHeightPercent(100.0)
        type         = "range"
        value        = slider.value.toString()
//        style.cursor = "inherit"
    }

    private val changed: (Slider, Double, Double) -> Unit = { it,_,_ ->
        sliderElement.value = it.value.toString()
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

    init {
        nativeEventHandler = handlerFactory(sliderElement, this).apply {
            registerFocusListener ()
            registerClickListener ()
            registerChangeListener()
        }

//        glassPanelElement = htmlFactory.create<HTMLElement>().apply {
//            style.setTop            (0.0      )
//            style.setLeft           (0.0      )
//            style.setOpacity        (0f       )
//            style.setPosition       (Position.Absolute)
//            style.setBoxSizing      (BoxSizing.Border)
//            style.setWidthPercent   (100.0    )
//            style.setHeightPercent  (100.0    )
//            style.setBackgroundColor(Color.red)
//
//            sliderElement.add(this)
//        }

        slider.apply {
            height               = this@NativeSlider.defaultSize.height
            changed             += this@NativeSlider.changed
            focusChanged        += this@NativeSlider.focusChanged
            enabledChanged      += this@NativeSlider.enabledChanged
            focusabilityChanged += this@NativeSlider.focusableChanged
        }
    }

    fun discard() {
        slider.apply {
            changed             -= this@NativeSlider.changed
            focusChanged        -= this@NativeSlider.focusChanged
            enabledChanged      -= this@NativeSlider.enabledChanged
            focusabilityChanged -= this@NativeSlider.focusableChanged
        }
    }

    fun render(canvas: Canvas) {
        if (canvas is CanvasImpl) {

            canvas.addData(listOf(sliderElement))

            if (slider.hasFocus) {
                sliderElement.focus()
            }
        }
    }

//    fun propertyChanged(aEvent: PropertyEvent) {
//        if (aEvent.getProperty() === Slider.ICON_ANCHOR) {
//            button.rerender()
//        }
//    }

//    override fun onClick(): Boolean {
//        button.click()
//        return true
//    }

    override fun onFocusGained(): Boolean {
        if (!slider.focusable) {
            return false
        }

        focusManager?.requestFocus(slider)

        return true
    }

    override fun onFocusLost(): Boolean {
        if (slider === focusManager?.focusOwner) {
            focusManager.clearFocus()
        }

        return true
    }

    override fun onChange(): Boolean {
        sliderElement.value.toDoubleOrNull()?.let {
            slider.value = it
        }

        return true
    }
}