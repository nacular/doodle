package io.nacular.doodle.drawing

import io.nacular.doodle.core.Camera
import io.nacular.doodle.core.Internal
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size

@Internal
public interface GraphicsSurface {
    public var position          : Point
    public var size              : Size
    public var bounds            : Rectangle
        get(   )                 = Rectangle(position, size)
        set(new)                 { position = new.position; size = new.size }
    public var index             : Int
    public var zOrder            : Int
    public var visible           : Boolean
    public var opacity           : Float
    public var transform         : AffineTransform
    public var camera            : Camera
    public var mirrored          : Boolean
    public var clipCanvasToBounds: Boolean
    public var childrenClipPath  : Path?

    public fun render(block: (Canvas) -> Unit)

    public fun release()
}
