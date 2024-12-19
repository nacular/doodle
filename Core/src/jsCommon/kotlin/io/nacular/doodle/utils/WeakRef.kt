@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.utils

internal actual class WeakReference<T: Any> actual constructor(value: T) {
    private val weakRef = WeakRef(value as JsAny)

    actual operator fun invoke() = weakRef.deref() as? T?
}

/** @suppress */
public expect external interface JsAny

private external class WeakRef(target: JsAny) {
    fun deref(): JsAny?
}