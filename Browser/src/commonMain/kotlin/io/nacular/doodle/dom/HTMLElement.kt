@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.dom

import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.Orientation

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
internal expect abstract external class CSSRule: JsAny {
    var cssText: String
}

internal expect abstract external class CSSRuleList: JsAny {
    abstract val length: Int
    fun item(index: Int): CSSRule?
}

internal expect abstract external class CSSStyleSheet: StyleSheet {
    val cssRules: CSSRuleList

    // FIXME: Reinstate once exception handling works for WASM
//    fun insertRule(rule: String, index: Int): Int
    fun deleteRule(index: Int)
}

internal expect fun CSSStyleSheet.tryInsertRule(rule: String, index: Int): Int

internal expect val CSSStyleSheet.numStyles: Int

internal expect abstract external class CSSStyleDeclaration: JsAny {
    var top                : String
    var font               : String
    var left               : String
    var right              : String
    var width              : String
    var color              : String
    var cursor             : String
    var height             : String
    var margin             : String
    var bottom             : String
    var filter             : String
    var border             : String
    var padding            : String
    var zIndex             : String
    var display            : String
    var opacity            : String
    var outline            : String
    var fontSize           : String
    var position           : String
    var transform          : String
    var marginTop          : String
    var overflowX          : String
    var overflowY          : String
    var boxShadow          : String
    var fontStyle          : String
    var textAlign          : String
    var textShadow         : String
    var textIndent         : String
    var fontFamily         : String
    var fontWeight         : String
    var background         : String
    var marginLeft         : String
    var whiteSpace         : String
    var lineHeight         : String
    var marginRight        : String
    var borderStyle        : String
    var borderColor        : String
    var borderWidth        : String
    var wordSpacing        : String
    var fontVariant        : String
    var borderRadius       : String
    var marginBottom       : String
    var outlineWidth       : String
    var letterSpacing      : String
    var backgroundSize     : String
    var textDecoration     : String
    var backgroundImage    : String
    var backgroundColor    : String
    var textDecorationLine : String
    var textDecorationColor: String
    var textDecorationStyle: String

    var writingMode        : String

    fun removeProperty(property: String): String
    fun setProperty   (property: String, value: String, priority: String)
    fun setProperty   (property: String, value: String                  )
}

internal expect var CSSStyleDeclaration.clipPath               : String
internal expect var CSSStyleDeclaration.willChange             : String
internal expect var CSSStyleDeclaration.scrollBehavior         : String
internal expect var CSSStyleDeclaration.textDecorationThickness: String
internal expect var CSSStyleDeclaration.caretColor             : String

internal expect var CSSStyleDeclaration._webkit_appearance     : String
internal expect var CSSStyleDeclaration._webkit_text_stroke    : String
internal expect var CSSStyleDeclaration.backDropFilter         : String
internal expect var CSSStyleDeclaration.maskImage              : String

internal expect var CSSStyleDeclaration.userSelect                 : String
internal expect var CSSStyleDeclaration._ms_user_select            : String
internal expect var CSSStyleDeclaration._moz_user_select           : String
internal expect var CSSStyleDeclaration._khtml_user_select         : String
internal expect var CSSStyleDeclaration._webkit_user_select        : String
internal expect var CSSStyleDeclaration._webkit_touch_callout      : String
internal expect var CSSStyleDeclaration._webkit_tap_highlight_color: String

internal expect external class TextMetrics: JsAny {
    val width                 : Double
    val fontBoundingBoxDescent: Double
}

internal expect abstract external class CanvasRenderingContext2D: JsAny {
    abstract var font: String
    fun measureText(string: String): TextMetrics
}

internal expect var CanvasRenderingContext2D.wordSpacing  : String?
internal expect var CanvasRenderingContext2D.letterSpacing: String?
internal expect fun CanvasRenderingContext2D.measureText_(string: String): Size

internal expect external interface RenderingContext

internal expect abstract external class HTMLCanvasElement: HTMLElement {
    fun getContext(contextId: String, vararg arguments: JsAny?): RenderingContext?
}

internal expect external class DOMRect: JsAny {
    var x     : Double
    var y     : Double
    var width : Double
    var height: Double
}

internal expect external interface ElementCSSInlineStyle: JsAny {
    val style: CSSStyleDeclaration
}

internal expect abstract external class Element: Node, ParentNode {
    open var id        : String
    open var className : String
    open var outerHTML : String
    open var scrollTop : Double
    open var scrollLeft: Double

    open val clientWidth : Int
    open val clientHeight: Int

    fun getBoundingClientRect(): DOMRect

    fun getAttribute   (                    qualifiedName: String               ): String?
    fun setAttribute   (                    qualifiedName: String, value: String)
    fun setAttributeNS (namespace: String?, qualifiedName: String, value: String)
    fun removeAttribute(                    qualifiedName: String               )
    fun scrollTo       (x: Double, y: Double)

    abstract fun remove()
}

internal expect abstract external class HTMLCollection(): JsAny {
    abstract val length: Int

    open fun item(index: Int): Element?
}

internal expect inline operator fun HTMLCollection.get(index: Int): Element?

internal expect external interface ParentNode: JsAny {
    val children: HTMLCollection
}

internal expect external class FocusOptions: JsAny {
    var preventScroll: Boolean
}

internal expect abstract external class HTMLElement: Element, ElementCSSInlineStyle {
    var title       : String
    var draggable   : Boolean
    val offsetTop   : Int
    val offsetLeft  : Int
    val offsetWidth : Int
    val offsetHeight: Int
    var tabIndex    : Int
    var spellcheck  : Boolean
    var dir         : String

    var onkeyup        : ((KeyboardEvent) -> Boolean)?
    var onkeydown      : ((KeyboardEvent) -> Boolean)?
    var onkeypress     : ((KeyboardEvent) -> Boolean)?

    var onwheel        : ((WheelEvent  ) -> Boolean)?
    var ondblclick     : ((MouseEvent  ) -> Boolean)?
    var onpointerup    : ((PointerEvent) -> Boolean)?
    var onpointerout   : ((PointerEvent) -> Unit   )?
    var onpointerdown  : ((PointerEvent) -> Boolean)?
    var onpointermove  : ((PointerEvent) -> Boolean)?
    var onpointerover  : ((PointerEvent) -> Unit   )?
    var onpointercancel: ((PointerEvent) -> Unit   )?
    var oncontextmenu  : ((MouseEvent  ) -> Boolean)?

    var onresize: ((Event) -> Unit)?

    var onload : ((Event) -> Unit)?
    var onerror: (() -> Unit)?

    var onblur  : ((FocusEvent) -> Unit   )?
    var onclick : ((MouseEvent) -> Boolean)?
    var onfocus : ((FocusEvent) -> Unit   )?
    var oninput : ((InputEvent) -> Boolean)?
    var onscroll: ((Event     ) -> Boolean)?
    var onchange: ((Event     ) -> Boolean)?
    var onselect: ((Event     ) -> Boolean)?

    var ondrop     : ((DragEvent) -> Unit)?
    var ondragend  : ((DragEvent) -> Unit)?
    var ondragover : ((DragEvent) -> Unit)?
    var ondragenter: ((DragEvent) -> Unit)?
    var ondragstart: ((DragEvent) -> Unit)?

    fun addEventListener   (to: String, listener: (Event) -> Unit)
    fun addEventListener   (to: String, listener: (Event) -> Unit, options: AddEventListenerOptions)
    fun removeEventListener(to: String, listener: (Event) -> Unit)
    fun removeEventListener(to: String, listener: (Event) -> Unit, options: AddEventListenerOptions)

    fun focus()
    fun focus(options: FocusOptions)
    fun blur ()
    fun click()
}

internal expect external interface AddEventListenerOptions: JsAny {
    var passive: Boolean?
    var once   : Boolean?
}

internal expect fun HTMLElement.stopMonitoringSize ()
internal expect fun HTMLElement.startMonitoringSize()

internal expect var HTMLElement.role: String?

internal expect fun HTMLElement.addActiveEventListener   (to: String, listener: (Event) -> Unit)
internal expect fun HTMLElement.removeActiveEventListener(to: String, listener: (Event) -> Unit)

internal expect var HTMLInputElement.orient: String?
internal expect fun HTMLInputElement.setOrientation(orientation: Orientation)

internal expect external interface ElementCreationOptions: JsAny

internal expect abstract external class StyleSheet: JsAny

internal expect inline operator fun StyleSheetList.get(index: Int): StyleSheet?

internal expect abstract external class StyleSheetList: JsAny {
    abstract val length: Int

    fun item(index: Int): StyleSheet?
}

internal expect abstract external class HTMLStyleElement: HTMLElement {
    val sheet: StyleSheet?
}

internal expect abstract external class HTMLMetaElement: HTMLElement {
    var name   : String
    var content: String
}

internal expect external class Document: JsAny {
    val head: HTMLHeadElement?
    var body: HTMLElement?

    fun addEventListener   (to: String, listener: (Event) -> Unit)
    fun removeEventListener(to: String, listener: (Event) -> Unit)
    fun getSelection       (): Selection?

    fun createElement  (localName: String, options: ElementCreationOptions): Element
    fun createElement  (localName: String): Element
    fun createElementNS(namespace: String?, qualifiedName: String, options: ElementCreationOptions): Element
    fun createElementNS(namespace: String?, qualifiedName: String): Element

    fun createTextNode(data: String): Text
    fun createRange(): Range
}

internal expect external class Range: JsAny {
    val collapsed: Boolean
    fun setStart(node: Node, offset: Int)
    fun setEnd  (node: Node, offset: Int)
}

internal expect external class Selection: JsAny {
    fun removeAllRanges()
    fun addRange(range: Range)
}

internal expect abstract external class CharacterData: Node

internal expect external class Text: CharacterData

internal expect abstract external class HTMLImageElement: HTMLElement {
    var src     : String
    var alt     : String
    val complete: Boolean
    val width   : Int
    val height  : Int
}

internal expect abstract external class HTMLHeadElement : HTMLElement

internal expect abstract external class HTMLInputElement: HTMLElement {
    var type         : String
    var step         : String
    var value        : String
    var accept       : String
    var checked      : Boolean
    var pattern      : String
    var disabled     : Boolean
    var multiple     : Boolean
    var placeholder  : String
    var indeterminate: Boolean

    var selectionStart: Int?
    var selectionEnd  : Int?

    val files: FileList?

    fun setSelectionRange(start: Int, end: Int)
}

internal expect fun HTMLInputElement.focusInput()

internal expect abstract external class HTMLButtonElement: HTMLElement {
    var disabled: Boolean
}

internal expect abstract external class HTMLAnchorElement: HTMLElement {
    abstract var href  : String
    abstract var host  : String
             var target: String
}

internal expect abstract external class HTMLIFrameElement: HTMLElement {
    open var src: String
}

internal expect open external class ResizeObserver(callback: (JsArray<ResizeObserverEntry>, ResizeObserver) -> Unit): JsAny {
    fun observe   (target: Node, options: ResizeObserverInit)
    fun observe   (target: Node                             )
    fun unobserve (target: Node)
    fun disconnect()
}

internal expect fun ResizeObserver.observeResize(target: Node, box: String)

internal expect external interface ResizeObserverInit: JsAny {
    var box: String?
}

internal expect abstract external class ResizeObserverEntry: JsAny {
    open val contentRect: DOMRect
    open val target: HTMLElement
}

internal expect val document: Document