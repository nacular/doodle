package com.nectar.doodle.dom

import com.nectar.doodle.CSSStyleSheet
import com.nectar.doodle.Document
import com.nectar.doodle.HTMLStyleElement
import com.nectar.doodle.numStyles

/**
 * Created by Nicholas Eddy on 10/31/17.
 */
interface SystemStyler {
    fun shutdown()
}

internal class SystemStylerImpl(htmlFactory: HtmlFactory, private val document: Document, allowDefaultDarkMode: Boolean): SystemStyler {
    private val style: HTMLStyleElement = htmlFactory.create("style")

    // FIXME: Make these styles local and applicable to the root not instead of assuming document.body
    init {
        document.head?.insert(style, 0)

        (style.sheet as? CSSStyleSheet)?.apply {
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

            insertRule("body div:focus { outline:none }", numStyles)

            insertRule("pre { margin:0 }", numStyles)
            insertRule("svg { display:inline-block;width:100%;height:100%;overflow:visible }", numStyles)
            insertRule("svg * { position:absolute }", numStyles)

//            insertRule(".custom-button { border:none;outline:none;user-select:none;padding:0 }", numStyles)
//            insertRule("button * { top:0;left:0 }", numStyles)

            try {
                insertRule("input[type=text]::-ms-clear{ display:none }", numStyles)
            } catch (ignore: Throwable) {}
        }
    }

    override fun shutdown() {
        document.head?.remove(style)
    }
}