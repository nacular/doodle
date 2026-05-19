@file:OptIn(ExperimentalWasmJsInterop::class)

package io.nacular.doodle.dom

import kotlin.js.toDouble
import kotlin.js.get        as jsGet
import kotlin.js.set        as jsSet
import kotlin.js.toJsString as jsToJsString

internal actual typealias JsAny      = kotlin.js.JsAny
internal actual typealias JsString   = kotlin.js.JsString
internal actual typealias JsArray<T> = kotlin.js.JsArray<T>
internal actual typealias JsNumber   = kotlin.js.JsNumber

internal actual fun JsAny.isNode             (): Boolean = isNode             (this)
internal actual fun JsAny.isHTMLElement      (): Boolean = isHTMLElement      (this)
internal actual fun JsAny.isHTMLInputElement (): Boolean = isHTMLInputElement (this)
internal actual fun JsAny.isHTMLButtonElement(): Boolean = isHTMLButtonElement(this)
internal actual fun JsAny.isHTMLAnchorElement(): Boolean = isHTMLAnchorElement(this)

internal actual fun JsAny.asNodeOrNull             (): Node?              = if (this.isNode             ()) this.unsafeCast() else null
internal actual fun JsAny.asHTMLElementOrNull      (): HTMLElement?       = if (this.isHTMLElement      ()) this.unsafeCast() else null
internal actual fun JsAny.asHTMLInputElementOrNull (): HTMLInputElement?  = if (this.isHTMLInputElement ()) this.unsafeCast() else null
internal actual fun JsAny.asHTMLButtonElementOrNull(): HTMLButtonElement? = if (this.isHTMLButtonElement()) this.unsafeCast() else null
internal actual fun JsAny.asHTMLAnchorElementOrNull(): HTMLAnchorElement? = if (this.isHTMLAnchorElement()) this.unsafeCast() else null

private fun isNode             (value: JsAny): Boolean = js("value instanceof Node"             )
private fun isHTMLElement      (value: JsAny): Boolean = js("value instanceof HTMLElement"      )
private fun isHTMLInputElement (value: JsAny): Boolean = js("value instanceof HTMLInputElement" )
private fun isHTMLButtonElement(value: JsAny): Boolean = js("value instanceof HTMLAnchorElement")
private fun isHTMLAnchorElement(value: JsAny): Boolean = js("value instanceof HTMLButtonElement")

internal actual fun JsNumber.toDouble(): Double = this.toDouble()

internal actual operator fun <T: JsAny?> JsArray<T>.get (index: Int          ): T?   = this.jsGet(index)
internal actual operator fun <T: JsAny?> JsArray<T>.set (index: Int, value: T): Unit = this.jsSet(index, value)
internal actual inline   fun <T: JsAny?> JsArray<T>.push(value: T            ): Unit = jsPush(this, value)

private fun <T: JsAny?> jsPush(array: JsArray<T>, value: T): Unit = js("array.push(value)")

internal actual fun String.toJsString(): JsString = this.jsToJsString()

// TODO: Can this be done w/o custom code?
internal actual fun <T: JsAny> jsArrayOf(vararg values: T): JsArray<T> = JsArray<T>().apply {
    values.forEach {
        push(it)
    }
}

internal actual operator fun DataTransferItemList.get(index: Int): DataTransferItem? = jsGet(this, index)

private fun jsGet(list: DataTransferItemList, index: Int): DataTransferItem = js("list[index]")