package io.nacular.doodle.system


public class Cursor private constructor(private val type: String) {

    override fun toString(): String = type

    public companion object {
        public val None     : Cursor = Cursor("none"      )
        public val Text     : Cursor = Cursor("text"      )
        public val Wait     : Cursor = Cursor("wait"      )
        public val Help     : Cursor = Cursor("help"      )
        public val Move     : Cursor = Cursor("move"      )
        public val Grab     : Cursor = Cursor("grab"      )
        public val Copy     : Cursor = Cursor("copy"      )
        public val Alias    : Cursor = Cursor("alias"     )
        public val ZoomIn   : Cursor = Cursor("zoom-in"   )
        public val NoDrop   : Cursor = Cursor("no-drop"   )
        public val ZoomOut  : Cursor = Cursor("zoom-out"  )
        public val Default  : Cursor = Cursor("default"   )
        public val Pointer  : Cursor = Cursor("pointer"   )
        public val NResize  : Cursor = Cursor("n-resize"  )
        public val SResize  : Cursor = Cursor("s-resize"  )
        public val EResize  : Cursor = Cursor("e-resize"  )
        public val WResize  : Cursor = Cursor("w-resize"  )
        public val EWResize : Cursor = Cursor("ew-resize" )
        public val Grabbing : Cursor = Cursor("grabbing"  )
        public val Progress : Cursor = Cursor("progress"  )
        public val NeResize : Cursor = Cursor("ne-resize" )
        public val NwResize : Cursor = Cursor("nw-resize" )
        public val SeResize : Cursor = Cursor("se-resize" )
        public val SwResize : Cursor = Cursor("sw-resize" )
        public val Crosshair: Cursor = Cursor("crosshair" )
        public val ColResize: Cursor = Cursor("col-resize")
        public val RowResize: Cursor = Cursor("row-resize")

        public fun custom(url: String, or: Cursor): Cursor = Cursor("url('$url'), $or")
    }
}
