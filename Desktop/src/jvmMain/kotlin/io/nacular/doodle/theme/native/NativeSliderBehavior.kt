//package io.nacular.doodle.theme.native
//
//import io.nacular.doodle.controls.buttons.Button
//import io.nacular.doodle.controls.range.Slider
//import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
//import io.nacular.doodle.core.View
//import io.nacular.doodle.drawing.Canvas
//import io.nacular.doodle.drawing.TextMetrics
//import io.nacular.doodle.event.KeyEvent
//import io.nacular.doodle.event.PointerEvent
//import io.nacular.doodle.event.PointerListener
//import io.nacular.doodle.focus.FocusManager
//import io.nacular.doodle.geometry.Rectangle
//import io.nacular.doodle.geometry.Size
//import io.nacular.doodle.image.impl.ImageImpl
//import io.nacular.doodle.skia.toImage
//import io.nacular.doodle.system.Cursor
//import io.nacular.doodle.system.Cursor.Companion.Default
//import io.nacular.doodle.system.SystemPointerEvent.Type.*
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.launch
//import org.jetbrains.skiko.SkiaWindow
//import java.awt.Color
//import java.awt.Component
//import java.awt.Dimension
//import java.awt.Graphics2D
//import java.awt.RenderingHints
//import java.awt.event.FocusEvent
//import java.awt.event.FocusListener
//import java.awt.event.MouseEvent
//import java.awt.event.MouseEvent.BUTTON1
//import java.awt.image.BufferedImage
//import java.awt.image.BufferedImage.TYPE_INT_ARGB
//import javax.swing.JButton
//import javax.swing.JSlider
//import kotlin.coroutines.CoroutineContext
//
//
///**
// * Created by Nicholas Eddy on 6/14/21.
// */
//internal class NativeSliderBehavior(
//        private val appScope: CoroutineScope,
//        private val uiDispatcher: CoroutineContext,
//        private val window: SkiaWindow,
//        textMetrics: TextMetrics,
//        private val focusManager: FocusManager?
//): CommonTextButtonBehavior<Button>(textMetrics) {
//
//    private inner class JSliderPeer(private val slider: Slider): JSlider(slider.model.limits.start) {
//        init {
//            value = slider.value
//
//            addFocusListener(object : FocusListener {
//                override fun focusGained(e: FocusEvent?) {
//                    focusManager?.requestFocus(slider)
//                }
//
//                override fun focusLost(e: FocusEvent?) {
//                    focusManager?.clearFocus()
//                }
//            })
//        }
//
//        override fun repaint(tm: Long, x: Int, y: Int, width: Int, height: Int) {
//            super.repaint(tm, x, y, width, height)
//            slider.rerender()
//        }
//
//        override public fun processMouseEvent(e: MouseEvent?) {
//            super.processMouseEvent(e)
//        }
//    }
//
//    private lateinit var nativePeer: JButtonPeer
//    private var oldCursor    : Cursor? = null
//    private var oldIdealSize : Size?   = null
//    private lateinit var bufferedImage: BufferedImage
//    private lateinit var graphics     : Graphics2D
//
//    private val focusChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
//        when (new) {
//            true -> nativePeer.requestFocus ()
//            else -> nativePeer.transferFocus()
//        }
//    }
//
//    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
//        nativePeer.isEnabled = new
//    }
//
//    private val focusableChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
//        nativePeer.isFocusable = new
//    }
//
//    private val boundsChanged: (View, Rectangle, Rectangle) -> Unit = { _,_,new ->
//        createNewBufferedImage(new.size)
//        size = new.size.run { Dimension(width.toInt(), height.toInt()) }
//    }
//
//    private fun createNewBufferedImage(size: Size) {
//        if (!size.empty && contentScale > 0f) {
//            bufferedImage = BufferedImage((size.width * contentScale).toInt(), (size.height * contentScale).toInt(), TYPE_INT_ARGB)
//            this@NativeButtonBehavior.graphics = bufferedImage.createGraphics().apply {
//                setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
//                scale(contentScale, contentScale)
//            }
//        }
//    }
//
//    override fun render(view: Button, canvas: Canvas) {
//        if (this::graphics.isInitialized) {
//            graphics.background = Color(255, 255, 255, 0)
//            graphics.clearRect(0, 0, bufferedImage.width, bufferedImage.height)
//
//            nativePeer.paint(graphics)
//            canvas.scale(1 / contentScale, 1 / contentScale) {
//                canvas.image(ImageImpl(bufferedImage.toImage(), ""))
//            }
//        }
//    }
//
//    override fun mirrorWhenRightToLeft(view: Button) = false
//
//    override fun install(view: Button) {
//        super.install(view)
//
//        nativePeer = JButtonPeer(view)
//
//        createNewBufferedImage(view.size)
//
//        view.apply {
//            focusChanged        += this@NativeButtonBehavior.focusChanged
//            boundsChanged       += this@NativeButtonBehavior.boundsChanged
//            enabledChanged      += this@NativeButtonBehavior.enabledChanged
//            pointerChanged      += this@NativeButtonBehavior
//            focusabilityChanged += this@NativeButtonBehavior.focusableChanged
//        }
//
//        appScope.launch(uiDispatcher) {
//            nativePeer.size = view.size.run { Dimension(view.width.toInt(), view.height.toInt()) }
//
//            view.apply {
//                cursor    = Default
//                idealSize = nativePeer.preferredSize.run { Size(width, height) }
//            }
//        }
//    }
//
//    override fun uninstall(view: Button) {
//        super.uninstall(view)
//
//        view.apply {
//            cursor    = oldCursor
//            idealSize = oldIdealSize
//
//            focusChanged        -= this@NativeButtonBehavior.focusChanged
//            boundsChanged       -= this@NativeButtonBehavior.boundsChanged
//            enabledChanged      -= this@NativeButtonBehavior.enabledChanged
//            pointerChanged      -= this@NativeButtonBehavior
//            focusabilityChanged -= this@NativeButtonBehavior.focusableChanged
//        }
//    }
//
//    override fun released(event: PointerEvent) {
//        val button = event.source as Button
//        val model  = button.model
//
//        if (button.enabled && event.buttons.isEmpty()) {
//            model.pressed = false
//            model.armed   = false
//        }
//
//        nativePeer.processMouseEvent(event.toAwt(nativePeer))
//    }
//
//
//    override fun entered(event: PointerEvent) {
//        nativePeer.processMouseEvent(event.toAwt(nativePeer))
//    }
//
//    override fun exited(event: PointerEvent) {
//        nativePeer.processMouseEvent(event.toAwt(nativePeer))
//    }
//
//    override fun pressed(event: PointerEvent) {
//        nativePeer.processMouseEvent(event.toAwt(nativePeer))
//    }
//}