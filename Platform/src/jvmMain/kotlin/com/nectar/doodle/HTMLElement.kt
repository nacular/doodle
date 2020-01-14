package com.nectar.doodle

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
actual abstract class CSSStyleSheet {
    actual fun insertRule(rule: String, index: Int) = 0
}

actual val CSSStyleSheet.numStyles: Int get() = 0


actual abstract class CSSStyleDeclaration {
    actual var top             = ""
    actual var font            = ""
    actual var left            = ""
    actual var right           = ""
    actual var width           = ""
    actual var color           = ""
    actual var height          = ""
    actual var margin          = ""
    actual var bottom          = ""
    actual var filter          = ""
    actual var border          = ""
    actual var padding         = ""
    actual var zIndex          = ""
    actual var display         = ""
    actual var opacity         = ""
    actual var fontSize        = ""
    actual var position        = ""
    actual var transform       = ""
    actual var marginTop       = ""
    actual var overflowX       = ""
    actual var overflowY       = ""
    actual var boxShadow       = ""
    actual var fontStyle       = ""
    actual var textShadow      = ""
    actual var textIndent      = ""
    actual var fontFamily      = ""
    actual var fontWeight      = ""
    actual var background      = ""
    actual var marginLeft      = ""
    actual var whiteSpace      = ""
    actual var marginRight     = ""
    actual var borderStyle     = ""
    actual var borderColor     = ""
    actual var borderWidth     = ""
    actual var borderRadius    = ""
    actual var marginBottom    = ""
    actual var outlineWidth    = ""
    actual var backgroundSize  = ""
    actual var backgroundImage = ""
    actual var backgroundColor = ""

    var clipPath_              = ""
    var willChange_            = ""
}

actual var CSSStyleDeclaration.clipPath get() = clipPath_
    set(new) { clipPath_ = new }

actual var CSSStyleDeclaration.willChange get() = willChange_
    set(new) { willChange_ = new }

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

actual class Event
actual class DragEvent

actual abstract class HTMLElement: Element() {
    actual var draggable    = false
    actual val offsetTop    = 0
    actual val offsetLeft   = 0
    actual val offsetWidth  = 0
    actual val offsetHeight = 0

    actual abstract val style: CSSStyleDeclaration

    abstract var onresize_   : ((Event) -> Unit)?
    abstract var ondragstart_: ((DragEvent) -> Boolean)?
}

actual var HTMLElement.onresize: ((Event) -> Unit)? get() = onresize_
    set(value) { onresize_ = value }

actual var HTMLElement.ondragstart: ((DragEvent) -> Boolean)? get() = ondragstart_
    set(value) { ondragstart_ = value }

actual interface ElementCreationOptions

actual class Document {
    actual val head: HTMLHeadElement? = null

    actual fun createElement(localName: String, options: ElementCreationOptions) = object: Element() {}
    actual fun createTextNode(data: String) = Text()
    actual fun createElementNS(namespace: String?, qualifiedName: String, options: ElementCreationOptions) = object: Element() {}
    actual val styleSheets = object: StyleSheetList() {
        override val length get() = values.size
    }
}

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

actual abstract class HTMLStyleElement : HTMLElement() {
    actual val sheet: StyleSheet?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}