package io.nacular.doodle.dom

/** @suppress */
public actual interface JsAny

/** @suppress */
public actual class JsString: JsAny

/** @suppress */
public actual fun String.toJsString(): JsString = JsString()

/** @suppress */
public actual class JsNumber: JsAny

internal actual fun JsNumber.toDouble(): Double = 0.0

/** @suppress */
public actual class JsArray<T: JsAny?>: JsAny {
    internal actual val length: Int = 0
}

internal actual operator fun <T: JsAny?> JsArray<T>.get(index: Int): T? = null
internal actual operator fun <T : JsAny?> JsArray<T>.set(index: Int, value: T) {}
internal actual fun <T: JsAny?> JsArray<T>.push(value: T) {}

internal actual operator fun JsArray<out JsString>.contains(value: JsString): Boolean {
    (0..this.length).forEach {
        if (this[it] == value) return true
    }

    return false
}

internal actual fun <T : JsAny> jsArrayOf(vararg values: T): JsArray<T> = JsArray()