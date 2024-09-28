package io.nacular.doodle.theme.native

import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Canvas as SkiaCanvas

/**
 * Created by Nicholas Eddy on 8/9/21.
 */
internal interface SwingGraphicsFactory {
    operator fun invoke(fontManager: FontMgr, skiaCanvas: SkiaCanvas): SkiaGraphics2D
}