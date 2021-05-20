package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.GraphicsSurface
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.utils.observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skija.Font
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import org.jetbrains.skiko.SkiaWindow

/**
 * Created by Nicholas Eddy on 5/19/21.
 */
internal class RealGraphicsSurface(
                    window             : SkiaWindow,
        private val defaultFont        : Font,
        private var parent             : RealGraphicsSurface?,
                    isContainer        : Boolean,
                    addToRootIfNoParent: Boolean): GraphicsSurface {

    private val layer = SkiaLayer().apply {
        renderer = object: SkiaRenderer {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun onRender(skiaCanvas: org.jetbrains.skija.Canvas, width: Int, height: Int, nanoTime: Long) {
                skiaCanvas.scale(contentScale, contentScale)
                renderBlock?.invoke(CanvasImpl(skiaCanvas, defaultFont).apply { size = Size(width, height) })
            }
        }
    }

    init {
        if (addToRootIfNoParent) {
            runBlocking(Dispatchers.Swing) {
                window.add(layer)
            }
        }
    }

    override var position: Point by observable(Origin) { _,new ->
        layer.setLocation(new.x.toInt(), new.y.toInt())
    }

    override var size: Size by observable(Empty) { _,new ->
        layer.setSize(new.width.toInt(), new.height.toInt())
    }

    override var index by observable(0) { _,_ -> }

    override var zOrder by observable(0) { _,_ -> }

    override var visible by observable(true) { _,_ -> }

    override var opacity by observable(1f) { _,_ -> }

    override var transform by observable(Identity) { _,_ -> }

    override var mirrored by observable(false) { _,_ -> }

    override var clipCanvasToBounds by observable(true) { _,_ -> }

    override var childrenClipPoly: Polygon? by observable(null) { _,_ -> }

    private var renderBlock: ((Canvas) -> Unit)? = null

    override fun render(block: (Canvas) -> Unit) {
        runBlocking(Dispatchers.Swing) {
            renderBlock = block
            layer.needRedraw()
        }
    }

    override fun release() {
        layer.dispose()
    }
}