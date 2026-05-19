package io.nacular.doodle.system.impl

import io.nacular.doodle.dom.EventTarget
import io.nacular.doodle.dom.asHTMLElementOrNull
import io.nacular.doodle.dom.hasScrollOverflow
import io.nacular.doodle.dom.isHTMLAnchorElement
import io.nacular.doodle.dom.isHTMLButtonElement
import io.nacular.doodle.dom.isHTMLInputElement


@Suppress("USELESS_IS_CHECK")
internal fun isNativeElement(target: EventTarget?) = target != null && (
    target.asHTMLElementOrNull()?.getAttribute("data-native") != null ||
    target.isHTMLButtonElement() ||
    target.isHTMLInputElement () ||
    target.isHTMLAnchorElement()
)

@Suppress("USELESS_IS_CHECK")
internal fun nativeScrollPanel(target: EventTarget?) = target?.asHTMLElementOrNull()?.let {
    it.hasScrollOverflow && it.getAttribute("data-native") != null
} ?: false