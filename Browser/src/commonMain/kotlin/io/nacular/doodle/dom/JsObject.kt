package io.nacular.doodle.dom

public expect fun jsObject(): JsAny

public expect operator fun JsAny.set(key: String, value: JsAny)
public expect operator fun JsAny.get(key: String              ): JsAny?