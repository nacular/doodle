package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 12/20/23.
 */
//public actual external interface JsAny
//
////@JsName("String")
//public actual external class JsString: JsAny
//
////@JsPrimitive("number")
//public actual external class JsNumber: JsAny
//
////@JsName("Array")
//public actual external class JsArray<T: JsAny?>: JsAny {
//    public actual val length: Int
//}

internal actual fun JsArray<out JsString>.contains(value: JsString): Boolean {
    (0..this.length).forEach {
        if (this[it] == value) return true
    }

    return false
}