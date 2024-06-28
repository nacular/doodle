package io.nacular.doodle.core.impl

import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.impl.RealGraphicsSurface
import io.nacular.doodle.geometry.Point
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.skia.Font
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skiko.SkiaLayer
import kotlin.coroutines.CoroutineContext

/**
 * Created by Nicholas Eddy on 5/31/24.
 */
internal interface DisplaySkiko: InternalDisplay {
    val locationOnScreen: Point
    val indexInParent   : Int

    fun shutdown()
}

internal interface DisplayFactory {
    operator fun invoke(
        appScope      : CoroutineScope,
        uiDispatcher  : CoroutineContext,
        skiaLayer     : SkiaLayer,
        defaultFont   : Font,
        fontCollection: FontCollection,
        device        : GraphicsDevice<RealGraphicsSurface>
    ): DisplaySkiko
}

internal class DisplayFactoryImpl: DisplayFactory {
    override fun invoke(
        appScope: CoroutineScope,
        uiDispatcher: CoroutineContext,
        skiaLayer: SkiaLayer,
        defaultFont: Font,
        fontCollection: FontCollection,
        device: GraphicsDevice<RealGraphicsSurface>
    ) = DisplayImpl(appScope, uiDispatcher, skiaLayer, defaultFont, fontCollection, device)
}