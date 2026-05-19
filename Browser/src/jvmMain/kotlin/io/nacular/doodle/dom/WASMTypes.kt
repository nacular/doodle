package io.nacular.doodle.dom

/** @suppress */
public actual interface JsAny

/** @suppress */
public actual class JsString: JsAny

/** @suppress */
public actual fun String.toJsString(): JsString = JsString()

/** @suppress */
public actual class JsNumber: JsAny

internal actual fun JsNumber.toDouble(): Double = 0.0

/** @suppress */
public actual class JsArray<T: JsAny?>: JsAny {
    internal actual val length: Int = 0
}

internal actual operator fun <T: JsAny?> JsArray<T>.get(index: Int): T? = null
internal actual operator fun <T : JsAny?> JsArray<T>.set(index: Int, value: T) {}
internal actual fun <T: JsAny?> JsArray<T>.push(value: T) {}

internal actual operator fun JsArray<out JsString>.contains(value: JsString): Boolean {
    (0..this.length).forEach {
        if (this[it] == value) return true
    }

    return false
}

internal actual fun <T : JsAny> jsArrayOf(vararg values: T): JsArray<T> = JsArray()

internal actual fun JsAny.isNode             (): Boolean = this is Node
internal actual fun JsAny.isHTMLElement      (): Boolean = this is HTMLElement
internal actual fun JsAny.isHTMLInputElement (): Boolean = this is HTMLInputElement
internal actual fun JsAny.isHTMLButtonElement(): Boolean = this is HTMLButtonElement
internal actual fun JsAny.isHTMLAnchorElement(): Boolean = this is HTMLAnchorElement

internal actual fun JsAny.asNodeOrNull             (): Node?              = this as? Node
internal actual fun JsAny.asHTMLElementOrNull      (): HTMLElement?       = this as? HTMLElement
internal actual fun JsAny.asHTMLInputElementOrNull (): HTMLInputElement?  = this as? HTMLInputElement
internal actual fun JsAny.asHTMLButtonElementOrNull(): HTMLButtonElement? = this as? HTMLButtonElement
internal actual fun JsAny.asHTMLAnchorElementOrNull(): HTMLAnchorElement? = this as? HTMLAnchorElement