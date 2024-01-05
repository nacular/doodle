@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.dom

public expect external interface JsAny

public expect external class JsString: JsAny

public expect external class JsNumber: JsAny

public expect fun JsNumber.toDouble(): Double

public expect fun String.toJsString(): JsString

public expect external class JsArray<T: JsAny?>: JsAny {
    public val length: Int
}

public expect operator fun <T: JsAny?> JsArray<T>.get(index: Int          ): T?
public expect operator fun <T: JsAny?> JsArray<T>.set(index: Int, value: T)
public expect fun <T: JsAny?> JsArray<T>.push(value: T)

public expect fun JsArray<out JsString>.contains(value: JsString): Boolean

public expect external fun <T : JsAny> jsArrayOf(vararg values: T): JsArray<T>