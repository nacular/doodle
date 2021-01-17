package io.nacular.doodle

import io.nacular.doodle.dom.DataTransfer
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.KeyboardEvent
import io.nacular.doodle.dom.MouseEvent
import io.nacular.doodle.dom.PointerEvent
import io.nacular.doodle.dom.TouchEvent
import io.nacular.doodle.dom.WheelEvent

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
expect abstract class CSSRule {
    var cssText: String
}

expect abstract class CSSRuleList {
    abstract val length: Int
    fun item(index: Int): CSSRule?
}

expect abstract class CSSStyleSheet: StyleSheet {
    val cssRules: CSSRuleList

    fun insertRule(rule: String, index: Int): Int
    fun deleteRule(index: Int)
}

expect val CSSStyleSheet.numStyles: Int

expect abstract class CSSStyleDeclaration {
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
    var fontSize           : String
    var position           : String
    var transform          : String
    var marginTop          : String
    var overflowX          : String
    var overflowY          : String
    var boxShadow          : String
    var fontStyle          : String
    var textShadow         : String
    var textIndent         : String
    var fontFamily         : String
    var fontWeight         : String
    var background         : String
    var marginLeft         : String
    var whiteSpace         : String
    var marginRight        : String
    var borderStyle        : String
    var borderColor        : String
    var borderWidth        : String
    var borderRadius       : String
    var marginBottom       : String
    var outlineWidth       : String
    var backgroundSize     : String
    var textDecoration     : String
    var backgroundImage    : String
    var backgroundColor    : String
    var textDecorationLine : String
    var textDecorationColor: String
    var textDecorationStyle: String
}

expect var CSSStyleDeclaration.clipPath               : String
expect var CSSStyleDeclaration.willChange             : String
expect var CSSStyleDeclaration.textDecorationThickness: String

expect class DOMRect {
    var x     : Double
    var y     : Double
    var width : Double
    var height: Double
}

expect interface ElementCSSInlineStyle {
    val style: CSSStyleDeclaration
}

expect abstract class Element: Node {
    open var id        : String
    open var className : String
    open var innerHTML : String
    open var scrollTop : Double
    open var scrollLeft: Double

    fun getBoundingClientRect(): DOMRect

    fun setAttribute   (                    qualifiedName: String, value: String)
    fun setAttributeNS (namespace: String?, qualifiedName: String, value: String)
    fun removeAttribute(                    qualifiedName: String               )
}

expect class DragEvent {
    val dataTransfer: DataTransfer?
}

expect abstract class HTMLElement: Element {
    var title       : String
    var draggable   : Boolean
    val offsetTop   : Int
    val offsetLeft  : Int
    val offsetWidth : Int
    val offsetHeight: Int
    abstract val style: CSSStyleDeclaration

    var onkeyup     : ((KeyboardEvent) -> Boolean)?
    var onkeydown   : ((KeyboardEvent) -> Boolean)?
    var onkeypress  : ((KeyboardEvent) -> Boolean)?

    var onwheel     : ((WheelEvent) -> Any)?
    var onmouseup   : ((MouseEvent) -> Any)?
    var onmouseout  : ((MouseEvent) -> Any)?
    var ondblclick  : ((MouseEvent) -> Any)?
    var onmousedown : ((MouseEvent) -> Any)?
    var onmousemove : ((MouseEvent) -> Any)?
    var onmouseover : ((MouseEvent) -> Any)?

    var onpointerup  : ((PointerEvent) -> Any)?
    var onpointerdown: ((PointerEvent) -> Any)?
    var onpointermove: ((PointerEvent) -> Any)?
    var onpointerover: ((PointerEvent) -> Any)?

    var onresize   : ((Event) -> Unit)?
    var ondragstart: ((DragEvent) -> Boolean)?

    var dir: String

    fun focus()
    fun blur ()
}

internal expect fun HTMLElement.stopMonitoringSize ()
internal expect fun HTMLElement.startMonitoringSize()

internal expect var HTMLElement.ontouchmove: ((TouchEvent  ) -> Any)?

expect var HTMLElement.role: String?

expect interface ElementCreationOptions

expect abstract class StyleSheet

expect inline operator fun StyleSheetList.get(index: Int): StyleSheet?

expect abstract class StyleSheetList {
    abstract val length: Int

    fun item(index: Int): StyleSheet?
}

expect abstract class HTMLStyleElement: HTMLElement {
    val sheet: StyleSheet?
}

expect abstract class HTMLMetaElement: HTMLElement {
    var name   : String
    var content: String
}

expect class Document {
    val head: HTMLHeadElement?
    var body: HTMLElement?
    val styleSheets: StyleSheetList

    fun createElement(localName: String, options: ElementCreationOptions = object: ElementCreationOptions {}): Element
    fun createTextNode(data: String): Text
    fun createElementNS(namespace: String?, qualifiedName: String, options: ElementCreationOptions = object: ElementCreationOptions {}): Element
}

expect fun Document.addEventListener(to: String, listener: (Event) -> Unit)
expect fun Document.removeEventListener(to: String, listener: (Event) -> Unit)

expect abstract class CharacterData: Node
expect          class Text: CharacterData
expect abstract class HTMLImageElement : HTMLElement {
    var src     : String
    val complete: Boolean
}

expect abstract class HTMLHeadElement  : HTMLElement
expect abstract class HTMLInputElement : HTMLElement
expect abstract class HTMLButtonElement: HTMLElement {
    var disabled: Boolean
}