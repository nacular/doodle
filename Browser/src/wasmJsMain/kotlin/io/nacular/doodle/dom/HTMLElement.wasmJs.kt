package io.nacular.doodle.dom

internal actual fun HTMLElement.addActiveEventListener   (to: String, listener: (Event) -> Unit) = addEventListener   (to, listener, jsObject { passive = false })
internal actual fun HTMLElement.removeActiveEventListener(to: String, listener: (Event) -> Unit) = removeEventListener(to, listener, jsObject { passive = false })

internal actual inline fun HTMLInputElement.focusInput() {
    focus(jsObject { preventScroll = true })
}

internal actual fun ResizeObserver.observeResize(target: Node, box: String) {
    this.observe(target, jsObject { this.box = box })
}

internal actual fun CSSStyleSheet.tryInsertRule(
    rule: String,
    index: Int
): Int = tryInsertRule(this, rule, index)

//@JsFun("(sheet, rule, index) => { try { sheet.insertRule(rule, index); } catch (error) { -1 /*throw Error();*/ } }")
private fun tryInsertRule(sheet: CSSStyleSheet, rule: String, index: Int): Int = js("""{
    try {
        return sheet.insertRule(rule, index);
    } catch (error) {
        return -1;
    }
}""")