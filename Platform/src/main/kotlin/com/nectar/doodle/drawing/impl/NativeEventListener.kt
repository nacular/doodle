package com.nectar.doodle.drawing.impl


interface NativeEventListener {
    fun onClick      () = true
    fun onFocusGained() = true
    fun onFocusLost  () = true
    fun onKeyUp      () = true
    fun onKeyDown    () = true
    fun onKeyPress   () = true
}
