package io.nacular.doodle.dom

public actual external interface JsAny

@JsName("String")
public actual external class JsString: JsAny

@JsName("Number")
public actual external class JsNumber: JsAny

@JsName("Array")
public actual external class JsArray<T: JsAny?>: JsAny {
    public actual val length: Int
}

public actual fun JsNumber.toDouble(): Double {
    return this.unsafeCast<Number>().toDouble()
}

public actual fun String.toJsString(): JsString = this.unsafeCast<JsString>()

public actual operator fun <T: JsAny?> JsArray<T>.get (index: Int          ): T?   = jsGet (this, index       )
public actual operator fun <T: JsAny?> JsArray<T>.set (index: Int, value: T): Unit = jsSet (this, index, value)
public actual          fun <T: JsAny?> JsArray<T>.push(value: T            ): Unit = jsPush(this, value       )

private fun <T: JsAny?> jsGet (array: JsArray<T>, index: Int          ): T? = js("array[index]"        ) as? T
private fun <T: JsAny?> jsSet (array: JsArray<T>, index: Int, value: T)     { js("array[index] = value") }
private fun <T: JsAny?> jsPush(array: JsArray<T>, value: T            )     { js("array.push(value)"   ) }

// TODO: Can this be done w/o custom code?
public actual fun <T: JsAny> jsArrayOf(vararg values: T): JsArray<T> = JsArray<T>().apply {
    values.forEach {
        push(it)
    }
}