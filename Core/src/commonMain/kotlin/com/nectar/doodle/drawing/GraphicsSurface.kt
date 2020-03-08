package com.nectar.doodle.drawing

import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size

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
    var clipToBounds: Boolean

    fun render(block: (Canvas) -> Unit)

    fun release()
}
