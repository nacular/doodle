package io.nacular.doodle.controls.carousel

import io.nacular.doodle.controls.carousel.Carousel.PresentedItem
import io.nacular.doodle.core.Camera
import io.nacular.doodle.core.View
import io.nacular.doodle.core.View.PolyClipPath
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Vector2D
import io.nacular.doodle.geometry.Vector3D
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.utils._90

/**
 * Shows contents of a [Carousel] one by one, as though they are hanging on walls that rotate
 * when the frame changes.
 *
 * @param floorPaint used to fill the "floor" below the "walls"
 * @param itemConstraints that determine the bounds of each item relative to the Carousel
 */
public class ReflectionPresenter<T>(
    private val floorPaint     : (Size) -> Paint = { (Black opacity 0.25f).paint },
                itemConstraints: ConstraintDslContext.(Bounds) -> Unit = fill
): ConstraintBasedPresenter<T>(itemConstraints) {
    private inner class Floor: View() {
        override fun render(canvas: Canvas) {
            canvas.rect(bounds.atOrigin, fill = floorPaint(size))
        }
    }

    override fun present(
        carousel         : Carousel<T, *>,
        position         : Position,
        progressToNext   : Float,
        supplementalViews: List<View>,
        items            : (at: Position) -> PresentedItem?
    ): Presentation {
        val floor              = mutableListOf<View>()
        val results            = mutableListOf<PresentedItem>()
        val globalCamera       = Camera(Point(carousel.width / 2, carousel.height / 2), 1000.0)
        val forwardTranslation = -carousel.width * 1 * when {
            progressToNext < 0.5f ->     progressToNext
            else                  -> 1 - progressToNext
        }
        val globalTransform = Identity.translate(z = forwardTranslation).rotateY(
            around = Vector3D(globalCamera.position.x, globalCamera.position.y, carousel.width / 2),
            by     = _90 * progressToNext
        )
        val viewRect = Rectangle(size = carousel.size)

        items(position)?.apply {
            results += this
            setBounds(this, carousel.size)
            transform = globalTransform

            if (x < 0 || y < 0 || bounds.right > carousel.width || bounds.bottom > carousel.height) {
                clipPath = PolyClipPath((globalCamera.projection * transform)(viewRect))
            }
        }?.let { current ->
            if (progressToNext != 0f) {
                reflection(carousel, current, position, items)?.also {
                    results += it

                    if (current.clipPath != null) {
                        it.clipPath = PolyClipPath((globalCamera.projection * it.transform)(viewRect))
                    }
                }

                position.next?.let { nextPosition ->
                    items(nextPosition)?.apply {
                        results += this
                        setBounds(this, carousel.size) { it.run { at(x = x + carousel.width) } }
                        transform = globalTransform * Identity.rotateY(around = Point(carousel.width, 0), -_90)

                        if (x < carousel.width || y < 0 || bounds.right > 2 * carousel.width || bounds.bottom > carousel.height) {
                            clipPath = PolyClipPath((globalCamera.projection * transform)(viewRect.at(x = carousel.width)))
                        }

                        reflection(carousel, this, nextPosition, items)?.also { reflection ->
                            results += reflection

                            if (clipPath != null) {
                                reflection.clipPath = PolyClipPath((globalCamera.projection * reflection.transform)(viewRect.at(x = carousel.width)))
                            }
                        }
                    }
                }

                getExistingFloor(supplementalViews, 0).apply {
                    floor    += this
                    bounds    = Rectangle(0.0, carousel.height, carousel.width, carousel.width)
                    camera    = globalCamera
                    transform = globalTransform * Identity.rotateX(around = Point(0, carousel.height), _90)
                }
            }
        }

        return Presentation(results.onEach { it.camera = globalCamera }, floor)
    }

    override fun distanceToNext(
        carousel: Carousel<T, *>,
        position: Position,
        offset  : Vector2D,
        items   : (Position) -> PresentedItem?
    ): Distance = Distance(Vector2D(x = 1), carousel.width)

    private fun reflection(carousel: Carousel<T, *>, source: PresentedItem, position: Position, items: (at: Position) -> PresentedItem?): PresentedItem? = items(position)?.apply {
        bounds    = source.bounds
        transform = source.transform * Identity.flipVertically(at = carousel.height)
    }

    private fun getExistingFloor(existingSupplementalViews: List<View>, index: Int) = existingSupplementalViews.getOrNull(index)?.let {
        if (it is ReflectionPresenter<*>.Floor) it else Floor()
    } ?: Floor()
}
