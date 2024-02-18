package io.nacular.doodle.dom

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class DynamicProperty<T: JsAny>(private val name: String, private val onError: ((Throwable) -> String)? = null): ReadWriteProperty<T, String> {
    override fun getValue(thisRef: T, property: KProperty<*>): String = when (onError) {
        null -> (thisRef[name] as? JsString).toString()
        else -> try { (thisRef[name] as? JsString).toString() } catch (throwable: Throwable) { onError.invoke(throwable) }
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: String) {
        try {
            thisRef[name] = value.toJsString()
        } catch (throwable: Throwable) {
            onError?.invoke(throwable)
        }
    }
}

internal class OptionalDynamicProperty<T: JsAny>(private val name: String, private val onError: ((Throwable) -> String?)? = null): ReadWriteProperty<T, String?> {
    override fun getValue(thisRef: T, property: KProperty<*>): String? = when (onError) {
        null -> (thisRef[name] as? JsString).toString()
        else -> try { (thisRef[name] as? JsString).toString() } catch (throwable: Throwable) { onError.invoke(throwable) }
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: String?) {
        try {
            value?.toJsString()?.let {
            thisRef[name] = it
            }
        } catch (throwable: Throwable) {
            onError?.invoke(throwable)
        }
    }
}