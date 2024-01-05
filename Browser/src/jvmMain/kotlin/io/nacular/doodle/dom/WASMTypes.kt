package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 12/20/23.
 */
public actual interface JsAny

public actual class JsString: JsAny

public actual fun String.toJsString(): JsString = JsString()

public actual class JsNumber: JsAny

public actual fun JsNumber.toDouble(): Double = 0.0

public actual class JsArray<T: JsAny?>: JsAny {
    public actual val length: Int = 0
}

public actual operator fun <T: JsAny?> JsArray<T>.get(index: Int): T? = null
public actual operator fun <T : JsAny?> JsArray<T>.set(index: Int, value: T) {}
public actual fun <T: JsAny?> JsArray<T>.push(value: T) {}

public actual fun JsArray<out JsString>.contains(value: JsString): Boolean {
    (0..this.length).forEach {
        if (this[it] == value) return true
    }

    return false
}

public actual fun <T : JsAny> jsArrayOf(vararg values: T): JsArray<T> = JsArray()