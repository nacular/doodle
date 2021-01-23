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
    public actual var fontSize: String            = ""
    public actual var position: String            = ""
    public actual var transform: String           = ""
    public actual var marginTop: String           = ""
    public actual var overflowX: String           = ""
    public actual var overflowY: String           = ""
    public actual var boxShadow: String           = ""
    public actual var fontStyle: String           = ""
    public actual var textShadow: String          = ""
    public actual var textIndent: String          = ""
    public actual var fontFamily: String          = ""
    public actual var fontWeight: String          = ""
    public actual var background: String          = ""
    public actual var marginLeft: String          = ""
    public actual var whiteSpace: String          = ""
    public actual var marginRight: String         = ""
    public actual var borderStyle: String         = ""
    public actual var borderColor: String         = ""
    public actual var borderWidth: String         = ""
    public actual var borderRadius: String        = ""
    public actual var marginBottom: String        = ""
    public actual var outlineWidth: String        = ""
    public actual var backgroundSize: String      = ""
    public actual var textDecoration: String      = ""
    public actual var backgroundImage: String     = ""
    public actual var backgroundColor: String     = ""
    public actual var textDecorationLine: String  = ""
    public actual var textDecorationColor: String = ""
    public actual var textDecorationStyle: String = ""

    internal var clipPath_: String                  = ""
    internal var willChange_: String                = ""
    internal var textDecorationThickness_: String   = ""
}

public actual var CSSStyleDeclaration.clipPath: String get() = clipPath_; set(new) { clipPath_ = new }
public actual var CSSStyleDeclaration.willChange: String get() = willChange_; set(new) { willChange_ = new }
public actual var CSSStyleDeclaration.textDecorationThickness: String get() = textDecorationThickness_; set(new) { textDecorationThickness_ = new }

public actual class DOMRect {
    public actual var x: Double      = 0.0
    public actual var y: Double      = 0.0
    public actual var width: Double  = 0.0
    public actual var height: Double = 0.0
}

public actual abstract class Element: Node() {
    public actual open var id: String         = ""
    public actual open var className: String  = ""
    public actual open var innerHTML: String  = ""
    public actual open var scrollTop: Double  = 0.0
    public actual open var scrollLeft: Double = 0.0

    public actual fun getBoundingClientRect(): DOMRect = DOMRect()

    public actual fun setAttribute   (                    qualifiedName: String, value: String) {}
    public actual fun setAttributeNS (namespace: String?, qualifiedName: String, value: String) {}
    public actual fun removeAttribute(                    qualifiedName: String               ) {}
}

public actual class DragEvent {
    public actual val dataTransfer: DataTransfer? = null
}

public actual abstract class HTMLElement: Element() {
    internal var role_             = "" as String?
    public actual var title: String = ""
    public actual var draggable: Boolean = false
    public actual val offsetTop: Int = 0
    public actual val offsetLeft: Int = 0
    public actual val offsetWidth: Int = 0
    public actual val offsetHeight: Int = 0

    public actual var onkeyup    : ((KeyboardEvent) -> Boolean)? = null
    public actual var onkeydown  : ((KeyboardEvent) -> Boolean)? = null
    public actual var onkeypress : ((KeyboardEvent) -> Boolean)? = null
    public actual var onwheel    : ((WheelEvent   ) -> Any    )? = null
    public actual var onmouseup  : ((MouseEvent   ) -> Any    )? = null
    public actual var onmouseout : ((MouseEvent   ) -> Any    )? = null
    public actual var ondblclick : ((MouseEvent   ) -> Any    )? = null
    public actual var onmousedown: ((MouseEvent   ) -> Any    )? = null
    public actual var onmousemove: ((MouseEvent   ) -> Any    )? = null
    public actual var onmouseover: ((MouseEvent   ) -> Any    )? = null

    public actual abstract val style: CSSStyleDeclaration

    public actual var onresize   : ((Event) -> Unit)? = {}
    public actual var ondragstart: ((DragEvent) -> Boolean)? = { false }

    public actual var onpointerup  : ((PointerEvent) -> Any)? = null
    public actual var onpointerdown: ((PointerEvent) -> Any)? = null
    public actual var onpointermove: ((PointerEvent) -> Any)? = null
    public actual var onpointerover: ((PointerEvent) -> Any)? = null

    public actual var dir: String = ""

    public actual fun focus() {}
    public actual fun blur () {}
}

public actual fun HTMLElement.stopMonitoringSize () {}
public actual fun HTMLElement.startMonitoringSize() {}

public actual var HTMLElement.role: String?
    get() = role_
    set(new) { role_ = new }

public actual var HTMLElement.ontouchmove: ((TouchEvent) -> Any)? get() = null
    set(new) {}

public actual interface ElementCreationOptions

public actual class Document {
    public actual var body: HTMLElement?     = null
    public actual val head: HTMLHeadElement? = null

    public actual fun createElement(localName: String, options: ElementCreationOptions): Element = object: Element() {}
    public actual fun createTextNode(data: String): Text = Text()
    public actual fun createElementNS(namespace: String?, qualifiedName: String, options: ElementCreationOptions): Element = object: Element() {}
    public actual val styleSheets: StyleSheetList = object: StyleSheetList() {
        override val length get() = values.size
    }
}

public actual fun Document.addEventListener   (to: String, listener: (Event) -> Unit) {}
public actual fun Document.removeEventListener(to: String, listener: (Event) -> Unit) {}

public actual abstract class CharacterData   : Node()
public actual          class Text            : CharacterData()
public actual abstract class HTMLImageElement: HTMLElement() {
    public actual var src: String = ""
    public actual val complete: Boolean = false
}

public actual abstract class HTMLHeadElement  : HTMLElement()
public actual abstract class HTMLInputElement : HTMLElement()
public actual abstract class HTMLButtonElement: HTMLElement() {
    public actual var disabled: Boolean = false
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