@file:OptIn(ExperimentalSkikoApi::class)

package io.nacular.doodle.core.impl

import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.impl.RealGraphicsSurface
import io.nacular.doodle.geometry.Point
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.skia.Font
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.SkiaLayer
import javax.accessibility.Accessible
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.coroutines.CoroutineContext

/**
 * Created by Nicholas Eddy on 5/31/24.
 */
internal interface DisplaySkiko: InternalDisplay {
    val locationOnScreen: Point
    val indexInParent   : Int
    val panel           : JPanel
    val surfaces        : MutableList<RealGraphicsSurface>

    fun syncSize   ()
    fun shutdown   ()
    fun paintNeeded()
}

internal interface DisplayFactory {
    operator fun invoke(
        appScope      : CoroutineScope,
        uiDispatcher  : CoroutineContext,
        accessible    : Accessible,
        defaultFont   : Font,
        fontCollection: FontCollection,
        device        : GraphicsDevice<RealGraphicsSurface>,
        targetWindow  : JFrame
    ): DisplaySkiko
}

internal class DisplayFactoryImpl: DisplayFactory {
    override fun invoke(
        appScope      : CoroutineScope,
        uiDispatcher  : CoroutineContext,
        accessible    : Accessible,
        defaultFont   : Font,
        fontCollection: FontCollection,
        device        : GraphicsDevice<RealGraphicsSurface>,
        targetWindow  : JFrame,
    ) = DisplayImpl(appScope, uiDispatcher, defaultFont, fontCollection, device, targetWindow) {
        SkiaLayer(externalAccessibleFactory = { accessible })
    }
}