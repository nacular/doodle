package com.nectar.doodle.system


class Cursor private constructor(private val type: String) {

    override fun toString() = type

    companion object {
        operator fun invoke(type: String) = cursors.getOrPut(type.toLowerCase()) {
            Cursor(type)
        }

        val Text      = Cursor("text"      )
        val Wait      = Cursor("wait"      )
        val Help      = Cursor("help"      )
        val Move      = Cursor("move"      )
        val Grab      = Cursor("grab"      )
        val Copy      = Cursor("copy"      )
        val Alias     = Cursor("alias"     )
        val ZoomIn    = Cursor("zoom-in"   )
        val NoDrop    = Cursor("no-drop"   )
        val ZoonOut   = Cursor("zoom-out"  )
        val Default   = Cursor("default"   )
        val Pointer   = Cursor("pointer"   )
        val NResize   = Cursor("n-resize"  )
        val SResize   = Cursor("s-resize"  )
        val EResize   = Cursor("e-resize"  )
        val WResize   = Cursor("w-resize"  )
        val Grabbing  = Cursor("grabbing"  )
        val Progress  = Cursor("progress"  )
        val NeResize  = Cursor("ne-resize" )
        val NwResize  = Cursor("nw-resize" )
        val SeResize  = Cursor("se-resize" )
        val SwResize  = Cursor("sw-resize" )
        val Crosshair = Cursor("crosshair" )
        val ColResize = Cursor("col-resize")
        val RowResize = Cursor("row-resize")

        private val cursors = mutableMapOf<String, Cursor>()

        init {
            add(Text     )
            add(Wait     )
            add(Help     )
            add(Move     )
            add(Default  )
            add(Pointer  )
            add(Progress )
            add(NResize  )
            add(SResize  )
            add(EResize  )
            add(WResize  )
            add(NeResize )
            add(NwResize )
            add(SeResize )
            add(SwResize )
            add(Crosshair)
        }

        private fun add(cursor: Cursor) {
            cursors[cursor.type] = cursor
        }
    }
}
