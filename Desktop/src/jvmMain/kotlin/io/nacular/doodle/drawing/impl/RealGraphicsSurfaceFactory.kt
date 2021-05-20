package io.nacular.doodle.drawing.impl

import io.nacular.doodle.core.View
import org.jetbrains.skija.Font
import org.jetbrains.skiko.SkiaWindow

/**
 * Created by Nicholas Eddy on 5/19/21.
 */
internal class RealGraphicsSurfaceFactory(private val window: SkiaWindow, private val defaultFont: Font): GraphicsSurfaceFactory<RealGraphicsSurface> {
    override fun invoke() = RealGraphicsSurface(window, parent = null, isContainer = false, addToRootIfNoParent = false, defaultFont = defaultFont)

    override fun invoke(parent             : RealGraphicsSurface?,
                        view               : View,
                        isContainer        : Boolean,
                        addToRootIfNoParent: Boolean
    ) = RealGraphicsSurface(window, defaultFont, parent, isContainer, addToRootIfNoParent)
}