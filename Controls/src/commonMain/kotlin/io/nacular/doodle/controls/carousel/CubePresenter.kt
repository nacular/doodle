package io.nacular.doodle.controls.carousel

import io.nacular.doodle.controls.carousel.Carousel.PresentedItem
import io.nacular.doodle.core.Camera
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Vector2D
import io.nacular.doodle.geometry.Vector3D
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.utils.BoxOrientation
import io.nacular.doodle.utils.BoxOrientation.Bottom
import io.nacular.doodle.utils.BoxOrientation.Left
import io.nacular.doodle.utils.BoxOrientation.Right
import io.nacular.doodle.utils.BoxOrientation.Top
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.Orientation.Vertical
import io.nacular.doodle.utils._90


/**
 * Shows contents of a [Carousel] as though they are the faces of a cubic shape that rotates
 * as the frame changes.
 *
 * @param orientation indicates which axis the cube rotates around
 * @param camera determining how the cube's perspective should be rendered
 * @param itemConstraints that determine the bounds of each item relative to the Carousel
 */
public class CubePresenter<T>(
    private val orientation    : Orientation = Horizontal,
    private val camera         : (viewPort: Size) -> Camera = { Camera(Point(it.width / 2, it.height / 2), 1000.0) },
                itemConstraints: ConstraintDslContext.(Bounds) -> Unit = fill
): ConstraintBasedPresenter<T>(itemConstraints) {
    private class CubeCap: View(), VisualItem {
        override fun render(canvas: Canvas) {
            canvas.rect(bounds.atOrigin, fill = Black.paint)
        }

        override var bounds_ : Rectangle get() = prospectiveBounds; set(value) { suggestBounds(value) }
    }

    private interface VisualItem {
        var zOrder   : Int
        var bounds_  : Rectangle
        var transform: AffineTransform
    }

    private class StageVisualItem(presentedItem: PresentedItem): VisualItem {
        override var zOrder    by presentedItem::zOrder
        override var bounds_   by presentedItem::bounds
        override var transform by presentedItem::transform
    }

    override fun present(
        carousel         : Carousel<T, *>,
        position         : Position,
        progressToNext   : Float,
        supplementalViews: List<View>,
        items            : (Position) -> PresentedItem?
    ): Presentation {
        val results              = mutableListOf<PresentedItem>()
        val globalCamera         = camera(carousel.size)
        val newSupplementalViews = mutableListOf<View>()

        val currentItem = (items(position) ?: position.next?.let(items))?.apply {
            setBounds(this, carousel.size)

            results += this
        } ?: return Presentation(items = results, newSupplementalViews)

        val capLocation = when {
            orientation == Horizontal && globalCamera.position.y < currentItem.y             -> Top
            orientation == Horizontal && globalCamera.position.y > currentItem.bounds.bottom -> Bottom
            orientation == Vertical   && globalCamera.position.x < currentItem.x             -> Left
            orientation == Vertical   && globalCamera.position.x > currentItem.bounds.right  -> Right
            else                                                                             -> null
        }

        val cap = capLocation?.let {
            getExistingCap(newSupplementalViews, 0).apply {
                camera    = globalCamera
                zOrder    = 3 // cap is always rendered above all other views
                transform = when (capLocation) {
                    Top    -> Identity.rotateX(around = currentItem.position,                             _90)
                    Bottom -> Identity.rotateX(around = Point(currentItem.x, currentItem.bounds.bottom), -_90)
                    Left   -> Identity.rotateY(around = currentItem.position,                            -_90)
                    Right  -> Identity.rotateY(around = Point(currentItem.bounds.right, currentItem.y),   _90)
                }
            }
        }

        val previousItem = if (
            (orientation == Horizontal && globalCamera.position.x < currentItem.x) ||
            (orientation == Vertical   && globalCamera.position.y < currentItem.y)) {

            position.previous?.let(items)?.let { previousItem ->
                results += previousItem

                setBounds(previousItem, carousel.size) {
                    it.run {
                        when (orientation) {
                            Horizontal -> at(x = currentItem.x - width )
                            else       -> at(y = currentItem.y - height)
                        }
                    }
                }

                previousItem.camera    = globalCamera
                previousItem.transform = when (orientation) {
                    Horizontal -> Identity.rotateY(around = currentItem.position,                           -_90)
                    else       -> Identity.rotateY(around = Point(currentItem.bounds.right, currentItem.y), -_90)
                }

                StageVisualItem(previousItem)
            } ?: getExistingCap(newSupplementalViews, 1).also {
                it.suggestBounds(currentItem.bounds.run {
                        when (orientation) {
                            Horizontal -> at(x = currentItem.x - width )
                            else       -> at(y = currentItem.y - height)
                        }
                    }
                )

                it.camera    = globalCamera
                it.transform = when (orientation) {
                    Horizontal -> Identity.rotateY(around = currentItem.position,                           -_90)
                    else       -> Identity.rotateY(around = Point(currentItem.bounds.right, currentItem.y), -_90)
                }
            }
        } else null

        var subsequentItem: VisualItem? = null

        when (val next = position.next?.let(items)) {
            null -> {
                currentItem.transform = Identity

                subsequentItem = getExistingCap(newSupplementalViews, 1).also {
                    it.suggestBounds(currentItem.bounds.run {
                            when (orientation) {
                                Horizontal -> at(x = right )
                                else       -> at(y = bottom)
                            }
                        }
                    )

                    it.camera    = globalCamera
                    it.transform = when (orientation) {
                        Horizontal -> Identity.rotateY(around = Point(x = currentItem.bounds.right ),  _90)
                        else       -> Identity.rotateX(around = Point(y = currentItem.bounds.bottom), -_90)
                    }
                }

                cap?.also {
                    val old = it.bounds

                    configureCap(cap, capLocation, currentItem, currentItem)

                    if (old != it.bounds) {
                        it.rerenderNow()
                    }
                }

                updateZOrder(globalCamera, StageVisualItem(currentItem), previousItem, subsequentItem)
            }
            else -> next.also { nextItem ->
                results += nextItem

                setBounds(nextItem, carousel.size) {
                    it.run {
                        when (orientation) {
                            Horizontal -> at(x = currentItem.bounds.right )
                            else       -> at(y = currentItem.bounds.bottom)
                        }
                    }
                }

                val cubeTransform = when (orientation) {
                    Horizontal -> Identity.
                        translate(z = (nextItem.bounds.width - currentItem.bounds.width) / 2 * progressToNext).
                        rotateY  (
                            Vector3D(
                                    x = currentItem.bounds.center.x,
                                    y = currentItem.bounds.center.y,
                                    z = -nextItem.bounds.width / 2
                            ),
                            -_90 * progressToNext
                        )
                    else      -> Identity.
                        translate(z = (nextItem.bounds.height - currentItem.bounds.height) / 2 * progressToNext).
                        rotateX  (
                            Vector3D(
                                    x = currentItem.bounds.center.x,
                                    y = currentItem.bounds.center.y,
                                    z = -nextItem.bounds.height / 2
                            ),
                            _90 * progressToNext
                        )
                }

                currentItem.camera    = globalCamera
                currentItem.transform = cubeTransform
                nextItem.camera       = globalCamera
                nextItem.transform    = cubeTransform * when (orientation) {
                    Horizontal -> Identity.rotateY(around = nextItem.bounds.position,  _90)
                    else       -> Identity.rotateX(around = nextItem.bounds.position, -_90)
                }

                // FIXME: Use placeholder if no subsequent item
                subsequentItem = if (
                    (orientation == Horizontal && globalCamera.position.x > currentItem.bounds.right  ) ||
                    (orientation == Vertical   && globalCamera.position.y > currentItem.bounds.bottom)) {
                    position.next?.next?.let(items)?.let { subsequentItem ->
                        results += subsequentItem

                        setBounds(subsequentItem, carousel.size) {
                            it.run {
                                when (orientation) {
                                    Horizontal -> at(x = nextItem.bounds.right )
                                    else       -> at(y = nextItem.bounds.bottom)
                                }
                            }
                        }

                        subsequentItem.camera    = globalCamera
                        subsequentItem.transform = nextItem.transform * when (orientation) {
                            Horizontal -> Identity.rotateY(around = subsequentItem.bounds.position,  _90)
                            else       -> Identity.rotateX(around = subsequentItem.bounds.position, -_90)
                        }

                        StageVisualItem(subsequentItem)
                    } ?: getExistingCap(newSupplementalViews, 1).also {
                        it.suggestBounds(nextItem.bounds.run {
                                when (orientation) {
                                    Horizontal -> at(x = right )
                                    else       -> at(y = bottom)
                                }
                            }
                        )

                        it.camera    = globalCamera
                        it.transform = nextItem.transform * when (orientation) {
                            Horizontal -> Identity.rotateY(around = it.bounds.position,  _90)
                            else       -> Identity.rotateX(around = it.bounds.position, -_90)
                        }
                    }
                } else null

                cap?.also {
                    val old = it.bounds

                    it.transform = cubeTransform * it.transform

                    configureCap(it, capLocation, currentItem, nextItem)

                    if (old != it.bounds) {
                        it.rerenderNow()
                    }
                }

                previousItem?.also {
                    it.transform = cubeTransform * it.transform
                }

                updateZOrder(globalCamera, StageVisualItem(currentItem), StageVisualItem(nextItem), previousItem, subsequentItem)
            }
        }

        cap?.let { newSupplementalViews += it }

        if (previousItem is CubeCap) {
            newSupplementalViews += previousItem
        }

        if (subsequentItem is CubeCap) {
            newSupplementalViews += subsequentItem as CubeCap
        }

        return Presentation(items = results, newSupplementalViews)
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

    private fun configureCap(cap: View, capLocation: BoxOrientation, currentItem: PresentedItem, nextItem: PresentedItem) {
        cap.suggestBounds(when (capLocation) {
            Top    -> Rectangle(x = currentItem.x,                   y = currentItem.y - nextItem.width, width = currentItem.width, height = nextItem.width    )
            Bottom -> Rectangle(x = currentItem.x,                   y = currentItem.bounds.bottom,      width = currentItem.width, height = nextItem.width    )
            Left   -> Rectangle(x = currentItem.x - nextItem.height, y = currentItem.y,                  width = nextItem.height,   height = currentItem.height)
            Right  -> Rectangle(x = currentItem.bounds.right,        y = currentItem.y,                  width = nextItem.width,    height = currentItem.height)
        })
    }

    private fun updateZOrder(camera: Camera, vararg items: VisualItem?) {
        // FIXME: This doesn't give the right answer sometimes
        items.mapNotNull {
            it?.let { getDepthPoint(camera, it) }
        }.sortedBy { it.first.z }.forEachIndexed { index, (_,item) ->
            item.zOrder = index
        }
    }

    private fun getExistingCap(existingSupplementalViews: List<View>, index: Int) = existingSupplementalViews.getOrNull(index)?.let {
        if (it is CubeCap) it else CubeCap()
    } ?: CubeCap()

    private fun getDepthPoint(camera: Camera, item: VisualItem) = item.let {
        (camera.projection * it.transform)(item.bounds_.center) to it
    }
}