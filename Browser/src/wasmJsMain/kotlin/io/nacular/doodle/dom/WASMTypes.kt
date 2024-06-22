package io.nacular.doodle.dom

import kotlin.js.toDouble
import kotlin.js.get        as jsGet
import kotlin.js.set        as jsSet
import kotlin.js.toJsString as jsToJsString

internal actual typealias JsAny      = kotlin.js.JsAny
internal actual typealias JsString   = kotlin.js.JsString
internal actual typealias JsArray<T> = kotlin.js.JsArray<T>
internal actual typealias JsNumber   = kotlin.js.JsNumber

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

private fun jsGet(list: DataTransferItemList, index: Int): DataTransferItem? = js("list[index]")