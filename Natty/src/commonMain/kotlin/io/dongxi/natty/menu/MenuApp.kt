package io.dongxi.natty.menu

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.application.Application
import io.nacular.doodle.core.Display
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constraints.constrain

/**
 * Simple app that places a [Menu] at the center of the display.
 */
class MenuApp(display: Display, animator: Animator, pathMetrics: PathMetrics) : Application {
    init {

        // create and display a single Menu
        with(display) {
            this += Menu(animator, pathMetrics).apply {
                size = Size(500, 100)
            }

            layout = constrain(first()) {
                it.top eq 2
                it.centerX eq parent.centerX
            }
        }
    }

    override fun shutdown() { /* no-op */
    }
}