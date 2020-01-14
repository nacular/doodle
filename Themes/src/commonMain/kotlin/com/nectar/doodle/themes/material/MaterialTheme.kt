package com.nectar.doodle.themes.material

import com.nectar.doodle.animation.AnimatorFactory
import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.controls.theme.basic.BasicTheme
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.FontDetector
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.focus.FocusManager

/**
 * Created by Nicholas Eddy on 1/8/20.
 */
class MaterialTheme(
        private val textMetrics    : TextMetrics,
                    labelFactory   : LabelFactory,
        private val fontDetector   : FontDetector,
                    focusManager   : FocusManager?,
        private val animatorFactory: AnimatorFactory): BasicTheme(labelFactory, textMetrics, focusManager) {
    override fun install(display: Display, all: Sequence<View>) = all.forEach {
        when (it) {
            is Button -> it.behavior = MaterialButtonBehavior(textMetrics, animatorFactory, fontDetector, textColor = Color.white, backgroundColor = Color(0x6200EEu), cornerRadius = 4.0)
            else      -> super.install(display, sequenceOf(it))
        }
    }
}