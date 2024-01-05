package io.nacular.doodle.dom

import kotlin.js.toDouble
import kotlin.js.get        as jsGet
import kotlin.js.set        as jsSet
import kotlin.js.toJsString as jsToJsString

public actual typealias JsAny      = kotlin.js.JsAny
public actual typealias JsString   = kotlin.js.JsString
public actual typealias JsArray<T> = kotlin.js.JsArray<T>
public actual typealias JsNumber   = kotlin.js.JsNumber

public actual fun JsNumber.toDouble(): Double = this.toDouble()

public actual operator fun <T: JsAny?> JsArray<T>.get (index: Int          ): T?   = this.jsGet(index)
public actual operator fun <T: JsAny?> JsArray<T>.set (index: Int, value: T): Unit = this.jsSet(index, value)
public actual inline   fun <T: JsAny?> JsArray<T>.push(value: T            ): Unit = jsPush(this, value)

public fun <T: JsAny?> jsPush(array: JsArray<T>, value: T): Unit = js("array.push(value)")

public actual fun String.toJsString(): JsString = this.jsToJsString()

// TODO: Can this be done w/o custom code?
public actual fun <T: JsAny> jsArrayOf(vararg values: T): JsArray<T> = JsArray<T>().apply {
    values.forEach {
        push(it)
    }
}