package com.nectar.doodle.theme.basic

import com.nectar.doodle.controls.buttons.Switch
import com.nectar.doodle.controls.theme.CommonButtonBehavior
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.Blue
import com.nectar.doodle.drawing.Color.Companion.Gray
import com.nectar.doodle.drawing.Color.Companion.Lightgray
import com.nectar.doodle.drawing.Color.Companion.White
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Point


class BasicSwitchBehavior(
        private val onBackground      : Color = Blue,
        private val onForeground      : Color = White,
        private val offBackground     : Color = Lightgray,
        private val offForeground     : Color = onForeground,
        private val disabledBackground: Color = offBackground,
        private val disabledForeground: Color = Gray): CommonButtonBehavior<Switch>() {

    override fun render(view: Switch, canvas: Canvas) {
        val radius = view.height / 2
        val border = maxOf(2.0, view.height / 20)

        val backgroundColor = when {
            !view.enabled -> disabledBackground
            view.selected -> onBackground
            else          -> offBackground
        }

//        canvas.innerShadow(blurRadius = 5.0) {
        canvas.rect(view.bounds.atOrigin, radius, ColorBrush(backgroundColor))
//        }

        val circleColor = when {
            !view.enabled -> disabledForeground
            view.selected -> onForeground
            else          -> offForeground
        }

        when {
            view.selected -> canvas.circle(Circle(Point(view.width - radius, radius), radius - border), ColorBrush(circleColor))
            else          -> canvas.circle(Circle(Point(radius, radius), radius - border), ColorBrush(circleColor))
        }
    }
}