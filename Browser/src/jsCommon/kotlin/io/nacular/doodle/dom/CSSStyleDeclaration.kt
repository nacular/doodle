@file:Suppress("EXTERNAL_TYPE_EXTENDS_NON_EXTERNAL_TYPE")

package io.nacular.doodle.dom

internal actual abstract external class CSSRule: JsAny {
    actual var cssText: String
}

internal actual abstract external class CSSRuleList: JsAny {
    actual abstract val length: Int
    actual fun item(index: Int): CSSRule?
}

internal actual abstract external class CSSStyleSheet: StyleSheet {
    actual val cssRules: CSSRuleList

//    actual fun insertRule(rule: String, index: Int): Int
    actual fun deleteRule(index: Int)
}

internal actual val CSSStyleSheet.numStyles: Int get() = this.cssRules.length

internal actual abstract external class CSSStyleDeclaration: JsAny {
    actual var top                : String
    actual var font               : String
    actual var left               : String
    actual var right              : String
    actual var width              : String
    actual var color              : String
    actual var cursor             : String
    actual var height             : String
    actual var margin             : String
    actual var bottom             : String
    actual var filter             : String
    actual var border             : String
    actual var padding            : String
    actual var zIndex             : String
    actual var display            : String
    actual var opacity            : String
    actual var outline            : String
    actual var fontSize           : String
    actual var position           : String
    actual var transform          : String
    actual var marginTop          : String
    actual var overflowX          : String
    actual var overflowY          : String
    actual var boxShadow          : String
    actual var fontStyle          : String
    actual var textAlign          : String
    actual var direction          : String
    actual var textShadow         : String
    actual var textIndent         : String
    actual var fontFamily         : String
    actual var fontWeight         : String
    actual var background         : String
    actual var marginLeft         : String
    actual var whiteSpace         : String
    actual var lineHeight         : String
    actual var marginRight        : String
    actual var borderStyle        : String
    actual var borderColor        : String
    actual var borderWidth        : String
    actual var wordSpacing        : String
    actual var fontVariant        : String
    actual var borderRadius       : String
    actual var marginBottom       : String
    actual var outlineWidth       : String
    actual var pointerEvents      : String
    actual var letterSpacing      : String
    actual var backgroundSize     : String
    actual var textDecoration     : String
    actual var backgroundImage    : String
    actual var backgroundColor    : String
    actual var textDecorationLine : String
    actual var textDecorationColor: String
    actual var textDecorationStyle: String

    actual var writingMode: String

    actual fun removeProperty(property: String): String
    actual fun setProperty   (property: String, value: String, priority: String)
    actual fun setProperty   (property: String, value: String                  )
}

internal actual var CSSStyleDeclaration.clipPath               : String get() = getString("clip-path"                ); set(value) = setString("clip-path"                , value) //by DynamicProperty("clip-path"                ) { "" }
internal actual var CSSStyleDeclaration.willChange             : String get() = getString("will-change"              ); set(value) = setString("will-change"              , value) //by DynamicProperty("will-change"              ) { "" }
internal actual var CSSStyleDeclaration.scrollBehavior         : String get() = getString("scroll-behavior"          ); set(value) = setString("scroll-behavior"          , value) //by DynamicProperty("scroll-behavior"          ) { "" }
internal actual var CSSStyleDeclaration.textDecorationThickness: String get() = getString("text-decoration-thickness"); set(value) = setString("text-decoration-thickness", value) //by DynamicProperty("text-decoration-thickness") { "" }
internal actual var CSSStyleDeclaration.caretColor             : String get() = getString("caret-color"              ); set(value) = setString("caret-color"              , value) //by DynamicProperty("caret-color"              ) { "" }
internal actual var CSSStyleDeclaration.userSelect             : String get() = getString("user-select"              ); set(value) = setString("user-select"              , value) //by DynamicProperty("user-select"              ) { "" }
//internal actual var CSSStyleDeclaration.touchAction            : String get() = getString("touch-action"); set(value) = setString("touch-action", value) //by DynamicProperty("touch-action"             ) { "" }

internal actual var CSSStyleDeclaration._webkit_text_stroke    : String get() = getString("-webkit-text-stroke"); set(value) = setString("-webkit-text-stroke", value) //by DynamicProperty("-webkit-text-stroke"      ) { "" }
internal actual var CSSStyleDeclaration._webkit_appearance     : String get() = getString("-webkit-appearance" ); set(value) = setString("-webkit-appearance",  value) //by DynamicProperty("-webkit-appearance"       ) { "" }
internal actual var CSSStyleDeclaration.backDropFilter: String get() = _backdropFilter; set(new) {
    _webkit_backdropFilter = new
    _backdropFilter        = new
}
internal actual var CSSStyleDeclaration.maskImage              : String get() = getString("mask-image"); set(value) = setString("mask-image", value) //by DynamicProperty("mask-image"            ) { "" }


private var CSSStyleDeclaration._backdropFilter          : String get() = getString("backdrop-filter"        ); set(value) = setString("backdrop-filter"        , value) //by DynamicProperty("backdrop-filter"        ) { "" }
private var CSSStyleDeclaration._webkit_backdropFilter   : String get() = getString("-webkit-backdrop-filter"); set(value) = setString("-webkit-backdrop-filter", value) //by DynamicProperty("-webkit-backdrop-filter") { "" }

internal actual var CSSStyleDeclaration._ms_user_select            : String get() = getString("-ms-user-select"            ); set(value) = setString("-ms-user-select",             value) //by DynamicProperty("-ms-user-select"            ) { "" }
internal actual var CSSStyleDeclaration._moz_user_select           : String get() = getString("-moz-user-select"           ); set(value) = setString("-moz-user-select",            value) //by DynamicProperty("-moz-user-select"           ) { "" }
internal actual var CSSStyleDeclaration._khtml_user_select         : String get() = getString("-khtml-user-select"         ); set(value) = setString("-khtml-user-select",          value) //by DynamicProperty("-khtml-user-select"         ) { "" }
internal actual var CSSStyleDeclaration._webkit_user_select        : String get() = getString("-webkit-user-select"        ); set(value) = setString("-webkit-user-select",         value) //by DynamicProperty("-webkit-user-select"        ) { "" }
internal actual var CSSStyleDeclaration._webkit_touch_callout      : String get() = getString("-webkit-touch-callout"      ); set(value) = setString("-webkit-touch-callout",       value) //by DynamicProperty("-webkit-touch-callout"      ) { "" }
internal actual var CSSStyleDeclaration._webkit_tap_highlight_color: String get() = getString("-webkit-tap-highlight_color"); set(value) = setString("-webkit-tap-highlight_color", value) //by DynamicProperty("-webkit-tap-highlight_color") { "" }