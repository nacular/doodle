package com.nectar.doodle.dom

import org.w3c.dom.css.get
import kotlin.browser.document

/**
 * Created by Nicholas Eddy on 10/31/17.
 */
interface SystemStyler

class SystemStylerImpl: SystemStyler {
    init {
        document.styleSheets[0].asDynamic().insertRule("body * { position:absolute;overflow:hidden }")

        document.styleSheets[0].asDynamic().insertRule("pre { margin:0 }")
        document.styleSheets[0].asDynamic().insertRule("svg { display:inline-block;width:100%;height:100% }")
        document.styleSheets[0].asDynamic().insertRule("svg * { position:absolute }")
    }
}