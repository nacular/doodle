package io.nacular.doodle.system.impl

import io.nacular.doodle.dom.HTMLAnchorElement
import io.nacular.doodle.dom.HTMLButtonElement
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HTMLInputElement
import io.nacular.doodle.dom.EventTarget
import io.nacular.doodle.dom.hasScrollOverflow


@Suppress("USELESS_IS_CHECK")
internal fun isNativeElement(target: EventTarget?) = target is HTMLElement && (
    target.getAttribute("data-native") != null ||
    target is HTMLButtonElement ||
    target is HTMLInputElement ||
    target is HTMLAnchorElement
)

@Suppress("USELESS_IS_CHECK")
internal fun nativeScrollPanel(target: EventTarget?) = target is HTMLElement && target.hasScrollOverflow && target.getAttribute("data-native") != null