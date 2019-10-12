package com.nectar.doodle.dom

import com.nectar.doodle.CSSStyleSheet
import com.nectar.doodle.Document
import com.nectar.doodle.get
import com.nectar.doodle.numStyles

/**
 * Created by Nicholas Eddy on 10/31/17.
 */
interface SystemStyler

internal class SystemStylerImpl(htmlFactory: HtmlFactory, document: Document, allowDefaultDarkMode: Boolean): SystemStyler {
    // FIXME: Make these styles local and applicable to the root not instead of assuming document.body
    init {
        if (document.styleSheets.length == 0) {
            document.head?.appendChild(htmlFactory.create("style"))
        }

        (document.styleSheets[0] as? CSSStyleSheet)?.apply {
            if (allowDefaultDarkMode) {
                insertRule(":root {color-scheme:light dark}", numStyles)
            }

            // Disable selection: https://stackoverflow.com/questions/826782/how-to-disable-text-selection-highlighting#4407335
            insertRule("body{ -webkit-touch-callout:none;-webkit-user-select:none;-khtml-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none }", numStyles)

            insertRule("html { border:0;box-sizing:border-box }", numStyles)
            insertRule("html,body { height:100%;width:100%;overflow:hidden;cursor:default;margin:0;padding:0 }", numStyles)

            insertRule("* { box-sizing:inherit }", numStyles)

            insertRule("body * { position:absolute;overflow:hidden;font-weight:$defaultFontSize;font-family:$defaultFontFamily;font-size:${defaultFontSize}px }", numStyles)
            insertRule("body pre { overflow:visible }", numStyles)
            insertRule("body div { display:inline }", numStyles)

            insertRule("pre { margin:0 }", numStyles)
            insertRule("svg { display:inline-block;width:100%;height:100% }", numStyles)
            insertRule("svg * { position:absolute }", numStyles)

            try {
                insertRule("input[type=text]::-ms-clear{ display:none }", numStyles)
            } catch (ignore: Throwable) {}
        }
    }
}