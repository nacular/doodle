package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.ConfinedValueModel
import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.impl.CanvasImpl
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.Cursor.Companion.Default
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.SystemPointerEvent.Type
import io.nacular.doodle.system.SystemPointerEvent.Type.*
import io.nacular.doodle.utils.Orientation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.skiko.SkiaWindow
import java.awt.Dimension
import java.awt.Event.MOUSE_DOWN
import java.awt.Event.MOUSE_DRAG
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseEvent
import javax.swing.BoundedRangeModel
import javax.swing.JSlider
import javax.swing.event.ChangeListener
import kotlin.coroutines.CoroutineContext
import kotlin.math.pow


private class ModelAdapter(private val delegate: ConfinedValueModel<Double>, precision: Int): BoundedRangeModel {
    private val listeners = mutableListOf<ChangeListener>()
    private var valueAdjusting = false
    private val multiplier = 10.0.pow(precision.toDouble())

    override fun getMinimum() = (delegate.limits.start * multiplier).toInt()

    override fun setMinimum(newMinimum: Int) {
        delegate.limits = newMinimum / multiplier .. delegate.limits.endInclusive
    }

    override fun getMaximum() = (delegate.limits.endInclusive * multiplier).toInt()

    override fun setMaximum(newMaximum: Int) {
        delegate.limits = delegate.limits.start .. newMaximum / multiplier
    }

    override fun getValue() = (delegate.value * multiplier).toInt()

    override fun setValue(newValue: Int) {
        delegate.value = newValue / multiplier
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

private open class DoubleSlider(slider: Slider, precision: Int = 2): JSlider(ModelAdapter(slider.model, precision)) {
    init {
        this.orientation = when (slider.orientation) {
            Orientation.Vertical -> VERTICAL
            else                 -> HORIZONTAL
        }
    }
}

internal class NativeSliderBehavior(
        private val appScope                 : CoroutineScope,
        private val uiDispatcher             : CoroutineContext,
        private val window                   : SkiaWindow,
        private val swingGraphicsFactory     : SwingGraphicsFactory,
        private val focusManager             : FocusManager?,
        private val nativePointerPreprocessor: NativePointerPreprocessor?
): Behavior<Slider> {

    private inner class JSliderPeer(slider: Slider): DoubleSlider(slider) {
        private val slider: Slider? = slider

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

    override fun render(view: Slider, canvas: Canvas) {
        nativePeer.paint(swingGraphicsFactory((canvas as CanvasImpl).skiaCanvas))
    }

    override fun install(view: Slider) {
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

    override fun uninstall(view: Slider) {
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