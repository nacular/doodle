package io.nacular.doodle.theme.native

import org.jetbrains.skija.Canvas as SkijaCanvas

/**
 * Created by Nicholas Eddy on 8/9/21.
 */
internal interface SwingGraphicsFactory {
    operator fun invoke(skiaCanvas: SkijaCanvas): SkiaGraphics2D
}