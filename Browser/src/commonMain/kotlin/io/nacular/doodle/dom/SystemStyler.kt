package io.nacular.doodle.dom

import io.nacular.doodle.CSSStyleSheet
import io.nacular.doodle.Document
import io.nacular.doodle.HTMLMetaElement
import io.nacular.doodle.HTMLStyleElement
import io.nacular.doodle.dom.SystemStyler.Style
import io.nacular.doodle.numStyles
import io.nacular.doodle.utils.IdGenerator

/**
 * Created by Nicholas Eddy on 10/31/17.
 */
internal interface SystemStyler {
    interface Style {
        var css: String

        fun delete()
    }

    fun insertRule(css: String): Style?

    fun shutdown()
}

internal class SystemStylerImpl(
        htmlFactory: HtmlFactory,
        idGenerator: IdGenerator,
        private val document: Document,
        isNested: Boolean,
        allowDefaultDarkMode: Boolean
): SystemStyler {
    private val style: HTMLStyleElement = htmlFactory.create("style")

    private val meta: HTMLMetaElement?  = when(htmlFactory.root) {
        document.body -> htmlFactory.create<HTMLMetaElement>("meta").apply {
            name    = "viewport"
            content = "width=device-width, initial-scale=1"
        }
        else -> null
    }

    private val id = when(htmlFactory.root) {
        document.body -> null
        else          -> "#${when (val i = htmlFactory.root.id) {
            "" -> idGenerator.nextId().also { htmlFactory.root.id = it }
            else      -> i
        }}"
    }

    private fun prefix(fallback: String = "") = id ?: fallback

    private val sheet: CSSStyleSheet?

    init {
        meta?.let { document.head?.insert(it, 0) }

        document.head?.insert(style, 0)

        sheet = style.sheet as? CSSStyleSheet?

        if (!isNested) {
            sheet?.apply {
                if (allowDefaultDarkMode) {
                    insertRule(":root {color-scheme:light dark}", numStyles)
                }

                // Disable selection: https://stackoverflow.com/questions/826782/how-to-disable-text-selection-highlighting#4407335
                insertRule("${prefix("body")} { -webkit-tap-highlight-color: transparent;-webkit-touch-callout:none;-webkit-user-select:none;-khtml-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale }", numStyles)

                insertRule("${prefix("html")} { border:0;box-sizing:border-box }", numStyles)
                insertRule("${prefix("body")} { height:100%;width:100%;overflow:hidden;cursor:default;margin:0;padding:0;font-weight:$defaultFontWeight;font-family:$defaultFontFamily;font-size:${defaultFontSize}px }", numStyles)
                insertRule("html { height:100%;width:100% }", numStyles)

                insertRule("${prefix()} * { box-sizing:inherit }", numStyles)

                insertRule("${prefix("body")} * { position:absolute;overflow:hidden;font-weight:$defaultFontSize;font-family:$defaultFontFamily;font-size:${defaultFontSize}px }", numStyles)
                insertRule("${prefix("body")} pre { overflow:visible }",   numStyles)
                insertRule("${prefix("body")} div { display:inline }",     numStyles)
                insertRule("${prefix("body")} div:focus { outline:none }", numStyles)
                insertRule("${prefix("body")} b { pointer-events:none }",  numStyles)

                insertRule("${prefix()} pre { margin:0;pointer-events:none }", numStyles)
                insertRule("${prefix()} svg { display:inline-block;width:100%;height:100%;overflow:visible;pointer-events:none }", numStyles)
                insertRule("${prefix()} svg * { position:absolute }", numStyles)
                insertRule("${prefix()} button div svg { left:0px }", numStyles)

                try {
                    insertRule("input[type=text]::-ms-clear{ display:none }", numStyles)
                } catch (ignore: Throwable) {
                }
            }
        }
    }

    private val ruleIndexes = mutableListOf<Int>()

    override fun insertRule(css: String): Style? = sheet?.run {
        try {
            val offset = ruleIndexes.size
            val styleIndex = insertRule(css, numStyles)

            ruleIndexes += styleIndex

            val sheet = this

            cssRules.item(styleIndex)?.let { rule ->
                object : Style {
                    override var css
                        get() = rule.cssText
                        set(new) {
                            rule.cssText = new
                        }

                    override fun delete() {
                        sheet.deleteRule(ruleIndexes[offset])

                        ruleIndexes[offset] = -1

                        for (i in offset + 1 until ruleIndexes.size) {
                            ruleIndexes[i] = ruleIndexes[i] - 1
                        }
                    }
                }
            }
        } catch (ignored: Throwable) { null }
    }

    override fun shutdown() {
        document.head?.remove(style)
    }
}