@file:Suppress("EXTERNAL_TYPE_EXTENDS_NON_EXTERNAL_TYPE")

package io.nacular.doodle.dom

import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Vertical

private val SIZE_OBSERVERS = mutableMapOf<HTMLElement, ResizeObserver>()

internal actual fun HTMLElement.startMonitoringSize() {
    SIZE_OBSERVERS.getOrPut(this) {
//        try {
            ResizeObserver { entries, _ ->
                (0..entries.length).forEach {
                    entries[it]?.let {
                        it.target.onresize?.let { it(Event("onresize")) }
                    }
                }
            }.also { it.observe(this) }


//            val observer = js(
//         """new ResizeObserver(function(entries) {
//                for (var i = 0; i < entries.length; ++i) {
//                    var entry = entries[i]
//                    if (entry.target.onresize) { entry.target.onresize(new Event("onresize")) }
//                }
//            })""")
//
//            observer.observe(this)

//        } catch (ignored: Throwable) {}
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

internal actual fun HTMLInputElement.setOrientation(orientation: Orientation) {
    when (orientation) {
        Vertical -> {
            orient                   = "vertical"
            style.writingMode        = "bt-lr"
            style._webkit_appearance = "slider-vertical"
        }
        else -> {
            orient                   = null
            style.writingMode        = ""
            style._webkit_appearance = ""
        }
    }
}

internal actual external class TextMetrics: JsAny {
    actual val width: Double
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

internal actual abstract external class Element: Node, ParentNode {
    actual open var id        : String
    actual open var className : String
    actual open var scrollTop : Double
    actual open var scrollLeft: Double

    actual open val clientWidth : Int
    actual open val clientHeight: Int

    actual open var outerHTML: String

    actual fun getBoundingClientRect(): DOMRect

    actual fun getAttribute   (                    qualifiedName: String): String?

    actual fun setAttribute   (                    qualifiedName: String, value: String)
    actual fun setAttributeNS (namespace: String?, qualifiedName: String, value: String)
    actual fun removeAttribute(                    qualifiedName: String               )

    actual fun scrollTo(x: Double, y: Double)

    actual abstract fun remove()
}

internal actual abstract external class HTMLCollection actual constructor(): JsAny {
    actual abstract val length: Int

    actual open fun item(index: Int): Element?
}

internal actual inline operator fun HTMLCollection.get(index: Int): Element? = this.item(index)

internal actual external interface ParentNode: JsAny {
    actual val children: HTMLCollection
}

internal actual abstract external class HTMLElement: Element, ElementCSSInlineStyle {
    actual var title       : String
    actual var draggable   : Boolean
    actual val offsetTop   : Int
    actual val offsetLeft  : Int
    actual val offsetWidth : Int
    actual val offsetHeight: Int
    actual var tabIndex    : Int
    actual var spellcheck  : Boolean

    actual var onkeyup     : ((KeyboardEvent) -> Boolean)?
    actual var onkeydown   : ((KeyboardEvent) -> Boolean)?
    actual var onkeypress  : ((KeyboardEvent) -> Boolean)?

    actual var onwheel        : ((WheelEvent  ) -> Boolean)?
    actual var ondblclick     : ((MouseEvent  ) -> Boolean)?
    actual var onpointerup    : ((PointerEvent) -> Boolean)?
    actual var onpointerout   : ((PointerEvent) -> Boolean)?
    actual var onpointerdown  : ((PointerEvent) -> Boolean)?
    actual var onpointermove  : ((PointerEvent) -> Boolean)?
    actual var onpointerover  : ((PointerEvent) -> Unit   )?
    actual var onpointercancel: ((PointerEvent) -> Unit   )?
    actual var oncontextmenu  : ((MouseEvent  ) -> Boolean)?

    actual var onresize: ((Event) -> Unit)?

    actual var onload : ((Event) -> Unit)?
    actual var onerror: ((/*JsAny, String, Int, Int, JsAny?*/) -> Unit)?

    actual var onblur  : ((FocusEvent) -> Unit   )?
    actual var onclick : ((MouseEvent) -> Boolean)?
    actual var onfocus : ((FocusEvent) -> Unit   )?
    actual var oninput : ((InputEvent) -> Boolean)?
    actual var onscroll: ((Event     ) -> Boolean)?
    actual var onchange: ((Event     ) -> Boolean)?
    actual var onselect: ((Event     ) -> Boolean)?

    actual var ondrop     : ((DragEvent) -> Unit)?
    actual var ondragend  : ((DragEvent) -> Unit)?
    actual var ondragover : ((DragEvent) -> Unit)?
    actual var ondragenter: ((DragEvent) -> Unit)?
    actual var ondragstart: ((DragEvent) -> Unit)?

    actual var dir: String

    actual fun focus()
    actual fun focus(options: FocusOptions)
    actual fun blur ()

    actual fun addEventListener   (to: String, listener: (Event) -> Unit)
    actual fun addEventListener   (to: String, listener: (Event) -> Unit, options: AddEventListenerOptions)
    actual fun removeEventListener(to: String, listener: (Event) -> Unit)
    actual fun removeEventListener(to: String, listener: (Event) -> Unit, options: AddEventListenerOptions)
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
//    internal actual fun getCaretFromPoint  (point: Point): CaretPosition?
//    internal actual fun elementFromPoint   (point: Point): Element?

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