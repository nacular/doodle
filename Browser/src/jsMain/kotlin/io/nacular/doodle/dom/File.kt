package io.nacular.doodle.dom

public actual operator fun Uint8Array.get(index: Int): Byte = this.asDynamic()[index] as Byte
