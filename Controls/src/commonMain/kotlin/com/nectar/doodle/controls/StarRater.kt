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
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.star
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import com.nectar.doodle.utils.roundToNearest
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

    private fun updateStar() {
        val radius = min(width / (2 * max), height / 2)
        val circle = Circle(Point(width/(2 * max), height/2), radius)

        star = when (val r2 = innerRadiusRatio) {
            null -> star(circle)
            else -> star(circle, innerCircle = circle.withRadius(radius * r2))
        }!!
    }

    override fun render(canvas: Canvas) {
        if (displayValue < max) {
            canvas.rect(bounds.atOrigin, PatternBrush(size = Size(width / max, height)) {
                outerShadow(color = black opacity 0.2f, horizontal = 0.0, vertical = 1.0, blurRadius = 4.0) {
                    poly(star, Pen(lightgray.opacity(0.7f)), backgroundColor?.let { ColorBrush(it) })
                }
            })
        }

        if (displayValue > 0) {
            foregroundColor?.let { foregroundColor ->
//                val rectWidth = displayValue * width / max

                canvas.rect(bounds.atOrigin.inset(Insets(right = (max - displayValue) * width / max)), PatternBrush(size = Size(width / max, height)) {
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