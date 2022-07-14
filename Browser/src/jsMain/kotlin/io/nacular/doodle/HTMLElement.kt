package io.nacular.doodle

import io.nacular.doodle.dom.Event
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Vertical
import org.w3c.dom.Node
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
internal actual typealias CSSRule       = org.w3c.dom.css.CSSRule
internal actual typealias CSSRuleList   = org.w3c.dom.css.CSSRuleList
internal actual typealias CSSStyleSheet = org.w3c.dom.css.CSSStyleSheet

internal actual val CSSStyleSheet.numStyles: Int get() = this.cssRules.length

internal actual typealias CSSStyleDeclaration = org.w3c.dom.css.CSSStyleDeclaration

internal actual var CSSStyleDeclaration.clipPath               : String by DynamicProperty("clip-path"                ) { "" }
internal actual var CSSStyleDeclaration.willChange             : String by DynamicProperty("will-change"              ) { "" }
internal actual var CSSStyleDeclaration.scrollBehavior         : String by DynamicProperty("scroll-behavior"          ) { "" }
internal actual var CSSStyleDeclaration.textDecorationThickness: String by DynamicProperty("text-decoration-thickness") { "" }
internal actual var CSSStyleDeclaration.touchAction            : String by DynamicProperty("touch-action"             ) { "" }
internal actual var CSSStyleDeclaration._webkit_appearance     : String by DynamicProperty("-webkit-appearance"       ) { "" }

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

internal actual var HTMLElement.role: String? get() = getAttribute("role")
    set(new) {
        when (new) {
            null -> removeAttribute("role"     )
            else -> setAttribute   ("role", new)
        }
    }

internal actual fun HTMLElement.addActiveEventListener   (to: String, listener: (Event) -> Unit) = this.addEventListener   (to, listener, jsObject { passive = false })
internal actual fun HTMLElement.removeActiveEventListener(to: String, listener: (Event) -> Unit) = this.removeEventListener(to, listener, jsObject { passive = false })



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

private class DynamicProperty<T, V>(private val name: String, private val onError: ((Throwable) -> V)? = null): ReadWriteProperty<T, V> {
    override fun getValue(thisRef: T, property: KProperty<*>): V = when (onError) {
        null -> thisRef.asDynamic()[name] as V
        else -> try { thisRef.asDynamic()[name] as V } catch (throwable: Throwable) { onError.invoke(throwable) }
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        try {
            thisRef.asDynamic()[name] = value
        } catch (ignored: Throwable) {}
    }
}

public actual open external class ResizeObserver actual constructor(callback: (Array<ResizeObserverEntry>, ResizeObserver) -> Unit) {
    public actual fun observe(target: Node, options: ResizeObserverInit)
    public actual fun unobserve(target: Node)
}

public actual external interface ResizeObserverInit {
    public actual var box: String? get() = definedExternally; set(value) = definedExternally
}

public actual abstract external class ResizeObserverEntry {
    public actual open val contentRect: DOMRect
}


@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun ResizeObserverInit(box: String? = undefined): ResizeObserverInit {
    val o = js("({})")
    o["box"] = box
    return o
}