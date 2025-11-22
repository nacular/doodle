package io.nacular.doodle.controls

import io.nacular.doodle.core.View
import io.nacular.doodle.core.renderProperty
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.OuterShadow
import io.nacular.doodle.drawing.PatternPaint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.inset
import io.nacular.doodle.geometry.path
import io.nacular.doodle.geometry.rounded
import io.nacular.doodle.geometry.star
import io.nacular.doodle.geometry.toPath
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.isEven
import io.nacular.doodle.utils.lerp
import io.nacular.doodle.utils.observable
import io.nacular.doodle.utils.roundToNearest
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.ReadWriteProperty

/**
 * Creates a control that shows a star rating.
 *
 * @param max value the rating can have
 * @param displayRounded rounds the displayed value to the nearest decimal. `0` means no rounding, `0.5` rounds to the nearest half.
 * @param pathMetrics that helps get a more accurate size of the stars when their tips are rounded
 *
 * @author Nicholas Eddy
 */
public class StarRater(
                max           : Int          = 5,
    private val displayRounded: Float        = 0f,
    private val pathMetrics   : PathMetrics? = null
): View() {

    /** The value of the rating: between `0` and [max] inclusive. */
    public var value: Double = 0.0; set(new) {
        val old = field
        field = max(0.0, min(max.toDouble(), new))

        displayValue = field.roundToNearest(displayRounded.toDouble())
        changed_(old, new)
    }

    /** The max value this rater can have. This value will always be positive. */
    public var max: Int = max(0, max); set(new) {
        val old = field
        field = max(0, new)
        value = value / old * new
        updateStar()
    }

    /** Minimum spacing between the stars. */
    public var minSpacing: Double by starProperty(0.0)

    /** Number of points for each star. */
    public var numStarPoints: Int by starProperty(5)

    /** Rotation angle of stars. */
    public var starRotation: Measure<Angle> by starProperty(0 * degrees)

    /** Determines how long the star arms are relative to the central part. `0` makes a really pointy star, while `1` gets closer to a circle. */
    public var innerRadiusRatio: Float? by starProperty(null)

    /** Determines how pointy the star arms are. A value of `0` means no rounding. While `1` means fully rounded */
    public var starPointRounding: Float by starProperty(0f)

    /** Color of the shadow shown behind the stars. */
    public var shadowColor: Color? get() = shadow?.color; set(new) {
        shadow = new?.let {
            OuterShadow(
                color      = new,
                horizontal = shadow?.horizontal ?: 0.0,
                vertical   = shadow?.vertical   ?: 1.0,
                blurRadius = shadow?.blurRadius ?: 4.0
            )
        }
    }

    /** Shadow behind stars. */
    private var shadow: OuterShadow? by renderProperty(OuterShadow(color = Black opacity 0.2f, horizontal = 0.0, vertical = 1.0, blurRadius = 4.0))

    /** Stroke used for unfilled stars. */
    private var stroke = Stroke(Lightgray opacity 0.7f)

    private var displayValue by renderProperty(value.roundToNearest(displayRounded.toDouble()))

    @Suppress("PrivatePropertyName")
    private val changed_ = PropertyObserversImpl<StarRater, Double>(this)

    public val changed: PropertyObservers<StarRater, Double> = changed_

    private var star       by renderProperty(path(Origin).close())
    private var starSize = Size.Empty
    private lateinit var fillBounds: Rectangle

    init {
        foregroundColor = Color(0x6200EEu)
        backgroundColor = White

        boundsChanged += { _,old,new ->
            if (old.size != new.size) {
                updateStar()
                rerender()
            }
        }

        styleChanged += {
            rerender()
        }

        clipCanvasToBounds = false

        updateStar()
    }

    private fun updateStar() {
        val radius = max(0.0, min(width / (2 * max) - minSpacing / 2, height / 2))
        val circle = Circle(Point(0.0, height / 2), radius)

        val tempStar = when (val r2 = innerRadiusRatio) {
            null -> star(circle, points = max(3, numStarPoints), rotation = starRotation)
            else -> star(circle, points = max(3, numStarPoints), rotation = starRotation, innerCircleRatio = r2)
        }!!

        updateDimensions { tempStar.boundingRectangle.size }

        var starCenter = Point(
            tempStar.points.minBy { it.x }.x + starSize.width  / 2,
            tempStar.points.minBy { it.y }.y + starSize.height / 2
        )

        star = when {
            starPointRounding > 0f -> {
                val length         = tempStar.points[1].distanceFrom(tempStar.points[0])
                val roundingRadius = lerp(0.0, length, starPointRounding)

                // Would be nice to avoid this double work. But no way to translate a path right now
                val newStar = tempStar.rounded(roundingRadius) { index,_ ->
                    index.isEven
                }

                pathMetrics?.let {
                    val pathBounds = it.bounds(newStar)
                    updateDimensions { pathBounds.size }
                    starCenter = Point(pathBounds.x + starSize.width / 2, pathBounds.y + starSize.height / 2)
                }

                tempStar.translateBy(
                    x = fillBounds.width  / 2 - starCenter.x,
                    y = fillBounds.height / 2 - starCenter.y
                ).rounded(roundingRadius) { index,_ ->
                    index.isEven
                }
            }
            else -> tempStar.translateBy(
                x = fillBounds.width  / 2 - starCenter.x,
                y = fillBounds.height / 2 - starCenter.y
            ).toPath()
        }
    }

    private fun updateDimensions(widthComputation: () -> Size) {
        starSize   = widthComputation()
        fillBounds = when(max) {
            1    -> Rectangle(width, height)
            else -> {
                val fillWidth = max(0.0, (width - starSize.width) / (max - 1))
                Rectangle(-(fillWidth - starSize.width) / 2, 0.0, fillWidth, height)
            }
        }
    }

    override fun render(canvas: Canvas) {
        val rect = bounds.atOrigin

        if (displayValue < max) {
            val fill = backgroundColor?.paint

            when {
                fill != null -> canvas.rect(rect, PatternPaint(bounds = fillBounds) {
                    when (val s = shadow) {
                        null ->             path(star, stroke, fill)
                        else -> shadow(s) { path(star, stroke, fill) }
                    }
                })
                else         -> canvas.rect(rect, PatternPaint(bounds = fillBounds) {
                    when (val s = shadow) {
                        null ->             path(star, stroke)
                        else -> shadow(s) { path(star, stroke) }
                    }
                })
            }
        }

        if (displayValue > 0) {
            foregroundColor?.let { foregroundColor ->
                val rectWidth = displayValue * starSize.width + (fillBounds.width - starSize.width) * when (max) {
                    1    -> 0.5
                    else -> floor(min(max - 1.0, displayValue))
                }

                canvas.rect(rect.inset(right = width - rectWidth), PatternPaint(bounds = fillBounds) {
                    val s = shadow

                    if (displayValue >= max && s != null) {
                        shadow(s) {
                            path(star, stroke, foregroundColor.paint)
                        }
                    } else {
                        path(star, stroke, foregroundColor.paint)
                    }
                })
            }
        }
    }

    private fun <T> starProperty(initial: T, onChange: View.(old: T, new: T) -> Unit = { _, _ -> }): ReadWriteProperty<StarRater, T> = observable(initial) { old, new ->
        updateStar()
        onChange(old, new)
    }
}

private fun Polygon.translateBy(x: Double = 0.0, y: Double = 0.0): Polygon = object: Polygon() {
    override val points = this@translateBy.points.map { it + Point(x, y) }
    override val area   = this@translateBy.area
    override val empty  = this@translateBy.empty

    override fun contains(point: Point) = point + Point(x, y) in this@translateBy

    override fun contains(rectangle: Rectangle) = rectangle.at(rectangle.x + x, rectangle.y + y) in this@translateBy

    override fun intersects(rectangle: Rectangle) = this@translateBy intersects rectangle.at(rectangle.x + x, rectangle.y + y)
}
