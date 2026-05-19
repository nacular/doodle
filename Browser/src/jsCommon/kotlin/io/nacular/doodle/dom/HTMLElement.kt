@file:Suppress("EXTERNAL_TYPE_EXTENDS_NON_EXTERNAL_TYPE")

package io.nacular.doodle.dom

import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Vertical

private val SIZE_OBSERVERS = mutableMapOf<HTMLElement, ResizeObserver>()

internal actual fun HTMLElement.startMonitoringSize() {
    SIZE_OBSERVERS.getOrPut(this) {
        ResizeObserver { entries, _ ->
            repeat(entries.length) { i ->
                entries[i]?.let { entry ->
                    entry.target.onresize?.let { it(Event("onresize")) }
                }
            }
        }.also { it.observe(this) }
    }
}

internal actual fun HTMLElement.stopMonitoringSize() {
    SIZE_OBSERVERS.remove(this)?.disconnect()
}

internal actual var HTMLElement.role: String? get() = getAttribute("role")
    set(new) {
        when (new) {
            null -> removeAttribute("role"     )
            else -> setAttribute   ("role", new)
        }
    }

internal actual var HTMLInputElement.orient: String? get() = getAttribute("orient")
    set(new) {
        when (new) {
            null -> removeAttribute("orient"     )
            else -> setAttribute   ("orient", new)
        }
    }

internal actual fun HTMLInputElement.setOrientation(orientation: Orientation) = when (orientation) {
    Vertical -> {
        orient                   = "vertical"
        style.writingMode        = "bt-lr"
        style._webkit_appearance = "slider-vertical" // FIXME: Chrome is deprecating this. Need better approach
    }
    else -> {
        orient                   = null
        style.writingMode        = ""
        style._webkit_appearance = ""
    }
}

internal actual external class TextMetrics: JsAny {
    actual val width                 : Double
    actual val fontBoundingBoxDescent: Double
}

internal actual abstract external class CanvasRenderingContext2D: JsAny {
    actual abstract var font: String

    actual fun measureText(string: String): TextMetrics
}

internal actual var CanvasRenderingContext2D.wordSpacing  : String? by OptionalDynamicProperty("wordSpacing"  ) { null }
internal actual var CanvasRenderingContext2D.letterSpacing: String? by OptionalDynamicProperty("letterSpacing") { null }

internal actual fun CanvasRenderingContext2D.measureText_(string: String): Size = this.measureText(string).run {
    Size(width, 0.0)
}

internal actual external interface RenderingContext

internal actual abstract external class HTMLCanvasElement: HTMLElement {
    actual fun getContext(contextId: String, vararg arguments: JsAny?): RenderingContext?
}

internal actual external class DOMRect: JsAny {
    actual var x     : Double
    actual var y     : Double
    actual var width : Double
    actual var height: Double
}

internal actual external interface ElementCSSInlineStyle: JsAny {
    actual val style: CSSStyleDeclaration
}

public actual abstract external class Element: Node, ParentNode {
    internal actual open var id        : String
    internal actual open var className : String
    internal actual open var scrollTop : Double
    internal actual open var scrollLeft: Double

    internal actual open val clientWidth : Int
    internal actual open val clientHeight: Int

    internal actual open var outerHTML: String

    internal actual fun getBoundingClientRect(): DOMRect

    internal actual fun getAttribute   (                    qualifiedName: String): String?

    internal actual fun setAttribute   (                    qualifiedName: String, value: String)
    internal actual fun setAttributeNS (namespace: String?, qualifiedName: String, value: String)
    internal actual fun removeAttribute(                    qualifiedName: String               )

    internal actual fun scrollTo(x: Double, y: Double)

    internal actual abstract fun remove()
}

internal actual abstract external class HTMLCollection actual constructor(): JsAny {
    actual abstract val length: Int

    actual open fun item(index: Int): Element?
}

internal actual inline operator fun HTMLCollection.get(index: Int): Element? = this.item(index)

internal actual external interface ParentNode: JsAny {
    actual val children: HTMLCollection
}

public actual abstract external class HTMLElement: Element, ElementCSSInlineStyle {
    internal actual var title       : String
    internal actual var draggable   : Boolean
    internal actual val offsetTop   : Int
    internal actual val offsetLeft  : Int
    internal actual val offsetWidth : Int
    internal actual val offsetHeight: Int
    internal actual var tabIndex    : Int
    internal actual var spellcheck  : Boolean

    internal actual var onkeyup     : ((KeyboardEvent) -> Boolean)?
    internal actual var onkeydown   : ((KeyboardEvent) -> Boolean)?
    internal actual var onkeypress  : ((KeyboardEvent) -> Boolean)?

    internal actual var onwheel        : ((WheelEvent  ) -> Boolean)?
    internal actual var ondblclick     : ((MouseEvent  ) -> Boolean)?
    internal actual var onpointerup    : ((PointerEvent) -> Boolean)?
    internal actual var onpointerout   : ((PointerEvent) -> Unit   )?
    internal actual var onpointerdown  : ((PointerEvent) -> Boolean)?
    internal actual var onpointermove  : ((PointerEvent) -> Boolean)?
    internal actual var onpointerover  : ((PointerEvent) -> Unit   )?
    internal actual var onpointercancel: ((PointerEvent) -> Unit   )?
    internal actual var oncontextmenu  : ((MouseEvent  ) -> Boolean)?

    internal actual var onresize: ((Event) -> Unit)?

    internal actual var onload : ((Event) -> Unit)?
    internal actual var onerror: ((/*JsAny, String, Int, Int, JsAny?*/) -> Unit)?

    internal actual var onblur  : ((FocusEvent) -> Unit   )?
    internal actual var onclick : ((MouseEvent) -> Boolean)?
    internal actual var onfocus : ((FocusEvent) -> Unit   )?
    internal actual var oninput : ((InputEvent) -> Boolean)?
    internal actual var onscroll: ((Event     ) -> Boolean)?
    internal actual var onchange: ((Event     ) -> Boolean)?
    internal actual var onselect: ((Event     ) -> Boolean)?

    internal actual var ondrop     : ((DragEvent) -> Unit)?
    internal actual var ondragend  : ((DragEvent) -> Unit)?
    internal actual var ondragover : ((DragEvent) -> Unit)?
    internal actual var ondragenter: ((DragEvent) -> Unit)?
    internal actual var ondragstart: ((DragEvent) -> Unit)?

    internal actual var dir: String

    internal actual fun focus()
    internal actual fun focus(options: FocusOptions)
    internal actual fun blur ()
    internal actual fun click()

    internal actual fun addEventListener   (to: String, listener: (Event) -> Unit)
    internal actual fun addEventListener   (to: String, listener: (Event) -> Unit, options: AddEventListenerOptions)
    internal actual fun removeEventListener(to: String, listener: (Event) -> Unit)
    internal actual fun removeEventListener(to: String, listener: (Event) -> Unit, options: AddEventListenerOptions)
}

internal actual external interface AddEventListenerOptions: JsAny {
    actual var passive: Boolean?
    actual var once   : Boolean?
}

internal actual external interface ElementCreationOptions: JsAny

internal actual abstract external class StyleSheet: JsAny

internal actual inline operator fun StyleSheetList.get(index: Int): StyleSheet? = item(index)

internal actual abstract external class StyleSheetList: JsAny {
    actual abstract val length: Int

    actual fun item(index: Int): StyleSheet?
}

internal actual abstract external class HTMLStyleElement: HTMLElement {
    actual val sheet: StyleSheet?
}

internal actual abstract external class HTMLMetaElement: HTMLElement {
    actual var name   : String
    actual var content: String
}

internal actual external class Document: JsAny {
    actual val head: HTMLHeadElement?
    actual var body: HTMLElement?
//    internal actual val styleSheets: StyleSheetList

    actual fun addEventListener   (to: String, listener: (Event) -> Unit)
    actual fun removeEventListener(to: String, listener: (Event) -> Unit)
    actual fun getSelection       (): Selection?

    actual fun createElement  (localName: String): Element
    actual fun createElement  (localName: String, options: ElementCreationOptions): Element
    actual fun createElementNS(namespace: String?, qualifiedName: String, options: ElementCreationOptions): Element
    actual fun createElementNS(namespace: String?, qualifiedName: String): Element

    actual fun createTextNode(data: String): Text
    actual fun createRange(): Range
}

internal actual external class Range: JsAny {
    actual val collapsed: Boolean
    actual fun setStart(node: Node, offset: Int)
    actual fun setEnd  (node: Node, offset: Int)
}

internal actual external class Selection: JsAny {
    actual fun removeAllRanges()
    actual fun addRange(range: Range)
}

internal actual abstract external class CharacterData: Node
internal actual external          class Text: CharacterData
internal actual abstract external class HTMLImageElement : HTMLElement {
    actual var src     : String
    actual var alt     : String
    actual val complete: Boolean
    actual val width   : Int
    actual val height  : Int
}

internal actual abstract external class HTMLHeadElement : HTMLElement

internal actual external class FocusOptions: JsAny {
    actual var preventScroll: Boolean
}

internal actual abstract external class HTMLInputElement: HTMLElement {
    actual var type         : String
    actual var step         : String
    actual var value        : String
    actual var accept       : String
    actual var checked      : Boolean
    actual var pattern      : String
    actual var disabled     : Boolean
    actual var multiple     : Boolean
    actual var placeholder  : String
    actual var indeterminate: Boolean

    actual var selectionStart: Int?
    actual var selectionEnd  : Int?

    actual val files: FileList?

    actual fun setSelectionRange(start: Int, end: Int)
}

internal actual abstract external class HTMLButtonElement: HTMLElement {
    actual var disabled: Boolean
}
internal actual abstract external class HTMLAnchorElement: HTMLElement {
    actual abstract var href  : String
    actual abstract var host  : String
    actual          var target: String
}

internal actual abstract external class HTMLIFrameElement: HTMLElement {
    actual open var src: String
}

internal actual open external class ResizeObserver actual constructor(callback: (JsArray<ResizeObserverEntry>, ResizeObserver) -> Unit): JsAny {
    actual fun observe  (target: Node, options: ResizeObserverInit)
    actual fun observe  (target: Node                             )
    actual fun unobserve(target: Node)
    actual fun disconnect()
}

internal actual external interface ResizeObserverInit: JsAny {
    actual var box: String?
}

internal actual abstract external class ResizeObserverEntry: JsAny {
    actual open val contentRect: DOMRect
    actual open val target: HTMLElement
}

internal actual external val document: Document

internal actual abstract external class HTMLDataListElement : HTMLElement {
    actual open val options: HTMLCollection
}

internal actual abstract external class HTMLOptionElement: HTMLElement {
    actual open var value: String
}