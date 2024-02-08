package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 1/8/24.
 */
internal actual fun jsObject(): JsAny = object: JsAny {}

internal actual operator fun JsAny.set(key: String, value: JsAny) {}
internal actual operator fun JsAny.get(key: String              ): JsAny? = null