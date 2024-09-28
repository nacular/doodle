package io.nacular.doodle.drawing.impl

import io.nacular.doodle.core.View
import org.jetbrains.skia.Font
import org.jetbrains.skia.paragraph.FontCollection

/**
 * Created by Nicholas Eddy on 5/19/21.
 */
internal class RealGraphicsSurfaceFactory(
    private val defaultFont   : Font,
    private val fontCollection: FontCollection,
    private val onPaintNeeded: () -> Unit,
): GraphicsSurfaceFactory<RealGraphicsSurface> {
    override fun invoke() = RealGraphicsSurface(defaultFont, parent = null, fontCollection = fontCollection, onPaintNeeded = onPaintNeeded)

    override fun invoke(parent             : RealGraphicsSurface?,
                        view               : View,
                        isContainer        : Boolean,
                        addToRootIfNoParent: Boolean
    ) = RealGraphicsSurface(defaultFont, fontCollection, parent, onPaintNeeded)
}