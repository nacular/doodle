package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.controls.theme.range.SliderBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.impl.CanvasImpl
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.Cursor.Companion.Default
import io.nacular.doodle.utils.Orientation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.Dimension
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseEvent
import javax.swing.BoundedRangeModel
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.event.ChangeListener
import kotlin.coroutines.CoroutineContext
import kotlin.math.pow

internal class NativeSliderBehavior<T>(
        private val appScope                 : CoroutineScope,
        private val uiDispatcher             : CoroutineContext,
        private val window                   : JPanel,
        private val swingGraphicsFactory     : SwingGraphicsFactory,
        private val focusManager             : FocusManager?,
        private val nativePointerPreprocessor: NativePointerPreprocessor?
): SliderBehavior<T> where T: Number, T: Comparable<T> {

    private inner class ModelAdapter<T>(
            private val delegate: Slider<T>,
            precision: Int,
            private val setValue: (Slider<T>, Double) -> Unit,
            private val setRange: (Slider<T>, ClosedRange<Double>) -> Unit
    ): BoundedRangeModel where T: Number, T: Comparable<T> {
        private val listeners = mutableListOf<ChangeListener>()
        private var valueAdjusting = false
        private val multiplier = 10.0.pow(precision.toDouble())

        override fun getMinimum() = (delegate.range.start.toDouble() * multiplier).toInt()

        override fun setMinimum(newMinimum: Int) {
            setRange(delegate, newMinimum / multiplier .. delegate.range.endInclusive.toDouble())
        }

        override fun getMaximum() = (delegate.range.endInclusive.toDouble() * multiplier).toInt()

        override fun setMaximum(newMaximum: Int) {
            setRange(delegate, delegate.range.start.toDouble() .. newMaximum / multiplier)
        }

        override fun getValue() = (delegate.value.toDouble() * multiplier).toInt()

        override fun setValue(newValue: Int) {
            setValue(delegate, newValue / multiplier)
        }

        override fun setValueIsAdjusting(b: Boolean) {
            valueAdjusting = b
        }

        override fun getValueIsAdjusting() = valueAdjusting

        override fun getExtent() = 0

        override fun setExtent(newExtent: Int) {
            // no-op
        }

        override fun setRangeProperties(value: Int, extent: Int, min: Int, max: Int, adjusting: Boolean) {
            minimum = min
            maximum = max
            setValue (value )
            setExtent(extent)
            valueAdjusting = adjusting
        }

        override fun addChangeListener(listener: ChangeListener) {
            listeners += listener
        }

        override fun removeChangeListener(listener: ChangeListener) {
            listeners -= listener
        }
    }

    private open inner class DoubleSlider<T>(
            slider: Slider<T>,
            precision: Int = 2,
            setValue: (Slider<T>, Double) -> Unit,
            setRange: (Slider<T>, ClosedRange<Double>) -> Unit
    ): JSlider(ModelAdapter(slider, precision, setValue, setRange)) where T: Number, T: Comparable<T> {
        init {
            this.orientation = when (slider.orientation) {
                Orientation.Vertical -> VERTICAL
                else                 -> HORIZONTAL
            }
        }
    }

    private inner class JSliderPeer(slider: Slider<T>): DoubleSlider<T>(slider, setValue = { s,d -> s.set(d) }, setRange = { s,r -> s.set(r) }) {
        private val slider: Slider<T>? = slider

        init {
            focusTraversalKeysEnabled = false

            addFocusListener(object : FocusListener {
                override fun focusGained(e: FocusEvent?) {
                    focusManager?.requestFocus(slider)
                }

                override fun focusLost(e: FocusEvent?) {
                    focusManager?.clearFocus()
                }
            })
        }

        override fun repaint(tm: Long, x: Int, y: Int, width: Int, height: Int) {
            slider?.rerender()
        }

        public override fun processMouseEvent(e: MouseEvent?) {
            super.processMouseEvent(e)
        }
    }

    private lateinit var nativePeer   : JSliderPeer
    private          var oldCursor    : Cursor? = null
    private          var oldIdealSize : Size?   = null

    private val focusChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        when (new) {
            true -> nativePeer.requestFocusInWindow()
            else -> nativePeer.transferFocus       ()
        }
    }

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        nativePeer.isEnabled = new
    }

    private val focusableChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        nativePeer.isFocusable = new
    }

    private val boundsChanged: (View, Rectangle, Rectangle) -> Unit = { _,_,new ->
        nativePeer.size = new.size.run { Dimension(width.toInt(), height.toInt()) }
    }

    override fun render(view: Slider<T>, canvas: Canvas) {
        nativePeer.paint(swingGraphicsFactory((canvas as CanvasImpl).skiaCanvas))
    }

    override fun install(view: Slider<T>) {
        super.install(view)

        nativePeer = JSliderPeer(view)

        nativePointerPreprocessor?.set(view, object: NativeMouseHandler {
            override fun invoke(event: PointerEvent) {
                nativePeer.processMouseEvent(event.toAwt(nativePeer))

//                if (event.type == Down || event.type == Drag) {
//                    event.consume()
//                }
            }
        })

        view.apply {
            focusChanged        += this@NativeSliderBehavior.focusChanged
            boundsChanged       += this@NativeSliderBehavior.boundsChanged
            enabledChanged      += this@NativeSliderBehavior.enabledChanged
            focusabilityChanged += this@NativeSliderBehavior.focusableChanged
        }

        appScope.launch(uiDispatcher) {
            nativePeer.size = view.size.run { Dimension(view.width.toInt(), view.height.toInt()) }

            view.apply {
                cursor    = Default
                idealSize = nativePeer.preferredSize.run { Size(width, height) }
            }

            window.add(nativePeer)

            if (view.hasFocus) {
                nativePeer.requestFocusInWindow()
            }
        }
    }

    override fun uninstall(view: Slider<T>) {
        super.uninstall(view)

        view.apply {
            cursor    = oldCursor
            idealSize = oldIdealSize

            focusChanged        -= this@NativeSliderBehavior.focusChanged
            boundsChanged       -= this@NativeSliderBehavior.boundsChanged
            enabledChanged      -= this@NativeSliderBehavior.enabledChanged
            focusabilityChanged -= this@NativeSliderBehavior.focusableChanged
        }

        appScope.launch(uiDispatcher) {
            window.remove(nativePeer)
        }
    }
}