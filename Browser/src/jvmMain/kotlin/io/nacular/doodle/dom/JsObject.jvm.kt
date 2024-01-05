package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 1/8/24.
 */
public actual fun jsObject(): JsAny = object: JsAny {}

public actual operator fun JsAny.set(key: String, value: JsAny) {}
public actual operator fun JsAny.get(key: String              ): JsAny? = null