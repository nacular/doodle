package io.nacular.doodle.drawing.impl

import io.nacular.doodle.core.Camera
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.GraphicsSurface
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.skia.skia
import io.nacular.doodle.skia.skia33
import io.nacular.doodle.utils.addOrAppend
import io.nacular.doodle.utils.observable
import org.jetbrains.skia.ClipMode.INTERSECT
import org.jetbrains.skia.Font
import org.jetbrains.skia.Matrix44
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Picture
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skiko.SkiaLayer
import kotlin.properties.ReadWriteProperty
import org.jetbrains.skia.Canvas as SkiaCanvas

/**
 * Created by Nicholas Eddy on 5/19/21.
 */
internal class RealGraphicsSurface(
        private val layer         : SkiaLayer,
        private val defaultFont   : Font,
        private val fontCollection: FontCollection,
        private val parent        : RealGraphicsSurface?): GraphicsSurface {

    private var picture         =  null as Picture?
    private val children        =  mutableListOf<RealGraphicsSurface>()
    private var renderBlock     by redrawProperty<((Canvas) -> Unit)?>(null)
    private var finalTransform  by parentRedrawProperty(Identity)
    private val pictureRecorder =  PictureRecorder()

    internal var postRender: ((Canvas) -> Unit)? = null

    init {
        parent?.add(this)
    }

    override var size  by redrawProperty(Empty)
    override var index = 0
        set(new) {
            parent?.let {
                it.children.remove(this)
                it.children.addOrAppend(new, this)
                it.needsRerender()
            }
        }
    override var zOrder             by parentRedrawProperty (0                  ) { _,_ ->   updateParentChildrenSort(        ) }
    override var visible            by redrawProperty       (true               )
    override var opacity            by redrawProperty       (0.5f               )
    override var position           by observable           (Origin             ) { _,new -> updateTransform         (new     ) }
    override var mirrored           by observable           (false              ) { _,_   -> updateTransform         (position) }
    override var transform          by observable           (Identity           ) { _,_   -> updateTransform         (position) }
    override var childrenClipPath   by redrawProperty<Path?>(null               )
    override var clipCanvasToBounds by redrawProperty       (true               )
    override var camera             by parentRedrawProperty (Camera(Origin, 0.0))

    override fun render(block: (Canvas) -> Unit) {
        renderBlock = block
    }

    private var released = false

    override fun release() {
        if (!released) {
            released = true

            picture?.close()
            picture = null
            pictureRecorder.close()

            children.forEach {
                it.release()
            }

            // FIXME: Should always be true
            if (parent?.released == false) {
                parent.remove(this)
            }
        }
    }

    internal fun onRender(skiaCanvas: SkiaCanvas) {
        if (picture == null) {
            val canvas = pictureRecorder.beginRecording(bounds.atOrigin.skia())
            drawToCanvas(canvas)
            picture = pictureRecorder.finishRecordingAsPicture()
        }

        skiaCanvas.save()

        when {
            finalTransform.is3d -> {
                val matrix = (camera.projection * finalTransform).matrix

                skiaCanvas.concat(Matrix44(
                    matrix[0,0].toFloat(), matrix[0,1].toFloat(), matrix[0,2].toFloat(), matrix[0,3].toFloat(),
                    matrix[1,0].toFloat(), matrix[1,1].toFloat(), matrix[1,2].toFloat(), matrix[1,3].toFloat(),
                    matrix[2,0].toFloat(), matrix[2,1].toFloat(), matrix[2,2].toFloat(), matrix[2,3].toFloat(),
                    matrix[3,0].toFloat(), matrix[3,1].toFloat(), matrix[3,2].toFloat(), matrix[3,3].toFloat(),
                ))
            }
            else                -> skiaCanvas.concat(finalTransform.skia33())
        }

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
        try {
            picture?.close()
        } catch (ignore: Throwable) {}

        picture = null

        when (parent) {
            null -> layer.needRedraw    ()
            else -> parent.needsRerender()
        }
    }

    private fun drawToCanvas(skiaCanvas: SkiaCanvas) {
        if (visible && !size.empty) {
            when (opacity) {
                1f   -> skiaCanvas.save()
                else -> skiaCanvas.saveLayer(null, Paint().apply {
                    alpha = (255 * opacity).toInt()
                })
            }

            if (clipCanvasToBounds) {
                skiaCanvas.clipRect(bounds.atOrigin.skia(), INTERSECT)
            }

            skiaCanvas.save()

            val canvas = CanvasImpl(skiaCanvas, defaultFont, fontCollection).apply { size = this@RealGraphicsSurface.size }

            renderBlock?.invoke(canvas)

            if (!clipCanvasToBounds) {
                // Need to do this explicitly if skipped above to ensure child clipping to bounds at least
                skiaCanvas.clipRect(bounds.atOrigin.skia(), INTERSECT)
            }

            childrenClipPath?.let {
                if (!clipCanvasToBounds) {
                    skiaCanvas.clipRect(bounds.atOrigin.skia(), INTERSECT)
                }
                skiaCanvas.clipPath(it.skia(), INTERSECT, antiAlias = true)
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

        when (parent) {
            null -> layer.needRedraw    ()
            else -> parent.needsRerender()
        }
    }
}
