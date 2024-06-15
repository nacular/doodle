package io.nacular.doodle.dom

/** @suppress */
public actual external interface JsAny

@JsName("String")
/** @suppress */
public actual external class JsString: JsAny

@JsName("Number")
/** @suppress */
public actual external class JsNumber: JsAny

@JsName("Array")
/** @suppress */
public actual external class JsArray<T: JsAny?>: JsAny {
    internal actual val length: Int
}

internal actual fun JsNumber.toDouble(): Double {
    return this.unsafeCast<Number>().toDouble()
}

internal actual fun String.toJsString(): JsString = this.unsafeCast<JsString>()

internal actual operator fun <T: JsAny?> JsArray<T>.get (index: Int          ): T?   = jsGet (this, index       )
internal actual operator fun <T: JsAny?> JsArray<T>.set (index: Int, value: T): Unit = jsSet (this, index, value)
internal actual          fun <T: JsAny?> JsArray<T>.push(value: T            ): Unit = jsPush(this, value       )

private fun <T: JsAny?> jsGet (array: JsArray<T>, index: Int          ): T? = js("array[index]"        ) as? T
private fun <T: JsAny?> jsSet (array: JsArray<T>, index: Int, value: T)     { js("array[index] = value") }
private fun <T: JsAny?> jsPush(array: JsArray<T>, value: T            )     { js("array.push(value)"   ) }

// TODO: Can this be done w/o custom code?
internal actual fun <T: JsAny> jsArrayOf(vararg values: T): JsArray<T> = JsArray<T>().apply {
    values.forEach {
        push(it)
    }
}

internal actual operator fun DataTransferItemList.get(index: Int): DataTransferItem? = jsGet(this, index)

private fun jsGet(list: DataTransferItemList, index: Int): DataTransferItem? = js("list[index]") as? DataTransferItem?
