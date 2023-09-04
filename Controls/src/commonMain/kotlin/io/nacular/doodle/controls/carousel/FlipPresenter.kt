package io.nacular.doodle.controls.carousel

import io.nacular.doodle.controls.carousel.Carousel.PresentedItem
import io.nacular.doodle.core.Camera
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Vector2D
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils._180


public class FlipPresenter<T>(
    private val orientation    : Orientation = Horizontal,
    private val camera         : (viewPort: Size) -> Camera = { Camera(Point(it.width / 2, it.height / 2), 1000.0) },
                itemConstraints: ConstraintDslContext.(Bounds) -> Unit = fill
): ConstraintBasedPresenter<T>(itemConstraints) {
    override fun present(
        carousel         : Carousel<T, *>,
        position         : Position,
        progressToNext   : Float,
        supplementalViews: List<View>,
        items            : (Position) -> PresentedItem?
    ): Presentation {
        val results = mutableListOf<PresentedItem>()

        val globalCamera = camera(carousel.size)
        var faceUp       = true

        items(position)?.apply {
            setBounds(this, carousel.size)

            camera    = globalCamera
            transform = when (orientation) {
                Horizontal -> Identity.rotateY(bounds.center, -_180 * progressToNext)
                else       -> Identity.rotateX(bounds.center,  _180 * progressToNext)
            }

            val points = (globalCamera.projection * transform).invoke(bounds.points.take(3))

            faceUp = (points[1] - points[0] cross points[2] - points[1]).z > 0.0

            opacity = if (faceUp) 1f else 0f

            results += this
        }

        if (!faceUp) {
            position.next?.let(items)?.apply {
                setBounds(this, carousel.size)
                camera    = globalCamera
                transform = when (orientation) {
                    Horizontal -> Identity.flipHorizontally(at = bounds.center.x).rotateY(bounds.center,  _180 * progressToNext)
                    else       -> Identity.flipVertically  (at = bounds.center.y).rotateX(bounds.center, -_180 * progressToNext)
                }

                results += this
            }
        }

        return Presentation(items = results)
    }

    override fun distanceToNext(
        carousel: Carousel<T, *>,
        position: Position,
        offset  : Vector2D,
        items   : (Position) -> PresentedItem?
    ): Distance = when (orientation) {
        Horizontal -> Distance(Vector2D(x = 1), carousel.width )
        else       -> Distance(Vector2D(y = 1), carousel.height)
    }
}