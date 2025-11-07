//@file:Suppress(PropertyName", "ObjectPropertyName")

package io.nacular.doodle.dom

import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.Orientation

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
internal actual abstract class CSSRule: JsAny {
    actual var cssText: String = ""
}

internal actual abstract class CSSRuleList: JsAny {
    actual abstract val length: Int

    actual fun item(index: Int): CSSRule? = null
}

internal actual abstract class CSSStyleSheet: StyleSheet() {
//    actual fun insertRule(rule: String, index: Int): Int = 0
    actual val cssRules: CSSRuleList
        get() = TODO("Not yet implemented")

    actual fun deleteRule(index: Int) {}
}

internal actual fun CSSStyleSheet.tryInsertRule(rule: String, index: Int): Int = 0


internal actual val CSSStyleSheet.numStyles: Int get() = 0

internal actual abstract class CSSStyleDeclaration: JsAny {
    actual var top: String                 = ""
    actual var font: String                = ""
    actual var left: String                = ""
    actual var right: String               = ""
    actual var width: String               = ""
    actual var color: String               = ""
    actual var height: String              = ""
    actual var margin: String              = ""
    actual var bottom: String              = ""
    actual var filter: String              = ""
    actual var border: String              = ""
    actual var cursor: String              = ""
    actual var zIndex: String              = ""
    actual var padding: String             = ""
    actual var display: String             = ""
    actual var opacity: String             = ""
    actual var outline: String             = ""
    actual var fontSize: String            = ""
    actual var position: String            = ""
    actual var transform: String           = ""
    actual var marginTop: String           = ""
    actual var overflowX: String           = ""
    actual var overflowY: String           = ""
    actual var boxShadow: String           = ""
    actual var fontStyle: String           = ""
    actual var textAlign: String           = ""
    actual var direction: String           = ""
    actual var textShadow: String          = ""
    actual var textIndent: String          = ""
    actual var fontFamily: String          = ""
    actual var fontWeight: String          = ""
    actual var background: String          = ""
    actual var marginLeft: String          = ""
    actual var whiteSpace: String          = ""
    actual var lineHeight: String          = ""
    actual var marginRight: String         = ""
    actual var borderStyle: String         = ""
    actual var borderColor: String         = ""
    actual var borderWidth: String         = ""
    actual var wordSpacing: String         = ""
    actual var fontVariant: String         = ""
    actual var borderRadius: String        = ""
    actual var marginBottom: String        = ""
    actual var outlineWidth: String        = ""
    actual var letterSpacing: String       = ""
    actual var backgroundSize: String      = ""
    actual var textDecoration: String      = ""
    actual var backgroundImage: String     = ""
    actual var backgroundColor: String     = ""
    actual var textDecorationLine: String  = ""
    actual var textDecorationColor: String = ""
    actual var textDecorationStyle: String = ""

    actual var writingMode        : String = ""

    internal var clipPath_               : String = ""
    internal var willChange_             : String = ""
    internal var scrollBehavior_         : String = ""
    internal var textDecorationThickness_: String = ""
    internal var touchAction_            : String = ""
    internal var _webkit_appearance_     : String = ""
    internal var caretColor_             : String = ""
    internal var userSelect_             : String = ""
    internal var textStroke_             : String = ""
    internal var backDropFilter_         : String = ""
    internal var maskImage_              : String = ""

    internal var _ms_user_select_            : String = ""
    internal var _moz_user_select_           : String = ""
    internal var _khtml_user_select_         : String = ""
    internal var _webkit_user_select_        : String = ""
    internal var _webkit_touch_callout_      : String = ""
    internal var _webkit_tap_highlight_color_: String = ""

    actual fun removeProperty(property: String): String = ""
    actual fun setProperty   (property: String, value: String, priority: String) {}
    actual fun setProperty   (property: String, value: String                  ) {}
}

internal actual var CSSStyleDeclaration.clipPath               : String get() = clipPath_;                set(new) { clipPath_                = new }
internal actual var CSSStyleDeclaration.willChange             : String get() = willChange_;              set(new) { willChange_              = new }
internal actual var CSSStyleDeclaration.scrollBehavior         : String get() = scrollBehavior_;          set(new) { scrollBehavior_          = new }
internal actual var CSSStyleDeclaration.textDecorationThickness: String get() = textDecorationThickness_; set(new) { textDecorationThickness_ = new }
internal actual var CSSStyleDeclaration.caretColor             : String get() = caretColor_;              set(new) { caretColor_              = new }

internal actual var CSSStyleDeclaration._webkit_text_stroke    : String get() = textStroke_;              set(new) { textStroke_              = new }
internal actual var CSSStyleDeclaration._webkit_appearance     : String get() = _webkit_appearance_;      set(new) { _webkit_appearance_      = new }
internal actual var CSSStyleDeclaration.backDropFilter         : String get() = backDropFilter_;          set(new) { backDropFilter_          = new }
internal actual var CSSStyleDeclaration.maskImage              : String get() = maskImage_;               set(new) { maskImage_               = new }

internal actual var CSSStyleDeclaration.userSelect             : String get() = userSelect_;              set(new) { userSelect_              = new }

internal actual var CSSStyleDeclaration._ms_user_select            : String get() = _ms_user_select_;             set(new) { _ms_user_select_             = new }
internal actual var CSSStyleDeclaration._moz_user_select           : String get() = _moz_user_select_;            set(new) { _moz_user_select_            = new }
internal actual var CSSStyleDeclaration._khtml_user_select         : String get() = _khtml_user_select_;          set(new) { _khtml_user_select_          = new }
internal actual var CSSStyleDeclaration._webkit_user_select        : String get() = _webkit_user_select_;         set(new) { _webkit_user_select_         = new }
internal actual var CSSStyleDeclaration._webkit_touch_callout      : String get() = _webkit_touch_callout_;       set(new) { _webkit_touch_callout_       = new }
internal actual var CSSStyleDeclaration._webkit_tap_highlight_color: String get() = _webkit_tap_highlight_color_; set(new) { _webkit_tap_highlight_color_ = new }

internal actual class TextMetrics: JsAny {
    actual val width                 : Double = 0.0
    actual val fontBoundingBoxDescent: Double = 0.0
}

internal actual abstract class CanvasRenderingContext2D: JsAny {
    internal var _wordSpacing  : String? = ""
    internal var _letterSpacing: String? = ""

    actual abstract var font: String

    actual fun measureText(string: String): TextMetrics = TextMetrics()
}

internal actual var CanvasRenderingContext2D.wordSpacing  : String? get() = _wordSpacing;   set(new) { _wordSpacing   = new }
internal actual var CanvasRenderingContext2D.letterSpacing: String? get() = _letterSpacing; set(new) { _letterSpacing = new }
internal actual fun CanvasRenderingContext2D.measureText_(string: String): Size = Size.Empty

internal actual interface RenderingContext

internal actual abstract class HTMLCanvasElement: HTMLElement() {
    actual fun getContext(contextId: String, vararg arguments: JsAny?): RenderingContext? = null
}

internal actual class DOMRect: JsAny {
    actual var x: Double      = 0.0
    actual var y: Double      = 0.0
    actual var width: Double  = 0.0
    actual var height: Double = 0.0
}

internal actual abstract class HTMLCollection actual constructor(): JsAny {
    protected val values: List<Element> = mutableListOf()

    actual abstract val length: Int

    actual open fun item(index: Int): Element? = try {
        values[index]
    } catch (e: Exception) {
        null
    }
}

internal actual inline operator fun HTMLCollection.get(index: Int): Element? = item(index)

private class HTMLCollectionImpl: HTMLCollection() {
    override val length: Int get() = values.size
}

internal actual interface ParentNode: JsAny {
    actual val children: HTMLCollection
}

internal actual abstract class Element: Node(), ParentNode {
    actual open var id        : String = ""
    actual open var className : String = ""
    actual open var scrollTop : Double = 0.0
    actual open var scrollLeft: Double = 0.0
    actual open val clientWidth: Int = 0
    actual open val clientHeight: Int = 0

    actual open var outerHTML: String = ""

    actual fun getBoundingClientRect(): DOMRect = DOMRect()

    actual fun getAttribute   (                    qualifiedName: String               ): String? = ""
    actual fun setAttribute   (                    qualifiedName: String, value: String) {}
    actual fun setAttributeNS (namespace: String?, qualifiedName: String, value: String) {}
    actual fun removeAttribute(                    qualifiedName: String               ) {}
    actual fun scrollTo(x: Double, y: Double) {}
    actual abstract fun remove()
}

internal actual class FocusOptions: JsAny {
    actual var preventScroll: Boolean = false
}

internal actual abstract class HTMLElement: Element(), ElementCSSInlineStyle {
    internal var role_             = "" as String?
    actual var title       : String  = ""
    actual var draggable   : Boolean = false
    actual val offsetTop   : Int     = 0
    actual val offsetLeft  : Int     = 0
    actual val offsetWidth : Int     = 0
    actual val offsetHeight: Int     = 0
    actual var tabIndex    : Int     = 0
    actual var spellcheck  : Boolean = false

    actual var onwheel    : ((WheelEvent   ) -> Boolean)? = null
    actual var onkeyup    : ((KeyboardEvent) -> Boolean)? = null
    actual var onkeydown  : ((KeyboardEvent) -> Boolean)? = null
    actual var onkeypress : ((KeyboardEvent) -> Boolean)? = null

    actual var onresize   : ((Event) -> Unit)? = null

    actual var onload : ((Event) -> Unit)? = null
    actual var onerror: ((/*JsAny, String, Int, Int, JsAny?*/) -> Unit)? = null

    actual var ondblclick     : ((MouseEvent  ) -> Boolean)? = null
    actual var onpointerup    : ((PointerEvent) -> Boolean)? = null
    actual var onpointerout   : ((PointerEvent) -> Unit   )? = null
    actual var onpointerdown  : ((PointerEvent) -> Boolean)? = null
    actual var onpointermove  : ((PointerEvent) -> Boolean)? = null
    actual var onpointerover  : ((PointerEvent) -> Unit   )? = null
    actual var onpointercancel: ((PointerEvent) -> Unit   )? = null
    actual var oncontextmenu  : ((MouseEvent  ) -> Boolean)? = null
    actual var onblur         : ((FocusEvent  ) -> Unit   )? = null
    actual var onclick        : ((MouseEvent  ) -> Boolean)? = null
    actual var onfocus        : ((FocusEvent  ) -> Unit   )? = null
    actual var onscroll       : ((Event       ) -> Boolean)? = null
    actual var oninput        : ((InputEvent  ) -> Boolean)? = null
    actual var onchange       : ((Event       ) -> Boolean)? = null
    actual var onselect       : ((Event       ) -> Boolean)? = null
    actual var ondrop         : ((DragEvent   ) -> Unit   )? = null
    actual var ondragend      : ((DragEvent   ) -> Unit   )? = null
    actual var ondragover     : ((DragEvent   ) -> Unit   )? = null
    actual var ondragenter    : ((DragEvent   ) -> Unit   )? = null
    actual var ondragstart    : ((DragEvent   ) -> Unit   )? = null

    actual var dir: String = ""

    actual fun focus() {}
    actual fun focus(options: FocusOptions) {}
    actual fun blur () {}
    actual fun click() {}

    actual fun addEventListener   (to: String, listener: (Event) -> Unit) {}
    actual fun addEventListener   (to: String, listener: (Event) -> Unit, options: AddEventListenerOptions) {}
    actual fun removeEventListener(to: String, listener: (Event) -> Unit) {}
    actual fun removeEventListener(to: String, listener: (Event) -> Unit, options: AddEventListenerOptions) {}
}

internal actual fun HTMLElement.addActiveEventListener   (to: String, listener: (Event) -> Unit) {}
internal actual fun HTMLElement.removeActiveEventListener(to: String, listener: (Event) -> Unit) {}

internal actual fun HTMLElement.stopMonitoringSize () {}
internal actual fun HTMLElement.startMonitoringSize() {}

internal actual var HTMLElement.role: String? get() = role_; set(new) { role_ = new }

internal actual var HTMLInputElement.orient: String? get() = orient_; set(new) { orient_ = new }
internal actual fun HTMLInputElement.setOrientation(orientation: Orientation) {}

internal actual interface ElementCreationOptions: JsAny

internal actual class Document: JsAny {
    actual var body: HTMLElement?     = null
    actual val head: HTMLHeadElement? = null

    actual fun addEventListener   (to: String, listener: (Event) -> Unit) {}
    actual fun removeEventListener(to: String, listener: (Event) -> Unit) {}
    actual fun getSelection       (): Selection? = null

    actual fun createElement  (localName: String, options: ElementCreationOptions): Element = DummyElement
    actual fun createElement  (localName: String): Element = DummyElement
    actual fun createElementNS(namespace: String?, qualifiedName: String, options: ElementCreationOptions): Element = DummyElement
    actual fun createElementNS(namespace: String?, qualifiedName: String): Element = DummyElement

    actual fun createTextNode(data: String): Text = Text()

    actual fun createRange(): Range = Range()
}

private object DummyElement: Element() {
    override val children: HTMLCollection get() = HTMLCollectionImpl()
    override fun remove() {}
}

private class ElementImpl: Element() {
    override val children: HTMLCollection get() = HTMLCollectionImpl()
    override fun remove() {}
}

internal actual class Range: JsAny {
    actual val collapsed: Boolean = false
    actual fun setStart(node: Node, offset: Int) {}
    actual fun setEnd  (node: Node, offset: Int) {}
}

internal actual class Selection: JsAny {
    actual fun removeAllRanges() {}
    actual fun addRange(range: Range) {}
}

internal actual abstract class CharacterData   : Node()
internal actual          class Text            : CharacterData()
internal actual abstract class HTMLImageElement: HTMLElement() {
    actual var src     : String = ""
    actual var alt     : String = ""
    actual val complete: Boolean = false
    actual val width   : Int = 0
    actual val height  : Int = 0
}

internal actual abstract class HTMLHeadElement : HTMLElement()
internal actual abstract class HTMLInputElement: HTMLElement() {
    var orient_ = "" as String?

    actual var type           = ""
    actual var step           = ""
    actual var value          = ""
    actual val files          = null as FileList?
    actual var accept         = ""
    actual var checked        = false
    actual var pattern        = ""
    actual var disabled       = false
    actual var multiple       = false
    actual var placeholder    = ""
    actual var selectionEnd   = null as Int?
    actual var indeterminate  = false
    actual var selectionStart = null as Int?

    actual fun setSelectionRange(start: Int, end: Int) {}
}

//internal actual fun HTMLInputElement.setSelectionRange(start: Int, end: Int) {}
internal actual fun HTMLInputElement.focusInput() {}

internal actual abstract class HTMLButtonElement: HTMLElement() {
    actual var disabled: Boolean = false
}

internal actual abstract class HTMLAnchorElement: HTMLElement() {
    actual abstract var href  : String
    actual abstract var host  : String
    actual          var target: String = ""
}

internal actual abstract class HTMLIFrameElement: HTMLElement() {
    actual open var src: String = ""
}

internal actual abstract class StyleSheet: JsAny

internal actual inline operator fun StyleSheetList.get(index: Int): StyleSheet? = item(index)

internal actual abstract class StyleSheetList: JsAny {
    protected val values: List<StyleSheet> = mutableListOf()

    actual abstract val length: Int

    actual fun item(index: Int): StyleSheet? = try {
        values[index]
    } catch (e: Exception) {
        null
    }
}

internal actual interface ElementCSSInlineStyle: JsAny {
    actual val style: CSSStyleDeclaration
}

internal actual abstract class HTMLStyleElement: HTMLElement() {
    actual val sheet: StyleSheet?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}

internal actual abstract class HTMLMetaElement: HTMLElement() {
    actual var name: String = ""
    actual var content: String = ""
}

internal actual open class ResizeObserver actual constructor(callback: (JsArray<ResizeObserverEntry>, ResizeObserver) -> Unit): JsAny {
    actual fun observe   (target: Node, options: ResizeObserverInit) {}
    actual fun observe   (target: Node                             ) {}
    actual fun unobserve (target: Node) {}
    actual fun disconnect() {}
}

internal actual interface ResizeObserverInit: JsAny {
    actual var box: String?
}

internal actual abstract class ResizeObserverEntry: JsAny {
    actual open val contentRect: DOMRect get() = TODO("Not yet implemented")
    actual open val target: HTMLElement get() = TODO("Not yet implemented")
}

internal actual fun ResizeObserver.observeResize(target: Node, box: String) {}

internal actual interface AddEventListenerOptions: JsAny {
    actual var passive: Boolean?
    actual var once: Boolean?
}

internal actual val document: Document get() = TODO("Not yet implemented")

internal actual abstract class HTMLDataListElement : HTMLElement() {
    actual open val options: HTMLCollection get() = TODO("Not yet implemented")
}

internal actual abstract class HTMLOptionElement: HTMLElement() {
    actual open var value: String = ""
}