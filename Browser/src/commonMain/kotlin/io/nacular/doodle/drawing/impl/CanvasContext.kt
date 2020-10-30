package io.nacular.doodle.drawing.impl

import io.nacular.doodle.Node
import io.nacular.doodle.drawing.Shadow
import io.nacular.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 10/24/17.
 */

internal interface CanvasContext {
    var size          : Size
    val shadows       : List<Shadow>
    val renderRegion  : Node
    var renderPosition: Node?

    fun markDirty()
}