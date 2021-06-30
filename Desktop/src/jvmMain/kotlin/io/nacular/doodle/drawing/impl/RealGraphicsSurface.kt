package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.GraphicsSurface
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.skia.skija
import io.nacular.doodle.utils.observable
import org.jetbrains.skija.ClipMode
import org.jetbrains.skija.Font
import org.jetbrains.skija.Paint
import org.jetbrains.skija.paragraph.FontCollection
import org.jetbrains.skiko.SkiaWindow
import kotlin.properties.Delegates.observable
import kotlin.properties.ReadWriteProperty
import org.jetbrains.skija.Canvas as SkijaCanvas

/**
 * Created by Nicholas Eddy on 5/19/21.
 */
internal class RealGraphicsSurface(
                    window             : SkiaWindow,
        private val defaultFont        : Font,
        private val fontCollection     : FontCollection,
        private val parent             : RealGraphicsSurface?,
                    addToRootIfNoParent: Boolean
): GraphicsSurface {

    private val layer          = window.layer
    private val children       = mutableListOf<RealGraphicsSurface>()
    private var needsRerender  = false

    private var dirty = false
        get() = field || true == parent?.dirty
        set(new) {
            field = new
            needsRerender()
        }

    private var renderBlock: ((Canvas) -> Unit)? by observable(null) { _,_,_ ->
        dirty = true
    }

    private var finalTransform by redrawProperty(Identity)

    init {
//        println("new surface: $id, $parent")
        parent?.add(this)
    }

    override var position: Point by observable(Origin) { _,new ->
        parent?.dirty = true
        updateTransform(new)
        needsRerender()
        layer.needRedraw()
    }

    override var size by observable(Empty) { _,_ ->
        parent?.dirty = true
        needsRerender()
        layer.needRedraw()
    }

    override var index by redrawProperty(parent?.children?.size ?: 0) { _,_ ->
        updateParentChildrenSort()
    }

    override var zOrder: Int by redrawProperty(0) { _,_ ->
        updateParentChildrenSort()
    }

    private fun updateParentChildrenSort() {
        val comparator = Comparator<RealGraphicsSurface> { a, b -> a.zOrder - b.zOrder }.
        thenComparing { a, b -> a.index - b.index }

        parent?.children?.sortWith(comparator)
    }

    override var visible by redrawProperty(true)

    override var opacity by redrawProperty(0.5f)

    override var transform by observable(Identity) { _,_ -> updateTransform(position) }

    override var mirrored by observable(false) { _,_ -> updateTransform(position) }

    override var clipCanvasToBounds by observable(true) { _,_ -> }

    override var childrenClipPoly: Polygon? by observable(null) { _, _ -> }

    override fun render(block: (Canvas) -> Unit) {
        renderBlock = block

        layer.needRedraw()
    }

    override fun release() {
        parent?.remove(this)
    }

    private fun add(child: RealGraphicsSurface) {
        children += child
    }

    private fun remove(child: RealGraphicsSurface) {
        children -= child
    }

    private fun needsRerender() {
        needsRerender = true
//        println("needsRedraw($id)")
        parent?.needsRerender()
    }

    internal fun onRender(skiaCanvas: SkijaCanvas, width: Int, height: Int, nanoTime: Long) {
//        println("onRender ($id) $width, $height, $size: ${Thread.currentThread()}")

        if (true) { //needsRerender) {
            if (visible && !size.empty) {
//                println("render [$id]")
//                skiaCanvas.save()

                when (opacity) {
                    1f   -> skiaCanvas.save()
                    else -> skiaCanvas.saveLayer(null, Paint().apply {
                        alpha = (255 * opacity).toInt()
                    })
                }

                skiaCanvas.setMatrix(skiaCanvas.localToDeviceAsMatrix33.makeConcat(finalTransform.skija()))

                if (clipCanvasToBounds) {
                    skiaCanvas.clipRect(bounds.atOrigin.skija(), ClipMode.INTERSECT)
                }

                if (true) { //dirty) {
                    renderBlock?.invoke(CanvasImpl(skiaCanvas, defaultFont, fontCollection).apply { size = this@RealGraphicsSurface.size })
                }

                if (!clipCanvasToBounds) {
                    // Need to do this explicitly if skipped above to ensure child clipping to bounds at least
                    skiaCanvas.clipRect(bounds.atOrigin.skija(), ClipMode.INTERSECT)
                }

                childrenClipPoly?.let {
                    if (!clipCanvasToBounds) {
                        skiaCanvas.clipRect(bounds.atOrigin.skija(), ClipMode.INTERSECT)
                    }
                    skiaCanvas.clipPath(it.skija(), ClipMode.INTERSECT)
                }

                children./*filter { it.needsRerender }.*/forEach {
//                    println("render nested [${it.id}]")
                    it.onRender(skiaCanvas, width, height, nanoTime)
                }

                skiaCanvas.restore()
            }

            dirty         = false
            needsRerender = false
        }
    }

    private fun updateTransform(new: Point) {
        finalTransform = when {
            !mirrored && transform.isIdentity -> Identity.translate(new)
            mirrored -> (transform translate new).flipHorizontally(at = size.width / 2)
            else     ->  transform translate new
        }
    }

    private fun <T> redrawProperty(initial: T, onChange: RealGraphicsSurface.(old: T, new: T) -> Unit = { _,_ -> }): ReadWriteProperty<RealGraphicsSurface, T> = observable(initial) { old, new ->
        onChange(old, new)
        layer.needRedraw()
    }
}
