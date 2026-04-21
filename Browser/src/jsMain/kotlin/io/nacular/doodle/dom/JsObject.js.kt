package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 1/8/24.
 */
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
internal actual inline fun jsObject(): JsAny = js("{}")

internal inline fun <T : Any> jsObject(init: T.() -> Unit): T = (js("{}").unsafeCast<T>()).apply(init)

internal actual operator fun JsAny.get(key: String              ): JsAny? = asDynamic()[key]?.unsafeCast<JsAny>()
internal actual operator fun JsAny.set(key: String, value: JsAny)         { asDynamic()[key] = value }

internal actual fun JsAny.getString(key: String               ): String = asDynamic()[key]?.unsafeCast<String>() ?: ""
internal actual fun JsAny.setString(key: String, value: String)         { asDynamic()[key] = value }