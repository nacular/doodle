package io.nacular.doodle

import io.nacular.doodle.dom.DataTransfer
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.KeyboardEvent
import io.nacular.doodle.dom.MouseEvent
import io.nacular.doodle.dom.PointerEvent
import io.nacular.doodle.dom.WheelEvent
import io.nacular.doodle.utils.Orientation

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
public expect abstract class CSSRule {
    internal var cssText: String
}

public expect abstract class CSSRuleList {
    public abstract val length: Int
    public fun item(index: Int): CSSRule?
}

public expect abstract class CSSStyleSheet: StyleSheet {
    internal val cssRules: CSSRuleList

    internal fun insertRule(rule: String, index: Int): Int
    internal fun deleteRule(index: Int)
}

internal expect val CSSStyleSheet.numStyles: Int

public expect abstract class CSSStyleDeclaration {
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
}

internal expect var CSSStyleDeclaration.clipPath               : String
internal expect var CSSStyleDeclaration.willChange             : String
internal expect var CSSStyleDeclaration.scrollBehavior         : String
internal expect var CSSStyleDeclaration.textDecorationThickness: String

internal expect var CSSStyleDeclaration.touchAction            : String
internal expect var CSSStyleDeclaration._webkit_appearance     : String

public expect abstract class CanvasRenderingContext2D

internal expect var CanvasRenderingContext2D.letterSpacing: String

public expect class DOMRect {
    internal var x     : Double
    internal var y     : Double
    internal var width : Double
    internal var height: Double
}

public expect interface ElementCSSInlineStyle {
    public val style: CSSStyleDeclaration
}

public expect abstract class Element: Node {
    public open var id        : String
    public open var className : String
    public open var scrollTop : Double
    public open var scrollLeft: Double

    public open val clientWidth : Int
    public open val clientHeight: Int

    public fun getBoundingClientRect(): DOMRect

    public fun getAttribute   (                    qualifiedName: String): String?

    public fun setAttribute   (                    qualifiedName: String, value: String)
    public fun setAttributeNS (namespace: String?, qualifiedName: String, value: String)
    public fun removeAttribute(                    qualifiedName: String               )

    public fun scrollTo(x: Double, y: Double)
}

public expect class DragEvent {
    internal val dataTransfer: DataTransfer?
}

public expect abstract class HTMLElement: Element {
    public var title       : String
    public var draggable   : Boolean
    public val offsetTop   : Int
    public val offsetLeft  : Int
    public val offsetWidth : Int
    public val offsetHeight: Int
    public abstract val style: CSSStyleDeclaration

    public var onkeyup     : ((KeyboardEvent) -> Boolean)?
    public var onkeydown   : ((KeyboardEvent) -> Boolean)?
    public var onkeypress  : ((KeyboardEvent) -> Boolean)?

    public var onwheel        : ((WheelEvent  ) -> Any)?
    public var ondblclick     : ((MouseEvent  ) -> Any)?
    public var onpointerup    : ((PointerEvent) -> Any)?
    public var onpointerout   : ((PointerEvent) -> Any)?
    public var onpointerdown  : ((PointerEvent) -> Any)?
    public var onpointermove  : ((PointerEvent) -> Any)?
    public var onpointerover  : ((PointerEvent) -> Any)?
    public var onpointercancel: ((PointerEvent) -> Any)?

    public var onresize   : ((Event) -> Unit)?
    public var ondragstart: ((DragEvent) -> Boolean)?

    public var onload : ((Event) -> Any)?
    public var onerror: ((Any, String, Int, Int, Any?) -> Any)?

    public var dir: String

    public fun focus()
    public fun blur ()
}

internal expect fun HTMLElement.stopMonitoringSize ()
internal expect fun HTMLElement.startMonitoringSize()

internal expect var HTMLElement.role: String?

internal expect fun HTMLElement.addActiveEventListener   (to: String, listener: (Event) -> Unit)
internal expect fun HTMLElement.removeActiveEventListener(to: String, listener: (Event) -> Unit)

internal expect var HTMLInputElement.orient: String?
internal expect fun HTMLInputElement.setOrientation(orientation: Orientation)

public expect interface ElementCreationOptions

public expect abstract class StyleSheet

internal expect inline operator fun StyleSheetList.get(index: Int): StyleSheet?

public expect abstract class StyleSheetList {
    public abstract val length: Int

    public fun item(index: Int): StyleSheet?
}

public expect abstract class HTMLStyleElement: HTMLElement {
    internal val sheet: StyleSheet?
}

public expect abstract class HTMLMetaElement: HTMLElement {
    public var name   : String
    public var content: String
}

public expect class Document {
    internal val head: HTMLHeadElement?
    internal var body: HTMLElement?
    internal val styleSheets: StyleSheetList

    internal fun createElement(localName: String, options: ElementCreationOptions = object: ElementCreationOptions {}): Element
    internal fun createTextNode(data: String): Text
    internal fun createElementNS(namespace: String?, qualifiedName: String, options: ElementCreationOptions = object: ElementCreationOptions {}): Element
}

internal expect fun Document.addEventListener   (to: String, listener: (Event) -> Unit)
internal expect fun Document.removeEventListener(to: String, listener: (Event) -> Unit)

public expect abstract class CharacterData: Node
public expect          class Text: CharacterData
public expect abstract class HTMLImageElement : HTMLElement {
    internal var src     : String
    internal val complete: Boolean
}

public expect abstract class HTMLHeadElement  : HTMLElement
public expect abstract class HTMLInputElement : HTMLElement
public expect abstract class HTMLButtonElement: HTMLElement {
    internal var disabled: Boolean
}
public expect abstract class HTMLAnchorElement : HTMLElement

public expect open class ResizeObserver(callback: (Array<ResizeObserverEntry>, ResizeObserver) -> Unit) {
    public fun observe(target: Node, options: ResizeObserverInit)
    public fun unobserve(target: Node)
}

public expect interface ResizeObserverInit {
    public open var box: String?
}

public expect abstract class ResizeObserverEntry {
    public open val contentRect: DOMRect
}