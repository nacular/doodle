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

internal actual fun JsAny.isNode             (): Boolean = js("this instanceof Node"             )
internal actual fun JsAny.isHTMLElement      (): Boolean = js("this instanceof HTMLElement"      )
internal actual fun JsAny.isHTMLInputElement (): Boolean = js("this instanceof HTMLInputElement" )
internal actual fun JsAny.isHTMLButtonElement(): Boolean = js("this instanceof HTMLAnchorElement")
internal actual fun JsAny.isHTMLAnchorElement(): Boolean = js("this instanceof HTMLButtonElement")

internal actual fun JsAny.asNodeOrNull             (): Node?              = if (this.isNode             ()) this.unsafeCast<Node>             () else null
internal actual fun JsAny.asHTMLElementOrNull      (): HTMLElement?       = if (this.isHTMLElement      ()) this.unsafeCast<HTMLElement>      () else null
internal actual fun JsAny.asHTMLInputElementOrNull (): HTMLInputElement?  = if (this.isHTMLInputElement ()) this.unsafeCast<HTMLInputElement> () else null
internal actual fun JsAny.asHTMLButtonElementOrNull(): HTMLButtonElement? = if (this.isHTMLButtonElement()) this.unsafeCast<HTMLButtonElement>() else null
internal actual fun JsAny.asHTMLAnchorElementOrNull(): HTMLAnchorElement? = if (this.isHTMLAnchorElement()) this.unsafeCast<HTMLAnchorElement>() else null