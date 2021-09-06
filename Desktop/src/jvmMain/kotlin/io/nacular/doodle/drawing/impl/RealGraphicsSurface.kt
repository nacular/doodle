package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.GraphicsSurface
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.skia.skia
import io.nacular.doodle.utils.addOrAppend
import io.nacular.doodle.utils.observable
import org.jetbrains.skija.ClipMode
import org.jetbrains.skija.Font
import org.jetbrains.skija.Paint
import org.jetbrains.skija.Picture
import org.jetbrains.skija.PictureRecorder
import org.jetbrains.skija.paragraph.FontCollection
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaWindow
import kotlin.properties.ReadWriteProperty
import org.jetbrains.skija.Canvas as SkijaCanvas

/**
 * Created by Nicholas Eddy on 5/19/21.
 */
internal class RealGraphicsSurface(
                    window        : SkiaWindow,
        private val defaultFont   : Font,
        private val fontCollection: FontCollection,
        private val parent        : RealGraphicsSurface?): GraphicsSurface {

    private var layer           =  window.layer as SkiaLayer?
    private var picture         =  null as Picture?
    private val children        =  mutableListOf<RealGraphicsSurface>()
    private var renderBlock     by redrawProperty<((Canvas) -> Unit)?>(null)
    private var finalTransform  by parentRedrawProperty(Identity)
    private val pictureRecorder =  PictureRecorder()

    internal var postRender: ((Canvas) -> Unit)? = null

    init {
        parent?.add(this)
    }

    override var size               by redrawProperty(Empty)
    override var index              = 0
        set(new) {
            parent?.let {
                it.children.remove(this)
                it.children.addOrAppend(new, this)
                it.needsRerender()
            }
        }
    override var zOrder             by parentRedrawProperty    (0                          ) { _,_ ->   updateParentChildrenSort(        ) }
    override var visible            by redrawProperty          (true                       )
    override var opacity            by redrawProperty          (0.5f                       )
    override var position           by observable              (Origin                     ) { _,new -> updateTransform         (new     ) }
    override var mirrored           by observable              (false                      ) { _,_   -> updateTransform         (position) }
    override var transform          by observable              (Identity                   ) { _,_   -> updateTransform         (position) }
    override var childrenClipPoly   by redrawProperty<Polygon?>(null                       )
    override var clipCanvasToBounds by redrawProperty          (true                       )

    override fun render(block: (Canvas) -> Unit) {
        renderBlock = block
    }

    private var released = false

    override fun release() {
        if (!released) {
            released = true

            layer    = null
            picture?.close()
            pictureRecorder.close()

            // FIXME: Should always be true
            if (parent?.released == false) {
                parent.remove(this)
            }
        }
    }

    internal fun onRender(skiaCanvas: SkijaCanvas) {
        if (picture == null) {
            val canvas = pictureRecorder.beginRecording(bounds.atOrigin.skia())
            drawToCanvas(canvas)
            picture = pictureRecorder.finishRecordingAsPicture()
        }

        skiaCanvas.save()
        skiaCanvas.setMatrix(skiaCanvas.localToDeviceAsMatrix33.makeConcat(finalTransform.skia()))
        skiaCanvas.drawPicture(picture!!)
        skiaCanvas.restore()
    }

    private fun updateParentChildrenSort() {
        parent?.let {
            it.children.sortWith(Comparator<RealGraphicsSurface> { a, b -> a.zOrder - b.zOrder }.thenComparing { a, b -> a.index - b.index })
            it.needsRerender()
        }
    }

    private fun add(child: RealGraphicsSurface) {
        children += child
        needsRerender()
    }

    private fun remove(child: RealGraphicsSurface) {
        children -= child
        needsRerender()
    }

    private fun needsRerender() {
        picture?.close()
        picture = null

        when (parent) {
            null -> layer?.needRedraw   ()
            else -> parent.needsRerender()
        }
    }

    private fun drawToCanvas(skiaCanvas: SkijaCanvas) {
        if (visible && !size.empty) {
            when (opacity) {
                1f   -> skiaCanvas.save()
                else -> skiaCanvas.saveLayer(null, Paint().apply {
                    alpha = (255 * opacity).toInt()
                })
            }

            if (clipCanvasToBounds) {
                skiaCanvas.clipRect(bounds.atOrigin.skia(), ClipMode.INTERSECT)
            }

            skiaCanvas.save()

            val canvas = CanvasImpl(skiaCanvas, defaultFont, fontCollection).apply { size = this@RealGraphicsSurface.size }

            renderBlock?.invoke(canvas)

            if (!clipCanvasToBounds) {
                // Need to do this explicitly if skipped above to ensure child clipping to bounds at least
                skiaCanvas.clipRect(bounds.atOrigin.skia(), ClipMode.INTERSECT)
            }

            childrenClipPoly?.let {
                if (!clipCanvasToBounds) {
                    skiaCanvas.clipRect(bounds.atOrigin.skia(), ClipMode.INTERSECT)
                }
                skiaCanvas.clipPath(it.skia(), ClipMode.INTERSECT)
            }

            children.forEach {
                it.onRender(skiaCanvas)
            }

            skiaCanvas.restore()

            postRender?.invoke(canvas)

            skiaCanvas.restore()
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
        needsRerender()
    }

    private fun <T> parentRedrawProperty(initial: T, onChange: RealGraphicsSurface.(old: T, new: T) -> Unit = { _,_ -> }): ReadWriteProperty<RealGraphicsSurface, T> = observable(initial) { old, new ->
        onChange(old, new)
        parent?.needsRerender()
    }
}
