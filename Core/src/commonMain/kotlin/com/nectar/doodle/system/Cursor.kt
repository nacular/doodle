package com.nectar.doodle.system


class Cursor private constructor(private val type: String) {

    override fun toString() = type

    companion object {
        val None      = Cursor("none"      )
        val Text      = Cursor("text"      )
        val Wait      = Cursor("wait"      )
        val Help      = Cursor("help"      )
        val Move      = Cursor("move"      )
        val Grab      = Cursor("grab"      )
        val Copy      = Cursor("copy"      )
        val Alias     = Cursor("alias"     )
        val ZoomIn    = Cursor("zoom-in"   )
        val NoDrop    = Cursor("no-drop"   )
        val ZoomOut   = Cursor("zoom-out"  )
        val Default   = Cursor("default"   )
        val Pointer   = Cursor("pointer"   )
        val NResize   = Cursor("n-resize"  )
        val SResize   = Cursor("s-resize"  )
        val EResize   = Cursor("e-resize"  )
        val WResize   = Cursor("w-resize"  )
        val EWResize  = Cursor("ew-resize" )
        val Grabbing  = Cursor("grabbing"  )
        val Progress  = Cursor("progress"  )
        val NeResize  = Cursor("ne-resize" )
        val NwResize  = Cursor("nw-resize" )
        val SeResize  = Cursor("se-resize" )
        val SwResize  = Cursor("sw-resize" )
        val Crosshair = Cursor("crosshair" )
        val ColResize = Cursor("col-resize")
        val RowResize = Cursor("row-resize")
    }
}
