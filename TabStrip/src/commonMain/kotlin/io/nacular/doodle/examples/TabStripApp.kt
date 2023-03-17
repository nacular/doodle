package io.nacular.doodle.examples

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.application.Application
import io.nacular.doodle.core.Display
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constraints.center
import io.nacular.doodle.layout.constraints.constrain

/**
 * Simple app that places a [TabStrip] at the center of the display.
 */
class TabStripApp(display: Display, animator: Animator, pathMetrics: PathMetrics): Application {
    init {
        // creat and display a single TabStrip
        with(display) {
            this += TabStrip(animator, pathMetrics).apply {
                size = Size(375, 100)
            }

            layout = constrain(first(), center)
        }
    }

    override fun shutdown() { /* no-op */ }
}