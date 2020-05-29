package com.nectar.doodle.theme.native

import com.nectar.doodle.controls.Slider
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.impl.NativeSliderFactory
import com.nectar.doodle.event.PointerEvent
import com.nectar.doodle.event.PointerListener
import com.nectar.doodle.event.PointerMotionListener
import com.nectar.doodle.system.Cursor.Companion.Default
import com.nectar.doodle.core.Behavior

/**
 * Created by Nicholas Eddy on 11/20/18.
 */
internal class NativeSliderBehavior(nativeSliderFactory: NativeSliderFactory, slider: Slider): Behavior<Slider>, PointerListener, PointerMotionListener {
    private val nativePeer by lazy { nativeSliderFactory(slider) }

    override fun render(view: Slider, canvas: Canvas) {
        nativePeer.render(canvas)
    }

    override fun install(view: Slider) {
        super.install(view)

        view.cursor              = Default
        view.pointerChanged       += this
        view.pointerMotionChanged += this

        view.rerender()
    }

    override fun uninstall(view: Slider) {
        super.uninstall(view)

        nativePeer.discard()

        view.cursor              = null
        view.pointerChanged       -= this
        view.pointerMotionChanged -= this
    }

    override fun pressed(event: PointerEvent) {
        event.consume()
    }

    override fun dragged(event: PointerEvent) {
        event.consume()
    }

}