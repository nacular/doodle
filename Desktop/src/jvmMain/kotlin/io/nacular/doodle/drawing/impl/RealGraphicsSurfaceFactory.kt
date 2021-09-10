package io.nacular.doodle.drawing.impl

import io.nacular.doodle.core.View
import org.jetbrains.skia.Font
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skiko.SkiaWindow

/**
 * Created by Nicholas Eddy on 5/19/21.
 */
internal class RealGraphicsSurfaceFactory(
        private val window        : SkiaWindow,
        private val defaultFont   : Font,
        private val fontCollection: FontCollection,
): GraphicsSurfaceFactory<RealGraphicsSurface> {
    override fun invoke() = RealGraphicsSurface(window, defaultFont, parent = null, fontCollection = fontCollection)

    override fun invoke(parent             : RealGraphicsSurface?,
                        view               : View,
                        isContainer        : Boolean,
                        addToRootIfNoParent: Boolean
    ) = RealGraphicsSurface(window, defaultFont, fontCollection, parent)
}