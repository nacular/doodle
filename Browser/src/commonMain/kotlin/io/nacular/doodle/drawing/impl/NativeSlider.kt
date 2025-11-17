package io.nacular.doodle.drawing.impl

import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.controls.range.marks
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HTMLDataListElement
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Visible
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.clear
import io.nacular.doodle.dom.setBounds
import io.nacular.doodle.dom.setMargin
import io.nacular.doodle.dom.setOrientation
import io.nacular.doodle.dom.setOverflow
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.IdGenerator
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.Orientation.Vertical
import io.nacular.doodle.utils.observable
import kotlin.math.roundToInt

/**
 * Created by Nicholas Eddy on 11/20/18.
 */
internal interface SliderValueAdapter<T> where T: Comparable<T> {
    operator fun get(slider: Slider<T>              ): Float
    operator fun set(slider: Slider<T>, value: Float)
}

internal interface NativeSliderFactory {
    operator fun <T> invoke(slider: Slider<T>, showTicks: Boolean, adapter: SliderValueAdapter<T>): NativeSlider<T> where T: Comparable<T>
}

internal class NativeSliderFactoryImpl internal constructor(
    private val htmlFactory              : HtmlFactory,
    private val elementRuler             : ElementRuler,
    private val nativeEventHandlerFactory: NativeEventHandlerFactory,
    private val focusManager             : FocusManager?,
    private val idGenerator              : IdGenerator,
): NativeSliderFactory {

    class SizeInfo(val vertical: Size, val horizontal: Size)

    private val defaultSize = elementRuler.size(htmlFactory.createRangeInput())

    private val sizeDifference = elementRuler.size(htmlFactory.create<HTMLElement>().apply {
        add(htmlFactory.createRangeInput(Horizontal).apply { style.position = "initial" })
    }).let {
        Size(it.width - defaultSize.width, it.height - defaultSize.height)
    }

    override fun <T> invoke(slider: Slider<T>, showTicks: Boolean, adapter: SliderValueAdapter<T>) where T: Comparable<T> = NativeSlider(
        slider               = slider,
        adapter              = adapter,
        showTicks            = showTicks,
        marginSize           = sizeDifference,
        htmlFactory          = htmlFactory,
        idGenerator          = idGenerator,
        defaultSize          = defaultSize,
        focusManager         = focusManager,
        handlerFactory       = nativeEventHandlerFactory,
        defaultSizeWithTicks = defaultSizeWithTicks(showTicks),
    )

    private fun defaultSizeWithTicks(showTicks: Boolean) = if (showTicks) elementRuler.size(createTickedSlider()) else Size.Empty

    private fun createTickedSlider(orientation: Orientation = Horizontal) = htmlFactory.create<HTMLElement>().apply {
        style.display  = "inline-block"

        val sliderElement = htmlFactory.createRangeInput(orientation).apply { style.position = "initial" }

        appendChild(sliderElement)
        appendChild(htmlFactory.createDataList().apply {
            id = idGenerator.nextId()

            sliderElement.setAttribute("list", id)

            repeat(4) {
                appendChild(htmlFactory.createOption().apply { value = "${25 * it}" })
            }
        })
    }
}

internal class NativeSlider<T> internal constructor(
                htmlFactory         : HtmlFactory,
                handlerFactory      : NativeEventHandlerFactory,
    private val focusManager        : FocusManager?,
    private val defaultSize         : Size,
    private val defaultSizeWithTicks: Size,
    private val marginSize          : Size,
    private val slider              : Slider<T>,
    private val adapter             : SliderValueAdapter<T>,
    private val idGenerator         : IdGenerator,
    private val showTicks           : Boolean,
): NativeEventListener where T: Comparable<T> {

    private var oldSliderMarks  = emptyList<Float>()
    private val oldSliderHeight = slider.height

    private val nativeEventHandler: NativeEventHandler

    private val changed: (Slider<T>, T, T) -> Unit = { it,_,_ ->
        sliderElement.value = "${adapter[it] * 100}"
    }

    private val styleChanged: (View) -> Unit = {
        if (showTicks && oldSliderMarks != slider.marks.toList()) {
            oldSliderMarks = slider.marks.toList()
            when {
                oldSliderMarks.isNotEmpty() -> ticksElement = (ticksElement?.apply { clear() } ?: htmlFactory.createDataList()).apply {
                    id = idGenerator.nextId()

                    sliderElement.setAttribute("list", id)

                    oldSliderMarks.forEach {
                        appendChild(htmlFactory.createOption().apply { value = "${(it * 100).roundToInt()}" })
                    }

                    boundsChanged(slider, Empty, slider.bounds)
                    slider.rerender()
                }
                else -> ticksElement?.apply {
                    ticksElement = null
                    remove()
                    boundsChanged(slider, Empty, slider.bounds)
                    slider.rerender()
                }
            }
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
        // Explicitly assuming the relative dimensions of the slider don't change based on its orientation.
        // This is done b/c it is challenging to get the real dimensions of a native vertical slider

        val ticksPresent = ticksElement != null
        val widthRatio   = if (ticksPresent && defaultSizeWithTicks.width  > 0) defaultSize.width  / defaultSizeWithTicks.width  else 1.0
        val heightRatio  = if (ticksPresent && defaultSizeWithTicks.height > 0) defaultSize.height / defaultSizeWithTicks.height else 1.0

        when (slider.orientation) {
            Vertical -> sliderElement.style.setBounds(Rectangle(
                x      = 0.0,
                y      = new.height * (1 - widthRatio), // Explicitly shift slider down since it needs to be aligned to the bottom of it's parent
                width  = new.width  * heightRatio,
                height = new.height * widthRatio
            ))
            else     -> sliderElement.style.setBounds(Rectangle(new.width * widthRatio, new.height * heightRatio))
        }
    }

    private val sliderElement = htmlFactory.createRangeInput().apply {
        setOrientation   (slider.orientation)
        style.setMargin  (0.0               )
        style.setOverflow(Visible()         )
    }

    private var ticksElement: HTMLDataListElement? by observable(null) { old, new ->
        old?.remove()
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

            val sizeWithTicks = when (orientation) {
                Horizontal -> Size(defaultSizeWithTicks.width  + marginSize.width,  defaultSizeWithTicks.height + marginSize.height)
                Vertical   -> Size(defaultSizeWithTicks.height + marginSize.height, defaultSizeWithTicks.width  + marginSize.width )
            }

            preferredSize = { _,_ ->
                when {
                    ticksElement != null -> sizeWithTicks
                    else                 -> size
                }
            }
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

            ticksElement?.let { canvas.addData(listOf(it)) }

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

        sliderElement.value = "${slider.fraction * 100}".take(17)

        return true
    }

    internal val <T> ClosedRange<T>.size: Double where T: Number, T: Comparable<T> get() = (endInclusive.toDouble() - start.toDouble())
}