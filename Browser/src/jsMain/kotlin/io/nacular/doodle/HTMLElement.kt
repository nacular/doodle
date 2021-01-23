package io.nacular.doodle

import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.TouchEvent

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
internal actual typealias CSSRule       = org.w3c.dom.css.CSSRule
internal actual typealias CSSRuleList   = org.w3c.dom.css.CSSRuleList
internal actual typealias CSSStyleSheet = org.w3c.dom.css.CSSStyleSheet

internal actual val CSSStyleSheet.numStyles: Int get() = this.cssRules.length

internal actual typealias CSSStyleDeclaration = org.w3c.dom.css.CSSStyleDeclaration

internal actual var CSSStyleDeclaration.clipPath: String get() = this.asDynamic()["clip-path"] as String
    set(new) { this.asDynamic()["clip-path"] = new }

internal actual var CSSStyleDeclaration.willChange: String get() = try { this.asDynamic()["will-change"] as String } catch (ignore: Exception) { "" }
    set(new) {
        try {
            this.asDynamic()["will-change"] = new
        } catch (ignore: Exception) {}
    }

internal actual var CSSStyleDeclaration.textDecorationThickness: String get() = try { this.asDynamic()["text-decoration-thickness"] as String } catch (ignore: Exception) { "" }
    set(new) {
        try {
            this.asDynamic()["text-decoration-thickness"] = new
        } catch (ignore: Exception) {}
    }

internal actual typealias ElementCSSInlineStyle = org.w3c.dom.css.ElementCSSInlineStyle

internal actual typealias DOMRect   = org.w3c.dom.DOMRect
internal actual typealias Element   = org.w3c.dom.Element
internal actual typealias DragEvent = org.w3c.dom.DragEvent

public actual typealias HTMLElement = org.w3c.dom.HTMLElement

private val SIZE_OBSERVERS = mutableMapOf<HTMLElement, dynamic>()

internal actual fun HTMLElement.startMonitoringSize() {
    SIZE_OBSERVERS.getOrPut(this) {
        try {
            val observer = js(
         """new ResizeObserver(function(entries) {
                for (var i = 0; i < entries.length; ++i) {
                    var entry = entries[i]
                    if (entry.target.onresize) { entry.target.onresize(new Event("onresize")) }
                }
            })""")

            observer.observe(this)

        } catch (ignored: Throwable) {}
    }
}

internal actual fun HTMLElement.stopMonitoringSize() {
    SIZE_OBSERVERS.remove(this)?.disconnect()
}

internal actual var HTMLElement.ontouchmove: ((TouchEvent) -> Any)? get() = this.asDynamic()["ontouchmove"]
    set(new) { this.asDynamic()["ontouchmove"] = new }

internal actual var HTMLElement.role: String? get() = this.getAttribute("role")
    set(new) { this.setAttribute("role", new ?: "") }

internal actual typealias ElementCreationOptions = org.w3c.dom.ElementCreationOptions

internal actual typealias Document = org.w3c.dom.Document

internal actual fun Document.addEventListener   (to: String, listener: (Event) -> Unit) = this.addEventListener(to, listener)
internal actual fun Document.removeEventListener(to: String, listener: (Event) -> Unit) = this.removeEventListener(to, listener)

internal actual typealias Text              = org.w3c.dom.Text
internal actual typealias CharacterData     = org.w3c.dom.CharacterData
internal actual typealias HTMLHeadElement   = org.w3c.dom.HTMLHeadElement
internal actual typealias HTMLImageElement  = org.w3c.dom.HTMLImageElement
internal actual typealias HTMLInputElement  = org.w3c.dom.HTMLInputElement
internal actual typealias HTMLButtonElement = org.w3c.dom.HTMLButtonElement

internal actual typealias StyleSheet = org.w3c.dom.css.StyleSheet

internal actual inline operator fun StyleSheetList.get(index: Int): StyleSheet? = item(index)

internal actual typealias StyleSheetList = org.w3c.dom.css.StyleSheetList

internal actual typealias HTMLStyleElement = org.w3c.dom.HTMLStyleElement

internal actual typealias HTMLMetaElement = org.w3c.dom.HTMLMetaElement