@file:Suppress("UNUSED_PARAMETER", "PropertyName", "ObjectPropertyName")

package io.nacular.doodle.dom

import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.Orientation

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
public actual abstract class CSSRule {
    public actual var cssText: String = ""
}

public actual abstract class CSSRuleList {
    public actual abstract val length: Int

    public actual fun item(index: Int): CSSRule? = null
}

public actual abstract class CSSStyleSheet: StyleSheet() {
    public actual fun insertRule(rule: String, index: Int): Int = 0
    public actual val cssRules: CSSRuleList
        get() = TODO("Not yet implemented")

    public actual fun deleteRule(index: Int) {}
}

public actual val CSSStyleSheet.numStyles: Int get() = 0


public actual abstract class CSSStyleDeclaration {
    public actual var top: String                 = ""
    public actual var font: String                = ""
    public actual var left: String                = ""
    public actual var right: String               = ""
    public actual var width: String               = ""
    public actual var color: String               = ""
    public actual var height: String              = ""
    public actual var margin: String              = ""
    public actual var bottom: String              = ""
    public actual var filter: String              = ""
    public actual var border: String              = ""
    public actual var cursor: String              = ""
    public actual var zIndex: String              = ""
    public actual var padding: String             = ""
    public actual var display: String             = ""
    public actual var opacity: String             = ""
    public actual var outline: String             = ""
    public actual var fontSize: String            = ""
    public actual var position: String            = ""
    public actual var transform: String           = ""
    public actual var marginTop: String           = ""
    public actual var overflowX: String           = ""
    public actual var overflowY: String           = ""
    public actual var boxShadow: String           = ""
    public actual var fontStyle: String           = ""
    public actual var textAlign: String           = ""
    public actual var textShadow: String          = ""
    public actual var textIndent: String          = ""
    public actual var fontFamily: String          = ""
    public actual var fontWeight: String          = ""
    public actual var background: String          = ""
    public actual var marginLeft: String          = ""
    public actual var whiteSpace: String          = ""
    public actual var lineHeight: String          = ""
    public actual var marginRight: String         = ""
    public actual var borderStyle: String         = ""
    public actual var borderColor: String         = ""
    public actual var borderWidth: String         = ""
    public actual var wordSpacing: String         = ""
    public actual var fontVariant: String         = ""
    public actual var borderRadius: String        = ""
    public actual var marginBottom: String        = ""
    public actual var outlineWidth: String        = ""
    public actual var letterSpacing: String       = ""
    public actual var backgroundSize: String      = ""
    public actual var textDecoration: String      = ""
    public actual var backgroundImage: String     = ""
    public actual var backgroundColor: String     = ""
    public actual var textDecorationLine: String  = ""
    public actual var textDecorationColor: String = ""
    public actual var textDecorationStyle: String = ""

    internal var clipPath_               : String = ""
    internal var willChange_             : String = ""
    internal var scrollBehavior_         : String = ""
    internal var textDecorationThickness_: String = ""
    internal var touchAction_            : String = ""
    internal var _webkit_appearance_     : String = ""
    internal var caretColor_             : String = ""
    internal var userSelect_             : String = ""

    internal var _ms_user_select_            : String = ""
    internal var _moz_user_select_           : String = ""
    internal var _khtml_user_select_         : String = ""
    internal var _webkit_user_select_        : String = ""
    internal var _webkit_touch_callout_      : String = ""
    internal var _webkit_tap_highlight_color_: String = ""

    public actual fun removeProperty(property: String): String = ""
}

public actual fun CSSStyleDeclaration.setProperty_(property: String, value: String, priority: String) {}
public actual fun CSSStyleDeclaration.setProperty_(property: String, value: String                  ) {}

public actual var CSSStyleDeclaration.clipPath               : String get() = clipPath_;                set(new) { clipPath_                = new }
public actual var CSSStyleDeclaration.willChange             : String get() = willChange_;              set(new) { willChange_              = new }
public actual var CSSStyleDeclaration.scrollBehavior         : String get() = scrollBehavior_;          set(new) { scrollBehavior_          = new }
public actual var CSSStyleDeclaration.textDecorationThickness: String get() = textDecorationThickness_; set(new) { textDecorationThickness_ = new }
public actual var CSSStyleDeclaration.touchAction            : String get() = touchAction_;             set(new) { touchAction_             = new }
public actual var CSSStyleDeclaration._webkit_appearance     : String get() = _webkit_appearance_;      set(new) { _webkit_appearance_      = new }
public actual var CSSStyleDeclaration.caretColor             : String get() = caretColor_;              set(new) { caretColor_              = new }
public actual var CSSStyleDeclaration.userSelect             : String get() = userSelect_;              set(new) { userSelect_              = new }

public actual var CSSStyleDeclaration._ms_user_select            : String get() = _ms_user_select_;             set(new) { _ms_user_select_             = new }
public actual var CSSStyleDeclaration._moz_user_select           : String get() = _moz_user_select_;            set(new) { _moz_user_select_            = new }
public actual var CSSStyleDeclaration._khtml_user_select         : String get() = _khtml_user_select_;          set(new) { _khtml_user_select_          = new }
public actual var CSSStyleDeclaration._webkit_user_select        : String get() = _webkit_user_select_;         set(new) { _webkit_user_select_         = new }
public actual var CSSStyleDeclaration._webkit_touch_callout      : String get() = _webkit_touch_callout_;       set(new) { _webkit_touch_callout_       = new }
public actual var CSSStyleDeclaration._webkit_tap_highlight_color: String get() = _webkit_tap_highlight_color_; set(new) { _webkit_tap_highlight_color_ = new }

public actual abstract class CanvasRenderingContext2D {
    internal var _wordSpacing  : String? = ""
    internal var _letterSpacing: String? = ""

    public actual abstract var font: String
}

public actual var CanvasRenderingContext2D.wordSpacing  : String? get() = _wordSpacing;   set(new) { _wordSpacing   = new }
public actual var CanvasRenderingContext2D.letterSpacing: String? get() = _letterSpacing; set(new) { _letterSpacing = new }
internal actual fun CanvasRenderingContext2D.measureText(string: String): Size = Size.Empty

public actual interface RenderingContext

public actual abstract class HTMLCanvasElement: HTMLElement() {
    internal actual fun getContext(contextId: String, vararg arguments: Any?): RenderingContext? = null
}

public actual class DOMRect {
    public actual var x: Double      = 0.0
    public actual var y: Double      = 0.0
    public actual var width: Double  = 0.0
    public actual var height: Double = 0.0
}

public actual abstract class HTMLCollection public actual constructor() {
    protected val values: List<Element> = mutableListOf()

    public actual abstract val length: Int

    public actual open fun item(index: Int): Element? = try {
        values[index]
    } catch (e: Exception) {
        null
    }
}

public actual inline operator fun HTMLCollection.get(index: Int): Element? = item(index)

private class HTMLCollectionImpl: HTMLCollection() {
    override val length: Int get() = values.size
}

public actual interface ParentNode {
    public actual val children: HTMLCollection
}

public actual abstract class Element: Node(), ParentNode {
    public actual open var id        : String = ""
    public actual open var className : String = ""
    public actual open var scrollTop : Double = 0.0
    public actual open var scrollLeft: Double = 0.0
    public actual open val clientWidth: Int = 0
    public actual open val clientHeight: Int = 0

    public actual open var outerHTML: String = ""

    public actual fun getBoundingClientRect(): DOMRect = DOMRect()

    public actual fun getAttribute   (                    qualifiedName: String               ): String? = ""
    public actual fun setAttribute   (                    qualifiedName: String, value: String) {}
    public actual fun setAttributeNS (namespace: String?, qualifiedName: String, value: String) {}
    public actual fun removeAttribute(                    qualifiedName: String               ) {}
    public actual fun scrollTo(x: Double, y: Double) {}
    public actual abstract fun remove()
}

public actual class DragEvent: MouseEvent() {
    public actual val dataTransfer: DataTransfer? = null
}

public actual abstract class HTMLElement: Element(), ElementCSSInlineStyle {
    internal var role_             = "" as String?
    public actual var title       : String  = ""
    public actual var draggable   : Boolean = false
    public actual val offsetTop   : Int     = 0
    public actual val offsetLeft  : Int     = 0
    public actual val offsetWidth : Int     = 0
    public actual val offsetHeight: Int     = 0
    public actual var tabIndex    : Int     = 0
    public actual var spellcheck  : Boolean = false

    public actual var onwheel    : ((WheelEvent   ) -> Any    )? = null
    public actual var onkeyup    : ((KeyboardEvent) -> Boolean)? = null
    public actual var onkeydown  : ((KeyboardEvent) -> Boolean)? = null
    public actual var onkeypress : ((KeyboardEvent) -> Boolean)? = null

    public actual var onresize   : ((Event) -> Unit)? = null

    public actual var onload : ((Event) -> Any)? = null
    public actual var onerror: ((Any, String, Int, Int, Any?) -> Any)? = null

    public actual var ondblclick     : ((MouseEvent  ) -> Any)? = null
    public actual var onpointerup    : ((PointerEvent) -> Any)? = null
    public actual var onpointerout   : ((PointerEvent) -> Any)? = null
    public actual var onpointerdown  : ((PointerEvent) -> Any)? = null
    public actual var onpointermove  : ((PointerEvent) -> Any)? = null
    public actual var onpointerover  : ((PointerEvent) -> Any)? = null
    public actual var onpointercancel: ((PointerEvent) -> Any)? = null
    public actual var oncontextmenu  : ((MouseEvent  ) -> Any)? = null
    public actual var onblur         : ((FocusEvent  ) -> Any)? = null
    public actual var onclick        : ((MouseEvent  ) -> Any)? = null
    public actual var onfocus        : ((FocusEvent  ) -> Any)? = null
    public actual var onscroll       : ((Event       ) -> Any)? = null
    public actual var oninput        : ((InputEvent  ) -> Any)? = null
    public actual var onchange       : ((Event       ) -> Any)? = null
    public actual var onselect       : ((Event       ) -> Any)? = null
    public actual var ondrop         : ((DragEvent   ) -> Any)? = null
    public actual var ondragend      : ((DragEvent   ) -> Any?)? = null
    public actual var ondragover     : ((DragEvent   ) -> Any)? = null
    public actual var ondragenter    : ((DragEvent   ) -> Any)? = null
    public actual var ondragstart    : ((DragEvent   ) -> Any)? = null

    public actual var dir: String = ""

    public actual fun focus() {}
    public actual fun blur () {}
}

public actual fun HTMLElement.stopMonitoringSize () {}
public actual fun HTMLElement.startMonitoringSize() {}

public actual var HTMLElement.role: String? get() = role_; set(new) { role_ = new }

internal actual fun HTMLElement.addEventListener_        (to: String, listener: (Event) -> Unit) {}
internal actual fun HTMLElement.removeEventListener_     (to: String, listener: (Event) -> Unit) {}
internal actual fun HTMLElement.addActiveEventListener   (to: String, listener: (Event) -> Unit) {}
internal actual fun HTMLElement.removeActiveEventListener(to: String, listener: (Event) -> Unit) {}

internal actual var HTMLInputElement.orient: String? get() = orient_; set(new) { orient_ = new }
internal actual fun HTMLInputElement.setOrientation(orientation: Orientation) {}

public actual interface ElementCreationOptions

public actual class Document {
    public actual var body: HTMLElement?     = null
    public actual val head: HTMLHeadElement? = null

    public actual val styleSheets: StyleSheetList = object: StyleSheetList() {
        override val length get() = values.size
    }

//    public actual fun createElement(localName: String, options: ElementCreationOptions): Element = object: Element() {
//        override val children: HTMLCollection get() = HTMLCollectionImpl()
//    }
//    public actual fun createElementNS(namespace: String?, qualifiedName: String, options: ElementCreationOptions): Element = object: Element() {
//        override val children: HTMLCollection get() = HTMLCollectionImpl()
//    }
    public actual fun createTextNode(data: String): Text = Text()

    internal actual fun createRange(): Range = Range()
}

internal actual fun Document.addEventListener   (to: String, listener: (Event) -> Unit) {}
internal actual fun Document.removeEventListener(to: String, listener: (Event) -> Unit) {}
internal actual fun Document.getSelection(): Selection? = null
internal actual fun Document.getCaretFromPoint(point: Point): CaretPosition? = null
internal actual fun Document.elementFromPoint(point: Point): Element?  = null

internal actual fun Document.createElement_  (localName: String, options: ElementCreationOptions): Element = ElementImpl()
internal actual fun Document.createElement_  (localName: String): Element = ElementImpl()
internal actual fun Document.createElementNS_(namespace: String?, qualifiedName: String, options: ElementCreationOptions): Element = ElementImpl()
internal actual fun Document.createElementNS_(namespace: String?, qualifiedName: String): Element = ElementImpl()

private class ElementImpl: Element() {
    override val children: HTMLCollection get() = HTMLCollectionImpl()
    override fun remove() {}
}

public actual class Range {
    public actual val collapsed: Boolean = false
    public actual fun setStart(node: Node, offset: Int) {}
    public actual fun setEnd  (node: Node, offset: Int) {}
}

internal actual class Selection {
    actual fun removeAllRanges() {}
    actual fun addRange(range: Range) {}
}

public actual abstract class CharacterData   : Node()
public actual          class Text            : CharacterData()
public actual abstract class HTMLImageElement: HTMLElement() {
    public actual var src     : String = ""
    public actual val complete: Boolean = false
}

public actual abstract class HTMLHeadElement : HTMLElement()
public actual abstract class HTMLInputElement: HTMLElement() {
    internal var orient_ = "" as String?

    internal actual var type           = ""
    internal actual var step           = ""
    internal actual var value          = ""
    internal actual val files          = null as FileList?
    internal actual var accept         = ""
    internal actual var checked        = false
    internal actual var pattern        = ""
    internal actual var disabled       = false
    internal actual var multiple       = false
    internal actual var placeholder    = ""
    internal actual var selectionEnd   = null as Int?
    internal actual var indeterminate  = false
    internal actual var selectionStart = null as Int?
}

internal actual fun HTMLInputElement.setSelectionRange(start: Int, end: Int) {}
internal actual fun HTMLInputElement.focusInput() {}

public actual abstract class HTMLButtonElement: HTMLElement() {
    internal actual var disabled: Boolean = false
}

public actual abstract class HTMLAnchorElement: HTMLElement() {
    public actual abstract var href  : String
    public actual abstract var host  : String
    public actual          var target: String = ""
}

public actual abstract class HTMLIFrameElement: HTMLElement() {
    public actual open var src: String = ""
}

public actual abstract class StyleSheet

public actual inline operator fun StyleSheetList.get(index: Int): StyleSheet? = item(index)

public actual abstract class StyleSheetList {
    protected val values: List<StyleSheet> = mutableListOf()

    public actual abstract val length: Int

    public actual fun item(index: Int): StyleSheet? = try {
        values[index]
    } catch (e: Exception) {
        null
    }
}

public actual interface ElementCSSInlineStyle {
    public actual val style: CSSStyleDeclaration
}

public actual abstract class HTMLStyleElement: HTMLElement() {
    public actual val sheet: StyleSheet?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}

public actual abstract class HTMLMetaElement: HTMLElement() {
    public actual var name: String = ""
    public actual var content: String = ""
}

public actual open class ResizeObserver actual constructor(callback: (Array<ResizeObserverEntry>, ResizeObserver) -> Unit) {
    public actual fun observe(target: Node, options: ResizeObserverInit) {}
    public actual fun unobserve(target: Node) {}
}

public actual interface ResizeObserverInit {
    public actual var box: String? get() = ""; set(new) {}
}

public actual abstract class ResizeObserverEntry {
    public actual open val contentRect: DOMRect get() = TODO("Not yet implemented")
}
