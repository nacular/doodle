package com.nectar.doodle.system


class Cursor private constructor(private val type: String) {

    override fun toString() = type

    companion object {
        fun create(type: String) = cursors.getOrPut(type) {
            Cursor(type)
        }

        val Text      = Cursor("text"     )
        val Wait      = Cursor("wait"     )
        val Help      = Cursor("help"     )
        val Move      = Cursor("move"     )
        val Default   = Cursor("default"  )
        val Pointer   = Cursor("pointer"  )
        val Progress  = Cursor("progress" )
        val NResize   = Cursor("n-resize" )
        val SResize   = Cursor("s-resize" )
        val EResize   = Cursor("e-resize" )
        val WResize   = Cursor("w-resize" )
        val NeResize  = Cursor("ne-resize")
        val NwResize  = Cursor("nw-resize")
        val SeResize  = Cursor("se-resize")
        val SwResize  = Cursor("sw-resize")
        val Crosshair = Cursor("crosshair")

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
