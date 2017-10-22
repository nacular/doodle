package com.zinoti.jaz.system


class Cursor private constructor(private val type: String) {

    override fun toString() = type

    companion object {
        fun create(type: String): Cursor {
            var cursor: Cursor? = cursors[type]

            if (cursor == null) {
                cursor = Cursor(type)

                cursors.put(type, cursor)
            }

            return cursor
        }

        val TEXT      = Cursor("text"     )
        val WAIT      = Cursor("wait"     )
        val HELP      = Cursor("help"     )
        val MOVE      = Cursor("move"     )
        val DEFAULT   = Cursor("default"  )
        val POINTER   = Cursor("pointer"  )
        val PROGRESS  = Cursor("progress" )
        val N_RESIZE  = Cursor("n-resize" )
        val S_RESIZE  = Cursor("s-resize" )
        val E_RESIZE  = Cursor("e-resize" )
        val W_RESIZE  = Cursor("w-resize" )
        val NE_RESIZE = Cursor("ne-resize")
        val NW_RESIZE = Cursor("nw-resize")
        val SE_RESIZE = Cursor("se-resize")
        val SW_RESIZE = Cursor("sw-resize")
        val CROSSHAIR = Cursor("crosshair")

        private val cursors = mutableMapOf<String, Cursor>()

        init {
            cursors.put(TEXT.type, TEXT)
            cursors.put(WAIT.type, WAIT)
            cursors.put(HELP.type, HELP)
            cursors.put(MOVE.type, MOVE)
            cursors.put(DEFAULT.type, DEFAULT)
            cursors.put(POINTER.type, POINTER)
            cursors.put(PROGRESS.type, PROGRESS)
            cursors.put(N_RESIZE.type, N_RESIZE)
            cursors.put(S_RESIZE.type, S_RESIZE)
            cursors.put(E_RESIZE.type, E_RESIZE)
            cursors.put(W_RESIZE.type, W_RESIZE)
            cursors.put(NE_RESIZE.type, NE_RESIZE)
            cursors.put(NW_RESIZE.type, NW_RESIZE)
            cursors.put(SE_RESIZE.type, SE_RESIZE)
            cursors.put(SW_RESIZE.type, SW_RESIZE)
            cursors.put(CROSSHAIR.type, CROSSHAIR)
        }
    }
}
