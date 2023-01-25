package io.nacular.doodle.system.impl

import io.nacular.doodle.HTMLAnchorElement
import io.nacular.doodle.HTMLButtonElement
import io.nacular.doodle.HTMLElement
import io.nacular.doodle.HTMLInputElement
import io.nacular.doodle.dom.EventTarget
import io.nacular.doodle.dom.hasScrollOverflow


@Suppress("USELESS_IS_CHECK")
internal fun isNativeElement(target: EventTarget?) = target is HTMLElement && (
    target.getAttribute("data-native") != null ||
    target is HTMLButtonElement                ||
    target is HTMLInputElement                 ||
    target is HTMLAnchorElement
)

@Suppress("USELESS_IS_CHECK")
internal fun nativeScrollPanel(target: EventTarget?) = target is HTMLElement && target.hasScrollOverflow && target.getAttribute("data-native") != null