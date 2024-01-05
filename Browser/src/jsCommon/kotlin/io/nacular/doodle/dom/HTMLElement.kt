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
    internal actual abstract var font: String

    actual fun measureText(string: String): TextMetrics
}

internal actual var CanvasRenderingContext2D.wordSpacing  : String? get() = null; set(value) {}
internal actual var CanvasRenderingContext2D.letterSpacing: String? get() = null; set(value) {}

//internal actual var CanvasRenderingContext2D.wordSpacing  : String? by DynamicProperty("wordSpacing"  ) { null }
//internal actual var CanvasRenderingContext2D.letterSpacing: String? by DynamicProperty("letterSpacing") { null }
internal actual fun CanvasRenderingContext2D.measureText_(string: String): Size = this.measureText(string).run {
    Size(width, 0.0)
}

internal actual external interface RenderingContext

internal actual abstract external class HTMLCanvasElement: HTMLElement {
    internal actual fun getContext(contextId: String, vararg arguments: JsAny?): RenderingContext?
}

internal actual external class DOMRect: JsAny {
    internal actual var x     : Double
    internal actual var y     : Double
    internal actual var width : Double
    internal actual var height: Double
}

internal actual external interface ElementCSSInlineStyle: JsAny {
    actual val style: CSSStyleDeclaration
}

internal actual abstract external class Element: Node, ParentNode {
    internal actual open var id        : String
    internal actual open var className : String
    internal actual open var scrollTop : Double
    internal actual open var scrollLeft: Double

    internal actual open val clientWidth : Int
    internal actual open val clientHeight: Int

    internal actual open var outerHTML: String

    actual fun getBoundingClientRect(): DOMRect

    actual fun getAttribute   (                    qualifiedName: String): String?

    actual fun setAttribute   (                    qualifiedName: String, value: String)
    actual fun setAttributeNS (namespace: String?, qualifiedName: String, value: String)
    actual fun removeAttribute(                    qualifiedName: String               )

    actual fun scrollTo(x: Double, y: Double)

    internal actual abstract fun remove()
}

internal actual abstract external class HTMLCollection actual constructor(): JsAny {
    internal actual abstract val length: Int

    internal actual open fun item(index: Int): Element?
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
    internal actual abstract val length: Int

    actual fun item(index: Int): StyleSheet?
}

internal actual abstract external class HTMLStyleElement: HTMLElement {
    internal actual val sheet: StyleSheet?
}

internal actual abstract external class HTMLMetaElement: HTMLElement {
    actual var name   : String
    actual var content: String
}

internal actual external class Document: JsAny {
    internal actual val head: HTMLHeadElement?
    actual var body: HTMLElement?
//    internal actual val styleSheets: StyleSheetList

    internal actual fun addEventListener   (to: String, listener: (Event) -> Unit)
    internal actual fun removeEventListener(to: String, listener: (Event) -> Unit)
    internal actual fun getSelection       (): Selection?
//    internal actual fun getCaretFromPoint  (point: Point): CaretPosition?
//    internal actual fun elementFromPoint   (point: Point): Element?

    internal actual fun createElement  (localName: String): Element
    internal actual fun createElement  (localName: String, options: ElementCreationOptions): Element
    internal actual fun createElementNS(namespace: String?, qualifiedName: String, options: ElementCreationOptions): Element
    internal actual fun createElementNS(namespace: String?, qualifiedName: String): Element

    internal actual fun createTextNode(data: String): Text
    internal actual fun createRange(): Range
}

internal actual external class Range: JsAny {
    internal actual val collapsed: Boolean
    internal actual fun setStart(node: Node, offset: Int)
    internal actual fun setEnd  (node: Node, offset: Int)
}

internal actual external class Selection: JsAny {
    actual fun removeAllRanges()
    actual fun addRange(range: Range)
}

internal actual abstract external class CharacterData: Node
internal actual external          class Text: CharacterData
internal actual abstract external class HTMLImageElement : HTMLElement {
    internal actual var src     : String
    internal actual val complete: Boolean
    internal actual val width   : Int
    internal actual val height  : Int
}

internal actual abstract external class HTMLHeadElement : HTMLElement

internal actual external class FocusOptions: JsAny {
    actual var preventScroll: Boolean
}

internal actual abstract external class HTMLInputElement: HTMLElement {
    internal actual var type         : String
    internal actual var step         : String
    internal actual var value        : String
    internal actual var accept       : String
    internal actual var checked      : Boolean
    internal actual var pattern      : String
    internal actual var disabled     : Boolean
    internal actual var multiple     : Boolean
    internal actual var placeholder  : String
    internal actual var indeterminate: Boolean

    internal actual var selectionStart: Int?
    internal actual var selectionEnd  : Int?

    internal actual val files: FileList?

    internal actual fun setSelectionRange(start: Int, end: Int)
}

internal actual abstract external class HTMLButtonElement: HTMLElement {
    internal actual var disabled: Boolean
}
internal actual abstract external class HTMLAnchorElement: HTMLElement {
    internal actual abstract var href  : String
    internal actual abstract var host  : String
    actual          var target: String
}

internal actual abstract external class HTMLIFrameElement: HTMLElement {
    internal actual open var src: String
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
    internal actual open val contentRect: DOMRect
    internal actual open val target: HTMLElement
}

internal actual external val document: Document