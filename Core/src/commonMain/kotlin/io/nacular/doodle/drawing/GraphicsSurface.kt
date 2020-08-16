package io.nacular.doodle.drawing

import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size

interface GraphicsSurface {
    var position    : Point
    var size        : Size
    var bounds      : Rectangle
        get(   )    = Rectangle(position, size)
        set(new)    { position = new.position; size = new.size }
    var index       : Int
    var zOrder      : Int
    var visible     : Boolean
    var transform   : AffineTransform
    var mirrored    : Boolean
    var clipToBounds: Boolean

    fun render(block: (Canvas) -> Unit)

    fun release()
}
