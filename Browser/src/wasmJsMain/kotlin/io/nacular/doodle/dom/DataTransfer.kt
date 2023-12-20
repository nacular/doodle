package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/13/20.
 */
internal actual typealias DataTransfer = org.w3c.dom.DataTransfer

internal actual val DataTransfer.types_: Array<out String> get() = Array(this.types.length) { this.types[it]!!.toString() }