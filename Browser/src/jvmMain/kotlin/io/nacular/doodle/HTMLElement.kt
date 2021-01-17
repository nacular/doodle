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
actual abstract class CSSRule {
    actual var cssText = ""
}

actual abstract class CSSRuleList {
    actual abstract val length: Int

    actual fun item(index: Int): CSSRule? = null
}

actual abstract class CSSStyleSheet: StyleSheet() {
    actual fun insertRule(rule: String, index: Int) = 0
    actual val cssRules: CSSRuleList
        get() = TODO("Not yet implemented")

    actual fun deleteRule(index: Int) {}
}

actual val CSSStyleSheet.numStyles: Int get() = 0


actual abstract class CSSStyleDeclaration {
    actual var top                 = ""
    actual var font                = ""
    actual var left                = ""
    actual var right               = ""
    actual var width               = ""
    actual var color               = ""
    actual var height              = ""
    actual var margin              = ""
    actual var bottom              = ""
    actual var filter              = ""
    actual var border              = ""
    actual var cursor              = ""
    actual var padding             = ""
    actual var zIndex              = ""
    actual var display             = ""
    actual var opacity             = ""
    actual var fontSize            = ""
    actual var position            = ""
    actual var transform           = ""
    actual var marginTop           = ""
    actual var overflowX           = ""
    actual var overflowY           = ""
    actual var boxShadow           = ""
    actual var fontStyle           = ""
    actual var textShadow          = ""
    actual var textIndent          = ""
    actual var fontFamily          = ""
    actual var fontWeight          = ""
    actual var background          = ""
    actual var marginLeft          = ""
    actual var whiteSpace          = ""
    actual var marginRight         = ""
    actual var borderStyle         = ""
    actual var borderColor         = ""
    actual var borderWidth         = ""
    actual var borderRadius        = ""
    actual var marginBottom        = ""
    actual var outlineWidth        = ""
    actual var backgroundSize      = ""
    actual var textDecoration      = ""
    actual var backgroundImage     = ""
    actual var backgroundColor     = ""
    actual var textDecorationLine  = ""
    actual var textDecorationColor = ""
    actual var textDecorationStyle = ""

    var clipPath_                  = ""
    var willChange_                = ""
    var textDecorationThickness_   = ""
}

actual var CSSStyleDeclaration.clipPath get() = clipPath_; set(new) { clipPath_ = new }
actual var CSSStyleDeclaration.willChange get() = willChange_; set(new) { willChange_ = new }
actual var CSSStyleDeclaration.textDecorationThickness get() = textDecorationThickness_; set(new) { textDecorationThickness_ = new }

actual class DOMRect {
    actual var x      = 0.0
    actual var y      = 0.0
    actual var width  = 0.0
    actual var height = 0.0
}

actual abstract class Element: Node() {
    actual open var id         = ""
    actual open var className  = ""
    actual open var innerHTML  = ""
    actual open var scrollTop  = 0.0
    actual open var scrollLeft = 0.0

    actual fun getBoundingClientRect() = DOMRect()

    actual fun setAttribute   (                    qualifiedName: String, value: String) {}
    actual fun setAttributeNS (namespace: String?, qualifiedName: String, value: String) {}
    actual fun removeAttribute(                    qualifiedName: String               ) {}
}

actual class DragEvent {
    actual val dataTransfer = null as DataTransfer?
}

actual abstract class HTMLElement: Element() {
    var role_               = "" as String?
    actual var title        = ""
    actual var draggable    = false
    actual val offsetTop    = 0
    actual val offsetLeft   = 0
    actual val offsetWidth  = 0
    actual val offsetHeight = 0

    actual var onkeyup      = null as ((KeyboardEvent) -> Boolean)?
    actual var onkeydown    = null as ((KeyboardEvent) -> Boolean)?
    actual var onkeypress   = null as ((KeyboardEvent) -> Boolean)?
    actual var onwheel      = null as ((WheelEvent) -> Any)?
    actual var onmouseup    = null as ((MouseEvent) -> Any)?
    actual var onmouseout   = null as ((MouseEvent) -> Any)?
    actual var ondblclick   = null as ((MouseEvent) -> Any)?
    actual var onmousedown  = null as ((MouseEvent) -> Any)?
    actual var onmousemove  = null as ((MouseEvent) -> Any)?
    actual var onmouseover  = null as ((MouseEvent) -> Any)?

    actual abstract val style: CSSStyleDeclaration

    actual var onresize   : ((Event) -> Unit)? = {}
    actual var ondragstart: ((DragEvent) -> Boolean)? = { false }

    actual var onpointerup   = null as ((PointerEvent) -> Any)?
    actual var onpointerdown = null as ((PointerEvent) -> Any)?
    actual var onpointermove = null as ((PointerEvent) -> Any)?
    actual var onpointerover = null as ((PointerEvent) -> Any)?

    actual var dir = ""

    actual fun focus() {}
    actual fun blur () {}
}

actual fun HTMLElement.stopMonitoringSize () {}
actual fun HTMLElement.startMonitoringSize() {}

actual var HTMLElement.role get() = role_
    set(new) { role_ = new }

actual var HTMLElement.ontouchmove: ((TouchEvent) -> Any)? get() = null
    set(new) {}

actual interface ElementCreationOptions

actual class Document {
    actual var body: HTMLElement?     = null
    actual val head: HTMLHeadElement? = null

    actual fun createElement(localName: String, options: ElementCreationOptions) = object: Element() {}
    actual fun createTextNode(data: String) = Text()
    actual fun createElementNS(namespace: String?, qualifiedName: String, options: ElementCreationOptions) = object: Element() {}
    actual val styleSheets = object: StyleSheetList() {
        override val length get() = values.size
    }
}

actual fun Document.addEventListener   (to: String, listener: (Event) -> Unit) {}
actual fun Document.removeEventListener(to: String, listener: (Event) -> Unit) {}

actual abstract class CharacterData   : Node()
actual          class Text            : CharacterData()
actual abstract class HTMLImageElement: HTMLElement() {
    actual var src      = ""
    actual val complete = false
}

actual abstract class HTMLHeadElement  : HTMLElement()
actual abstract class HTMLInputElement : HTMLElement()
actual abstract class HTMLButtonElement: HTMLElement() {
    actual var disabled = false
}

actual abstract class StyleSheet

actual inline operator fun StyleSheetList.get(index: Int): StyleSheet? = item(index)

actual abstract class StyleSheetList {
    protected val values: List<StyleSheet> = mutableListOf()

    actual abstract val length: Int

    actual fun item(index: Int) = try {
        values[index]
    } catch (e: Exception) {
        null
    }
}

actual interface ElementCSSInlineStyle {
    actual val style: CSSStyleDeclaration
}

actual abstract class HTMLStyleElement: HTMLElement() {
    actual val sheet: StyleSheet?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}

actual abstract class HTMLMetaElement: HTMLElement() {
    actual var name    = ""
    actual var content = ""
}