package io.nacular.doodle.dom

import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.jsObject
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Vertical
import org.w3c.dom.get
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

public actual inline fun CSSStyleDeclaration.setProperty_(property: String, value: String, priority: String): Unit = this.setProperty(property, value, priority)
public actual inline fun CSSStyleDeclaration.setProperty_(property: String, value: String                  ): Unit = this.setProperty(property, value)

internal actual var CSSStyleDeclaration.clipPath               : String by DynamicProperty("clip-path"                ) { "" }
internal actual var CSSStyleDeclaration.willChange             : String by DynamicProperty("will-change"              ) { "" }
internal actual var CSSStyleDeclaration.scrollBehavior         : String by DynamicProperty("scroll-behavior"          ) { "" }
internal actual var CSSStyleDeclaration.textDecorationThickness: String by DynamicProperty("text-decoration-thickness") { "" }
internal actual var CSSStyleDeclaration.touchAction            : String by DynamicProperty("touch-action"             ) { "" }
internal actual var CSSStyleDeclaration._webkit_appearance     : String by DynamicProperty("-webkit-appearance"       ) { "" }
internal actual var CSSStyleDeclaration.caretColor             : String by DynamicProperty("caret-color"              ) { "" }
internal actual var CSSStyleDeclaration.userSelect             : String by DynamicProperty("user-select"              ) { "" }

internal actual var CSSStyleDeclaration._ms_user_select            : String by DynamicProperty("-ms-user-select"            ) { "" }
internal actual var CSSStyleDeclaration._moz_user_select           : String by DynamicProperty("-moz-user-select"           ) { "" }
internal actual var CSSStyleDeclaration._khtml_user_select         : String by DynamicProperty("-khtml-user-select"         ) { "" }
internal actual var CSSStyleDeclaration._webkit_user_select        : String by DynamicProperty("-webkit-user-select"        ) { "" }
internal actual var CSSStyleDeclaration._webkit_touch_callout      : String by DynamicProperty("-webkit-touch-callout"      ) { "" }
internal actual var CSSStyleDeclaration._webkit_tap_highlight_color: String by DynamicProperty("-webkit-tap-highlight_color") { "" }

internal actual typealias CanvasRenderingContext2D = org.w3c.dom.CanvasRenderingContext2D

internal actual var CanvasRenderingContext2D.wordSpacing  : String? by DynamicProperty("wordSpacing"  ) { null }
internal actual var CanvasRenderingContext2D.letterSpacing: String? by DynamicProperty("letterSpacing") { null }
internal actual fun CanvasRenderingContext2D.measureText(string: String): Size = this.asDynamic().measureText(string).run {
    Size(this.asDynamic().width as Double, 0.0)
} as Size

public   actual typealias RenderingContext      = org.w3c.dom.RenderingContext
public   actual typealias HTMLCanvasElement     = org.w3c.dom.HTMLCanvasElement
internal actual typealias ElementCSSInlineStyle = org.w3c.dom.css.ElementCSSInlineStyle
internal actual typealias DOMRect               = org.w3c.dom.DOMRect
internal actual typealias Element               = org.w3c.dom.Element
public   actual typealias ParentNode            = org.w3c.dom.ParentNode
public   actual typealias HTMLElement           = org.w3c.dom.HTMLElement
public   actual typealias HTMLCollection        = org.w3c.dom.HTMLCollection

internal actual inline operator fun HTMLCollection.get(index: Int): Element? = get(index)

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

internal actual inline fun HTMLElement.addEventListener_        (to: String, noinline listener: (Event) -> Unit) = this.addEventListener   (to, listener)
internal actual inline fun HTMLElement.removeEventListener_     (to: String, noinline listener: (Event) -> Unit) = this.removeEventListener(to, listener)
internal actual inline fun HTMLElement.addActiveEventListener   (to: String, noinline listener: (Event) -> Unit) = this.addEventListener   (to, listener, jsObject { passive = false })
internal actual inline fun HTMLElement.removeActiveEventListener(to: String, noinline listener: (Event) -> Unit) = this.removeEventListener(to, listener, jsObject { passive = false })

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

internal actual fun Document.addEventListener   (to: String, listener: (Event) -> Unit): Unit = this.addEventListener(to, listener)
internal actual fun Document.removeEventListener(to: String, listener: (Event) -> Unit): Unit = this.removeEventListener(to, listener)
internal actual fun Document.getSelection(): Selection? = (this.asDynamic()["getSelection"])() as Selection
internal actual fun Document.getCaretFromPoint(point: Point): CaretPosition? =
    when (val caretPositionFromPoint = this.asDynamic()["caretPositionFromPoint"]) {
        null -> when (val caretRangeFromPoint = this.asDynamic()["caretRangeFromPoint"] ){
            null -> null
            else -> {
                val range = caretRangeFromPoint.call(this, point.x, point.y) as Range?
                range?.let {
                    CaretPosition(it.startContainer, it.startOffset)
                }
            }
        }
        else -> when (val caret = caretPositionFromPoint.call(this, point.x, point.y)) {
            null -> null
            else -> CaretPosition(caret.offsetNode as Node, caret.offset as Int)
        }
    }
internal actual fun Document.elementFromPoint(point: Point): Element? = (this.asDynamic()["elementFromPoint"])(point.x, point.y) as Element?

internal actual inline fun Document.createElement_  (localName: String, options: ElementCreationOptions): Element = this.createElement(localName, options)
internal actual inline fun Document.createElement_  (localName: String                                 ): Element = this.createElement(localName         )
internal actual inline fun Document.createElementNS_(namespace: String?, qualifiedName: String, options: ElementCreationOptions): Element = this.createElementNS(namespace, qualifiedName, options)
internal actual inline fun Document.createElementNS_(namespace: String?, qualifiedName: String): Element = this.createElementNS(namespace, qualifiedName)

internal actual external class Selection {
    actual fun removeAllRanges()
    actual fun addRange(range: Range)
}

internal actual typealias Text              = org.w3c.dom.Text
internal actual typealias Range             = org.w3c.dom.Range
internal actual typealias StyleSheet        = org.w3c.dom.css.StyleSheet
internal actual typealias CharacterData     = org.w3c.dom.CharacterData
internal actual typealias StyleSheetList    = org.w3c.dom.css.StyleSheetList
internal actual typealias HTMLHeadElement   = org.w3c.dom.HTMLHeadElement
internal actual typealias HTMLMetaElement   = org.w3c.dom.HTMLMetaElement
internal actual typealias HTMLImageElement  = org.w3c.dom.HTMLImageElement
internal actual typealias HTMLInputElement  = org.w3c.dom.HTMLInputElement
internal actual typealias HTMLStyleElement  = org.w3c.dom.HTMLStyleElement
internal actual typealias HTMLButtonElement = org.w3c.dom.HTMLButtonElement
internal actual typealias HTMLAnchorElement = org.w3c.dom.HTMLAnchorElement
internal actual typealias HTMLIFrameElement = org.w3c.dom.HTMLIFrameElement

internal actual inline fun HTMLInputElement.setSelectionRange(start: Int, end: Int) = this.setSelectionRange(start, end)
internal actual inline fun HTMLInputElement.focusInput() = this.asDynamic().focus(jsObject { preventScroll = true })

internal actual inline operator fun StyleSheetList.get(index: Int): StyleSheet? = item(index)

private class DynamicProperty<T, V>(private val name: String, private val onError: ((Throwable) -> V)? = null): ReadWriteProperty<T, V> {
    override fun getValue(thisRef: T, property: KProperty<*>): V = when (onError) {
        null -> thisRef.asDynamic()[name] as V
        else -> try { thisRef.asDynamic()[name] as V } catch (throwable: Throwable) { onError.invoke(throwable) }
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        try {
            thisRef.asDynamic()[name] = value
        } catch (throwable: Throwable) {
            onError?.invoke(throwable)
        }
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