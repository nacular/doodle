@file:OptIn(ExperimentalWasmJsInterop::class)

package io.nacular.doodle.dom

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => ( {} )")
internal actual external fun jsObject(): JsAny

internal inline fun <T : JsAny> jsObject(init: T.() -> Unit): T = (jsObject().unsafeCast<T>()).apply(init)

internal actual operator fun JsAny.set(key: String, value: JsAny): Unit   = objectSet(this, key, value)
internal actual operator fun JsAny.get(key: String              ): JsAny? = objectGet(this, key       )

internal actual fun JsAny.setString(key: String, value: String): Unit   = objectSet(this, key, value.toJsString())
internal actual fun JsAny.getString(key: String               ): String = objectGet(this, key) as? String ?: ""

@JsFun("(obj, key, value) => ( obj[key] = value )")
private external fun objectSet(obj: JsAny, key: String, value: JsAny)

@JsFun("(obj, key) => ( obj[key] )")
private external fun objectGet(obj: JsAny, key: String): JsAny?
