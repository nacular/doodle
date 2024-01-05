package io.nacular.doodle.dom

internal actual abstract external class CSSRule: JsAny {
    internal actual var cssText: String
}

internal actual abstract external class CSSRuleList: JsAny {
    internal actual abstract val length: Int
    internal actual fun item(index: Int): CSSRule?
}

internal actual abstract external class CSSStyleSheet: StyleSheet {
    internal actual val cssRules: CSSRuleList

//    internal actual fun insertRule(rule: String, index: Int): Int
    internal actual fun deleteRule(index: Int)
}

internal actual val CSSStyleSheet.numStyles: Int get() = this.cssRules.length

internal actual abstract external class CSSStyleDeclaration: JsAny {
    internal actual var top                : String
    internal actual var font               : String
    internal actual var left               : String
    internal actual var right              : String
    internal actual var width              : String
    internal actual var color              : String
    internal actual var cursor             : String
    internal actual var height             : String
    internal actual var margin             : String
    internal actual var bottom             : String
    internal actual var filter             : String
    internal actual var border             : String
    internal actual var padding            : String
    internal actual var zIndex             : String
    internal actual var display            : String
    internal actual var opacity            : String
    internal actual var outline            : String
    internal actual var fontSize           : String
    internal actual var position           : String
    internal actual var transform          : String
    internal actual var marginTop          : String
    internal actual var overflowX          : String
    internal actual var overflowY          : String
    internal actual var boxShadow          : String
    internal actual var fontStyle          : String
    internal actual var textAlign          : String
    internal actual var textShadow         : String
    internal actual var textIndent         : String
    internal actual var fontFamily         : String
    internal actual var fontWeight         : String
    internal actual var background         : String
    internal actual var marginLeft         : String
    internal actual var whiteSpace         : String
    internal actual var lineHeight         : String
    internal actual var marginRight        : String
    internal actual var borderStyle        : String
    internal actual var borderColor        : String
    internal actual var borderWidth        : String
    internal actual var wordSpacing        : String
    internal actual var fontVariant        : String
    internal actual var borderRadius       : String
    internal actual var marginBottom       : String
    internal actual var outlineWidth       : String
    internal actual var letterSpacing      : String
    internal actual var backgroundSize     : String
    internal actual var textDecoration     : String
    internal actual var backgroundImage    : String
    internal actual var backgroundColor    : String
    internal actual var textDecorationLine : String
    internal actual var textDecorationColor: String
    internal actual var textDecorationStyle: String

    internal actual var writingMode: String

    actual fun removeProperty(property: String): String
    actual fun setProperty   (property: String, value: String, priority: String)
    actual fun setProperty   (property: String, value: String                  )
}

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
