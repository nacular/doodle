package io.nacular.doodle.dom

internal actual operator fun Uint8Array.get(index: Int): Byte = jsGet(this, index)

private fun jsGet(array: Uint8Array, index: Int): Byte = js("array[index]")
