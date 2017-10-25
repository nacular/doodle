package com.zinoti.jaz.drawing

import com.zinoti.jaz.geometry.Point
import com.zinoti.jaz.geometry.Rectangle
import com.zinoti.jaz.geometry.Size

interface GraphicsSurface {
    var position: Point
    var size    : Size
    var bounds  : Rectangle
        get(   ) = Rectangle(position.x, position.y, size.width, size.height)
        set(new) { position = new.position; size = new.size }
    var zIndex  : Int
    var visible : Boolean
    val canvas  : Canvas

    fun beginRender()
    fun endRender  ()
}
