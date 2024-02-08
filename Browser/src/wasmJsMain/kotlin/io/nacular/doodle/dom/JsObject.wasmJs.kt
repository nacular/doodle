package io.nacular.doodle.dom

@JsFun("() => ( {} )")
internal actual external fun jsObject(): JsAny

internal inline fun <T : JsAny> jsObject(init: T.() -> Unit): T = (jsObject().unsafeCast<T>()).apply(init)

internal actual operator fun JsAny.set(key: String, value: JsAny): Unit   = objectSet(this, key, value)
internal actual operator fun JsAny.get(key: String              ): JsAny? = objectGet(this, key       )

@JsFun("(obj, key, value) => ( obj[key] = value )")
private external fun objectSet(obj: JsAny, key: String, value: JsAny)

@JsFun("(obj, key) => ( obj[key] )")
private external fun objectGet(obj: JsAny, key: String): JsAny?
