package io.nacular.doodle.drawing

import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size

public interface GraphicsSurface {
    public var position           : Point
    public var size               : Size
    public var bounds             : Rectangle
        get(   )           = Rectangle(position, size)
        set(new)           { position = new.position; size = new.size }
    public var index              : Int
    public var zOrder             : Int
    public var visible            : Boolean
    public var transform          : AffineTransform
    public var mirrored           : Boolean
    public var clipCanvasToBounds : Boolean
    public var childdrenClipPoly  : Polygon?

    public fun render(block: (Canvas) -> Unit)

    public fun release()
}
