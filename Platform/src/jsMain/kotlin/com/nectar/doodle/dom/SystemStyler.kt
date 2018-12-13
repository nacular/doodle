package com.nectar.doodle.dom

import org.w3c.dom.css.CSSStyleSheet
import org.w3c.dom.css.get
import kotlin.browser.document

/**
 * Created by Nicholas Eddy on 10/31/17.
 */
interface SystemStyler

internal class SystemStylerImpl: SystemStyler {
    // FIXME: Make these styles local and applicable to the root not instead of assuming document.body
    init {
        if (document.styleSheets.length == 0) {
            document.head?.appendChild(document.createElement("style"))
        }

        (document.styleSheets[0] as? CSSStyleSheet)?.apply {
            insertRule("body{-webkit-touch-callout: none; -webkit-user-select: none; -khtml-user-select: none;-moz-user-select: none;-ms-user-select: none;user-select: none}", 0)

            insertRule("html { border:0 }", 0)
            insertRule("html,body{ height:100%;width:100%;overflow:hidden;cursor:default;margin:0;padding:0 }", 0)

            insertRule("body * { position:absolute;overflow:hidden;font-family:monospace;font-size:inherit }", 0)
            insertRule("body pre { overflow:visible }", 0)
            insertRule("body div { display:inline }", 0)

            insertRule("pre { margin:0 }", 0)
            insertRule("svg { display:inline-block;width:100%;height:100% }", 0)
            insertRule("svg * { position:absolute }", 0)
        }
    }
}