package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 12/20/23.
 */
public actual interface JsAny

public actual class JsString: JsAny

public actual fun String.toJsString(): JsString = JsString()

public actual class JsNumber: JsAny

internal actual fun JsNumber.toDouble(): Double = 0.0

public actual class JsArray<T: JsAny?>: JsAny {
    internal actual val length: Int = 0
}

internal actual operator fun <T: JsAny?> JsArray<T>.get(index: Int): T? = null
internal actual operator fun <T : JsAny?> JsArray<T>.set(index: Int, value: T) {}
internal actual fun <T: JsAny?> JsArray<T>.push(value: T) {}

internal actual fun JsArray<out JsString>.contains(value: JsString): Boolean {
    (0..this.length).forEach {
        if (this[it] == value) return true
    }

    return false
}

internal actual fun <T : JsAny> jsArrayOf(vararg values: T): JsArray<T> = JsArray()