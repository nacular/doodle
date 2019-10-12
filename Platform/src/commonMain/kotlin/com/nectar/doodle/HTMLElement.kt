package com.nectar.doodle

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
expect abstract class CSSStyleSheet {
    fun insertRule(rule: String, index: Int): Int
}

expect val CSSStyleSheet.numStyles: Int

expect abstract class CSSStyleDeclaration {
    var top            : String
    var font           : String
    var left           : String
    var right          : String
    var width          : String
    var color          : String
    var height         : String
    var margin         : String
    var bottom         : String
    var filter         : String
    var border         : String
    var zIndex         : String
    var display        : String
    var opacity        : String
    var fontSize       : String
    var position       : String
    var transform      : String
    var marginTop      : String
    var overflowX      : String
    var overflowY      : String
    var boxShadow      : String
    var fontStyle      : String
    var textShadow     : String
    var textIndent     : String
    var fontFamily     : String
    var fontWeight     : String
    var background     : String
    var marginLeft     : String
    var whiteSpace     : String
    var marginRight    : String
    var borderStyle    : String
    var borderColor    : String
    var borderWidth    : String
    var borderRadius   : String
    var marginBottom   : String
    var outlineWidth   : String
    var backgroundImage: String
    var backgroundColor: String
}

expect var CSSStyleDeclaration.clipPath: String

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
    open var innerHTML : String
    open var scrollTop : Double
    open var scrollLeft: Double

    fun getBoundingClientRect(): DOMRect

    fun setAttribute   (                    qualifiedName: String, value: String)
    fun setAttributeNS (namespace: String?, qualifiedName: String, value: String)
    fun removeAttribute(                    qualifiedName: String               )
}

expect class Event
expect class DragEvent

expect abstract class HTMLElement: Element {
    var draggable   : Boolean
    val offsetTop   : Int
    val offsetLeft  : Int
    val offsetWidth : Int
    val offsetHeight: Int

    abstract val style: CSSStyleDeclaration
}

expect var HTMLElement.onresize   : ((Event) -> Unit)?
expect var HTMLElement.ondragstart: ((DragEvent) -> Boolean)?

expect interface ElementCreationOptions

expect abstract class StyleSheet

expect inline operator fun StyleSheetList.get(index: Int): StyleSheet?

expect abstract class StyleSheetList {
    val length: Int

    fun item(index: Int): StyleSheet?
}

expect class Document {
    val head: HTMLHeadElement?
    val styleSheets: StyleSheetList

    fun createElement(localName: String, options: ElementCreationOptions = object: ElementCreationOptions {}): Element
    fun createTextNode(data: String): Text
    fun createElementNS(namespace: String?, qualifiedName: String, options: ElementCreationOptions = object: ElementCreationOptions {}): Element
}

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