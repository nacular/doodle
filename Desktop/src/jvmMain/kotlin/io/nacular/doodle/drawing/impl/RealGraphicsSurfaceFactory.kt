package io.nacular.doodle.drawing.impl

import io.nacular.doodle.core.View
import org.jetbrains.skia.Font
import org.jetbrains.skia.paragraph.FontCollection

/**
 * Created by Nicholas Eddy on 5/19/21.
 */
internal class RealGraphicsSurfaceFactory(
    private val parent        : GraphicsSurfaceParent,
    private val defaultFont   : Font,
    private val fontCollection: FontCollection,
): GraphicsSurfaceFactory<RealGraphicsSurface> {
    override fun invoke() = RealGraphicsSurface(
        parent         = parent,
        defaultFont    = defaultFont,
        fontCollection = fontCollection,
    )

    override fun invoke(
        parent             : RealGraphicsSurface?,
        view               : View,
        isContainer        : Boolean,
        addToRootIfNoParent: Boolean
    ) = RealGraphicsSurface(
        parent         = parent ?: this.parent,
        defaultFont    = defaultFont,
        fontCollection = fontCollection,
    )
}