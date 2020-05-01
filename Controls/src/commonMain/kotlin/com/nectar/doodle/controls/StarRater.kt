package com.nectar.doodle.controls

import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.black
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.PatternBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Polygon
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.star
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import com.nectar.doodle.utils.roundToNearest
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 4/25/20.
 */
class StarRater(max: Int = 5, private val displayRounded: Float = 0f): View() {
    val max = max(0, max)

    var innerRadiusRatio = null as Float?
        set(new) {
            field = new
            updateStar()
        }

    var minSpacing = 0.0
        set(new) {
            field = new
            updateStar()
        }

    var numStarPoints = 5
        set(new) {
            field = new
            updateStar()
        }

    var value = 0.0
        set(new) {
            val old = field
            field = new

            displayValue = field.roundToNearest(displayRounded.toDouble())
            changed_(old, new)
        }

    private var displayValue = value.roundToNearest(displayRounded.toDouble())
        set(new) {
            val old = field
            field = new

            if (field != old) {
                rerender()
            }
        }

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { PropertyObserversImpl<StarRater, Double>(this) }

    val changed: PropertyObservers<StarRater, Double> = changed_

    private lateinit var star: Polygon

    init {
        foregroundColor = Color(0x6200EEu)
        backgroundColor = white

        boundsChanged += { _,_,_ ->
            updateStar()
            rerender()
        }

        styleChanged += {
            rerender()
        }

        updateStar()
    }

    private lateinit var brushBounds: Rectangle

    private fun updateStar() {
        val radius = max(0.0, min(width / (2 * max) - minSpacing / 2, height / 2))
        val circle = Circle(Point(0.0, height / 2), radius)

        val tempStart = when (val r2 = innerRadiusRatio) {
            null -> star(circle, points = numStarPoints)
            else -> star(circle, points = numStarPoints, innerCircle = circle.withRadius(radius * r2))
        }!!

        val starWidth = tempStart.boundingRectangle.width

        brushBounds = when(max) {
            1    -> Rectangle(width, height)
            else -> {
                val brushWidth = (width - starWidth * max) / (2 * (max - 1)) * 2 + starWidth
                Rectangle(-(brushWidth - starWidth) / 2, 0.0, brushWidth, height)
            }
        }

        star = tempStart.translateBy(x = brushBounds.width / 2)
    }

    override fun render(canvas: Canvas) {
        val rect = bounds.atOrigin

        if (displayValue < max) {
            canvas.rect(rect, PatternBrush(bounds = brushBounds) {
                outerShadow(color = black opacity 0.2f, horizontal = 0.0, vertical = 1.0, blurRadius = 4.0) {
                    poly(star, Pen(lightgray.opacity(0.7f)), backgroundColor?.let { ColorBrush(it) })
                }
            })
        }

        if (displayValue > 0) {
            foregroundColor?.let { foregroundColor ->
                val starWidth = star.boundingRectangle.width
                val rectWidth = displayValue * starWidth + (brushBounds.width - starWidth) * when (max) {
                    1    -> 0.5
                    else -> floor(displayValue)
                }

                canvas.rect(rect.inset(Insets(right = width - rectWidth)), PatternBrush(bounds = brushBounds) {
                    if (displayValue.toInt() == max) {
                        outerShadow(color = black opacity 0.2f, horizontal = 0.0, vertical = 1.0, blurRadius = 4.0) {
                            poly(star, ColorBrush(foregroundColor))
                        }
                    } else {
                        poly(star, ColorBrush(foregroundColor))
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
