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
import io.nacular.doodle.theme.native.NativeTheme.WindowDiscovery
import io.nacular.doodle.utils.Orientation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.skia.FontMgr
import java.awt.Dimension
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseEvent
import java.awt.event.MouseEvent.MOUSE_DRAGGED
import java.awt.event.MouseEvent.MOUSE_MOVED
import javax.swing.BoundedRangeModel
import javax.swing.JSlider
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import kotlin.coroutines.CoroutineContext
import kotlin.math.pow

internal class NativeSliderBehavior<T>(
    private val appScope                 : CoroutineScope,
    private val uiDispatcher             : CoroutineContext,
    private val window                   : WindowDiscovery,
    private val fontManager              : FontMgr,
    private val swingGraphicsFactory     : SwingGraphicsFactory,
    private val focusManager             : FocusManager?,
    private val nativePointerPreprocessor: NativePointerPreprocessor?
): SliderBehavior<T> where T: Comparable<T> {

    private interface SliderValueAdapter<T> where T: Comparable<T> {
        operator fun get(slider: Slider<T>              ): Float
        operator fun set(slider: Slider<T>, value: Float)
    }

    private inner class ModelAdapter<T>(
            private val delegate : Slider<T>,
                        precision: Int,
            private val adapter  : SliderValueAdapter<T>
    ): BoundedRangeModel where T: Comparable<T> {
        private val listeners      = mutableListOf<ChangeListener>()
        private val multiplier     = 10.0.pow(precision.toDouble())
        private var valueAdjusting = false

        fun notifyChanged() {
            val event = ChangeEvent(this)

            listeners.forEach {
                it.stateChanged(event)
            }
        }

        override fun getMinimum() = 0

        override fun setMinimum(newMinimum: Int) {
            // no-op
        }

        override fun getMaximum() = multiplier.toInt()

        override fun setMaximum(newMaximum: Int) {
            // no-op
        }

        override fun getValue() = (adapter[delegate] * multiplier).toInt()

        override fun setValue(newValue: Int) {
            adapter[delegate] = (newValue / multiplier).toFloat()
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

    private fun adapter(): SliderValueAdapter<T> = object: SliderValueAdapter<T> {
        override fun get(slider: Slider<T>) = slider.fraction

        override fun set(slider: Slider<T>, value: Float) {
            slider.setFraction(value)
        }
    }

    private open inner class JSliderAdapter<T>(
            slider   : Slider<T>,
            precision: Int = 2,
            adapter: SliderValueAdapter<T>,
        val model    : ModelAdapter<T> = ModelAdapter(slider, precision, adapter)
    ): JSlider(model) where T: Comparable<T> {
        init {
            this.orientation = when (slider.orientation) {
                Orientation.Vertical -> VERTICAL
                else                 -> HORIZONTAL
            }
        }

        fun notifyChange() {
            model.notifyChanged()
        }
    }

    private inner class JSliderPeer(slider: Slider<T>): JSliderAdapter<T>(slider, adapter = adapter()) {
        private val slider: Slider<T>? = slider

        init {
            focusTraversalKeysEnabled = false

            addFocusListener(object: FocusListener {
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

        public fun handleMouseEvent(e: MouseEvent) = when (e.id) {
            MOUSE_MOVED, MOUSE_DRAGGED -> super.processMouseMotionEvent(e)
            else                       -> super.processMouseEvent(e)
        }
    }

    private lateinit var nativePeer   : JSliderPeer
    private          var oldCursor    : Cursor? = null
    private          var oldIdealSize = Size.Empty

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

    private val changeListener      : (Slider<T>, T,              T             ) -> Unit = { _,_,_ -> nativePeer.notifyChange() }
    private val limitsChangeListener: (Slider<T>, ClosedRange<T>, ClosedRange<T>) -> Unit = { _,_,_ -> nativePeer.notifyChange() }

    override fun render(view: Slider<T>, canvas: Canvas) {
        nativePeer.paint(swingGraphicsFactory(fontManager, (canvas as CanvasImpl).skiaCanvas))
    }

    override fun install(view: Slider<T>) {
        super.install(view)

        nativePeer = JSliderPeer(view)

        nativePointerPreprocessor?.set(view, object: NativePointerHandler {
            override fun invoke(event: PointerEvent) {
                if (event.source == view) {
                    nativePeer.handleMouseEvent(event.toAwt(nativePeer))
                }
            }
        })

        view.apply {
            focusChanged        += this@NativeSliderBehavior.focusChanged
            boundsChanged       += this@NativeSliderBehavior.boundsChanged
            enabledChanged      += this@NativeSliderBehavior.enabledChanged
            focusabilityChanged += this@NativeSliderBehavior.focusableChanged
            changed             += this@NativeSliderBehavior.changeListener
            limitsChanged       += this@NativeSliderBehavior.limitsChangeListener
        }

        appScope.launch(uiDispatcher) {
            nativePeer.size = view.size.run { Dimension(view.width.toInt(), view.height.toInt()) }

            view.apply {
                cursor    = Default
                idealSize = nativePeer.preferredSize.run { Size(width, height) }
            }

            window.frameFor(view)?.add(nativePeer)

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
            changed             -= this@NativeSliderBehavior.changeListener
            limitsChanged       -= this@NativeSliderBehavior.limitsChangeListener
        }

        appScope.launch(uiDispatcher) {
            window.frameFor(view)?.remove(nativePeer)
        }
    }
}