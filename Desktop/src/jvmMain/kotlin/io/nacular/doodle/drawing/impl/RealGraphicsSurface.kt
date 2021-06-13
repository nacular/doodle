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
import org.jetbrains.skija.Font
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import org.jetbrains.skiko.SkiaWindow
import java.awt.Container
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import org.jetbrains.skija.Canvas as SkijaCanvas


/**
 * Created by Nicholas Eddy on 5/19/21.
 */
internal class RealGraphicsSurface(
                    window             : SkiaWindow,
        private val defaultFont        : Font,
                    parent             : RealGraphicsSurface?,
                    addToRootIfNoParent: Boolean): GraphicsSurface {

    private inner class SkiaPanel: Container() {
        val layer = SkiaLayer()

        init {
            layout = null
        }

//        override fun add(component: Component): Component {
//            layer.clipComponents.add(ClipComponent(component))
//            return super.add(component)
//        }

        override fun addNotify() {
            super.addNotify()
            super.add(layer)

            addComponentListener(object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    layer.setSize(width, height)
                }
            })
        }

        override fun removeNotify() {
            layer.dispose()
            super.removeNotify()
        }
    }

    private val skiaPanel = SkiaPanel().apply {
        layer.renderer = object: SkiaRenderer {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun onRender(skiaCanvas: SkijaCanvas, width: Int, height: Int, nanoTime: Long) {
                skiaCanvas.scale(layer.contentScale, layer.contentScale)

//                val a = layer.parent.bounds.run { Rectangle(-position.x, -position.y, width.toDouble() /*- position.x*/, height.toDouble() /*- position.y*/).skija() }
//                skiaCanvas.clipRect(a, ClipMode.INTERSECT)
                renderBlock?.invoke(CanvasImpl(skiaCanvas, defaultFont).apply { size = this@RealGraphicsSurface.size })
            }
        }
    }

    init {
        val parentLayer = when {
            parent != null      -> parent.skiaPanel
            addToRootIfNoParent -> window
            else                -> null
        }

        parentLayer?.add(skiaPanel)
    }

    override var position: Point by observable(Origin) { _, new ->
        skiaPanel.setLocation(new.x.toInt(), new.y.toInt())
    }

    override var size: Size by observable(Empty) { _, new ->
        skiaPanel.setSize(new.width.toInt(), new.height.toInt())
    }

    override var index by observable(0) { _, _ -> }

    override var zOrder by observable(0) { _, _ -> }

    override var visible by observable(true) { _, new ->
        skiaPanel.isVisible = new
    }

    override var opacity by observable(0.5f) { _, _ -> }

    override var transform by observable(Identity) { _, _ -> }

    override var mirrored by observable(false) { _, _ -> }

    override var clipCanvasToBounds by observable(true) { _, _ -> }

    override var childrenClipPoly: Polygon? by observable(null) { _, _ -> }

    private var renderBlock: ((Canvas) -> Unit)? = null

    override fun render(block: (Canvas) -> Unit) {
        renderBlock = block
//        skiaPanel.repaint()
        skiaPanel.layer.paintImmediately(0, 0, size.width.toInt(), size.height.toInt())
    }

    override fun release() {
        skiaPanel.parent?.remove(skiaPanel)
    }
}