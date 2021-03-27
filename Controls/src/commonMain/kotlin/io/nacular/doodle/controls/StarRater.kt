package io.nacular.doodle.controls

import io.nacular.doodle.core.View
import io.nacular.doodle.core.renderProperty
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.PatternPaint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.star
import io.nacular.doodle.geometry.withRadius
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.roundToNearest
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates.observable

/**
 * Created by Nicholas Eddy on 4/25/20.
 */
public class StarRater(max: Int = 5, private val displayRounded: Float = 0f): View() {
    public var max: Int = max(0, max)
        set(new) {
            field = max(0, new)
            updateStar()
            value = min(field.toDouble(), value)
        }

    public var innerRadiusRatio: Float? by observable(null) { _,_,_ -> updateStar() }

    public var minSpacing: Double by observable(0.0) { _,_,_ -> updateStar() }

    public var numStarPoints: Int by observable(5) { _,_,_ -> updateStar() }

    public var starRotation: Measure<Angle> by observable(0 * degrees) { _,_,_ -> updateStar() }

    public var shadowColor: Color? by renderProperty(Black opacity 0.2f)

    public var value: Double = 0.0
        set(new) {
            val old = field
            field = max(0.0, min(max.toDouble(), new))

            displayValue = field.roundToNearest(displayRounded.toDouble())
            changed_(old, new)
        }

    private var displayValue by renderProperty(value.roundToNearest(displayRounded.toDouble()))

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { PropertyObserversImpl<StarRater, Double>(this) }

    public val changed: PropertyObservers<StarRater, Double> = changed_

    private lateinit var star: Polygon

    init {
        foregroundColor = Color(0x6200EEu)
        backgroundColor = White

        boundsChanged += { _,_,_ ->
            updateStar()
            rerender()
        }

        styleChanged += {
            rerender()
        }

        clipCanvasToBounds = false

        updateStar()
    }

    private lateinit var fillBounds: Rectangle

    private fun updateStar() {
        val radius = max(0.0, min(width / (2 * max) - minSpacing / 2, height / 2))
        val circle = Circle(Point(0.0, height / 2), radius)

        val tempStart = when (val r2 = innerRadiusRatio) {
            null -> star(circle, points = numStarPoints, rotation = starRotation)
            else -> star(circle, points = numStarPoints, rotation = starRotation, innerCircle = circle.withRadius(radius * r2))
        }!!

        val starWidth = tempStart.boundingRectangle.width

        fillBounds = when(max) {
            1    -> Rectangle(width, height)
            else -> {
                val fillWidth = (width - starWidth * max) / (2 * (max - 1)) * 2 + starWidth
                Rectangle(-(fillWidth - starWidth) / 2, 0.0, fillWidth, height)
            }
        }

        star = tempStart.translateBy(x = fillBounds.width / 2)

        rerender()
    }

    override fun render(canvas: Canvas) {
        val rect = bounds.atOrigin

        if (displayValue < max) {
            canvas.rect(rect, PatternPaint(bounds = fillBounds) {
                shadowColor?.let {
                    outerShadow(color = it, horizontal = 0.0, vertical = 1.0, blurRadius = 4.0) {
                        poly(star, Stroke(Lightgray opacity 0.7f), backgroundColor?.paint)
                    }
                } ?: {
                    poly(star, Stroke(Lightgray opacity 0.7f), backgroundColor?.paint)
                }()
            })
        }

        if (displayValue > 0) {
            foregroundColor?.let { foregroundColor ->
                val starWidth = star.boundingRectangle.width
                val rectWidth = displayValue * starWidth + (fillBounds.width - starWidth) * when (max) {
                    1    -> 0.5
                    else -> floor(displayValue)
                }

                canvas.rect(rect.inset(Insets(right = width - rectWidth)), PatternPaint(bounds = fillBounds) {
                    if (displayValue.toInt() == max) {
                        outerShadow(color = Black opacity 0.2f, horizontal = 0.0, vertical = 1.0, blurRadius = 4.0) {
                            poly(star, foregroundColor.paint)
                        }
                    } else {
                        poly(star, foregroundColor.paint)
                    }
                })
            }
        }
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
