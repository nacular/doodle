package com.nectar.doodle.theme.system

import com.nectar.doodle.controls.Slider
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.impl.NativeSliderFactory
import com.nectar.doodle.system.Cursor.Companion.Default
import com.nectar.doodle.theme.Renderer

/**
 * Created by Nicholas Eddy on 11/20/18.
 */
class SystemSliderUI(nativeSliderFactory: NativeSliderFactory, slider: Slider): Renderer<Slider> {
    private val nativePeer by lazy{ nativeSliderFactory(slider) }

    override fun render(view: Slider, canvas: Canvas) {
        nativePeer.render(canvas)
    }

    override fun install(view: Slider) {
        super.install(view)

        view.cursor = Default

        view.rerender()

        view.idealSize = nativePeer.idealSize

        view.idealSize?.let {
            view.size = it
        }

//        if (view.idealSize != null /*&&
//            ( aSlider.getParent() == null || aSlider.getParent().getLayout() == null )*/) {
//            view.size = view.idealSize
//        }
    }

    override fun uninstall(view: Slider) {
        super.uninstall(view)

        nativePeer.discard()

        view.cursor = null
    }
}