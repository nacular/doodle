package io.nacular.doodle.dom

import io.nacular.doodle.CSSStyleSheet
import io.nacular.doodle.Document
import io.nacular.doodle.HTMLMetaElement
import io.nacular.doodle.HTMLStyleElement
import io.nacular.doodle.numStyles

/**
 * Created by Nicholas Eddy on 10/31/17.
 */
internal interface SystemStyler {
    fun shutdown()
}

internal class SystemStylerImpl(htmlFactory: HtmlFactory, private val document: Document, allowDefaultDarkMode: Boolean): SystemStyler {
    private val style: HTMLStyleElement = htmlFactory.create("style")

    private val meta: HTMLMetaElement?  = when(htmlFactory.root) {
        document.body -> htmlFactory.create<HTMLMetaElement>("meta" ).apply {
            name    = "viewport"
            content = "width=device-width, initial-scale=1"
        }
        else -> null
    }

    private val id = when(htmlFactory.root) {
        document.body -> null
        else          -> "#${when (val i = htmlFactory.root.id) {
            ""        -> "__doodle__${currentId++}".also { htmlFactory.root.id = it }
            else      -> i
        }}"
    }

    private fun prefix(fallback: String = "") = id ?: fallback

    init {
        meta?.let { document.head?.insert(it, 0) }

        document.head?.insert(style, 0)

        (style.sheet as? CSSStyleSheet)?.apply {
            if (allowDefaultDarkMode) {
                insertRule(":root {color-scheme:light dark}", numStyles)
            }

            // Disable selection: https://stackoverflow.com/questions/826782/how-to-disable-text-selection-highlighting#4407335
            insertRule("${prefix("body")} { -webkit-touch-callout:none;-webkit-user-select:none;-khtml-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none }", numStyles)

            insertRule("${prefix("html")} { border:0;box-sizing:border-box }", numStyles)
            insertRule("${prefix("body")} { height:100%;width:100%;overflow:hidden;cursor:default;margin:0;padding:0;font-weight:$defaultFontSize;font-family:$defaultFontFamily;font-size:${defaultFontSize}px }", numStyles)
            insertRule("html { height:100%;width:100% }", numStyles)

            insertRule("${prefix()} * { box-sizing:inherit }", numStyles)

            insertRule("${prefix("body")} * { position:absolute;overflow:hidden;font-weight:$defaultFontSize;font-family:$defaultFontFamily;font-size:${defaultFontSize}px }", numStyles)
            insertRule("${prefix("body")} pre { overflow:visible }", numStyles)
            insertRule("${prefix("body")} div { display:inline }", numStyles)

            insertRule("${prefix("body")} div:focus { outline:none }", numStyles)

            insertRule("${prefix()} pre { margin:0 }", numStyles)
            insertRule("${prefix()} svg { display:inline-block;width:100%;height:100%;overflow:visible }", numStyles)
            insertRule("${prefix()} svg * { position:absolute }", numStyles)

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

    private companion object {
        private var currentId = 0
    }
}