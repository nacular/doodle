@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.dom

import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.Orientation

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
internal expect abstract external class CSSRule: JsAny {
    internal var cssText: String
}

internal expect abstract external class CSSRuleList: JsAny {
    internal abstract val length: Int
    internal fun item(index: Int): CSSRule?
}

internal expect abstract external class CSSStyleSheet: StyleSheet {
    internal val cssRules: CSSRuleList

    // FIXME: Reinstate once exception handling works for WASM
//    internal fun insertRule(rule: String, index: Int): Int
    internal fun deleteRule(index: Int)
}

internal expect fun CSSStyleSheet.tryInsertRule(rule: String, index: Int): Int

internal expect val CSSStyleSheet.numStyles: Int

internal expect abstract external class CSSStyleDeclaration: JsAny {
    internal var top                : String
    internal var font               : String
    internal var left               : String
    internal var right              : String
    internal var width              : String
    internal var color              : String
    internal var cursor             : String
    internal var height             : String
    internal var margin             : String
    internal var bottom             : String
    internal var filter             : String
    internal var border             : String
    internal var padding            : String
    internal var zIndex             : String
    internal var display            : String
    internal var opacity            : String
    internal var outline            : String
    internal var fontSize           : String
    internal var position           : String
    internal var transform          : String
    internal var marginTop          : String
    internal var overflowX          : String
    internal var overflowY          : String
    internal var boxShadow          : String
    internal var fontStyle          : String
    internal var textAlign          : String
    internal var textShadow         : String
    internal var textIndent         : String
    internal var fontFamily         : String
    internal var fontWeight         : String
    internal var background         : String
    internal var marginLeft         : String
    internal var whiteSpace         : String
    internal var lineHeight         : String
    internal var marginRight        : String
    internal var borderStyle        : String
    internal var borderColor        : String
    internal var borderWidth        : String
    internal var wordSpacing        : String
    internal var fontVariant        : String
    internal var borderRadius       : String
    internal var marginBottom       : String
    internal var outlineWidth       : String
    internal var letterSpacing      : String
    internal var backgroundSize     : String
    internal var textDecoration     : String
    internal var backgroundImage    : String
    internal var backgroundColor    : String
    internal var textDecorationLine : String
    internal var textDecorationColor: String
    internal var textDecorationStyle: String

    internal var writingMode        : String

    internal fun removeProperty(property: String): String
    internal fun setProperty   (property: String, value: String, priority: String)
    internal fun setProperty   (property: String, value: String                  )
}

internal expect var CSSStyleDeclaration.clipPath               : String
internal expect var CSSStyleDeclaration.willChange             : String
internal expect var CSSStyleDeclaration.scrollBehavior         : String
internal expect var CSSStyleDeclaration.textDecorationThickness: String

internal expect var CSSStyleDeclaration.touchAction            : String
internal expect var CSSStyleDeclaration._webkit_appearance     : String
internal expect var CSSStyleDeclaration.caretColor             : String
internal expect var CSSStyleDeclaration.userSelect             : String

internal expect var CSSStyleDeclaration._ms_user_select            : String
internal expect var CSSStyleDeclaration._moz_user_select           : String
internal expect var CSSStyleDeclaration._khtml_user_select         : String
internal expect var CSSStyleDeclaration._webkit_user_select        : String
internal expect var CSSStyleDeclaration._webkit_touch_callout      : String
internal expect var CSSStyleDeclaration._webkit_tap_highlight_color: String

internal expect external class TextMetrics: JsAny {
    internal val width: Double
}

internal expect abstract external class CanvasRenderingContext2D: JsAny {
    internal abstract var font: String
    internal fun measureText(string: String): TextMetrics
}

internal expect var CanvasRenderingContext2D.wordSpacing  : String?
internal expect var CanvasRenderingContext2D.letterSpacing: String?
internal expect fun CanvasRenderingContext2D.measureText_(string: String): Size

internal expect external interface RenderingContext

internal expect abstract external class HTMLCanvasElement: HTMLElement {
    internal fun getContext(contextId: String, vararg arguments: JsAny?): RenderingContext?
}

internal expect external class DOMRect: JsAny {
    internal var x     : Double
    internal var y     : Double
    internal var width : Double
    internal var height: Double
}

internal expect external interface ElementCSSInlineStyle: JsAny {
    val style: CSSStyleDeclaration
}

internal expect abstract external class Element: Node, ParentNode {
    internal open var id        : String
    internal open var className : String
    internal open var scrollTop : Double
    internal open var scrollLeft: Double

    internal open val clientWidth : Int
    internal open val clientHeight: Int

    internal open var outerHTML: String

    internal fun getBoundingClientRect(): DOMRect

    internal fun getAttribute   (                    qualifiedName: String): String?

    internal fun setAttribute   (                    qualifiedName: String, value: String)
    internal fun setAttributeNS (namespace: String?, qualifiedName: String, value: String)
    internal fun removeAttribute(                    qualifiedName: String               )

    internal fun scrollTo(x: Double, y: Double)

    internal abstract fun remove()
}

internal expect abstract external class HTMLCollection(): JsAny {
    internal abstract val length: Int

    internal open fun item(index: Int): Element?
}

internal expect inline operator fun HTMLCollection.get(index: Int): Element?

internal expect external interface ParentNode: JsAny {
    val children: HTMLCollection
}

internal expect external class FocusOptions: JsAny {
    internal var preventScroll: Boolean
}

internal expect abstract external class HTMLElement: Element, ElementCSSInlineStyle {
    internal var title       : String
    internal var draggable   : Boolean
    internal val offsetTop   : Int
    internal val offsetLeft  : Int
    internal val offsetWidth : Int
    internal val offsetHeight: Int
    internal var tabIndex    : Int
    internal var spellcheck  : Boolean

    internal var onkeyup     : ((KeyboardEvent) -> Boolean)?
    internal var onkeydown   : ((KeyboardEvent) -> Boolean)?
    internal var onkeypress  : ((KeyboardEvent) -> Boolean)?

    internal var onwheel        : ((WheelEvent  ) -> Boolean)?
    internal var ondblclick     : ((MouseEvent  ) -> Boolean)?
    internal var onpointerup    : ((PointerEvent) -> Boolean)?
    internal var onpointerout   : ((PointerEvent) -> Boolean)?
    internal var onpointerdown  : ((PointerEvent) -> Boolean)?
    internal var onpointermove  : ((PointerEvent) -> Boolean)?
    internal var onpointerover  : ((PointerEvent) -> Unit   )?
    internal var onpointercancel: ((PointerEvent) -> Unit   )?
    internal var oncontextmenu  : ((MouseEvent  ) -> Boolean)?

    internal var onresize: ((Event) -> Unit)?

    internal var onload : ((Event) -> Unit)?
    internal var onerror: (() -> Unit)?

    internal var onblur  : ((FocusEvent) -> Unit   )?
    internal var onclick : ((MouseEvent) -> Boolean)?
    internal var onfocus : ((FocusEvent) -> Unit   )?
    internal var oninput : ((InputEvent) -> Boolean)?
    internal var onscroll: ((Event     ) -> Boolean)?
    internal var onchange: ((Event     ) -> Boolean)?
    internal var onselect: ((Event     ) -> Boolean)?

    internal var ondrop     : ((DragEvent) -> Unit)?
    internal var ondragend  : ((DragEvent) -> Unit)?
    internal var ondragover : ((DragEvent) -> Unit)?
    internal var ondragenter: ((DragEvent) -> Unit)?
    internal var ondragstart: ((DragEvent) -> Unit)?

    internal fun addEventListener   (to: String, listener: (Event) -> Unit)
    internal fun addEventListener   (to: String, listener: (Event) -> Unit, options: AddEventListenerOptions)
    internal fun removeEventListener(to: String, listener: (Event) -> Unit)
    internal fun removeEventListener(to: String, listener: (Event) -> Unit, options: AddEventListenerOptions)

    internal var dir: String

    internal fun focus()
    internal fun focus(options: FocusOptions)
    internal fun blur ()
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
    internal abstract val length: Int

    internal fun item(index: Int): StyleSheet?
}

internal expect abstract external class HTMLStyleElement: HTMLElement {
    internal val sheet: StyleSheet?
}

internal expect abstract external class HTMLMetaElement: HTMLElement {
    internal var name   : String
    internal var content: String
}

internal expect external class Document: JsAny {
    internal val head: HTMLHeadElement?
    internal var body: HTMLElement?
//    internal val styleSheets: StyleSheetList

    internal fun addEventListener   (to: String, listener: (Event) -> Unit)
    internal fun removeEventListener(to: String, listener: (Event) -> Unit)
    internal fun getSelection       (): Selection?
//    internal fun getCaretFromPoint  (point: Point): CaretPosition?
//    internal fun elementFromPoint   (point: Point): Element?

    internal fun createElement  (localName: String, options: ElementCreationOptions): Element
    internal fun createElement  (localName: String): Element
    internal fun createElementNS(namespace: String?, qualifiedName: String, options: ElementCreationOptions): Element
    internal fun createElementNS(namespace: String?, qualifiedName: String): Element

    internal fun createTextNode(data: String): Text
    internal fun createRange(): Range
}

//internal class CaretPosition(val offsetNode: Node, val offset: Int)

internal expect external class Range: JsAny {
    internal val collapsed: Boolean
    internal fun setStart(node: Node, offset: Int)
    internal fun setEnd  (node: Node, offset: Int)
}

internal expect external class Selection: JsAny {
    fun removeAllRanges()
    fun addRange(range: Range)
}

internal expect abstract external class CharacterData: Node

internal expect external class Text: CharacterData

internal expect abstract external class HTMLImageElement: HTMLElement {
    internal var src     : String
    internal val complete: Boolean
    internal val width   : Int
    internal val height  : Int
}

internal expect abstract external class HTMLHeadElement : HTMLElement

internal expect abstract external class HTMLInputElement: HTMLElement {
    internal var type         : String
    internal var step         : String
    internal var value        : String
    internal var accept       : String
    internal var checked      : Boolean
    internal var pattern      : String
    internal var disabled     : Boolean
    internal var multiple     : Boolean
    internal var placeholder  : String
    internal var indeterminate: Boolean

    internal var selectionStart: Int?
    internal var selectionEnd  : Int?

    internal val files: FileList?

    internal fun setSelectionRange(start: Int, end: Int)
}

internal expect fun HTMLInputElement.focusInput()

internal expect abstract external class HTMLButtonElement: HTMLElement {
    internal var disabled: Boolean
}

internal expect abstract external class HTMLAnchorElement: HTMLElement {
    internal abstract var href  : String
    internal abstract var host  : String
    internal          var target: String
}

internal expect abstract external class HTMLIFrameElement: HTMLElement {
    internal open var src: String
}

internal expect open external class ResizeObserver(callback: (JsArray<ResizeObserverEntry>, ResizeObserver) -> Unit): JsAny {
    internal fun observe   (target: Node, options: ResizeObserverInit)
    internal fun observe   (target: Node                             )
    internal fun unobserve (target: Node)
    internal fun disconnect()
}

internal expect fun ResizeObserver.observeResize(target: Node, box: String)

internal expect external interface ResizeObserverInit: JsAny {
    var box: String?
}

internal expect abstract external class ResizeObserverEntry: JsAny {
    internal open val contentRect: DOMRect
    internal open val target: HTMLElement
}

internal expect val document: Document