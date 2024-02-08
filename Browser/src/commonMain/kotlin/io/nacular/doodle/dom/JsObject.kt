package io.nacular.doodle.dom

internal expect fun jsObject(): JsAny

internal expect operator fun JsAny.set(key: String, value: JsAny)
internal expect operator fun JsAny.get(key: String              ): JsAny?