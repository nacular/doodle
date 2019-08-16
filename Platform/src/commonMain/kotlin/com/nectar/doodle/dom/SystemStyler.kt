package com.nectar.doodle.dom

import com.nectar.doodle.CSSStyleSheet
import com.nectar.doodle.Document
import com.nectar.doodle.get

/**
 * Created by Nicholas Eddy on 10/31/17.
 */
interface SystemStyler

internal class SystemStylerImpl(htmlFactory: HtmlFactory, document: Document): SystemStyler {
    // FIXME: Make these styles local and applicable to the root not instead of assuming document.body
    init {
        if (document.styleSheets.length == 0) {
            document.head?.appendChild(htmlFactory.create("style"))
        }

        (document.styleSheets[0] as? CSSStyleSheet)?.apply {
            // Disable selection: https://stackoverflow.com/questions/826782/how-to-disable-text-selection-highlighting#4407335
            insertRule("body{ -webkit-touch-callout:none;-webkit-user-select:none;-khtml-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none }", 0)

            insertRule("html { border:0;box-sizing:border-box }", 0)
            insertRule("html,body{ height:100%;width:100%;overflow:hidden;cursor:default;margin:0;padding:0 }", 0)

            insertRule("* { box-sizing:inherit }", 0)

            insertRule("body * { position:absolute;overflow:hidden;font-weight:$defaultFontSize;font-family:$defaultFontFamily;font-size:${defaultFontSize}px }", 0)
            insertRule("body pre { overflow:visible }", 0)
            insertRule("body div { display:inline }", 0)

            insertRule("pre { margin:0 }", 0)
            insertRule("svg { display:inline-block;width:100%;height:100% }", 0)
            insertRule("svg * { position:absolute }", 0)

            try {
                insertRule("input[type=text]::-ms-clear{ display:none }", 0)
            } catch (ignore: Throwable) {}
        }
    }
}