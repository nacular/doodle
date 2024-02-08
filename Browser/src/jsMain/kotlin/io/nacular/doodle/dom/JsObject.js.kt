package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 1/8/24.
 */
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
internal actual inline fun jsObject(): JsAny {
    return js("{}")
}

internal inline fun <T : Any> jsObject(init: T.() -> Unit): T {
    return (js("{}").unsafeCast<T>()).apply(init)
}

internal actual operator fun JsAny.set(key: String, value: JsAny) {
    this.asDynamic()[key] = value
}

internal actual operator fun JsAny.get(key: String): JsAny? = this.asDynamic()[key]?.unsafeCast<JsAny>() //as JsAny?
//public actual operator fun <T: JsAny> JsAny.get(key: String): T? = this.asDynamic()[key] as T?